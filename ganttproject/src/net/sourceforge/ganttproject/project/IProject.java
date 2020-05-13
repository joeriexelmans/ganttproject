package net.sourceforge.ganttproject.project;

import biz.ganttproject.core.calendar.GPCalendarCalc;
import net.sourceforge.ganttproject.CustomPropertyManager;
import net.sourceforge.ganttproject.GanttPreviousState;
import net.sourceforge.ganttproject.PrjInfos;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.task.TaskManager;

import java.util.ArrayList;

/**
 * This is a temporary interface to help us move the domain model Project logic out of the GanttProject class, and the Project class instead.
 *
 * The names of the methods come from those in IGanttProject.
 */
public interface IProject {
    public GPCalendarCalc getActiveCalendar();
    public RoleManager getRoleManager();
    public CustomPropertyManager getResourceCustomPropertyManager();
    public HumanResourceManager getHumanResourceManager();
    public TaskManager getTaskManager();
    public CustomPropertyManager getTaskCustomPropertyManager();
    public PrjInfos getPrjInfos();
    public ArrayList<GanttPreviousState> getBaselines();
}
