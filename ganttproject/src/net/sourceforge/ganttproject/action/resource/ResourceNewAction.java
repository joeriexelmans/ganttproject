/*
GanttProject is an opensource project management tool.
Copyright (C) 2011 GanttProject Team

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
package net.sourceforge.ganttproject.action.resource;

import net.sourceforge.ganttproject.action.GPAction;
import net.sourceforge.ganttproject.assignment.AssignmentManager;
import net.sourceforge.ganttproject.gui.GanttDialogPerson;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.UIUtil;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.task.TaskManager;

import java.awt.event.ActionEvent;

/**
 * Action connected to the menu item for insert a new resource
 */
public class ResourceNewAction extends ResourceAction {
  private final UIFacade myUIFacade;
  private final Project myProject;

  public ResourceNewAction(Project project, UIFacade uiFacade) {
    super("resource.new", project.getHumanResourceManager());
    myUIFacade = uiFacade;
    myProject = project;
  }

  private ResourceNewAction(Project project, UIFacade uiFacade, IconSize size) {
    super("resource.new", project.getHumanResourceManager(), null, size);
    myUIFacade = uiFacade;
    myProject = project;
  }

  @Override
  public GPAction withIcon(IconSize size) {
    return new ResourceNewAction(myProject, myUIFacade, size);
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    if (calledFromAppleScreenMenu(event)) {
      return;
    }
    final HumanResource resource = getManager().newHumanResource();
    resource.setRole(myProject.getRoleManager().getDefaultRole());
    GanttDialogPerson dp = new GanttDialogPerson(getManager().getCustomPropertyManager(), myProject.getTaskManager(), myProject.getAssignmentManager(), myUIFacade, resource);
    dp.setVisible(true);
    if (dp.result()) {
      myUIFacade.getUndoManager().undoableEdit(getLocalizedDescription(), new Runnable() {
        @Override
        public void run() {
          getManager().add(resource);
          myUIFacade.getResourceTree().setSelected(resource, true);
        }
      });
    }
  }

  @Override
  public void updateAction() {
    super.updateAction();
  }

  @Override
  public ResourceNewAction asToolbarAction() {
    ResourceNewAction result = new ResourceNewAction(myProject, myUIFacade);
    result.setFontAwesomeLabel(UIUtil.getFontawesomeLabel(result));
    return result;
  }
}
