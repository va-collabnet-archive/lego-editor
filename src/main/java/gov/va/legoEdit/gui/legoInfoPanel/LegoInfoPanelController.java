package gov.va.legoEdit.gui.legoInfoPanel;


import gov.va.legoEdit.gui.util.CopyableLabel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;


public class LegoInfoPanelController implements Initializable {

    @FXML //  fx:id="legoAuthor"
    private Label legoAuthor; // Value injected by FXMLLoader
    @FXML //  fx:id="legoDate"
    private Label legoDate; // Value injected by FXMLLoader
    @FXML //  fx:id="legoModule"
    private Label legoModule; // Value injected by FXMLLoader
    @FXML //  fx:id="legoPath"
    private Label legoPath; // Value injected by FXMLLoader
    @FXML //  fx:id="legoUUID"
    private Label legoUUID; // Value injected by FXMLLoader
    @FXML //  fx:id="pncsID"
    private Label pncsID; // Value injected by FXMLLoader
    @FXML //  fx:id="pncsName"
    private Label pncsName; // Value injected by FXMLLoader
    @FXML //  fx:id="pncsValue"
    private Label pncsValue; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources)
    {
        assert legoAuthor != null : "fx:id=\"legoAuthor\" was not injected: check your FXML file 'LegoInfoPanel.fxml'.";
        assert legoDate != null : "fx:id=\"legoDate\" was not injected: check your FXML file 'LegoInfoPanel.fxml'.";
        assert legoModule != null : "fx:id=\"legoModule\" was not injected: check your FXML file 'LegoInfoPanel.fxml'.";
        assert legoPath != null : "fx:id=\"legoPath\" was not injected: check your FXML file 'LegoInfoPanel.fxml'.";
        assert legoUUID != null : "fx:id=\"legoUUID\" was not injected: check your FXML file 'LegoInfoPanel.fxml'.";
        assert pncsID != null : "fx:id=\"pncsID\" was not injected: check your FXML file 'LegoInfoPanel.fxml'.";
        assert pncsName != null : "fx:id=\"pncsName\" was not injected: check your FXML file 'LegoInfoPanel.fxml'.";
        assert pncsValue != null : "fx:id=\"pncsValue\" was not injected: check your FXML file 'LegoInfoPanel.fxml'.";
        // initialize your logic here: all @FXML variables will have been injected
    }
    
    protected void finishInit(String pncsName, String pncsValue, String pncsID, final String legoUUID, String author, String module, String date, String path)
    {
        this.legoAuthor.setText(author);
        this.legoDate.setText(date);
        this.legoModule.setText(module);
        this.legoPath.setText(path);
        this.legoUUID.setText(legoUUID);
        this.pncsID.setText(pncsID);
        this.pncsName.setText(pncsName);
        this.pncsValue.setText(pncsValue);
        
        CopyableLabel.addCopyMenu(this.legoAuthor);
        CopyableLabel.addCopyMenu(this.legoDate);
        CopyableLabel.addCopyMenu(this.legoModule);
        CopyableLabel.addCopyMenu(this.legoPath);
        CopyableLabel.addCopyMenu(this.legoUUID);
        CopyableLabel.addCopyMenu(this.pncsID);
        CopyableLabel.addCopyMenu(this.pncsName);
        CopyableLabel.addCopyMenu(this.pncsValue);
    }
    
    protected void update(String author, String module, String date, String path)
    {
        this.legoAuthor.setText(author);
        this.legoDate.setText(date);
        this.legoModule.setText(module);
        this.legoPath.setText(path);
    }
}
