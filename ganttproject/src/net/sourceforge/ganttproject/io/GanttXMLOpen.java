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

import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.parser.*;
import net.sourceforge.ganttproject.task.TaskManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Allows to load a gantt file from xml format, using SAX parser
 */
public class GanttXMLOpen implements GPParser {

  private final ArrayList<TagHandler> myTagHandlers = new ArrayList<TagHandler>();

  private final ArrayList<ParsingListener> myListeners = new ArrayList<ParsingListener>();

  public GanttXMLOpen() {}

  @Override
  public void load(InputStream inStream) throws IOException {
    // Use an instance of ourselves as the SAX event handler
    XmlParser parser = new XmlParser(myTagHandlers, myListeners);
    parser.parse(inStream);
  }

  @Override
  public void addTagHandler(TagHandler handler) {
    myTagHandlers.add(handler);
  }

  @Override
  public void addParsingListener(ParsingListener listener) {
    myListeners.add(listener);
  }
}
