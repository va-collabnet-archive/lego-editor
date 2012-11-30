package gov.va.legoEdit.gui.dialogs;


import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.util.TimeConvert;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;



public class CreateLegoController
    implements Initializable {

    @FXML //  fx:id="cancelButton"
    private Button cancelButton; // Value injected by FXMLLoader
    @FXML //  fx:id="legoListName"
    private Label legoListName; // Value injected by FXMLLoader
    @FXML //  fx:id="okButton"
    private Button okButton; // Value injected by FXMLLoader
    @FXML //  fx:id="pncsID"
    private TextField pncsID; // Value injected by FXMLLoader
    @FXML //  fx:id="pncsName"
    private TextField pncsName; // Value injected by FXMLLoader
    @FXML //  fx:id="pncsValue"
    private TextField pncsValue; // Value injected by FXMLLoader
    @FXML //  fx:id="rootPane"
    private AnchorPane rootPane; // Value injected by FXMLLoader

    private LegoList ll;
    private DropShadow ds;
    private LegoTreeItem legoTreeItem;
    
    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources)
    {
        assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'CreateLegoPanel.fxml'.";
        assert legoListName != null : "fx:id=\"legoList\" was not injected: check your FXML file 'CreateLegoPanel.fxml'.";
        assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'CreateLegoPanel.fxml'.";
        assert pncsID != null : "fx:id=\"pncsID\" was not injected: check your FXML file 'CreateLegoPanel.fxml'.";
        assert pncsName != null : "fx:id=\"pncsName\" was not injected: check your FXML file 'CreateLegoPanel.fxml'.";
        assert pncsValue != null : "fx:id=\"pncsValue\" was not injected: check your FXML file 'CreateLegoPanel.fxml'.";

        ds = new DropShadow();
        ds.setColor(Color.RED);
        
        pncsName.setPromptText("The PNCS Name");
        pncsValue.setPromptText("The PNCS Value");
        pncsID.setPromptText("The PNCS ID (must be an integer)");
        
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
                Lego l = new Lego();
                Pncs pncs = new Pncs();
                pncs.setName(pncsName.getText());
                pncs.setValue(pncsValue.getText());
                pncs.setId(Integer.parseInt(pncsID.getText())); 
                l.setPncs(pncs);
                
                Stamp s = new Stamp();
                s.setAuthor("author"); //TODO get the stamp details
                s.setModule("module");
                s.setPath("path");
                s.setStatus("Active");
                s.setTime(TimeConvert.convert(System.currentTimeMillis()));
                s.setUuid(UUID.randomUUID().toString());
                l.setStamp(s);
                
                l.setLegoUUID(UUID.randomUUID().toString());
                
                Assertion a = new Assertion();
                a.setAssertionUUID(UUID.randomUUID().toString());
                l.getAssertion().add(a);
                
                ll.getLego().add(l);
                //TODO this is ugly, need to find a better way.
                legoTreeItem.getChildren().clear();
                legoTreeItem.buildPNCSChildren();
                ll = null;
                legoTreeItem = null;
                //TODO build lego...kick the right tree for update...
                ((Stage) rootPane.getScene().getWindow()).close();
            }
        });
        
        final ArrayList<TextFieldValidator> validators = new ArrayList<>();
        validators.add(new TextFieldValidator(pncsID, true));
        validators.add(new TextFieldValidator(pncsName, false));
        validators.add(new TextFieldValidator(pncsValue, false));
        
        BooleanBinding bb = new BooleanBinding()
        {
            {
                for (TextFieldValidator tfv : validators)
                {
                    super.bind(tfv.valid);
                }
            }
            
            @Override
            protected boolean computeValue()
            {
                for (TextFieldValidator tfv : validators)
                {
                    if (!tfv.isValid().get())
                    {
                        return false;
                    }
                }
                return true;
            }
        };
        
        okButton.disableProperty().bind(bb.not());
    }
    
    public void init(LegoList ll, LegoTreeItem lti)
    {
        this.ll = ll;
        this.legoTreeItem = lti;
        legoListName.setText(ll.getGroupDescription());
        pncsID.setText("");
        pncsName.setText("");
        pncsValue.setText("");
    }
    
    private class TextFieldValidator
    {
        private BooleanProperty valid = new SimpleBooleanProperty(true);
        private TextField textField;
        
        public TextFieldValidator(TextField tf, final boolean intOnly)
        {
            textField = tf;
            valid.addListener(new ChangeListener<Boolean>()
            {                
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                {
                    if (valid.get())
                    {
                        textField.setEffect(null);
                    }
                    else
                    {
                        textField.setEffect(ds);
                    }
                }
            });
            
            valid.set(textField.getText().length() > 0);
            textField.textProperty().addListener(new ChangeListener<String>()
            {

                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
                {
                    if (newValue.length() > 0)
                    {
                        if (intOnly)
                        {
                            try
                            {
                                Integer.parseInt(newValue);
                                valid.set(true);
                            }
                            catch (NumberFormatException e)
                            {
                                valid.set(false);
                            }
                        }
                        else
                        {
                            valid.set(true);
                        }
                    }
                    else
                    {
                        valid.set(false);
                    }
                }
            });
        }
        
        public BooleanProperty isValid()
        {
            return valid;
        }
    }
   
}
