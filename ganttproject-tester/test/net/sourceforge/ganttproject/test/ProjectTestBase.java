package net.sourceforge.ganttproject.test;

import biz.ganttproject.core.time.CalendarFactory;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
import junit.framework.TestCase;
import net.sourceforge.ganttproject.ProjectStub;
import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.parser.Parser;
import net.sourceforge.ganttproject.parser.ParserTest;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.project.ProjectFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Locale;

public abstract class ProjectTestBase extends TestCase {
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

    protected final static ProjectFactory factory = new ProjectFactory(){
        public Project newProject() {
            return new ProjectStub();
        }
    };

    protected final static Project getTestProject(String resourcePath) throws IOException {
        Parser parser = new Parser(null);
        InputStream is = ParserTest.class.getResourceAsStream(resourcePath);
        Project project = factory.newProject();
        parser.parse(project, is);
        return project;
    }
}
