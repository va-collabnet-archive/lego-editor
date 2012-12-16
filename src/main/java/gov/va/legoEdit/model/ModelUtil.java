package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Lego;

public class ModelUtil
{
    public static String makeUniqueLegoID(Lego lego)
    {
        return lego.getLegoUUID() + lego.getStamp().getUuid();
    }
}
