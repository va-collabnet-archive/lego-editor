package gov.va.legoEdit.gui.legoInfoPanel;

import gov.va.legoEdit.LegoGUI;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

public class LegoInfoPanel
{
    public static AnchorPane build(String pncsName, String pncsValue, String pncsID, String legoUUID, String author, String module, String date, String path)
    {
        FXMLLoader loader = new FXMLLoader();
        AnchorPane ap;
        try
        {
            ap = (AnchorPane)loader.load(LegoInfoPanel.class.getResourceAsStream("LegoInfoPanel.fxml"));
        }
        catch (IOException e)
        {
            throw new RuntimeException("unexpected", e);
        }
        LegoInfoPanelController lipc = loader.getController();
        ap.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
        
        lipc.finishInit(pncsName, pncsValue, pncsID, legoUUID, author, module, date, path);
        
        return ap;
    }
}
