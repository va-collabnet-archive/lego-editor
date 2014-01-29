package gov.va.legoEdit.gui.legoInfoPanel;

import gov.va.legoEdit.LegoGUI;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

/**
 * 
 * LegoInfoPanel
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class LegoInfoPanel
{
    AnchorPane ap;
    LegoInfoPanelController lipc;
    
    public LegoInfoPanel(String pncsName, String pncsValue, String pncsID, String legoUUID, String author, String module, String date, String path)
    {
        FXMLLoader loader = new FXMLLoader();
        
        try
        {
            ap = (AnchorPane)loader.load(LegoInfoPanel.class.getResourceAsStream("LegoInfoPanel.fxml"));
        }
        catch (IOException e)
        {
            throw new RuntimeException("unexpected", e);
        }
        lipc = loader.getController();
        ap.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
        
        lipc.finishInit(pncsName, pncsValue, pncsID, legoUUID, author, module, date, path);
    }
    
    public void update(String author, String module, String date, String path)
    {
        lipc.update(author, module, date, path);
    }
    
    public AnchorPane getPane()
    {
        return ap;
    }
}
