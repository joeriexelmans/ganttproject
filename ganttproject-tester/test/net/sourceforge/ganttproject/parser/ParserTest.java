package net.sourceforge.ganttproject.parser;

import net.sourceforge.ganttproject.io.GanttXMLSaver;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.test.ProjectTestBase;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * This test was written to understand the minimum dependencies of parsing an XML file,
 * specifically, if we can parse an XML file apart from any UI stuff.
 * For the usage of the tag handlers, we base ourselves on ProxyDocument.ParsingState.enter()
 *
 * @author joeriexelmans
  */
public class ParserTest extends ProjectTestBase {

    public void testParser() throws IOException {
        Project project = getTestProject("/testproject.xml");

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
        Project project = getTestProject("/testproject.xml");

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
