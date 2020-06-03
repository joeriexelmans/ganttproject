/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2010 Dmitry Barashev

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
package net.sourceforge.ganttproject.gui.taskproperties;

import net.sourceforge.ganttproject.assignment.AssignmentManager;
import net.sourceforge.ganttproject.gui.TableModelExt;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.roles.Role;
import net.sourceforge.ganttproject.assignment.LocalAssignment;
import net.sourceforge.ganttproject.task.ResourceAssignmentMutator;
import net.sourceforge.ganttproject.task.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Table model of a table of resources assigned to a specific task.
 *
 * @author dbarashev (Dmitry Barashev)
 */
class ResourcesTableModel extends TableModelExt<LocalAssignment>
{

  static enum Column {
    ID("id", String.class), NAME("resourcename", String.class), UNIT("unit", String.class), COORDINATOR("coordinator",
        Boolean.class), ROLE("role", String.class);

    private final String myName;
    private final Class<?> myClass;

    Column(String key, Class<?> clazz) {
      myName = GanttLanguage.getInstance().getText(key);
      myClass = clazz;
    }

    String getName() {
      return myName;
    }

    Class<?> getColumnClass() {
      return myClass;
    }
  }

  private final AssignmentManager myAssignmentManager;
  private final AssignmentManager.Mutator myMutator;
  private final Task myTask;
  private final List<LocalAssignment> myAssignments;
//  private final ResourceAssignmentMutator myMutator;
  private boolean isChanged = false;

  public ResourcesTableModel(AssignmentManager assignmentManager, Task task) {
    myAssignmentManager = assignmentManager;
    myMutator = myAssignmentManager.createMutator();
    myTask = task;

    myAssignments = new ArrayList(myAssignmentManager.getTaskAssignments(task)); // copy
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return Column.values()[columnIndex].getColumnClass();
  }

  @Override
  public int getColumnCount() {
    return Column.values().length;
  }

  @Override
  public int getRowCount() {
    return myAssignments.size() + 1;
  }

  @Override
  public String getColumnName(int col) {
    return Column.values()[col].getName();
  }

  @Override
  public Object getValueAt(int row, int col) {
    Object result;
    if (row >= 0) {
      if (row < myAssignments.size()) {
        LocalAssignment assignment = myAssignments.get(row);
        switch (col) {
        case 0:
          result = String.valueOf(assignment.getResource().getId());
          break;
        case 1:
          result = assignment.getResource();
          break;
        case 2:
          result = String.valueOf(assignment.getLoad());
          break;
        case 3:
          result = new Boolean(assignment.isCoordinator());
          break;
        case 4:
          result = assignment.getRoleForAssignment();
          break;
        default:
          result = "";
        }
      } else {
        result = null;
      }
    } else {
      throw new IllegalArgumentException("I can't return data in row=" + row);
    }
    return result;
  }

  @Override
  public boolean isCellEditable(int row, int col) {
    boolean result = col > 0;
    if (result) {
      result = (col == 2 ? row < myAssignments.size() : row <= myAssignments.size()) || col == 3 || col == 4;
    }
    return result;
  }

  @Override
  public void setValueAt(Object value, int row, int col) {
    if (row >= 0) {
      if (row >= myAssignments.size()) {
        createAssignment(value);
      } else {
        updateAssignment(value, row, col);
      }
    } else {
      throw new IllegalArgumentException("I can't set data in row=" + row);
    }
    isChanged = true;
  }

  private void updateAssignment(Object value, int row, int col) {
    LocalAssignment updateTarget = myAssignments.get(row);
    switch (col) {
    case 4: {
      updateTarget.setRoleForAssignment((Role) value);
      break;
    }
    case 3: {
      updateTarget.setCoordinator(((Boolean) value).booleanValue());
      break;
    }
    case 2: {
      float loadAsFloat = Float.parseFloat(String.valueOf(value));
      updateTarget.setLoad(loadAsFloat);
      break;
    }
    case 1: {
      if (value == null) {
        myMutator.removeAssignment(myTask, updateTarget.getResource());
//        updateTarget.delete();
        myAssignments.remove(row);
        fireTableRowsDeleted(row, row);
      } else if (value instanceof HumanResource) {
        myMutator.removeAssignment(myTask, updateTarget.getResource());
        float load = updateTarget.getLoad();
        boolean coord = updateTarget.isCoordinator();
//        updateTarget.delete();
//        myMutator.deleteAssignment(updateTarget.getResource());
//        LocalAssignment newAssignment = myMutator.addAssignment((HumanResource) value);
        LocalAssignment newAssignment = myMutator.createAssignment(myTask, (HumanResource) value);
        newAssignment.setLoad(load);
        newAssignment.setCoordinator(coord);
        myAssignments.set(row, newAssignment);
      }
      break;

    }
    default:
      break;
    }
  }

  private void createAssignment(Object value) {
    if (value instanceof HumanResource) {
//      LocalAssignment newAssignment = myMutator.addAssignment((HumanResource) value);
      LocalAssignment newAssignment = myMutator.createAssignment(myTask, (HumanResource) value);
      newAssignment.setLoad(100);

      boolean coord = false;
      if (myAssignments.isEmpty())
        coord = true;
      newAssignment.setCoordinator(coord);
      newAssignment.setRoleForAssignment(newAssignment.getResource().getRole());
      myAssignments.add(newAssignment);
      fireTableRowsInserted(myAssignments.size(), myAssignments.size());
    }
  }

  public List<LocalAssignment> getResourcesAssignments() {
    return Collections.unmodifiableList(myAssignments);
  }

  public void commit() {
    myMutator.commit();
  }

  public boolean isChanged() {
    return isChanged;
  }

  public void delete(int[] selectedRows) {
    List<LocalAssignment> selected = new ArrayList<LocalAssignment>();
    for (int row : selectedRows) {
      if (row < myAssignments.size()) {
        selected.add(myAssignments.get(row));
      }
    }
    for (LocalAssignment ra : selected) {
      myAssignmentManager.removeAssignment(myTask, ra.getResource());
//      ra.delete();
    }
    myAssignments.removeAll(selected);
    fireTableDataChanged();
  }

  @Override
  public List<LocalAssignment> getAllValues() {
    return myAssignments;
  }

}
