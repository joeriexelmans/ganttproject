package net.sourceforge.ganttproject.parser;

import net.sourceforge.ganttproject.document.DocumentManager;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.project.IProject;
import net.sourceforge.ganttproject.task.TaskManager;

/**
 * Like Parser, but also parses UI-related tags.
 */
public class UIParser extends Parser {
    UIFacade myUIFacade;
    
    public UIParser(DocumentManager docManager, UIFacade uiFacade) {
        super(docManager);
        myUIFacade = uiFacade;
    }

    @Override
    protected GPParser createParser(ParsingContext ctx, IProject project) {
        GPParser parser = super.createParser(ctx, project);

        TaskManager taskManager = project.getTaskManager();

        // These tag handlers read the tags that indicate which columns to display in the task treeview
        TaskDisplayColumnsTagHandler pilsenTaskDisplayHandler = TaskDisplayColumnsTagHandler.createPilsenHandler();
        TaskDisplayColumnsTagHandler legacyTaskDisplayHandler = TaskDisplayColumnsTagHandler.createLegacyHandler();
        TaskDisplayColumnsTagHandler resourceFieldsHandler = TaskDisplayColumnsTagHandler.createPilsenHandler();

        parser.addTagHandler(pilsenTaskDisplayHandler);
        parser.addTagHandler(legacyTaskDisplayHandler);
        parser.addTagHandler(resourceFieldsHandler);

        // These listeners actually update the treeview's columns when parsing is finished
        parser.addParsingListener(TaskDisplayColumnsTagHandler.createTaskDisplayColumnsWrapper(myUIFacade.getTaskTree().getVisibleFields(), pilsenTaskDisplayHandler, legacyTaskDisplayHandler));
        parser.addParsingListener(TaskDisplayColumnsTagHandler.createTaskDisplayColumnsWrapper(myUIFacade.getResourceTree().getVisibleFields(), resourceFieldsHandler));

        // These handlers make sure the above tag handlers are only enabled if we are within a <view> tag.
        // Without them, the above tag handlers/listeners would do nothing.
        parser.addTagHandler(new ViewTagHandler("gantt-chart", myUIFacade, pilsenTaskDisplayHandler));
        parser.addTagHandler(new ViewTagHandler("resource-table", myUIFacade, resourceFieldsHandler));

        // This handler does only one thing: expanding all tasks in the view when parsing is finished
        parser.addParsingListener(new TaskParsingListener(taskManager, myUIFacade.getTaskTree()));

        // Handles view attributes of <project> tag
        ProjectViewAttrsTagHandler projectViewAttrsTagHandler = new ProjectViewAttrsTagHandler(myUIFacade);
        parser.addTagHandler(projectViewAttrsTagHandler);
        parser.addParsingListener(projectViewAttrsTagHandler);

        // Handles <timeline> tag, which contains a list of task IDs to show in the timeline
        TimelineTagHandler timelineTagHandler = new TimelineTagHandler(myUIFacade, taskManager);
        parser.addTagHandler(timelineTagHandler);
        parser.addParsingListener(timelineTagHandler);

        return parser;
    }
}
