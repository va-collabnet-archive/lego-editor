package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Pncs;

public class LegoReference
{
    private String legoUUID;
    private String stampUUID;
    private Pncs pncs;
    
    public LegoReference(Lego lego)
    {
        this.legoUUID = lego.getLegoUUID();
        this.stampUUID = lego.getStamp().getUuid();
        this.pncs = lego.getPncs();
    }

    public String getLegoUUID()
    {
        return legoUUID;
    }

    public String getStampUUID()
    {
        return stampUUID;
    }
    
    public Pncs getPncs()
    {
        return pncs;
    }
    
    
    public String getUniqueId()
    {
        return ModelUtil.makeUniqueLegoID(legoUUID, stampUUID);
    }
}
