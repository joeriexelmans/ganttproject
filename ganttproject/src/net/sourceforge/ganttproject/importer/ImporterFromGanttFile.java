/*
GanttProject is an opensource project management tool.
Copyright (C) 2010-2011 Dmitry Barashev, GanttProject team

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sourceforge.ganttproject.importer;

import biz.ganttproject.core.calendar.ImportCalendarOption;
import biz.ganttproject.core.option.ChangeValueEvent;
import biz.ganttproject.core.option.ChangeValueListener;
import biz.ganttproject.core.option.GPOption;
import net.sourceforge.ganttproject.CustomPropertyDefinition;
import net.sourceforge.ganttproject.CustomPropertyManager;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.document.Document;
import net.sourceforge.ganttproject.document.Document.DocumentException;
import net.sourceforge.ganttproject.document.FileDocument;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.project.IProject;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.resource.HumanResourceMerger;
import net.sourceforge.ganttproject.resource.HumanResourceMerger.MergeResourcesOption;
import net.sourceforge.ganttproject.resource.OverwritingMerger;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManagerImpl;
import net.sourceforge.ganttproject.task.algorithm.AlgorithmCollection;
import org.osgi.service.prefs.Preferences;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ImporterFromGanttFile extends ImporterBase {
  private final HumanResourceMerger.MergeResourcesOption myMergeResourcesOption = new HumanResourceMerger.MergeResourcesOption();
  private final ImportCalendarOption myImportCalendarOption = new ImportCalendarOption();

  private final GPOption[] myOptions = new GPOption[]{myMergeResourcesOption, myImportCalendarOption};

  public ImporterFromGanttFile() {
    super("ganttprojectFiles");
    myMergeResourcesOption.loadPersistentValue(HumanResourceMerger.MergeResourcesOption.BY_ID);
    myImportCalendarOption.setSelectedValue(ImportCalendarOption.Values.NO);
  }

  @Override
  public String getFileNamePattern() {
    return "xml|gan";
  }

  @Override
  protected GPOption[] getOptions() {
    return myOptions;
  }

  @Override
  public void setContext(IGanttProject app, IProject project, UIFacade uiFacade, Preferences preferences) {
    super.setContext(app, project, uiFacade, preferences);
    final Preferences node = preferences.node("/instance/net.sourceforge.ganttproject/import");
    myMergeResourcesOption.lock();
    myMergeResourcesOption.loadPersistentValue(node.get(myMergeResourcesOption.getID(),
        HumanResourceMerger.MergeResourcesOption.BY_ID));
    myMergeResourcesOption.commit();
    myMergeResourcesOption.addChangeValueListener(new ChangeValueListener() {
      @Override
      public void changeValue(ChangeValueEvent event) {
        node.put(myMergeResourcesOption.getID(), String.valueOf(event.getNewValue()));
      }
    });
  }

  @Override
  public void run() {
    final File selectedFile = getFile();
    final IProject targetProject = getProject();
    final BufferProject bufferProject = createBufferProject(targetProject, getUiFacade());
    getUiFacade().getUndoManager().undoableEdit("Import", new Runnable() {
      @Override
      public void run() {
        try {
          Document document = getApp().getDocumentManager().getDocument(selectedFile.getAbsolutePath());
          AlgorithmCollection algs = bufferProject.getTaskManager().getAlgorithmCollection();
          try {
            algs.getScheduler().setEnabled(false);
            algs.getRecalculateTaskScheduleAlgorithm().setEnabled(false);
            algs.getAdjustTaskBoundsAlgorithm().setEnabled(false);
            document.read(bufferProject);
          } finally {
            algs.getRecalculateTaskScheduleAlgorithm().setEnabled(true);
            algs.getAdjustTaskBoundsAlgorithm().setEnabled(true);
            algs.getScheduler().setEnabled(true);
          }

          importBufferProject(targetProject, bufferProject, getUiFacade(), myMergeResourcesOption, myImportCalendarOption);
        } catch (DocumentException e) {
          getUiFacade().showErrorDialog(e);
        } catch (IOException e) {
          getUiFacade().showErrorDialog(e);
        }
      }
    });
  }

  private BufferProject createBufferProject(final IProject targetProject, final UIFacade uiFacade) {
    return new BufferProject(targetProject);
  }

  protected Document getDocument(File selectedFile) {
    return new FileDocument(selectedFile);
  }

  public static Map<Task, Task> importProject(IProject targetProject, IProject sourceProject, MergeResourcesOption mergeOption, ImportCalendarOption importCalendarOption) {
    targetProject.getRoleManager().importData(sourceProject.getRoleManager());
    if (importCalendarOption != null) {
      targetProject.getActiveCalendar().importCalendar(sourceProject.getActiveCalendar(), importCalendarOption);
    }
    {
      CustomPropertyManager targetResCustomPropertyMgr = targetProject.getResourceCustomPropertyManager();
      targetResCustomPropertyMgr.importData(sourceProject.getResourceCustomPropertyManager());
    }
    Map<HumanResource, HumanResource> original2ImportedResource = targetProject.getHumanResourceManager().importData(
            sourceProject.getHumanResourceManager(), new OverwritingMerger(mergeOption));

    Map<Task, Task> result = null;
    {
      CustomPropertyManager targetCustomColumnStorage = targetProject.getTaskCustomPropertyManager();
      Map<CustomPropertyDefinition, CustomPropertyDefinition> that2thisCustomDefs = targetCustomColumnStorage.importData(sourceProject.getTaskCustomPropertyManager());
      TaskManagerImpl origTaskManager = (TaskManagerImpl) targetProject.getTaskManager();
      try {
        origTaskManager.setEventsEnabled(false);
        result = origTaskManager.importData(sourceProject.getTaskManager(), that2thisCustomDefs);
        origTaskManager.importAssignments(sourceProject.getTaskManager(), targetProject.getHumanResourceManager(),
                result, original2ImportedResource);
      } finally {
        origTaskManager.setEventsEnabled(true);
      }
    }
    return result;
  }

  public static Map<Task, Task> importBufferProject(IProject targetProject, BufferProject bufferProject, UIFacade uiFacade, MergeResourcesOption mergeOption, ImportCalendarOption importCalendarOption) {
    Map<Task,Task> result = importProject(targetProject, bufferProject, mergeOption, importCalendarOption);
    uiFacade.refresh();
    uiFacade.getTaskTree().getVisibleFields().importData(bufferProject.getTaskVisibleFields());
    uiFacade.getResourceTree().getVisibleFields().importData(bufferProject.getResourceVisibleFields());
    return result;
  }
}
