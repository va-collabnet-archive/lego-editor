package gov.va.legoEdit.gui.legoFilterPane;

import gov.va.legoEdit.LegoGUIModel;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeView;
import gov.va.legoEdit.gui.util.AlphanumComparator;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.CloseableIterator;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class LegoFilterPaneController  implements Initializable {

    @FXML //  fx:id="borderPane"
    private BorderPane borderPane; // Value injected by FXMLLoader
    @FXML //  fx:id="pncsItem"
    private ComboBox<PncsItem> pncsItem; // Value injected by FXMLLoader
    @FXML //  fx:id="pncsNameOrId"
    private ComboBox<String> pncsNameOrId; // Value injected by FXMLLoader
    @FXML //  fx:id="pncsValue"
    private ComboBox<String> pncsValue; // Value injected by FXMLLoader
    @FXML //  fx:id="snomedId"
    private TextField snomedId; // Value injected by FXMLLoader
    @FXML //  fx:id="snomedLabel"
    private Label snomedLabel; // Value injected by FXMLLoader
    
    private LegoTreeView ltv;
    private volatile AtomicInteger updateDisabled = new AtomicInteger(0);  //Update will only run when this is 0 

    public static LegoFilterPaneController init()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.load(LegoFilterPaneController.class.getResourceAsStream("LegoFilterPane.fxml"));
            return loader.getController();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unexpected", e);
        }
    }
    
    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) 
    {
        assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'Untitled 1'.";
        assert pncsItem != null : "fx:id=\"pncsItem\" was not injected: check your FXML file 'Untitled 1'.";
        assert pncsNameOrId != null : "fx:id=\"pncsNameOrId\" was not injected: check your FXML file 'Untitled 1'.";
        assert pncsValue != null : "fx:id=\"pncsValue\" was not injected: check your FXML file 'Untitled 1'.";

        // initialize your logic here: all @FXML variables will have been injected

        ltv = new LegoTreeView();
        borderPane.setCenter(ltv.wrapInScrollPane());
        AnchorPane.setBottomAnchor(borderPane, 0.0);
        AnchorPane.setTopAnchor(borderPane, 0.0);
        AnchorPane.setLeftAnchor(borderPane, 0.0);
        AnchorPane.setRightAnchor(borderPane, 0.0);
        
        pncsValue.setDisable(true);

        pncsNameOrId.getItems().add("Name");
        pncsNameOrId.getItems().add("Id");
        pncsNameOrId.getSelectionModel().select(0);
        
        pncsNameOrId.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                updateDisabled.incrementAndGet();
                //TODO ugly hack cause I can't figure out how to tell it to redo the items...
                PncsItem selected = pncsItem.getSelectionModel().getSelectedItem();
                ObservableList<PncsItem> items = FXCollections.observableArrayList();
                items.addAll(pncsItem.getItems());
                pncsItem.getItems().clear();
                pncsItem.getItems().addAll(items);
                FXCollections.sort(pncsItem.getItems(), new PncsItemComparator());
                pncsItem.getSelectionModel().select(selected);
                updateDisabled.decrementAndGet();
            }
        });
        
        CloseableIterator<Pncs> iterator = BDBDataStoreImpl.getInstance().getPncs();
        HashMap<String, PncsItem> unique = new HashMap<>();
        while (iterator.hasNext())
        {
            Pncs pncs = iterator.next();
            unique.put(pncs.getName() + pncs.getId(), new PncsItem(pncs, pncsNameOrId.valueProperty()));
        }
        pncsItem.getItems().addAll(new PncsItem(PncsItem.ANY, -1, pncsNameOrId.valueProperty()));
        pncsItem.getItems().addAll(unique.values());
        FXCollections.sort(pncsItem.getItems(), new PncsItemComparator());
        pncsItem.getSelectionModel().select(0);
        
        pncsItem.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                PncsItem item = pncsItem.getValue();
                if (item != null)
                {
                    updateDisabled.incrementAndGet();
                    if (item.getName().equals(PncsItem.ANY))
                    {
                        pncsValue.setDisable(true);
                        pncsValue.getItems().clear();
                    }
                    else
                    {
                        pncsValue.setDisable(false);
                        pncsValue.getItems().clear();
                        List<Pncs> items = BDBDataStoreImpl.getInstance().getPncs(pncsItem.getValue().getId());
                        for (Pncs pncs : items)
                        {
                            pncsValue.getItems().add(pncs.getValue());
                        }
                        FXCollections.sort(pncsValue.getItems(), new AlphanumComparator(true));
                        pncsValue.getItems().add(0, PncsItem.ANY);
                        pncsValue.getSelectionModel().select(0);
                    }
                    updateDisabled.decrementAndGet();
                    updateLegoList();
                }
            }
        });
        
        pncsValue.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                updateLegoList();
            }
        });
        
        snomedId.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                if (oldValue.length() > 0 && newValue.length() == 0)
                {
                    snomedLabel.setText("No Snomed concept entered");
                    updateLegoList();
                }
                
                if (newValue.length() > 0)
                {
                    //TODO lookup snomed
                    //TODO validator
                    snomedLabel.setText("Description goes here");
                    //TODO only update if valid
                    updateLegoList();
                }
            }
        });
        
        updateLegoList();
    }
    
    public synchronized void filterOnConcept(String conceptId)
    {
        updateDisabled.incrementAndGet();
        pncsItem.getSelectionModel().select(0);
        snomedId.setText(conceptId);
        updateDisabled.decrementAndGet();
        updateLegoList();
        
    }
    
    public void updateLegoList()
    {
        if (updateDisabled.get() > 0)
        {
            return;
        }
        
        Integer pncsFilterId = null;
        String pncsFilterValue = null;
        String conceptFilter = null;
        if (!pncsItem.getSelectionModel().getSelectedItem().getName().equals(PncsItem.ANY))
        {
            pncsFilterId = pncsItem.getSelectionModel().getSelectedItem().getId();
        }
        if (!pncsValue.isDisable() && !pncsValue.getSelectionModel().getSelectedItem().equals(PncsItem.ANY))
        {
            pncsFilterValue = pncsValue.getSelectionModel().getSelectedItem();
        }
        if (snomedId.getText().length() > 0)
        {
            conceptFilter = snomedId.getText();
        }
        
        LegoGUIModel.getInstance().initializeLegoListNames(ltv.getRoot().getChildren(), pncsFilterId, pncsFilterValue, conceptFilter);
    }
    
    public BorderPane getBorderPane()
    {
        return borderPane;
    }
}
