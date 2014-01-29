package gov.va.legoEdit.gui.dialogs;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * 
 * YesNoDialogController
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class YesNoDialogController implements Initializable
{
    @FXML //  fx:id="rootPane"
    private AnchorPane rootPane; // Value injected by FXMLLoader
    @FXML // fx:id="icon"
    private ImageView icon; // Value injected by FXMLLoader
    @FXML// fx:id="noButton"
    private Button noButton; // Value injected by FXMLLoader
    @FXML // fx:id="question"
    private Label question; // Value injected by FXMLLoader
    @FXML// fx:id="yesButton"
    private Button yesButton; // Value injected by FXMLLoader
    
    private Answer answer = null;
    
    public enum Answer{YES, NO};

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources)
    {
        assert icon != null : "fx:id=\"icon\" was not injected: check your FXML file 'YesNoDialog.fxml'.";
        assert noButton != null : "fx:id=\"noButton\" was not injected: check your FXML file 'YesNoDialog.fxml'.";
        assert question != null : "fx:id=\"question\" was not injected: check your FXML file 'YesNoDialog.fxml'.";
        assert yesButton != null : "fx:id=\"yesButton\" was not injected: check your FXML file 'YesNoDialog.fxml'.";

        // initialize your logic here: all @FXML variables will have been injected
        
        yesButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                YesNoDialogController.this.answer = Answer.YES;
                ((Stage) rootPane.getScene().getWindow()).close();
            }
        });
        
        noButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                YesNoDialogController.this.answer = Answer.NO;
                ((Stage) rootPane.getScene().getWindow()).close();
            }
        });
    }

    public void init(String question)
    {
        this.question.setText(question);
        this.answer = null;
        noButton.requestFocus();
    }
    
    public Answer getAnswer()
    {
        return answer;
    }
    
}
