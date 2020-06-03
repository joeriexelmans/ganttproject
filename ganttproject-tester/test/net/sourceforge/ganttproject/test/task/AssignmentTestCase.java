package net.sourceforge.ganttproject.test.task;

import biz.ganttproject.core.calendar.AlwaysWorkingTimeCalendarImpl;
import biz.ganttproject.core.option.ColorOption;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
import net.sourceforge.ganttproject.assignment.AssignmentManager;
import net.sourceforge.ganttproject.gui.NotificationManager;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.TaskManagerConfig;
import org.junit.Before;

import java.awt.*;
import java.net.URL;

public class AssignmentTestCase {
    protected TaskManager myTaskManager;
    protected AssignmentManager myAssignmentManager;
    protected HumanResourceManager myHumanResourceManager;

    @Before
    public void setUp() {
        myAssignmentManager = new AssignmentManager();
        myHumanResourceManager = new HumanResourceManager(myAssignmentManager, RoleManager.Access
                .getInstance().getDefaultRole(), null);
        myHumanResourceManager.create("test resource#1", 1);
        myHumanResourceManager.create("test resource#2", 2);
        myTaskManager = newTaskManager();
    }

    private TaskManager newTaskManager() {
        return TaskManager.Access.newInstance(
                null,
                myHumanResourceManager,
                myAssignmentManager,
                new AlwaysWorkingTimeCalendarImpl(),
                GPTimeUnitStack.getInstance(),
                new TaskManagerConfig() {

            @Override
            public Color getDefaultColor() {
                return null;
            }

          @Override
          public ColorOption getDefaultColorOption() {
            return null;
          }

            @Override
            public URL getProjectDocumentURL() {
                return null;
            }

            @Override
            public NotificationManager getNotificationManager() {
              return null;
            }
        });
    }
}
