package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.model.schemaModel.Lego;
import javafx.event.Event;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

public class LegoTreeView extends TreeView<String>
{
    private Lego lego_;
    
	public LegoTreeView()
	{
		getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		setCellFactory(new Callback<TreeView<String>, TreeCell<String>>()
		{
			@Override
			public TreeCell<String> call(TreeView<String> arg0)
			{
				return new LegoTreeCell<String>();
			}
		});
		setEditable(true);
		LegoTreeItem treeRoot = new LegoTreeItem();
        setShowRoot(false);
        setRoot(treeRoot);
	}
	
	public void setLego(Lego lego)
	{
	    lego_ = lego;
	}

	@Override
	public void edit(TreeItem<String> ti)
	{
	    if (ti != null)
	    {
    	    LegoTreeItem lti = (LegoTreeItem)ti;
    	    if (lti.getNodeType() != null)
    	    {
        	    if (LegoTreeNodeType.legoListLego == lti.getNodeType())
        	    {
        	        Lego l = (Lego)lti.getExtraData();
        	        LegoGUI.getInstance().getLegoGUIController().beginLegoEdit(l);
        	        //When we begin a lego edit, we want to highlight it in the legoListTree.  We need to tell the tree to redraw this tree item.
        	        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
        	    }
        	    else if (lti.getNodeType() == LegoTreeNodeType.legoList)
        	    {
// moved this to a right click menu        	        
//        	        LegoList ll = (LegoList)cti.getExtraData();
//        	        LegoGUI.getInstance().getLegoGUIController().displayXMLViewWindow(ll);
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
        
        AnchorPane.setBottomAnchor(sp, 0.0);
        AnchorPane.setTopAnchor(sp, 0.0);
        AnchorPane.setLeftAnchor(sp, 0.0);
        AnchorPane.setRightAnchor(sp, 0.0);
        return sp;
	}
	
	protected Lego getLego()
	{
	    return lego_;
	}
}
