package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.LegoGUIModel;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.model.PendingConcept;
import gov.va.legoEdit.model.PendingConcepts;
import gov.va.legoEdit.model.SchemaClone;
import gov.va.legoEdit.model.SchemaEquals;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.userPrefs.UserPreferences;
import gov.va.legoEdit.storage.wb.ConceptLookupCallback;
import gov.va.legoEdit.storage.wb.WBUtility;
import gov.va.legoEdit.util.Utility;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConceptNode This class handles the GUI display of snomed concepts in the tree view
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 * 
 */
public class ConceptNode implements ConceptLookupCallback
{
	private static Logger logger = LoggerFactory.getLogger(ConceptNode.class);
	private static UserPreferences up = LegoGUIModel.getInstance().getUserPreferences();

	private HBox hbox_;
	private ComboBox<ComboBoxConcept> cb_;
	private Label typeLabel_;
	private ProgressIndicator pi_;
	private ImageView lookupFailImage_;
	private Concept c_;
	private LegoTreeView legoTreeView_;
	private LegoTreeItem lti_;
	private ComboBoxConcept codeSetComboBoxConcept_ = null;
	private BooleanProperty isValid = new SimpleBooleanProperty(true);
	private BooleanProperty isPending = new SimpleBooleanProperty(false);
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

	public ConceptNode(String typeLabel, Concept c, ConceptUsageType cut, LegoTreeItem lti, LegoTreeView legoTreeView)
	{
		c_ = c;
		legoTreeView_ = legoTreeView;
		lti_ = lti;
		cb_ = new ComboBox<>();
		cb_.setConverter(new StringConverter<ComboBoxConcept>()
		{
			@Override
			public String toString(ComboBoxConcept object)
			{
				return object.getDescription();
			}

			@Override
			public ComboBoxConcept fromString(String string)
			{
				return new ComboBoxConcept(string, string);
			}
		});
		cb_.setEditable(true);
		cb_.setMaxWidth(Double.MAX_VALUE);
		cb_.setPrefWidth(ComboBox.USE_COMPUTED_SIZE);
		cb_.setMinWidth(200.0);
		cb_.setPromptText("Drop or Select a Concept");
		cb_.setItems(FXCollections.observableArrayList(LegoGUI.getInstance().getLegoGUIController().getCommonlyUsedConcept().getSuggestions(cut)));
		cb_.setVisibleRowCount(11);
		
		if (typeLabel != null && typeLabel.length() > 0)
		{
			typeLabel_ = new Label(typeLabel);
			typeLabel_.setMinWidth(Label.USE_PREF_SIZE);
		}
		
		//Would be nice to have a single global one of these, but don't have time to deal with the memory-leak implications
		new LookAheadConceptPopup(cb_);

		updateGUI();
		//don't force lookup on load for blank items
		if (cb_.getValue().getId().length() > 0)
		{
			if (!lookup() && PendingConcepts.getInstance().hasConcept(c_.getUuid()))
			{
				//set pending if necessary if no lookup happens, otherwise, let the lookup deal with it.
				isPending.set(true);
			}
		}
		else
		{
			isValid.set(false);
		}

		cb_.valueProperty().addListener(new ChangeListener<ComboBoxConcept>()
		{
			@Override
			public void changed(ObservableValue<? extends ComboBoxConcept> observable, ComboBoxConcept oldValue, ComboBoxConcept newValue)
			{
				if (newValue.shouldIgnoreChange())
				{
					return;
				}
				//Whenever the focus leaves the combo box editor, a new combo box is generated.  But, the new generated will have the description
				//in place of the id.  detect and ignore.
				if (codeSetComboBoxConcept_ != null && newValue.getId().equals(codeSetComboBoxConcept_.getDescription()))
				{
					return;
				}
				lookup();
			}
		});

		LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(legoTreeView_.getLego(), cb_);

		pi_ = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		pi_.visibleProperty().bind(lookupInProgress);
		pi_.setPrefHeight(16.0);
		pi_.setPrefWidth(16.0);
		pi_.setMaxWidth(16.0);
		pi_.setMaxHeight(16.0);

		lookupFailImage_ = Images.EXCLAMATION.createImageView();
		lookupFailImage_.visibleProperty().bind(isValid.not());
		Tooltip t = new Tooltip("The specified concept was not found in the Snomed Database.");
		Tooltip.install(lookupFailImage_, t);
		
		ImageView pending = Images.PENDING.createImageView();
		pending.visibleProperty().bind(isPending);
		Tooltip.install(pending,  new Tooltip("Pending Concept"));

		StackPane sp = new StackPane();
		sp.setMaxWidth(Double.MAX_VALUE);
		sp.getChildren().add(cb_);
		sp.getChildren().add(pending);
		sp.getChildren().add(lookupFailImage_);
		sp.getChildren().add(pi_);
		StackPane.setAlignment(cb_, Pos.CENTER_LEFT);
		StackPane.setAlignment(pending, Pos.CENTER_RIGHT);
		StackPane.setMargin(pending, new Insets(2.0, 30.0, 0.0, 0.0));
		StackPane.setAlignment(lookupFailImage_, Pos.CENTER_RIGHT);
		StackPane.setMargin(lookupFailImage_, new Insets(0.0, 30.0, 0.0, 0.0));
		StackPane.setAlignment(pi_, Pos.CENTER_RIGHT);
		StackPane.setMargin(pi_, new Insets(0.0, 20.0, 0.0, 0.0));

		hbox_ = new HBox();
		hbox_.setSpacing(5.0);
		hbox_.setAlignment(Pos.CENTER_LEFT);

		if (typeLabel_ != null)
		{
			hbox_.getChildren().add(typeLabel_);
		}

		hbox_.getChildren().add(sp);
		HBox.setHgrow(sp, Priority.SOMETIMES);
		cb_.getEditor().setOnDragDetected(new EventHandler<MouseEvent>()
		{
			public void handle(MouseEvent event)
			{
				/* drag was detected, start a drag-and-drop gesture */
				/* allow any transfer mode */
				if (c_ != null)
				{
					Dragboard db = cb_.startDragAndDrop(TransferMode.COPY);

					/* Put a string on a dragboard */
					String drag = null;
					if (c_.getUuid() != null)
					{
						drag = c_.getUuid();
					}
					else if (c_.getSctid() != null)
					{
						drag = c_.getSctid() + "";
					}
					if (drag != null)
					{
						ClipboardContent content = new ClipboardContent();
						content.putString(drag);
						db.setContent(content);
						LegoGUI.getInstance().getLegoGUIController().snomedDragStarted();
						event.consume();
					}
				}
			}
		});

		cb_.getEditor().setOnDragDone(new EventHandler<DragEvent>()
		{
			public void handle(DragEvent event)
			{
				LegoGUI.getInstance().getLegoGUIController().snomedDragCompleted();
			}
		});
	}

