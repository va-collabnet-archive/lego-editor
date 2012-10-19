package gov.va.legoEdit.search.PNCS;

import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.CloseableIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author darmbrust
 */
public class PncsSearchModel
{
    Logger logger = LoggerFactory.getLogger(PncsSearchModel.class);
    private static volatile PncsSearchModel instance_;
    private HashMap <Integer , SortedSet<String>> idValsMap = new HashMap();
    private Map<String, Lego> searchResultMap = new HashMap();
    private ObservableList<String> importedLegos = FXCollections.observableArrayList();
    private boolean isDisplaying = false;

    private Comparator<Pncs>  pncsComparator = new Comparator<Pncs> () {
        public int compare(Pncs o1, Pncs o2) { 
            return o1.getName().compareTo(o2.getName());
        } 
    }; 

   private List<Pncs> pncsList = new ArrayList<Pncs>() { 
        public boolean add(Pncs mt) { 
             super.add(mt); 
             Collections.sort(pncsList, pncsComparator); 
             return true; 
        } 
    };  
    
    public static PncsSearchModel getInstance()
    {
        if (instance_ == null)
        {
            synchronized (PncsSearchModel.class)
            {
                if (instance_ == null)
                {
                    instance_ = new PncsSearchModel();
                }
            }
        }
        return instance_;
    }

    private PncsSearchModel()
    {
         CloseableIterator<Pncs> pncsItr = BDBDataStoreImpl.getInstance().getPncs();
         
         
         while (pncsItr.hasNext()) {
             Pncs p = pncsItr.next();
             
             if (!idValsMap.containsKey(p.getId())) {
                 idValsMap.put(p.getId(), new TreeSet());
                 pncsList.add(p);
             }
             
             idValsMap.get(p.getId()).add(p.getValue());
         }
         
    }
    
    public List<String> getPncsIdComboList() {
        List<String> list = new ArrayList();
        
        for (Pncs p : pncsList) {
            list.add(p.getName());
        }
        
        return list;
    }
    
    public SortedSet<String> getPncsVals(int idx) {
        Pncs p = pncsList.get(idx);
        
        return idValsMap.get(p.getId());
    }
    
    public int getPncsId(int idx) {
        return pncsList.get(idx).getId();
    }

    public String getPncsName(int idx) {
        return pncsList.get(idx).getName();
    }
    void setSearchResultMap(Map<String, Lego> map) {
        searchResultMap = map;
    }
    
    public Lego getSearchResultLego(String displayStr) {
        return searchResultMap.get(displayStr);
    }

    void setImportedLegos(ObservableList<String> legoListNames) {
        importedLegos.clear();
        importedLegos.addAll(legoListNames);
    }

    public ObservableList<String> getImportedLegos() {
        return importedLegos;
    }

    public void setDisplaying(boolean val) {
        isDisplaying = val;
    }

    public boolean isDisplaying() {
        return isDisplaying;
    }

}
