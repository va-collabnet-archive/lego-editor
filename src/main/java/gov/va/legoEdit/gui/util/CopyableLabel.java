package gov.va.legoEdit.gui.util;

import gov.va.legoEdit.LegoGUI;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class CopyableLabel extends Label
{
    public CopyableLabel()
    {
        super();
        addMenu();
    }
    
    public CopyableLabel(String text)
    {
        super(text);
        addMenu();
    }
    
    public CopyableLabel(String text, Node graphic)
    {
        super(text, graphic);
        addMenu();
    }
    
    private void addMenu()
    {
       addCopyMenu(this);
    }
    
    public static void addCopyMenu(final Label l)
    {
        MenuItem mi = new MenuItem("Copy");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                ClipboardContent content = new ClipboardContent();
                content.putString(l.getText());
                Clipboard.getSystemClipboard().setContent(content);
                LegoGUI.getInstance().getLegoGUIController().updateRecentCodes(l.getText());
            }
        });
        l.setContextMenu(new ContextMenu(mi));
    }
}
