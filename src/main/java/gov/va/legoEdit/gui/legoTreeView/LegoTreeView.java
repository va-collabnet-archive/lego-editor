package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.util.LegoTab;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.schemaModel.Lego;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

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
		setEditable(true);
		LegoTreeItem treeRoot = new LegoTreeItem();
        setShowRoot(false);
        setRoot(treeRoot);
	}
	
	public void setLegoTab(LegoTab legoTab)
	{
	    legoTab_ = legoTab;
	}

	@Override
	public void edit(TreeItem<String> ti)
	{
	    if (ti != null)
	    {
    	    LegoTreeItem lti = (LegoTreeItem)ti;
    	    if (lti.getNodeType() != null)
    	    {
        	    if (LegoTreeNodeType.legoReference == lti.getNodeType())
        	    {
        	        LegoReference lr = (LegoReference)lti.getExtraData();
        	        LegoGUI.getInstance().getLegoGUIController().beginLegoEdit(lr, lti);
        	    }
    	    }
    		super.edit(ti);
	    }
	}
	
	public ScrollPane wrapInScrollPane()
	{
	    ScrollPane sp = new ScrollPane();
        sp.setContent(this);
        sp.setFitToHeight(true);
        sp.setFitToWidth(true);
        return sp;
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
