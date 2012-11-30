package gov.va.legoEdit.gui.legoInfoPanel;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;


public class LegoInfoPanelController
    implements Initializable {

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
        
        addCopyMenu(this.legoAuthor);
        addCopyMenu(this.legoDate);
        addCopyMenu(this.legoModule);
        addCopyMenu(this.legoPath);
        addCopyMenu(this.legoUUID);
        addCopyMenu(this.pncsID);
        addCopyMenu(this.pncsName);
        addCopyMenu(this.pncsValue);
    }
    
    private void addCopyMenu(final Label label)
    {
        MenuItem mi = new MenuItem("Copy");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                ClipboardContent content = new ClipboardContent();
                content.putString(label.getText());
                Clipboard.getSystemClipboard().setContent(content);
            }
        });
        label.setContextMenu(new ContextMenu(mi));
    }
}
