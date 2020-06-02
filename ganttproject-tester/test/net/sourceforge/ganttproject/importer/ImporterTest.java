package net.sourceforge.ganttproject.importer;

import biz.ganttproject.core.calendar.ImportCalendarOption;
import net.sourceforge.ganttproject.CustomProperty;
import net.sourceforge.ganttproject.CustomPropertyDefinition;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.resource.HumanResourceMerger;
import net.sourceforge.ganttproject.test.ProjectTestBase;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static net.sourceforge.ganttproject.resource.HumanResourceMerger.MergeResourcesOption.*;
import static org.junit.Assert.assertEquals;

// Individual test methods pass if ran independently, however, some tests fail if the tests are ran in sequence
// Probably earlier tests leave the "state" dirty, don't understand how...
public class ImporterTest extends ProjectTestBase {

    private void testVariant(ImportCalendarOption calendarOption, HumanResourceMerger.MergeResourcesOption mergeOption, int expectedNumResources) throws IOException {
        // Project with 2 human resources: Jimmy and Bobby
        Project target = loadTestProject("/testproject.xml");

        // We start with 2 resources
        assertEquals(2, target.getHumanResourceManager().getResources().size());

        // Project with a single resource defined: Louis, with custom property 'age' set to '60'.
        Project source = loadTestProject("/importable.xml");

        ImporterFromGanttFile.importProject(target, source, mergeOption, calendarOption);

        // Print custom properties
        System.out.println("custom properties...");
        System.out.println(target.getResourceCustomPropertyManager().getDefinitions().size());
        for (CustomPropertyDefinition customPropertyDef: target.getResourceCustomPropertyManager().getDefinitions()) {
            System.out.println(customPropertyDef.getName());
        }

        // Print resources
        System.out.println("resources...");
        System.out.println(target.getHumanResourceManager().getResources().size());
        for (HumanResource hr: target.getHumanResourceManager().getResources()) {
            System.out.println(hr.getName());
            for (CustomProperty c: hr.getCustomProperties()) {
                System.out.println(c.getDefinition().getName());
                System.out.println(c.getValueAsString());
            }
        }

        // 1 resource was imported
        assertEquals(expectedNumResources, target.getHumanResourceManager().getResources().size());

        // Imported resource had a custom property "age" defined,
        // this should be the only custom property in the merged project
        List<CustomPropertyDefinition> actualCustomProperties = target.getHumanResourceManager().getCustomPropertyManager().getDefinitions();
        assertEquals(1, actualCustomProperties.size());
        assertEquals("age", actualCustomProperties.get(0).getName());

        // Now check if the property is correctly set on the imported resource

        for (HumanResource hr: target.getHumanResourceManager().getResources()) {
            if (hr.getName().equals("Louis")) {
                assertEquals("age", hr.getCustomProperties().get(0).getDefinition().getName());
                assertEquals("60", hr.getCustomProperties().get(0).getValue());
            }
        }
    }

    // Tests for all possible combinations of merge options

    @Test
    public void importNameMerge() throws IOException {
        HumanResourceMerger.MergeResourcesOption o = new HumanResourceMerger.MergeResourcesOption();
        o.setValue(BY_NAME);
        testVariant(new ImportCalendarOption(ImportCalendarOption.Values.MERGE), o, 3);
    }

    // None of the resource have e-mail set, so the following doesn't work
    @Test
    public void importEmailMerge() throws IOException {
        HumanResourceMerger.MergeResourcesOption o = new HumanResourceMerger.MergeResourcesOption();
        o.setValue(BY_EMAIL);
        testVariant(new ImportCalendarOption(ImportCalendarOption.Values.MERGE), o, 2);
    }

    // Doesn't work because resource IDs tend to be non-unique across different projects
    @Test
    public void importIdMerge() throws IOException {
        HumanResourceMerger.MergeResourcesOption o = new HumanResourceMerger.MergeResourcesOption();
        o.setValue(BY_ID);
        testVariant(new ImportCalendarOption(ImportCalendarOption.Values.MERGE), o, 2);
    }


}
