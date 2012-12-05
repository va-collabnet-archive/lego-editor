package gov.va.legoEdit;

import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.gui.legoInfoPanel.LegoInfoPanel;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeNodeType;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeView;
import gov.va.legoEdit.gui.sctTreeView.SimTreeView;
import gov.va.legoEdit.gui.util.DropTargetLabel;
import gov.va.legoEdit.gui.util.LegoTab;
import gov.va.legoEdit.gui.util.LegoTreeItemComparator;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.search.PNCS.PncsSearchModel;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.wb.WBDataStore;
import gov.va.legoEdit.util.TimeConvert;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import org.ihtsdo.fxmodel.FxTaxonomyReferenceWithConcept;
import org.ihtsdo.fxmodel.concept.FxConcept;
import org.ihtsdo.fxmodel.fetchpolicy.RefexPolicy;
import org.ihtsdo.fxmodel.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.fxmodel.fetchpolicy.VersionPolicy;
import org.ihtsdo.tk.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.tk.binding.Taxonomies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegoGUIController implements Initializable
{
    Logger logger = LoggerFactory.getLogger(LegoGUIController.class);
  
    private MenuItem menuDeleteLegoList;
    private SimTreeView sctTree;
    private LegoTreeView legoListTV;
    private HashMap<String, ArrayList<Node>> snomedCodeDropTargets = new HashMap<>();
    private HashMap<String, Tab> displayedLegos = new HashMap<>();
    private Thread dbConnectThread;

    // Copypaste from gui tool
    @FXML //  fx:id="rootPane"
    private AnchorPane rootPane; // Value injected by FXMLLoader
    @FXML //  fx:id="editorTabPane"
    private TabPane editorTabPane; // Value injected by FXMLLoader
    @FXML //  fx:id="leftButtons"
    private AnchorPane leftButtons; // Value injected by FXMLLoader
    @FXML //  fx:id="leftPaneLabel"
    private Label leftPaneLabel; // Value injected by FXMLLoader
    @FXML //  fx:id="menu"
    private MenuBar menu; // Value injected by FXMLLoader
    @FXML //  fx:id="menuFile"
    private Menu menuFile; // Value injected by FXMLLoader
    @FXML //  fx:id="menuFileExit"
    private MenuItem menuFileExit; // Value injected by FXMLLoader
    @FXML //  fx:id="menuFileImport"
    private MenuItem menuFileImport; // Value injected by FXMLLoader
    @FXML //  fx:id="menuSearchByPNCS"
    private MenuItem menuSearchByPNCS; // Value injected by FXMLLoader
    @FXML //  fx:id="menuSearchLegos"
    private Menu menuSearchLegos; // Value injected by FXMLLoader
    @FXML //  fx:id="menuView"
    private Menu menuView; // Value injected by FXMLLoader
    @FXML //  fx:id="showAllLegoListBtn"
    private ToggleButton showAllLegoListBtn; // Value injected by FXMLLoader
    @FXML //  fx:id="showSearchLegoListBtn"
    private ToggleButton showSearchLegoListBtn; // Value injected by FXMLLoader
    @FXML //  fx:id="showSnomedBtn"
    private ToggleButton showSnomedBtn; // Value injected by FXMLLoader
    @FXML //  fx:id="splitLeft"
    private AnchorPane splitLeft; // Value injected by FXMLLoader
    @FXML //  fx:id="splitLeftAllLegos"
    private AnchorPane splitLeftAllLegos; // Value injected by FXMLLoader
    @FXML //  fx:id="splitLeftFilteredLegos"
    private AnchorPane splitLeftFilteredLegos; // Value injected by FXMLLoader
    @FXML //  fx:id="splitLeftSct"
    private AnchorPane splitLeftSct; // Value injected by FXMLLoader
    @FXML //  fx:id="splitPane"
    private SplitPane splitPane; // Value injected by FXMLLoader
    @FXML //  fx:id="splitRight"
    private AnchorPane splitRight; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources)
    {
        // Copy paste from gui tool
        assert editorTabPane != null : "fx:id=\"editorTabPane\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert leftButtons != null : "fx:id=\"leftButtons\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert leftPaneLabel != null : "fx:id=\"leftPaneLabel\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert menu != null : "fx:id=\"menu\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert menuFile != null : "fx:id=\"menuFile\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert menuFileExit != null : "fx:id=\"menuFileExit\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert menuFileImport != null : "fx:id=\"menuFileImport\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert menuSearchByPNCS != null : "fx:id=\"menuSearchByPNCS\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert menuSearchLegos != null : "fx:id=\"menuSearchLegos\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert menuView != null : "fx:id=\"menuView\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert showAllLegoListBtn != null : "fx:id=\"showAllLegoListBtn\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert showSearchLegoListBtn != null : "fx:id=\"showSearchLegoListBtn\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert showSnomedBtn != null : "fx:id=\"showSnomedBtn\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert splitLeft != null : "fx:id=\"splitLeft\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert splitLeftAllLegos != null : "fx:id=\"splitLeftAllLegos\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert splitLeftFilteredLegos != null : "fx:id=\"splitLeftFilteredLegos\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert splitLeftSct != null : "fx:id=\"splitLeftSct\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert splitPane != null : "fx:id=\"splitPane\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert splitRight != null : "fx:id=\"splitRight\" was not injected: check your FXML file 'LegoGUI.fxml'.";


        // initialize your logic here: all @FXML variables will have been injected

       

        // legoList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
        // {
        // @Override
        // public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue)
        // {
        // changeSelectedLego(newValue);
        // }
        // });
        //
        // filteredLegoList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
        // {
        // @Override
        // public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue)
        // {
        // changeSelectedLego(newValue);
        // }
        // });


    }
    
    /**
     * Some of our init code needs the scene, etc, to be set up.
     */
    protected void finishInit()
    {
        legoListTV = new LegoTreeView();
        splitLeftAllLegos.getChildren().add(legoListTV.wrapInScrollPane());

        LegoGUIModel.getInstance().initializeLegoListNames(legoListTV.getRoot().getChildren());

        // filteredLegoList.setItems(LegoGUIModel.getInstance().getLegoListNames());

        setupMenus();
        setupSctTree(); // This kicks off a thread that opens the DB connection
        
        rootPane.getScene().getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent event)
            {
                shutdown();
            }
        });

        // legoList.setCellFactory(ContextMenuListCell.<String>forListView(new ContextMenu(menuDeleteLego)));
        // filteredLegoList.setCellFactory(ContextMenuListCell.<String>forListView(new ContextMenu(menuDeleteLego)));

        //huh, the FXGui editor is broken - I have to set this policy manually.
        editorTabPane.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
        editorTabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>()
        {
            @Override
            public void changed(ObservableValue<? extends Tab> tab, Tab oldTab, Tab newTab)
            {
                //TODO find and highlight lego in tree
            }
        });

        final ToggleGroup tg = new ToggleGroup();
        tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
        {
            double dividerPosition = 0.3;

            public void changed(ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle new_toggle)
            {
                if (new_toggle == null)
                {
                    dividerPosition = splitPane.getDividerPositions()[0];
                    splitPane.getItems().remove(splitLeft);
                }
                else if (!splitPane.getItems().contains(splitLeft))
                {
                    splitPane.getItems().add(0, splitLeft);
                    splitPane.setDividerPosition(0, dividerPosition);
                }
            }
        });

        showAllLegoListBtn.setTooltip(new Tooltip("Show all Lego Lists in the system"));
        showAllLegoListBtn.setToggleGroup(tg);
        showAllLegoListBtn.setSelected(true);
        showAllLegoListBtn.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                if (showAllLegoListBtn.isSelected())
                {
                    splitLeftSct.setVisible(false);
                    splitLeftFilteredLegos.setVisible(false);
                    leftPaneLabel.setText("All Lego Lists");
                    splitLeftAllLegos.setVisible(true);
                }
            }
        });

        showSearchLegoListBtn.setTooltip(new Tooltip("Show a filtered view of Lego Lists"));
        showSearchLegoListBtn.setToggleGroup(tg);
        showSearchLegoListBtn.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                if (showSearchLegoListBtn.isSelected())
                {
                    splitLeftSct.setVisible(false);
                    splitLeftAllLegos.setVisible(false);
                    splitLeftFilteredLegos.setVisible(true);
                    leftPaneLabel.setText("Filtered Lego Lists");
                    // TODO
                }
            }
        });
        showSearchLegoListBtn.setVisible(false);

        showSnomedBtn.setTooltip(new Tooltip("Show the Snomed Tree"));
        showSnomedBtn.setToggleGroup(tg);
        showSnomedBtn.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                if (showSnomedBtn.isSelected())
                {
                    splitLeftAllLegos.setVisible(false);
                    splitLeftFilteredLegos.setVisible(false);
                    leftPaneLabel.setText("Snomed Browser");
                    splitLeftSct.setVisible(true);
                }
            }
        });
    }
    
    private void addSnomedDropTargetInternal(Lego lego, Node node)
    {
        String legoId = makeUniqueLegoID(lego);
        ArrayList<Node> nodes = snomedCodeDropTargets.get(legoId);
        if (nodes == null)
        {
            nodes = new ArrayList<Node>();
            snomedCodeDropTargets.put(legoId, nodes);
        }
        nodes.add(node);
    }
    
    public void addSnomedDropTarget(Lego lego, final DropTargetLabel label)
    {
        if (label.getDropContextMenu().getItems().size() == 0)
        {
            return;
        }
        addSnomedDropTargetInternal(lego, (Node)label);

        setSnomedDropShadows(label);
        
        label.setOnDragDropped(new EventHandler<DragEvent>()
        {
            public void handle(DragEvent event)
            {
                /* data dropped */
                Dragboard db = event.getDragboard();
                boolean success = false;
                try
                {
                    if (db.hasString())
                    {
                        ContextMenu cm =  label.getDropContextMenu();
                        if (cm != null)
                        {
                            label.setDroppedValue(db.getString());
                            cm.show(label, Side.RIGHT, 0.0, 0.0);
                        }
                        success = true;
                    }
                }
                catch (Exception ex)
                {
                    logger.error("Error dropping snomed concept", ex);
                    LegoGUI.getInstance().showErrorDialog("Unexpected Error",
                            "There was an unexpected error dropping the snomed concept", ex.toString());
                }
                /*
                 * let the source know whether the string was successfully transferred and used
                 */
                event.setDropCompleted(success);
                event.consume();
            }
        });

    }

    public void addSnomedDropTarget(Lego lego, final ComboBox<String> n)
    {
        addSnomedDropTargetInternal(lego, ((Node)n));

        setSnomedDropShadows(n);

        n.setOnDragDropped(new EventHandler<DragEvent>()
        {
            public void handle(DragEvent event)
            {
                /* data dropped */
                Dragboard db = event.getDragboard();
                boolean success = false;
                try
                {
                    if (db.hasString())
                    {
                        n.setValue(db.getString());
                        success = true;
                    }
                }
                catch (Exception ex)
                {
                    logger.error("Error dropping snomed concept", ex);
                    LegoGUI.getInstance().showErrorDialog("Unexpected Error",
                            "There was an unexpected error dropping the snomed concept", ex.toString());
                }
                /*
                 * let the source know whether the string was successfully transferred and used
                 */
                event.setDropCompleted(success);
                event.consume();
            }
        });
    }
    
    private void setSnomedDropShadows(final Node n)
    {
        n.setOnDragOver(new EventHandler<DragEvent>()
        {
            public void handle(DragEvent event)
            {
                /*
                 * data is dragged over the target accept it only if it is not dragged from the same node and if it has
                 * a string data
                 */
                if (event.getGestureSource() != n && event.getDragboard().hasString())
                {
                    /* allow for both copying and moving, whatever user chooses */
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            }
        });

        n.setOnDragEntered(new EventHandler<DragEvent>()
        {
            public void handle(DragEvent event)
            {
                /* show to the user that it is an actual gesture target */
                DropShadow ds = new DropShadow();
                ds.setColor(Color.GREEN);
                n.setEffect(ds);
                event.consume();
            }
        });

        n.setOnDragExited(new EventHandler<DragEvent>()
        {
            public void handle(DragEvent event)
            {
                /* mouse moved away, remove the graphical cues */
                DropShadow ds = new DropShadow();
                ds.setColor(Color.LIGHTGREEN);
                n.setEffect(ds);
                event.consume();
            }
        });
    }
    

    private void snomedDragStarted()
    {
        String legoId = ((LegoTab)editorTabPane.getSelectionModel().getSelectedItem()).getDisplayedLegoID();
        for (Node n : snomedCodeDropTargets.get(legoId))
        {
            DropShadow ds = new DropShadow();
            ds.setColor(Color.LIGHTGREEN);
            n.setEffect(ds);
        }
    }

    private void snomedDragCompleted()
    {
        String legoId = ((LegoTab)editorTabPane.getSelectionModel().getSelectedItem()).getDisplayedLegoID();
        for (Node n : snomedCodeDropTargets.get(legoId))
        {
            n.setEffect(null);
        }
    }

    private void setupSctTree()
    {
        // Do the SCT connecting in a background thread - if it is a local DB, it will be slow.
        Runnable r = new Runnable()
        {
            FxConcept fxc;

            @Override
            public void run()
            {
                logger.info("Opening Workbench Database");
                try
                {
                    fxc = WBDataStore.Ts().getFxConcept(Taxonomies.SNOMED.getUuids()[0],
                            StandardViewCoordinates.getSnomedLatest(), VersionPolicy.ACTIVE_VERSIONS,
                            RefexPolicy.REFEX_MEMBERS,
                            RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
                    logger.info("Finished Opening Workbench Database");
                    
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                sctTree = new SimTreeView(fxc, WBDataStore.Ts());
                                sctTree.setOnDragDetected(new EventHandler<MouseEvent>()
                                {
                                    public void handle(MouseEvent event)
                                    {
                                        /* drag was detected, start a drag-and-drop gesture */
                                        /* allow any transfer mode */
                                        Dragboard db = sctTree.startDragAndDrop(TransferMode.COPY);
        
                                        /* Put a string on a dragboard */
                                        TreeItem<FxTaxonomyReferenceWithConcept> dragItem = sctTree.getSelectionModel()
                                                .getSelectedItem();
        
                                        ClipboardContent content = new ClipboardContent();
                                        content.putString(dragItem.getValue().getConcept().getPrimordialUuid().toString());
                                        db.setContent(content);
                                        snomedDragStarted();
                                        event.consume();
                                    }
                                });
        
                                sctTree.setOnDragDone(new EventHandler<DragEvent>()
                                {
                                    public void handle(DragEvent event)
                                    {
                                        snomedDragCompleted();
                                    }
                                });
        
                                AnchorPane.setTopAnchor(sctTree, 0.0);
                                AnchorPane.setBottomAnchor(sctTree, 0.0);
                                AnchorPane.setLeftAnchor(sctTree, 0.0);
                                AnchorPane.setRightAnchor(sctTree, 0.0);
                                splitLeftSct.getChildren().remove(0);
                                splitLeftSct.getChildren().add(sctTree);
                            }
                            catch (Exception e)
                            {
                                logger.error("Couldn't open the WB DB - snomed will not be available", e);
                            }
                        }
                    });
                }
                catch (Exception e)
                {
                    logger.error("Unexpected error connecting to workbench database", e);
                }

                dbConnectThread = null;
            }
        };

        dbConnectThread = new Thread(r, "SCT_DB_Open");
        dbConnectThread.start();
    }

    private void setupMenus()
    {
        menuFileImport.setGraphic(new ImageView(new Image(LegoGUI.class
                .getResourceAsStream("/fugue/16x16/icons/folder-open-table.png"))));
        menuFileImport.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
        menuFileImport.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent t)
            {
                FileChooser fc = new FileChooser();
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("LEGO xml Files (*.xml)", "*.xml"));
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*"));
                List<File> files = fc.showOpenMultipleDialog(rootPane.getScene().getWindow());
                if (files != null && files.size() > 0)
                {
                    StringBuilder errors = new StringBuilder();
                    for (File f : files)
                    {
                        if (f.exists() && f.isFile())
                        {
                            try
                            {
                                LegoXMLUtils.validate((f));
                                LegoGUIModel.getInstance().importLegoList(LegoXMLUtils.readLegoList(f));
                            }
                            catch (Exception ex)
                            {
                                logger.info("Error loading file " + f.getName(), ex);
                                errors.append("Error loading file " + f.getName() + ": ");
                                errors.append((ex.getLocalizedMessage() == null ? ex.toString() : ex
                                        .getLocalizedMessage()));
                                errors.append(System.getProperty("line.separator"));
                                errors.append(System.getProperty("line.separator"));
                            }
                        }
                    }
                    if (errors.length() > 0)
                    {
                        LegoGUI.getInstance().showErrorDialog("Error Loading LEGOs",
                                "There was an error loading the specified files", errors.toString());
                    }
                }
            }
        });

        menuFileExit.setGraphic(new ImageView(new Image(LegoGUI.class
                .getResourceAsStream("/fugue/16x16/icons/cross.png"))));
        menuFileExit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        menuFileExit.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent t)
            {
                shutdown();
            }
        });

        menuSearchByPNCS.setGraphic(new ImageView(new Image(LegoGUI.class
                .getResourceAsStream("/fugue/16x16/icons/cross.png"))));
        menuSearchByPNCS.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        menuSearchByPNCS.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent t)
            {
                LegoGUI.getInstance().showPNCSSearchDialog(PncsSearchModel.getInstance().getPncsIdComboList());
            }
        });
        menuSearchByPNCS.setDisable(true);

        // Floating Context menus
        menuDeleteLegoList = new MenuItem("Delete LegoList");
        menuDeleteLegoList.setDisable(true);
        menuDeleteLegoList.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
