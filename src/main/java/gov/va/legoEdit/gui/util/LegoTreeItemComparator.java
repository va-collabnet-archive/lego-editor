package gov.va.legoEdit.gui.util;

import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import java.util.Comparator;
import javafx.scene.control.TreeItem;

/**
 * 
 * LegoTreeItemComparator
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class LegoTreeItemComparator implements Comparator<TreeItem<String>>
{
    private AlphanumComparator ac;

    /**
     * Create a new instance of an AlphanumComparator.
     * 
     * @param caseSensitive
     */
    public LegoTreeItemComparator(boolean ignoreCase)
    {
        ac = new AlphanumComparator(ignoreCase);
    }

    @Override
    public int compare(TreeItem<String> s1, TreeItem<String> s2)
    {
        int r = 0;
        if (s1 instanceof LegoTreeItem && s2 instanceof LegoTreeItem)
        {
            r = ((LegoTreeItem)s1).getSortOrder() - ((LegoTreeItem)s2).getSortOrder();
        }
        
        if (r == 0)
        {
            return ac.compare(s1.getValue(), s2.getValue());
        }
        else
        {
            return r;
        }
    }
}
