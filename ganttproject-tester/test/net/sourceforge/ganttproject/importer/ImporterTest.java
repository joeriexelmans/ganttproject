package net.sourceforge.ganttproject.importer;

import biz.ganttproject.core.calendar.ImportCalendarOption;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.resource.HumanResourceMerger;
import net.sourceforge.ganttproject.test.ProjectTestBase;

import java.io.IOException;

import static biz.ganttproject.core.calendar.ImportCalendarOption.Values.*;
import static net.sourceforge.ganttproject.resource.HumanResourceMerger.MergeResourcesOption.*;

public class ImporterTest extends ProjectTestBase {
    public void testImport() throws IOException {
        Project target = getTestProject("/testproject.gan");

        assertEquals(2, target.getHumanResourceManager().getResources().size());

        // Project with a single resource defined
        Project source = getTestProject("/importable.gan");

        HumanResourceMerger.MergeResourcesOption mergeOption = new HumanResourceMerger.MergeResourcesOption();
        mergeOption.setValue(BY_NAME);

        ImportCalendarOption calendarOption = new ImportCalendarOption(MERGE);

        ImporterFromGanttFile.importProject(target, source, mergeOption, calendarOption);

        assertEquals(3, target.getHumanResourceManager().getResources().size());
    }
}
