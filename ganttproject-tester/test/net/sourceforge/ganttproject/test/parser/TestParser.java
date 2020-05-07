package net.sourceforge.ganttproject.test.parser;

import biz.ganttproject.core.calendar.WeekendCalendarImpl;
import biz.ganttproject.core.option.ListOption;
import biz.ganttproject.core.time.CalendarFactory;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
import junit.framework.TestCase;
import net.sourceforge.ganttproject.GanttPreviousState;
import net.sourceforge.ganttproject.PrjInfos;
import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.document.Document;
import net.sourceforge.ganttproject.document.ProxyDocument;
import net.sourceforge.ganttproject.gui.GPColorChooser;
import net.sourceforge.ganttproject.gui.UIConfiguration;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.io.GanttXMLOpen;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.parser.*;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.roles.RoleManagerImpl;
import net.sourceforge.ganttproject.task.CustomColumnsManager;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.TaskManagerImpl;
import org.xml.sax.Attributes;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This test was written to understand the minimum dependencies of parsing an XML file,
 * specifically, if we can parse an XML file apart from any UI stuff.
 * For the usage of the tag handlers, we base ourselves on ProxyDocument.ParsingState.enter()
 *
 * @author joeriexelmans
  */
public class TestParser extends TestCase {
    private static final String projectFile =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Untitled Gantt Project\" company=\"\" webLink=\"http://\" view-date=\"2020-04-22\" view-index=\"0\" gantt-divider-location=\"420\" resource-divider-location=\"300\" version=\"2.8.10\" locale=\"en\">\n" +
            "    <description/>\n" +
            "    <view zooming-state=\"default:2\" id=\"gantt-chart\">\n" +
            "        <field id=\"tpd3\" name=\"Name\" width=\"157\" order=\"0\"/>\n" +
            "        <field id=\"tpd4\" name=\"Begin date\" width=\"81\" order=\"1\"/>\n" +
            "        <field id=\"tpd5\" name=\"End date\" width=\"81\" order=\"2\"/>\n" +
            "        <field id=\"tpc0\" name=\"category\" width=\"97\" order=\"3\"/>\n" +
            "        <option id=\"taskLabelDown\" value=\"advancement\"/>\n" +
            "    </view>\n" +
            "    <view id=\"resource-table\">\n" +
            "        <field id=\"0\" name=\"Name\" width=\"210\" order=\"0\"/>\n" +
            "        <field id=\"1\" name=\"Default role\" width=\"86\" order=\"1\"/>\n" +
            "    </view>\n" +
            "    <!-- -->\n" +
            "    <calendars>\n" +
            "        <day-types>\n" +
            "            <day-type id=\"0\"/>\n" +
            "            <day-type id=\"1\"/>\n" +
            "            <default-week id=\"1\" name=\"default\" sun=\"1\" mon=\"0\" tue=\"0\" wed=\"0\" thu=\"0\" fri=\"0\" sat=\"1\"/>\n" +
            "            <only-show-weekends value=\"false\"/>\n" +
            "            <overriden-day-types/>\n" +
            "            <days/>\n" +
            "        </day-types>\n" +
            "    </calendars>\n" +
            "    <tasks empty-milestones=\"true\">\n" +
            "        <taskproperties>\n" +
            "            <taskproperty id=\"tpd0\" name=\"type\" type=\"default\" valuetype=\"icon\"/>\n" +
            "            <taskproperty id=\"tpd1\" name=\"priority\" type=\"default\" valuetype=\"icon\"/>\n" +
            "            <taskproperty id=\"tpd2\" name=\"info\" type=\"default\" valuetype=\"icon\"/>\n" +
            "            <taskproperty id=\"tpd3\" name=\"name\" type=\"default\" valuetype=\"text\"/>\n" +
            "            <taskproperty id=\"tpd4\" name=\"begindate\" type=\"default\" valuetype=\"date\"/>\n" +
            "            <taskproperty id=\"tpd5\" name=\"enddate\" type=\"default\" valuetype=\"date\"/>\n" +
            "            <taskproperty id=\"tpd6\" name=\"duration\" type=\"default\" valuetype=\"int\"/>\n" +
            "            <taskproperty id=\"tpd7\" name=\"completion\" type=\"default\" valuetype=\"int\"/>\n" +
            "            <taskproperty id=\"tpd8\" name=\"coordinator\" type=\"default\" valuetype=\"text\"/>\n" +
            "            <taskproperty id=\"tpd9\" name=\"predecessorsr\" type=\"default\" valuetype=\"text\"/>\n" +
            "            <taskproperty id=\"tpc0\" name=\"category\" type=\"custom\" valuetype=\"text\"/>\n" +
            "        </taskproperties>\n" +
            "        <task id=\"0\" name=\"Reengineering project\" color=\"#000000\" meeting=\"false\" start=\"2020-04-22\" duration=\"3\" complete=\"0\" thirdDate=\"2020-04-22\" thirdDate-constraint=\"0\" expand=\"true\">\n" +
            "            <depend id=\"3\" type=\"2\" difference=\"0\" hardness=\"Strong\"/>\n" +
            "            <task id=\"1\" name=\"Refactoring plan\" color=\"#8cb6ce\" meeting=\"false\" start=\"2020-04-22\" duration=\"1\" complete=\"0\" thirdDate=\"2020-04-22\" thirdDate-constraint=\"0\" expand=\"true\">\n" +
            "                <depend id=\"2\" type=\"2\" difference=\"0\" hardness=\"Strong\"/>\n" +
            "                <customproperty taskproperty-id=\"tpc0\" value=\"planning\"/>\n" +
            "            </task>\n" +
            "            <task id=\"2\" name=\"Actual refactoring\" color=\"#8cb6ce\" meeting=\"false\" start=\"2020-04-23\" duration=\"2\" complete=\"0\" thirdDate=\"2020-04-22\" thirdDate-constraint=\"0\" expand=\"true\"/>\n" +
            "        </task>\n" +
            "        <task id=\"3\" name=\"stuff\" color=\"#8cb6ce\" meeting=\"false\" start=\"2020-04-27\" duration=\"1\" complete=\"0\" thirdDate=\"2020-04-22\" thirdDate-constraint=\"0\" expand=\"true\"/>\n" +
            "    </tasks>\n" +
            "    <resources>\n" +
            "        <resource id=\"0\" name=\"Bobby\" function=\"Default:0\" contacts=\"\" phone=\"\"/>\n" +
            "        <resource id=\"1\" name=\"Jimmy\" function=\"Default:1\" contacts=\"\" phone=\"\"/>\n" +
            "    </resources>\n" +
            "    <allocations>\n" +
            "        <allocation task-id=\"1\" resource-id=\"0\" function=\"Default:0\" responsible=\"true\" load=\"100.0\"/>\n" +
            "        <allocation task-id=\"2\" resource-id=\"1\" function=\"Default:0\" responsible=\"true\" load=\"100.0\"/>\n" +
            "    </allocations>\n" +
            "    <vacations/>\n" +
            "    <previous>\n" +
            "        <previous-tasks name=\"original intent\">\n" +
            "            <previous-task id=\"0\" start=\"2020-04-22\" duration=\"2\" meeting=\"false\" super=\"true\"/>\n" +
            "            <previous-task id=\"1\" start=\"2020-04-22\" duration=\"1\" meeting=\"false\" super=\"false\"/>\n" +
            "            <previous-task id=\"2\" start=\"2020-04-23\" duration=\"1\" meeting=\"false\" super=\"false\"/>\n" +
            "        </previous-tasks>\n" +
            "        <previous-tasks name=\"new intent\">\n" +
            "            <previous-task id=\"0\" start=\"2020-04-22\" duration=\"3\" meeting=\"false\" super=\"true\"/>\n" +
            "            <previous-task id=\"1\" start=\"2020-04-22\" duration=\"1\" meeting=\"false\" super=\"false\"/>\n" +
            "            <previous-task id=\"2\" start=\"2020-04-23\" duration=\"2\" meeting=\"false\" super=\"false\"/>\n" +
            "            <previous-task id=\"3\" start=\"2020-04-27\" duration=\"1\" meeting=\"false\" super=\"false\"/>\n" +
            "        </previous-tasks>\n" +
            "    </previous>\n" +
            "    <roles roleset-name=\"Default\"/>\n" +
            "</project>";