//                try
//                {
////                    LegoGUIModel.getInstance().removeLegoList(currentLegoList.getGroupName());
////                    selectedLegoListChanged(null);
//                }
//                catch (WriteException ex)
//                {
//                    logger.error("Unexpeted error removing lego", ex);
//                    LegoGUI.getInstance().showErrorDialog("Error Removing Lego",
//                            "There was an removing the specified file", ex.getLocalizedMessage());
//                }
            }
        });
    }

    public Lego getLegoBeingEdited()
    {
        return null;
    }
    
    private String makeUniqueLegoID(Lego lego)
    {
        return lego.getLegoUUID() + lego.getStamp().getUuid();
    }

    public void beginLegoEdit(Lego newLego)
    {
        if (newLego != null)
        {
            String legoId = makeUniqueLegoID(newLego);
            if (displayedLegos.containsKey(legoId))
            {
                editorTabPane.getSelectionModel().select(displayedLegos.get(legoId));
            }
            else
            {
                final LegoTab tab = new LegoTab("Lego", legoId);
                displayedLegos.put(legoId, tab);
                LegoTreeView legoTree = new LegoTreeView();
                legoTree.setLego(newLego);
                
                BorderPane bp = new BorderPane();
                bp.setTop(LegoInfoPanel.build(newLego.getPncs().getName(), newLego.getPncs().getValue(), newLego.getPncs().getId() + "",
                        newLego.getLegoUUID(), newLego.getStamp().getAuthor(), newLego.getStamp().getModule(), 
                        new Date(TimeConvert.convert(newLego.getStamp().getTime())).toString(), newLego.getStamp().getPath()));
                bp.setCenter(legoTree.wrapInScrollPane());
                
                tab.setContent(bp);
                tab.setOnClosed(new EventHandler<Event>()
                {
                    public void handle(Event arg0) 
                    {
                        LegoGUIController.this.legoEditTabClosed(tab);
                    }
                });
                
                editorTabPane.getTabs().add(tab);
                editorTabPane.getSelectionModel().select(tab);
                legoTree.getRoot().getChildren().add(new LegoTreeItem(newLego.getStamp().getStatus(), LegoTreeNodeType.status));
                for (Assertion a : newLego.getAssertion())
                {
                    legoTree.getRoot().getChildren().add(new LegoTreeItem(a));
                }
                legoTree.getRoot().getChildren().add(new LegoTreeItem("Add Assertion", LegoTreeNodeType.addAssertionPlaceholder));
                recursiveSort(legoTree.getRoot().getChildren());
                expandAll(legoTree.getRoot());
            }
        }
    }
    
    private void recursiveSort(ObservableList<TreeItem<String>> items)
    {
        FXCollections.sort(items, new LegoTreeItemComparator(true));
        for (TreeItem<String> item : items)
        {
            recursiveSort(item.getChildren());
        }
    }
    
    public void legoEditTabClosed(LegoTab tab)
    {
        displayedLegos.remove(tab.getDisplayedLegoID());
        snomedCodeDropTargets.remove(tab.getDisplayedLegoID());
    }

    private void expandAll(TreeItem<String> ti)
    {
        ti.setExpanded(true);
        for (TreeItem<String> tiChild : ti.getChildren())
        {
            expandAll(tiChild);
        }
    }

