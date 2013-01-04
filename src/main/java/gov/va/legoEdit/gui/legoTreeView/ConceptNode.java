package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.storage.wb.ConceptLookupCallback;
import gov.va.legoEdit.storage.wb.WBUtility;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

public class ConceptNode implements ConceptLookupCallback
{
    private static DropShadow invalidDropShadow = new DropShadow();
    static
    {
        invalidDropShadow.setColor(Color.RED);
    }
    
    private VBox vbox_;
    private ComboBox<ComboBoxConcept> cb_;
    private Label descriptionLabel_;
    private Label contentLabel_;
    private ProgressIndicator pi_;
    private Concept c_;
    private LegoTreeView legoTreeView_;
    
    private BooleanProperty isValid = new SimpleBooleanProperty(true);
    private BooleanProperty lookupInProgress = new SimpleBooleanProperty(false);

    public ConceptNode(String label, Concept c, ConceptUsageType cut, LegoTreeNodeType tct, LegoTreeView legoTreeView)
    {
        c_ = c;
        legoTreeView_ = legoTreeView;
        vbox_ = new VBox();
        vbox_.setSpacing(5.0);
        vbox_.setAlignment(Pos.CENTER_LEFT);
        vbox_.setFillWidth(true);
        if (label != null && label.length() > 0)
        {
            contentLabel_ = new Label(label);
            contentLabel_.getStyleClass().add("boldLabel");
        }
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
        cb_.setMinWidth(200.0);
        cb_.setPrefWidth(200.0);
        cb_.setPromptText("Specify or drop a Snomed SCTID or UUID");
        cb_.setItems(LegoGUI.getInstance().getLegoGUIController().getCommonlyUsedConcept().getSuggestions(cut));
        cb_.setVisibleRowCount(11);
        
        descriptionLabel_ = new Label();
        descriptionLabel_.visibleProperty().bind(lookupInProgress.not());

        updateGUI();
        lookup();
        
        cb_.valueProperty().addListener(new ChangeListener<ComboBoxConcept>()
        {
            @Override
            public void changed(ObservableValue<? extends ComboBoxConcept> observable, ComboBoxConcept oldValue, ComboBoxConcept newValue)
            {
                lookup();
            }
        });

        LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(legoTreeView_.getLego(), cb_);
        
        AnchorPane ap = new AnchorPane();
        pi_ = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        pi_.visibleProperty().bind(lookupInProgress);
        pi_.setPrefHeight(20.0);
        pi_.setPrefWidth(20.0);
        ap.getChildren().add(pi_);
        ap.getChildren().add(descriptionLabel_);
               
        if (contentLabel_ != null)
        {
            vbox_.getChildren().add(contentLabel_);
            VBox.setMargin(cb_, new Insets(0, 0, 0, 10));
            VBox.setMargin(ap, new Insets(0, 0, 0, 10));
        }
        vbox_.getChildren().add(cb_);        
        vbox_.getChildren().add(ap);
        
        vbox_.setOnDragDetected(new EventHandler<MouseEvent>()
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

        vbox_.setOnDragDone(new EventHandler<DragEvent>()
        {
            public void handle(DragEvent event)
            {
                LegoGUI.getInstance().getLegoGUIController().snomedDragCompleted();
            }
        });
        
        MenuItem mi = new MenuItem("View Concept");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                waitForLookupToComplete();
                if (isValid.get())
                {
                    LegoGUI.getInstance().showSnomedConceptDialog(UUID.fromString(c_.getUuid()));
                }
                else
                {
                    LegoGUI.getInstance().showErrorDialog("Unknown Concept", "Can't lookup an invalid concept", "");
                }
            }
        });
        
        //Would like to do this, as well, but can't, cause javafx doesn't let you get to this menu (yet - its a bug)
        //cb_.getEditor().getContextMenu().getItems().add(0, mi);
        
        descriptionLabel_.setContextMenu(new ContextMenu(mi));
        
        isValid.addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (newValue)
                {
                    cb_.setEffect(null);
                }
                else
                {
                    cb_.setEffect(invalidDropShadow);
                }
            }
        });
    }
    
    private void updateGUI()
    {
        //Bad design by dan.  In the drop down, I want to show description.  However, in the combo field, I want to show id.
        //So my hack fix is to put the ID in both fields, when the update comes back from the lookup.  When there is a value change, we only
        //read the ID.
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
        //If the concept is fully populated, and the sctId equals the displayed value, 
        //don't bother doing the lookup (conceptNodes are created whenever a tree expand/collapse takes place - most of the time
        //the value hasn't changed....
        if (c_ != null && c_.getDesc() != null && c_.getUuid() != null && c_.getSctid() != null 
                && c_.getDesc().length() > 0 && c_.getUuid().length() > 0 && cb_.getValue().getId().equals(c_.getSctid() + ""))
        {
            return;
        }
                
        waitForLookupToComplete();
        lookupInProgress.set(true);
        WBUtility.lookupSnomedIdentifier(cb_.getValue().getId(), this);
    }
    
    private void waitForLookupToComplete()
    {
        synchronized (lookupInProgress)
        {
            while (lookupInProgress.get())
            {
                //Wait for previous lookup to complete
                try
                {
                    lookupInProgress.wait();
                }
                catch (InterruptedException e)
                {
                    // noop
                }
            }
        }
    }

    public Node getNode()
    {
        return vbox_;
    }

    public Concept getConcept()
    {
        return c_;
    }
    
    public BooleanProperty isValid()
    {
        waitForLookupToComplete();
        return isValid;
    }

    @Override
    public void lookupComplete(final Concept concept)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                lookupInProgress.set(false);
                synchronized (lookupInProgress)
                {
                    lookupInProgress.notifyAll();
                }
                if (concept != null)
                {
                    c_.setDesc(concept.getDesc());
                    c_.setSctid(concept.getSctid());
                    c_.setUuid(concept.getUuid());
                    isValid.set(true);
                }
                else
                {
                    c_.setSctid(null);
                    c_.setUuid(cb_.getValue().getId());
                    c_.setDesc(cb_.getValue().getId().length() > 0 ? "Invalid Concept" : "");
                    isValid.set(false);
                }
                legoTreeView_.contentChanged();
                updateGUI();
            }
        });
    }
}
