package net.sourceforge.ganttproject.parser;

import biz.ganttproject.core.time.CalendarFactory;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
import junit.framework.TestCase;
import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.io.GanttXMLSaver;
import net.sourceforge.ganttproject.project.IProject;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.project.ProjectFactory;
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
public class ParserTest extends TestCase {
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

    private final static ProjectFactory factory = new ProjectFactory(){
        public Project newProject() {
            return new Project(
                    new GPTimeUnitStack(),
                    null,
                    new TestSetupHelper.TaskManagerTestConfig());
        }
    };

    private IProject project;

    public void testParser() throws IOException {
        Parser parser = new Parser(null);
        InputStream is = ParserTest.class.getResourceAsStream("/testproject.xml");
        project = factory.newProject();
        parser.parse(project, is);

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
        // parse our project like in testParser
        Parser parser = new Parser(null);
        InputStream is = ParserTest.class.getResourceAsStream("/testproject.xml");
        project = factory.newProject();
        parser.parse(project, is);

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
