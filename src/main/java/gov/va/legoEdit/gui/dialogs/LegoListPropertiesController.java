package gov.va.legoEdit.gui.dialogs;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.LegoGUIModel;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.WriteException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class LegoListPropertiesController implements Initializable
{
    @FXML // fx:id="rootPane"
    private AnchorPane rootPane; // Value injected by FXMLLoader
    @FXML //  fx:id="cancelButton"
    private Button cancelButton; // Value injected by FXMLLoader
    @FXML //  fx:id="legoListDescription"
    private TextField legoListDescription; // Value injected by FXMLLoader
    @FXML //  fx:id="legoListName"
    private TextField legoListName; // Value injected by FXMLLoader
    @FXML //  fx:id="legoListUUID"
    private TextField legoListUUID; // Value injected by FXMLLoader
    @FXML //  fx:id="okButton"
    private Button okButton; // Value injected by FXMLLoader
    
    StringProperty description_;
    boolean creatingNew_ = false;
    
    BooleanProperty nameValid = new SimpleBooleanProperty(false);
    BooleanProperty descValid = new SimpleBooleanProperty(false);
    BooleanBinding formValid;
    
    private static DropShadow invalidDropShadow = new DropShadow();
    static
    {
        invalidDropShadow.setColor(Color.RED);
    }
    

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources)
    {
        assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'LegoListProperties.fxml'.";
        assert legoListDescription != null : "fx:id=\"legoListDescription\" was not injected: check your FXML file 'LegoListProperties.fxml'.";
        assert legoListName != null : "fx:id=\"legoListName\" was not injected: check your FXML file 'LegoListProperties.fxml'.";
        assert legoListUUID != null : "fx:id=\"legoListUUID\" was not injected: check your FXML file 'LegoListProperties.fxml'.";
        assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'LegoListProperties.fxml'.";

        // initialize your logic here: all @FXML variables will have been injected
        
        legoListName.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                if (!creatingNew_ || (newValue.length() > 0 && BDBDataStoreImpl.getInstance().getLegoListByName(newValue) == null))
                {
                    nameValid.set(true);
                    legoListName.setEffect(null);
                }
                else
                {
                    nameValid.set(false);
                    legoListName.setEffect(invalidDropShadow);
                }
            }
        });
        
        legoListDescription.textProperty().addListener(new ChangeListener<String>()
        {
            
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                if (newValue.length() > 0)
                {
                    descValid.set(true);
                    legoListDescription.setEffect(null);
                }
                else
                {
                    descValid.set(false);
                    legoListDescription.setEffect(invalidDropShadow);
                }
            }
        });
        
        formValid = new BooleanBinding()
        {
            {
                bind(nameValid, descValid);
            }
            
            @Override
            protected boolean computeValue()
            {
                return nameValid.get() && descValid.get();
            }
        };
        
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
                description_.setValue(legoListDescription.getText());
                if (creatingNew_)
                {
                    LegoList ll = new LegoList();
                    ll.setGroupDescription(legoListDescription.getText());
                    ll.setGroupName(legoListName.getText());
                    ll.setLegoListUUID(legoListUUID.getText());
                    try
                    {
                        LegoGUIModel.getInstance().importLegoList(ll);
                    }
                    catch (WriteException e)
                    {
                        LegoGUI.getInstance().showErrorDialog("Unexpected Error", "Error creating Lego List", e.toString());
                    }
                }
                ((Stage) rootPane.getScene().getWindow()).close();
            }
        });
        
        okButton.disableProperty().bind(formValid.not());
    }
    
    public void setVariables(String name, String uuid, StringProperty description)
    {
        if (name == null || name.length() == 0)
        {
            creatingNew_ = true;
        }
        else
        {
            creatingNew_ = false;
        }
        description_ = description;
        legoListDescription.setText(description.getValue());
        legoListName.setText(name);
        legoListUUID.setText(uuid);
        legoListName.setEditable(creatingNew_);
        //TODO allow description to be editable after we get a way to save it.
        legoListDescription.setEditable(description.get().length() == 0);
    }
}
