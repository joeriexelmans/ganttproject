/*
GanttProject is an opensource project management tool.
Copyright (C) 2011 Dmitry Barashev, GanttProject Team

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
package net.sourceforge.ganttproject.action;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import net.sourceforge.ganttproject.GanttPreviousState;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.action.CancelAction;
import net.sourceforge.ganttproject.action.GPAction;
import net.sourceforge.ganttproject.action.OkAction;
import net.sourceforge.ganttproject.chart.GanttChart;
import net.sourceforge.ganttproject.gui.AbstractTableAndActionsComponent;
import net.sourceforge.ganttproject.gui.EditableList;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.options.OptionsPageBuilder;
import net.sourceforge.ganttproject.project.IProject;

public class BaselineDialogAction extends GPAction {
  private final IGanttProject myApp;
  private final IProject myProject;
  private final UIFacade myUiFacade;
  private final GanttChart myGanttChart;
  private List<GanttPreviousState> myBaselines;
  private List<GanttPreviousState> myTrash = new ArrayList<GanttPreviousState>();

  public BaselineDialogAction(IGanttProject app, IProject project, UIFacade uiFacade, GanttChart ganttChart) {
    super("baseline.dialog");
    myApp = app;
    myProject = project;
    myUiFacade = uiFacade;
    myGanttChart = ganttChart;
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    myBaselines = new ArrayList<GanttPreviousState>(myProject.getBaselines());

    final EditableList<GanttPreviousState> list = new EditableList<GanttPreviousState>(myBaselines,
        Collections.<GanttPreviousState> emptyList()) {

      @Override
      protected GanttPreviousState updateValue(GanttPreviousState newValue, GanttPreviousState curValue) {
        curValue.setName(newValue.getName());
        return curValue;
      }

      @Override
      protected GanttPreviousState createValue(GanttPreviousState prototype) {
        try {
          prototype.init();
          prototype.saveFile();
          return prototype;
        } catch (IOException e) {
          myUiFacade.showErrorDialog(e);
          return null;
        }
      }

      @Override
      protected GanttPreviousState createPrototype(Object editValue) {
        if (editValue == null) {
          return null;
        }
        GanttPreviousState newBaseline = new GanttPreviousState(String.valueOf(editValue),
            GanttPreviousState.createTasks(myProject.getTaskManager()));
        return newBaseline;
      }

      @Override
      protected void deleteValue(GanttPreviousState value) {
        for (GanttPreviousState baseline : myBaselines) {
          if (baseline.getName().equals(value.getName())) {
            myTrash.add(baseline);
            break;
          }
        }
      }

      @Override
      protected String getStringValue(GanttPreviousState baseline) {
        return baseline.getName();
      }
    };
    list.setUndefinedValueLabel(getI18n("baseline.dialog.undefinedValueLabel"));
    list.getTableAndActions().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    if (myGanttChart.getBaseline() != null) {
      int index = myBaselines.indexOf(myGanttChart.getBaseline());
      list.getTableAndActions().setSelection(index);
    }
    list.getTableAndActions().addSelectionListener(
        new AbstractTableAndActionsComponent.SelectionListener<GanttPreviousState>() {

          @Override
          public void selectionChanged(List<GanttPreviousState> selection) {
            if (selection.isEmpty()) {
              myGanttChart.setBaseline(null);
            } else {
              myGanttChart.setBaseline(selection.get(0));
            }
            myGanttChart.reset();
          }
        });
    list.getTableAndActions().addAction(new GPAction("baseline.dialog.hide") {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        list.getTableAndActions().setSelection(-1);
      }
    });

    Action[] actions = new Action[] { new OkAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        list.stopEditing();
        myProject.getBaselines().clear();
        myProject.getBaselines().addAll(myBaselines);
        for (GanttPreviousState trashBaseline : myTrash) {
          trashBaseline.remove();
        }
        myApp.setModified();
      }
    }, CancelAction.EMPTY };

    OptionsPageBuilder optionsBuilder = new OptionsPageBuilder();
    optionsBuilder.setUiFacade(myUiFacade);
    JPanel contentPanel = new JPanel(new BorderLayout());
    contentPanel.add(list.createDefaultComponent(), BorderLayout.CENTER);
    contentPanel.add(optionsBuilder.createGroupComponent(myGanttChart.getBaselineColorOptions()), BorderLayout.SOUTH);
    myUiFacade.createDialog(contentPanel, actions, getI18n("baseline.dialog.title")).show();
  }
}
