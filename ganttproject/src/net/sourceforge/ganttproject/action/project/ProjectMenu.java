/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2005-2012 GanttProject Team

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
package net.sourceforge.ganttproject.action.project;

import biz.ganttproject.storage.StorageDialogAction;
import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.action.GPAction;
import net.sourceforge.ganttproject.document.DocumentsMRU;
import net.sourceforge.ganttproject.document.webdav.WebDavStorageImpl;
import net.sourceforge.ganttproject.project.IProject;

import javax.swing.*;

/**
 * Collection of actions present in the project menu
 */
public class ProjectMenu extends JMenu {

  private final NewProjectAction myNewProjectAction;
  private final SaveProjectAction mySaveProjectAction;
  private final PrintAction myPrintAction;
  private final DocumentsMRU myMRU;
  private final OpenProjectAction myOpenProjectAction;

  public ProjectMenu(final GanttProject app, IProject project, DocumentsMRU mru, String key) {
    super(GPAction.createVoidAction(key));
    myNewProjectAction = new NewProjectAction(app);
    mySaveProjectAction = new SaveProjectAction(app);
    myPrintAction = new PrintAction(app);
    myMRU = mru;

    ProjectPropertiesAction projectSettingsAction = new ProjectPropertiesAction(app, project);
    myOpenProjectAction = new OpenProjectAction(app, app.getProjectUIFacade());
    SaveProjectAsAction saveProjectAsAction = new SaveProjectAsAction(app);
//    OpenURLAction openURLAction = new OpenURLAction(project.getProject(), project.getUIFacade(),
//        project.getProjectUIFacade());
//    SaveURLAction saveURLAction = new SaveURLAction(project.getProject(), project.getUIFacade(),
//        project.getProjectUIFacade());
    ExitAction exitAction = new ExitAction(app);
    ProjectImportAction projectImportAction = new ProjectImportAction(app.getUIFacade(), app, project);
    ProjectExportAction projectExportAction = new ProjectExportAction(app.getUIFacade(), app, project,
        app.getGanttOptions().getPluginPreferences());

    WebDavStorageImpl webdavStorage = (WebDavStorageImpl) app.getDocumentManager().getWebDavStorageUi();
    StorageDialogAction cloudDialogAction = new StorageDialogAction(
        app, app.getProjectUIFacade(), app.getDocumentManager(), myMRU, webdavStorage.getServersOption());
    add(cloudDialogAction);
    add(projectSettingsAction);
    add(myNewProjectAction);
    //add(myOpenProjectAction);
    //add(mru);

    //addSeparator();
    //add(mySaveProjectAction);
    //add(saveProjectAsAction);
    //addSeparator();

    add(projectImportAction);
    add(projectExportAction);
    addSeparator();

//    JMenu mServer = UIUtil.createTooltiplessJMenu(GPAction.createVoidAction("webServer"));
//    mServer.add(openURLAction);
//    mServer.add(saveURLAction);
//    add(mServer);

    add(myPrintAction);
    add(new ProjectPreviewAction(app));
    addSeparator();
    add(exitAction);
    setToolTipText(null);
  }

  @Override
  public JMenuItem add(Action a) {
    a.putValue(Action.SHORT_DESCRIPTION, null);
    return super.add(a);
  }

  public AbstractAction getNewProjectAction() {
    return myNewProjectAction;
  }

  public SaveProjectAction getSaveProjectAction() {
    return mySaveProjectAction;
  }

  public AbstractAction getPrintAction() {
    return myPrintAction;
  }

  public OpenProjectAction getOpenProjectAction() {
    return myOpenProjectAction;
  }

}
