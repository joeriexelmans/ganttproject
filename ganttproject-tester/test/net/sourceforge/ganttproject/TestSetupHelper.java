package net.sourceforge.ganttproject;

import biz.ganttproject.core.calendar.AlwaysWorkingTimeCalendarImpl;
import biz.ganttproject.core.calendar.GPCalendarCalc;
import biz.ganttproject.core.option.ColorOption;
import biz.ganttproject.core.option.DefaultColorOption;
import biz.ganttproject.core.time.CalendarFactory;
import biz.ganttproject.core.time.GanttCalendar;
import biz.ganttproject.core.time.TimeUnitStack;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
import net.sourceforge.ganttproject.gui.NotificationManager;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.roles.RoleManagerImpl;
import net.sourceforge.ganttproject.task.CustomColumnsManager;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.TaskManagerConfig;

import java.awt.*;
import java.net.URL;

public class TestSetupHelper {
    public static class TaskManagerTestConfig implements TaskManagerConfig {
        static private final DefaultColorOption DEFAULT_COLOR_OPTION = new DefaultColorOption("taskcolor", Color.CYAN);

        @Override
        public Color getDefaultColor() {
            return DEFAULT_COLOR_OPTION.getValue();
        }

        @Override
        public ColorOption getDefaultColorOption() {
            return DEFAULT_COLOR_OPTION;
        }

        @Override
        public URL getProjectDocumentURL() {
            return null;
        }

        @Override
        public NotificationManager getNotificationManager() {
            return null;
        }
    }

    public static class TaskManagerBuilder {
        private TaskManagerTestConfig myConfig = new TaskManagerTestConfig();
        private GPCalendarCalc myGPCalendar = new AlwaysWorkingTimeCalendarImpl();
        private TimeUnitStack myTimeUnitStack = new GPTimeUnitStack();
        private RoleManager myRoleManager = new RoleManagerImpl();
        private HumanResourceManager myResourceManager = new HumanResourceManager(myRoleManager.getDefaultRole(), new CustomColumnsManager(), myRoleManager);

        public TaskManagerBuilder withCalendar(GPCalendarCalc calendar) {
            myGPCalendar = calendar;
            return this;
        }

        public HumanResourceManager getResourceManager() {
            return myResourceManager;
        }

        public TimeUnitStack getTimeUnitStack() {
            return myTimeUnitStack;
        }

        public TaskManagerTestConfig getConfig() {
            return myConfig;
        }

        public TaskManager build() {
            return TaskManager.Access.newInstance(null, myResourceManager, myGPCalendar, myTimeUnitStack, myConfig);
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
