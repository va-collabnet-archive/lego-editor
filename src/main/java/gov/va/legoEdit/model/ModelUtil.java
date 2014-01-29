package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Lego;

/**
 * 
 * ModelUtil
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class ModelUtil
{
    public static String makeUniqueLegoID(Lego lego)
    {
        return makeUniqueLegoID(lego.getLegoUUID(), lego.getStamp().getUuid());
    }
    
    public static String makeUniqueLegoID(String legoUUID, String stampUUID)
    {
        return legoUUID + ":" + stampUUID;
    }
}
