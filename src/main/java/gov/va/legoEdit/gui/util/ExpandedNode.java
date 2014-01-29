/**
 * 
 */
package gov.va.legoEdit.gui.util;

import java.util.ArrayList;
import java.util.List;

/**
 * ExpandedNode - Just a silly class to store the hierarchy of an expanded tree for later replication.  
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 * 
 */
public class ExpandedNode
{
	ArrayList<ExpandedNode> items = new ArrayList<>();
	
	public void addExpanded(ExpandedNode children)
	{
		items.add(children);
	}
	
	public void addCollapsed()
	{
		items.add(null);
	}
	
	public List<ExpandedNode> getItems()
	{
		return items;
	}
}
