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
package net.sourceforge.ganttproject;

import com.google.common.base.Function;
import net.sourceforge.ganttproject.action.BaselineDialogAction;
import net.sourceforge.ganttproject.action.CalculateCriticalPathAction;
import net.sourceforge.ganttproject.chart.Chart;
import net.sourceforge.ganttproject.chart.GanttChart;
import net.sourceforge.ganttproject.chart.overview.GPToolbar;
import net.sourceforge.ganttproject.chart.overview.ToolbarBuilder;
import net.sourceforge.ganttproject.gui.UIConfiguration;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.view.GPView;
import net.sourceforge.ganttproject.project.Project;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

class GanttChartTabContentPanel extends ChartTabContentPanel implements GPView {
  private final Container myTaskTree;
  private final GanttChart myGanttChart;
  private final JComponent myChartComponent;
  private final TreeTableContainer myTreeFacade;
  private final CalculateCriticalPathAction myCriticalPathAction;
  private final BaselineDialogAction myBaselineAction;
  private JComponent myComponent;

  GanttChartTabContentPanel(GanttProject app, Project project, UIFacade uiFacade, TreeTableContainer treeFacade,
                            JComponent chartComponent, UIConfiguration uiConfiguration) {
    super(project, uiFacade, app.getGanttChart());
    myGanttChart = app.getGanttChart();
    myTreeFacade = treeFacade;
    myTaskTree = (Container) treeFacade.getTreeComponent();
    myChartComponent = chartComponent;
    // FIXME KeyStrokes of these 2 actions are not working...
    myCriticalPathAction = new CalculateCriticalPathAction(app, project.getTaskManager(), uiConfiguration, uiFacade);
    myBaselineAction = new BaselineDialogAction(app, project, uiFacade, myGanttChart);
    addChartPanel(createSchedulePanel());
    addTableResizeListeners(myTaskTree, myTreeFacade.getTreeTable().getScrollPane().getViewport());
  }

  private Component createSchedulePanel() {
    return new ToolbarBuilder()
        .withDpiOption(myUiFacade.getDpiOption())
        .withLafOption(getUiFacade().getLafOption(), new Function<String, Float>() {
          @Override
          public Float apply(@Nullable String s) {
            return (s.indexOf("nimbus") >= 0) ? 2f : 1f;
          }
        })
        .withGapFactory(ToolbarBuilder.Gaps.VDASH)
        .withBackground(myGanttChart.getStyle().getSpanningHeaderBackgroundColor())
        .withHeight(24)
        .addButton(myCriticalPathAction)
        .addButton(myBaselineAction)
        .build()
        .getToolbar();
  }

  JComponent getComponent() {
    if (myComponent == null) {
      myComponent = createContentComponent();
    }
    return myComponent;
  }

  @Override
  protected Component createButtonPanel() {
    ToolbarBuilder builder = new ToolbarBuilder()
        .withHeight(24)
        .withSquareButtons()
        .withDpiOption(myUiFacade.getDpiOption())
        .withLafOption(myUiFacade.getLafOption(), new Function<String, Float>() {
          @Override
          public Float apply(@Nullable String s) {
            return (s.indexOf("nimbus") >= 0) ? 2f : 1f;
          }
        });
    myTreeFacade.addToolbarActions(builder);
    final GPToolbar toolbar = builder.build();
    return toolbar.getToolbar();
  }

  @Override
  protected Component getChartComponent() {
    return myChartComponent;
  }

  @Override
  protected Component getTreeComponent() {
    return myTaskTree;
  }

  // //////////////////////////////////////////////
  // GPView
  @Override
  public void setActive(boolean active) {
    if (active) {
      myTaskTree.requestFocus();
      myTreeFacade.getNewAction().updateAction();
    }
  }

  @Override
  public Component getViewComponent() {
    return getComponent();
  }
}
