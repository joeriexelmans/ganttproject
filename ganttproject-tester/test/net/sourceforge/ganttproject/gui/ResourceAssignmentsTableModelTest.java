package net.sourceforge.ganttproject.gui;

import net.sourceforge.ganttproject.gui.ResourceAssignmentsTableModel;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.test.task.AssignmentTestCase;
import org.junit.Test;

public class ResourceAssignmentsTableModelTest extends AssignmentTestCase {
    @Test
    public void emptyModel() {
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        task.getAssignmentCollection().addAssignment(res1);

        new ResourceAssignmentsTableModel(res1);
    }
}
