package gov.va.legoEdit.gui.legoFilterPane;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.LegoGUIModel;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeView;
import gov.va.legoEdit.gui.util.AlphanumComparator;
import gov.va.legoEdit.gui.util.Utility;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.CloseableIterator;
import gov.va.legoEdit.storage.wb.WBUtility;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @FXML //  fx:id="clearButton"
    private Button clearButton; // Value injected by FXMLLoader
    
    private LegoTreeView ltv;
    private volatile AtomicInteger updateDisabled = new AtomicInteger(0);  //Update will only run when this is 0 
    private Concept concept_;

    Logger logger = LoggerFactory.getLogger(LegoFilterPaneController.class);
    
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
        borderPane.setCenter(ltv);
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
                //UGLY HACK cause I can't figure out how to tell the combo box that the value of the items it is displaying changed.
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
        
        loadPncs();
        
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
        
        final BooleanProperty snomedIdValid = new SimpleBooleanProperty(true);
        snomedIdValid.addListener(new ChangeListener<Boolean>()
        {
            
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (snomedIdValid.get())
                {
                    snomedId.setEffect(null);
                }
                else
                {
                    snomedId.setEffect(Utility.redDropShadow);
                }
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
                    snomedIdValid.set(true);
                    concept_ = null;
                    updateLegoList();
                }
                
                if (newValue.length() > 0)
                {
                    concept_ = WBUtility.lookupSnomedIdentifier(newValue);
                    if (concept_ != null)
                    {
                        if (concept_.getSctid() != null)
                        {
                            snomedId.setText(concept_.getSctid() + "");
                        }
                        snomedLabel.setText(concept_.getDesc());
                        snomedIdValid.set(true);
                        updateLegoList();
                    }
                    else
                    {
                        concept_ = null;
                        snomedIdValid.set(false);
                        snomedLabel.setText("Cannot find Snomed Concept");
                    }
                }
            }
        });
        
        LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(snomedId);
        
        clearButton.setOnAction(new EventHandler<ActionEvent>()
        {
            
            @Override
            public void handle(ActionEvent event)
            {
                updateDisabled.incrementAndGet();
                pncsItem.getSelectionModel().select(0);
                snomedId.setText("");
                updateDisabled.decrementAndGet();
                updateLegoList();
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
        LegoGUI.getInstance().getLegoGUIController().showLegoLists();
    }
    
    public void updateLegoList()
    {
        if (updateDisabled.get() > 0)
        {
            return;
        }

        Integer pncsFilterId = null;
        String pncsFilterValue = null;
        if (!pncsItem.getSelectionModel().getSelectedItem().getName().equals(PncsItem.ANY))
        {
            pncsFilterId = pncsItem.getSelectionModel().getSelectedItem().getId();
        }
        if (!pncsValue.isDisable() && !pncsValue.getSelectionModel().getSelectedItem().equals(PncsItem.ANY))
        {
            pncsFilterValue = pncsValue.getSelectionModel().getSelectedItem();
        }
        
        LegoGUIModel.getInstance().initializeLegoListNames(ltv.getRoot().getChildren(), pncsFilterId, pncsFilterValue, concept_);
    }
    
    public BorderPane getBorderPane()
    {
        return borderPane;
    }
    
    public void reloadOptions()
    {
        updateDisabled.incrementAndGet();
        loadPncs();
        snomedId.setText("");;
        updateDisabled.decrementAndGet();
    }
    
    private void loadPncs()
    {
        CloseableIterator<Pncs> iterator = BDBDataStoreImpl.getInstance().getPncs();
        HashMap<String, PncsItem> unique = new HashMap<>();
        while (iterator.hasNext())
        {
            Pncs pncs = iterator.next();
            unique.put(pncs.getName() + pncs.getId(), new PncsItem(pncs, pncsNameOrId.valueProperty()));
        }
        pncsItem.getItems().clear();
        pncsItem.getItems().addAll(new PncsItem(PncsItem.ANY, -1, pncsNameOrId.valueProperty()));
        pncsItem.getItems().addAll(unique.values());
        FXCollections.sort(pncsItem.getItems(), new PncsItemComparator());
        pncsItem.getSelectionModel().select(0);
    }
}
