/*
GanttProject is an opensource project management tool.
Copyright (C) 2005-2011 GanttProject Team

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sourceforge.ganttproject.action.edit;

import net.sourceforge.ganttproject.GPTransferable;
import net.sourceforge.ganttproject.action.GPAction;
import net.sourceforge.ganttproject.chart.ChartSelection;
import net.sourceforge.ganttproject.document.DocumentManager;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.UIUtil;
import net.sourceforge.ganttproject.gui.view.GPViewManager;
import net.sourceforge.ganttproject.project.BufferProject;
import net.sourceforge.ganttproject.importer.ImporterFromGanttFile;
import net.sourceforge.ganttproject.parser.UIParser;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.resource.HumanResourceMerger;
import net.sourceforge.ganttproject.undo.GPUndoManager;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.InputStream;

//TODO Enable/Disable action depending on clipboard contents
public class PasteAction extends GPAction {
  private final GPViewManager myViewmanager;
  private final GPUndoManager myUndoManager;
  private final Project myProject;
  private final UIFacade myUiFacade;
  private final DocumentManager myDocumentManager;

  public PasteAction(Project project, UIFacade uiFacade, DocumentManager documentManager, GPViewManager viewManager, GPUndoManager undoManager) {
    super("paste");
    myViewmanager = viewManager;
    myUndoManager = undoManager;
    myProject = project;
    myUiFacade = uiFacade;
    myDocumentManager = documentManager;
  }

  private PasteAction(Project project, UIFacade uiFacade, DocumentManager documentManager,GPViewManager viewmanager, GPUndoManager undoManager, IconSize size) {
    super("paste", size);
    myViewmanager = viewmanager;
    myUndoManager = undoManager;
    myProject = project;
    myUiFacade = uiFacade;
    myDocumentManager = documentManager;
  }

  @Override
  public GPAction withIcon(IconSize size) {
    return new PasteAction(myProject, myUiFacade, myDocumentManager, myViewmanager, myUndoManager, size);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    if (calledFromAppleScreenMenu(evt)) {
      return;
    }
    ChartSelection selection = myViewmanager.getSelectedArtefacts();
    if (!selection.isEmpty()) {
      pasteInternalFlavor(selection);
      return;
    }
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    if (clipboard.isDataFlavorAvailable(GPTransferable.EXTERNAL_DOCUMENT_FLAVOR)) {
      try {
        Object data = clipboard.getData(GPTransferable.EXTERNAL_DOCUMENT_FLAVOR);
        if (data instanceof InputStream == false) {
          return;
        }
        pasteExternalDocument((InputStream) data);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void pasteExternalDocument(InputStream data) {
    try {
      final BufferProject bufferProject = new BufferProject(myProject);

      UIParser parser = new UIParser(myDocumentManager, myUiFacade);
      parser.parse(bufferProject, data);

      HumanResourceMerger.MergeResourcesOption mergeOption = new HumanResourceMerger.MergeResourcesOption();
      mergeOption.setValue(HumanResourceMerger.MergeResourcesOption.NO);
      ImporterFromGanttFile.importBufferProject(myProject, bufferProject, myUiFacade, mergeOption, null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void pasteInternalFlavor(final ChartSelection selection) {
    myUndoManager.undoableEdit(getLocalizedName(), new Runnable() {
      @Override
      public void run() {
        myViewmanager.getActiveChart().paste(selection);
        selection.commitClipboardTransaction();
      }
    });
  }

  @Override
  public PasteAction asToolbarAction() {
    final PasteAction result = new PasteAction(myProject, myUiFacade, myDocumentManager, myViewmanager, myUndoManager);
    result.setFontAwesomeLabel(UIUtil.getFontawesomeLabel(result));
    this.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("enabled".equals(evt.getPropertyName())) {
          result.setEnabled((Boolean) evt.getNewValue());
        }
      }
    });
    result.setEnabled(this.isEnabled());
    return result;
  }
}
