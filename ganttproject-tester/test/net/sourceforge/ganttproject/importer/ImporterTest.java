package net.sourceforge.ganttproject.importer;

import biz.ganttproject.core.calendar.ImportCalendarOption;
import net.sourceforge.ganttproject.CustomPropertyDefinition;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.resource.HumanResourceMerger;
import net.sourceforge.ganttproject.test.ProjectTestBase;

import java.io.IOException;
import java.util.List;

import static biz.ganttproject.core.calendar.ImportCalendarOption.Values.*;
import static net.sourceforge.ganttproject.resource.HumanResourceMerger.MergeResourcesOption.*;

public class ImporterTest extends ProjectTestBase {
    public void testImport() throws IOException {
        Project target = getTestProject("/testproject.xml");

        // We start with 2 resources
        assertEquals(2, target.getHumanResourceManager().getResources().size());

        // Project with a single resource defined
        Project source = getTestProject("/importable.xml");

        HumanResourceMerger.MergeResourcesOption mergeOption = new HumanResourceMerger.MergeResourcesOption();
        mergeOption.setValue(BY_NAME);

        ImportCalendarOption calendarOption = new ImportCalendarOption(MERGE);

        ImporterFromGanttFile.importProject(target, source, mergeOption, calendarOption);

        // 1 resource was imported
        assertEquals(3, target.getHumanResourceManager().getResources().size());

        // Imported resource had a custom property "age" defined,
        // this should be the only custom property in the merged project
        List<CustomPropertyDefinition> actualCustomProperties = target.getHumanResourceManager().getCustomPropertyManager().getDefinitions();
        assertEquals(1, actualCustomProperties.size());
        assertEquals("age", actualCustomProperties.get(0).getName());

        // Now check if the property is correctly set on the imported resource
        assertEquals("age", target.getHumanResourceManager().getResources().get(2).getCustomProperties().get(0).getDefinition().getName());
        assertEquals("60", target.getHumanResourceManager().getResources().get(2).getCustomProperties().get(0).getValue());
    }
}
