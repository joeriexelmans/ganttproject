/*
Copyright 2002-2019 Alexandre Thomas, BarD Software s.r.o

This file is part of GanttProject, an open-source project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.sourceforge.ganttproject;

import biz.ganttproject.app.FXSearchUi;
import biz.ganttproject.app.FXToolbar;
import biz.ganttproject.app.FXToolbarBuilder;
import biz.ganttproject.core.calendar.GPCalendarCalc;
import biz.ganttproject.core.calendar.GPCalendarListener;
import biz.ganttproject.core.calendar.WeekendCalendarImpl;
import biz.ganttproject.core.option.*;
import biz.ganttproject.core.table.ColumnList;
import biz.ganttproject.core.time.TimeUnitStack;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
import biz.ganttproject.platform.UpdateOptions;
import biz.ganttproject.storage.cloud.GPCloudOptions;
import biz.ganttproject.storage.cloud.GPCloudStatusBar;
import com.bardsoftware.eclipsito.update.Updater;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.collect.Lists;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import net.sourceforge.ganttproject.action.ActiveActionProvider;
import net.sourceforge.ganttproject.action.ArtefactAction;
import net.sourceforge.ganttproject.action.ArtefactDeleteAction;
import net.sourceforge.ganttproject.action.ArtefactNewAction;
import net.sourceforge.ganttproject.action.ArtefactPropertiesAction;
import net.sourceforge.ganttproject.action.GPAction;
import net.sourceforge.ganttproject.action.edit.EditMenu;
import net.sourceforge.ganttproject.action.help.HelpMenu;
import net.sourceforge.ganttproject.action.project.ProjectMenu;
import net.sourceforge.ganttproject.action.resource.ResourceActionSet;
import net.sourceforge.ganttproject.action.view.ViewCycleAction;
import net.sourceforge.ganttproject.action.view.ViewMenu;
import net.sourceforge.ganttproject.action.zoom.ZoomActionSet;
import net.sourceforge.ganttproject.chart.Chart;
import net.sourceforge.ganttproject.chart.ChartModelBase;
import net.sourceforge.ganttproject.chart.GanttChart;
import net.sourceforge.ganttproject.chart.TimelineChart;
import net.sourceforge.ganttproject.client.RssFeedChecker;
import net.sourceforge.ganttproject.document.Document;
import net.sourceforge.ganttproject.document.Document.DocumentException;
import net.sourceforge.ganttproject.document.DocumentCreator;
import net.sourceforge.ganttproject.document.DocumentManager;
import net.sourceforge.ganttproject.document.DocumentsMRU;
import net.sourceforge.ganttproject.export.CommandLineExportApplication;
import net.sourceforge.ganttproject.gui.*;
import net.sourceforge.ganttproject.gui.scrolling.ScrollingManager;
import net.sourceforge.ganttproject.gui.view.GPViewManager;
import net.sourceforge.ganttproject.gui.view.ViewManagerImpl;
import net.sourceforge.ganttproject.gui.window.ContentPaneBuilder;
import net.sourceforge.ganttproject.importer.Importer;
import net.sourceforge.ganttproject.io.GPSaver;
import net.sourceforge.ganttproject.io.GanttXMLSaver;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.language.GanttLanguage.Event;
import net.sourceforge.ganttproject.parser.ParserFactory;
import net.sourceforge.ganttproject.plugins.PluginManager;
import net.sourceforge.ganttproject.print.PrintManager;
import net.sourceforge.ganttproject.project.IProject;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.resource.ResourceEvent;
import net.sourceforge.ganttproject.resource.ResourceView;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.task.*;
import net.sourceforge.ganttproject.undo.GPUndoManager;
import net.sourceforge.ganttproject.undo.UndoManagerImpl;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Main frame of the project
 */
public class GanttProject extends JFrame implements IGanttProject, IProject, ResourceView, GanttLanguage.Listener {
  private final static GanttLanguage language = GanttLanguage.getInstance();

  private final ViewManagerImpl myViewManager;
  private final List<ProjectEventListener> myModifiedStateChangeListeners = new ArrayList<ProjectEventListener>();
  private final UIFacadeImpl myUIFacade;
  private final ProjectUIFacadeImpl myProjectUIFacade;
  private final GPUndoManager myUndoManager;
  private final RssFeedChecker myRssChecker;
  private final ContentPaneBuilder myContentPaneBuilder;
  private Updater myUpdater;

  // Current project:
  private final WeekendCalendarImpl myCalendar = new WeekendCalendarImpl();
  private RoleManager myRoleManager = RoleManager.Access.getInstance();
  private final CustomColumnsManager myResourceCustomPropertyManager = new CustomColumnsManager();
  private HumanResourceManager myHumanResourceManager;
  private final TaskManager myTaskManager;
  public PrjInfos prjInfos = new PrjInfos();
  private ArrayList<GanttPreviousState> myPreviousStates = new ArrayList<GanttPreviousState>();

  // Graphical layout and controls
  private final GanttStatusBar statusBar;
  private final GanttTabbedPane myTabPane;
  private TaskTreePanel tree;
  private ResourceTreePanel resp;
  private final EditMenu myEditMenu;
  private final ProjectMenu myProjectMenu;
  private final ResourceActionSet myResourceActions;
  private final ZoomActionSet myZoomActions;
  private final FacadeInvalidator myFacadeInvalidator;
  private MouseListener myStopEditingMouseListener = null;

