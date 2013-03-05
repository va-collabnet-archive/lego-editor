package gov.va.legoEdit.gui.legoFilterPane;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.LegoGUIModel;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeView;
import gov.va.legoEdit.gui.util.AlphanumComparator;
import gov.va.legoEdit.gui.util.ExpandedNode;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.gui.util.Utility;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.CloseableIterator;
import gov.va.legoEdit.storage.wb.ConceptLookupCallback;
import gov.va.legoEdit.storage.wb.WBUtility;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegoFilterPaneController implements Initializable, ConceptLookupCallback
{
	@FXML// fx:id="borderPane"
	private BorderPane borderPane; // Value injected by FXMLLoader
	@FXML// fx:id="pncsItem"
	private ComboBox<PncsItem> pncsItem; // Value injected by FXMLLoader
	@FXML// fx:id="pncsNameOrId"
	private ComboBox<String> pncsNameOrId; // Value injected by FXMLLoader
	@FXML// fx:id="pncsValue"
	private ComboBox<String> pncsValue; // Value injected by FXMLLoader
	@FXML// fx:id="snomedId"
	private TextField snomedId; // Value injected by FXMLLoader
	@FXML //  fx:id="snomedIdStack"
	private StackPane snomedIdStack; // Value injected by FXMLLoader
	@FXML// fx:id="clearButton"
	private Button clearButton; // Value injected by FXMLLoader
	@FXML// fx:id="advancedButton"
	private Button advancedButton; // Value injected by FXMLLoader

	private LegoTreeView ltv;
	private volatile AtomicInteger updateDisabled = new AtomicInteger(0);  // Update will only run when this is 0
	private Concept concept_;
	private Tooltip conceptInvalidTooltip_;

	private ProgressIndicator pi_;
	BooleanProperty snomedIdValid = new SimpleBooleanProperty(true);
	private volatile long lookupUpdateTime_ = 0;
	private AtomicInteger lookupsInProgress_ = new AtomicInteger();
	private BooleanBinding lookupInProgress = new BooleanBinding()
	{
		@Override
		protected boolean computeValue()
		{
			return lookupsInProgress_.get() > 0;
		}
	};

	Logger logger = LoggerFactory.getLogger(LegoFilterPaneController.class);

	public static LegoFilterPaneController init()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader();
			loader.load(LegoFilterPaneController.class.getResourceAsStream("LegoFilterPane.fxml"));
			return loader.getController();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Unexpected", e);
		}
	}

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'Untitled 1'.";
		assert pncsItem != null : "fx:id=\"pncsItem\" was not injected: check your FXML file 'Untitled 1'.";
		assert pncsNameOrId != null : "fx:id=\"pncsNameOrId\" was not injected: check your FXML file 'Untitled 1'.";
		assert pncsValue != null : "fx:id=\"pncsValue\" was not injected: check your FXML file 'Untitled 1'.";

		// initialize your logic here: all @FXML variables will have been injected

		ltv = new LegoTreeView();
		borderPane.setCenter(ltv);
		AnchorPane.setBottomAnchor(borderPane, 0.0);
		AnchorPane.setTopAnchor(borderPane, 0.0);
		AnchorPane.setLeftAnchor(borderPane, 0.0);
		AnchorPane.setRightAnchor(borderPane, 0.0);

		pncsValue.setDisable(true);

		pncsNameOrId.getItems().add("Name");
		pncsNameOrId.getItems().add("Id");
		pncsNameOrId.getSelectionModel().select(0);

		pncsNameOrId.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				updateDisabled.incrementAndGet();
				// UGLY HACK cause I can't figure out how to tell the combo box that the value of the items it is displaying changed.
				PncsItem selected = pncsItem.getSelectionModel().getSelectedItem();
				ObservableList<PncsItem> items = FXCollections.observableArrayList();
				items.addAll(pncsItem.getItems());
				pncsItem.getItems().clear();
				pncsItem.getItems().addAll(items);
				FXCollections.sort(pncsItem.getItems(), new PncsItemComparator());
				pncsItem.getSelectionModel().select(selected);
				updateDisabled.decrementAndGet();
			}
		});

		loadPncs();

		pncsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				PncsItem item = pncsItem.getValue();
				if (item != null)
				{
					updateDisabled.incrementAndGet();
					if (item.getName().equals(PncsItem.ANY))
					{
						pncsValue.setDisable(true);
						pncsValue.getItems().clear();
					}
					else
					{
						pncsValue.setDisable(false);
						pncsValue.getItems().clear();
						List<Pncs> items = BDBDataStoreImpl.getInstance().getPncs(pncsItem.getValue().getId());
						for (Pncs pncs : items)
						{
							pncsValue.getItems().add(pncs.getValue());
						}
						FXCollections.sort(pncsValue.getItems(), new AlphanumComparator(true));
						pncsValue.getItems().add(0, PncsItem.ANY);
						pncsValue.getSelectionModel().select(0);
					}
					updateDisabled.decrementAndGet();
					updateLegoList();
				}
			}
		});

		pncsValue.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				updateLegoList();
			}
		});

		ImageView lookupFailImage = Images.EXCLAMATION.createImageView();
		lookupFailImage.visibleProperty().bind(snomedIdValid.not());
		conceptInvalidTooltip_ = new Tooltip("The specified concept was not found in the Snomed Database.");
		Tooltip.install(lookupFailImage, conceptInvalidTooltip_);

		snomedId.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				if (oldValue.length() > 0 && newValue.length() == 0)
				{
					snomedIdValid.set(true);
					snomedId.setTooltip(null);
					concept_ = null;
					updateLegoList();
				}

				if (newValue.length() > 0)
				{
					if (updateDisabled.get() > 0)
					{
						return;
					}
					lookupsInProgress_.incrementAndGet();
					lookupInProgress.invalidate();
					WBUtility.lookupSnomedIdentifier(newValue, LegoFilterPaneController.this);
				}
			}
		});

		LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(snomedId);

		clearButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				clearFilter();
			}
		});
		
		pi_ = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		pi_.visibleProperty().bind(lookupInProgress);
		pi_.setPrefHeight(16.0);
		pi_.setPrefWidth(16.0);
		pi_.setMaxWidth(16.0);
		pi_.setMaxHeight(16.0);
		
		snomedIdStack.getChildren().add(pi_);
		StackPane.setAlignment(pi_, Pos.CENTER_RIGHT);
		StackPane.setMargin(pi_, new Insets(0.0, 5.0, 0.0, 0.0));
		snomedIdStack.getChildren().add(lookupFailImage);
		StackPane.setAlignment(lookupFailImage, Pos.CENTER_RIGHT);
		StackPane.setMargin(lookupFailImage, new Insets(0.0, 5.0, 0.0, 0.0));
		
		advancedButton.setDisable(true);

		updateLegoList();
	}

	public synchronized void filterOnConcept(String conceptId)
	{
		updateDisabled.incrementAndGet();
		clearFilter();
		updateDisabled.decrementAndGet();
		snomedId.setText(conceptId);
		LegoGUI.getInstance().getLegoGUIController().showLegoLists();
	}
	
	private void clearFilter()
	{
		updateDisabled.incrementAndGet();
		pncsItem.getSelectionModel().select(0);
		snomedId.setText("");
		snomedId.setTooltip(null);
		updateDisabled.decrementAndGet();
		updateLegoList();
	}

	public void updateLegoList()
	{
		if (updateDisabled.get() > 0)
		{
			return;
		}

		Integer pncsFilterId = null;
		String pncsFilterValue = null;
		if (!pncsItem.getSelectionModel().getSelectedItem().getName().equals(PncsItem.ANY))
		{
			pncsFilterId = pncsItem.getSelectionModel().getSelectedItem().getId();
		}
		if (!pncsValue.isDisable() && !pncsValue.getSelectionModel().getSelectedItem().equals(PncsItem.ANY))
		{
			pncsFilterValue = pncsValue.getSelectionModel().getSelectedItem();
		}

		ExpandedNode before = Utility.buildExpandedNodeHierarchy(ltv.getRoot());
		LegoGUIModel.getInstance().initializeLegoListNames(ltv.getRoot().getChildren(), pncsFilterId, pncsFilterValue, concept_);
		Utility.setExpandedStates(before, ltv.getRoot());
		ltv.getSelectionModel().clearSelection();
	}

	public BorderPane getBorderPane()
	{
		return borderPane;
	}

	public void reloadOptions()
	{
		updateDisabled.incrementAndGet();
		loadPncs();
		updateDisabled.decrementAndGet();
		clearFilter();
	}

	private void loadPncs()
	{
		CloseableIterator<Pncs> iterator = BDBDataStoreImpl.getInstance().getPncs();
		HashMap<String, PncsItem> unique = new HashMap<>();
		while (iterator.hasNext())
		{
			Pncs pncs = iterator.next();
			unique.put(pncs.getName() + pncs.getId(), new PncsItem(pncs, pncsNameOrId.valueProperty()));
		}
		pncsItem.getItems().clear();
		pncsItem.getItems().addAll(new PncsItem(PncsItem.ANY, -1, pncsNameOrId.valueProperty()));
		pncsItem.getItems().addAll(unique.values());
		FXCollections.sort(pncsItem.getItems(), new PncsItemComparator());
		pncsItem.getSelectionModel().select(0);
	}

	@Override
	public void lookupComplete(final Concept concept, final long submitTime)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				lookupsInProgress_.decrementAndGet();
				lookupInProgress.invalidate();

				if (submitTime < lookupUpdateTime_)
				{
					// Throw it away, we already got back a newer lookup.
					logger.debug("throwing away a lookup");
					return;
				}
				else
				{
					lookupUpdateTime_ = submitTime;
				}

				concept_ = concept;

				if (concept_ != null)
				{
					updateDisabled.incrementAndGet();
					snomedId.setText(concept_.getDesc());
					snomedId.setTooltip(new Tooltip(concept_.getDesc() + " " + (concept_.getSctid() != null ? concept_.getSctid() : "")));
					updateDisabled.decrementAndGet();
					snomedIdValid.set(true);
					updateLegoList();
				}
				else
				{
					concept_ = null;
					snomedIdValid.set(false);
					conceptInvalidTooltip_.setText("The specified concept was not found in the Snomed Database, and this value is not being used in the filter.");
				}
			}
		});
	}
}
