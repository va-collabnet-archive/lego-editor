package gov.va.legoEdit;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ErrorDialogController implements Initializable
{
    @FXML //  fx:id="detailedMessage"
    private TextArea detailedMessage; // Value injected by FXMLLoader
    @FXML //  fx:id="errorMessage"
    private Label errorMessage; // Value injected by FXMLLoader
    @FXML //  fx:id="okButton"
    private Button okButton; // Value injected by FXMLLoader

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources)
    {
        assert detailedMessage != null : "fx:id=\"detailedMessage\" was not injected: check your FXML file 'ErrorDialog.fxml'.";
        assert errorMessage != null : "fx:id=\"errorMessage\" was not injected: check your FXML file 'ErrorDialog.fxml'.";
        assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'ErrorDialog.fxml'.";

        // initialize your logic here: all @FXML variables will have been injected

        LegoGUI.setErrorDialogController(this);
        
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                LegoGUI.getErrorDialogStage().close();
            }
        });
    }
    
    public void setVariables(String errorMessage, String detailedErrorMessage)
    {
        this.errorMessage.setText(errorMessage);
        this.detailedMessage.setText(detailedErrorMessage);
    }
}
