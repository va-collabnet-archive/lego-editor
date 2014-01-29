package gov.va.legoEdit.gui.util;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;

/**
 * 
 * DropTargetLabel
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class DropTargetLabel extends Label
{
	private String droppedValue = null;
	private ContextMenu dropContextMenu;

	public DropTargetLabel(String value, ContextMenu standardContextMenu)
	{
		super(value);
		setContextMenu(standardContextMenu);
		dropContextMenu = new ContextMenu();
		setMinWidth(USE_PREF_SIZE);
	}

	public void setDroppedValue(String droppedValue)
	{
		this.droppedValue = droppedValue;
	}

	public String getDroppedValue()
	{
		return this.droppedValue;
	}

	public ContextMenu getDropContextMenu()
	{
		return dropContextMenu;
	}
}