//    public void selectedLegoListChanged(String newLegoList)
//    {
//        // TODO Make this filter aware
//        tabsRenderedSinceSelect.clear();
//        if (newLegoList == null)
//        {
//            currentLegoList = null;
//            menuDeleteLegoList.setDisable(true);
//            menuDeleteLegoList.setText("Delete Lego");
//            // legoList.getSelectionModel().clearSelection();
//            // filteredLegoList.getSelectionModel().clearSelection();
//        }
//        else
//        {
//            if (newLegoList.contains("PNCS:") && newLegoList.contains("Val:"))
//            {
//                Lego l = LegoGUIModel.getInstance().getLego(newLegoList);
//                currentLegoList = new LegoList();
//                currentLegoList.getLego().add(l);
//            }
//            else
//            {
//                currentLegoList = LegoGUIModel.getInstance().getLegoList(newLegoList);
//            }
//
//            if (currentLegoList == null)
//            {
//                LegoGUI.getInstance().showErrorDialog("Error Reading LegoList",
//                        "Unexpected error reading LegoList from storage",
//                        "The LegoList for '" + newLegoList + "' could not be found");
//                // legoList.getSelectionModel().clearSelection();
//            }
//            else
//            {
//                menuDeleteLegoList.setText("Delete Lego '" + newLegoList + "'");
//                menuDeleteLegoList.setDisable(false);
//            }
//        }
//        // updateXMLTab();
//        updateEditorTab();
//    }

//    private void updateEditorTab()
//    {
//        if (tabPane.getSelectionModel().getSelectedItem() == editTab && !tabsRenderedSinceSelect.contains(editTab))
//        {
//            legoListTV.setRoot(new LegoTreeItem(currentLegoList));
//            tabsRenderedSinceSelect.add(editTab);
//        }
//    }

    

    private void shutdown()
    {
        logger.info("shutdown called");
        SimTreeView.shutdown();
        Thread t = dbConnectThread;
        if (t != null)
        {
            try
            {
                logger.info("Waiting for DB init thread to complete before continuing shutdown");
                t.join();
            }
            catch (InterruptedException e)
            {
                // noop
            }
        }
        BDBDataStoreImpl.getInstance().shutdown();
        WBDataStore.shutdown();
        System.exit(0);
    }
}
