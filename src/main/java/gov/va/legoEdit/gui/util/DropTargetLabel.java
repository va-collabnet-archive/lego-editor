package gov.va.legoEdit.gui.util;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;

public class DropTargetLabel extends Label
{
    private String droppedValue = null;
    private ContextMenu dropContextMenu;
    
    public DropTargetLabel(String value, ContextMenu standardContextMenu)
    {
        super(value);
        setContextMenu(standardContextMenu);
        dropContextMenu = new ContextMenu();
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
