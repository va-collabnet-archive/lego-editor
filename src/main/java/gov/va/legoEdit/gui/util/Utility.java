package gov.va.legoEdit.gui.util;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class Utility
{
    public static Node prependLabel(String label, Node node)
    {
        HBox hbox = new HBox();
        hbox.setSpacing(10.0);
        hbox.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label);
        hbox.getChildren().add(l);
        hbox.getChildren().add(node);
        HBox.setHgrow(node, Priority.ALWAYS);
        return hbox;
    }
}