    // Apparently this is how one "sets up" the locale for the calendar...
    static {
        new CalendarFactory() {
            {
                setLocaleApi(new LocaleApi() {
                    public Locale getLocale() {
                        return Locale.US;
                    }
                    public DateFormat getShortDateFormat() {
                        return DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
                    }
                });
            }
        };
    }

    public void testParser() throws IOException {
        // All the stuff that supposedly makes up "a project"
        WeekendCalendarImpl calendar = new WeekendCalendarImpl();
        GPTimeUnitStack timeUnitStack = new GPTimeUnitStack();
        RoleManagerImpl roleManager = new RoleManagerImpl();
        CustomColumnsManager hrCustomPropertyManager = new CustomColumnsManager();
        HumanResourceManager hrManager = new HumanResourceManager(roleManager.getDefaultRole(), hrCustomPropertyManager, roleManager);
        TaskManager taskManager = TaskManager.Access.newInstance(null, hrManager, calendar, timeUnitStack, new TestSetupHelper.TaskManagerTestConfig());
        PrjInfos prjinfos = new PrjInfos();
        ArrayList<GanttPreviousState> baseLines = new ArrayList<GanttPreviousState>();

        // Parse...
        GPParser opener = new GanttXMLOpen();
        ParsingContext ctx = new ParsingContext();
        ResourceTagHandler resourceHandler = new ResourceTagHandler(hrManager, roleManager,
                hrCustomPropertyManager);
        DependencyTagHandler dependencyHandler = new DependencyTagHandler(ctx, taskManager);
        AllocationTagHandler allocationHandler = new AllocationTagHandler(hrManager, taskManager, roleManager);
        VacationTagHandler vacationHandler = new VacationTagHandler(hrManager);
        PreviousStateTasksTagHandler previousStateHandler = new PreviousStateTasksTagHandler(baseLines);
        RoleTagHandler rolesHandler = new RoleTagHandler(roleManager);
        TaskTagHandler taskHandler = new TaskTagHandler(taskManager, ctx);
        DefaultWeekTagHandler weekHandler = new DefaultWeekTagHandler(calendar);
        OnlyShowWeekendsTagHandler onlyShowWeekendsHandler = new OnlyShowWeekendsTagHandler(calendar);

        TaskPropertiesTagHandler taskPropHandler = new TaskPropertiesTagHandler(taskManager.getCustomPropertyManager());
        opener.addTagHandler(taskPropHandler);
        CustomPropertiesTagHandler customPropHandler = new CustomPropertiesTagHandler(ctx, taskManager);
        opener.addTagHandler(customPropHandler);

        TaskDisplayColumnsTagHandler pilsenTaskDisplayHandler = TaskDisplayColumnsTagHandler.createPilsenHandler();
        TaskDisplayColumnsTagHandler legacyTaskDisplayHandler = TaskDisplayColumnsTagHandler.createLegacyHandler();

        opener.addTagHandler(pilsenTaskDisplayHandler);
        opener.addTagHandler(legacyTaskDisplayHandler);

        TaskDisplayColumnsTagHandler resourceFieldsHandler = new TaskDisplayColumnsTagHandler(
                "field", "id", "order", "width", "visible");
        opener.addTagHandler(resourceFieldsHandler);

        opener.addTagHandler(taskHandler);
        opener.addParsingListener(customPropHandler);
        opener.addTagHandler(new DescriptionTagHandler(prjinfos));
        opener.addTagHandler(new NotesTagHandler(ctx));
        opener.addTagHandler(new ProjectTagHandler(prjinfos));
        opener.addTagHandler(new TasksTagHandler(taskManager));

        opener.addTagHandler(resourceHandler);
        opener.addTagHandler(dependencyHandler);
        opener.addTagHandler(allocationHandler);
        opener.addParsingListener(allocationHandler);
        opener.addTagHandler(vacationHandler);
        opener.addTagHandler(previousStateHandler);
        opener.addTagHandler(rolesHandler);
        opener.addTagHandler(weekHandler);
        opener.addTagHandler(onlyShowWeekendsHandler);
        opener.addTagHandler(new OptionTagHandler<ListOption<Color>>(GPColorChooser.getRecentColorsOption()));
        opener.addParsingListener(dependencyHandler);
        opener.addParsingListener(resourceHandler);

        HolidayTagHandler holidayHandler = new HolidayTagHandler(calendar);
        opener.addTagHandler(new CalendarsTagHandler(calendar));
        opener.addTagHandler(holidayHandler);
        InputStream is = new ByteArrayInputStream(projectFile.getBytes(StandardCharsets.UTF_8));
        opener.load(is);

        assertEquals(4, taskManager.getTaskCount());
        assertEquals(2, hrManager.getResources().size());
        assertEquals(1, taskManager.getTask(1).getAssignments().length);
        assertEquals(0, taskManager.getTask(1).getAssignments()[0].getResource().getId());
    }


    public void setUp() throws Exception {
    }
}
