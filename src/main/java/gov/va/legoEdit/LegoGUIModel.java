package gov.va.legoEdit;

import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeNodeType;
import gov.va.legoEdit.gui.util.LegoTreeItemComparator;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.ModelUtil;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.WriteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
    private ObservableList<TreeItem<String>> legoLists_ = null;

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

    public void initializeLegoListNames(ObservableList<TreeItem<String>> list, Integer pncsFilterId, String pncsFilterValue, String conceptFilter)
    {
        legoLists_ = list;
        legoLists_.clear();
        //TODO need to figure out how to store the lego list name changes - currently they get thrown away...
        
        ArrayList<Lego> legos = new ArrayList<>();
        HashMap<String, LegoListByReference> legoLists = new HashMap<>();
        
        if (pncsFilterId == null)
        {
            //no pncs filter
            if (conceptFilter == null)
            {
                //No usable filter - get all - shortcircut the lego processing, just get the lists directly.
                Iterator<LegoList> ll = BDBDataStoreImpl.getInstance().getLegoLists();
                while (ll.hasNext())
                {
                    //Throw away the legos, just keep the refs.
                    LegoList legoList = ll.next();
                    legoLists.put(legoList.getLegoListUUID(), new LegoListByReference(legoList, false));
                }  
            }
            else  //use the concept filter
            {
                legos.addAll(BDBDataStoreImpl.getInstance().getLegosContainingConcept(conceptFilter));
            }
        }
        else
        {
            //we have a pncs filter - start there.
            if (pncsFilterValue == null)
            {
                //id filter, no value
                legos.addAll(BDBDataStoreImpl.getInstance().getLegosForPncs(pncsFilterId));
            }
            else
            {
                //id filter and value
                legos.addAll(BDBDataStoreImpl.getInstance().getLegosForPncs(pncsFilterId, pncsFilterValue));
            }
            
            if (conceptFilter != null)
            {
                //Need to remove any legos that don't match the concept filter
                HashSet<String> conceptLegoKeys = new HashSet<>();
                List<Lego> conceptLegos = BDBDataStoreImpl.getInstance().getLegosContainingConcept(conceptFilter);
                for (Lego l : conceptLegos)
                {
                    conceptLegoKeys.add(ModelUtil.makeUniqueLegoID(l));
                }
                Iterator<Lego> iterator = legos.iterator();
                while (iterator.hasNext())
                {
                    if (!conceptLegoKeys.contains(ModelUtil.makeUniqueLegoID(iterator.next())))
                    {
                        iterator.remove();
                    }
                }
            }
        }
        
        if (legos.size() > 0)
        {
            //Need to work backwards from the legos now, and get the legoRefs - and wire them back together.
            assert legoLists.size() == 0;
            
            for (Lego l : legos)
            {
                //Could be more than one, usually only one in practice, however
                List<String> legoListIds = BDBDataStoreImpl.getInstance().getLegoListByLego(l.getLegoUUID());
                for (String legoListId : legoListIds)
                {
                    LegoListByReference llbr = legoLists.get(legoListId);
                    if (llbr == null)
                    {
                        llbr = new LegoListByReference(BDBDataStoreImpl.getInstance().getLegoListByID(legoListId), true);
                        legoLists.put(legoListId, llbr);
                    }
                    llbr.getLegoReference().add(new LegoReference(l));
                }
            }
        }
        
        for (LegoListByReference llbr : legoLists.values())
        {
            legoLists_.add(new LegoTreeItem(llbr));
        }

        legoLists_.add(new LegoTreeItem(LegoTreeNodeType.blankLegoListEndNode));
        FXCollections.sort(legoLists_, new LegoTreeItemComparator(true));
    }

    public void importLegoList(LegoList ll) throws WriteException
    {
        //TODO need to update the filter pncs lists...
        BDBDataStoreImpl.getInstance().importLegoList(ll);
        //Long way around to get back to the method above... but I need the filter params.
        LegoGUI.getInstance().getLegoGUIController().getLegoFilterPaneController().updateLegoList();
    }

    public void removeLegoList(LegoListByReference legoListByReference) throws WriteException
    {
        //TODO call this
        BDBDataStoreImpl.getInstance().deleteLegoList(legoListByReference.getLegoListUUID());
        legoLists_.remove(legoListByReference);
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
