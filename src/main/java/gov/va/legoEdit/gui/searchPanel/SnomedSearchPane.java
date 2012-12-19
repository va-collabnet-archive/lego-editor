package gov.va.legoEdit.gui.searchPanel;

import gov.va.legoEdit.LegoGUI;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class SnomedSearchPane
{
    private Pane pane_;
    SnomedSearchPanelController controller_;
    
    public SnomedSearchPane()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            pane_ = (Pane)loader.load(SnomedSearchPane.class.getResourceAsStream("SnomedSearchPanel.fxml"));
            controller_ = loader.getController();
            pane_.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
            AnchorPane.setBottomAnchor(pane_, 0.0);
            AnchorPane.setTopAnchor(pane_, 0.0);
            AnchorPane.setLeftAnchor(pane_, 0.0);
            AnchorPane.setRightAnchor(pane_, 0.0);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unexpected", e);
        }
    }
    
    public Pane getPane()
    {
        return pane_;
    }
    
    public SnomedSearchPanelController getController()
    {
        return controller_;
    }
}
