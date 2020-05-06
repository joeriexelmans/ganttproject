package net.sourceforge.ganttproject.parser;

import net.sourceforge.ganttproject.PrjInfos;
import org.xml.sax.Attributes;

/**
 * Parses non-UI attributes from &lt;project&gt; tag (=typically the root of a GanttProject document)
 */
public class ProjectTagHandler extends AbstractTagHandler {
    private PrjInfos myProjectInfo;

    public ProjectTagHandler(PrjInfos prjinfo) {
        super("project");
        myProjectInfo = prjinfo;
    }

    @Override
    protected boolean onStartElement(Attributes attrs) {
        myProjectInfo.setName(attrs.getValue("name"));
        myProjectInfo.setOrganization(attrs.getValue("company"));
        myProjectInfo.setWebLink(attrs.getValue("webLink"));
        return true;
    }
}