  // Main app state
  public boolean askForSave = false;
  private SimpleObjectProperty<Document> myObservableDocument = new SimpleObjectProperty<>();
  private final boolean isOnlyViewer;
  private UIConfiguration myUIConfiguration;
  private final GanttOptions options;

  // Logic
  private final TimeUnitStack myTimeUnitStack;
  private final DocumentManager myDocumentManager;


  private TaskContainmentHierarchyFacadeImpl myCachedFacade;
  private GanttChartTabContentPanel myGanttChartTabContent;
  private ResourceChartTabContentPanel myResourceChartTabContent;
  private List<RowHeightAligner> myRowHeightAligners = Lists.newArrayList();
  private ParserFactory myParserFactory = new ParserFactoryImpl();
  private static Runnable ourQuitCallback;
  private FXSearchUi mySearchUi;
  private final DocumentsMRU myMRU;

  public GanttProject(boolean isOnlyViewer) {
    //// Begin GanttProjectBase constructor ///////////////////////////////
    super("GanttProject");

    statusBar = new GanttStatusBar(this);
    myTabPane = new GanttTabbedPane();
    myContentPaneBuilder = new ContentPaneBuilder(myTabPane, statusBar);

    myTimeUnitStack = new GPTimeUnitStack();
    NotificationManagerImpl notificationManager = new NotificationManagerImpl(myContentPaneBuilder.getAnimationHost());
    myUIFacade = new UIFacadeImpl(this, statusBar, notificationManager, this);
    GPLogger.setUIFacade(myUIFacade);
    myDocumentManager = new DocumentCreator(this, prjInfos, myUIFacade, null) {
      @Override
      protected ParserFactory getParserFactory() {
        return myParserFactory;
      }
      @Override
      protected ColumnList getTaskVisibleFields() {
        return tree.getVisibleFields();
      }
      @Override
      protected ColumnList getResourceVisibleFields() {
        return resp.getVisibleFields();
      }
    };
    myUndoManager = new UndoManagerImpl(this, null, myDocumentManager) {
      @Override
      protected ParserFactory getParserFactory() {
        return myParserFactory;
      }
    };
    myViewManager = new ViewManagerImpl(this, myUIFacade, myTabPane, myUndoManager);
    myMRU = new DocumentsMRU(5);
    myProjectUIFacade = new ProjectUIFacadeImpl(myUIFacade, myDocumentManager, myMRU, myUndoManager);
    myRssChecker = new RssFeedChecker((GPTimeUnitStack) getTimeUnitStack(), myUIFacade);
    myUIFacade.addOptions(myRssChecker.getUiOptions());
    //// End GanttProjectBase constructor ///////////////////////////////

    System.err.println("Creating main frame...");
    ToolTipManager.sharedInstance().setInitialDelay(200);
    ToolTipManager.sharedInstance().setDismissDelay(60000);

    myCalendar.addListener(new GPCalendarListener() {
      @Override
      public void onCalendarChange() {
        GanttProject.this.setModified();
      }
    });
    prjInfos.addListener(new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        setAskForSave(true);
      }
    });
    Mediator.registerTaskSelectionManager(myUIFacade.getTaskSelectionManager());

    this.isOnlyViewer = isOnlyViewer;
    if (!isOnlyViewer) {
      setTitle(language.getText("appliTitle"));
    } else {
      setTitle("GanttViewer");
    }
    setFocusable(true);
    System.err.println("1. loading look'n'feels");
    options = new GanttOptions(myRoleManager, myMRU);
    myUIConfiguration = options.getUIConfiguration();
    myUIConfiguration.setChartFontOption(myUIFacade.getChartFontOption());
    myUIConfiguration.setDpiOption(myUIFacade.getDpiOption());

    myHumanResourceManager = new HumanResourceManager(myRoleManager.getDefaultRole(),
            getResourceCustomPropertyManager());
    myHumanResourceManager.addView(this);

    class TaskManagerConfigImpl implements TaskManagerConfig {
      final DefaultColorOption myDefaultColorOption = new GanttProjectImpl.DefaultTaskColorOption();
      @Override
      public Color getDefaultColor() {
        return tree.area.getTaskDefaultColorOption().getValue();
      }
      @Override
      public ColorOption getDefaultColorOption() {
        return myDefaultColorOption;
      }
      @Override
      public URL getProjectDocumentURL() {
        try {
          return myObservableDocument.get().getURI().toURL();
        } catch (MalformedURLException e) {
          e.printStackTrace();
          return null;
        }
      }
      @Override
      public NotificationManager getNotificationManager() {
        return myUIFacade.getNotificationManager();
      }
    }
    TaskManagerConfig taskConfig = new TaskManagerConfigImpl();
    myTaskManager = TaskManager.Access.newInstance(new TaskContainmentHierarchyFacade.Factory() {
      @Override
      public TaskContainmentHierarchyFacade createFacade() {
        if (myFacadeInvalidator == null) {
          return TaskContainmentHierarchyFacade.STUB;
        }
        if (!myFacadeInvalidator.isValid() || myCachedFacade == null) {
          myCachedFacade = new TaskContainmentHierarchyFacadeImpl(tree);
          myFacadeInvalidator.reset();
        }
        return myCachedFacade;
      }
    }, myHumanResourceManager, myCalendar, myTimeUnitStack, taskConfig);
    addProjectEventListener(myTaskManager.getProjectListener());
    myCalendar.addListener(myTaskManager.getCalendarListener());
    ImageIcon icon = new ImageIcon(getClass().getResource("/icons/ganttproject-logo-512.png"));
    setIconImage(icon.getImage());

    resp = new ResourceTreePanel(this, myUIFacade);
    resp.init();
    myRowHeightAligners.add(resp.getRowHeightAligner());
    myHumanResourceManager.addView(resp);

    tree = new TaskTreePanel(this, myTaskManager, myUIFacade.getTaskSelectionManager(), myUIFacade);
    myFacadeInvalidator = new FacadeInvalidator(tree.getModel(), myRowHeightAligners);
    this.addProjectEventListener(myFacadeInvalidator);
    tree.init();
    options.addOptionGroups(myUIFacade.getOptions());
    options.addOptionGroups(tree.area.getOptionGroups());
    options.addOptionGroups(resp.area.getOptionGroups());
    options.addOptionGroups(myProjectUIFacade.getOptionGroups());
    options.addOptionGroups(myDocumentManager.getNetworkOptionGroups());
    options.addOptions(GPCloudOptions.INSTANCE.getOptionGroup());
    options.addOptions(myRssChecker.getOptions());
    options.addOptions(UpdateOptions.INSTANCE.getOptionGroup());

    System.err.println("2. loading options");
    initOptions();
    // Not a joke. This takes value from the option and applies it to the UI.
    myUIFacade.setLookAndFeel(myUIFacade.getLookAndFeel());
    myRowHeightAligners.add(tree.getRowHeightAligner());
    myUIFacade.getAppFontOption().addChangeValueListener(new ChangeValueListener() {
      @Override
      public void changeValue(ChangeValueEvent event) {
        for (RowHeightAligner aligner : myRowHeightAligners) {
          aligner.optionsChanged();
        }
      }
    });

    myUIFacade.getZoomManager().addZoomListener(tree.area.getZoomListener());

    ScrollingManager scrollingManager = myUIFacade.getScrollingManager();
    scrollingManager.addScrollingListener(tree.area.getViewState());
    scrollingManager.addScrollingListener(resp.area.getViewState());

    System.err.println("3. creating menus...");
    myResourceActions = resp.getResourceActionSet();
    myZoomActions = new ZoomActionSet(myUIFacade.getZoomManager());
    JMenuBar bar = new JMenuBar();
    setJMenuBar(bar);
    // Allocation of the menus

    // Project menu related sub menus and items
    ProjectMRUMenu mruMenu = new ProjectMRUMenu(this, myUIFacade, myProjectUIFacade, "lastOpen");
    mruMenu.setIcon(new ImageIcon(getClass().getResource("/icons/recent_16.gif")));
    myMRU.addListener(mruMenu);

    myProjectMenu = new ProjectMenu(this, myMRU, "project");
    bar.add(myProjectMenu);

    myEditMenu = new EditMenu(this, myUIFacade, myViewManager, () -> mySearchUi.requestFocus(), "edit");
    bar.add(myEditMenu);

    ViewMenu viewMenu = new ViewMenu(this, myViewManager, myUIFacade.getDpiOption(), myUIFacade.getChartFontOption(), "view");
    bar.add(viewMenu);

    {
      JMenu mTask = UIUtil.createTooltiplessJMenu(GPAction.createVoidAction("task"));
      mTask.add(tree.getNewAction());
      mTask.add(tree.getPropertiesAction());
      mTask.add(tree.getDeleteAction());
      resp.setTaskPropertiesAction(tree.getPropertiesAction());
      bar.add(mTask);
    }
    JMenu mHuman = UIUtil.createTooltiplessJMenu(GPAction.createVoidAction("human"));
    for (AbstractAction a : myResourceActions.getActions()) {
      mHuman.add(a);
    }
    mHuman.add(myResourceActions.getResourceSendMailAction());
    bar.add(mHuman);

    HelpMenu helpMenu = new HelpMenu(this, myUIFacade, myProjectUIFacade);
    bar.add(helpMenu.createMenu());

    System.err.println("4. creating views...");
    myGanttChartTabContent = new GanttChartTabContentPanel(this, myUIFacade, tree, tree.area.getJComponent(),
        myUIConfiguration);
    myViewManager.createView(myGanttChartTabContent, new ImageIcon(getClass().getResource("/icons/tasks_16.gif")));
    myViewManager.toggleVisible(myGanttChartTabContent);

    myResourceChartTabContent = new ResourceChartTabContentPanel(this, myUIFacade, resp,
        resp.area);
    myViewManager.createView(myResourceChartTabContent, new ImageIcon(getClass().getResource("/icons/res_16.gif")));
    myViewManager.toggleVisible(myResourceChartTabContent);

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentShown(ComponentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            tree.area.reset();
            getResourceChart().reset();
            // This will clear any modifications which might be caused by
            // adjusting widths of table columns during initial layout process.
            setModified(false);
          }
        });
      }
    });
    System.err.println("5. calculating size and packing...");

    FXToolbar fxToolbar = createToolbar();
    Platform.runLater(() -> {
      GPCloudStatusBar cloudStatusBar = new GPCloudStatusBar(myObservableDocument, myUIFacade);
      Scene statusBarScene = new Scene(cloudStatusBar.getLockPanel(), javafx.scene.paint.Color.TRANSPARENT);
      statusBarScene.getStylesheets().add("biz/ganttproject/app/StatusBar.css");
      statusBar.setLeftScene(statusBarScene);
    });

    createContentPane(fxToolbar.getComponent());
    //final FXToolbar toolbar = fxToolbar;
    //final List<? extends JComponent> buttons = addButtons(getToolBar());
    // Chart tabs
    myTabPane.setSelectedIndex(0);

    System.err.println("6. changing language ...");
    languageChanged(null);
    // Add Listener after language update (to be sure that it is not updated
    // twice)
    language.addListener(this);

    System.err.println("7. first attempt to restore bounds");
    restoreBounds();
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent windowEvent) {
        quitApplication();
      }

      @Override
      public void windowOpened(WindowEvent e) {
        System.err.println("Resizing window...");
        GPLogger.log(String.format("Bounds after opening: %s", GanttProject.this.getBounds()));
        restoreBounds();
        // It is important to run aligners after look and feel is set and font sizes
        // in the UI manager updated.
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            for (RowHeightAligner aligner : myRowHeightAligners) {
              aligner.optionsChanged();
            }
          }
        });
        myUIFacade.getDpiOption().addChangeValueListener(new ChangeValueListener() {
          @Override
          public void changeValue(ChangeValueEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                getContentPane().doLayout();
              }
            });
          }
        });
        tree.area.reset();
        getResourceChart().reset();
        // This will clear any modifications which might be caused by
        // adjusting widths of table columns during initial layout process.
        setModified(false);
      }
    });

    System.err.println("8. finalizing...");
    // applyComponentOrientation(GanttLanguage.getInstance()
    // .getComponentOrientation());
    myTaskManager.addTaskListener(new TaskModelModificationListener(this, myUIFacade));
    addMouseListenerToAllContainer(this.getComponents());

    // Add globally available actions/key strokes
    GPAction viewCycleForwardAction = new ViewCycleAction(myViewManager, true);
    UIUtil.pushAction(myTabPane, true, viewCycleForwardAction.getKeyStroke(), viewCycleForwardAction);

    GPAction viewCycleBackwardAction = new ViewCycleAction(myViewManager, false);
    UIUtil.pushAction(myTabPane, true, viewCycleBackwardAction.getKeyStroke(), viewCycleBackwardAction);

    try {
      myObservableDocument.set(myDocumentManager.newUntitledDocument());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  private void restoreBounds() {
    if (options.isLoaded()) {
      if (options.isMaximized()) {
        setExtendedState(getExtendedState() | Frame.MAXIMIZED_BOTH);
      }
      Rectangle bounds = new Rectangle(options.getX(), options.getY(), options.getWidth(), options.getHeight());
      GPLogger.log(String.format("Bounds stored in the  options: %s", bounds));

      UIUtil.MultiscreenFitResult fit = UIUtil.multiscreenFit(bounds);
      // If more than 1/4 of the rectangle is visible on screen devices then leave it where it is
      if (fit.totalVisibleArea < 0.25 || Math.max(bounds.width, bounds.height) < 100) {
        // Otherwise if it is visible on at least one device, try to fit it there
        if (fit.argmaxVisibleArea != null) {
          bounds = fitBounds(fit.argmaxVisibleArea, bounds);
        } else {
          UIUtil.MultiscreenFitResult currentFit = UIUtil.multiscreenFit(this.getBounds());
          if (currentFit.argmaxVisibleArea != null) {
            // If there are no devices where rectangle is visible, fit it on the current device
            bounds = fitBounds(currentFit.argmaxVisibleArea, bounds);
          } else {
            GPLogger.log(String.format("We have not found the display corresponding to bounds %s. Leaving the window where it is", bounds));
            return;
          }
        }
      }
      setBounds(bounds);
    }
  }

  static private Rectangle fitBounds(GraphicsConfiguration display, Rectangle bounds) {
    Rectangle displayBounds = display.getBounds();
    Rectangle visibleBounds = bounds.intersection(displayBounds);
    int fitX = visibleBounds.x;
    if (fitX + bounds.width > displayBounds.x + displayBounds.width) {
      fitX = Math.max(displayBounds.x, displayBounds.x + displayBounds.width - bounds.width);
    }
    int fitY = visibleBounds.y;
    if (fitY + bounds.height > displayBounds.y + displayBounds.height) {
      fitY = Math.max(displayBounds.y, displayBounds.y + displayBounds.height - bounds.height);
    }
    return new Rectangle(fitX, fitY, bounds.width, bounds.height);

  }

  private void initOptions() {
    // Color color = GanttGraphicArea.taskDefaultColor;
    // myApplicationConfig.register(options);
    options.setUIConfiguration(myUIConfiguration);
    options.load();
    myUIConfiguration = options.getUIConfiguration();
  }

  private void addMouseListenerToAllContainer(Component[] containers) {
    for (Component container : containers) {
      container.addMouseListener(getStopEditingMouseListener());
      if (container instanceof Container) {
        addMouseListenerToAllContainer(((Container) container).getComponents());
      }
    }
  }

  /**
   * @return A mouseListener that stop the edition in the ganttTreeTable.
   */
  private MouseListener getStopEditingMouseListener() {
    if (myStopEditingMouseListener == null)
      myStopEditingMouseListener = new MouseAdapter() {
        // @Override
        // public void mouseClicked(MouseEvent e) {
        // if (e.getSource() != bNew && e.getClickCount() == 1) {
        // tree.stopEditing();
        // }
        // if (e.getButton() == MouseEvent.BUTTON1
        // && !(e.getSource() instanceof JTable)
        // && !(e.getSource() instanceof AbstractButton)) {
        // Task taskUnderPointer =
        // area.getChartImplementation().findTaskUnderPointer(e.getX(),
        // e.getY());
        // if (taskUnderPointer == null) {
        // getTaskSelectionManager().clear();
        // }
        // }
        // }
      };
    return myStopEditingMouseListener;
  }

  /**
   * @return the options of ganttproject.
   */
  public GanttOptions getGanttOptions() {
    return options;
  }

  /**
   * Function to change language of the project
   */
  @Override
  public void languageChanged(Event event) {
    applyComponentOrientation(language.getComponentOrientation());
    tree.area.repaint();
    resp.area.repaint();

    CustomColumnsStorage.changeLanguage(language);

    applyComponentOrientation(language.getComponentOrientation());
  }

  /**
   * @return the ToolTip in HTML (with gray bgcolor)
   */
  public static String getToolTip(String msg) {
    return "<html><body bgcolor=#EAEAEA>" + msg + "</body></html>";
  }

  /**
   * Create the button on toolbar
   */
  private FXToolbar createToolbar() {
    FXToolbarBuilder builder = new FXToolbarBuilder();
    builder.addButton(myProjectMenu.getOpenProjectAction().asToolbarAction())
        .addButton(myProjectMenu.getSaveProjectAction().asToolbarAction())
        .addWhitespace();

    final ArtefactAction newAction;
    {
      final GPAction taskNewAction = tree.getNewAction().asToolbarAction();
      final GPAction resourceNewAction = resp.getNewAction().asToolbarAction();
      newAction = new ArtefactNewAction(new ActiveActionProvider() {
        @Override
        public AbstractAction getActiveAction() {
          return myTabPane.getSelectedIndex() == UIFacade.GANTT_INDEX ? taskNewAction : resourceNewAction;
        }
      }, new Action[]{taskNewAction, resourceNewAction});
      builder.addButton(taskNewAction).addButton(resourceNewAction);
    }

    final ArtefactAction deleteAction;
    {
      final GPAction taskDeleteAction = tree.getDeleteAction().asToolbarAction();
      final GPAction resourceDeleteAction = resp.getDeleteAction().asToolbarAction();
      deleteAction = new ArtefactDeleteAction(new ActiveActionProvider() {
        @Override
        public AbstractAction getActiveAction() {
          return myTabPane.getSelectedIndex() == UIFacade.GANTT_INDEX ? taskDeleteAction : resourceDeleteAction;
        }
      }, new Action[]{taskDeleteAction, resourceDeleteAction});
    }

    final ArtefactAction propertiesAction;
    {
      final GPAction taskPropertiesAction = tree.getPropertiesAction().asToolbarAction();
      final GPAction resourcePropertiesAction = resp.getPropertiesAction().asToolbarAction();
      propertiesAction = new ArtefactPropertiesAction(new ActiveActionProvider() {
        @Override
        public AbstractAction getActiveAction() {
          return myTabPane.getSelectedIndex() == UIFacade.GANTT_INDEX ? taskPropertiesAction : resourcePropertiesAction;
        }
      }, new Action[]{taskPropertiesAction, resourcePropertiesAction});
    }

    UIUtil.registerActions(getRootPane(), false, newAction, propertiesAction, deleteAction);
    UIUtil.registerActions(myGanttChartTabContent.getComponent(), true, newAction, propertiesAction, deleteAction);
    UIUtil.registerActions(myResourceChartTabContent.getComponent(), true, newAction, propertiesAction, deleteAction);
    myTabPane.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        // Tell artefact actions that the active provider changed, so they
        // are able to update their state according to the current delegate
        newAction.actionStateChanged();
        propertiesAction.actionStateChanged();
        deleteAction.actionStateChanged();
        myTabPane.getSelectedComponent().requestFocus();
      }
    });

    builder.addButton(deleteAction)
        .addWhitespace()
        .addButton(propertiesAction)
        .addButton(getCutAction().asToolbarAction())
        .addButton(getCopyAction().asToolbarAction())
        .addButton(getPasteAction().asToolbarAction())
        .addWhitespace()
        .addButton(myEditMenu.getUndoAction().asToolbarAction())
        .addButton(myEditMenu.getRedoAction().asToolbarAction());
    mySearchUi = new FXSearchUi(this, myUIFacade);
    builder.addSearchBox(mySearchUi);

    //return result;
    return builder.build();
  }

  void doShow() {
    setVisible(true);
    GPLogger.log(String.format("Bounds after setVisible: %s", getBounds()));
    DesktopIntegration.setup(GanttProject.this);
    getActiveChart().reset();
    myRssChecker.setOptionsVersion(options.getVersion());
    myRssChecker.setUpdater(myUpdater);
    myRssChecker.run();
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
  }

  @Override
  public ArrayList<GanttPreviousState> getBaselines() {
    return myPreviousStates;
  }

  /**
   * Refresh the information of the project on the status bar.
   */
  public void refreshProjectInformation() {
    if (myTaskManager.getTaskCount() == 0 && resp.nbPeople() == 0) {
      statusBar.setSecondText("");
    } else {
      statusBar.setSecondText(
          language.getCorrectedLabel("task") + " : " + myTaskManager.getTaskCount() + "  "
              + language.getCorrectedLabel("resources") + " : " + resp.nbPeople());
    }
  }

  /**
   * Print the project
   */
  public void printProject() {
    Chart chart = getActiveChart();
    if (chart == null) {
      myUIFacade.showErrorDialog(
          "Failed to find active chart.\nPlease report this problem to GanttProject development team");
      return;
    }
    try {
      PrintManager.printChart(chart, options.getExportSettings());
    } catch (OutOfMemoryError e) {
      myUIFacade.showErrorDialog(GanttLanguage.getInstance().getText("printing.out_of_memory"));
    }
  }

  /**
   * Create a new project
   */
  public void newProject() {
    myProjectUIFacade.createProjectWizard(this);
    fireProjectCreated();
  }

  @Override
  public void open(Document document) throws IOException, DocumentException {
    document.read();
    myMRU.add(document.getPath(), true);
    myObservableDocument.set(document);
    setTitle(language.getText("appliTitle") + " [" + document.getFileName() + "]");
    for (Chart chart : PluginManager.getCharts()) {
      chart.reset();
    }

    // myDelayManager.fireDelayObservation(); // it is done in repaint2
    addMouseListenerToAllContainer(this.getComponents());

    fireProjectOpened();
  }

  public void openStartupDocument(String path) {
    if (path != null) {
      final Document document = myDocumentManager.getDocument(path);
      try {
        myProjectUIFacade.openProject(document, this);
      } catch (DocumentException e) {
        fireProjectCreated(); // this will create columns in the tables, which are removed by previous call to openProject()
        if (!tryImportDocument(document)) {
          myUIFacade.showErrorDialog(e);
        }
      } catch (IOException e) {
        fireProjectCreated(); // this will create columns in the tables, which are removed by previous call to openProject()
        if (!tryImportDocument(document)) {
          myUIFacade.showErrorDialog(e);
        }
      }
    }
  }

  private boolean tryImportDocument(Document document) {
    boolean success = false;
    List<Importer> importers = PluginManager.getExtensions(Importer.EXTENSION_POINT_ID, Importer.class);
    for (Importer importer : importers) {
      if (Pattern.matches(".*(" + importer.getFileNamePattern() + ")$", document.getFilePath())) {
        try {
          ((TaskManagerImpl) myTaskManager).setEventsEnabled(false);
          importer.setContext(this, myUIFacade, options.getPluginPreferences());
          importer.setFile(new File(document.getFilePath()));
          importer.run();
          success = true;
          break;
        } catch (Throwable e) {
          if (!GPLogger.log(e)) {
            e.printStackTrace(System.err);
          }
        } finally {
          ((TaskManagerImpl) myTaskManager).setEventsEnabled(true);
        }
      }
    }
    return success;
  }

  /**
   * Save the project as (with a dialog file chooser)
   */
  public boolean saveAsProject() {
    myProjectUIFacade.saveProjectAsDialog(this);
    return true;
  }

  /**
   * Save the project on a file
   */
  public void saveProject() {
    myProjectUIFacade.saveProject(this);
  }

  public void changeWorkingDirectory(String newWorkDir) {
    if (null != newWorkDir) {
      options.setWorkingDirectory(newWorkDir);
    }
  }

  /**
   * @return the UIConfiguration.
   */
  @Override
  public UIConfiguration getUIConfiguration() {
    return myUIConfiguration;
  }

  private boolean myQuitEntered = false;

  /**
   * Quit the application
   */
  public boolean quitApplication() {
    if (myQuitEntered) {
      return false;
    }
    myQuitEntered = true;
    try {
      options.setWindowPosition(getX(), getY());
      options.setWindowSize(getWidth(), getHeight(), (getExtendedState() & Frame.MAXIMIZED_BOTH) != 0);
      options.setUIConfiguration(myUIConfiguration);
      options.save();
      if (myProjectUIFacade.saveChangesDialog(this)) {
        this.close();
        setVisible(false);
        dispose();
        if (ourQuitCallback != null) {
          ourQuitCallback.run();
        }
        return true;
      } else {
        setVisible(true);
        return false;
      }
    } finally {
      myQuitEntered = false;
    }
  }

  public void setAskForSave(boolean afs) {
    if (isOnlyViewer) {
      return;
    }
    fireProjectModified(afs);
    String title = getTitle();
    askForSave = afs;
    try {
      if (System.getProperty("mrj.version") != null) {
        rootPane.putClientProperty("windowModified", Boolean.valueOf(afs));
        // see http://developer.apple.com/qa/qa2001/qa1146.html
      } else {
        if (askForSave) {
          if (!title.endsWith(" *")) {
            setTitle(title + " *");
          }
        }
      }
    } catch (AccessControlException e) {
      // This can happen when running in a sandbox (Java WebStart)
      System.err.println(e + ": " + e.getMessage());
    }
  }

  public ResourceTreePanel getResourcePanel() {
    return resp;
  }

  public GanttGraphicArea getArea() {
    return tree.area;
  }

  public TaskTreePanel getTree() {
    return tree;
  }

  public GPAction getCopyAction() {
    return myViewManager.getCopyAction();
  }

  public GPAction getCutAction() {
    return myViewManager.getCutAction();
  }

  public GPAction getPasteAction() {
    return myViewManager.getPasteAction();
  }

  public ZoomActionSet getZoomActionSet() {
    return myZoomActions;
  }

  public static class Args {
    @Parameter(names = "-log", description = "Enable logging", arity = 1)
    public boolean log = true;

    @Parameter(names = "-log_file", description = "Log file name")
    public String logFile = "auto";

    @Parameter(names = {"-h", "-help"}, description = "Print usage")
    public boolean help = false;

    @Parameter(names = {"-version"}, description = "Print version number")
    public boolean version = false;

    @Parameter(description = "Input file name")
    public List<String> file = null;
  }

  /**
   * The main
   */
  public static boolean main(String[] arg) throws InvocationTargetException, InterruptedException {
    GPLogger.init();
    CommandLineExportApplication cmdlineApplication = new CommandLineExportApplication();
    final Args mainArgs = new Args();
    try {
      JCommander cmdLineParser = new JCommander(new Object[]{mainArgs, cmdlineApplication.getArguments()}, arg);
      if (mainArgs.help) {
        cmdLineParser.usage();
        System.exit(0);
      }
      if (mainArgs.version) {
        System.out.println(GPVersion.getCurrentVersionNumber());
        System.exit(0);
      }
    } catch (Throwable e) {
      e.printStackTrace();
      return false;
    }
    if (mainArgs.log && "auto".equals(mainArgs.logFile)) {
      mainArgs.logFile = System.getProperty("user.home") + File.separator + "ganttproject.log";
    }
    if (mainArgs.log && !mainArgs.logFile.trim().isEmpty()) {
      try {
        GPLogger.setLogFile(mainArgs.logFile);
        File logFile = new File(mainArgs.logFile);
        System.setErr(new PrintStream(new FileOutputStream(logFile)));
        System.out.println("Writing log to " + logFile.getAbsolutePath());
      } catch (IOException e) {
        System.err.println("Failed to write log to file: " + e.getMessage());
        e.printStackTrace();
      }
    }

    GPLogger.logSystemInformation();
    // Check if an export was requested from the command line
    if (cmdlineApplication.export(mainArgs)) {
      // Export succeeded so exit application
      return false;
    }


    AppKt.startUiApp(mainArgs, ganttProject -> {
      ganttProject.setUpdater(org.eclipse.core.runtime.Platform.getUpdater());
      return null;
    });
    return true;
  }

  void doOpenStartupDocument(Args args) {
    fireProjectCreated();
    if (args.file != null && !args.file.isEmpty()) {
      openStartupDocument(args.file.get(0));
    }
  }

  // ///////////////////////////////////////////////////////
  // IGanttProject implementation
  @Override
  public String getProjectName() {
    return prjInfos.getName();
  }

  @Override
  public void setProjectName(String projectName) {
    prjInfos.setName(projectName);
  }

  @Override
  public String getDescription() {
    return prjInfos.getDescription();
  }

  @Override
  public void setDescription(String description) {
    prjInfos.setDescription(description);
  }

  @Override
  public String getOrganization() {
    return prjInfos.getOrganization();
  }

  @Override
  public void setOrganization(String organization) {
    prjInfos.setOrganization(organization);
  }

  @Override
  public String getWebLink() {
    return prjInfos.getWebLink();
  }

  @Override
  public void setWebLink(String webLink) {
    prjInfos.setWebLink(webLink);
  }

  @Override
  public HumanResourceManager getHumanResourceManager() {
    return myHumanResourceManager;
  }

  @Override
  public TaskManager getTaskManager() {
    return myTaskManager;
  }

  @Override
  public RoleManager getRoleManager() {
    return myRoleManager;
  }

  @Override
  public PrjInfos getPrjInfos() {
    return prjInfos;
  }

  @Override
  public CustomPropertyManager getTaskCustomPropertyManager() {
    return myTaskManager.getCustomPropertyManager();
  }

  @Override
  public IProject getCurrentProject() {
    // TODO: instead return a 'Project' object
    return this;
  }

  @Override
  public Document getDocument() {
    return myObservableDocument.get();
  }

  @Override
  public void setDocument(Document document) {
    myObservableDocument.set(document);
  }

  @Override
  public GPCalendarCalc getActiveCalendar() {
    return myCalendar;
  }

  @Override
  public void setModified() {
    setAskForSave(true);
  }

  @Override
  public void setModified(boolean modified) {
    setAskForSave(modified);

    String title = getTitle();
    if (modified == false && title.endsWith(" *")) {
      // Remove * from title
      setTitle(title.substring(0, title.length() - 2));
    }
  }

  @Override
  public boolean isModified() {
    return askForSave;
  }

  @Override
  public void close() {
    fireProjectClosed();
    prjInfos = new PrjInfos();
    prjInfos.addListener(new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        setAskForSave(true);
      }
    });
    myRoleManager.clear();
    myObservableDocument.set(null);
    getTaskCustomPropertyManager().reset();
    getResourceCustomPropertyManager().reset();

    for (int i = 0; i < myPreviousStates.size(); i++) {
      myPreviousStates.get(i).remove();
    }
    myPreviousStates = new ArrayList<GanttPreviousState>();
    myCalendar.reset();
    myFacadeInvalidator.projectClosed();
  }

  // ///////////////////////////////////////////////////////////////
  // ResourceView implementation
  @Override
  public void resourceAdded(ResourceEvent event) {
    if (statusBar != null) {
      // tabpane.setSelectedIndex(1);
      String description = language.getCorrectedLabel("resource.new.description");
      if (description == null) {
        description = language.getCorrectedLabel("resource.new");
      }
      myUIFacade.setStatusText(description);
      setAskForSave(true);
      refreshProjectInformation();
    }
  }

  @Override
  public void resourcesRemoved(ResourceEvent event) {
    refreshProjectInformation();
    setAskForSave(true);
  }

  @Override
  public void resourceChanged(ResourceEvent e) {
    setAskForSave(true);
  }

  @Override
  public void resourceAssignmentsChanged(ResourceEvent e) {
    setAskForSave(true);
  }

  // ///////////////////////////////////////////////////////////////
  // UIFacade

  public GanttChart getGanttChart() {
    return tree.area;
  }

  public TimelineChart getResourceChart() {
    return resp.area;
  }

  public int getGanttDividerLocation() {
    return myGanttChartTabContent.getDividerLocation();
  }

  public void setGanttDividerLocation(int location) {
    myGanttChartTabContent.setDividerLocation(location);
  }

  public int getResourceDividerLocation() {
    return myResourceChartTabContent.getDividerLocation();
  }

  public void setResourceDividerLocation(int location) {
    myResourceChartTabContent.setDividerLocation(location);
  }

  public TaskTreeUIFacade getTaskTree() {
    return tree;
  }

  public ResourceTreeUIFacade getResourceTree() {
    return resp;
  }

  private class ParserFactoryImpl implements ParserFactory {
    @Override
    public GPSaver newSaver() {
      return new GanttXMLSaver(GanttProject.this, tree.area, myUIFacade);
    }
  }

  public int getViewIndex() {
    if (myTabPane == null) {
      return -1;
    }
    return myTabPane.getSelectedIndex();
  }

  public void setViewIndex(int viewIndex) {
    if (myTabPane.getTabCount() > viewIndex) {
      myTabPane.setSelectedIndex(viewIndex);
    }
  }

  public static void setApplicationQuitCallback(Runnable callback) {
    ourQuitCallback = callback;
  }

  public void refresh() {
    myTaskManager.processCriticalPath(myTaskManager.getRootTask());
    resp.getResourceTreeTableModel().updateResources();
    resp.getResourceTreeTable().setRowHeight(getResourceChart().getModel().calculateRowHeight());
    for (Chart chart : PluginManager.getCharts()) {
      chart.reset();
    }
    super.repaint();
  }


  //// GanttProjectBase methods /////////////// ////////////////////////////////////////////
  @Override
  public void addProjectEventListener(ProjectEventListener listener) {
    myModifiedStateChangeListeners.add(listener);
  }

  @Override
  public void removeProjectEventListener(ProjectEventListener listener) {
    myModifiedStateChangeListeners.remove(listener);
  }

  private void fireProjectModified(boolean isModified) {
    for (ProjectEventListener modifiedStateChangeListener : myModifiedStateChangeListeners) {
      try {
        if (isModified) {
          modifiedStateChangeListener.projectModified();
        } else {
          modifiedStateChangeListener.projectSaved();
        }
      } catch (Exception e) {
        myUIFacade.showErrorDialog(e);
      }
    }
  }

  private void fireProjectCreated() {
    for (ProjectEventListener modifiedStateChangeListener : myModifiedStateChangeListeners) {
      modifiedStateChangeListener.projectCreated();
    }
    // A new project just got created, so it is not yet modified
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        setModified(false);
      }
    });
  }

  private void fireProjectClosed() {
    for (ProjectEventListener modifiedStateChangeListener : myModifiedStateChangeListeners) {
      modifiedStateChangeListener.projectClosed();
    }
  }

  private void fireProjectOpened() {
    for (ProjectEventListener modifiedStateChangeListener : myModifiedStateChangeListeners) {
      modifiedStateChangeListener.projectOpened();
    }
  }

  // ////////////////////////////////////////////////////////////////
  public ProjectUIFacade getProjectUIFacade() {
    return myProjectUIFacade;
  }

  public UIFacade getUIFacade() {
    return myUIFacade;
  }

  public GPUndoManager getUndoManager() {
    return myUndoManager;
  }

  public GPViewManager getViewManager() {
    return myViewManager;
  }

  public Chart getActiveChart() {
    return myViewManager.getSelectedView().getChart();
  }

  protected static class RowHeightAligner implements GPOptionChangeListener {
    private final ChartModelBase myChartModel;

    private final TreeTableContainer myTreeView;

    public RowHeightAligner(TreeTableContainer treeView, ChartModelBase chartModel) {
      myChartModel = chartModel;
      myTreeView = treeView;
      myChartModel.addOptionChangeListener(this);
    }

    @Override
    public void optionsChanged() {
      myTreeView.getTreeTable().setRowHeight(myChartModel.calculateRowHeight());
      AbstractTableModel model = (AbstractTableModel) myTreeView.getTreeTable().getModel();
      model.fireTableStructureChanged();
      myTreeView.updateUI();
    }
  }

  private void createContentPane(JComponent toolbar) {
    myContentPaneBuilder.build(toolbar, getContentPane());
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension windowSize = getPreferredSize();
    // Put the frame at the middle of the screen
    setLocation(screenSize.width / 2 - (windowSize.width / 2), screenSize.height / 2 - (windowSize.height / 2));
    pack();
  }
  
  @Override
  public TimeUnitStack getTimeUnitStack() {
    return myTimeUnitStack;
  }

  @Override
  public CustomPropertyManager getResourceCustomPropertyManager() {
    return myResourceCustomPropertyManager;
  }

  protected void setUpdater(Updater updater) {
    myUpdater = updater;
  }
  
  @Override
  public DocumentManager getDocumentManager() {
    return myDocumentManager;
  }
}
