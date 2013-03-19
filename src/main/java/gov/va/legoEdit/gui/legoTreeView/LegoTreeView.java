package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.gui.util.LegoTab;
import gov.va.legoEdit.model.schemaModel.Lego;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * LegoTreeView An enhanced tree view for Lego editing 
 * @author Dan Armbrust 
 * Copyright 2013
 * 
 */
public class LegoTreeView extends TreeView<String>
{
	private LegoTab legoTab_;

	public LegoTreeView()
	{
		getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		setCellFactory(new Callback<TreeView<String>, TreeCell<String>>()
		{
			@Override
			public TreeCell<String> call(TreeView<String> arg0)
			{
				return new LegoTreeCell<String>(LegoTreeView.this);
			}
		});
		// Not going to use the edit API, not reliable. Just detect doubleclick instead.
		setEditable(false);
		LegoTreeItem treeRoot = new LegoTreeItem();
		setShowRoot(false);
		setRoot(treeRoot);
	}

	public void setLegoTab(LegoTab legoTab)
	{
		legoTab_ = legoTab;
	}

	protected Lego getLego()
	{
		if (legoTab_ != null)
		{
			return legoTab_.getLego();
		}
		return null;
	}

	/**
	 * Pass in null if there is no need to revalidate the node based on this change (useful for things like 
	 * boolean dropdowns that can never be set invalid by the editor)
	 */
	protected void contentChanged(LegoTreeItem lti)
	{
		if (legoTab_ != null)
		{
			legoTab_.contentChanged();
		}
		if (lti != null)
		{
			lti.revalidateToRootThreaded();
		}
	}
}
