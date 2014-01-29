package gov.va.legoEdit.util;

import gov.va.legoEdit.model.ModelUtil;
import gov.va.legoEdit.model.schemaModel.Lego;
import java.util.Collection;
import java.util.HashMap;

/**
 * 
 * UnsavedLegos
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class UnsavedLegos
{
    private HashMap<String, Lego> legoIdToLego = new HashMap<String, Lego>();
    private HashMap<String, String> legoIdToLegoListId = new HashMap<String, String>();

    public void addLego(Lego lego, String legoListUUID)
    {
        String legoId = ModelUtil.makeUniqueLegoID(lego);
        legoIdToLego.put(legoId, lego);
        legoIdToLegoListId.put(legoId, legoListUUID);
    }
    
    public void removeLego(String legoUniqueId)
    {
        legoIdToLego.remove(legoUniqueId);
        legoIdToLegoListId.remove(legoUniqueId);
    }
    
    public Lego getLego(String legoUniqueId)
    {
        return legoIdToLego.get(legoUniqueId);
    }
    
    public String getLegoListIdForLego(String legoUniqueId)
    {
        return legoIdToLegoListId.get(legoUniqueId);
    }
    
    public Collection<Lego> getLegos()
    {
        return legoIdToLego.values();
    }
 }
