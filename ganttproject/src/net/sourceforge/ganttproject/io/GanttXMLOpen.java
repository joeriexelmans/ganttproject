/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2002-2011 Thomas Alexandre, GanttProject Team

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
package net.sourceforge.ganttproject.io;

import biz.ganttproject.core.time.GanttCalendar;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.sourceforge.ganttproject.GPLogger;
import net.sourceforge.ganttproject.PrjInfos;
import net.sourceforge.ganttproject.gui.UIConfiguration;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.parser.AbstractTagHandler;
import net.sourceforge.ganttproject.parser.GPParser;
import net.sourceforge.ganttproject.parser.ParsingContext;
import net.sourceforge.ganttproject.parser.ParsingListener;
import net.sourceforge.ganttproject.parser.TagHandler;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;
import org.xml.sax.Attributes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Allows to load a gantt file from xml format, using SAX parser
 */
public class GanttXMLOpen implements GPParser {
  /** 0-->description of project, 1->note for task */
  int typeChar = -1;

  private final ArrayList<TagHandler> myTagHandlers = new ArrayList<TagHandler>();

  private final ArrayList<ParsingListener> myListeners = new ArrayList<ParsingListener>();

  private final ParsingContext myContext;

  private final TaskManager myTaskManager;

  private UIFacade myUIFacade = null;

  private TagHandler myTimelineTagHandler = new TimelineTagHandler();

  public GanttXMLOpen(TaskManager taskManager, UIFacade uiFacade) {
    this(taskManager);
    myUIFacade = uiFacade;
  }

  public GanttXMLOpen(TaskManager taskManager) {
    myContext = new ParsingContext();
    myTaskManager = taskManager;
  }

  @Override
  public boolean load(InputStream inStream) throws IOException {
    // Use an instance of ourselves as the SAX event handler
    XmlParser parser = new XmlParser(myTagHandlers, myListeners);
    parser.parse(inStream);
    return true;
  }

  public boolean load(File file) {
    XmlParser parser = new XmlParser(myTagHandlers, myListeners);
    try {
      parser.parse(new BufferedInputStream(new FileInputStream(file)));
    } catch (Exception e) {
      myUIFacade.showErrorDialog(e);
      return false;
    }
    return true;
  }

  @Override
  public void addTagHandler(TagHandler handler) {
    myTagHandlers.add(handler);
  }

  @Override
  public void addParsingListener(ParsingListener listener) {
    myListeners.add(listener);
  }

  @Override
  public ParsingContext getContext() {
    return myContext;
  }

  @Override
  public TagHandler getTimelineTagHandler() {
    return myTimelineTagHandler;
  }

  class TimelineTagHandler extends AbstractTagHandler implements ParsingListener {
    private final List<Integer> myIds = Lists.newArrayList();

    public TimelineTagHandler() {
      super("timeline", true);
    }

    @Override
    public void parsingStarted() {
    }

    @Override
    public void parsingFinished() {
      myUIFacade.getCurrentTaskView().getTimelineTasks().clear();
      for (Integer id : myIds) {
        Task t = myTaskManager.getTask(id);
        if (t != null) {
          myUIFacade.getCurrentTaskView().getTimelineTasks().add(t);
        }
      }
    }

    @Override
    protected boolean onStartElement(Attributes attrs) {
      clearCdata();
      return super.onStartElement(attrs);
    }

    @Override
    protected void onEndElement() {
      String[] ids = getCdata().split(",");
      for (String id : ids) {
        try {
          myIds.add(Integer.valueOf(id.trim()));
        } catch (NumberFormatException e) {
          GPLogger.logToLogger(e);
        }
      }
      clearCdata();
    }
  }
}
