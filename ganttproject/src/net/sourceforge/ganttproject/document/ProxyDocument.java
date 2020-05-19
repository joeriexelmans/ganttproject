/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2005-2011 GanttProject Team

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
package net.sourceforge.ganttproject.document;

import biz.ganttproject.core.calendar.GPCalendarCalc;
import biz.ganttproject.core.option.ListOption;
import biz.ganttproject.core.table.ColumnList;
import com.google.common.base.Preconditions;
import net.sourceforge.ganttproject.io.GanttXMLOpen;
import net.sourceforge.ganttproject.project.IProject;
import net.sourceforge.ganttproject.PrjInfos;
import net.sourceforge.ganttproject.gui.GPColorChooser;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.io.GPSaver;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.parser.*;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.TaskManagerImpl;
import org.eclipse.core.runtime.IStatus;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * @author bard
 */
public class ProxyDocument implements Document {
  private Document myPhysicalDocument;

  private IProject myProject;

  private PrjInfos myPrjInfos;

  private UIFacade myUIFacade;

  private final ParserFactory myParserFactory;

  private final DocumentCreator myCreator;

  private final ColumnList myTaskVisibleFields;

  private final ColumnList myResourceVisibleFields;

  ProxyDocument(DocumentCreator creator, Document physicalDocument, IProject project, PrjInfos prji, UIFacade uiFacade, ColumnList taskVisibleFields, ColumnList resourceVisibleFields, ParserFactory parserFactory) {
    myPhysicalDocument = physicalDocument;
    myProject = project;
    myPrjInfos = prji;
    myUIFacade = uiFacade;
    myParserFactory = parserFactory;
    myCreator = creator;
    myTaskVisibleFields = taskVisibleFields;
    myResourceVisibleFields = resourceVisibleFields;
  }

  public Document getRealDocument() {
    return myPhysicalDocument;
  }
  @Override
  public void setMirror(Document mirrorDocument) {
    myPhysicalDocument = Preconditions.checkNotNull(mirrorDocument);
  }

  @Override
  public String getFileName() {
    return myPhysicalDocument.getFileName();
  }

  @Override
  public boolean canRead() {
    return myPhysicalDocument.canRead();
  }

  @Override
  public IStatus canWrite() {
    return myPhysicalDocument.canWrite();
  }

  @Override
  public boolean isValidForMRU() {
    return myPhysicalDocument.isValidForMRU();
  }

  @Override
  public boolean acquireLock() {
    return myPhysicalDocument.acquireLock();
  }

  @Override
  public void releaseLock() {
    myPhysicalDocument.releaseLock();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return myPhysicalDocument.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return myPhysicalDocument.getOutputStream();
  }

  @Override
  public String getPath() {
    return myPhysicalDocument.getPath();
  }

  @Override
  public String getFilePath() {
    String result = myPhysicalDocument.getFilePath();
    if (result == null) {
      try {
        result = myCreator.createTemporaryFile();
      } catch (IOException e) {
        myUIFacade.showErrorDialog(e);
      }
    }
    return result;
  }

  @Override
  public String getUsername() {
    return myPhysicalDocument.getUsername();
  }

  @Override
  public String getPassword() {
    return myPhysicalDocument.getPassword();
  }

  @Override
  public String getLastError() {
    return myPhysicalDocument.getLastError();
  }

  @Override
  public void read() throws IOException, DocumentException {
    ParsingState parsing = new ParsingState();
    try {
      ((TaskManagerImpl) myProject.getTaskManager()).setEventsEnabled(false);
      parsing.enter();
    } finally {
      ((TaskManagerImpl) myProject.getTaskManager()).setEventsEnabled(true);
    }
  }

  @Override
  public void write() throws IOException {
    GPSaver saver = myParserFactory.newSaver();
    byte[] buffer;
    try {
      ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
      saver.save(bufferStream);
      bufferStream.flush();
      buffer = bufferStream.toByteArray();
    } catch (IOException e) {
      myUIFacade.showErrorDialog(e);
      return;
    }
    OutputStream output = getOutputStream();
    try {
      output.write(buffer);
      output.flush();
    } finally {
      output.close();
    }
  }

