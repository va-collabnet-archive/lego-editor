package gov.va.legoEdit.gui.dialogs;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.model.SchemaToString;
import gov.va.legoEdit.storage.templates.LegoTemplateManager;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CreateTemplateController implements Initializable
{
    @FXML //  fx:id="cancelButton"
    private Button cancelButton; // Value injected by FXMLLoader
    @FXML //  fx:id="legoListName"
    private TextField templateDescription; // Value injected by FXMLLoader
    @FXML //  fx:id="okButton"
    private Button okButton; // Value injected by FXMLLoader
    @FXML //  fx:id="rootPane"
    private AnchorPane rootPane; // Value injected by FXMLLoader
    @FXML //  fx:id="templateSummary"
    private TextArea templateSummary; // Value injected by FXMLLoader
    @FXML //  fx:id="title"
    private Label title; // Value injected by FXMLLoader
    @FXML //  fx:id="stackPane"
    private StackPane stackPane; // Value injected by FXMLLoader

    private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };
    private BooleanProperty formValid = new SimpleBooleanProperty(false);
    private Object template_;
    private ImageView invalidImage_;
    private Tooltip invalidDescriptionReason_;
    
    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources)
    {
        assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'CreateTemplate.fxml'.";
        assert templateDescription != null : "fx:id=\"legoListName\" was not injected: check your FXML file 'CreateTemplate.fxml'.";
        assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'CreateTemplate.fxml'.";
        assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'CreateTemplate.fxml'.";
        assert templateSummary != null : "fx:id=\"templateSummary\" was not injected: check your FXML file 'CreateTemplate.fxml'.";
        assert title != null : "fx:id=\"title\" was not injected: check your FXML file 'CreateTemplate.fxml'.";
        assert stackPane != null : "fx:id=\"stackPane\" was not injected: check your FXML file 'CreateTemplate.fxml'.";


        // initialize your logic here: all @FXML variables will have been injected
        
        invalidImage_ = Images.EXCLAMATION.createImageView();
        invalidDescriptionReason_ = new Tooltip("A description is required");
        Tooltip.install(invalidImage_, invalidDescriptionReason_);
        
        stackPane.getChildren().add(invalidImage_);
        StackPane.setAlignment(invalidImage_, Pos.CENTER_RIGHT);
        StackPane.setMargin(invalidImage_, new Insets(0.0, 5.0, 0.0, 0.0));
        
        templateDescription.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                if (newValue.length() > 0)
                {
                    for (char c : ILLEGAL_CHARACTERS)
                    {
                        if (newValue.indexOf(c) > 0)
                        {
                            formValid.set(false);
                            invalidDescriptionReason_.setText("The description contains an illegal character '" + c + "'");
                            return;
                        }
                        else if (LegoTemplateManager.getInstance().isNameInUse(newValue))
                        {
                            formValid.set(false);
                            invalidDescriptionReason_.setText("The description must be unique.  Another template already exists with that description");
                            return;
                        }
                    }
                    
                    formValid.set(true);
                }
                else
                {
                    invalidDescriptionReason_.setText("A description is required");
                    formValid.set(false);
                }
            }
        });
        
        cancelButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                ((Stage) rootPane.getScene().getWindow()).close();
            }
        });
        
        okButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                try
                {
                    LegoTemplateManager.getInstance().storeTemplate(templateDescription.getText(), template_);
                }
                catch (Exception e)
                {
                    LegoGUI.getInstance().showErrorDialog("Error storing template", e.getMessage(), e.getCause().toString());
                }
                ((Stage) rootPane.getScene().getWindow()).close();
            }
        });
        
        okButton.disableProperty().bind(formValid.not());
        invalidImage_.visibleProperty().bind(formValid.not());
    }
    
    public void setVariables(Object template)
    {
       templateDescription.setText("");
       title.setText("Create a " + template.getClass().getSimpleName() + " Template");
       templateSummary.setText(SchemaToString.toString(template));
       this.template_ = template;
    }
}
