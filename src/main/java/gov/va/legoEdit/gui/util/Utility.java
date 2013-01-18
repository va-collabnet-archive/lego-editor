package gov.va.legoEdit.gui.util;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

public class Utility
{
    public static DropShadow redDropShadow = new DropShadow();
    public static DropShadow greenDropShadow = new DropShadow();
    public static DropShadow lightGreenDropShadow = new DropShadow();
    static
    {
        redDropShadow.setColor(Color.RED);
        greenDropShadow.setColor(Color.GREEN);
        lightGreenDropShadow.setColor(Color.LIGHTGREEN);
    }
    
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
    
    public static void expandAll(TreeItem<?> ti)
    {
        ti.setExpanded(true);
        for (TreeItem<?> tiChild : ti.getChildren())
        {
            expandAll(tiChild);
        }
    }
    
    public static void expandParents(TreeItem<?> ti)
    {
        TreeItem<?> parent = ti.getParent();
        if (parent != null)
        {
            ti.getParent().setExpanded(true);
            expandParents(parent);
        }
    }
    
}
