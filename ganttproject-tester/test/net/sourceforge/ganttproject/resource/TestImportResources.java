package net.sourceforge.ganttproject.resource;

import net.sourceforge.ganttproject.assignment.AssignmentManager;
import net.sourceforge.ganttproject.resource.HumanResourceMerger.MergeResourcesOption;
import net.sourceforge.ganttproject.task.CustomColumnsManager;
import junit.framework.TestCase;

public class TestImportResources extends TestCase {

    public void testMergeResourcesByName() {
        MergeResourcesOption mergeOption = new MergeResourcesOption();
        mergeOption.setValue(MergeResourcesOption.BY_NAME);

        AssignmentManager am = new AssignmentManager();
        HumanResourceManager mergeTo = new HumanResourceManager(am, null, new CustomColumnsManager());
        mergeTo.add(new HumanResource("joe", 1, mergeTo, am));
        mergeTo.add(new HumanResource("john", 2, mergeTo, am));

        HumanResourceManager mergeFrom = new HumanResourceManager(am, null, new CustomColumnsManager());
        mergeFrom.add(new HumanResource("jack", 1, mergeFrom, am));
        mergeFrom.add(new HumanResource("joe", 2, mergeFrom, am));

        mergeTo.importData(mergeFrom, new OverwritingMerger(mergeOption));

        assertEquals(3, mergeTo.getResources().size());
        assertEquals("joe", mergeTo.getById(1).getName());
        assertEquals("john", mergeTo.getById(2).getName());
        assertEquals("jack", mergeTo.getById(3).getName());
    }

    public void testMergeByID() {
        MergeResourcesOption mergeOption = new MergeResourcesOption();
        mergeOption.setValue(MergeResourcesOption.BY_ID);

        AssignmentManager am = new AssignmentManager();
        HumanResourceManager mergeTo = new HumanResourceManager(am, null, new CustomColumnsManager());
        mergeTo.add(new HumanResource("joe", 1, mergeTo, am));
        mergeTo.add(new HumanResource("john", 2, mergeTo, am));

        HumanResourceManager mergeFrom = new HumanResourceManager(am, null, new CustomColumnsManager());
        mergeFrom.add(new HumanResource("jack", 1, mergeFrom, am));
        mergeFrom.add(new HumanResource("joe", 3, mergeFrom, am));

        mergeTo.importData(mergeFrom, new OverwritingMerger(mergeOption));

        assertEquals(3, mergeTo.getResources().size());
        assertEquals("jack", mergeTo.getById(1).getName());
        assertEquals("john", mergeTo.getById(2).getName());
        assertEquals("joe", mergeTo.getById(3).getName());
    }


}
