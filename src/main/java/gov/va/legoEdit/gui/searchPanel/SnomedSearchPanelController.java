package gov.va.legoEdit.gui.searchPanel;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.storage.DataStoreException;
import gov.va.legoEdit.storage.wb.WBDataStore;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnomedSearchPanelController implements Initializable
{
    Logger logger = LoggerFactory.getLogger(SnomedSearchPanelController.class);
    private boolean cancelSearch = false;
    private BooleanProperty searchRunning = new SimpleBooleanProperty(false);
    
    @FXML // fx:id="searchButton"
    private Button searchButton; // Value injected by FXMLLoader
    @FXML //  fx:id="searchProgress"
    private ProgressIndicator searchProgress; // Value injected by FXMLLoader
    @FXML //  fx:id="searchText"
    private TextField searchText; // Value injected by FXMLLoader
    @FXML // fx:id="searchResults"
    private ListView<SearchResult> searchResults; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources)
    {
        assert searchButton != null : "fx:id=\"searchButton\" was not injected: check your FXML file 'SearchPanel.fxml'.";
        assert searchResults != null : "fx:id=\"searchResults\" was not injected: check your FXML file 'SearchPanel.fxml'.";

        // initialize your logic here: all @FXML variables will have been injected

        searchResults.setCellFactory(new Callback<ListView<SearchResult>, ListCell<SearchResult>>()
        {
            @Override
            public ListCell<SearchResult> call(ListView<SearchResult> arg0)
            {
                return new ListCell<SearchResult>()
                {
                    @Override
                    protected void updateItem(final SearchResult item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (!empty)
                        {
                            VBox box = new VBox();
                            box.setFillWidth(true);
                            ConceptChronicleBI wbConcept = item.getConcept();
                            String preferredText = (wbConcept != null ? wbConcept.toUserString() : "error - see log");
                            Label concept = new Label(preferredText);
                            concept.getStyleClass().add("boldLabel");
                            box.getChildren().add(concept);

                            for (String s : item.getMatchStrings())
                            {
                                if (s.equals(preferredText))
                                {
                                    continue;
                                }
                                Label matchString = new Label(s);
                                VBox.setMargin(matchString, new Insets(0.0, 0.0, 0.0, 10.0));
                                box.getChildren().add(matchString);
                            }
                            setGraphic(box);
                            
                            ContextMenu cm = new ContextMenu();
                            MenuItem mi = new MenuItem("Copy UUID");
                            mi.setOnAction(new EventHandler<ActionEvent>()
                            {

                                @Override
                                public void handle(ActionEvent event)
                                {
                                    ClipboardContent cc = new ClipboardContent();
                                    if (item.getConcept() != null)
                                    {
                                        cc.putString(item.getConcept().getUUIDs().get(0).toString());
                                        Clipboard.getSystemClipboard().setContent(cc);
                                    }
                                }
                            });
                            cm.getItems().add(mi);
                            setContextMenu(cm);
                        }
                    }
                };
            }
        });

        searchResults.setOnDragDetected(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
                /* drag was detected, start a drag-and-drop gesture */
                /* allow any transfer mode */
                Dragboard db = searchResults.startDragAndDrop(TransferMode.COPY);

                /* Put a string on a dragboard */
                SearchResult dragItem = searchResults.getSelectionModel().getSelectedItem();

                if (dragItem.getConcept() != null)
                {
                    ClipboardContent content = new ClipboardContent();
                    content.putString(dragItem.getConcept().getUUIDs().get(0).toString());
                    db.setContent(content);
                    LegoGUI.getInstance().getLegoGUIController().snomedDragStarted();
                    event.consume();
                }
            }
        });

        searchResults.setOnDragDone(new EventHandler<DragEvent>()
        {
            public void handle(DragEvent event)
            {
                LegoGUI.getInstance().getLegoGUIController().snomedDragCompleted();
            }
        });

        final BooleanProperty searchTextValid = new SimpleBooleanProperty(false);
        searchProgress.visibleProperty().bind(searchRunning);
        searchButton.disableProperty().bind(searchTextValid.not());

        searchButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                if (searchRunning.get())
                {
                    cancelSearch = true;
                }
                else
                {
                    search();
                }
            }
        });

        searchRunning.addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (searchRunning.get())
                {
                    searchButton.setText("Cancel");
                }
                else
                {
                    searchButton.setText("Search");
                }

            }
        });

        searchText.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                if (searchTextValid.getValue())
                {
                    search();
                }
            }
        });

        searchText.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                if (newValue.length() > 1)
                {
                    searchTextValid.set(true);
                }
                else
                {
                    searchTextValid.set(false);
                }
            }
        });
    }

    private synchronized void search()
    {
        searchRunning.set(true);
        searchResults.getItems().clear();
        Thread t = new Thread(new Searcher(searchText.getText()));
        t.setDaemon(true);
        t.start();
    }

    private class Searcher implements Runnable
    {
        private String searchString_;

        public Searcher(String searchText)
        {
            searchString_ = searchText;
        }

        @Override
        public void run()
        {
            try
            {
                cancelSearch = false;
                List<ComponentChroncileBI<?>> result = WBDataStore.getInstance().descriptionSearch(searchString_);

                final HashMap<Integer, SearchResult> viewableResult = new HashMap<>();
                
                if (result == null)
                {
                    LegoGUI.getInstance().showErrorDialog("Search Not Supported", "Search not yet supported", "Search currently only works with a local database.");
                    logger.error("Search not yet supported with FxConcept API");
                    return;
                }
                
                for (ComponentChroncileBI<?> cc : result)
                {
                    if (cancelSearch)
                    {
                        break;
                    }
                    SearchResult sr = viewableResult.get(cc.getConceptNid());
                    if (sr == null)
                    {
                        sr = new SearchResult(cc.getConceptNid());
                        viewableResult.put(cc.getConceptNid(), sr);
                    }
                    if (cc instanceof DescriptionAnalogBI)
                    {
                        sr.addMatchingString(((DescriptionAnalogBI<?>) cc).getText());
                    }
                    else
                    {
                        logger.error("Unexpected type returned from search");
                        sr.addMatchingString("oops");
                    }
                }

                Platform.runLater(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        searchResults.getItems().addAll(viewableResult.values());
                        FXCollections.sort(searchResults.getItems(), new SearchResultComparator());
                    }
                });
            }
            catch (DataStoreException | IOException e)
            {
                logger.error("Unexpected Search Error", e);
            }
            finally
            {
                Platform.runLater(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        searchRunning.set(false);

                    }
                });
            }
        }
    }
}
