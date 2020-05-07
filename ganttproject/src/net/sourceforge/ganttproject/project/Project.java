package net.sourceforge.ganttproject.project;

import biz.ganttproject.core.calendar.WeekendCalendarImpl;
import biz.ganttproject.core.time.TimeUnit;
import biz.ganttproject.core.time.TimeUnitStack;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import net.sourceforge.ganttproject.GanttPreviousState;
import net.sourceforge.ganttproject.PrjInfos;
import net.sourceforge.ganttproject.ProjectEventListener;
import net.sourceforge.ganttproject.gui.UIConfiguration;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.roles.RoleManagerImpl;
import net.sourceforge.ganttproject.task.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain model class for everything that makes up a "project" in GanttProject, and nothing more.
 * Does not include or depend on any "view" or UI logic.
 */
public class Project implements Observable {
    public final WeekendCalendarImpl calendar;
    public final GPTimeUnitStack timeUnitStack;
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
    public Project(TaskContainmentHierarchyFacade.Factory facadeFactory, TaskManagerConfig tmConfig) {
        calendar = new WeekendCalendarImpl();
        timeUnitStack = new GPTimeUnitStack();
        roleManager = new RoleManagerImpl();
        hrCustomPropertyManager = new CustomColumnsManager();
        hrManager = new HumanResourceManager(roleManager.getDefaultRole(), hrCustomPropertyManager, roleManager);
        taskManager = TaskManager.Access.newInstance(facadeFactory, hrManager, calendar, timeUnitStack, tmConfig);
        prjinfos = new PrjInfos();
        baseLines = new ArrayList<GanttPreviousState>();
    }

    // "Observable" implementation
    private final ArrayList<InvalidationListener> myObservers = new ArrayList<>();

    @Override
    public void addListener(InvalidationListener l) {
        myObservers.add(l);
    }

    @Override
    public void removeListener(InvalidationListener l) {
        myObservers.remove(l);
    }
}
