package gov.va.legoEdit.gui.util;

import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
/**
 * 
 * Utility
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
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

	public static Node prependLabel(String label, Node node, double spacing)
	{
		HBox hbox = new HBox();
		hbox.setSpacing(spacing);
		hbox.setAlignment(Pos.CENTER_LEFT);
		Label l = new Label(label);
		l.setAlignment(Pos.CENTER_LEFT);
		l.setMaxHeight(Double.MAX_VALUE);
		l.setMinWidth(Label.USE_PREF_SIZE);
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

	public static void setupErrorImage(StackPane stack, BooleanBinding visible, Tooltip tooltip, double offsetFromRight)
	{
		ImageView image = Images.EXCLAMATION.createImageView();
		image.visibleProperty().bind(visible);
		Tooltip.install(image, tooltip);
		stack.getChildren().add(image);
		StackPane.setAlignment(image, Pos.CENTER_RIGHT);
		StackPane.setMargin(image, new Insets(0.0, offsetFromRight, 0.0, 0.0));
	}

	public static ExpandedNode buildExpandedNodeHierarchy(TreeItem<?> treeItem)
	{
		//If this is called, assume the called node is expanded.  Only care about children.
		ExpandedNode en = new ExpandedNode();
		for (TreeItem<?> ti : treeItem.getChildren())
		{
			if (ti.isExpanded())
			{
				en.addExpanded(buildExpandedNodeHierarchy(ti));
			}
			else
			{
				en.addCollapsed();
			}
		}
		return en;
	}
	
	public static void setExpandedStates(ExpandedNode expandedNode, TreeItem<?> treeItem)
	{
		for (int i = 0; i < expandedNode.getItems().size(); i++)
		{
			//If the structure changed, just have to ignore
			//maybe in the future, try to keep track of ids or something, so we can better align
			if (treeItem.getChildren().size() >  i)
			{
				if (expandedNode.getItems().get(i) != null)
				{
					treeItem.getChildren().get(i).setExpanded(true);
					setExpandedStates(expandedNode.getItems().get(i), treeItem.getChildren().get(i));
				}
				else
				{
					treeItem.getChildren().get(i).setExpanded(false);
				}
			}
		}
	}
}
