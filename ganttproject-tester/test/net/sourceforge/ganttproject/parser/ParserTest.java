package net.sourceforge.ganttproject.parser;

import biz.ganttproject.core.time.CalendarFactory;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
import junit.framework.TestCase;
import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.io.GanttXMLSaver;
import net.sourceforge.ganttproject.project.IProject;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.project.ProjectFactory;
import net.sourceforge.ganttproject.test.ProjectTestBase;
import net.sourceforge.ganttproject.update.UpdateParserTest;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Locale;

/**
 * This test was written to understand the minimum dependencies of parsing an XML file,
 * specifically, if we can parse an XML file apart from any UI stuff.
 * For the usage of the tag handlers, we base ourselves on ProxyDocument.ParsingState.enter()
 *
 * @author joeriexelmans
  */
public class ParserTest extends ProjectTestBase {

    public void testParser() throws IOException {
        Project project = getTestProject("/testproject.gan");

        // task and resource count
        assertEquals(5, project.getTaskManager().getTaskCount());
        assertEquals(2, project.getHumanResourceManager().getResources().size());

        // allocations
        assertEquals(1, project.getTaskManager().getTask(1).getAssignments().length);
        assertEquals(1, project.getHumanResourceManager().getById(0).getAssignments().length);

        assertTrue(project.getTaskManager().getTask(1).getAssignments()[0].getResource() == project.getHumanResourceManager().getById(0));
        assertTrue(project.getHumanResourceManager().getById(0).getAssignments()[0].getTask() == project.getTaskManager().getTask(1));

        // baseline count
        assertEquals(2, project.getBaselines().size());
    }

    public void testUnparser() throws IOException, InterruptedException {
        Project project = getTestProject("/testproject.gan");

        // now serialize it
        GanttXMLSaver saver = new GanttXMLSaver(project);
        PipedOutputStream os = new PipedOutputStream();
        PipedInputStream pipe = new PipedInputStream();
        os.connect(pipe);

        // run serialization in separate thread
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    saver.save(os);
                } catch (IOException e) {
                }
            }
        });
        t.start();

        InputStream expected = ParserTest.class.getResourceAsStream("/expected_testproject.xml");

        try {
            assertTrue(IOUtils.contentEquals(expected, pipe));
        } finally {
            pipe.close(); // this will make the serialization thread fail if it is still running (trying to write)
            t.join();
        }
    }
}
