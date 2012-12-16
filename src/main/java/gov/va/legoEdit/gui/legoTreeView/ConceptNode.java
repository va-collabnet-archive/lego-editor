package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.storage.wb.WBDataStore;
import java.util.Observable;
import java.util.UUID;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.ihtsdo.fxmodel.concept.FxConcept;
import org.ihtsdo.tk.api.coordinate.StandardViewCoordinates;

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
        // TODO deal with UUIDs
        if (c.getSctid() != null)
        {
            cb_.setValue(c.getSctid() + "");
        }
        else if (c.getUuid() != null)
        {
            cb_.setValue(c.getUuid() + "");
        }
        else
        {
            cb_.setValue("");
        }
        cb_.setEditable(true);
        cb_.setMaxWidth(Double.MAX_VALUE);
        cb_.setMinWidth(320.0);
        cb_.setPromptText("Specify or drop a Snomed SCTID or UUID");

        // TODO populate dropdown with most common values
        descriptionLabel_ = new Label((c.getDesc() == null || c.getDesc().length() == 0) ? "Enter a concept ID"
                : c.getDesc());

        cb_.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                // TODO this needs to be backgrounded as well... and add a spinner or something.
                try
                {
                    System.out.println("changed within conceptNode");
                    // TODO more work - figure out if UUID or not, ect
                    UUID conId = UUID.fromString(cb_.getValue());
                    FxConcept con = WBDataStore.Ts().getFxConcept(conId, StandardViewCoordinates.getSnomedLatest());
                    descriptionLabel_.setText(con.toString());
                    c_.setUuid(conId.toString());
                    c_.setSctid(null);
                    c_.setDesc(con.toString());
                    ConceptNode.this.setChanged();
                    ConceptNode.this.notifyObservers();
                }
                catch (Exception e)
                {
                    // TODO more work
                    descriptionLabel_.setText("Couldn't match to a snomed concept!");
                }

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
