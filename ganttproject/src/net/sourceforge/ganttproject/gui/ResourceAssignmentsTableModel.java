/*
Copyright 2017 Oleg Kushnikov, BarD Software s.r.o

This file is part of GanttProject, an opensource project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.sourceforge.ganttproject.gui;

import net.sourceforge.ganttproject.assignment.AssignmentManager;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.assignment.LocalAssignment;
import net.sourceforge.ganttproject.task.ResourceAssignmentMutator;
import net.sourceforge.ganttproject.task.Task;

import java.util.*;

/**
 * Table model for table of tasks a specific resource is assigned to.
 * @author Oleg Kushnikov
 */
public class ResourceAssignmentsTableModel extends TableModelExt<LocalAssignment> {
  enum Column {
    ID("id", String.class),
    NAME("taskname", String.class),
    UNIT("unit", Float.class);

    private final String myCaption;
    private final Class<?> myClass;

    Column(String key, Class clazz) {
      myCaption = GanttLanguage.getInstance().getText(key);
      myClass = clazz;
    }

    String getCaption() {
      return myCaption;
    }

    public Class<?> getColumnClass() {
      return myClass;
    }
  }

  /** Assignments currently displayed in the table */
  private final List<LocalAssignment> myAssignments;

  private final AssignmentManager myAssignmentManager;

//  private final List<LocalAssignment> myAssignmentsToDelete = new ArrayList<>();
  private final HumanResource myResource;
//  private final Map<Task, ResourceAssignmentMutator> myTask2MutatorMap = new HashMap<>();

  private final AssignmentManager.Mutator myMutator;


  ResourceAssignmentsTableModel(AssignmentManager assignmentManager, HumanResource person) {
    myAssignmentManager = assignmentManager;
    myMutator = myAssignmentManager.createMutator();
    myResource = person;

    myAssignments = new ArrayList(assignmentManager.getResourceAssignments(person)); // copy
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return Column.values()[columnIndex].getColumnClass();
  }

  @Override
  public int getRowCount() {
    return myAssignments.size() + 1;
  }

  @Override
  public boolean isCellEditable(int row, int col) {
    if (row == myAssignments.size()) {
      return Column.NAME.equals(Column.values()[col]);
    } else {
      return Column.UNIT.equals(Column.values()[col]);
    }
  }

  @Override
  public String getColumnName(int col) {

    return Column.values()[col].getCaption();
  }

  @Override
  public int getColumnCount() {

    return Column.values().length;
  }

  @Override
  public Object getValueAt(int row, int col) {
    assert row >= 0 && row < getRowCount() && col >= 0 && col < getColumnCount() :
        String.format("Row/column index is out of bounds: (%d,%d) [%d,%d]", row, col, getRowCount(), getColumnCount());
    if (row == myAssignments.size()) {
      return null;
    }
    LocalAssignment ra = myAssignments.get(row);
    Column column = Column.values()[col];
    switch (column) {
      case ID: {
        return ra.getTask().getTaskID();
      }
      case NAME: {
        return ra.getTask();
      }
      case UNIT: {
        return ra.getLoad();
      }
      default:
        throw new IllegalArgumentException("Illegal row number=" + row);
    }
  }

  @Override
  public void setValueAt(Object val, int row, int col) {
    if (val == null) {
      return;
    }
    if (row >= 0) {
      if (row >= myAssignments.size()) {
        createAssignment(val);
      } else {
        updateAssignment(val, row, col);
      }
    } else {
      throw new IllegalArgumentException("I can't set data in row=" + row);
    }
  }

  private void updateAssignment(Object val, int row, int col) {
    Column column = Column.values()[col];
    LocalAssignment ra = myAssignments.get(row);
    switch (column) {
      case UNIT:
        ra.setLoad((Float)val);
        break;
    }
    fireTableCellUpdated(row, col);
  }

  private void createAssignment(Object value) {
    Task task = ((Task) value);
//    ResourceAssignmentMutator mutator = getMutator(task);
    LocalAssignment a = myMutator.createAssignment(task, myResource);
//    LocalAssignment ra = mutator.addAssignment(myResource);
    a.setLoad(100);
    myAssignments.add(a);
    fireTableRowsInserted(myAssignments.size(), myAssignments.size());
  }

//  private ResourceAssignmentMutator getMutator(Task task) {
//    ResourceAssignmentMutator mutator = myTask2MutatorMap.get(task);
//    if (mutator == null) {
//      mutator = task.getAssignmentCollection().createMutator();
//      myTask2MutatorMap.put(task, mutator);
//    }
//    return mutator;
//  }

  List<LocalAssignment> getResourcesAssignments() {
    return Collections.unmodifiableList(myAssignments);
  }

  @Override
  public void delete(int[] selectedRows) {
    List<LocalAssignment> selected = new ArrayList<>();
    for (int row : selectedRows) {
      if (row < myAssignments.size()) {
        LocalAssignment ra = myAssignments.get(row);
//        ResourceAssignmentMutator mutator = getMutator(ra.getTask());
        myMutator.removeAssignment(ra.getTask(), myResource);
//        mutator.deleteAssignment(myResource);
//        myAssignmentsToDelete.add(ra);
        selected.add(ra);
      }
    }
    myAssignments.removeAll(selected);
    fireTableDataChanged();
  }

  @Override
  public List<LocalAssignment> getAllValues() {
    return myAssignments;
  }

  public void commit() {
    myMutator.commit();
//    for (ResourceAssignmentMutator m : myTask2MutatorMap.values()) {
//      m.commit();
//    }
//    for (LocalAssignment ra : myAssignmentsToDelete) {
//      if (!myAssignments.contains(ra)) {
//        ra.delete();
//      }
//    }
  }
}
