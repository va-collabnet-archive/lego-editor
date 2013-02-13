package gov.va.legoEdit.gui.templates;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.util.CustomClipboard;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.model.SchemaToString;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Value;
import gov.va.legoEdit.storage.templates.LegoTemplate;
import gov.va.legoEdit.storage.templates.LegoTemplateComparator;
import gov.va.legoEdit.storage.templates.LegoTemplateManager;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplatesPaneController implements Initializable
{
    Logger logger = LoggerFactory.getLogger(TemplatesPaneController.class);
    private boolean cancelSearch = false;
    private BooleanProperty searchRunning = new SimpleBooleanProperty(false);

    @FXML // fx:id="filterButton"
    private Button filterButton; // Value injected by FXMLLoader
    @FXML // fx:id="filterProgress"
    private ProgressIndicator filterProgress; // Value injected by FXMLLoader
    @FXML // fx:id="filterText"
    private TextField filterText; // Value injected by FXMLLoader
    @FXML // fx:id="searchResults"
    private ListView<LegoTemplate> filterResults; // Value injected by FXMLLoader
    @FXML // fx:id="borderPane"
    private BorderPane borderPane; // Value injected by FXMLLoader

    public static TemplatesPaneController init()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.load(TemplatesPaneController.class.getResourceAsStream("TemplatesPane.fxml"));
            return loader.getController();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unexpected", e);
        }
    }

    // This method is called by the FXMLLoader when initialization is complete
    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources)
    {
        borderPane.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
        AnchorPane.setBottomAnchor(borderPane, 0.0);
        AnchorPane.setTopAnchor(borderPane, 0.0);
        AnchorPane.setLeftAnchor(borderPane, 0.0);
        AnchorPane.setRightAnchor(borderPane, 0.0);

        filterResults.setCellFactory(new Callback<ListView<LegoTemplate>, ListCell<LegoTemplate>>()
        {
            @Override
            public ListCell<LegoTemplate> call(ListView<LegoTemplate> arg0)
            {
                return new ListCell<LegoTemplate>()
                {
                    @Override
                    protected void updateItem(final LegoTemplate item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (!empty)
                        {
                            setText(item.getTemplate().getClass().getSimpleName() + ": " + item.getDescription());

                            ContextMenu cm = new ContextMenu();
                            MenuItem mi = new MenuItem("Copy Template");
                            mi.setOnAction(new EventHandler<ActionEvent>()
                            {
                                @Override
                                public void handle(ActionEvent event)
                                {
                                    if (item.getTemplate() instanceof Lego)
                                    {
                                        CustomClipboard.set((Lego) item.getTemplate());
                                    }
                                    else if (item.getTemplate() instanceof Assertion)
                                    {
                                        CustomClipboard.set((Assertion) item.getTemplate());
                                    }
                                    else if (item.getTemplate() instanceof Discernible)
                                    {
                                        CustomClipboard.set((Discernible) item.getTemplate());
                                    }
                                    else if (item.getTemplate() instanceof Qualifier)
                                    {
                                        CustomClipboard.set((Qualifier) item.getTemplate());
                                    }
                                    else if (item.getTemplate() instanceof Value)
                                    {
                                        CustomClipboard.set((Value) item.getTemplate());
                                    }
                                    else if (item.getTemplate() instanceof Expression)
                                    {
                                        CustomClipboard.set((Expression) item.getTemplate());
                                    }
                                    else
                                    {
                                        LegoGUI.getInstance().showErrorDialog("Unhandled template type",
                                                "Unhandled template type", "Please report this bug: " + item);
                                        logger.error("Unhandled template type " + item);
                                    }
                                }
                            });
                            mi.setGraphic(Images.COPY.createImageView());
                            cm.getItems().add(mi);

                            setContextMenu(cm);
                        }
                    }
                };
            }
        });

        filterProgress.visibleProperty().bind(searchRunning);
        filterButton.setOnAction(new EventHandler<ActionEvent>()
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
                    filterButton.setText("Cancel");
                }
                else
                {
                    filterButton.setText("Filter");
                }

            }
        });

        filterText.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                search();
            }
        });
        
        filterText.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                if (newValue.length() == 0)
                {
                    
                    search();
                }
            }
        });

        LegoTemplateManager.getInstance().addListener(new InvalidationListener()
        {
            @Override
            public void invalidated(Observable arg0)
            {
                search();
            }
        });
        search();
    }

    private synchronized void search()
    {
        cancelSearch = true;
        while (searchRunning.get())
        {
            try
            {
                Thread.sleep(50);
            }
            catch (InterruptedException e)
            {
                // noop
            }
        }
        searchRunning.set(true);
        filterResults.getItems().clear();
        Thread t = new Thread(new Searcher(filterText.getText()));
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

                final ArrayList<LegoTemplate> results = new ArrayList<>();
                if (searchString_.length() > 0)
                {
                    for (LegoTemplate t : LegoTemplateManager.getInstance().getTemplates())
                    {
                        if (t.getDescription().toLowerCase().indexOf(searchString_.toLowerCase()) >= 0
                                || SchemaToString.toString(t.getTemplate()).toLowerCase().indexOf(searchString_.toLowerCase()) >= 0)
                        {
                            results.add(t);
                        }
                        if (cancelSearch)
                        {
                            break;
                        }
                    }
                }
                else
                {
                    results.addAll(LegoTemplateManager.getInstance().getTemplates());
                }

                Platform.runLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        filterResults.getItems().addAll(results);
                        FXCollections.sort(filterResults.getItems(), new LegoTemplateComparator());
                    }
                });
            }
            catch (Exception e)
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

    public BorderPane getBorderPane()
    {
        return borderPane;
    }
}
