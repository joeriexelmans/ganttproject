/*
 * Created on 22.10.2005
 */
package net.sourceforge.ganttproject;

import net.sourceforge.ganttproject.chart.Chart;
import net.sourceforge.ganttproject.chart.ResourceChart;
import net.sourceforge.ganttproject.chart.TimelineChart;
import net.sourceforge.ganttproject.chart.overview.GPToolbar;
import net.sourceforge.ganttproject.chart.overview.ToolbarBuilder;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.view.GPView;
import net.sourceforge.ganttproject.project.IProject;

import javax.swing.*;
import java.awt.*;

class ResourceChartTabContentPanel extends ChartTabContentPanel implements GPView {
  private TreeTableContainer myTreeFacade;
  private Component myResourceChart;
  private JComponent myTabContentPanel;

  ResourceChartTabContentPanel(IProject project, UIFacade workbenchFacade, TreeTableContainer resourceTree,
                               ResourceLoadGraphicArea resourceChart) {
    super(project, workbenchFacade, resourceChart);
    myTreeFacade = resourceTree;
    myResourceChart = resourceChart;
    addTableResizeListeners(resourceTree.getTreeComponent(), myTreeFacade.getTreeTable().getScrollPane().getViewport());
  }

  JComponent getComponent() {
    if (myTabContentPanel == null) {
      myTabContentPanel = createContentComponent();
    }
    return myTabContentPanel;
  }

  @Override
  protected Component createButtonPanel() {
    ToolbarBuilder builder = new ToolbarBuilder()
        .withHeight(24)
        .withSquareButtons()
        .withDpiOption(getUiFacade().getDpiOption())
        .withLafOption(getUiFacade().getLafOption(), null);
    myTreeFacade.addToolbarActions(builder);
    final GPToolbar toolbar = builder.build();
    return toolbar.getToolbar();
  }

  @Override
  protected Component getChartComponent() {
    return myResourceChart;
  }

  @Override
  protected Component getTreeComponent() {
    return myTreeFacade.getTreeComponent();
  }

  @Override
  public Component getViewComponent() {
    return getComponent();
  }
}
