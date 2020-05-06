package net.sourceforge.ganttproject.parser;

import net.sourceforge.ganttproject.task.TaskManager;
import org.xml.sax.Attributes;

/**
 * TagHandler for &lt;tasks&gt;, i.e. the project's collection of tasks.
 * Not to be confused with TaskTagHandler (which handles a single task).
 */
public class TasksTagHandler extends AbstractTagHandler {
    private TaskManager myTaskManager;

    public TasksTagHandler(TaskManager tm) {
        super("tasks");
        myTaskManager = tm;
    }

    protected boolean onStartElement(Attributes attrs) {
        String val = attrs.getValue("empty-milestones");
        myTaskManager.setZeroMilestones(val != null && Boolean.parseBoolean(val));
        return true;
    }
}
