package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Pncs;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

public class LegoReference
{
    private String legoUUID;
    private String stampUUID;
    private XMLGregorianCalendar stampTime;
    private Pncs pncs;
    private boolean isNew = false;
    
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
    
    public void setIsNew(boolean isNew)
    {
        this.isNew = isNew;
    }
    
    public boolean isNew()
    {
        return isNew;
    }
    
    public static List<LegoReference> convert(Collection<Lego> legos, boolean unsaved)
    {
        List<LegoReference> result = convert(legos);
        if (unsaved)
        {
            for (LegoReference lr : result)
            {
                lr.setIsNew(true);
            }
        }
        return result;
    }
    
    public static List<LegoReference> convert(Collection<Lego> legos)
    {
        ArrayList<LegoReference> result = new ArrayList<>(legos.size());
        for (Lego l : legos)
        {
            result.add(new LegoReference(l));
        }
        return result;
    }
}
