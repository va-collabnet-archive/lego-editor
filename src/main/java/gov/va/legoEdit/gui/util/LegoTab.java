package gov.va.legoEdit.gui.util;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.formats.LegoValidateCallback;
import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.gui.dialogs.YesNoDialogController.Answer;
import gov.va.legoEdit.gui.legoInfoPanel.LegoInfoPanel;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeNodeType;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeView;
import gov.va.legoEdit.model.ModelUtil;
import gov.va.legoEdit.model.SchemaClone;
import gov.va.legoEdit.model.SchemaEquals;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.util.TimeConvert;
import java.util.ArrayList;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import com.sun.javafx.scene.control.skin.TabPaneSkin;

public class LegoTab extends Tab
{
	private Lego displayedLego;  
	private ArrayList<Lego> displayedLegoHistory;  //This is for storing a history of changes for undo/redo
	private int displayedLegoPositionInHistory;  //part of the history	 for undo/redo
	private LegoInfoPanel lip;
	private BooleanBinding legoNeedsSaving;
	private BooleanBinding hasChangedSinceLastValidate;
	private long lastValidateTime = 0;
	private long lastChangeTime = 1;
	private ImageView open = Images.LEGO.createImageView();
	private ImageView openEdited = Images.LEGO_EDIT.createImageView();
	private LegoTreeView legoTree;
	
	private BooleanBinding canUndo = new BooleanBinding()
	{
		@Override
		protected boolean computeValue()
		{
			return (displayedLegoPositionInHistory + 1) < displayedLegoHistory.size();
		}
	};
	
	private BooleanBinding canRedo = new BooleanBinding()
	{
		@Override
		protected boolean computeValue()
		{
			return displayedLegoPositionInHistory > 0;
		}
	};

