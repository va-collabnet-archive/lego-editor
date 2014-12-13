package gov.va.legoEdit.gui.legoInfoPanel;


import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.util.CopyableLabel;
import gov.va.legoEdit.gui.util.CustomClipboard;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

/**
 * 
 * LegoInfoPanelController
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
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
    private Text pncsName; // Value injected by FXMLLoader
    @FXML //  fx:id="pncsValue"
    private Label pncsValue; // Value injected by FXMLLoader

    private ContextMenu pncsNameContextMenu;

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
    
    protected void finishInit(String pncsNameString, String pncsValue, String pncsID, final String legoUUID, String author, String module, String date, String path)
    {
        this.legoAuthor.setText(author);
        this.legoDate.setText(date);
        this.legoModule.setText(module);
        this.legoPath.setText(path);
        this.legoUUID.setText(legoUUID);
        this.pncsID.setText(pncsID);
        this.pncsName.wrappingWidthProperty().bind(((Region)this.pncsName.getParent()).widthProperty().subtract(20));
        this.pncsName.setText(pncsNameString);
        // *cough* HACK *cough* - for some reason, it doesn't layout the height right unless you do this...
        this.pncsName.wrappingWidthProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                Platform.runLater(() ->    
                {
                    pncsName.getParent().requestLayout();
                });
            }
        });
        this.pncsValue.setText(pncsValue);
        
        CopyableLabel.addCopyMenu(this.legoAuthor);
        CopyableLabel.addCopyMenu(this.legoDate);
        CopyableLabel.addCopyMenu(this.legoModule);
        CopyableLabel.addCopyMenu(this.legoPath);
        CopyableLabel.addCopyMenu(this.legoUUID);
        CopyableLabel.addCopyMenu(this.pncsID);
        pncsNameContextMenu = new ContextMenu();
        MenuItem mi = new MenuItem("Copy");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                CustomClipboard.set(pncsName.getText());
                LegoGUI.getInstance().getLegoGUIController().updateRecentCodes(pncsName.getText());
            }
        });
        pncsNameContextMenu.getItems().add(mi);
        
        this.pncsName.setOnMousePressed(new EventHandler<MouseEvent>() 
        {
            @Override
            public void handle(MouseEvent event) 
            {
                if (event.isSecondaryButtonDown()) 
                {
                    pncsNameContextMenu.show(pncsName, event.getScreenX(), event.getScreenY());
                }
            };
        });
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
