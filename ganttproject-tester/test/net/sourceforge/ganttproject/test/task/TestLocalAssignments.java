package net.sourceforge.ganttproject.test.task;

import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.task.*;
import net.sourceforge.ganttproject.task.LocalAssignment;
import org.junit.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class TestLocalAssignments extends AssignmentTestCase {

    @Test
    public void testResourceAppearsInListAfterCreation() {
        Task task = myTaskManager.createTask();
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
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        HumanResource res2 = myHumanResourceManager.getById(2);
        task.getAssignmentCollection().addAssignment(res1);
        LocalAssignment asgn2 = task.getAssignmentCollection()
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
        Task task = myTaskManager.createTask();
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
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        task.getAssignmentCollection().addAssignment(res1);
        task.delete();
        LocalAssignment[] assignments = res1.getAssignments();
        assertTrue(
                "Resource is expected to have no assignments after task deletion",
                assignments.length == 0);
    }

    @Test
    public void testAssignmentsDisappearOnSummaryTaskDeletion() {
        HumanResource res1 = myHumanResourceManager.getById(1);
        Task summaryTask = myTaskManager.createTask();
        summaryTask.getAssignmentCollection().addAssignment(res1);

        Task childTask = myTaskManager.newTaskBuilder().withParent(summaryTask).build();
        childTask.getAssignmentCollection().addAssignment(res1);

        myTaskManager.deleteTask(summaryTask);
        LocalAssignment[] assignments = res1.getAssignments();
        assertTrue(
            "Resource is expected to have no assignments after summary task deletion",
            assignments.length == 0);
    }

    @Test
    public void testAssignmentDisappearOnResourceDeletion() {
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        task.getAssignmentCollection().addAssignment(res1);
        res1.delete();
        Set<HumanResource> resources = extractResources(task);
        assertTrue("It is expected that after resource deletion assignments disappear", resources.isEmpty());
    }

    // See https://github.com/bardsoftware/ganttproject/issues/612
    @Test
    public void testAssignmentUpdateAndDelete() {
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        LocalAssignment assignment = task.getAssignmentCollection().addAssignment(res1);
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

        Task task = myTaskManager.createTask();
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
        Task task = myTaskManager.createTask();
        HumanResource res1 = myHumanResourceManager.getById(1);
        ResourceAssignmentMutator mutator = task.getAssignmentCollection().createMutator();
        mutator.addAssignment(res1);
        mutator.commit();

        mutator.deleteAssignment(res1); // should throw IllegalStateException
    }

    private Set<HumanResource> extractResources(Task task) {
        Set<HumanResource> result = new HashSet<HumanResource>();
        LocalAssignment[] assignments = task.getAssignments();
        for (int i = 0; i < assignments.length; i++) {
            LocalAssignment next = assignments[i];
            result.add(next.getResource());
            assertEquals("Unexpected task is owning resource assignment="
                    + next, task, next.getTask());
        }
        return result;
    }

}
