package net.sourceforge.ganttproject.parser;

import biz.ganttproject.core.calendar.GPCalendarCalc;
import org.xml.sax.Attributes;

public class CalendarsTagHandler extends AbstractTagHandler {
    private final GPCalendarCalc myCalendar;

    public CalendarsTagHandler(GPCalendarCalc cal) {
        super("calendars");
        myCalendar = cal;
    }

    @Override
    protected boolean onStartElement(Attributes attrs) {
        myCalendar.setBaseCalendarID(attrs.getValue("base-id"));
        return true;
    }
}
