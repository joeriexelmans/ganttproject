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
import com.google.common.collect.ImmutableSet;
import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.IProject;
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
import org.xml.sax.Attributes;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Set;

/**
 * @author bard
 */
public class ProxyDocument {
  private Document myPhysicalDocument;

  private IProject myProject;

  private UIFacade myUIFacade;

  private final ParserFactory myParserFactory;

  private final DocumentCreator myCreator;

  private PortfolioImpl myPortfolio;

  private final ColumnList myTaskVisibleFields;

  private final ColumnList myResourceVisibleFields;

  ProxyDocument(DocumentCreator creator, Document physicalDocument, IProject project, UIFacade uiFacade, ColumnList taskVisibleFields, ColumnList resourceVisibleFields, ParserFactory parserFactory) {
    myPhysicalDocument = physicalDocument;
    myProject = project;
    myUIFacade = uiFacade;
    myParserFactory = parserFactory;
    myCreator = creator;
    myTaskVisibleFields = taskVisibleFields;
    myResourceVisibleFields = resourceVisibleFields;
  }

  public Document getRealDocument() {
    return myPhysicalDocument;
  }

  public void setMirror(Document mirrorDocument) {
    myPhysicalDocument = Preconditions.checkNotNull(mirrorDocument);
  }

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

  public void read() throws IOException, Document.DocumentException {
    ParsingState parsing = new ParsingState();
    // OpenCopyConfirmationState confirmation = new OpenCopyConfirmationState(
    // parsing, failure);
    // AcquireLockState lock = new AcquireLockState(parsing, confirmation);
    try {
      ((TaskManagerImpl) myProject.getTaskManager()).setEventsEnabled(false);
      parsing.enter();
    } finally {
      ((TaskManagerImpl) myProject.getTaskManager()).setEventsEnabled(true);
    }
    // lock.enter();
  }

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
    OutputStream output = myPhysicalDocument.getOutputStream();
    try {
      output.write(buffer);
      output.flush();
    } finally {
      output.close();
    }
  }

  // class AcquireLockState {
  // OpenCopyConfirmationState myConfirmationState;
  //
  // ParsingState myParsingState;
  //
  // public AcquireLockState(ParsingState parsing,
  // OpenCopyConfirmationState confirmation) {
  // myParsingState = parsing;
  // myConfirmationState = confirmation;
  // }
  //
  // void enter() throws IOException, DocumentException {
  // boolean locked = acquireLock();
  // if (!locked) {
  // myConfirmationState.enter();
  // } else {
  // myParsingState.enter();
  // }
  // }
  // }
  //
  // class OpenCopyConfirmationState {
  // private final ParsingState myParsingState;
  //
  // private final FailureState myExitState;
  //
  // public OpenCopyConfirmationState(ParsingState parsing,
  // FailureState failure) {
  // myParsingState = parsing;
  // myExitState = failure;
  // }
  //
  // void enter() throws IOException, DocumentException {
  // String message = GanttLanguage.getInstance().getText("msg13");
  // String title = GanttLanguage.getInstance().getText("warning");
  // if (UIFacade.Choice.YES==getUIFacade().showConfirmationDialog(message,
  // title)) {
  // myParsingState.enter();
  // } else {
  // myExitState.enter();
  // }
  // }
  // }

  class ParsingState {

    public ParsingState() {
    }

    void enter() throws IOException, Document.DocumentException {
      GPParser opener = myParserFactory.newParser();
      ParsingContext ctx = new ParsingContext();

      HumanResourceManager hrManager = myProject.getHumanResourceManager();
      RoleManager roleManager = myProject.getRoleManager();
      TaskManager taskManager = myProject.getTaskManager();
      GPCalendarCalc calendar = myProject.getActiveCalendar();
      PrjInfos prjInfos = myProject.getPrjInfos();

      ResourceTagHandler resourceHandler = new ResourceTagHandler(hrManager, roleManager,
          myProject.getResourceCustomPropertyManager());
      DependencyTagHandler dependencyHandler = new DependencyTagHandler(ctx, taskManager);
      AllocationTagHandler allocationHandler = new AllocationTagHandler(hrManager, taskManager, roleManager);
      VacationTagHandler vacationHandler = new VacationTagHandler(hrManager);
      PreviousStateTasksTagHandler previousStateHandler = new PreviousStateTasksTagHandler(myProject.getBaselines());
      RoleTagHandler rolesHandler = new RoleTagHandler(roleManager);
      TaskTagHandler taskHandler = new TaskTagHandler(taskManager, ctx);
      TaskParsingListener taskParsingListener = new TaskParsingListener(taskManager, myUIFacade.getTaskTree());
      DefaultWeekTagHandler weekHandler = new DefaultWeekTagHandler(calendar);
      OnlyShowWeekendsTagHandler onlyShowWeekendsHandler = new OnlyShowWeekendsTagHandler(calendar);

      TaskPropertiesTagHandler taskPropHandler = new TaskPropertiesTagHandler(taskManager.getCustomPropertyManager());
      opener.addTagHandler(taskPropHandler);
      CustomPropertiesTagHandler customPropHandler = new CustomPropertiesTagHandler(ctx, taskManager);
      opener.addTagHandler(customPropHandler);


      TaskDisplayColumnsTagHandler pilsenTaskDisplayHandler = TaskDisplayColumnsTagHandler.createPilsenHandler();
      TaskDisplayColumnsTagHandler legacyTaskDisplayHandler = TaskDisplayColumnsTagHandler.createLegacyHandler();

      opener.addTagHandler(pilsenTaskDisplayHandler);
      opener.addTagHandler(legacyTaskDisplayHandler);
      opener.addParsingListener(TaskDisplayColumnsTagHandler.createTaskDisplayColumnsWrapper(myTaskVisibleFields, pilsenTaskDisplayHandler, legacyTaskDisplayHandler));
      opener.addTagHandler(new ViewTagHandler("gantt-chart", myUIFacade, pilsenTaskDisplayHandler));


      TaskDisplayColumnsTagHandler resourceFieldsHandler = new TaskDisplayColumnsTagHandler(
          "field", "id", "order", "width", "visible");
      opener.addTagHandler(resourceFieldsHandler);
      opener.addParsingListener(TaskDisplayColumnsTagHandler.createTaskDisplayColumnsWrapper(myResourceVisibleFields, resourceFieldsHandler));
      opener.addTagHandler(new ViewTagHandler("resource-table", myUIFacade, resourceFieldsHandler));

      opener.addTagHandler(taskHandler);
      opener.addParsingListener(taskParsingListener);

      opener.addParsingListener(customPropHandler);

      opener.addTagHandler(new DescriptionTagHandler(prjInfos));
      opener.addTagHandler(new NotesTagHandler(ctx));
      opener.addTagHandler(new ProjectTagHandler(prjInfos));
      opener.addTagHandler(new ProjectViewAttrsTagHandler(myUIFacade));
      opener.addTagHandler(new TasksTagHandler(taskManager));

      TimelineTagHandler timelineTagHandler = new TimelineTagHandler(myUIFacade, taskManager);
      opener.addTagHandler(timelineTagHandler);
      opener.addParsingListener(timelineTagHandler);

      opener.addTagHandler(resourceHandler);
      opener.addTagHandler(dependencyHandler);
      opener.addTagHandler(allocationHandler);
      opener.addParsingListener(allocationHandler);
      opener.addTagHandler(vacationHandler);
      opener.addTagHandler(previousStateHandler);
      opener.addTagHandler(rolesHandler);
      opener.addTagHandler(weekHandler);
      opener.addTagHandler(onlyShowWeekendsHandler);
      opener.addTagHandler(new OptionTagHandler<ListOption<Color>>(GPColorChooser.getRecentColorsOption()));
      opener.addParsingListener(dependencyHandler);
      opener.addParsingListener(resourceHandler);


      HolidayTagHandler holidayHandler = new HolidayTagHandler(calendar);
      opener.addTagHandler(new CalendarsTagHandler(calendar));
      opener.addTagHandler(holidayHandler);

      PortfolioTagHandler portfolioHandler = new PortfolioTagHandler();
      opener.addTagHandler(portfolioHandler);
      InputStream is;
      try {
        is = myPhysicalDocument.getInputStream();
      } catch (IOException e) {
        throw new Document.DocumentException(GanttLanguage.getInstance().getText("msg8") + ": " + e.getLocalizedMessage(), e);
      }
      if (opener.load(is)) {
      } else {
      }
    }
  }

  @Override
  public boolean equals(Object other) {
    if (! (other instanceof ProxyDocument)) {
      return false;
    }
    return myPhysicalDocument.getPath().equals(((ProxyDocument)other).myPhysicalDocument.getPath());
  }

  private PortfolioImpl getPortfolioImpl() {
    if (myPortfolio == null) {
      myPortfolio = new PortfolioImpl();
    }
    return myPortfolio;
  }

  private class PortfolioImpl implements Portfolio {
    private Document myDefaultDocument;

    @Override
    public Document getDefaultDocument() {
      return myDefaultDocument;
    }

    void setDefaultDocument(Document document) {
      if (myDefaultDocument != null) {
        throw new IllegalStateException("Don't set default document twice");
      }
      myDefaultDocument = document;
    }
  }

  private class PortfolioTagHandler extends AbstractTagHandler {
    private static final String PORTFOLIO_TAG = "portfolio";
    private static final String PROJECT_TAG = "project";
    private final Set<String> TAGS = ImmutableSet.of(PORTFOLIO_TAG, PROJECT_TAG);
    private static final String LOCATION_ATTR = "location";
    private boolean isReadingPortfolio = false;

    public PortfolioTagHandler() {
      super(null, false);
    }
    @Override
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs)
        throws FileFormatException {
      if (!TAGS.contains(qName)) {
        return;
      }
      setTagStarted(true);
      if (PORTFOLIO_TAG.equals(qName)) {
        isReadingPortfolio = true;
        return;
      }
      if (PROJECT_TAG.equals(qName) && isReadingPortfolio) {
        String locationAsString = attrs.getValue(LOCATION_ATTR);
        if (locationAsString != null) {
          Document document = myCreator.getDocument(locationAsString);
          getPortfolioImpl().setDefaultDocument(document);
        }

      }
    }

    @Override
    public void endElement(String namespaceURI, String sName, String qName) {
      if (!TAGS.contains(qName)) {
        return;
      }
      if (PORTFOLIO_TAG.equals(qName)) {
        isReadingPortfolio = false;
      }
      setTagStarted(false);
    }
  }

}
