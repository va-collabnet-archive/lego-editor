package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.storage.wb.ConceptLookupCallback;
import gov.va.legoEdit.storage.wb.WBUtility;
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
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConceptNode This class handles the GUI display of snomed concepts in the tree view
 * @author Dan Armbrust 
 * Copyright 2013
 * 
 */
public class ConceptNode implements ConceptLookupCallback
{
	private static Logger logger = LoggerFactory.getLogger(ConceptNode.class);

	private HBox hbox_;
	private ComboBox<ComboBoxConcept> cb_;
	private Label descriptionLabel_;
	private Label typeLabel_;
	private ProgressIndicator pi_;
	private ImageView lookupFailImage_;
	private Concept c_;
	private LegoTreeView legoTreeView_;
	private LegoTreeItem lti_;

	private BooleanProperty isValid = new SimpleBooleanProperty(true);
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
		cb_.setMaxWidth(320.0);
		cb_.setMinWidth(320.0);
		cb_.setPrefWidth(320.0);
		cb_.setPromptText("SCTID or UUID");
		cb_.setItems(FXCollections.observableArrayList(LegoGUI.getInstance().getLegoGUIController().getCommonlyUsedConcept().getSuggestions(cut)));
		cb_.setVisibleRowCount(11);

		if (typeLabel != null && typeLabel.length() > 0)
		{
			typeLabel_ = new Label(typeLabel);
		}

		descriptionLabel_ = new Label();
		descriptionLabel_.setMaxWidth(Double.MAX_VALUE);
		descriptionLabel_.setMinWidth(100.0);
		descriptionLabel_.visibleProperty().bind(lookupInProgress.not());

		updateGUI();
		lookup();

		cb_.valueProperty().addListener(new ChangeListener<ComboBoxConcept>()
		{
			@Override
			public void changed(ObservableValue<? extends ComboBoxConcept> observable, ComboBoxConcept oldValue, ComboBoxConcept newValue)
			{
				if (oldValue.getId().trim().equals(newValue.getId().trim()))
				{
					// Not a real change
					return;
				}
				c_.setDesc("");
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

		StackPane sp = new StackPane();
		sp.getChildren().add(cb_);
		sp.getChildren().add(lookupFailImage_);
		sp.getChildren().add(pi_);
		StackPane.setAlignment(cb_, Pos.CENTER_LEFT);
		StackPane.setAlignment(lookupFailImage_, Pos.CENTER_RIGHT);
		StackPane.setMargin(lookupFailImage_, new Insets(0.0, 20.0, 0.0, 0.0));
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
		// HBox.setHgrow(sp, Priority.ALWAYS);
		hbox_.getChildren().add(descriptionLabel_);

		hbox_.setOnDragDetected(new EventHandler<MouseEvent>()
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

		hbox_.setOnDragDone(new EventHandler<DragEvent>()
		{
			public void handle(DragEvent event)
			{
				LegoGUI.getInstance().getLegoGUIController().snomedDragCompleted();
			}
		});
	}

	private void updateGUI()
	{
		// Bad design by dan. In the drop down, I want to show description. However, in the combo field, I want to show id.
		// So my hack fix is to put the ID in both fields, when the update comes back from the lookup. When there is a value change, we only
		// read the ID.
		if (c_.getSctid() != null)
		{
			cb_.setValue(new ComboBoxConcept(c_.getSctid() + "", c_.getSctid() + ""));
		}
		else if (c_.getUuid() != null)
		{
			cb_.setValue(new ComboBoxConcept(c_.getUuid(), c_.getUuid()));
		}
		else
		{
			cb_.setValue(new ComboBoxConcept("", ""));
		}
		descriptionLabel_.setText(c_.getDesc() == null ? "" : c_.getDesc());
	}

	private synchronized void lookup()
	{
		// If the concept is fully populated, and the sctId equals the displayed value,
		// don't bother doing the lookup (conceptNodes are created whenever a tree expand/collapse takes place - most of the time
		// the value hasn't changed....
		if (c_ != null && c_.getDesc() != null && c_.getUuid() != null && c_.getSctid() != null && c_.getDesc().length() > 0 && c_.getUuid().length() > 0
				&& cb_.getValue().getId().equals(c_.getSctid() + ""))
		{
			return;
		}

		lookupsInProgress_.incrementAndGet();
		lookupInProgress.invalidate();
		WBUtility.lookupSnomedIdentifier(cb_.getValue().getId(), this);
	}

	public Node getNode()
	{
		return hbox_;
	}

	public Concept getConcept()
	{
		return c_;
	}

	public boolean isValid()
	{
		return isValid.get();
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

				if (concept != null)
				{
					c_.setDesc(concept.getDesc());
					c_.setSctid(concept.getSctid());
					c_.setUuid(concept.getUuid());
					LegoGUI.getInstance().getLegoGUIController().updateRecentCodes(c_);
					isValid.set(true);
				}
				else
				{
					// lookup failed - try to store what they entered, even if not valid.
					try
					{
						c_.setSctid(Long.parseLong(cb_.getValue().getId().toString().trim()));
						c_.setUuid(null);
					}
					catch (NumberFormatException e)
					{
						// that won't work. Is it a UUID?
						try
						{
							c_.setUuid(UUID.fromString(cb_.getValue().getId()).toString().trim());
							c_.setSctid(null);
						}
						catch (IllegalArgumentException e1)
						{
							// nope Can't save it anywhere.
							c_.setUuid(null);
							c_.setSctid(null);
						}
					}
					isValid.set(false);
				}
				legoTreeView_.contentChanged(lti_);
				updateGUI();
			}
		});
	}
}
