package net.sourceforge.ganttproject.test.task;

import biz.ganttproject.core.calendar.AlwaysWorkingTimeCalendarImpl;
import biz.ganttproject.core.option.ColorOption;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
import net.sourceforge.ganttproject.gui.NotificationManager;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.task.ResourceAssignment;
import net.sourceforge.ganttproject.task.ResourceAssignmentMutator;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.TaskManagerConfig;
import org.junit.*;

import java.awt.*;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class TestResourceAssignments {
    private TaskManager myTaskManager;

    private HumanResourceManager myHumanResourceManager;

    @Test
    public void testResourceAppearsInListAfterCreation() {
        TaskManager taskManager = myTaskManager;
        Task task = taskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        HumanResource res2 = myHumanResourceManager.getById(2);
        task.getAssignmentCollection().addAssignment(res1);
        task.getAssignmentCollection().addAssignment(res2);
        Set<HumanResource> actualResources = extractResources(task);
        Set<HumanResource> expectedResources = new HashSet<HumanResource>(
                Arrays.asList(res1, res2));
        assertEquals("Unexpected set of resources assigned to task=" + task,
                expectedResources, actualResources);
    }

    @Test
    public void testResourceDisappearsFromListAfterAssignmentDeletion() {
        TaskManager taskManager = myTaskManager;
        Task task = taskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        HumanResource res2 = myHumanResourceManager.getById(2);
        task.getAssignmentCollection().addAssignment(res1);
        ResourceAssignment asgn2 = task.getAssignmentCollection()
                .addAssignment(res2);

        asgn2.delete();

        Set<HumanResource> actualResources = extractResources(task);
        Set<HumanResource> expectedResources = new HashSet<HumanResource>(
                Arrays.asList(res1));
        assertEquals("Unexpected set of resources assigned to task=" + task,
                expectedResources, actualResources);
    }

    @Test
    public void testResourceIsNotAssignedTwice() {
        TaskManager taskManager = myTaskManager;
        Task task = taskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        task.getAssignmentCollection().addAssignment(res1);
        task.getAssignmentCollection().addAssignment(res1);
        Set<HumanResource> actualResources = extractResources(task);
        Set<HumanResource> expectedResources = new HashSet<HumanResource>(
                Arrays.asList(res1));
        assertEquals("Unexpected set of resources assigned to task=" + task,
                expectedResources, actualResources);
    }


    @Test
    public void testAssignmentsDisappearOnTaskDeletion() {
        TaskManager taskManager = myTaskManager;
        Task task = taskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        task.getAssignmentCollection().addAssignment(res1);
        task.delete();
        ResourceAssignment[] assignments = res1.getAssignments();
        assertTrue(
                "Resource is expected to have no assignments after task deletion",
                assignments.length == 0);
    }

    @Test
    public void testAssignmentsDisappearOnSummaryTaskDeletion() {
        HumanResource res1 = myHumanResourceManager.getById(1);
        TaskManager taskManager = myTaskManager;
        Task summaryTask = taskManager.createTask();
        summaryTask.getAssignmentCollection().addAssignment(res1);

        Task childTask = taskManager.newTaskBuilder().withParent(summaryTask).build();
        childTask.getAssignmentCollection().addAssignment(res1);

        taskManager.deleteTask(summaryTask);
        ResourceAssignment[] assignments = res1.getAssignments();
        assertTrue(
            "Resource is expected to have no assignments after summary task deletion",
            assignments.length == 0);
    }

    @Test
    public void testAssignmentDisappearOnResourceDeletion() {
        TaskManager taskManager = myTaskManager;
        Task task = taskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        task.getAssignmentCollection().addAssignment(res1);
        res1.delete();
        Set<HumanResource> resources = extractResources(task);
        assertTrue("It is expected that after resource deletion assignments disappear", resources.isEmpty());
    }

    // See https://github.com/bardsoftware/ganttproject/issues/612
    @Test
    public void testAssignmentUpdateAndDelete() {
        TaskManager taskManager = myTaskManager;
        Task task = taskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        ResourceAssignment assignment = task.getAssignmentCollection().addAssignment(res1);
        ResourceAssignmentMutator mutator = task.getAssignmentCollection().createMutator();
        assignment.delete();
        assignment = mutator.addAssignment(res1);
        assignment.setLoad(50);
        assignment.delete();
        mutator.commit();

        Set<HumanResource> resources = extractResources(task);
        assertTrue("It is expected that assignment is removed after sequential update+delete via mutator", resources.isEmpty());
    }

    @Test
    public void testResourceAssignmentCollectionMutatorDeletion() {
        Set<HumanResource> actualResources;
        Set<HumanResource> expectedResources;

        TaskManager taskManager = myTaskManager;
        Task task = taskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);

        // First we check assign followed by delete
        ResourceAssignmentMutator mutator = task.getAssignmentCollection().createMutator();
        mutator.addAssignment(res1);
        mutator.deleteAssignment(res1);
        mutator.commit();

        actualResources = extractResources(task);
        expectedResources = new HashSet<HumanResource>();
        assertEquals("Unexpected set of resources assigned to task=" + task,
                expectedResources, actualResources);

        // Then we check assign + commit, delete + commit
        ResourceAssignmentMutator mutator2 = task.getAssignmentCollection().createMutator();
        mutator2.addAssignment(res1);
        mutator2.commit();

        actualResources = extractResources(task);
        expectedResources = new HashSet<HumanResource>(
                Arrays.asList(res1));
        assertEquals("Unexpected set of resources assigned to task=" + task,
                expectedResources, actualResources);

        ResourceAssignmentMutator mutator3 = task.getAssignmentCollection().createMutator();
        mutator3.deleteAssignment(res1);
        mutator3.commit();

        actualResources = extractResources(task);
        expectedResources = new HashSet<HumanResource>();
        assertEquals("Unexpected set of resources assigned to task=" + task,
                expectedResources, actualResources);
    }

    @Test(expected = IllegalStateException.class)
    public void testResourceAssignmentCollectionMutatorInvalidation() {
        TaskManager taskManager = myTaskManager;
        Task task = taskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        ResourceAssignmentMutator mutator = task.getAssignmentCollection().createMutator();
        mutator.addAssignment(res1);
        mutator.commit();

        mutator.deleteAssignment(res1); // should throw IllegalStateException
    }

    private Set<HumanResource> extractResources(Task task) {
        Set<HumanResource> result = new HashSet<HumanResource>();
        ResourceAssignment[] assignments = task.getAssignments();
        for (int i = 0; i < assignments.length; i++) {
            ResourceAssignment next = assignments[i];
            result.add(next.getResource());
            assertEquals("Unexpected task is owning resource assignment="
                    + next, task, next.getTask());
        }
        return result;
    }

    @Before
    public void setUp() {
        myHumanResourceManager = new HumanResourceManager(RoleManager.Access
                .getInstance().getDefaultRole(), null);
        myHumanResourceManager.create("test resource#1", 1);
        myHumanResourceManager.create("test resource#2", 2);
        myTaskManager = newTaskManager();
    }

    private TaskManager newTaskManager() {
        return TaskManager.Access.newInstance(
                null,
                myHumanResourceManager,
                new AlwaysWorkingTimeCalendarImpl(),
                GPTimeUnitStack.getInstance(),
                new TaskManagerConfig() {

            @Override
            public Color getDefaultColor() {
                return null;
            }

          @Override
          public ColorOption getDefaultColorOption() {
            return null;
          }

            @Override
            public URL getProjectDocumentURL() {
                return null;
            }

            @Override
            public NotificationManager getNotificationManager() {
              return null;
            }
        });
    }
}