	private void updateGUI()
	{
		codeSetComboBoxConcept_ = new ComboBoxConcept(
				(c_.getDesc() == null ? "" : c_.getDesc()),
				(c_.getSctid() == null ? (c_.getUuid() == null ? "" : c_.getUuid()) : c_.getSctid() + ""), 
				true); 
		cb_.setValue(codeSetComboBoxConcept_);
		if (c_.getDesc() != null && c_.getDesc().length() > 0)
		{
			Tooltip t = new Tooltip(c_.getDesc());
			cb_.setTooltip(t);
		}
		else
		{
			cb_.setTooltip(null);
		}
	}

	/**
	 * returns true if launched, false if skipped because it decided it wasn't necessary
	 */
	private synchronized boolean lookup()
	{
		// If the concept is fully populated, and the id matches one of the proper IDs, and the description is not an ID
		// don't bother doing the lookup (conceptNodes are created whenever a tree expand/collapse takes place - most of the time
		// the value hasn't changed....
		//However, check to see if the desc type has changed.... let the lookup happen if the description type has changed.
		if (c_ != null && !Utility.isEmpty(c_.getDesc()) && !Utility.isEmpty(c_.getUuid()) && c_.getSctid() != null
				&& (cb_.getValue().getId().equals(c_.getSctid() + "") || cb_.getValue().getId().equals(c_.getUuid()))
				&& (!cb_.getValue().getDescription().equals(cb_.getValue().getId()))
				&& (
					(up.getUseFSN() && c_.getDesc().indexOf("(") > 0 && c_.getDesc().indexOf(")") > 0)
					|| (!up.getUseFSN() && c_.getDesc().indexOf("(") == -1 && c_.getDesc().indexOf(")") == -1)))
		{
			return false;
		}

		lookupsInProgress_.incrementAndGet();
		lookupInProgress.invalidate();
		WBUtility.lookupSnomedIdentifier(cb_.getValue().getId(), cb_.getValue().getDescription(), this, null);
		return true;
	}

	public Node getNode()
	{
		return hbox_;
	}
	
	public void setContextMenu(ContextMenu cm)
	{
		cb_.getEditor().setContextMenu(cm);
	}

	public Concept getConcept()
	{
		return c_;
	}
	
	protected String getDisplayedText()
	{
		return cb_.getValue().getDescription();
	}

	protected void set(String newValue)
	{
		cb_.setValue(new ComboBoxConcept(newValue, newValue));
	}
	
	public boolean isValid()
	{
		return isValid.get();
	}

	@Override
	public void lookupComplete(final Concept concept, final long submitTime, Integer callId)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				lookupsInProgress_.decrementAndGet();
				lookupInProgress.invalidate();
				isPending.set(false);

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

				Concept conceptBefore = SchemaClone.clone(c_);
				if (concept != null)
				{
					c_.setDesc(concept.getDesc());
					c_.setSctid(concept.getSctid());
					c_.setUuid(concept.getUuid());
					LegoGUI.getInstance().getLegoGUIController().updateRecentCodes(c_);
					isValid.set(true);
					if (concept instanceof PendingConcept)
					{
						isPending.set(true);
					}
				}
				else
				{
					// lookup failed - try to store what they entered, even if not valid.
					try
					{
						c_.setSctid(Long.parseLong(cb_.getValue().getId().toString().trim()));
						c_.setUuid(null);
						c_.setDesc(cb_.getValue().getId());
					}
					catch (NumberFormatException e)
					{
						// that won't work. Is it a UUID?
						try
						{
							c_.setUuid(UUID.fromString(cb_.getValue().getId()).toString().trim());
							c_.setSctid(null);
							c_.setDesc(cb_.getValue().getId());
						}
						catch (IllegalArgumentException e1)
						{
							// nope - just stuff it in the description field
							c_.setUuid(null);
							c_.setSctid(null);
							c_.setDesc(cb_.getValue().getId());
						}
					}
					isValid.set(false);
				}
				if (!SchemaEquals.equals(conceptBefore, c_))
				{
					//only notify changed if actually changed, otherwise we mess up the undo/redo history with concepts that fail lookup
					legoTreeView_.contentChanged(lti_);
				}
				updateGUI();
			}
		});
	}
}
