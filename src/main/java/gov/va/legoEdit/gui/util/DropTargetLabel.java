package gov.va.legoEdit.gui.util;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;

public class DropTargetLabel extends Label
{
    private String droppedValue = null;
    private ContextMenu dropContextMenu;
    
    public DropTargetLabel(String value)
    {
        super(value);
    }
    
    public void setDropContextMenu(ContextMenu cm)
    {
        this.dropContextMenu = cm;
    }
    
    public void setDroppedValue(String droppedValue)
    {
        this.droppedValue = droppedValue;
    }
    
    public String getDroppedValue()
    {
        return this.droppedValue;
    }
    
    public ContextMenu getDropContextMenu()
    {
        return dropContextMenu;
    }
}
