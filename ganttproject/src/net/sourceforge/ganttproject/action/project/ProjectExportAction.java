/*
GanttProject is an opensource project management tool.
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

import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.action.GPAction;
import net.sourceforge.ganttproject.export.ExportFileWizardImpl;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.projectwizard.WizardImpl;
import net.sourceforge.ganttproject.project.IProject;
import org.osgi.service.prefs.Preferences;

import java.awt.event.ActionEvent;

/**
 * @author bard
 */
public class ProjectExportAction extends GPAction {
  private final IGanttProject myApp;

  private final IProject myProject;

  private final UIFacade myUIFacade;

  private Preferences myPluginPrerences;

  public ProjectExportAction(UIFacade uiFacade, IGanttProject app, IProject project, Preferences pluginPrerences) {
    super("project.export");
    myApp = app;
    myProject = project;
    myUIFacade = uiFacade;
    myPluginPrerences = pluginPrerences;
  }

  @Override
  protected String getIconFilePrefix() {
    return "export_";
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (calledFromAppleScreenMenu(e)) {
      return;
    }
    WizardImpl wizard = new ExportFileWizardImpl(myUIFacade, myApp, myProject, myPluginPrerences);
    wizard.show();
  }
}
