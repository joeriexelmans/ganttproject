/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2013 BarD Software s.r.o

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

import biz.ganttproject.core.table.ColumnList;
import net.sourceforge.ganttproject.ProjectStub;
import net.sourceforge.ganttproject.project.Project;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Buffer project is a target for importing functions, and when it is filled with
 * the imported data, it is merged into the real opened project.
 *
 * @author dbarashev
 */
public class BufferProject extends ProjectStub {
  private final ColumnList myTaskVisibleFields = new VisibleFieldsImpl();
  private final ColumnList myResourceVisibleFields = new VisibleFieldsImpl();

  public BufferProject(Project targetProject) {
    getTaskManager().getDependencyHardnessOption().setValue(targetProject.getTaskManager().getDependencyHardnessOption().getValue());
  }

  public ColumnList getTaskVisibleFields() {
    return myTaskVisibleFields;
  }

  public ColumnList getResourceVisibleFields() {
    return myResourceVisibleFields;
  }

  private static class TaskFieldImpl implements ColumnList.Column {
    private final String myID;
    private final int myOrder;
    private final int myWidth;

    TaskFieldImpl(String id, int order, int width) {
      myID = id;
      myOrder = order;
      myWidth = width;
    }

    @Override
    public SortOrder getSort() {
      return SortOrder.UNSORTED;
    }

    @Override
    public void setSort(SortOrder sort) {

    }

    @Override
    public String getID() {
      return myID;
    }

    @Override
    public int getOrder() {
      return myOrder;
    }

    @Override
    public int getWidth() {
      return myWidth;
    }

    @Override
    public boolean isVisible() {
      return true;
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public void setVisible(boolean visible) {
    }

    @Override
    public void setOrder(int order) {
    }

    @Override
    public void setWidth(int width) {
    }
  }

  public static class VisibleFieldsImpl implements ColumnList {
    private final List<Column> myFields = new ArrayList<Column>();

    @Override
    public void add(String name, int order, int width) {
      myFields.add(new TaskFieldImpl(name, order, width));
    }

    @Override
    public void clear() {
      myFields.clear();
    }

    @Override
    public Column getField(int index) {
      return myFields.get(index);
    }

    @Override
    public int getSize() {
      return myFields.size();
    }

    @Override
    public void importData(ColumnList source) {
      for (int i = 0; i < source.getSize(); i++) {
        Column nextField = source.getField(i);
        myFields.add(nextField);
      }
    }
  }
}
