package gov.va.legoEdit;

import gov.va.legoEdit.search.PNCS.PncsSearchModel;
import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.guiUtil.ContextMenuListCell;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.WriteException;
import gov.va.ohi.sim.fx.concept.GetConceptService;
import gov.va.ohi.sim.fx.taxonomy.Icons;
import gov.va.ohi.sim.fx.taxonomy.SimTreeCell;
import gov.va.ohi.sim.fx.taxonomy.SimTreeItem;
import gov.va.ohi.sim.fx.taxonomy.TaxonomyProgressIndicatorSkin;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.control.TreeView;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.ihtsdo.fxmodel.FxTaxonomyReferenceWithConcept;
import org.ihtsdo.fxmodel.concept.FxConcept;
import org.ihtsdo.fxmodel.fetchpolicy.RefexPolicy;
import org.ihtsdo.fxmodel.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.fxmodel.fetchpolicy.VersionPolicy;
import org.ihtsdo.tk.binding.Taxonomies;
import org.ihtsdo.tk.rest.client.TtkRestClient;

public class LegoGUIController implements Initializable
{
    private static final DataFormat simDragItem =
        new DataFormat("SimItem");

	Logger logger = LoggerFactory.getLogger(LegoGUIController.class);
    private LegoList currentLegoList = null;
    private MenuItem menuDeleteLego;
    private HashSet<Tab> tabsRenderedSinceSelect = new HashSet<>();
    //Copypaste from gui tool
    //
    private GetConceptService         getConceptService = new GetConceptService();
    
