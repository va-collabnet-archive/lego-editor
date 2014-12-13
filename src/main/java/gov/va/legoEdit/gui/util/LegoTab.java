package gov.va.legoEdit.gui.util;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.LegoGUIModel;
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
import gov.va.legoEdit.model.SchemaToString;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.util.TimeConvert;
import java.util.ArrayList;
import javafx.application.Platform;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import com.sun.javafx.scene.control.skin.TabPaneSkin;

/**
 * 
 * LegoTab
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
@SuppressWarnings("restriction")
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
	private TextArea summary;
	private VBox summaryVBox = null;
	
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
		closeBtn.getStyleClass().add("tab-close-button");
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

		VBox tabContent = new VBox();
		tabContent.getChildren().add(lip.getPane());
		lip.getPane().setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(lip.getPane(), Priority.SOMETIMES);

		if (LegoGUIModel.getInstance().getUserPreferences().getShowSummary())
		{
			setupSummary();
			tabContent.getChildren().add(summaryVBox);
		}
		
		tabContent.getChildren().add(legoTree);
		VBox.setVgrow(legoTree, Priority.ALWAYS);
		this.setContent(tabContent);

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
	
	private void setupSummary()
	{
		summaryVBox = new VBox();
		summaryVBox.getStyleClass().add("itemBorder");
		Label l = new Label("Summary");
		l.getStyleClass().add("boldLabel");
		summaryVBox.getChildren().add(l);
		summary = new TextArea();
		summary.setEditable(false);
		summary.setWrapText(false);
		summary.setFocusTraversable(false);
		updateSummary();
		summaryVBox.getChildren().add(summary);
		VBox.setVgrow(summary, Priority.ALWAYS);
		summaryVBox.setMaxHeight(200.0);
		summaryVBox.setMinHeight(200.0);
		VBox.setVgrow(summaryVBox, Priority.NEVER);
	}
	
	public void updateForSummaryPrefChange()
	{
		if (LegoGUIModel.getInstance().getUserPreferences().getShowSummary() && summaryVBox == null)
		{
			setupSummary();
			((VBox)this.getContent()).getChildren().add(1, summaryVBox);
		}
		else if (!LegoGUIModel.getInstance().getUserPreferences().getShowSummary() && summaryVBox != null)
		{
			((VBox)this.getContent()).getChildren().remove(summaryVBox);
			summaryVBox = null;
			summary = null;
		}
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
		if (summary != null)
		{
			updateSummary();
		}
	}
	
	private void updateSummary()
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				final StringBuilder value = new StringBuilder();
				for (Assertion a : displayedLego.getAssertion())
				{
					value.append(SchemaToString.summary(a));
					value.append(SchemaToString.eol);
				}
				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						double scrollTop = summary.getScrollTop();
						summary.setText(value.toString());
						summary.setScrollTop(scrollTop);
					}
				});
				
			}
		};
		
		gov.va.legoEdit.util.Utility.tpe.execute(r);
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
