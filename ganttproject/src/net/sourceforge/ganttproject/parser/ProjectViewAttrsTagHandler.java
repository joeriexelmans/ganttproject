package net.sourceforge.ganttproject.parser;

import biz.ganttproject.core.time.GanttCalendar;
import net.sourceforge.ganttproject.gui.UIFacade;
import org.xml.sax.Attributes;

/**
 * TagHandler for view attributes from &lt;project&gt;
 */
public class ProjectViewAttrsTagHandler extends AbstractTagHandler implements ParsingListener {
    private UIFacade myUIFacade;
    private int viewIndex = 0;
    private int ganttDividerLocation = 300; // TODO is this arbitrary value right ?
    private int resourceDividerLocation = 300;

    public ProjectViewAttrsTagHandler(UIFacade ui) {
        super("project");
        myUIFacade = ui;
    }

    @Override
    protected boolean onStartElement(Attributes attrs) {
        String attrViewDate = attrs.getValue("view-date");
        String attrViewIndex = attrs.getValue("view-index");
        String attrGanttDivLoc = attrs.getValue("gantt-divider-location");
        String attrResDivLoc = attrs.getValue("resource-divider-location");

        if (attrViewDate != null) {
            myUIFacade.getScrollingManager().scrollTo(GanttCalendar.parseXMLDate(attrViewDate).getTime());
        }
        if (attrViewIndex != null) {
            viewIndex = new Integer(attrs.getValue(attrViewIndex)).hashCode();
        }
        if (attrGanttDivLoc != null) {
            ganttDividerLocation = new Integer(attrs.getValue(attrGanttDivLoc)).intValue();
        }
        if (attrResDivLoc != null) {
            resourceDividerLocation = new Integer(attrs.getValue(attrResDivLoc)).intValue();
        }
        return true;
    }

    @Override
    public void parsingStarted() {
    }

    @Override
    public void parsingFinished() {
        myUIFacade.setViewIndex(viewIndex);
        myUIFacade.setGanttDividerLocation(ganttDividerLocation);
        if (resourceDividerLocation != 0) {
            myUIFacade.setResourceDividerLocation(resourceDividerLocation);
        }
    }
}
