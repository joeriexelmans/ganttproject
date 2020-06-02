package net.sourceforge.ganttproject.task;

import biz.ganttproject.core.time.GanttCalendar;
import biz.ganttproject.core.time.TimeDuration;
import net.sourceforge.ganttproject.resource.HumanResource;

import java.util.List;

/**
 * An assignment of a resource to "something", could be a task in our project, or some other project.
 *
 * Note that we cannot get the task through this interface, as it may belong to another project.
 */
public interface Assignment {
    GanttCalendar getStart();

    GanttCalendar getEnd();

    HumanResource getResource();

    float getLoad();

    void setLoad(float load);
}
