package gov.va.legoEdit;

import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.gui.dialogs.YesNoDialogController.Answer;
import gov.va.legoEdit.gui.legoFilterPane.LegoFilterPaneController;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.sctSearch.SnomedSearchPaneController;
import gov.va.legoEdit.gui.sctTreeView.SimTreeView;
import gov.va.legoEdit.gui.util.DropTargetLabel;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.gui.util.LegoTab;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.ModelUtil;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.DataStoreException;
import gov.va.legoEdit.storage.WriteException;
import gov.va.legoEdit.storage.wb.WBDataStore;
import gov.va.legoEdit.storage.wb.WBUtility;
import gov.va.legoEdit.util.UnsavedLegos;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
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
  
    private SimTreeView sctTree;
    private LegoFilterPaneController lfpc;
    private HashMap<String, ArrayList<Node>> snomedCodeDropTargets = new HashMap<>();
    private HashMap<Node, Effect> existingEffect = new HashMap<Node, Effect>();
    private HashMap<String, LegoTab> displayedLegos = new HashMap<>();
    private HashMap<String, StringProperty> displayedLegosStyleInfo = new HashMap<>();
    private UnsavedLegos newLegos = new UnsavedLegos();
    private SnomedSearchPaneController sspc;
    private Thread dbConnectThread;
    Random random = new Random();
    
    private BooleanBinding enableSaveButton;
    
    private static String NONE = "NONE";

    // Copypaste from gui tool
    @FXML //  fx:id="rootPane"
    private AnchorPane rootPane; // Value injected by FXMLLoader
    @FXML //  fx:id="buttonSaveLego"
    private Button buttonSaveLego; // Value injected by FXMLLoader
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
    @FXML //  fx:id="menuFileCreateLego"
    private MenuItem menuFileCreateLego; // Value injected by FXMLLoader
    @FXML //  fx:id="menuFileExportLegoLists"
    private MenuItem menuFileExportLegoLists; // Value injected by FXMLLoader
    @FXML //  fx:id="menuEdit"
    private Menu menuEdit; // Value injected by FXMLLoader
    @FXML //  fx:id="menuEditPreferences"
    private MenuItem menuEditPreferences; // Value injected by FXMLLoader
    @FXML //  fx:id="menuRecentSctCodes"
    private MenuButton menuRecentSctCodes; // Value injected by FXMLLoader
    @FXML //  fx:id="showAllLegoListBtn"
    private ToggleButton showAllLegoListBtn; // Value injected by FXMLLoader
    @FXML //  fx:id="showSnomedBtn"
    private ToggleButton showSnomedBtn; // Value injected by FXMLLoader
    @FXML //  fx:id="showSnomedSearchBtn"
    private ToggleButton showSnomedSearchBtn; // Value injected by FXMLLoader
    @FXML //  fx:id="splitLeft"
    private AnchorPane splitLeft; // Value injected by FXMLLoader
    @FXML //  fx:id="splitLeftAllLegos"
    private AnchorPane splitLeftAllLegos; // Value injected by FXMLLoader
    @FXML //  fx:id="splitLeftSctSearch"
    private AnchorPane splitLeftSctSearch; // Value injected by FXMLLoader
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
        assert menuEdit != null : "fx:id=\"menuView\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert showAllLegoListBtn != null : "fx:id=\"showAllLegoListBtn\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert showSnomedBtn != null : "fx:id=\"showSnomedBtn\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert splitLeft != null : "fx:id=\"splitLeft\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert splitLeftAllLegos != null : "fx:id=\"splitLeftAllLegos\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert splitLeftSct != null : "fx:id=\"splitLeftSct\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert splitPane != null : "fx:id=\"splitPane\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert splitRight != null : "fx:id=\"splitRight\" was not injected: check your FXML file 'LegoGUI.fxml'.";

        // initialize your logic here: all @FXML variables will have been injected
    }
    
    /**
     * Some of our init code needs the scene, etc, to be set up.
     */
    protected void finishInit()
    {
        lfpc = LegoFilterPaneController.init();
        splitLeftAllLegos.getChildren().add(lfpc.getBorderPane());
        
        sspc = SnomedSearchPaneController.init();
        splitLeftSctSearch.getChildren().add(sspc.getBorderPane());

        setupMenus();
        setupSctTree(); // This kicks off a thread that opens the DB connection
        
        rootPane.getScene().getWindow().setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent event)
            {
                //Note - this is broke with javafx < 2.2.6 (which isn't released yet - currently beta - with the 1.7.0_12 early access release)
                //http://javafx-jira.kenai.com/browse/RT-25528
                shutdown();
                event.consume();

            }
        });

        //huh, the FXGui editor is broken - I have to set this policy manually.
        editorTabPane.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
        editorTabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>()
        {
            @Override
            public void changed(ObservableValue<? extends Tab> tab, Tab oldTab, Tab newTab)
            {
                if (newTab != null)
                {
                    showTreeItem(null, ((LegoTab)newTab).getDisplayedLegoID());
                }
                enableSaveButton.invalidate();
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

        showAllLegoListBtn.setGraphic(Images.LEGO_LIST_VIEW.createImageView());
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
                    splitLeftSctSearch.setVisible(false);
                    leftPaneLabel.setText("Lego Lists");
                    splitLeftAllLegos.setVisible(true);
                }
            }
        });

        showSnomedBtn.setGraphic(Images.ROOT.createImageView());
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
                    splitLeftSctSearch.setVisible(false);
                    leftPaneLabel.setText("Snomed Browser");
                    splitLeftSct.setVisible(true);
                }
            }
        });
        
        showSnomedSearchBtn.setGraphic(Images.LEGO_SEARCH.createImageView());
        showSnomedSearchBtn.setTooltip(new Tooltip("Show the Search Panel"));
        showSnomedSearchBtn.setToggleGroup(tg);
        showSnomedSearchBtn.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                if (showSnomedSearchBtn.isSelected())
                {
                    splitLeftAllLegos.setVisible(false);
                    splitLeftSct.setVisible(false);
                    leftPaneLabel.setText("Snomed Search");
                    splitLeftSctSearch.setVisible(true);
                }
            }
        });
        
        enableSaveButton = new BooleanBinding()
        {
            @Override
            protected boolean computeValue()
            {
                Tab t = editorTabPane.getSelectionModel().getSelectedItem();
                if (t != null)
                {
                    return ((LegoTab)t).hasUnsavedChangesProperty().get();
                }
                return false;
            }
        };
        enableSaveButton.invalidate();
        
        
        buttonSaveLego.setGraphic(Images.SAVE.createImageView());
        buttonSaveLego.disableProperty().bind(enableSaveButton.not());
        buttonSaveLego.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                LegoTab lt = (LegoTab)editorTabPane.getSelectionModel().getSelectedItem();
                Lego lego = lt.getLego();
                String oldId = ModelUtil.makeUniqueLegoID(lego);
                displayedLegos.remove(oldId);
                StringProperty style = displayedLegosStyleInfo.remove(oldId);
                ArrayList<Node> dropTargets = snomedCodeDropTargets.get(oldId);
                
                
                String legoListUUIDtoUse = null;
                for (String legoListId : BDBDataStoreImpl.getInstance().getLegoListByLego(lego.getLegoUUID()))
                {
                    //We may get more than one legoList - need to look through each of them - find the one that has the matching stamp.
                    LegoList ll = BDBDataStoreImpl.getInstance().getLegoListByID(legoListId);
                    for (Lego tempLego : ll.getLego()) 
                    {
                        if (oldId.equals(ModelUtil.makeUniqueLegoID(tempLego)))
                        {
                            if (legoListUUIDtoUse != null)
                            {
                               logger.error("Found more than one lego list with the same lego / stamp combination.  Only saving the new lego to the first");
                            }
                            else
                            {
                                legoListUUIDtoUse = legoListId;
                            }
                            break;
                        }
                    }
                }
                
                if (legoListUUIDtoUse == null)
                {
                    logger.error("Couldn't find the right legoList to store the new Lego to!");
                    LegoGUI.getInstance().showErrorDialog("Error Saving Changes", "Couldn't find the right Lego List to store the Lego to.", null);
                    return;
                }
                
                try
                {
                    Stamp updatedStamp = BDBDataStoreImpl.getInstance().commitLego(lego, legoListUUIDtoUse);
                    lego.setStamp(updatedStamp);
                    String newId = ModelUtil.makeUniqueLegoID(lego);
                    displayedLegos.put(newId, lt);
                    displayedLegosStyleInfo.put(newId, style);
                    snomedCodeDropTargets.put(newId, dropTargets);
                    getLegoFilterPaneController().updateLegoList();
                    lt.hasUnsavedChangesProperty().invalidate();
                    lt.updateInfoPanel(updatedStamp);
                }
                catch (DataStoreException | WriteException e)
                {
                    logger.error("Unexpected error saving Lego", e);
                    LegoGUI.getInstance().showErrorDialog("Error Saving Changes", "Unexpected error storing lego", e.toString());
                }
            }
        });
    }
    
    private void addSnomedDropTargetInternal(Lego lego, Node node)
    {
        String legoId;
        if (lego == null)
        {
            legoId = NONE;
        }
        else
        {
            legoId = ModelUtil.makeUniqueLegoID(lego);
        }
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
                            updateRecentCodes(db.getString());
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
                        updateRecentCodes(db.getString());
                        success = true;
                        //It will have updated its effect upon the set - we don't want to restore an old one.
                        existingEffect.remove(n);
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
    
    public void addSnomedDropTarget(final TextField n)
    {
        addSnomedDropTargetInternal(null, ((Node)n));

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
                        n.setText(db.getString());
                        updateRecentCodes(db.getString());
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
    
    public void updateRecentCodes(String newCode)
    {
        ObservableList<MenuItem> items =  menuRecentSctCodes.getItems();
        MenuItem temp = null;
        for (MenuItem mi: items)
        {
            if (mi.getUserData().equals(newCode))
            {
                temp = mi;
                break;
            }
        }
        if (temp != null)
        {
            //Move it to the top
            items.remove(temp);
            items.add(0, temp);
        }
        else
        {
            Concept c = WBUtility.lookupSnomedIdentifier(newCode);
            final Label l = new Label(c == null ? newCode : c.getDesc());
            final MenuItem mi = new MenuItem(null, l);
            mi.setUserData(newCode);
            
            l.setOnDragDetected(new EventHandler<MouseEvent>()
            {
                public void handle(MouseEvent event)
                {
                    /* drag was detected, start a drag-and-drop gesture */
                    /* allow any transfer mode */

                    Dragboard db = l.startDragAndDrop(TransferMode.COPY);

                    /* Put a string on a dragboard */
                    ClipboardContent content = new ClipboardContent();
                    content.putString(mi.getUserData().toString());
                    db.setContent(content);
                    LegoGUI.getInstance().getLegoGUIController().snomedDragStarted();
                    event.consume();
                }
            });

            l.setOnDragDone(new EventHandler<DragEvent>()
            {
                public void handle(DragEvent event)
                {
                    LegoGUI.getInstance().getLegoGUIController().snomedDragCompleted();
                    menuRecentSctCodes.hide();
                }
            });
            
            items.add(0, mi);
        }
        if (items.size() > 7)
        {
            items.remove(items.size() - 1);
        }
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
    

    public void snomedDragStarted()
    {
        if (editorTabPane.getSelectionModel().getSelectedItem() != null)
        {
            String legoId = ((LegoTab)editorTabPane.getSelectionModel().getSelectedItem()).getDisplayedLegoID();
            for (Node n : snomedCodeDropTargets.get(legoId))
            {
                DropShadow ds = new DropShadow();
                ds.setColor(Color.LIGHTGREEN);
                Effect existing = n.getEffect();
                if (existing != null)
                {
                    existingEffect.put(n, existing);
                }
                n.setEffect(ds);
            }
        }
        for (Node n : snomedCodeDropTargets.get(NONE))
        {
            DropShadow ds = new DropShadow();
            ds.setColor(Color.LIGHTGREEN);
            Effect existing = n.getEffect();
            if (existing != null)
            {
                existingEffect.put(n, existing);
            }
            n.setEffect(ds);
        }
    }

    public void snomedDragCompleted()
    {
        if (editorTabPane.getSelectionModel().getSelectedItem() != null)
        {
            String legoId = ((LegoTab)editorTabPane.getSelectionModel().getSelectedItem()).getDisplayedLegoID();
            for (Node n : snomedCodeDropTargets.get(legoId))
            {
                n.setEffect(existingEffect.remove(n));
            }
        }
        for (Node n : snomedCodeDropTargets.get(NONE))
        {
            n.setEffect(existingEffect.remove(n));
        }
    }
    
    public void showLegoLists()
    {
        showAllLegoListBtn.selectedProperty().set(true);
        showAllLegoListBtn.fireEvent(new ActionEvent());
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
        menuFileImport.setGraphic(Images.LEGO_IMPORT.createImageView());
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
        
        menuFileCreateLego.setGraphic(Images.LEGO_ADD.createImageView());
        menuFileCreateLego.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        menuFileCreateLego.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent t)
            {
                LegoGUI.getInstance().showLegoListPropertiesDialog("", UUID.randomUUID().toString(),
                        new SimpleStringProperty(""));
            }
        });
        
        menuFileExportLegoLists.setGraphic(Images.LEGO_EXPORT_ALL.createImageView());
        menuFileExportLegoLists.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                LegoGUIModel.getInstance().exportAllLegoLists();
            }
        });

        menuFileExit.setGraphic(Images.EXIT.createImageView());
        menuFileExit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        menuFileExit.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent t)
            {
                shutdown();
            }
        });
        
        menuEditPreferences.setGraphic(Images.PREFERENCES.createImageView());
        menuEditPreferences.setOnAction(new EventHandler<ActionEvent>()
        {
            
            @Override
            public void handle(ActionEvent event)
            {
                LegoGUI.getInstance().showUserPreferences();
            }
        });
    }

    public StringProperty getStyleForLego(LegoReference legoReference)
    {
        return displayedLegosStyleInfo.get(legoReference.getUniqueId());
    }
    
    public void addNewLego(String legoListUUID, Lego lego)
    {
        newLegos.addLego(lego, legoListUUID);
    }
    
    public UnsavedLegos getUnsavedLegos()
    {
        return newLegos;
    }
    
    /**
     * This is only for deleting a lego that hasn't yet been committed.
     */
    public void removeNewLego(String legoUniqueId)
    {
        newLegos.removeLego(legoUniqueId);
    }
    
    public void beginLegoEdit(LegoReference legoReference, LegoTreeItem lti)
    {
        if (legoReference != null)
        {
            Lego newLego = BDBDataStoreImpl.getInstance().getLego(legoReference.getLegoUUID(), legoReference.getStampUUID());
            if (newLego == null)
            {
                newLego = newLegos.getLego(legoReference.getUniqueId());
                if (newLego == null)
                {
                    logger.error("Couldn't find a lego that should have existed!");
                    LegoGUI.getInstance().showErrorDialog("Couldn't find Lego", "Couldn't find a Lego which should have existed.", "");
                    return;
                }
            }
            String legoId = ModelUtil.makeUniqueLegoID(newLego);
            if (displayedLegos.containsKey(legoId))
            {
                editorTabPane.getSelectionModel().select(displayedLegos.get(legoId));
            }
            else
            {
                final LegoTab tab = new LegoTab("Lego", newLego);
                tab.hasUnsavedChangesProperty().addListener(new InvalidationListener()
                {
                    @Override
                    public void invalidated(Observable observable)
                    {
                        enableSaveButton.invalidate();
                    }
                });
                displayedLegos.put(legoId, tab);
                
                int hue = random.nextInt(361);
                int saturation = random.nextInt(20) + 20;  //Saturation between 20% and 40%
                int brightness = 90;  //%
                
                displayedLegosStyleInfo.put(legoId, new SimpleStringProperty("-fx-effect: innershadow(two-pass-box , hsb(" 
                        + hue + ", " + saturation + "%," + brightness + "%), 15, 0.0 , 0 , 0);"));
                tab.setStyle(displayedLegosStyleInfo.get(legoId).getValue());
                tab.setOnClosed(new EventHandler<Event>()
                {
                    public void handle(Event arg0) 
                    {
                        LegoGUIController.this.legoEditTabClosed(tab);
                    }
                });
                
                editorTabPane.getTabs().add(tab);
                editorTabPane.getSelectionModel().select(tab);
            }
            
            showTreeItem(lti, legoReference.getUniqueId());
        }
    }
    
    /**
     * @param ti - optional - it will try to find it based on the legoUniqueId if not provided.
     * @param legoUniqueId - optional - only used if ti is not provided
     */
    private void showTreeItem(TreeItem<String> ti, String legoUniqueId)
    {
        if (ti == null)
        {
            ti = LegoGUIModel.getInstance().findTreeItem(legoUniqueId);
        }
        if (ti != null)
        {
            expandParents(ti);
            Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
        }
    }
    
    protected void showLegosForAllOpenTabs()
    {
        for (String legoId : displayedLegos.keySet())
        {
            showTreeItem(null, legoId);
        }
    }
    
    private void expandParents(TreeItem<String> ti)
    {
        TreeItem<String> parent = ti.getParent();
        if (parent != null)
        {
            ti.getParent().setExpanded(true);
            expandParents(parent);
        }
    }
    
    public void legoEditTabClosed(LegoTab tab)
    {
        displayedLegos.remove(tab.getDisplayedLegoID());
        StringProperty style = displayedLegosStyleInfo.remove(tab.getDisplayedLegoID());
        if (style != null)
        {
            style.setValue("-fx-effect: innershadow(two-pass-box , white , 0, 0.0 , 0 , 0);");  //Lego tree node is bound to this - auto update when we clear it.
        }
        snomedCodeDropTargets.remove(tab.getDisplayedLegoID());
    }
    
    public void closeTabIfOpen(LegoReference lr)
    {
        LegoTab lt = displayedLegos.get(lr.getUniqueId());
        if (lt != null)
        {
            editorTabPane.getTabs().remove(lt);
            legoEditTabClosed(lt);
        }
    }
    
    public LegoFilterPaneController getLegoFilterPaneController()
    {
        return lfpc;
    }

    private void shutdown()
    {
        logger.info("shutdown called");
        
        for (LegoTab lt : displayedLegos.values())
        {
            if (lt.hasUnsavedChangesProperty().get())
            {
                Answer answer = LegoGUI.getInstance().showYesNoDialog("Unsaved Changes", "One or more Legos has unsaved changes.  Do you want to close anyway?");
                if (answer == null || answer == Answer.NO)
                {
                    //don't close
                    return;
                }
                else
                {
                    break;
                }
            }
        }
        
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
