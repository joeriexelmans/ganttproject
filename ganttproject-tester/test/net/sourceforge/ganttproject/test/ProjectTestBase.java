package net.sourceforge.ganttproject.test;

import biz.ganttproject.core.time.CalendarFactory;
import net.sourceforge.ganttproject.project.ProjectStub;
import net.sourceforge.ganttproject.parser.Parser;
import net.sourceforge.ganttproject.parser.ParserTest;
import net.sourceforge.ganttproject.project.Project;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Locale;

public abstract class ProjectTestBase {
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

    protected final static Project loadTestProject(String resourcePath) throws IOException {
        Parser parser = new Parser(null);
        InputStream is = ProjectTestBase.class.getResourceAsStream(resourcePath);
        Project project = new ProjectStub();
        parser.parse(project, is);
        return project;
    }
}
