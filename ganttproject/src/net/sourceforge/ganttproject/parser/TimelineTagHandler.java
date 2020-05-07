package net.sourceforge.ganttproject.parser;

import com.google.common.collect.Lists;
import net.sourceforge.ganttproject.GPLogger;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;
import org.xml.sax.Attributes;

import java.util.List;

public class TimelineTagHandler extends AbstractTagHandler implements ParsingListener {
  private final UIFacade myUIFacade;
  private final TaskManager myTaskManager;
  private final List<Integer> myIds = Lists.newArrayList();

  public TimelineTagHandler(UIFacade ui, TaskManager tm) {
    super("timeline", true);
    myUIFacade = ui;
    myTaskManager = tm;
  }

  @Override
  public void parsingStarted() {
  }

  @Override
  public void parsingFinished() {
    myUIFacade.getCurrentTaskView().getTimelineTasks().clear();
    for (Integer id : myIds) {
      Task t = myTaskManager.getTask(id);
      if (t != null) {
        myUIFacade.getCurrentTaskView().getTimelineTasks().add(t);
      }
    }
  }

  @Override
  protected void onEndElement() {
    String[] ids = getCdata().split(",");
    for (String id : ids) {
      try {
        myIds.add(Integer.valueOf(id.trim()));
      } catch (NumberFormatException e) {
        GPLogger.logToLogger(e);
      }
    }
    clearCdata();
  }
}
