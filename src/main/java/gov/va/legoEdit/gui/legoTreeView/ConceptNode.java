package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.storage.wb.ConceptLookupCallback;
import gov.va.legoEdit.storage.wb.Utility;
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

public class ConceptNode implements ConceptLookupCallback
{
    private static DropShadow invalidDropShadow = new DropShadow();
    static
    {
        invalidDropShadow.setColor(Color.RED);
    }
    
    VBox vbox_;
    ComboBox<String> cb_;
    Label descriptionLabel_;
    Label contentLabel_;
    ProgressIndicator pi_;
    Concept c_;
    
    BooleanProperty isValid = new SimpleBooleanProperty(true);
    BooleanProperty lookupInProgress = new SimpleBooleanProperty(false);

    public ConceptNode(String label, Concept c, LegoTreeNodeType tct, Lego lego)
    {
        c_ = c;
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
        
        cb_.setEditable(true);
        cb_.setMaxWidth(Double.MAX_VALUE);
        cb_.setMinWidth(320.0);
        cb_.setPromptText("Specify or drop a Snomed SCTID or UUID");
        
        // TODO populate dropdown with most common values
        descriptionLabel_ = new Label();
        descriptionLabel_.visibleProperty().bind(lookupInProgress.not());

        updateGUI();
        lookup();
        
        cb_.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                lookup();
            }
        });

        LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(lego, cb_);
        
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
        if (c_.getSctid() != null)
        {
            cb_.setValue(c_.getSctid() + "");
        }
        else if (c_.getUuid() != null)
        {
            cb_.setValue(c_.getUuid());
        }
        else
        {
            cb_.setValue("");
        }
        descriptionLabel_.setText(c_.getDesc() == null ? "" : c_.getDesc());
    }
    
    private synchronized void lookup()
    {
        //If the concept is fully populated, and the sctId equals the displayed value, 
        //don't bother doing the lookup (conceptNodes are created whenever a tree expand/collapse takes place - most of the time
        //the value hasn't changed....
        if (c_ != null && c_.getDesc() != null && c_.getUuid() != null && c_.getSctid() != null 
                && c_.getDesc().length() > 0 && c_.getUuid().length() > 0 && cb_.getValue().equals(c_.getSctid() + ""))
        {
            return;
        }
                
        waitForLookupToComplete();
        lookupInProgress.set(true);
        Utility.lookupSnomedIdentifier(cb_.getValue(), this);
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
                    c_.setUuid(cb_.getValue());
                    c_.setDesc(cb_.getValue().length() > 0 ? "Invalid Concept" : "");
                    isValid.set(false);
                }
                updateGUI();
            }
        });
    }
}
