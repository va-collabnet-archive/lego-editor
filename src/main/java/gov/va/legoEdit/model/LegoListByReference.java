/**
 * A LegoList representation that only keeps the identifiers necessary to fetch the Lego, rather than keeping the entire Lego in memory.
 * Used to trim the memory footprint of the GUI.
 * 
 * Note - it may be useful if the DB could return something like this - but will wait and see if it is necessary for performance or not.  
 * At the moment, I don't think it is.
 */
package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import java.util.ArrayList;
import java.util.List;

public class LegoListByReference
{
    private String groupName;
    private String legoListUUID;
    private String groupDescription;
    private List<LegoReference> lego;

    public LegoListByReference(LegoList legoList, boolean skipLegoRefs)
    {
        this.groupName = legoList.getGroupName();
        this.groupDescription = legoList.getGroupDescription();
        this.legoListUUID = legoList.getLegoListUUID();
        if (!skipLegoRefs)
        {
            for (Lego l : legoList.getLego())
            {
                getLegoReference().add(new LegoReference(l));
            }
        }
    }
    
    public String getGroupName()
    {
        return groupName;
    }

    public String getLegoListUUID()
    {
        return legoListUUID;
    }

    public String getGroupDescription()
    {
        return groupDescription;
    }

    public List<LegoReference> getLegoReference()
    {
        if (lego == null)
        {
            lego = new ArrayList<LegoReference>();
        }
        return this.lego;
    }

}
