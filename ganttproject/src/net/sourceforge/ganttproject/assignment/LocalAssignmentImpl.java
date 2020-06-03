package net.sourceforge.ganttproject.assignment;

import biz.ganttproject.core.time.GanttCalendar;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.roles.Role;
import net.sourceforge.ganttproject.task.Task;

public class LocalAssignmentImpl implements LocalAssignment {
    private final Task myTask;
    private final HumanResource myResource;

    private float myLoad;
    private boolean myIsCoordinator;
    private Role myRole;

    public LocalAssignmentImpl(Task task, HumanResource resource) {
        myTask = task;
        myResource = resource;
    }

    LocalAssignmentImpl copy() {
        LocalAssignmentImpl copy = new LocalAssignmentImpl(myTask, myResource);
        copy.myLoad = myLoad;
        copy.myIsCoordinator = myIsCoordinator;
        copy.myRole = myRole;
        return copy;
    }

    @Override
    public Task getTask() {
        return myTask;
    }

    @Override
    public HumanResource getResource() {
        return myResource;
    }

    @Override
    public GanttCalendar getStart() {
        return myTask.getStart();
    }

    @Override
    public GanttCalendar getEnd() {
        return myTask.getEnd();
    }

    @Override
    public float getLoad() {
        return myLoad;
    }

    @Override
    public void setLoad(float load) {
        myLoad = load;
    }

    @Override
    public boolean isCoordinator() {
        return myIsCoordinator;
    }

    @Override
    public void setCoordinator(boolean isCoordinator) {
        myIsCoordinator = isCoordinator;
    }

    @Override
    public Role getRoleForAssignment() {
        return myRole;
    }

    @Override
    public void setRoleForAssignment(Role role) {
        myRole = role;
    }

    @Override
    public String toString() {
      return myResource.getName() + " -> " + myTask.getName();
    }
}
