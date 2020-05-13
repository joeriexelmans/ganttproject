package net.sourceforge.ganttproject.parser;

import biz.ganttproject.core.calendar.GPCalendarCalc;
import biz.ganttproject.core.option.ListOption;
import net.sourceforge.ganttproject.document.ProxyDocument;
import net.sourceforge.ganttproject.gui.GPColorChooser;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.project.IProject;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.project.ProjectFactory;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.task.TaskManager;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * Like Parser, but also parses UI-related tags.
 */
public class UIParser extends Parser {
    UIFacade myUIFacade;
    
    public UIParser(ProjectFactory f, UIFacade uiFacade) {
        super(f);
        myUIFacade = uiFacade;
    }

    protected GPParser createParser(ParsingContext ctx, Project project) {
        GPParser parser = super.createParser(ctx, project);
//
//        HumanResourceManager hrManager = project.getHumanResourceManager();
//        RoleManager roleManager = project.getRoleManager();
//        TaskManager taskManager = project.getTaskManager();
//        GPCalendarCalc calendar = project.getActiveCalendar();
//
//        ResourceTagHandler resourceHandler = new ResourceTagHandler(hrManager, roleManager,
//                project.getResourceCustomPropertyManager());
//        DependencyTagHandler dependencyHandler = new DependencyTagHandler(ctx, taskManager);
//        AllocationTagHandler allocationHandler = new AllocationTagHandler(hrManager, taskManager, roleManager);
//        VacationTagHandler vacationHandler = new VacationTagHandler(hrManager);
//        PreviousStateTasksTagHandler previousStateHandler = new PreviousStateTasksTagHandler(project.getBaselines());
//        RoleTagHandler rolesHandler = new RoleTagHandler(roleManager);
//        TaskTagHandler taskHandler = new TaskTagHandler(taskManager, ctx);
//        TaskParsingListener taskParsingListener = new TaskParsingListener(taskManager, myUIFacade.getTaskTree());
//        DefaultWeekTagHandler weekHandler = new DefaultWeekTagHandler(calendar);
//        OnlyShowWeekendsTagHandler onlyShowWeekendsHandler = new OnlyShowWeekendsTagHandler(calendar);
//
//        TaskPropertiesTagHandler taskPropHandler = new TaskPropertiesTagHandler(taskManager.getCustomPropertyManager());
//        parser.addTagHandler(taskPropHandler);
//        CustomPropertiesTagHandler customPropHandler = new CustomPropertiesTagHandler(ctx, taskManager);
//        parser.addTagHandler(customPropHandler);
//
//
//        TaskDisplayColumnsTagHandler pilsenTaskDisplayHandler = TaskDisplayColumnsTagHandler.createPilsenHandler();
//        TaskDisplayColumnsTagHandler legacyTaskDisplayHandler = TaskDisplayColumnsTagHandler.createLegacyHandler();
//
//        parser.addTagHandler(pilsenTaskDisplayHandler);
//        parser.addTagHandler(legacyTaskDisplayHandler);
//        parser.addParsingListener(TaskDisplayColumnsTagHandler.createTaskDisplayColumnsWrapper(myTaskVisibleFields, pilsenTaskDisplayHandler, legacyTaskDisplayHandler));
//        parser.addTagHandler(new ViewTagHandler("gantt-chart", myUIFacade, pilsenTaskDisplayHandler));
//
//
//        TaskDisplayColumnsTagHandler resourceFieldsHandler = new TaskDisplayColumnsTagHandler(
//                "field", "id", "order", "width", "visible");
//        parser.addTagHandler(resourceFieldsHandler);
//        parser.addParsingListener(TaskDisplayColumnsTagHandler.createTaskDisplayColumnsWrapper(myResourceVisibleFields, resourceFieldsHandler));
//        parser.addTagHandler(new ViewTagHandler("resource-table", myUIFacade, resourceFieldsHandler));
//
//        parser.addTagHandler(taskHandler);
//        parser.addParsingListener(taskParsingListener);
//
//        parser.addParsingListener(customPropHandler);
//
//        parser.addTagHandler(new DescriptionTagHandler(project.getPrjInfos()));
//        parser.addTagHandler(new NotesTagHandler(ctx));
//        parser.addTagHandler(new ProjectTagHandler(project.getPrjInfos()));
//        parser.addTagHandler(new ProjectViewAttrsTagHandler(myUIFacade));
//        parser.addTagHandler(new TasksTagHandler(taskManager));
//
//        TimelineTagHandler timelineTagHandler = new TimelineTagHandler(myUIFacade, taskManager);
//        parser.addTagHandler(timelineTagHandler);
//        parser.addParsingListener(timelineTagHandler);
//
//        parser.addTagHandler(resourceHandler);
//        parser.addTagHandler(dependencyHandler);
//        parser.addTagHandler(allocationHandler);
//        parser.addParsingListener(allocationHandler);
//        parser.addTagHandler(vacationHandler);
//        parser.addTagHandler(previousStateHandler);
//        parser.addTagHandler(rolesHandler);
//        parser.addTagHandler(weekHandler);
//        parser.addTagHandler(onlyShowWeekendsHandler);
//        parser.addTagHandler(new OptionTagHandler<ListOption<Color>>(GPColorChooser.getRecentColorsOption()));
//        parser.addParsingListener(dependencyHandler);
//        parser.addParsingListener(resourceHandler);
//
//
//        HolidayTagHandler holidayHandler = new HolidayTagHandler(calendar);
//        parser.addTagHandler(new CalendarsTagHandler(calendar));
//        parser.addTagHandler(holidayHandler);
//
//        ProxyDocument.PortfolioTagHandler portfolioHandler = new ProxyDocument.PortfolioTagHandler();
//        parser.addTagHandler(portfolioHandler);


        return parser;
    }
}
