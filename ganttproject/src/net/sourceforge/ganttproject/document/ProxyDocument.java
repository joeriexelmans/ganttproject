/*
GanttProject is an opensource project management tool. License: GPL3
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
package net.sourceforge.ganttproject.document;

import biz.ganttproject.core.calendar.GPCalendarCalc;
import biz.ganttproject.core.option.ListOption;
import biz.ganttproject.core.table.ColumnList;
import com.google.common.base.Preconditions;
import net.sourceforge.ganttproject.io.GanttXMLOpen;
import net.sourceforge.ganttproject.project.Project;
import net.sourceforge.ganttproject.PrjInfos;
import net.sourceforge.ganttproject.gui.GPColorChooser;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.io.GPSaver;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.parser.*;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.TaskManagerImpl;
import org.eclipse.core.runtime.IStatus;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * @author bard
 */
public class ProxyDocument implements Document {
  private Document myPhysicalDocument;

  private UIFacade myUIFacade;

  private final ParserFactory myParserFactory;

  private final DocumentCreator myCreator;

  ProxyDocument(DocumentCreator creator, Document physicalDocument, UIFacade uiFacade, ParserFactory parserFactory) {
    myPhysicalDocument = physicalDocument;
    myUIFacade = uiFacade;
    myParserFactory = parserFactory;
    myCreator = creator;
  }

  public Document getRealDocument() {
    return myPhysicalDocument;
  }
  @Override
  public void setMirror(Document mirrorDocument) {
    myPhysicalDocument = Preconditions.checkNotNull(mirrorDocument);
  }

  @Override
  public String getFileName() {
    return myPhysicalDocument.getFileName();
  }

  @Override
  public boolean canRead() {
    return myPhysicalDocument.canRead();
  }

  @Override
  public IStatus canWrite() {
    return myPhysicalDocument.canWrite();
  }

  @Override
  public boolean isValidForMRU() {
    return myPhysicalDocument.isValidForMRU();
  }

  @Override
  public boolean acquireLock() {
    return myPhysicalDocument.acquireLock();
  }

  @Override
  public void releaseLock() {
    myPhysicalDocument.releaseLock();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return myPhysicalDocument.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return myPhysicalDocument.getOutputStream();
  }

  @Override
  public String getPath() {
    return myPhysicalDocument.getPath();
  }

  @Override
  public String getFilePath() {
    String result = myPhysicalDocument.getFilePath();
    if (result == null) {
      try {
        result = myCreator.createTemporaryFile();
      } catch (IOException e) {
        myUIFacade.showErrorDialog(e);
      }
    }
    return result;
  }

  @Override
  public String getUsername() {
    return myPhysicalDocument.getUsername();
  }

  @Override
  public String getPassword() {
    return myPhysicalDocument.getPassword();
  }

  @Override
  public String getLastError() {
    return myPhysicalDocument.getLastError();
  }

  @Override
  public void read(Project project) throws IOException {
    try {
      ((TaskManagerImpl) project.getTaskManager()).setEventsEnabled(false);
      UIParser parser = new UIParser(myCreator, myUIFacade);
      parser.parse(project, getInputStream());
    } finally {
      ((TaskManagerImpl) project.getTaskManager()).setEventsEnabled(true);
    }
  }

  @Override
  public void write() throws IOException {
    GPSaver saver = myParserFactory.newSaver();
    byte[] buffer;
    try {
      ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
      saver.save(bufferStream);
      bufferStream.flush();
      buffer = bufferStream.toByteArray();
    } catch (IOException e) {
      myUIFacade.showErrorDialog(e);
      return;
    }
    OutputStream output = getOutputStream();
    try {
      output.write(buffer);
      output.flush();
    } finally {
      output.close();
    }
  }

  @Override
  public URI getURI() {
    return myPhysicalDocument.getURI();
  }

  @Override
  public boolean isLocal() {
    return myPhysicalDocument.isLocal();
  }

  @Override
  public boolean equals(Object doc) {
    if (false == doc instanceof ProxyDocument) {
      return false;
    }
    return getPath().equals(((Document) doc).getPath());
  }
}
