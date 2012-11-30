package gov.va.legoEdit.gui.util;

import java.util.Comparator;
import javafx.scene.control.TreeItem;

/**
 * @author darmbrust
 */
public class LLTreeItemComparator implements Comparator<TreeItem<String>>
{
    private AlphanumComparator ac;

    /**
     * Create a new instance of an AlphanumComparator.
     * 
     * @param caseSensitive
     */
    public LLTreeItemComparator(boolean ignoreCase)
    {
        ac = new AlphanumComparator(ignoreCase);
    }

    @Override
    public int compare(TreeItem<String> s1, TreeItem<String> s2)
    {
        //TODO redo this with sort keys hard coded into the TreeItems - so we can use sort all over, upon adds and removes, and 
        //keeps things in the order that we want.
        return ac.compare(s1.getValue(), s2.getValue());
    }
}
