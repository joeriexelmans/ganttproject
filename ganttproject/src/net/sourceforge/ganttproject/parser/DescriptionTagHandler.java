package net.sourceforge.ganttproject.parser;

import net.sourceforge.ganttproject.PrjInfos;

/**
 * Parses and sets project description
 */
public class DescriptionTagHandler extends AbstractTagHandler {
    private PrjInfos prjInfos;

    public DescriptionTagHandler(PrjInfos prji) {
        super("description", true);
        prjInfos = prji;
    }

    @Override
    protected void onEndElement() {
        prjInfos.setDescription(getCdata());
        clearCdata();
    }
}