    @FXML //  fx:id="dragTest"
    private TextField dragTest; // Value injected by FXMLLoader
    @FXML //  fx:id="sctTree"
    private TreeView sctTree; // Value injected by FXMLLoader
    @FXML //  fx:id="editTab"
    private Tab editTab; // Value injected by FXMLLoader
    @FXML //  fx:id="editorGridPane"
    private GridPane editorGridPane; // Value injected by FXMLLoader
    @FXML //  fx:id="hideLegoListMenu"
    private MenuItem menuHideLL; // Value injected by FXMLLoader
    @FXML //  fx:id="legoAccordion"
    private Accordion legoAccordion; // Value injected by FXMLLoader
    @FXML //  fx:id="legoGroupDescription"
    private TextField legoGroupDescription; // Value injected by FXMLLoader
    @FXML //  fx:id="legoGroupName"
    private Label legoGroupName; // Value injected by FXMLLoader
    @FXML //  fx:id="legoGroupUUID"
    private Label legoGroupUUID; // Value injected by FXMLLoader
    @FXML //  fx:id="legoList"
    private ListView<String> legoList; // Value injected by FXMLLoader
    @FXML //  fx:id="menu"
    private MenuBar menu; // Value injected by FXMLLoader
    @FXML //  fx:id="menuFile"
    private Menu menuFile; // Value injected by FXMLLoader
    @FXML //  fx:id="menuFileExit"
    private MenuItem menuFileExit; // Value injected by FXMLLoader
    @FXML //  fx:id="menuFileImport"
    private MenuItem menuFileImport; // Value injected by FXMLLoader
    @FXML //  fx:id="splitLeft"
    private AnchorPane splitLeft; // Value injected by FXMLLoader
    @FXML //  fx:id="splitPane"
    private SplitPane splitPane; // Value injected by FXMLLoader
    @FXML //  fx:id="splitRight"
    private AnchorPane splitRight; // Value injected by FXMLLoader
    @FXML //  fx:id="tabPane"
    private TabPane tabPane; // Value injected by FXMLLoader
    @FXML //  fx:id="xmlTab"
    private Tab xmlTab; // Value injected by FXMLLoader
    @FXML //  fx:id="xmlViewer"
    private WebView xmlViewer; // Value injected by FXMLLoader
    @FXML //  fx:id="menuView"
    private Menu menuView; // Value injected by FXMLLoader
    @FXML //  fx:id="menuViewShowALL"
    private CheckMenuItem menuViewShowAllLL; // Value injected by FXMLLoader
    @FXML //  fx:id="menuSearchLegos"
    private Menu menuSearchLegos; // Value injected by FXMLLoader
    @FXML //  fx:id="menuSearchByPNCS"
    private MenuItem menuSearchByPNCS; // Value injected by FXMLLoader
    private FxConcept fxc;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources)
    {
        //Copy paste from gui tool
        assert dragTest != null : "fx:id=\"dragTest\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert editTab != null : "fx:id=\"editTab\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert editorGridPane != null : "fx:id=\"editorGridPane\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert legoGroupDescription != null : "fx:id=\"legoGroupDescription\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert legoGroupName != null : "fx:id=\"legoGroupName\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert legoList != null : "fx:id=\"legoList\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert legoAccordion != null : "fx:id=\"legoTree\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert menu != null : "fx:id=\"menu\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert menuFile != null : "fx:id=\"menuFile\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert menuFileExit != null : "fx:id=\"menuFileExit\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert menuFileImport != null : "fx:id=\"menuFileImport\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert menuSearchLegos != null : "fx:id=\"menuSearchLegos\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert menuSearchByPNCS != null : "fx:id=\"menuSearchByPNCS\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert splitLeft != null : "fx:id=\"splitLeft\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert splitPane != null : "fx:id=\"splitPane\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert splitRight != null : "fx:id=\"splitRight\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert tabPane != null : "fx:id=\"tabPane\" was not injected: check your FXML file 'LegoGUI.fxml'.";
        assert xmlTab != null : "fx:id=\"xmlTab\" was not injected: check your FXML file 'LegoGUI.fxml'.";

        // initialize your logic here: all @FXML variables will have been injected
        legoList.setItems(LegoGUIModel.getInstance().getLegoListNames());
        xmlViewer.setContextMenuEnabled(false);

        setupMenus();
        
        legoList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue)
            {
                changeSelectedLego(newValue);
            }
        });

        LegoGUI.getMainStage().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent event)
            {
                BDBDataStoreImpl.getInstance().shutdown();
            }
        });
    
        legoList.setCellFactory(ContextMenuListCell.<String>forListView(new ContextMenu(menuDeleteLego)));

        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>()
        {
            @Override
            public void changed(ObservableValue<? extends Tab> tab, Tab oldTab, Tab newTab)
            {
                if (!tabsRenderedSinceSelect.contains(newTab))
                {
                    updateEditorTab();
                    updateXMLTab();
                }
            }
        });

    
    
      sctTree.setCellFactory(new Callback<TreeView<FxTaxonomyReferenceWithConcept>,
              TreeCell<FxTaxonomyReferenceWithConcept>>() {
         @Override
         public TreeCell<FxTaxonomyReferenceWithConcept> call(TreeView<FxTaxonomyReferenceWithConcept> p) {
            return new SimTreeCell();
         }
      });
      
      sctTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
      sctTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
         @Override
         public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            if (newValue instanceof SimTreeItem) {
               SimTreeItem simTreeItem = (SimTreeItem) newValue;
//
//               getConceptService.setConceptUuid(simTreeItem.getValue().getConcept().getPrimordialUuid());
//               getConceptService.restart();
            }
         }
      });
      sctTree.setOnDragDetected(new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {
            /* drag was detected, start a drag-and-drop gesture*/
            /* allow any transfer mode */
            Dragboard db = sctTree.startDragAndDrop(TransferMode.ANY);

            /* Put a string on a dragboard */
            SimTreeItem dragItem = (SimTreeItem)sctTree.getSelectionModel().getSelectedItem();
    
            ClipboardContent content = new ClipboardContent();
            content.putString(dragItem.getValue().getConcept().getPrimordialUuid().toString());
            db.setContent(content);

            event.consume();
        }
    });
      
      dragTest.setOnDragOver(new EventHandler<DragEvent>() {
        public void handle(DragEvent event) {
            /* data is dragged over the target */
            /* accept it only if it is not dragged from the same node 
             * and if it has a string data */
            if (event.getGestureSource() != dragTest &&
                    event.getDragboard().hasString()) {
                /* allow for both copying and moving, whatever user chooses */
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            event.consume();
        }
    });

      dragTest.setOnDragEntered(new EventHandler<DragEvent>() {
        public void handle(DragEvent event) {
        /* the drag-and-drop gesture entered the target */
        /* show to the user that it is an actual gesture target */
             if (event.getGestureSource() != dragTest &&
                     event.getDragboard().hasString()) {
                  DropShadow ds = new DropShadow();
                 ds.setColor(Color.GREEN);

                 dragTest.setEffect(ds);
             }

             event.consume();
        }
    });

      dragTest.setOnDragExited(new EventHandler<DragEvent>() {
        public void handle(DragEvent event) {
            /* mouse moved away, remove the graphical cues */
                  DropShadow ds = new DropShadow();
                 ds.setColor(Color.WHITE);

                 dragTest.setEffect(ds);

            event.consume();
        }
    });

      dragTest.setOnDragDropped(new EventHandler<DragEvent>() {
        public void handle(DragEvent event) {
            /* data dropped */
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                UUID conId = UUID.fromString(db.getString());
                FxConcept con;
                try {
                    con = TtkRestClient.getRestClient().getFxConcept(conId,
                              UUID.fromString("d0a05080-b5de-11e1-afa6-0800200c9a66"));
//                    con = TtkRestClient.getRestClient().getFxConcept(Taxonomies.SNOMED.getUuids()[0],
//                        conId, VersionPolicy.ACTIVE_VERSIONS,
//                        RefexPolicy.REFEX_MEMBERS,
//                        RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
                    dragTest.setText(con.toString());
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(LegoGUIController.class.getName()).log(Level.SEVERE, null, ex);
                }
               success = true;
            }
            /* let the source know whether the string was successfully 
             * transferred and used */
            event.setDropCompleted(success);

            event.consume();
         }
    });


      sctTree.setShowRoot(true);

      FxTaxonomyReferenceWithConcept root       = new FxTaxonomyReferenceWithConcept();
      SimTreeItem                    rootItem   = new SimTreeItem(root);
      FxTaxonomyReferenceWithConcept snomedRoot = new FxTaxonomyReferenceWithConcept();
        try {
            fxc = TtkRestClient.getRestClient().getFxConcept(Taxonomies.SNOMED.getUuids()[0],
                    UUID.fromString("d0a05080-b5de-11e1-afa6-0800200c9a66"));
            fxc = TtkRestClient.getRestClient().getFxConcept(Taxonomies.SNOMED.getUuids()[0],
                    UUID.fromString("d0a05080-b5de-11e1-afa6-0800200c9a66"), VersionPolicy.ACTIVE_VERSIONS,
                    RefexPolicy.REFEX_MEMBERS,
                    RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(LegoGUIController.class.getName()).log(Level.SEVERE, null, ex);
        }

      snomedRoot.setConcept(fxc);

      SimTreeItem item = new SimTreeItem(snomedRoot, Icons.ROOT.getImageView());

      rootItem.getChildren().add(item);

      // item.computeGraphic();
      item.addChildren();

      // put this event handler on the root
      item.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler() {
         @Override
         public void handle(Event t) {

            // remove grandchildren
            SimTreeItem sourceTreeItem = (SimTreeItem) t.getSource();

            sourceTreeItem.removeGrandchildren();
         }
      });
      item.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler() {
         @Override
         public void handle(Event t) {

            // add grandchildren
            SimTreeItem       sourceTreeItem = (SimTreeItem) t.getSource();
            ProgressIndicator p2             = new ProgressIndicator();

            p2.setSkin(new TaxonomyProgressIndicatorSkin(p2));
            p2.setPrefSize(16, 16);
            p2.setProgress(-1);
            sourceTreeItem.setProgressIndicator(p2);
            sourceTreeItem.addChildrenConceptsAndGrandchildrenItems(p2);
         }
      });
      sctTree.setRoot(rootItem);

    
    }
    
    private void setupMenus()
    {
        menuFileImport.setGraphic(new ImageView(new Image(LegoGUI.class.getResourceAsStream("/fugue/16x16/icons/folder-open-table.png"))));
        menuFileImport.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
        menuFileImport.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent t)
            {
                FileChooser fc = new FileChooser();
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("LEGO xml Files (*.xml)", "*.xml"));
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*"));
                List<File> files = fc.showOpenMultipleDialog(LegoGUI.getMainStage());
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
                                errors.append((ex.getLocalizedMessage() == null ? ex.toString() : ex.getLocalizedMessage()));
                                errors.append(System.getProperty("line.separator"));
                                errors.append(System.getProperty("line.separator"));
                            }
                        }
                    }
                    if (errors.length() > 0)
                    {
                        LegoGUI.showErrorDialog("Error Loading LEGOs", "There was an error loading the specified files", errors.toString());
                    }
                }
            }
        });

        menuFileExit.setGraphic(new ImageView(new Image(LegoGUI.class.getResourceAsStream("/fugue/16x16/icons/cross.png"))));
        menuFileExit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        menuFileExit.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent t)
            {
                BDBDataStoreImpl.getInstance().shutdown();
                System.exit(0);
            }
        });
        
        menuViewShowAllLL.setSelected(true);
        //TODO Note this doesn't work:  http://javafx-jira.kenai.com/browse/RT-21192
        //menuViewShowAllLL.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
        menuViewShowAllLL.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                if (PncsSearchModel.getInstance().isDisplaying()) {
                    try {
                        LegoGUIModel.getInstance().replaceLegoList(PncsSearchModel.getInstance().getImportedLegos());
                    } catch (WriteException ex) {
                    	logger.error("Unexpected error replacing the lego list", ex);
                    }
                    PncsSearchModel.getInstance().setDisplaying(false);
                }
                if (menuViewShowAllLL.isSelected())
                {
                    if (!splitPane.getItems().contains(splitLeft))
                    {
                        splitPane.getItems().add(0, splitLeft);
                    }
                    
                }
                else
                {
                    splitPane.getItems().remove(splitLeft);
                }
            }
        });
        
        menuSearchByPNCS.setGraphic(new ImageView(new Image(LegoGUI.class.getResourceAsStream("/fugue/16x16/icons/cross.png"))));
        menuSearchByPNCS.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        menuSearchByPNCS.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent t)
            {                    
                LegoGUI.showPNCSSearchDialog(PncsSearchModel.getInstance().getPncsIdComboList());
                
                if (PncsSearchModel.getInstance().isDisplaying()) {
                    menuViewShowAllLL.setSelected(false);
                }
            }
        });

        //Floating Context menus
        
        menuDeleteLego = new MenuItem("Delete Lego");
        menuDeleteLego.setDisable(true);
        menuDeleteLego.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                try
                {
                    LegoGUIModel.getInstance().removeLegoList(currentLegoList.getGroupName());
                    changeSelectedLego(null);
                    legoList.getSelectionModel().clearSelection();
                }
                catch (WriteException ex)
                {
                    logger.error("Unexpeted error removing lego", ex);
                    LegoGUI.showErrorDialog("Error Removing Lego", "There was an removing the specified file", ex.getLocalizedMessage());
                }
            }
        });
        
        //TODO note: this doesn't work either http://javafx-jira.kenai.com/browse/RT-24518
        //menuHideLL.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
        menuHideLL.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                menuViewShowAllLL.setSelected(false);
                splitPane.getItems().remove(splitLeft);
            }
        });
    }

    private void changeSelectedLego(String newLego)
    {
        tabsRenderedSinceSelect.clear();
        if (newLego == null)
        {
            currentLegoList = null;
            menuDeleteLego.setDisable(true);
            menuDeleteLego.setText("Delete Lego");
        }
        else
        {
            if (newLego.contains("PNCS:") && newLego.contains("Val:")) {
                Lego l = LegoGUIModel.getInstance().getLego(newLego);
                currentLegoList = new LegoList();
                currentLegoList.getLego().add(l);
            } else {
                currentLegoList = LegoGUIModel.getInstance().getLegoList(newLego);
            }
            
            if (currentLegoList == null)
            {
                LegoGUI.showErrorDialog("Error Reading LegoList", "Unexpected error reading LegoList from storage", "The LegoList for '" + newLego + "' could not be found");
                legoList.getSelectionModel().clearSelection();
            }
            else
            {
                menuDeleteLego.setText("Delete Lego '" + newLego + "'");
                menuDeleteLego.setDisable(false);
            }
        }
        updateXMLTab();
        updateEditorTab();
    }

    private TitledPane buildTitledPane(Lego l)
    {
        TitledPane tp = new TitledPane();

        tp.setText(l.getPncs().getName() + " : " + l.getPncs().getValue());


        GridPane gp = new GridPane();
        int rowIndex = 0;

        gp.add(new Label("LEGO UUID: " + l.getLegoUUID()), 0, rowIndex++);

        for (Assertion a : l.getAssertion())
        {
            TextArea ta = new TextArea();
            ta.setWrapText(true);
            ta.setEditable(false);
            try
            {
                ta.setText(LegoXMLUtils.toXML(a).trim());
            }
            catch (Exception e)
            {
                ta.setText("Unexpected error displaying assertion: " + e.getLocalizedMessage());
            }
            ScrollPane sp = new ScrollPane();
            sp.setContent(ta);
            sp.setFitToHeight(true);
            sp.setFitToWidth(true);
            sp.setPrefHeight(250d);
            gp.add(sp, 0, rowIndex++);
        }

        ColumnConstraints cc = new ColumnConstraints();
        cc.setFillWidth(true);
        cc.setHgrow(Priority.ALWAYS);

        gp.getColumnConstraints().add(cc);

        tp.setContent(gp);
        return tp;
    }

    private void updateEditorTab()
    {
        if (tabPane.getSelectionModel().getSelectedItem() == editTab && !tabsRenderedSinceSelect.contains(editTab))
        {
            if (currentLegoList == null)
            {
                legoGroupName.setText("");
                legoGroupDescription.setText("");
                legoGroupUUID.setText("");
                legoAccordion.getPanes().clear();
            }
            else
            {

                legoGroupName.setText(currentLegoList.getGroupName());
                legoGroupDescription.setText(currentLegoList.getGroupDescription());
                legoGroupUUID.setText(currentLegoList.getLegoListUUID());
                legoAccordion.getPanes().clear();
                for (Lego l : currentLegoList.getLego())
                {
                    legoAccordion.getPanes().add(buildTitledPane(l));
                }
            }
            tabsRenderedSinceSelect.add(editTab);
        }
    }

    private void updateXMLTab()
    {
        //Can't use xmlTab.isSelected because it lags the change listener...
        if (tabPane.getSelectionModel().getSelectedItem() == xmlTab && !tabsRenderedSinceSelect.contains(xmlTab))
        {
            if (currentLegoList == null)
            {
                xmlViewer.getEngine().loadContent("");
            }
            else
            {
                try
                {
                    //todo render on click
                    xmlViewer.getEngine().loadContent(LegoXMLUtils.toHTML(currentLegoList));
                }
                catch (Exception e)
                {
                    logger.error("There was an error formatting the lego as XML", e);
                    xmlViewer.getEngine().loadContent("There was an error formatting the lego as XML");
                }
            }
            tabsRenderedSinceSelect.add(xmlTab);
        }
    }
}
