package gov.va.legoEdit.gui.util;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * adopted from http://fxexperience.com/2012/05/listview-custom-cell-factories-and-context-menus/
 */
public class ContextMenuListCell<T> extends ListCell<T>
{
    public ContextMenuListCell(ContextMenu contextMenu)
    {
        setContextMenu(contextMenu);
    }

    public static <T> Callback<ListView<T>, ListCell<T>> forListView(ContextMenu contextMenu)
    {
        return forListView(contextMenu, null);
    }

    public static <T> Callback<ListView<T>, ListCell<T>> forListView(final ContextMenu contextMenu, final Callback<ListView<T>, ListCell<T>> cellFactory)
    {
        return new Callback<ListView<T>, ListCell<T>>()
        {
            @Override
            public ListCell<T> call(ListView<T> listView)
            {
                ListCell<T> cell = (cellFactory == null ? new DefaultListCell<T>() : cellFactory.call(listView));
                cell.setContextMenu(contextMenu);
                return cell;
            }
        };
    }
}