  class ParsingState {
    void enter() throws IOException, DocumentException {
      GPParser opener = new GanttXMLOpen();
      ParsingContext ctx = new ParsingContext();

      HumanResourceManager hrManager = myProject.getHumanResourceManager();
      RoleManager roleManager = myProject.getRoleManager();
      TaskManager taskManager = myProject.getTaskManager();
      GPCalendarCalc calendar = myProject.getActiveCalendar();

      ResourceTagHandler resourceHandler = new ResourceTagHandler(hrManager, roleManager,
          myProject.getResourceCustomPropertyManager());
      DependencyTagHandler dependencyHandler = new DependencyTagHandler(ctx, taskManager);
      AllocationTagHandler allocationHandler = new AllocationTagHandler(hrManager, taskManager, roleManager);
      VacationTagHandler vacationHandler = new VacationTagHandler(hrManager);
      PreviousStateTasksTagHandler previousStateHandler = new PreviousStateTasksTagHandler(myProject.getBaselines());
      RoleTagHandler rolesHandler = new RoleTagHandler(roleManager);
      TaskTagHandler taskHandler = new TaskTagHandler(taskManager, ctx);
      DefaultWeekTagHandler weekHandler = new DefaultWeekTagHandler(calendar);
      OnlyShowWeekendsTagHandler onlyShowWeekendsHandler = new OnlyShowWeekendsTagHandler(calendar);

      TaskPropertiesTagHandler taskPropHandler = new TaskPropertiesTagHandler(taskManager.getCustomPropertyManager());
      opener.addTagHandler(taskPropHandler);

      TaskDisplayColumnsTagHandler pilsenTaskDisplayHandler = TaskDisplayColumnsTagHandler.createPilsenHandler();
      TaskDisplayColumnsTagHandler legacyTaskDisplayHandler = TaskDisplayColumnsTagHandler.createLegacyHandler();

      opener.addTagHandler(pilsenTaskDisplayHandler);
      opener.addTagHandler(legacyTaskDisplayHandler);

      opener.addParsingListener(TaskDisplayColumnsTagHandler.createTaskDisplayColumnsWrapper(myUIFacade.getTaskTree().getVisibleFields(), pilsenTaskDisplayHandler, legacyTaskDisplayHandler));
      opener.addTagHandler(new ViewTagHandler("gantt-chart", myUIFacade, pilsenTaskDisplayHandler));

      TaskDisplayColumnsTagHandler resourceFieldsHandler = TaskDisplayColumnsTagHandler.createPilsenHandler();
      opener.addTagHandler(resourceFieldsHandler);

      opener.addParsingListener(TaskDisplayColumnsTagHandler.createTaskDisplayColumnsWrapper(myUIFacade.getResourceTree().getVisibleFields(), resourceFieldsHandler));
      opener.addTagHandler(new ViewTagHandler("resource-table", myUIFacade, resourceFieldsHandler));

      opener.addTagHandler(taskHandler);
      TaskParsingListener taskParsingListener = new TaskParsingListener(taskManager, myUIFacade.getTaskTree());
      opener.addParsingListener(taskParsingListener);

      CustomPropertiesTagHandler customPropHandler = new CustomPropertiesTagHandler(ctx, taskManager);
      opener.addTagHandler(customPropHandler);
      opener.addParsingListener(customPropHandler);

      opener.addTagHandler(new DescriptionTagHandler(myPrjInfos));
      opener.addTagHandler(new NotesTagHandler(ctx));
      opener.addTagHandler(new ProjectTagHandler(myPrjInfos));

      ProjectViewAttrsTagHandler projectViewAttrsTagHandler = new ProjectViewAttrsTagHandler(myUIFacade);
      opener.addTagHandler(projectViewAttrsTagHandler);
      opener.addParsingListener(projectViewAttrsTagHandler);

      opener.addTagHandler(new TasksTagHandler(taskManager));

      TimelineTagHandler timelineTagHandler = new TimelineTagHandler(myUIFacade, taskManager);
      opener.addTagHandler(timelineTagHandler);
      opener.addParsingListener(timelineTagHandler);

      opener.addTagHandler(resourceHandler);
      opener.addTagHandler(dependencyHandler);
      opener.addTagHandler(allocationHandler);
      opener.addParsingListener(allocationHandler);
      opener.addTagHandler(vacationHandler);
      opener.addTagHandler(previousStateHandler);
      opener.addTagHandler(rolesHandler);
      opener.addTagHandler(weekHandler);
      opener.addTagHandler(onlyShowWeekendsHandler);
      opener.addTagHandler(new OptionTagHandler<ListOption<Color>>(GPColorChooser.getRecentColorsOption()));
      opener.addParsingListener(dependencyHandler);
      opener.addParsingListener(resourceHandler);


      HolidayTagHandler holidayHandler = new HolidayTagHandler(calendar);
      opener.addTagHandler(new CalendarsTagHandler(calendar));
      opener.addTagHandler(holidayHandler);

      PortfolioTagHandler portfolioHandler = new PortfolioTagHandler(myCreator);
      opener.addTagHandler(portfolioHandler);

      InputStream is;
      try {
        is = getInputStream();
      } catch (IOException e) {
        throw new DocumentException(GanttLanguage.getInstance().getText("msg8") + ": " + e.getLocalizedMessage(), e);
      }
      opener.load(is);

      if (portfolioHandler.getDefaultDocument() != null) {
        portfolioHandler.getDefaultDocument().read();
      }
    }
  }

  @Override
  public URI getURI() {
    return myPhysicalDocument.getURI();
  }

  @Override
  public boolean isLocal() {
    return myPhysicalDocument.isLocal();
  }

  @Override
  public boolean equals(Object doc) {
    if (false == doc instanceof ProxyDocument) {
      return false;
    }
    return getPath().equals(((Document) doc).getPath());
  }
}
