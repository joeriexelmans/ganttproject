package net.sourceforge.ganttproject;

import biz.ganttproject.core.calendar.AlwaysWorkingTimeCalendarImpl;
import biz.ganttproject.core.calendar.GPCalendarCalc;
import biz.ganttproject.core.time.CalendarFactory;
import biz.ganttproject.core.time.GanttCalendar;
import biz.ganttproject.core.time.TimeUnitStack;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
import net.sourceforge.ganttproject.assignment.Assignment;
import net.sourceforge.ganttproject.assignment.AssignmentManager;
import net.sourceforge.ganttproject.project.ProjectStub;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.roles.RoleManagerImpl;
import net.sourceforge.ganttproject.task.CustomColumnsManager;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.TaskManagerConfig;

public class TestSetupHelper {
    public static class TaskManagerBuilder {
        private ProjectStub.TaskManagerConfigStub myConfig = new ProjectStub.TaskManagerConfigStub();
        private GPCalendarCalc myGPCalendar = new AlwaysWorkingTimeCalendarImpl();
        private TimeUnitStack myTimeUnitStack = GPTimeUnitStack.getInstance();
        private RoleManager myRoleManager = new RoleManagerImpl();
        private AssignmentManager myAssignmentManager = new AssignmentManager();
        private HumanResourceManager myResourceManager = new HumanResourceManager(myAssignmentManager, myRoleManager.getDefaultRole(), new CustomColumnsManager(), myRoleManager);

        public TaskManagerBuilder withCalendar(GPCalendarCalc calendar) {
            myGPCalendar = calendar;
            return this;
        }

        public HumanResourceManager getResourceManager() {
            return myResourceManager;
        }

        public AssignmentManager getAssignmentManager() {
            return myAssignmentManager;
        }

        public TimeUnitStack getTimeUnitStack() {
            return myTimeUnitStack;
        }

        public TaskManagerConfig getConfig() {
            return myConfig;
        }

        public TaskManager build() {
            return TaskManager.Access.newInstance(null, myResourceManager, new AssignmentManager(), myGPCalendar, myTimeUnitStack, myConfig);
        }
    }

    public static TaskManagerBuilder newTaskManagerBuilder() {
        return new TaskManagerBuilder();
    }

    public static GanttCalendar newFriday() {
        return CalendarFactory.createGanttCalendar(2004, 9, 15);
    }

    public static GanttCalendar newSaturday() {
        return CalendarFactory.createGanttCalendar(2004, 9, 16);
    }

    public static GanttCalendar newSunday() {
        return CalendarFactory.createGanttCalendar(2004, 9, 17);
    }

    public static GanttCalendar newTuesday() {
        return CalendarFactory.createGanttCalendar(2004, 9, 19);
    }

    public static GanttCalendar newMonday() {
        return CalendarFactory.createGanttCalendar(2004, 9, 18);
    }

    public static GanttCalendar newWendesday() {
        return CalendarFactory.createGanttCalendar(2004, 9, 20);
    }

    public static GanttCalendar newThursday() {
        return CalendarFactory.createGanttCalendar(2004, 9, 21);
    }
}
