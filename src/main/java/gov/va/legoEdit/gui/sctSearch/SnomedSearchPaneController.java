package gov.va.legoEdit.gui.sctSearch;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.util.CustomClipboard;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.storage.DataStoreException;
import gov.va.legoEdit.storage.wb.WBDataStore;
import gov.va.legoEdit.storage.wb.WBUtility;
import gov.va.legoEdit.util.Utility;
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
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnomedSearchPaneController implements Initializable
{
	Logger logger = LoggerFactory.getLogger(SnomedSearchPaneController.class);
	private boolean cancelSearch = false;
	private BooleanProperty searchRunning = new SimpleBooleanProperty(false);

	@FXML // fx:id="searchButton"
	private Button searchButton; // Value injected by FXMLLoader
	@FXML // fx:id="searchProgress"
	private ProgressIndicator searchProgress; // Value injected by FXMLLoader
	@FXML // fx:id="searchText"
	private TextField searchText; // Value injected by FXMLLoader
	@FXML // fx:id="searchResults"
	private ListView<SearchResult> searchResults; // Value injected by FXMLLoader
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
							final ConceptVersionBI wbConcept = item.getConcept();
							String preferredText = (wbConcept != null ? WBUtility.getFSN(wbConcept) : "error - see log");
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
		Utility.tpe.submit(new Searcher(searchText.getText()));
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

	public BorderPane getBorderPane()
	{
		return borderPane;
	}

}
