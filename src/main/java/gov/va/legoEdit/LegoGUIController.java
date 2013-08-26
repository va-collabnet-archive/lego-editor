package gov.va.legoEdit;

import gov.va.legoEdit.formats.LegoValidateCallback;
import gov.va.legoEdit.gui.dialogs.YesNoDialogController.Answer;
import gov.va.legoEdit.gui.legoFilterPane.LegoFilterPaneController;
import gov.va.legoEdit.gui.legoTreeView.ComboBoxConcept;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.pendingConcepts.PendingConceptsPaneController;
import gov.va.legoEdit.gui.sctSearch.SnomedSearchPaneController;
import gov.va.legoEdit.gui.sctTreeView.SimTreeView;
import gov.va.legoEdit.gui.templates.TemplatesPaneController;
import gov.va.legoEdit.gui.util.DropTargetLabel;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.gui.util.LegoTab;
import gov.va.legoEdit.gui.util.Utility;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.ModelUtil;
import gov.va.legoEdit.model.PendingConcepts;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.CommonlyUsedConcepts;
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.geometry.Insets;
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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.Effect;
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
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
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
	private TemplatesPaneController tpc;
	private PendingConceptsPaneController pcpc;
	private Thread dbConnectThread;
	private Random random = new Random();
	private CommonlyUsedConcepts cut;
	private File importInitialDirectory = null;
	private Tooltip legoInvalidReason = new Tooltip();
	private ProgressIndicator legoValidationInProgress = new ProgressIndicator();
	private volatile long dragStartedAt = 0;
	private double dividerPosition = 0.3;
	private AnchorPane[] splitPaneOrder;
	private BooleanProperty findConceptInProgress = new SimpleBooleanProperty(false);
	
	private LegoTabInvalidationListener legoTabInvalidationListener = new LegoTabInvalidationListener();
	private BooleanBinding enableSaveButton, enableUndoButton, enableRedoButton;

	private static String NONE = "NONE";

	// Copypaste from gui tool
	@FXML// fx:id="buttonRedo"
	private Button buttonRedo; // Value injected by FXMLLoader
	@FXML// fx:id="buttonSaveLego"
	private Button buttonSaveLego; // Value injected by FXMLLoader
	@FXML// fx:id="buttonUndo"
	private Button buttonUndo; // Value injected by FXMLLoader
	@FXML// fx:id="editorTabPane"
	private TabPane editorTabPane; // Value injected by FXMLLoader
	@FXML// fx:id="legoInvalidImageView"
	private ImageView legoInvalidImageView; // Value injected by FXMLLoader
	@FXML// fx:id="legoInvalidStack"
	private StackPane legoInvalidStack; // Value injected by FXMLLoader
	@FXML// fx:id="legoListsPanel"
	private AnchorPane legoListsPanel; // Value injected by FXMLLoader
	@FXML// fx:id="mainSplitPane"
	private SplitPane mainSplitPane; // Value injected by FXMLLoader
	@FXML// fx:id="menu"
	private MenuBar menu; // Value injected by FXMLLoader
	@FXML// fx:id="menuEdit"
	private Menu menuEdit; // Value injected by FXMLLoader
	@FXML// fx:id="menuEditAddPending"
	private MenuItem menuEditAddPending; // Value injected by FXMLLoader
	@FXML// fx:id="menuEditPreferences"
	private MenuItem menuEditPreferences; // Value injected by FXMLLoader
	@FXML// fx:id="menuEditCemTool"
	private MenuItem menuEditCemTool; // Value injected by FXMLLoader
	@FXML// fx:id="menuFile"
	private Menu menuFile; // Value injected by FXMLLoader
	@FXML// fx:id="menuFileCreateLego"
	private MenuItem menuFileCreateLego; // Value injected by FXMLLoader
	@FXML// fx:id="menuFileCreateLego"
	private MenuItem menuFileCreateLegosFromFile; // Value injected by FXMLLoader
	@FXML// fx:id="menuFileExit"
	private MenuItem menuFileExit; // Value injected by FXMLLoader
	@FXML// fx:id="menuFileExportLegoLists"
	private MenuItem menuFileExportLegoLists; // Value injected by FXMLLoader
	@FXML// fx:id="menuFileImport"
	private MenuItem menuFileImport; // Value injected by FXMLLoader
	@FXML// fx:id="menuHelp"
	private Menu menuHelp; // Value injected by FXMLLoader
	@FXML// fx:id="menuHelpAbout"
	private MenuItem menuHelpAbout; // Value injected by FXMLLoader
	@FXML// fx:id="menuRecentSctCodes"
	private MenuButton menuRecentSctCodes; // Value injected by FXMLLoader
	@FXML// fx:id="pendingConceptsPanel"
	private AnchorPane pendingConceptsPanel; // Value injected by FXMLLoader
	@FXML// fx:id="rootPane"
	private AnchorPane rootPane; // Value injected by FXMLLoader
	@FXML// fx:id="showAllLegoListBtn"
	private ToggleButton showAllLegoListBtn; // Value injected by FXMLLoader
	@FXML// fx:id="showPendingBtn"
	private ToggleButton showPendingBtn; // Value injected by FXMLLoader
	@FXML// fx:id="showSnomedBtn"
	private ToggleButton showSnomedBtn; // Value injected by FXMLLoader
	@FXML// fx:id="showSnomedSearchBtn"
	private ToggleButton showSnomedSearchBtn; // Value injected by FXMLLoader
	@FXML// fx:id="showTemplatesBtn"
	private ToggleButton showTemplatesBtn; // Value injected by FXMLLoader
	@FXML// fx:id="snomedBrowserPanel"
	private BorderPane snomedBrowserPanel; // Value injected by FXMLLoader
	@FXML// fx:id="snomedSearchPanel"
	private AnchorPane snomedSearchPanel; // Value injected by FXMLLoader
	@FXML// fx:id="splitLeft"
	private SplitPane splitLeft; // Value injected by FXMLLoader
	@FXML// fx:id="splitPanelLegoLists"
	private AnchorPane splitPanelLegoLists; // Value injected by FXMLLoader
	@FXML// fx:id="splitPanelLegoSearch"
	private AnchorPane splitPanelSnomedSearch; // Value injected by FXMLLoader
	@FXML// fx:id="splitPanelPendingConcepts"
	private AnchorPane splitPanelPendingConcepts; // Value injected by FXMLLoader
	@FXML// fx:id="splitPanelSnomedSearch"
	private AnchorPane splitPanelSnomedBrowser; // Value injected by FXMLLoader
	@FXML// fx:id="splitPanelTemplates"
	private AnchorPane splitPanelTemplates; // Value injected by FXMLLoader
	@FXML// fx:id="splitRight"
	private AnchorPane splitRight; // Value injected by FXMLLoader
	@FXML// fx:id="templatesPanel"
	private AnchorPane templatesPanel; // Value injected by FXMLLoader
	@FXML// fx:id="pendingConceptsHeader"
	private AnchorPane pendingConceptsHeader; // Value injected by FXMLLoader
	@FXML// fx:id="snomedSearchHeader"
	private AnchorPane snomedSearchHeader; // Value injected by FXMLLoader
	@FXML// fx:id="templatesHeader"
	private AnchorPane templatesHeader; // Value injected by FXMLLoader
	@FXML// fx:id="legoListsHeader"
	private AnchorPane legoListsHeader; // Value injected by FXMLLoader
	@FXML// fx:id="snomedBrowserHeader"
	private AnchorPane snomedBrowserHeader; // Value injected by FXMLLoader
	@FXML //  fx:id="findConceptPI"
	private ProgressIndicator findConceptPI; // Value injected by FXMLLoader

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		// Copy paste from gui tool
		assert buttonRedo != null : "fx:id=\"buttonRedo\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert buttonSaveLego != null : "fx:id=\"buttonSaveLego\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert buttonUndo != null : "fx:id=\"buttonUndo\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert editorTabPane != null : "fx:id=\"editorTabPane\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert legoInvalidImageView != null : "fx:id=\"legoInvalidImageView\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert legoInvalidStack != null : "fx:id=\"legoInvalidStack\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert legoListsPanel != null : "fx:id=\"legoListsPanel\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert mainSplitPane != null : "fx:id=\"mainSplitPane\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert menu != null : "fx:id=\"menu\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert menuEdit != null : "fx:id=\"menuEdit\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert menuEditAddPending != null : "fx:id=\"menuEditAddPending\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert menuEditPreferences != null : "fx:id=\"menuEditPreferences\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert menuFile != null : "fx:id=\"menuFile\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert menuFileCreateLego != null : "fx:id=\"menuFileCreateLego\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert menuFileCreateLegosFromFile != null : "fx:id=\"menuFileCreateLegosFromFile\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert menuFileExit != null : "fx:id=\"menuFileExit\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert menuFileExportLegoLists != null : "fx:id=\"menuFileExportLegoLists\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert menuFileImport != null : "fx:id=\"menuFileImport\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert menuHelp != null : "fx:id=\"menuHelp\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert menuHelpAbout != null : "fx:id=\"menuHelpAbout\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert menuRecentSctCodes != null : "fx:id=\"menuRecentSctCodes\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert pendingConceptsPanel != null : "fx:id=\"pendingConceptsPanel\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert showAllLegoListBtn != null : "fx:id=\"showAllLegoListBtn\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert showPendingBtn != null : "fx:id=\"showPendingBtn\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert showSnomedBtn != null : "fx:id=\"showSnomedBtn\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert showSnomedSearchBtn != null : "fx:id=\"showSnomedSearchBtn\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert showTemplatesBtn != null : "fx:id=\"showTemplatesBtn\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert snomedBrowserPanel != null : "fx:id=\"snomedBrowserPanel\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert snomedSearchPanel != null : "fx:id=\"snomedSearchPanel\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert splitLeft != null : "fx:id=\"splitLeft\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert splitPanelLegoLists != null : "fx:id=\"splitPanelLegoLists\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert splitPanelSnomedSearch != null : "fx:id=\"splitPanelLegoSearch\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert splitPanelPendingConcepts != null : "fx:id=\"splitPanelPendingConcepts\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert splitPanelSnomedBrowser != null : "fx:id=\"splitPanelSnomedSearch\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert splitPanelTemplates != null : "fx:id=\"splitPanelTemplates\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert splitRight != null : "fx:id=\"splitRight\" was not injected: check your FXML file 'LegoGUI.fxml'.";
		assert templatesPanel != null : "fx:id=\"templatesPanel\" was not injected: check your FXML file 'LegoGUI.fxml'.";

		// initialize your logic here: all @FXML variables will have been injected
	}

	/**
	 * Some of our init code needs the scene, etc, to be set up.
	 */
	protected void finishInit()
	{
		lfpc = LegoFilterPaneController.init();
		legoListsPanel.getChildren().add(lfpc.getBorderPane());

		sspc = SnomedSearchPaneController.init();
		snomedSearchPanel.getChildren().add(sspc.getBorderPane());

		tpc = TemplatesPaneController.init();
		templatesPanel.getChildren().add(tpc.getBorderPane());
		
		pcpc = PendingConceptsPaneController.init();
		pendingConceptsPanel.getChildren().add(pcpc.getBorderPane());
		
		legoInvalidImageView.setVisible(false);
		Tooltip.install(legoInvalidImageView, legoInvalidReason);
		
		legoValidationInProgress = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		legoValidationInProgress.setVisible(false);
		legoValidationInProgress.setPrefHeight(16.0);
		legoValidationInProgress.setPrefWidth(16.0);
		legoValidationInProgress.setMaxWidth(16.0);
		legoValidationInProgress.setMaxHeight(16.0);
		Tooltip.install(legoValidationInProgress, new Tooltip("Schema Validation in progress"));
		
		legoInvalidStack.getChildren().add(legoValidationInProgress);
		legoInvalidStack.setMaxHeight(20.0);
		legoInvalidStack.setMaxWidth(20.0);
		
		findConceptPI.visibleProperty().bind(findConceptInProgress);

		setupMenus();
		setupSctTree(); // This kicks off a thread that opens the DB connection
		cut = new CommonlyUsedConcepts();

		rootPane.getScene().getWindow().setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				// Note - this is broke with javafx < 2.2.6 (which isn't released yet - currently beta - with the 1.7.0_12 early access release)
				//double note - it seems broken again in 2.2.7.  Sigh.  Supposedly only impacts linux, so, no herorics are being done here.
				// http://javafx-jira.kenai.com/browse/RT-25528
				shutdown();
				event.consume();
			}
		});

		// huh, the FXGui editor is broken - I have to set this policy manually.
		editorTabPane.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		editorTabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>()
		{
			@Override
			public void changed(ObservableValue<? extends Tab> tab, Tab oldTab, Tab newTab)
			{
				if (newTab != null)
				{
					showTreeItem(null, ((LegoTab) newTab).getDisplayedLegoID());
				}
				enableSaveButton.invalidate();
				enableUndoButton.invalidate();
				enableRedoButton.invalidate();
				legoTabInvalidationListener.schemaValidate((LegoTab) newTab);
			}
		});
		
		//The fxml file puts all 5 views into the split pane.  Remove all but the lego list for starters.
		splitLeft.getItems().remove(splitPanelSnomedBrowser);
		splitLeft.getItems().remove(splitPanelSnomedSearch);
		splitLeft.getItems().remove(splitPanelTemplates);
		splitLeft.getItems().remove(splitPanelPendingConcepts);
		
		splitPaneOrder = new AnchorPane[] {splitPanelLegoLists, splitPanelSnomedBrowser, splitPanelSnomedSearch, splitPanelTemplates, splitPanelPendingConcepts};
		
		createPanelCloseButton(legoListsHeader, showAllLegoListBtn, splitPanelLegoLists);
		createPanelCloseButton(snomedBrowserHeader, showSnomedBtn, splitPanelSnomedBrowser);
		createPanelCloseButton(snomedSearchHeader, showSnomedSearchBtn, splitPanelSnomedSearch);
		createPanelCloseButton(templatesHeader, showTemplatesBtn, splitPanelTemplates);
		createPanelCloseButton(pendingConceptsHeader, showPendingBtn, splitPanelPendingConcepts);

		showAllLegoListBtn.setGraphic(Images.LEGO_LIST_VIEW.createImageView());
		showAllLegoListBtn.setTooltip(new Tooltip("Show all Lego Lists in the system"));
		showAllLegoListBtn.setSelected(true);
		showAllLegoListBtn.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				updatePanels(showAllLegoListBtn, splitPanelLegoLists);
			}
		});

		showSnomedBtn.setGraphic(Images.ROOT.createImageView());
		showSnomedBtn.setTooltip(new Tooltip("Show the Snomed Viewer"));
		showSnomedBtn.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				updatePanels(showSnomedBtn, splitPanelSnomedBrowser);
			}
		});

		showSnomedSearchBtn.setGraphic(Images.LEGO_SEARCH.createImageView());
		showSnomedSearchBtn.setTooltip(new Tooltip("Show the Snomed Search Panel"));
		showSnomedSearchBtn.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				updatePanels(showSnomedSearchBtn, splitPanelSnomedSearch);
			}
		});

		showTemplatesBtn.setGraphic(Images.TEMPLATE.createImageView());
		showTemplatesBtn.setTooltip(new Tooltip("Show the Templates List"));
		showTemplatesBtn.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				updatePanels(showTemplatesBtn, splitPanelTemplates);
			}
		});
		
		showPendingBtn.setGraphic(Images.CONCEPT_VIEW.createImageView());
		showPendingBtn.setTooltip(new Tooltip("Show the Pending Concepts List"));
		showPendingBtn.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				updatePanels(showPendingBtn, splitPanelPendingConcepts);
			}
		});
		
		enableUndoButton = new BooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				Tab t = editorTabPane.getSelectionModel().getSelectedItem();
				if (t != null)
				{
					return ((LegoTab) t).canUndo().get();
				}
				return false;
			}
		};
		
		buttonUndo.setGraphic(Images.UNDO.createImageView());
		buttonUndo.disableProperty().bind(enableUndoButton.not());
		buttonUndo.setOnAction(new EventHandler<ActionEvent>()
		{
			
			@Override
			public void handle(ActionEvent event)
			{
				((LegoTab) editorTabPane.getSelectionModel().getSelectedItem()).undo();
			}
		});

		enableRedoButton = new BooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				Tab t = editorTabPane.getSelectionModel().getSelectedItem();
				if (t != null)
				{
					return ((LegoTab) t).canRedo().get();
				}
				return false;
			}
		};
		
		buttonRedo.setGraphic(Images.REDO.createImageView());
		buttonRedo.disableProperty().bind(enableRedoButton.not());
		buttonRedo.setOnAction(new EventHandler<ActionEvent>()
		{
			
			@Override
			public void handle(ActionEvent event)
			{
				((LegoTab) editorTabPane.getSelectionModel().getSelectedItem()).redo();
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
					return ((LegoTab) t).hasUnsavedChangesProperty().get();
				}
				return false;
			}
		};
		
		buttonSaveLego.setGraphic(Images.SAVE.createImageView());
		buttonSaveLego.disableProperty().bind(enableSaveButton.not());
		buttonSaveLego.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				LegoTab lt = (LegoTab) editorTabPane.getSelectionModel().getSelectedItem();
				Lego lego = lt.getLego();
				String oldId = ModelUtil.makeUniqueLegoID(lego);
				displayedLegos.remove(oldId);
				StringProperty style = displayedLegosStyleInfo.remove(oldId);
				ArrayList<Node> dropTargets = snomedCodeDropTargets.get(oldId);

				String legoListUUIDtoUse = null;
				for (String legoListId : BDBDataStoreImpl.getInstance().getLegoListByLego(lego.getLegoUUID()))
				{
					// We may get more than one legoList - need to look through each of them - find the one that has the matching stamp.
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

				boolean wasNew = false;
				if (legoListUUIDtoUse == null)
				{
					// try to get it from the newLegos list
					legoListUUIDtoUse = getUnsavedLegos().getLegoListIdForLego(oldId);
					wasNew = true;
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
					removeNewLego(oldId);
					
					displayedLegos.put(newId, lt);
					displayedLegosStyleInfo.put(newId, style);
					snomedCodeDropTargets.put(newId, dropTargets);
					getLegoFilterPaneController().updateLegoList();
					lt.hasUnsavedChangesProperty().invalidate();
					lt.updateForSave(updatedStamp);
					cut.legoCommitted(lego);
					
					if (wasNew)
					{
						//If it was a new lego, we will need to refresh the PNCS filter options.
						LegoGUI.getInstance().getLegoGUIController().getLegoFilterPaneController().reloadOptions();
					}
				}
				catch (DataStoreException | WriteException e)
				{
					logger.error("Unexpected error saving Lego", e);
					LegoGUI.getInstance().showErrorDialog("Error Saving Changes", "Unexpected error storing lego", e.toString());
				}
			}
		});
	}
	
	private void updatePanels(ToggleButton button, AnchorPane splitPanePanel)
	{
		if (button.isSelected())
		{
			if (!splitLeft.getItems().contains(splitPanePanel))
			{
				//always put the in the same order as the buttons
				int insertPosition = 0;
				for (int i = 0; i < splitPaneOrder.length; i++)
				{
					if (splitPaneOrder[i] == splitPanePanel)
					{
						insertPosition = i;
						break;
					}
				}
				for (int i = insertPosition - 1; i >= 0; i--)
				{
					if (!splitLeft.getItems().contains(splitPaneOrder[i]))
					{
						insertPosition--;
					}
				}
				
				splitLeft.getItems().add(insertPosition, splitPanePanel);
			}
			if (mainSplitPane.getItems().size() == 1)
			{
				mainSplitPane.getItems().add(0, splitLeft);
				mainSplitPane.setDividerPosition(0, dividerPosition);
			}
		}
		else
		{
			if (splitLeft.getItems().contains(splitPanePanel))
			{
				splitLeft.getItems().remove(splitPanePanel);
			}
			if (splitLeft.getItems().size() == 0)
			{
				dividerPosition = mainSplitPane.getDividerPositions()[0];
				mainSplitPane.getItems().remove(splitLeft);
			}
		}
		if (splitLeft.getItems().size() > 1)
		{
			double size = 1.0 / (double)splitLeft.getItems().size();
			for (int i = 0; i < splitLeft.getItems().size() - 1; i++)
			{
				splitLeft.setDividerPosition(i, (size  * (double)(i + 1)));
			}
		}
	}
	
	private void createPanelCloseButton(AnchorPane headerPanel, final ToggleButton button, final AnchorPane splitPanePanel)
	{
		StackPane closeBtn = new StackPane();
		closeBtn.getStyleClass().setAll(new String[] { "tab-close-button" });
		closeBtn.setStyle("-fx-cursor:hand;");
		closeBtn.setPadding(new Insets(10, 10, 10, 10));
		AnchorPane.setTopAnchor(closeBtn, 0.0);
		AnchorPane.setRightAnchor(closeBtn, 5.0);
		headerPanel.getChildren().add(closeBtn);

		closeBtn.setOnMouseReleased(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent paramT)
			{
				button.setSelected(false);
				updatePanels(button, splitPanePanel);
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
		addSnomedDropTargetInternal(lego, (Node) label);

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
						ContextMenu cm = label.getDropContextMenu();
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
					LegoGUI.getInstance().showErrorDialog("Unexpected Error", "There was an unexpected error dropping the snomed concept", ex.toString());
				}
				/*
				 * let the source know whether the string was successfully transferred and used
				 */
				event.setDropCompleted(success);
				event.consume();
			}
		});

	}

	public void addSnomedDropTarget(Lego lego, final ComboBox<ComboBoxConcept> n)
	{
		addSnomedDropTargetInternal(lego, ((Node) n));

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
						n.setValue(new ComboBoxConcept(db.getString(), db.getString()));
						success = true;
						// It will have updated its effect upon the set - we don't want to restore an old one.
						existingEffect.remove(n);
					}
				}
				catch (Exception ex)
				{
					logger.error("Error dropping snomed concept", ex);
					LegoGUI.getInstance().showErrorDialog("Unexpected Error", "There was an unexpected error dropping the snomed concept", ex.toString());
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
		addSnomedDropTargetInternal(null, ((Node) n));

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
						success = true;
					}
				}
				catch (Exception ex)
				{
					logger.error("Error dropping snomed concept", ex);
					LegoGUI.getInstance().showErrorDialog("Unexpected Error", "There was an unexpected error dropping the snomed concept", ex.toString());
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
		Concept c = WBUtility.lookupSnomedIdentifier(newCode);
		if (c != null)
		{
			updateRecentCodes(c);
		}
	}

	public void updateRecentCodes(Concept concept)
	{
		ObservableList<MenuItem> items = menuRecentSctCodes.getItems();
		MenuItem temp = null;
		for (MenuItem mi : items)
		{
			if (mi.getUserData().equals(concept.getUuid()))
			{
				temp = mi;
				break;
			}
		}
		if (temp != null)
		{
			// Move it to the top
			items.remove(temp);
			items.add(0, temp);
		}
		else
		{
			final Label l = new Label(concept.getDesc());
			final MenuItem mi = new MenuItem(null, l);
			mi.setUserData(concept.getUuid());

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
				 * data is dragged over the target accept it only if it is not dragged from the same node and if it has a string data
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
				n.setEffect(Utility.greenDropShadow);
				event.consume();
			}
		});

		n.setOnDragExited(new EventHandler<DragEvent>()
		{
			public void handle(DragEvent event)
			{
				/* mouse moved away, remove the graphical cues */
				n.setEffect(Utility.lightGreenDropShadow);
				event.consume();
			}
		});
	}

	public void snomedDragStarted()
	{
		//There is a bug in javafx with comboboxs - it seems to fire dragStarted events twice.
		//http://javafx-jira.kenai.com/browse/RT-28778
		if ((System.currentTimeMillis() - dragStartedAt) < 2000)
		{
			logger.debug("Ignoring duplicate drag event");
			return;
		}
		if (dragStartedAt > 0)
		{
			logger.warn("Unclosed drag event is still active while another was started!");
		}
		dragStartedAt = System.currentTimeMillis();
		if (editorTabPane.getSelectionModel().getSelectedItem() != null)
		{
			String legoId = ((LegoTab) editorTabPane.getSelectionModel().getSelectedItem()).getDisplayedLegoID();
			for (Node n : snomedCodeDropTargets.get(legoId))
			{
				Effect existing = n.getEffect();
				if (existing != null)
				{
					existingEffect.put(n, existing);
				}
				n.setEffect(Utility.lightGreenDropShadow);
			}
		}
		for (Node n : snomedCodeDropTargets.get(NONE))
		{
			Effect existing = n.getEffect();
			if (existing != null)
			{
				existingEffect.put(n, existing);
			}
			n.setEffect(Utility.lightGreenDropShadow);
		}
	}

	public void snomedDragCompleted()
	{
		dragStartedAt = 0;
		if (editorTabPane.getSelectionModel().getSelectedItem() != null)
		{
			String legoId = ((LegoTab) editorTabPane.getSelectionModel().getSelectedItem()).getDisplayedLegoID();
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
	
	public void findConceptInTree(UUID concept)
	{
		findConceptInProgress.set(true);
		sctTree.showConcept(concept, findConceptInProgress);
		if (!showSnomedBtn.selectedProperty().get())
		{
			showSnomedBtn.selectedProperty().set(true);
			showSnomedBtn.fireEvent(new ActionEvent());
		}
	}
	
	public void findPendingConcept(String conceptId)
	{
		Concept concept = PendingConcepts.getInstance().getConcept(conceptId);
		if (concept != null)
		{
			pcpc.searchFor(concept.getDesc());
			if (!showPendingBtn.selectedProperty().get())
			{
				showPendingBtn.selectedProperty().set(true);
				showPendingBtn.fireEvent(new ActionEvent());
			}
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
								AnchorPane.setTopAnchor(sctTree, 0.0);
								AnchorPane.setBottomAnchor(sctTree, 0.0);
								AnchorPane.setLeftAnchor(sctTree, 0.0);
								AnchorPane.setRightAnchor(sctTree, 0.0);
								snomedBrowserPanel.setCenter(sctTree);
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
				if (importInitialDirectory != null)
				{
					fc.setInitialDirectory(importInitialDirectory);
				}
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("LEGO xml Files (*.xml)", "*.xml"));
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*"));
				List<File> files = fc.showOpenMultipleDialog(rootPane.getScene().getWindow());
				if (files != null && files.size() > 0)
				{
					importInitialDirectory = files.get(0).getParentFile();
					LegoGUI.getInstance().showImportDialog(files);
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
				LegoGUI.getInstance().showLegoListPropertiesDialog(null, null);
			}
		});
		
		menuFileCreateLegosFromFile.setGraphic(Images.LEGO_ADD.createImageView());
		menuFileCreateLegosFromFile.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent t)
			{
				LegoGUI.getInstance().showCreateLegosFromFileDialog();
			}
		});

		menuFileExportLegoLists.setGraphic(Images.LEGO_EXPORT_ALL.createImageView());
		menuFileExportLegoLists.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				LegoGUI.getInstance().showExportDialog(null);
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
		
		menuEditAddPending.setGraphic(Images.CONCEPT_VIEW.createImageView());
		menuEditAddPending.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				LegoGUI.getInstance().showAddPendingConcept(null);
			}
		});
		
		menuEditCemTool.setGraphic(Images.XML_VIEW_16.createImageView());
		menuEditCemTool.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				LegoGUI.getInstance().showCemTool();
			}
		});

		menuHelpAbout.setGraphic(Images.INFO.createImageView());
		menuHelpAbout.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				LegoGUI.getInstance().showAboutDialog();
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
		beginLegoEdit(lego, null);
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
			beginLegoEdit(newLego, lti);
		}
	}

	public LegoTab getLegoEditTabIfOpen(String uniqueLegoId)
	{
		return displayedLegos.get(uniqueLegoId);
	}
	
	public void showLegoSummaryPrefChanged()
	{
		for (LegoTab l : displayedLegos.values())
		{
			l.updateForSummaryPrefChange();
		}
	}

	private void beginLegoEdit(Lego lego, LegoTreeItem lti)
	{
		if (lego != null)
		{
			String legoId = ModelUtil.makeUniqueLegoID(lego);
			if (displayedLegos.containsKey(legoId))
			{
				editorTabPane.getSelectionModel().select(displayedLegos.get(legoId));
			}
			else
			{
				final LegoTab tab = new LegoTab("Lego", lego);
				tab.hasUnsavedChangesProperty().addListener(legoTabInvalidationListener);
				tab.hasChangedSinceLastValidate().addListener(legoTabInvalidationListener);
				tab.canRedo().addListener(legoTabInvalidationListener);
				tab.canUndo().addListener(legoTabInvalidationListener);
				displayedLegos.put(legoId, tab);

				int hue = random.nextInt(361);
				int saturation = random.nextInt(20) + 20; // Saturation between 20% and 40%
				int brightness = 90; // %

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
			
			showTreeItem(lti, ModelUtil.makeUniqueLegoID(lego));
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
			Utility.expandParents(ti);
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

	public void legoEditTabClosed(LegoTab tab)
	{
		displayedLegos.remove(tab.getDisplayedLegoID());
		StringProperty style = displayedLegosStyleInfo.remove(tab.getDisplayedLegoID());
		if (style != null)
		{
			style.setValue("-fx-effect: innershadow(two-pass-box , white , 0, 0.0 , 0 , 0);"); // Lego tree node is bound to this - auto update when
																								// we clear it.
		}
		
		tab.hasUnsavedChangesProperty().removeListener(legoTabInvalidationListener);
		tab.hasChangedSinceLastValidate().removeListener(legoTabInvalidationListener);
		tab.canRedo().removeListener(legoTabInvalidationListener);
		tab.canUndo().removeListener(legoTabInvalidationListener);
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
	
	public void rebuildSCTTree()
	{
		sctTree.rebuild();
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
					// don't close
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

	public CommonlyUsedConcepts getCommonlyUsedConcept()
	{
		return cut;
	}
	
	private class LegoTabInvalidationListener implements InvalidationListener, LegoValidateCallback
	{
		@Override
		public void invalidated(Observable observable)
		{
			enableSaveButton.invalidate();
			enableUndoButton.invalidate();
			enableRedoButton.invalidate();
			Tab t = editorTabPane.getSelectionModel().getSelectedItem();
			if (t != null)
			{
				if (((LegoTab)t).hasChangedSinceLastValidate().get())
				{
					schemaValidate(((LegoTab)t));
				}
			}
		}
		
		public void schemaValidate(LegoTab t)
		{
			legoInvalidImageView.setVisible(false);
			if (t == null)
			{
				legoInvalidImageView.setVisible(false);
			}
			else
			{
				legoValidationInProgress.setVisible(true);
				t.schemaValidate(this);
			}
		}

		@Override
		public void validateComplete(final boolean valid, final String errorMessage)
		{
			Platform.runLater(new Runnable()
			{
				
				@Override
				public void run()
				{
					legoValidationInProgress.setVisible(false);
					if (valid)
					{
						legoInvalidImageView.setVisible(false);
					}
					else
					{
						legoInvalidReason.setText("Lego Validation Failure:" + System.getProperty("line.separator") + errorMessage);
						legoInvalidImageView.setVisible(true);
					}
				}
			});
		}
	}
}
