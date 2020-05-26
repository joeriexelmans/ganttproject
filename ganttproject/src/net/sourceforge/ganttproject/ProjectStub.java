/*
GanttProject is an opensource project management tool.
Copyright (C) 2005-2011 Dmitry Barashev, GanttProject team

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

import biz.ganttproject.core.option.ColorOption;
import biz.ganttproject.core.option.DefaultColorOption;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
import net.sourceforge.ganttproject.gui.NotificationManager;
import net.sourceforge.ganttproject.gui.options.model.GP1XOptionConverter;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.task.TaskManagerConfig;

import java.awt.*;
import java.net.URL;

public class ProjectStub extends Project {
  public ProjectStub() {
    super(null, new TaskManagerConfigStub());
  }

  private static Color DEFAULT_TASK_COLOR = new Color(140, 182, 206);

  public static class TaskManagerConfigStub implements TaskManagerConfig {
    private final ColorOption myDefaultTaskColorOption;

    TaskManagerConfigStub() {
      myDefaultTaskColorOption = new DefaultTaskColorOption(DEFAULT_TASK_COLOR);
    }

    @Override
    public Color getDefaultColor() {
      return myDefaultTaskColorOption.getValue();
    }

    @Override
    public ColorOption getDefaultColorOption() {
      return myDefaultTaskColorOption;
    }

    @Override
    public URL getProjectDocumentURL() {
      // STUB
      return null;
    }

    @Override
    public NotificationManager getNotificationManager() {
      // STUB
      return null;
    }
  }

  static class DefaultTaskColorOption extends DefaultColorOption implements GP1XOptionConverter {
    DefaultTaskColorOption() {
      this(DEFAULT_TASK_COLOR);
    }

    private DefaultTaskColorOption(Color defaultColor) {
      super("taskDefaultColor", defaultColor);
    }

    @Override
    public String getTagName() {
      return "colors";
    }

    @Override
    public String getAttributeName() {
      return "tasks";
    }

    @Override
    public void loadValue(String legacyValue) {
      loadPersistentValue(legacyValue);
      commit();
    }
  }
}
