package net.sourceforge.ganttproject.parser;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import net.sourceforge.ganttproject.gui.TaskTreeUIFacade;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;

import java.util.List;

/**
 * @author joeriexelmans
 *
 * Expands tasks in view when parsing is finished.
 *
 * Split off from TaskTagHandler to make TaskTagHandler independent of the UI.
 */
public class TaskParsingListener implements ParsingListener {
    private final TaskManager myManager;
    private final TaskTreeUIFacade myTreeFacade;

    public TaskParsingListener(TaskManager mgr, TaskTreeUIFacade fac) {
        myManager = mgr;
        myTreeFacade = fac;
    }

    @Override
    public void parsingStarted() {
    }

    @Override
    public void parsingFinished() {
        List<Task> tasksBottomUp = Lists.reverse(myManager.getTaskHierarchy().breadthFirstSearch(null, false));

        for (Task t : tasksBottomUp) {
            myTreeFacade.setExpanded(t, MoreObjects.firstNonNull(t.getExpand(), Boolean.TRUE));
        }
    }
}
