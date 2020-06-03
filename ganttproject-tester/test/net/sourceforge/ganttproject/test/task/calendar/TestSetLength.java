/*
 * Created on 31.10.2004
 */
package net.sourceforge.ganttproject.test.task.calendar;

import biz.ganttproject.core.time.impl.GregorianTimeUnitStack;
import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.project.ProjectStub;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author bard
 */
public class TestSetLength extends TestWeekendCalendar {
  @Test
  public void testTaskStartingOnFridayLastingTwoDaysEndsOnTuesday() {
    TaskManager taskManager = new ProjectStub().taskManager;
    Task t = taskManager.newTaskBuilder()
        .withStartDate(TestSetupHelper.newFriday().getTime())
        .build();
    t.setDuration(taskManager.createLength(GregorianTimeUnitStack.DAY,
        2));
    assertEquals(
        "unXpected end of task which starts on friday and is 2 days long",
        TestSetupHelper.newTuesday(), t.getEnd());
  }

  @Test
  public void testTaskStartingOnSaturdayLastingOneDayEndsOnTuesday() {
    TaskManager taskManager = new ProjectStub().taskManager;
    Task t = taskManager.newTaskBuilder()
        .withStartDate(TestSetupHelper.newSaturday().getTime())
        .build();
    t.setDuration(taskManager.createLength(GregorianTimeUnitStack.DAY,
        1));
    assertEquals(
        "unXpected end of task which starts on saturday and is 1 day long",
        TestSetupHelper.newTuesday(), t.getEnd());
  }

  @Test
  public void testTaskStartingOnSundayLastingOneDayEndsOnTuesday() {
    TaskManager taskManager = new ProjectStub().taskManager;
    Task t = taskManager.newTaskBuilder()
        .withStartDate(TestSetupHelper.newSunday().getTime())
        .build();
    t.setDuration(taskManager.createLength(GregorianTimeUnitStack.DAY,
        1));
    assertEquals(
        "unXpected end of task which starts on sunday and is 1 day long",
        TestSetupHelper.newTuesday(), t.getEnd());

  }
}
