package net.sourceforge.ganttproject.gui.taskproperties;

import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.assignment.Assignment;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.test.task.AssignmentTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResourcesTableModelTest extends AssignmentTestCase {
    @Test
    public void emptyModel() {
        Task task = myTaskManager.createTask();
        ResourcesTableModel model = new ResourcesTableModel(myAssignmentManager, task);

        assertEquals(1, model.getRowCount()); // there's always an empty row for adding new assignments
        assertEquals(5, model.getColumnCount());
    }

    @Test
    public void getValues() {
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        task.getAssignmentCollection().addAssignment(res1);
        ResourcesTableModel model = new ResourcesTableModel(myAssignmentManager, task);

        assertEquals(2, model.getRowCount());
        assertEquals(5, model.getColumnCount());

        assertEquals(String.valueOf(res1.getId()), model.getValueAt(0, 0));
        assertEquals(res1, model.getValueAt(0,1));
        // could test the other 3 columns (load, isCoordinator, role) but kinda boring...
    }

    @Test
    public void setValueUpdateAssignment() {
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        Assignment a = task.getAssignmentCollection().addAssignment(res1);
        ResourcesTableModel model = new ResourcesTableModel(myAssignmentManager, task);

        // update load:
        model.setValueAt(0.5f, 0, 2);
        model.commit();

        assertEquals(0.5f, a.getLoad(), 0.0000001f);
    }

    @Test
    public void setValueReassign() {
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        HumanResource res2 = myHumanResourceManager.getById(2);
        Assignment a = task.getAssignmentCollection().addAssignment(res1);
        ResourcesTableModel model = new ResourcesTableModel(myAssignmentManager, task);

        // re-assign to another resource:
        model.setValueAt(res2, 0, 1);
        model.commit();

        assertEquals(1, task.getAssignmentCollection().getAssignments().length);
        assertEquals(res2, task.getAssignmentCollection().getAssignments()[0].getResource());
    }

    @Test
    public void setValueRemoveAssignment() {
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        Assignment a = task.getAssignmentCollection().addAssignment(res1);
        ResourcesTableModel model = new ResourcesTableModel(myAssignmentManager, task);

        // remove assignment
        model.setValueAt(null, 0, 1);
        model.commit();

        assertEquals(0, task.getAssignmentCollection().getAssignments().length);
    }

    @Test
    public void setValueCreateAssignment() {
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        HumanResource res2 = myHumanResourceManager.getById(2);
        Assignment a = task.getAssignmentCollection().addAssignment(res1);
        ResourcesTableModel model = new ResourcesTableModel(myAssignmentManager, task);

        // add assignment
        model.setValueAt(res2, 1, 1);
        model.commit();

        assertEquals(2, task.getAssignmentCollection().getAssignments().length);
    }

    @Test
    public void deleteRow() {
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        Assignment a = task.getAssignmentCollection().addAssignment(res1);
        ResourcesTableModel model = new ResourcesTableModel(myAssignmentManager, task);

        // delete first row
        model.delete(new int[]{0});
        model.commit();

        // assignment should be removed
        assertEquals(0, task.getAssignmentCollection().getAssignments().length);
    }
}
