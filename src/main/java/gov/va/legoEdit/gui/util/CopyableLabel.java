package gov.va.legoEdit.gui.util;

import gov.va.legoEdit.LegoGUI;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;

public class CopyableLabel extends Label
{
	public CopyableLabel()
	{
		super();
		addCopyMenu();
	}

	public CopyableLabel(String text)
	{
		super(text);
		addCopyMenu();
	}

	public CopyableLabel(String text, Node graphic)
	{
		super(text, graphic);
		addCopyMenu();
	}

	private void addCopyMenu()
	{
		addCopyMenu(this);
	}

	public static void addCopyMenu(final Label l)
	{
		MenuItem mi = new MenuItem("Copy");
		mi.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				CustomClipboard.set(l.getText());
				LegoGUI.getInstance().getLegoGUIController().updateRecentCodes(l.getText());
			}
		});
		l.setContextMenu(new ContextMenu(mi));
	}
}
