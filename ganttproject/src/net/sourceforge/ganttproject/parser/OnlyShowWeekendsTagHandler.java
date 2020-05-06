package net.sourceforge.ganttproject.parser;

import biz.ganttproject.core.calendar.GPCalendarCalc;
import org.xml.sax.Attributes;

public class OnlyShowWeekendsTagHandler extends AbstractTagHandler {

  private final GPCalendarCalc calendar;

  public OnlyShowWeekendsTagHandler(GPCalendarCalc calendar) {
    super("only-show-weekends");
    this.calendar = calendar;
  }

  @Override
  protected boolean onStartElement(Attributes attrs) {
    calendar.setOnlyShowWeekends(Boolean.parseBoolean(attrs.getValue("value")));
    return true;
  }
}
