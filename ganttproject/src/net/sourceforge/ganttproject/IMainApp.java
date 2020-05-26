package net.sourceforge.ganttproject;

import biz.ganttproject.core.time.TimeUnitStack;
import net.sourceforge.ganttproject.action.GPAction;
import net.sourceforge.ganttproject.document.Document;
import net.sourceforge.ganttproject.document.DocumentManager;
import net.sourceforge.ganttproject.gui.UIConfiguration;
import net.sourceforge.ganttproject.project.IProject;

import java.io.IOException;

/**
 * Represents the 'main app' with all project domain model logic left out
 */
public interface IMainApp {
    TimeUnitStack getTimeUnitStack();

    // Get the currently opened project
    public IProject getCurrentProject();

    // main app logic
    void open(Document document) throws IOException, Document.DocumentException;
    void close();
    void setModified();
    void setModified(boolean modified);
    boolean isModified();

    // low level hacks?
    Document getDocument();
    void setDocument(Document document); // used by UndoableEditImpl
    DocumentManager getDocumentManager();

    void addProjectEventListener(ProjectEventListener listener);
    void removeProjectEventListener(ProjectEventListener listener);

    // UI
    UIConfiguration getUIConfiguration();


    public GPAction getCopyAction();
    public GPAction getCutAction();
    public GPAction getPasteAction();
    public void refreshProjectInformation();
}
