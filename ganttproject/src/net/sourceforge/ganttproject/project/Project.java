package net.sourceforge.ganttproject.project;

import biz.ganttproject.core.calendar.GPCalendarListener;
import biz.ganttproject.core.calendar.WeekendCalendarImpl;
import biz.ganttproject.core.time.TimeUnitStack;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
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
public class Project implements IProject {
    public final WeekendCalendarImpl calendar;
    public final RoleManager roleManager;
    public final CustomColumnsManager hrCustomPropertyManager;
    public final HumanResourceManager hrManager;
    public final TaskManagerImpl taskManager;
    public final PrjInfos prjinfos;
    public final ArrayList<GanttPreviousState> baseLines;

    private final CallbackList onModifiedCallbacks = new CallbackList();
    private final CallbackList onResetCallbacks = new CallbackList();

    /**
     * After construction, you will have a blank project.
     *
     * @param facadeFactory This is a "hack" to allow the view to be updated when anything changes to the task hierarchy. Can be set to null if there is no view.
     * @param tmConfig
     */
    public Project(TaskContainmentHierarchyFacade.Factory facadeFactory, TaskManagerConfig tmConfig) {
        calendar = new WeekendCalendarImpl();
        roleManager = RoleManager.Access.getInstance();
        hrCustomPropertyManager = new CustomColumnsManager();
        hrManager = new HumanResourceManager(roleManager.getDefaultRole(), hrCustomPropertyManager, roleManager);
        taskManager = (TaskManagerImpl) TaskManager.Access.newInstance(facadeFactory, hrManager, calendar, GPTimeUnitStack.getInstance(), tmConfig);
        prjinfos = new PrjInfos();
        baseLines = new ArrayList<GanttPreviousState>();

        calendar.addListener(taskManager.getCalendarListener());

        // Each of the 'things' a project consists of defines its own listener type.
        // We subscribe to all of them.

        prjinfos.onModified(() -> { onModifiedCallbacks.runAll(); });
        calendar.addListener(() -> onModifiedCallbacks.runAll());
        roleManager.addRoleListener(e -> onModifiedCallbacks.runAll());
        hrManager.addListener(new ResourceListener() {
            @Override
            public void resourceAdded(ResourceEvent event) {
                onModifiedCallbacks.runAll();
            }

            @Override
            public void resourcesRemoved(ResourceEvent event) {
                onModifiedCallbacks.runAll();
            }

            @Override
            public void resourceChanged(ResourceEvent e) {
                onModifiedCallbacks.runAll();
            }

            @Override
            public void resourceAssignmentsChanged(ResourceEvent e) {
                onModifiedCallbacks.runAll();
            }
        });
        taskManager.addTaskListener(new TaskListener() {
            @Override
            public void taskScheduleChanged(TaskScheduleEvent e) {
                onModifiedCallbacks.runAll();
            }

            @Override
            public void dependencyAdded(TaskDependencyEvent e) {
                onModifiedCallbacks.runAll();
            }

            @Override
            public void dependencyRemoved(TaskDependencyEvent e) {
                onModifiedCallbacks.runAll();
            }

            @Override
            public void dependencyChanged(TaskDependencyEvent e) {
                onModifiedCallbacks.runAll();
            }

            @Override
            public void taskAdded(TaskHierarchyEvent e) {
                onModifiedCallbacks.runAll();
            }

            @Override
            public void taskRemoved(TaskHierarchyEvent e) {
                onModifiedCallbacks.runAll();
            }

            @Override
            public void taskMoved(TaskHierarchyEvent e) {
                onModifiedCallbacks.runAll();
            }

            @Override
            public void taskPropertiesChanged(TaskPropertyEvent e) {
                onModifiedCallbacks.runAll();
            }

            @Override
            public void taskProgressChanged(TaskPropertyEvent e) {
                onModifiedCallbacks.runAll();
            }

            @Override
            public void taskModelReset() {
                onModifiedCallbacks.runAll();
            }
        });
    }

    public void onModified(Runnable callback) {
        onModifiedCallbacks.add(callback);
    }

    public void onReset(Runnable callback) {
        onResetCallbacks.add(callback);
    }

    /**
     * Clear all project contents, turning it back into a blank project.
     */
    public void reset() {
        onResetCallbacks.runAll();

        taskManager.reset();
        prjinfos.reset();
        roleManager.clear();
        taskManager.getCustomPropertyManager().reset();
        hrCustomPropertyManager.reset();

        for (int i = 0; i < baseLines.size(); i++) {
            baseLines.get(i).remove();
        }
        baseLines.clear();
        calendar.reset();
    }

    // Implementation of IProject:

    @Override
    public WeekendCalendarImpl getActiveCalendar() {
        return calendar;
    }
    @Override
    public RoleManager getRoleManager() {
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
