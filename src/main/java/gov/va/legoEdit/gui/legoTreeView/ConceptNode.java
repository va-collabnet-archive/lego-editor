package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.storage.wb.Utility;
import java.util.Observable;
import java.util.UUID;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ConceptNode extends Observable
{
    VBox vbox_;
    ComboBox<String> cb_;
    Label descriptionLabel_;
    Label contentLabel_;
    Concept c_;

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

        update();
        
        cb_.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                // TODO this needs to be backgrounded as well... and add a spinner or something.

                c_ = Utility.lookupSnomedIdentifier(newValue);
                update();
                ConceptNode.this.setChanged();
                ConceptNode.this.notifyObservers();
            }
        });

        LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(lego, cb_);
        if (contentLabel_ != null)
        {
            vbox_.getChildren().add(contentLabel_);
            VBox.setMargin(cb_, new Insets(0, 0, 0, 10));
            VBox.setMargin(descriptionLabel_, new Insets(0, 0, 0, 15));
        }
        vbox_.getChildren().add(cb_);
        vbox_.getChildren().add(descriptionLabel_);
        
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
                Concept c = c_;
                if (c.getUuid() == null)
                {
                    c = Utility.lookupSnomedIdentifier(c_.getSctid() + "");
                }
                LegoGUI.getInstance().showSnomedConceptDialog(UUID.fromString(c.getUuid()));
            }
        });
        
        //Would like to do this, as well, but can't, cause javafx doesn't let you get to this menu (yet - its a bug)
        //cb_.getEditor().getContextMenu().getItems().add(0, mi);
        
        descriptionLabel_.setContextMenu(new ContextMenu(mi));
        
    }
    
    private void update()
    {
        if (c_ != null)
        {
            descriptionLabel_.setText(c_.getDesc() == null || c_.getDesc().length() == 0 ? "Enter a Snomed Identifier" : c_.getDesc());
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
            cb_.setEffect(null);
        }
        else
        {
            DropShadow ds = new DropShadow();
            ds.setColor(Color.RED);
            cb_.setEffect(ds);
            descriptionLabel_.setText("Couldn't match to a snomed concept!");
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
}
