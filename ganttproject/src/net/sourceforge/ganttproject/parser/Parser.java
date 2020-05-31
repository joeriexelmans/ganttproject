package net.sourceforge.ganttproject.parser;

import biz.ganttproject.core.calendar.GPCalendarCalc;
import biz.ganttproject.core.option.ListOption;
import net.sourceforge.ganttproject.CustomPropertyManager;
import net.sourceforge.ganttproject.PrjInfos;
import net.sourceforge.ganttproject.document.DocumentManager;
import net.sourceforge.ganttproject.gui.GPColorChooser;
import net.sourceforge.ganttproject.io.GanttXMLOpen;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.task.TaskManager;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 *  Parse function for GanttProject files with tag handlers for all domain model entities,
 *  not including tag handlers that depend on the UI.
 */
public class Parser {
    DocumentManager documentManager;

    public Parser(DocumentManager docManager) {
        documentManager = docManager;
    }

    /**
     * Constructs a new Project object from parsing a stream in XML format.
     * @param project A blank project
     * @param is Input stream for reading XML data from
     * @throws IOException
     */
    public final void parse(Project project, InputStream is) throws IOException {
        ParsingContext ctx = new ParsingContext();
        GPParser parser = this.createParser(ctx, project);

        // "special" tag handler
        PortfolioTagHandler portfolioTagHandler = new PortfolioTagHandler(documentManager);
        parser.addTagHandler(portfolioTagHandler);

        parser.load(is);

        if (portfolioTagHandler.getDefaultDocument() != null) {
            // we're dealing with a portfolio, not with a document
            parse(project, portfolioTagHandler.getDefaultDocument().getInputStream());
        }
    }

    // The "overridable" part
    protected GPParser createParser(ParsingContext ctx, Project project) {
        TaskManager taskManager = project.getTaskManager();
        HumanResourceManager hrManager = project.getHumanResourceManager();
        CustomPropertyManager hrCustomPropertyManager = project.getResourceCustomPropertyManager();
        RoleManager roleManager = project.getRoleManager();
        GPCalendarCalc calendar = project.getActiveCalendar();
        PrjInfos prjInfos = project.getPrjInfos();

        GPParser parser = new GanttXMLOpen();

        parser.addTagHandler(new TaskTagHandler(taskManager, ctx));
        parser.addTagHandler(new TaskPropertiesTagHandler(taskManager.getCustomPropertyManager()));
        parser.addTagHandler(new DescriptionTagHandler(prjInfos));
        parser.addTagHandler(new NotesTagHandler(ctx));
        parser.addTagHandler(new ProjectTagHandler(prjInfos));
        parser.addTagHandler(new TasksTagHandler(taskManager));
        parser.addTagHandler(new VacationTagHandler(hrManager));
        parser.addTagHandler(new PreviousStateTasksTagHandler(project.getBaselines()));
        parser.addTagHandler(new RoleTagHandler(roleManager));
        parser.addTagHandler(new DefaultWeekTagHandler(calendar));
        parser.addTagHandler(new OnlyShowWeekendsTagHandler(calendar));
        parser.addTagHandler(new OptionTagHandler<ListOption<Color>>(GPColorChooser.getRecentColorsOption()));
        parser.addTagHandler(new CalendarsTagHandler(calendar));
        parser.addTagHandler(new HolidayTagHandler(calendar));

        CustomPropertiesTagHandler customPropHandler = new CustomPropertiesTagHandler(ctx, taskManager);
        ResourceTagHandler resourceHandler = new ResourceTagHandler(hrManager, roleManager,
                hrCustomPropertyManager);
        DependencyTagHandler dependencyHandler = new DependencyTagHandler(ctx, taskManager);
        AllocationTagHandler allocationHandler = new AllocationTagHandler(hrManager, taskManager, roleManager);

        parser.addTagHandler(customPropHandler);
        parser.addTagHandler(resourceHandler);
        parser.addTagHandler(dependencyHandler);
        parser.addTagHandler(allocationHandler);
        parser.addParsingListener(customPropHandler);
        parser.addParsingListener(allocationHandler);
        parser.addParsingListener(dependencyHandler);
        parser.addParsingListener(resourceHandler);

        return parser;
    }
}
