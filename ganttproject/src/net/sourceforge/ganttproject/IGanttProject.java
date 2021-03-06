/*
GanttProject is an opensource project management tool.
Copyright (C) 2002-2011 GanttProject Team

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
package net.sourceforge.ganttproject;

import java.io.IOException;

import biz.ganttproject.core.time.TimeUnitStack;
import net.sourceforge.ganttproject.document.Document;
import net.sourceforge.ganttproject.document.DocumentManager;
import net.sourceforge.ganttproject.document.Document.DocumentException;
import net.sourceforge.ganttproject.gui.UIConfiguration;
import net.sourceforge.ganttproject.project.Project;

/**
 * This interface represents a project as a logical business entity, without any
 * UI (except some configuration options :)
 *
 * @author bard
 */
public interface IGanttProject {

  TimeUnitStack getTimeUnitStack();

  /**
   * Get the currently opened project.
   *
   * Deprecated because any parts of the code that depend on a Project should just get that project as a parameter.
   * This function was introduced to ease the refactoring of the GanttProject God class.
   */
  @Deprecated
  public Project getCurrentProject();

  // main app logic
  void open(Document document) throws IOException, DocumentException;
  void close();
  void setModified();
  void setModified(boolean modified);
  boolean isModified();

  // low level hacks?
  Document getDocument();
  void setDocument(Document document); // used by UndoableEditImpl
  DocumentManager getDocumentManager();

  void addProjectEventListener(ProjectEventListener listener);
  void removeProjectEventListener(ProjectEventListener listener);

  // UI
  UIConfiguration getUIConfiguration();
}