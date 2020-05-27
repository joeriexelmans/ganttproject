/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2010-2011 Dmitry Barashev, GanttProject Team

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
package net.sourceforge.ganttproject.chart.overview;

import biz.ganttproject.core.option.GPOption;
import biz.ganttproject.core.option.IntegerOption;
import com.google.common.base.Function;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.action.scroll.*;
import net.sourceforge.ganttproject.chart.TimelineChart;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.project.IProject;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

public class NavigationPanel {
  private final TimelineChart myChart;

  private final AbstractAction[] myScrollActions;
  private final AbstractAction myScrollBackAction;
  private final AbstractAction myScrollForwardAction;
  private final IntegerOption myDpiOption;
  private final GPOption<String> myLafOption;

  public NavigationPanel(IProject project, TimelineChart chart, UIFacade uiFacade) {
    myChart = chart;
    myScrollActions = new AbstractAction[] { new ScrollToStartAction(project, myChart),
        new ScrollToTodayAction(myChart), new ScrollToEndAction(project, myChart),
        new ScrollToSelectionAction(uiFacade, myChart) };
    myScrollBackAction = new ScrollTimeIntervalAction("scroll.back", -1, project.getTaskManager(), chart.getModel(),
        uiFacade.getScrollingManager());
    myScrollForwardAction = new ScrollTimeIntervalAction("scroll.forward", 1, project.getTaskManager(),
        chart.getModel(), uiFacade.getScrollingManager());
    myDpiOption = uiFacade.getDpiOption();
    myLafOption = uiFacade.getLafOption();
  }

  public Component getComponent() {
    return new ToolbarBuilder()
        .withDpiOption(myDpiOption)
        .withLafOption(myLafOption, new Function<String, Float>() {
          @Override
          public Float apply(@Nullable String s) {
            return (s.indexOf("nimbus") >= 0) ? 2f : 1f;
          }
        })
        .withHeight(24)
        .withGapFactory(ToolbarBuilder.Gaps.VDASH)
        .withBackground(myChart.getStyle().getSpanningHeaderBackgroundColor())
        .addComboBox(myScrollActions, myScrollActions[1])
        .button(myScrollBackAction).withAutoRepeat(200).add()
        .button(myScrollForwardAction).withAutoRepeat(200).add()
        .build()
        .getToolbar();
  }

}
