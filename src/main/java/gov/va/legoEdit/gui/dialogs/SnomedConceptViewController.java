package gov.va.legoEdit.gui.dialogs;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.util.CopyableLabel;
import gov.va.legoEdit.storage.wb.WBUtility;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.ihtsdo.fxmodel.concept.FxConcept;
import org.ihtsdo.fxmodel.concept.component.attribute.FxConceptAttributesChronicle;
import org.ihtsdo.fxmodel.concept.component.attribute.FxConceptAttributesVersion;
import org.ihtsdo.fxmodel.concept.component.description.FxDescriptionChronicle;
import org.ihtsdo.fxmodel.concept.component.description.FxDescriptionVersion;
import org.ihtsdo.fxmodel.concept.component.identifier.FxIdentifier;
import org.ihtsdo.fxmodel.concept.component.relationship.FxRelationshipChronicle;
import org.ihtsdo.fxmodel.concept.component.relationship.FxRelationshipVersion;

public class SnomedConceptViewController implements Initializable
{
    @FXML //  fx:id="IDs"
    private TableView<StringWithRefList> IDs; // Value injected by FXMLLoader
    @FXML //  fx:id="anchorPane"
    private AnchorPane anchorPane; // Value injected by FXMLLoader
    @FXML //  fx:id="conceptDefined"
    private Label conceptDefined; // Value injected by FXMLLoader
    @FXML //  fx:id="conceptStatus"
    private Label conceptStatus; // Value injected by FXMLLoader
    @FXML //  fx:id="descriptions"
    private TableView<StringWithRefList> descriptions; // Value injected by FXMLLoader
    @FXML //  fx:id="fsnLabel"
    private Label fsnLabel; // Value injected by FXMLLoader
    @FXML //  fx:id="sourceRelationships"
    private TableView<StringWithRefList> sourceRelationships; // Value injected by FXMLLoader
    @FXML //  fx:id="uuid"
    private Label uuid; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources)
    {
        assert IDs != null : "fx:id=\"IDs\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
        assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
        assert conceptDefined != null : "fx:id=\"conceptDefined\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
        assert conceptStatus != null : "fx:id=\"conceptStatus\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
        assert descriptions != null : "fx:id=\"descriptions\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
        assert fsnLabel != null : "fx:id=\"fsnLabel\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
        assert sourceRelationships != null : "fx:id=\"sourceRelationships\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
        assert uuid != null : "fx:id=\"uuid\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
    }
    
    public void init(FxConcept concept)
    {
        FxConceptAttributesChronicle ca = concept.getConceptAttributes();
        FxConceptAttributesVersion cav = ca.getVersions().get(ca.getVersions().size() - 1); 
        conceptDefined.setText(cav.isDefined() + "");
        conceptStatus.setText(cav.getStatusReference().getText());
        fsnLabel.setText(WBUtility.getFSN(concept));
        CopyableLabel.addCopyMenu(fsnLabel);
        uuid.setText(concept.getPrimordialUuid().toString());
        CopyableLabel.addCopyMenu(uuid);
        LegoGUI.getInstance().getLegoGUIController().updateRecentCodes(concept.getPrimordialUuid().toString());
        
        
        uuid.setOnDragDetected(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
                /* drag was detected, start a drag-and-drop gesture */
                /* allow any transfer mode */
                Dragboard db = uuid.startDragAndDrop(TransferMode.COPY);

                /* Put a string on a dragboard */
                ClipboardContent content = new ClipboardContent();
                content.putString(uuid.getText());
                db.setContent(content);
                LegoGUI.getInstance().getLegoGUIController().snomedDragStarted();
                event.consume();
            }
        });

        uuid.setOnDragDone(new EventHandler<DragEvent>()
        {
            public void handle(DragEvent event)
            {
                LegoGUI.getInstance().getLegoGUIController().snomedDragCompleted();
            }
        });
        
        Callback<TableColumn.CellDataFeatures<StringWithRefList, StringWithRef>,ObservableValue<StringWithRef>> cellValueFactory 
            = new Callback<TableColumn.CellDataFeatures<StringWithRefList, StringWithRef>,ObservableValue<StringWithRef>>()
        {

            @Override
            public ObservableValue<StringWithRef> call(CellDataFeatures<StringWithRefList, StringWithRef> param)
            {
                StringWithRefList st = param.getValue();
                return new SimpleObjectProperty<SnomedConceptViewController.StringWithRef>(st.get(Integer.parseInt(param.getTableColumn().getId())));
            }
        };

        Callback<TableColumn<StringWithRefList, StringWithRef>, TableCell<StringWithRefList, StringWithRef>> cellFactory 
            = new Callback<TableColumn<StringWithRefList, StringWithRef>, TableCell<StringWithRefList, StringWithRef>>()
        {
            @Override
            public TableCell<StringWithRefList, StringWithRef> call(TableColumn<StringWithRefList, StringWithRef> param)
            {
                final TableCell<StringWithRefList, StringWithRef> cell = new TableCell<StringWithRefList, StringWithRef>()
                {
                    @Override
                    public void updateItem(final StringWithRef item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (!isEmpty())
                        {
                            Text text = new Text(item.text);
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty());
                            setGraphic(text);
                            MenuItem mi = new MenuItem("Copy");
                            mi.setOnAction(new EventHandler<ActionEvent>()
                            {
                                @Override
                                public void handle(ActionEvent arg0)
                                {
                                    ClipboardContent content = new ClipboardContent();
                                    content.putString(item.text);
                                    Clipboard.getSystemClipboard().setContent(content);
                                }
                            });
                            
                            ContextMenu cm = new ContextMenu(mi);
                            
                            if (item.ref != null)
                            {
                                mi = new MenuItem("View Concept");
                                mi.setOnAction(new EventHandler<ActionEvent>()
                                {
                                    @Override
                                    public void handle(ActionEvent arg0)
                                    {
                                        LegoGUI.getInstance().showSnomedConceptDialog(item.ref);
                                    }
                                });
                                cm.getItems().add(mi);
                            }
                            setContextMenu(cm);
                        }
                    }
                };
                return cell;
            }
        };
        
        
        for (FxDescriptionChronicle d : concept.getDescriptions())
        {
            FxDescriptionVersion dv = d.getVersions().get(d.getVersions().size() - 1);
            descriptions.getItems().add(new StringWithRefList(new StringWithRef(dv.getTypeReference().getText(), dv.getTypeReference().getUuid()),
                    new StringWithRef(dv.getText()), 
                    new StringWithRef(dv.getStatusReference().getText(), dv.getStatusReference().getUuid())));
        }
        setupTable(new String[] {"Type", "Text", "Status"}, descriptions, cellValueFactory, cellFactory);
        
        
        for (FxIdentifier id : ca.getAdditionalIds())
        {
            IDs.getItems().add(new StringWithRefList(new StringWithRef(id.getAuthorityRef().getText(), id.getAuthorityRef().getUuid()),
                    new StringWithRef(id.getDenotation() +""),
                    new StringWithRef(id.getStatusReference().getText(), id.getStatusReference().getUuid())));
        }
        setupTable(new String[] {"Authority", "Value", "Status"}, IDs, cellValueFactory, cellFactory);
        
        for (FxRelationshipChronicle r : concept.getOriginRelationships())
        {
            FxRelationshipVersion rv = r.getVersions().get(r.getVersions().size() - 1);
            sourceRelationships.getItems().add(new StringWithRefList(new StringWithRef(rv.getTypeReference().getText(), rv.getTypeReference().getUuid()),
                    new StringWithRef(rv.getDestinationReference().getText(), rv.getDestinationReference().getUuid()),
                    new StringWithRef(rv.getStatusReference().getText(), rv.getStatusReference().getUuid())));
        }
        setupTable(new String[] {"Type", "Destination", "Status"}, sourceRelationships, cellValueFactory, cellFactory);
    }
    
    private void setupTable(String[] columns, 
            TableView<StringWithRefList> tableView, 
            Callback<TableColumn.CellDataFeatures<StringWithRefList, StringWithRef>,ObservableValue<StringWithRef>> cellValueFactory,
            Callback<TableColumn<StringWithRefList, StringWithRef>, TableCell<StringWithRefList, StringWithRef>> cellFactory)
    {
        for (int i = 0; i < columns.length; i++)
        {
            float colWidth = 1.0f /  (float)columns.length;
            TableColumn<StringWithRefList, StringWithRef> tc = new TableColumn<StringWithRefList, StringWithRef>(columns[i]);
            tc.setId(i + "");
            tc.setCellValueFactory(cellValueFactory);
            tc.setCellFactory(cellFactory);
            tc.prefWidthProperty().bind(tableView.widthProperty().multiply(colWidth).subtract(5.0));
            tableView.getColumns().add(tc);
        }
        tableView.setPrefHeight(tableView.getMinHeight() + (20.0 * tableView.getItems().size()));
        tableView.setPlaceholder(new Label());
    }
    
    public String getTitle()
    {
        return fsnLabel.getText();
    }
    
    protected class StringWithRefList
    {
        private ArrayList<StringWithRef> items_ = new ArrayList<StringWithRef>();
        
        StringWithRefList(StringWithRef ... items)
        {
            for (StringWithRef swr : items)
            {
                items_.add(swr);
            }
        }
        
       public StringWithRef get(int index)
       {
           return items_.get(index);
       }
    }
    
    protected class StringWithRef
    {
        String text;
        UUID ref;
        
        StringWithRef(String text, UUID ref)
        {
            this.text = text;
            this.ref = ref;
        }
        
        StringWithRef(String text)
        {
            this.text = text;
        }
    }
}

