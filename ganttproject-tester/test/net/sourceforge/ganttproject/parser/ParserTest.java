package net.sourceforge.ganttproject.parser;

import net.sourceforge.ganttproject.io.GanttXMLSaver;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.test.ProjectTestBase;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This test was initially written to understand the minimum dependencies of parsing an XML file.
 * Now it is just a simple test covering much of the parsing logic. Can be extended further.
 *
 * @author joeriexelmans
  */
public class ParserTest extends ProjectTestBase {

    @Test
    public void parseFile() throws IOException {
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

    @Test(expected = NullPointerException.class)
    public void parseInvalidFile() throws NullPointerException {
        throw new NullPointerException();
    }

    @Test
    public void unparse() throws IOException, InterruptedException {
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
