package gov.va.legoEdit.gui.sctSearch;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.util.CustomClipboard;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.gui.util.TaskCompleteCallback;
import gov.va.legoEdit.storage.wb.SnomedSearchHandle;
import gov.va.legoEdit.storage.wb.WBDataStore;
import gov.va.legoEdit.storage.wb.WBUtility;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * SnomedSearchPaneController
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class SnomedSearchPaneController implements Initializable, TaskCompleteCallback
{
	Logger logger = LoggerFactory.getLogger(SnomedSearchPaneController.class);
	private BooleanProperty searchRunning = new SimpleBooleanProperty(false);
	private SnomedSearchHandle ssh = null;

	@FXML // fx:id="searchButton"
	private Button searchButton; // Value injected by FXMLLoader
	@FXML // fx:id="searchProgress"
	private ProgressIndicator searchProgress; // Value injected by FXMLLoader
	@FXML // fx:id="searchText"
	private TextField searchText; // Value injected by FXMLLoader
	@FXML // fx:id="searchResults"
	private ListView<SnomedSearchResult> searchResults; // Value injected by FXMLLoader
	@FXML // fx:id="borderPane"
	private BorderPane borderPane; // Value injected by FXMLLoader

	public static SnomedSearchPaneController init()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader();
			loader.load(SnomedSearchPaneController.class.getResourceAsStream("SnomedSearchPane.fxml"));
			return loader.getController();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Unexpected", e);
		}
	}

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert searchButton != null : "fx:id=\"searchButton\" was not injected: check your FXML file 'SearchPanel.fxml'.";
		assert searchResults != null : "fx:id=\"searchResults\" was not injected: check your FXML file 'SearchPanel.fxml'.";

		// initialize your logic here: all @FXML variables will have been injected

		borderPane.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
		AnchorPane.setBottomAnchor(borderPane, 0.0);
		AnchorPane.setTopAnchor(borderPane, 0.0);
		AnchorPane.setLeftAnchor(borderPane, 0.0);
		AnchorPane.setRightAnchor(borderPane, 0.0);

		searchResults.setCellFactory(new Callback<ListView<SnomedSearchResult>, ListCell<SnomedSearchResult>>()
		{
			@Override
			public ListCell<SnomedSearchResult> call(ListView<SnomedSearchResult> arg0)
			{
				return new ListCell<SnomedSearchResult>()
				{
					@Override
					protected void updateItem(final SnomedSearchResult item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!empty)
						{
							VBox box = new VBox();
							box.setFillWidth(true);
							final ConceptVersionBI wbConcept = item.getConcept();
							String preferredText = (wbConcept != null ? WBUtility.getDescription(wbConcept) : "error - see log");
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
									if (item.getConcept() != null)
									{
										CustomClipboard.set(item.getConcept().getUUIDs().get(0).toString());
										LegoGUI.getInstance().getLegoGUIController().updateRecentCodes(CustomClipboard.getString());
									}
								}
							});
							mi.setGraphic(Images.COPY.createImageView());
							cm.getItems().add(mi);

							mi = new MenuItem("View Concept");
							mi.setOnAction(new EventHandler<ActionEvent>()
							{
								@Override
								public void handle(ActionEvent event)
								{
									LegoGUI.getInstance().showSnomedConceptDialog(item.getConcept().getUUIDs().get(0));
								}
							});
							mi.setGraphic(Images.CONCEPT_VIEW.createImageView());
							cm.getItems().add(mi);
							
							mi = new MenuItem("Find Concept in Tree");
							mi.setOnAction(new EventHandler<ActionEvent>()
							{
								@Override
								public void handle(ActionEvent arg0)
								{
									LegoGUI.getInstance().getLegoGUIController().findConceptInTree(item.getConcept().getUUIDs().get(0));
								}
							});
							mi.setGraphic(Images.ROOT.createImageView());
							cm.getItems().add(mi);

							mi = new MenuItem("Filter for Legos that use this Concept");
							mi.setOnAction(new EventHandler<ActionEvent>()
							{

								@Override
								public void handle(ActionEvent event)
								{
									if (item.getConcept() != null)
									{
										LegoGUI.getInstance().getLegoGUIController().getLegoFilterPaneController()
												.filterOnConcept(item.getConcept().getUUIDs().get(0).toString());
									}
								}
							});
							mi.setGraphic(Images.FILTER.createImageView());
							cm.getItems().add(mi);

							setContextMenu(cm);

							setOnMouseClicked(new EventHandler<MouseEvent>()
							{
								@Override
								public void handle(MouseEvent mouseEvent)
								{
									if (mouseEvent.getButton().equals(MouseButton.PRIMARY))
									{
										if (mouseEvent.getClickCount() == 2)
										{
											LegoGUI.getInstance().showSnomedConceptDialog(wbConcept.getUUIDs().get(0));
										}
									}
								}
							});
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
				SnomedSearchResult dragItem = searchResults.getSelectionModel().getSelectedItem();

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
				if (searchRunning.get() && ssh != null)
				{
					ssh.cancel();
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
				if (searchTextValid.getValue() && !searchRunning.get())
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
		if (searchRunning.get())
		{
			return;
		}
		searchRunning.set(true);
		searchResults.getItems().clear();
		//we get called back when the results are ready.
		ssh = WBDataStore.descriptionSearch(searchText.getText(), this);
	}

	public BorderPane getBorderPane()
	{
		return borderPane;
	}

	@Override
	public void taskComplete(long taskStartTime, Integer taskId)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (!ssh.isCancelled())
					{
						searchResults.getItems().addAll(ssh.getResults());
					}
				}
				catch (Exception e)
				{
					LegoGUI.getInstance().showErrorDialog("Search Error", "There was an unexpected error running the search", e.toString());
					logger.error("Unexpected Search Error", e);
					searchResults.getItems().clear();
				}
				finally
				{
					searchRunning.set(false);
				}
			}
		});
		
	}
}
