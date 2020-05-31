/*
GanttProject is an opensource project management tool.
Copyright (C) 2005-2011 GanttProject team

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
package net.sourceforge.ganttproject.gui;

import java.io.IOException;

import biz.ganttproject.core.option.GPOptionGroup;

import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.document.Document;
import net.sourceforge.ganttproject.document.Document.DocumentException;
import net.sourceforge.ganttproject.project.Project;

public interface ProjectUIFacade {
  /**
   * Attempt to save project (overwrite), possibly showing dialog(s) if additional input required (untitled document, signin, ...)
   */
  void saveProject(IGanttProject project);

  /**
   * Shows "save as" dialog
   */
  void saveProjectAsDialog(IGanttProject project);

  /**
   * Shows "save project?" confirmation dialog if there are changes.
   * Typically called before closing the current project.
   * @return Whether the caller may proceed closing the current project. False only if "cancel" was selected. True if there are no changes, or user decided to save or discard changes.
   */
  public boolean saveChangesDialog(IGanttProject project);

  /**
   * Shows dialog
   */
  void openProjectDialog(IGanttProject project) throws IOException, DocumentException;

  /**
   * Actual "open" logic
   */
  void openProject(Document document, IGanttProject project) throws IOException, DocumentException;

  /**
   * Shows new project wizard.
   */
  void createProjectWizard(IGanttProject app, Project project);

  GPOptionGroup[] getOptionGroups();
}