	public LegoTab(String tabName, Lego displayedLego)
	{
		super();
		this.displayedLego = displayedLego;
		displayedLegoHistory = new ArrayList<>();
		displayedLegoPositionInHistory = 0;
		this.displayedLegoHistory.add(0, SchemaClone.clone(displayedLego));
		this.setClosable(false); // Don't show the native close button
		HBox hbox = new HBox();
		final Label titleLabel = new Label(tabName);
		titleLabel.setAlignment(Pos.CENTER_LEFT);
		titleLabel.getStyleClass().clear();
		titleLabel.getStyleClass().add("tab-label");
		titleLabel.setMaxHeight(Double.MAX_VALUE);
		hbox.getChildren().add(titleLabel);

		final StackPane closeBtn = new StackPane();
		closeBtn.getStyleClass().setAll(new String[] { "tab-close-button" });
		closeBtn.setStyle("-fx-cursor:hand;");
		closeBtn.setPadding(new Insets(0, 7, 0, 7));
		closeBtn.visibleProperty().bind(this.selectedProperty());

		final EventHandler<ActionEvent> closeEvent = new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent paramT)
			{
				((TabPaneSkin) LegoTab.this.getTabPane().getSkin()).getBehavior().closeTab(LegoTab.this);
			}
		};

		// Handler for the close button.
		closeBtn.setOnMouseReleased(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent paramT)
			{
				if (legoNeedsSaving.get())
				{
					Answer answer = LegoGUI.getInstance().showYesNoDialog("Unsaved Changes", "This Lego has unsaved changes.  Do you want to close anyway?");
					if (answer == null || answer == Answer.NO)
					{
						// don't close
						return;
					}
				}

				closeEvent.handle(null);
			}
		});

		hbox.getChildren().add(closeBtn);
		setGraphic(hbox);

		legoNeedsSaving = new BooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				Lego storedLego = BDBDataStoreImpl.getInstance().getLego(LegoTab.this.displayedLego.getLegoUUID(),
						LegoTab.this.displayedLego.getStamp().getUuid());
				return !SchemaEquals.equals(storedLego, LegoTab.this.displayedLego);
			}
		};

		legoNeedsSaving.addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				if (newValue)
				{
					titleLabel.setGraphic(openEdited);
				}
				else
				{
					titleLabel.setGraphic(open);
				}
			}
		});
		
		hasChangedSinceLastValidate = new BooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				return lastChangeTime > lastValidateTime;
			}
		};

		titleLabel.setGraphic(legoNeedsSaving.get() ? openEdited : open);

		buildLegoView(displayedLego);
	}
	
	private void buildLegoView(Lego displayedLego)
	{
		this.setContent(null);
		lip = new LegoInfoPanel(displayedLego.getPncs().getName(), displayedLego.getPncs().getValue(), displayedLego.getPncs().getId() + "", displayedLego.getLegoUUID(),
				displayedLego.getStamp().getAuthor(), displayedLego.getStamp().getModule(), TimeConvert.format(displayedLego.getStamp().getTime()), displayedLego
						.getStamp().getPath());

		legoTree = new LegoTreeView();
		legoTree.setLegoTab(this);

		BorderPane bp = new BorderPane();
		bp.setTop(lip.getPane());
		bp.setCenter(legoTree);
		this.setContent(bp);

		legoTree.getRoot().getChildren().add(new LegoTreeItem(displayedLego.getStamp(), LegoTreeNodeType.status));
		legoTree.getRoot().getChildren().add(new LegoTreeItem(displayedLego, LegoTreeNodeType.comment));
		for (Assertion a : displayedLego.getAssertion())
		{
			LegoTreeItem lti = new LegoTreeItem(a);
			legoTree.getRoot().getChildren().add(lti);
			lti.setExpanded(true);
		}
		legoTree.getRoot().getChildren().add(new LegoTreeItem(LegoTreeNodeType.blankLegoEndNode));
		recursiveSort(legoTree.getRoot().getChildren());
		legoTree.getRoot().setExpanded(true);
	}

	public String getDisplayedLegoID()
	{
		return ModelUtil.makeUniqueLegoID(displayedLego);
	}

	public Lego getLego()
	{
		return displayedLego;
	}

	public BooleanBinding hasUnsavedChangesProperty()
	{
		return legoNeedsSaving;
	}
	
	public BooleanBinding canUndo()
	{
		return canUndo;
	}
	
	public BooleanBinding canRedo()
	{
		return canRedo;
	}

	public BooleanBinding hasChangedSinceLastValidate()
	{
		return hasChangedSinceLastValidate;
	}
	
	public void schemaValidate(LegoValidateCallback sendAnswerTo)
	{
		LegoXMLUtils.schemaValidateLego(displayedLego, sendAnswerTo);
		lastValidateTime = System.currentTimeMillis();
		hasChangedSinceLastValidate.invalidate();
	}

	public void contentChanged()
	{
		while (displayedLegoPositionInHistory > 0)
		{
			displayedLegoHistory.remove(0);
			displayedLegoPositionInHistory--;
		}
		//And clone the new current state
		displayedLegoHistory.add(0, SchemaClone.clone(displayedLego));
		
		lastChangeTime = System.currentTimeMillis();
		legoNeedsSaving.invalidate();
		canUndo.invalidate();
		canRedo.invalidate();
		hasChangedSinceLastValidate.invalidate();
	}
	
	public void undo()
	{
		ExpandedNode currentState = Utility.buildExpandedNodeHierarchy(legoTree.getRoot());

		displayedLegoPositionInHistory++;
		displayedLego = SchemaClone.clone(displayedLegoHistory.get(displayedLegoPositionInHistory));
		
		buildLegoView(displayedLego);
		
		lastChangeTime = System.currentTimeMillis();
		legoNeedsSaving.invalidate();
		canUndo.invalidate();
		canRedo.invalidate();
		hasChangedSinceLastValidate.invalidate();
		
		Utility.setExpandedStates(currentState, legoTree.getRoot());
	}
	
	public void redo()
	{
		ExpandedNode currentState = Utility.buildExpandedNodeHierarchy(legoTree.getRoot());
		
		displayedLegoPositionInHistory--;
		displayedLego = SchemaClone.clone(displayedLegoHistory.get(displayedLegoPositionInHistory));
		
		buildLegoView(displayedLego);
		
		lastChangeTime = System.currentTimeMillis();
		legoNeedsSaving.invalidate();
		canUndo.invalidate();
		canRedo.invalidate();
		hasChangedSinceLastValidate.invalidate();
		Utility.setExpandedStates(currentState, legoTree.getRoot());
	}

	public void updateForSave(Stamp stamp)
	{
		lip.update(stamp.getAuthor(), stamp.getModule(), TimeConvert.format(stamp.getTime()), stamp.getPath());
		displayedLegoPositionInHistory = 0;  //would be very tricky to maintain the history after a save, so just clear it.
		displayedLegoHistory.clear();
		this.displayedLegoHistory.add(0, SchemaClone.clone(displayedLego));
		canUndo.invalidate();
		canRedo.invalidate();
	}

	private void recursiveSort(ObservableList<TreeItem<String>> items)
	{
		FXCollections.sort(items, new LegoTreeItemComparator(true));
		for (TreeItem<String> item : items)
		{
			recursiveSort(item.getChildren());
		}
	}
}
