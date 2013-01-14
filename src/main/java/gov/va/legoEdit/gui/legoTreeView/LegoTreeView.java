package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.gui.util.LegoTab;
import gov.va.legoEdit.model.schemaModel.Lego;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class LegoTreeView extends TreeView<String>
{
    private LegoTab legoTab_;
    
	public LegoTreeView(boolean editable)
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
		setEditable(editable);
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
	
	protected void contentChanged()
	{
	    if (legoTab_ != null)
	    {
	        legoTab_.hasUnsavedChangesProperty().invalidate();
	    }
	}
}
