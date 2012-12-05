package gov.va.legoEdit;

import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeNodeType;
import gov.va.legoEdit.gui.util.LegoTreeItemComparator;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.search.PNCS.PncsSearchModel;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.WriteException;
import java.util.Iterator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author darmbrust
 */
public class LegoGUIModel
{
    Logger logger = LoggerFactory.getLogger(LegoGUIModel.class);
    private static volatile LegoGUIModel instance_;
    private ObservableList<TreeItem<String>> legoListNames_ = null;

    public static LegoGUIModel getInstance()
    {
        if (instance_ == null)
        {
            synchronized (LegoGUIModel.class)
            {
                if (instance_ == null)
                {
                    instance_ = new LegoGUIModel();
                }
            }
        }
        return instance_;
    }

    private LegoGUIModel()
    {
    }

    public LegoList getLegoList(String legoName)
    {
        return BDBDataStoreImpl.getInstance().getLegoListByName(legoName);
    }

    public Lego getLego(String legoName)
    {
        return PncsSearchModel.getInstance().getSearchResultLego(legoName);
    }

    public void initializeLegoListNames(ObservableList<TreeItem<String>> list)
    {
        legoListNames_ = list;
        Iterator<LegoList> ll = BDBDataStoreImpl.getInstance().getLegoLists();
        while (ll.hasNext())
        {
            legoListNames_.add(new LegoTreeItem(BDBDataStoreImpl.getInstance().getLegoListByID(ll.next().getLegoListUUID())));
        }
        //zzz makes it sort to the end - isn't used in the tree display
        legoListNames_.add(new LegoTreeItem("zzz", LegoTreeNodeType.addLegoListPlaceholder));
        FXCollections.sort(legoListNames_, new LegoTreeItemComparator(true));
    }

    public void importLegoList(LegoList ll) throws WriteException
    {
        BDBDataStoreImpl.getInstance().importLegoList(ll);
        legoListNames_.add(new LegoTreeItem(ll));
        FXCollections.sort(legoListNames_, new LegoTreeItemComparator(true));
    }

    public void removeLegoList(String legoListName) throws WriteException
    {
        LegoList ll = BDBDataStoreImpl.getInstance().getLegoListByName(legoListName);
        if (ll == null)
        {
            return;
        }
        BDBDataStoreImpl.getInstance().deleteLegoList(ll.getLegoListUUID());
        legoListNames_.remove(legoListName);
    }

    public void replaceLegoList(ObservableList<String> replacements) throws WriteException
    {
     //   legoListNames_.clear();
     //   for (String s : replacements) {
    //        legoNames_.add(s);
    //    }
   //     FXCollections.sort(legoNames_, new AlphanumComparator(true));
    }
}
