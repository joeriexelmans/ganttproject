package net.sourceforge.ganttproject.gui;

import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.assignment.Assignment;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.test.task.AssignmentTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResourceAssignmentsTableModelTest extends AssignmentTestCase {
    @Test
    public void emptyModel() {
        HumanResource res1 = myHumanResourceManager.getById(1);
        ResourceAssignmentsTableModel model = new ResourceAssignmentsTableModel(myAssignmentManager, res1);

        assertEquals( 1, model.getRowCount() ); // one empty row
        assertEquals( 3, model.getColumnCount() );
    }

    @Test
    public void getValues() {
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        Assignment a = task.getAssignmentCollection().addAssignment(res1);
        ResourceAssignmentsTableModel model = new ResourceAssignmentsTableModel(myAssignmentManager, res1);

        assertEquals(2, model.getRowCount()); // one assignment + one empty row
        assertEquals(3, model.getColumnCount());

        assertEquals(task.getTaskID(), model.getValueAt(0,0));
        assertEquals(task, model.getValueAt(0, 1));
        assertEquals(a.getLoad(), model.getValueAt(0, 2));
    }

    @Test
    public void setValueUpdateAssignment() {
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        Assignment a = task.getAssignmentCollection().addAssignment(res1);
        ResourceAssignmentsTableModel model = new ResourceAssignmentsTableModel(myAssignmentManager, res1);

        // update load:
        model.setValueAt(0.5f, 0, 2);
        model.commit();

        assertEquals(0.5f, a.getLoad(), 0.0000001f);
    }

    @Test
    public void setValueCreateAssignment() {
        Task task = myTaskManager.createTask();
        Task task2 = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        Assignment a = task.getAssignmentCollection().addAssignment(res1);
        ResourceAssignmentsTableModel model = new ResourceAssignmentsTableModel(myAssignmentManager, res1);

        // add assignment
        model.setValueAt(task2, 1, 1);
        model.commit();

        assertEquals(1, task.getAssignmentCollection().getAssignments().length);
        assertEquals(1, task2.getAssignmentCollection().getAssignments().length);
    }

    @Test
    public void deleteRow() {
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        Assignment a = task.getAssignmentCollection().addAssignment(res1);
        ResourceAssignmentsTableModel model = new ResourceAssignmentsTableModel(myAssignmentManager, res1);

        // delete first row
        model.delete(new int[]{0});
        model.commit();

        // assignment should be removed
        assertEquals(0, task.getAssignmentCollection().getAssignments().length);
    }
}
