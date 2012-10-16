package gov.va.legoEdit;

import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.guiUtil.AlphanumComparator;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.WriteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private ObservableList<String> legoNames_ = null;

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

    public ObservableList<String> getLegoListNames()
    {
        if (legoNames_ == null)
        {
            ArrayList<String> legoListNames = new ArrayList();
            Iterator<LegoList> ll = BDBDataStoreImpl.getInstance().getLegoLists();
            while (ll.hasNext())
            {
                legoListNames.add(ll.next().getGroupName());
            }
            Collections.sort(legoListNames, new AlphanumComparator(true));
            legoNames_ = FXCollections.observableArrayList(legoListNames);
        }
        return legoNames_;
    }

    public void importLegoList(LegoList ll) throws WriteException
    {
        BDBDataStoreImpl.getInstance().importLegoList(ll);
        legoNames_.add(ll.getGroupName());
        FXCollections.sort(legoNames_, new AlphanumComparator(true));
    }

    public void removeLegoList(String legoName) throws WriteException
    {
        LegoList ll = BDBDataStoreImpl.getInstance().getLegoListByName(legoName);
        if (ll == null)
        {
            return;
        }
        BDBDataStoreImpl.getInstance().deleteLegoList(ll.getLegoListUUID());
        legoNames_.remove(legoName);
    }
}
