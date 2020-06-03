package net.sourceforge.ganttproject.assignment;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import kotlin.Pair;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages the many-to-many relationship of assignments between tasks and resources.
 */
public class AssignmentManager {
    // Every LocalAssignmentImpl instance occurs in each of these containers. This way we can lookup assignments both by Task and by Resource:
    private final Table<Task, HumanResource, LocalAssignmentImpl> assignments = HashBasedTable.create();
    private final HashMultimap<Task, LocalAssignmentImpl> taskToAssignments = HashMultimap.create();
    private final HashMultimap<HumanResource, LocalAssignmentImpl> resourceToAssignments = HashMultimap.create();

    /**
     * Creates a new assignment between task and resource. If an assignment between task and resource already exists, it is "overwritten" by the newly created asssignment, making this operation idempotent.
     */
    public LocalAssignment createAssignment(Task task, HumanResource resource) {
        LocalAssignmentImpl assignment = new LocalAssignmentImpl(task, resource);
        putAssignment(task, resource, assignment);
        return assignment;
    }

    // Used internally. Also idempotent.
    private void putAssignment(Task task, HumanResource resource, LocalAssignmentImpl assignment) {
        assignments.put(task, resource, assignment);
        taskToAssignments.put(task, assignment);
        resourceToAssignments.put(resource, assignment);
    }

    /**
     * Removes assignment between task and resource, if such assignment exists. Idempotent.
     */
    public void removeAssignment(Task task, HumanResource resource) {
        LocalAssignmentImpl assignment = assignments.remove(task, resource);
        if (assignment != null) {
            taskToAssignments.remove(task, assignment);
            resourceToAssignments.remove(resource, assignment);
        }
    }

    /**
     * Remove all assignments to task. Idempotent.
     */
    public void clearTaskAssignments(Task task) {
        Set<LocalAssignmentImpl> removed = taskToAssignments.removeAll(task);
        for (LocalAssignmentImpl assignment: removed) {
            resourceToAssignments.remove(assignment.getResource(), assignment);
            assignments.remove(task, assignment.getResource());
        }
    }

    /**
     * Remove all assignments of resource. Idempotent.
     */
    public void clearResourceAssignment(HumanResource resource) {
        Set<LocalAssignmentImpl> removed = resourceToAssignments.removeAll(resource);
        for (LocalAssignmentImpl assignment: removed) {
            taskToAssignments.remove(assignment.getTask(), assignment);
            assignments.remove(assignment.getTask(), resource);
        }
    }

    /**
     * Get a snapshot of the assignments of a task. Idempotent.
     */
    public ImmutableList<LocalAssignment> getTaskAssignments(Task task) {
        return ImmutableList.copyOf(taskToAssignments.get(task));
    }

    /**
     * Get a snapshot of the assignments of a resource. Idempotent.
     */
    public ImmutableList<LocalAssignment> getResourceAssignments(HumanResource resource) {
        return ImmutableList.copyOf(resourceToAssignments.get(resource));
    }

    /**
     * Get assignment between task and resource, or null if no such assignment exists. Idempotent.
     */
    public LocalAssignmentImpl getAssignment(Task task, HumanResource resource) {
        return assignments.get(task, resource);
    }

    public Mutator createMutator() {
        return new Mutator();
    }

    private class Mutator {
        private final Table<Task, HumanResource, LocalAssignmentImpl> toPut = HashBasedTable.create();
        private final Set<Pair<Task, HumanResource>> toRemove = new HashSet<>();
        private boolean invalidated = false;

        public LocalAssignment createAssignment(Task task, HumanResource resource) {
            Preconditions.checkState(!invalidated);
            toRemove.remove(new Pair(task, resource));
            LocalAssignmentImpl assignment = new LocalAssignmentImpl(task, resource);
            toPut.put(task, resource, assignment);
            return assignment;
        }

        public void removeAssignment(Task task, HumanResource resource) {
            Preconditions.checkState(!invalidated);
            toPut.remove(task, resource);
            toRemove.add(new Pair(task, resource));
        }

        public LocalAssignment getAssignment(Task task, HumanResource resource) {
            Preconditions.checkState(!invalidated);
            LocalAssignmentImpl ourCopy = toPut.get(task, resource);
            if (ourCopy != null) {
                return ourCopy;
            }

            LocalAssignmentImpl original = AssignmentManager.this.getAssignment(task, resource);
            if (original != null) {
                ourCopy = original.copy();
                toPut.put(task, resource, ourCopy);
                return ourCopy;
            }

            // no such assignment exists in our copy or in the original
            return null;
        }

        public void commit() {
            Preconditions.checkState(!invalidated);
            for (Pair<Task, HumanResource> rm: toRemove) {
                AssignmentManager.this.removeAssignment(rm.getFirst(), rm.getSecond());
            }

            for (LocalAssignmentImpl assignment: toPut.values()) {
                AssignmentManager.this.putAssignment(assignment.getTask(), assignment.getResource(), assignment);
            }
            invalidated = true;
        }
    }

    /**
     * @param from AssignmentManager of the project we are importing from.
     * @param resourceManager HumanResourceManager of the project we are importing TO, NOT importing FROM, so the resourceManager should belong to the same project this AssignmentManager belongs to.
     */
    public void importData(AssignmentManager from, HumanResourceManager resourceManager) {
        for (LocalAssignmentImpl assignment: from.assignments.values()) {
            TaskImpl task = (TaskImpl) assignment.getTask();
            HumanResource resource = assignment.getResource();

            if (task.isUnplugged()) {
                putAssignment(task, resource, assignment);
            } else {
                HumanResource ourResource = resourceManager.getById(resource.getId());
                if (ourResource != null) {
                    LocalAssignmentImpl copy = new LocalAssignmentImpl(task, ourResource);
                    copy.setLoad(assignment.getLoad());
                    copy.setCoordinator(assignment.isCoordinator());
                    copy.setRoleForAssignment(assignment.getRoleForAssignment());
                    putAssignment(task, ourResource, copy);
                }
            }
        }

       // Original method: ResourceAssignmentCollectionImpl.import
//        if (myTask.isUnplugged()) {
//            LocalAssignment[] assignments = assignmentCollection.getAssignments();
//            for (int i = 0; i < assignments.length; i++) {
//                LocalAssignment next = assignments[i];
//                addAssignment(next);
//            }
//        } else {
//            LocalAssignment[] assignments = assignmentCollection.getAssignments();
//            for (int i = 0; i < assignments.length; i++) {
//                LocalAssignment ass = assignments[i];
//                HumanResource res = ass.getResource();
//                HumanResource importedRes = myResourceManager.getById(res.getId());
//                if (importedRes != null) {
//                    LocalAssignment copy = new LocalAssignmentImpl(importedRes);
//                    copy.setLoad(ass.getLoad());
//                    copy.setCoordinator(ass.isCoordinator());
//                    copy.setRoleForAssignment(ass.getRoleForAssignment());
//                    addAssignment(copy);
//                }
//            }
//        }
    }
}
