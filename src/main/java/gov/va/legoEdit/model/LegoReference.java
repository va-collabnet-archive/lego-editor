package gov.va.legoEdit.model;

import javax.xml.datatype.XMLGregorianCalendar;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Pncs;

public class LegoReference
{
    private String legoUUID;
    private String stampUUID;
    private XMLGregorianCalendar stampTime;
    private Pncs pncs;
    
    public LegoReference(Lego lego)
    {
        this.legoUUID = lego.getLegoUUID();
        this.stampUUID = lego.getStamp().getUuid();
        this.stampTime = lego.getStamp().getTime();
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
    
    public XMLGregorianCalendar getStampTime()
    {
        return stampTime;
    }
    
    public String getUniqueId()
    {
        return ModelUtil.makeUniqueLegoID(legoUUID, stampUUID);
    }
}
