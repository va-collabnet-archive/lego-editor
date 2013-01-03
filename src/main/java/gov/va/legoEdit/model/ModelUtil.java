package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Lego;

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
