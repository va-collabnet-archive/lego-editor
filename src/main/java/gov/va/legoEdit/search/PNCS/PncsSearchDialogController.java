/**
 * Sample Skeleton for "PNCSSearchDialog.fxml" Controller Class
 * You can copy and paste this code into your favorite IDE
 **/

package gov.va.legoEdit.search.PNCS;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.LegoGUIModel;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.WriteException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.WindowEvent;


public class PncsSearchDialogController
    implements Initializable {

    @FXML //  fx:id="PNCSIdSelector"
    private ComboBox<String> PNCSIdSelector; // Value injected by FXMLLoader

    @FXML //  fx:id="PNCSSearchPane"
    private AnchorPane PNCSSearchPane; // Value injected by FXMLLoader

    @FXML //  fx:id="PNCSValueSelector"
    private ComboBox<String> PNCSValueSelector; // Value injected by FXMLLoader

    @FXML //  fx:id="SearchByPncsMessage"
    private Label SearchByPncsMessage; // Value injected by FXMLLoader

    @FXML //  fx:id="okPncsButton"
    private Button okPncsButton; // Value injected by FXMLLoader

    @FXML //  fx:id="searchBoxCancelButton"
    private Button searchBoxCancelButton; // Value injected by FXMLLoader

    private String selectedPncsName;
    private String selectedVal;
    private int selectedPncsId;
    
    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert PNCSIdSelector != null : "fx:id=\"PNCSIdSelector\" was not injected: check your FXML file 'PNCSSearchDialog.fxml'.";
        assert PNCSSearchPane != null : "fx:id=\"PNCSSearchPane\" was not injected: check your FXML file 'PNCSSearchDialog.fxml'.";
        assert PNCSValueSelector != null : "fx:id=\"PNCSValueSelector\" was not injected: check your FXML file 'PNCSSearchDialog.fxml'.";
        assert SearchByPncsMessage != null : "fx:id=\"SearchByPncsMessage\" was not injected: check your FXML file 'PNCSSearchDialog.fxml'.";
        assert okPncsButton != null : "fx:id=\"okPncsButton\" was not injected: check your FXML file 'PNCSSearchDialog.fxml'.";
        assert searchBoxCancelButton != null : "fx:id=\"searchBoxCancelButton\" was not injected: check your FXML file 'PNCSSearchDialog.fxml'.";
        
        // initialize your logic here: all @FXML variables will have been injected

        LegoGUI.setPNCSSearchDialogController(this);
                
        okPncsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                List<String> items = new ArrayList();
                Map<String, Lego> searchResultMap = new HashMap();
                
                List<Lego> legoSearchResultList = BDBDataStoreImpl.getInstance().getLegosForPncs(selectedPncsId, selectedVal);
                for (Lego l : legoSearchResultList) {
                    String displayStr = "PNCS: " + selectedPncsName + " Val: " + selectedVal ;
                    items.add(displayStr);
                    searchResultMap.put(displayStr, l);
                   
                }
                
                PncsSearchModel.getInstance().setSearchResultMap(searchResultMap);
                
                ObservableList<String> options = FXCollections.observableArrayList(items);
                
                if (!isDisplaying()) {
                    PncsSearchModel.getInstance().setImportedLegos(LegoGUIModel.getInstance().getLegoListNames());
                }
                
                try {
                    LegoGUIModel.getInstance().replaceLegoList(options);
                } catch (WriteException ex) {
                    Logger.getLogger(PncsSearchDialogController.class.getName()).log(Level.SEVERE, null, ex);
                }
                PncsSearchModel.getInstance().setDisplaying(true);

                LegoGUI.getPNCSSearchDialogStage().close();
                
            }
    
        });
//        
//            LegoGUI.getPNCSSearchDialogStage().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>()
//            {
//                @Override
//                public void handle(WindowEvent event)
//                {
//                    PNCSValueSelector.setEditable(true);
//                    BDBDataStoreImpl.getInstance().shutdown();
//                }
//            });
                    

            searchBoxCancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                LegoGUI.getPNCSSearchDialogStage().close();
            }
        });
    }
    
    public void setVariables(List<String> PncsIdList)
    {
        PNCSIdSelector.setEditable(true);
        okPncsButton.setDisable(false);
        ObservableList<String> options = FXCollections.observableArrayList(PncsIdList);
        this.PNCSIdSelector.setItems(options);
        PNCSIdSelector.setEditable(false);
        okPncsButton.setDisable(true);
    }
    

    // Handler for ComboBox[fx:id="PNCSIdSelector"] onAction
    public void handleId(ActionEvent event) {
        int idx = this.PNCSIdSelector.getSelectionModel().getSelectedIndex();
        
        if (idx >= 0) {
            SortedSet<String> vals = PncsSearchModel.getInstance().getPncsVals(idx);
            ObservableList<String> options = FXCollections.observableArrayList(vals);
            PNCSValueSelector.setItems(options);        
            PNCSValueSelector.setEditable(false);
        } else {
            PNCSValueSelector.setEditable(true);
        }
    }
    // Handler for ComboBox[fx:id="PNCSValueSelector"] onAction
    public void handleValue(ActionEvent event) {
        if (this.PNCSIdSelector.getSelectionModel().getSelectedIndex() >= 0) {
            selectedPncsId = PncsSearchModel.getInstance().getPncsId(this.PNCSIdSelector.getSelectionModel().getSelectedIndex());
            selectedPncsName = PncsSearchModel.getInstance().getPncsName(this.PNCSIdSelector.getSelectionModel().getSelectedIndex());
            selectedVal = this.PNCSValueSelector.getSelectionModel().getSelectedItem();
            okPncsButton.setDisable(false);
        } else {
            PNCSValueSelector.setEditable(true);
        }
    }

    public boolean isDisplaying() {
        return PncsSearchModel.getInstance().isDisplaying();
    }
}
    
