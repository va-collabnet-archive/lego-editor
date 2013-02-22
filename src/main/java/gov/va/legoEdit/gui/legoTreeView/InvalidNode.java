package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.gui.util.Images;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

/**
 * InvalidNode A node injected into the tree when an item (or child) is invalid
 * @author Dan Armbrust 
 * Copyright 2013
 * 
 */
public class InvalidNode extends ImageView
{
	private Tooltip tt;
	
	public InvalidNode(String reason)
	{
		super(Images.EXCLAMATION.getImage());
		setFitWidth(16.0);
		setFitHeight(16.0);
		tt = new Tooltip(reason);
		Tooltip.install(this, tt);
	}
	
	public void setInvalidReason(String reason)
	{
		tt.setText(reason);
	}
}
