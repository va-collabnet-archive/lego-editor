package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.gui.util.Images;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * InvalidNode A node injected into the tree when an item (or child) is invalid
 * @author Dan Armbrust 
 * Copyright 2013
 * 
 */
public class InvalidNode extends StackPane
{
	private Tooltip tt;
	
	public InvalidNode(String reason)
	{
		ImageView iv = new ImageView(Images.EXCLAMATION.getImage());
		iv.setFitWidth(16.0);
		iv.setFitHeight(16.0);
		tt = new Tooltip(reason);
		Tooltip.install(iv, tt);
		this.getChildren().add(iv);
		StackPane.setAlignment(iv, Pos.CENTER_LEFT);
		this.setMinHeight(20.0);
		this.setMaxHeight(20.0);
	}
	
	public void setInvalidReason(String reason)
	{
		tt.setText(reason);
	}
}
