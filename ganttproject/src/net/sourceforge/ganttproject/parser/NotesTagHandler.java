package net.sourceforge.ganttproject.parser;

/**
 * TagHandler for 'notes' of a task
 */
public class NotesTagHandler extends AbstractTagHandler {
    private final ParsingContext myContext;

    public NotesTagHandler(ParsingContext ctx) {
        super("notes", true);
        myContext = ctx;
    }

    @Override
    protected void onEndElement() {
        myContext.peekTask().setNotes(getCdata());
        clearCdata();
    }
}
