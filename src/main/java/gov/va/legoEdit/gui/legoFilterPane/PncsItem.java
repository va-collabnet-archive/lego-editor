package gov.va.legoEdit.gui.legoFilterPane;

import gov.va.legoEdit.model.schemaModel.Pncs;
import javafx.beans.value.ObservableObjectValue;

public class PncsItem
{
    public static String ANY = "-ANY-";
    String name;
    int id;
    ObservableObjectValue<String> displayType;
    
    public PncsItem(String name, int id, ObservableObjectValue<String> displayType)
    {
        this.name = name;
        this.id = id;
        this.displayType = displayType;
    }
    
    public PncsItem(Pncs pncs, ObservableObjectValue<String> displayType)
    {
        this.name = pncs.getName();
        this.id = pncs.getId();
        this.displayType = displayType;
    }

    @Override
    public String toString()
    {
        if (displayType.get().equals("Name") || name.equals(ANY))
        {
            return name;
        }
        else
        {
            return id + "";
        }
    }

    public String getName()
    {
        return name;
    }

    public int getId()
    {
        return id;
    }
}
