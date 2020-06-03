package net.sourceforge.ganttproject.assignment;

import net.sourceforge.ganttproject.resource.HumanResource;

public interface AssignmentListener {
    public void resourceAssignmentsChanged(HumanResource resource);
}
