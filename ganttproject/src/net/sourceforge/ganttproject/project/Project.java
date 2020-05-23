package net.sourceforge.ganttproject.project;

import biz.ganttproject.core.calendar.GPCalendarListener;
import biz.ganttproject.core.calendar.WeekendCalendarImpl;
import biz.ganttproject.core.time.TimeUnitStack;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import net.sourceforge.ganttproject.*;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.resource.ResourceEvent;
import net.sourceforge.ganttproject.resource.ResourceListener;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.roles.RoleManagerImpl;
import net.sourceforge.ganttproject.task.*;
import net.sourceforge.ganttproject.task.event.*;

import java.util.ArrayList;

/**
 * Domain model class for everything that makes up a "project" in GanttProject, and nothing more.
 * Does not include or depend on any "view" or UI logic.
 */
public class Project extends ObservableImpl implements IProject {
    public final WeekendCalendarImpl calendar;
    public final RoleManagerImpl roleManager;
    public final CustomColumnsManager hrCustomPropertyManager;
    public final HumanResourceManager hrManager;
    public final TaskManager taskManager;
    public final PrjInfos prjinfos;
    public final ArrayList<GanttPreviousState> baseLines;

    /**
     * @param facadeFactory This is a "hack" to allow the view to be updated when anything changes to the task hierarchy. Can be set to null if there is no view.
     * @param tmConfig
     */
    public Project(TimeUnitStack timeUnitStack, TaskContainmentHierarchyFacade.Factory facadeFactory, TaskManagerConfig tmConfig) {
        calendar = new WeekendCalendarImpl();
        roleManager = new RoleManagerImpl();
        hrCustomPropertyManager = new CustomColumnsManager();
        hrManager = new HumanResourceManager(roleManager.getDefaultRole(), hrCustomPropertyManager, roleManager);
        taskManager = TaskManager.Access.newInstance(facadeFactory, hrManager, calendar, timeUnitStack, tmConfig);
        prjinfos = new PrjInfos();
        baseLines = new ArrayList<GanttPreviousState>();

        // Each of the 'things' a project consists of defines its own listener type.
        // We subscribe to all of them.

        calendar.addListener(new GPCalendarListener() {
            @Override
            public void onCalendarChange() {
                notifyListeners();
            }
        });

        roleManager.addRoleListener(new RoleManager.Listener() {
            @Override
            public void rolesChanged(RoleManager.RoleEvent e) {
                notifyListeners();
            }
        });

        hrManager.addListener(new ResourceListener() {
            @Override
            public void resourceAdded(ResourceEvent event) {
                notifyListeners();
            }

            @Override
            public void resourcesRemoved(ResourceEvent event) {
                notifyListeners();
            }

            @Override
            public void resourceChanged(ResourceEvent e) {
                notifyListeners();
            }

            @Override
            public void resourceAssignmentsChanged(ResourceEvent e) {
                notifyListeners();
            }
        });

        taskManager.addTaskListener(new TaskListener() {
            @Override
            public void taskScheduleChanged(TaskScheduleEvent e) {
                notifyListeners();
            }

            @Override
            public void dependencyAdded(TaskDependencyEvent e) {
                notifyListeners();
            }

            @Override
            public void dependencyRemoved(TaskDependencyEvent e) {
                notifyListeners();
            }

            @Override
            public void dependencyChanged(TaskDependencyEvent e) {
                notifyListeners();
            }

            @Override
            public void taskAdded(TaskHierarchyEvent e) {
                notifyListeners();
            }

            @Override
            public void taskRemoved(TaskHierarchyEvent e) {
                notifyListeners();
            }

            @Override
            public void taskMoved(TaskHierarchyEvent e) {
                notifyListeners();
            }

            @Override
            public void taskPropertiesChanged(TaskPropertyEvent e) {
                notifyListeners();
            }

            @Override
            public void taskProgressChanged(TaskPropertyEvent e) {
                notifyListeners();
            }

            @Override
            public void taskModelReset() {
                notifyListeners();
            }
        });

        prjinfos.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                notifyListeners();
            }
        });
    }

    // Implementation of IProject:

    @Override
    public WeekendCalendarImpl getActiveCalendar() {
        return calendar;
    }
    @Override
    public RoleManagerImpl getRoleManager() {
        return roleManager;
    }
    @Override
    public CustomColumnsManager getResourceCustomPropertyManager() {
        return hrCustomPropertyManager;
    }
    @Override
    public HumanResourceManager getHumanResourceManager() {
        return hrManager;
    }
    @Override
    public TaskManager getTaskManager() {
        return taskManager;
    }
    @Override
    public CustomPropertyManager getTaskCustomPropertyManager() {
        return taskManager.getCustomPropertyManager();
    }
    @Override
    public PrjInfos getPrjInfos() {
        return prjinfos;
    }
    @Override
    public ArrayList<GanttPreviousState> getBaselines() {
        return baseLines;
    }
}
