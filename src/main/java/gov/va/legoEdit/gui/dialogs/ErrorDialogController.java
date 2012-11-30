package gov.va.legoEdit.gui.dialogs;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ErrorDialogController implements Initializable
{
    @FXML // fx:id="rootPane"
    private AnchorPane rootPane; // Value injected by FXMLLoader
    @FXML // fx:id="detailedMessage"
    private TextArea detailedMessage; // Value injected by FXMLLoader
    @FXML // fx:id="errorMessage"
    private Label errorMessage; // Value injected by FXMLLoader
    @FXML // fx:id="okButton"
    private Button okButton; // Value injected by FXMLLoader
    @FXML// fx:id="titleError"
    private Label titleError; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources)
    {
        assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'ErrorDialog.fxml'.";
        assert detailedMessage != null : "fx:id=\"detailedMessage\" was not injected: check your FXML file 'ErrorDialog.fxml'.";
        assert errorMessage != null : "fx:id=\"errorMessage\" was not injected: check your FXML file 'ErrorDialog.fxml'.";
        assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'ErrorDialog.fxml'.";

        // initialize your logic here: all @FXML variables will have been injected

        okButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                ((Stage) rootPane.getScene().getWindow()).close();
            }
        });
    }

    public void setVariables(String titleMessage, String errorMessage, String detailedErrorMessage)
    {
        this.titleError.setText(titleMessage);
        this.errorMessage.setText(errorMessage);
        this.detailedMessage.setText(detailedErrorMessage);
    }
}
