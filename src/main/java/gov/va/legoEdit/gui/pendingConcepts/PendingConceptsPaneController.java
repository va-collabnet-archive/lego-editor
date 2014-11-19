package gov.va.legoEdit.gui.pendingConcepts;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.dialogs.YesNoDialogController.Answer;
import gov.va.legoEdit.gui.util.CustomClipboard;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.model.PendingConcept;
import gov.va.legoEdit.model.PendingConcepts;
import gov.va.legoEdit.model.SchemaConceptComparator;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.util.Utility;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * PendingConceptsPaneController
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class PendingConceptsPaneController implements Initializable
{
	Logger logger = LoggerFactory.getLogger(PendingConceptsPaneController.class);
	private volatile boolean cancelSearch = false;
	private BooleanProperty searchRunning = new SimpleBooleanProperty(false);

	@FXML // fx:id="filterButton"
	private Button filterButton; // Value injected by FXMLLoader
	@FXML // fx:id="filterProgress"
	private ProgressIndicator filterProgress; // Value injected by FXMLLoader
	@FXML // fx:id="filterText"
	private TextField filterText; // Value injected by FXMLLoader
	@FXML // fx:id="searchResults"
	private ListView<PendingConcept> filterResults; // Value injected by FXMLLoader
	@FXML // fx:id="borderPane"
	private BorderPane borderPane; // Value injected by FXMLLoader

	public static PendingConceptsPaneController init()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader();
			loader.load(PendingConceptsPaneController.class.getResourceAsStream("PendingConceptsPane.fxml"));
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

		filterResults.setCellFactory(new Callback<ListView<PendingConcept>, ListCell<PendingConcept>>()
		{
			@Override
			public ListCell<PendingConcept> call(ListView<PendingConcept> arg0)
			{
				return new ListCell<PendingConcept>()
				{
					@Override
					protected void updateItem(final PendingConcept item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!empty)
						{
							setText(item.getDesc());
							Concept parent = PendingConcepts.getInstance().getParentConcept(item.getSctid());
							setTooltip(new Tooltip(item.getDesc() + " (" + item.getSctid() + ")" 
									+ (parent == null ? "" : " is a " + parent.getDesc() + " (" + parent.getSctid() + ")")));
							ContextMenu cm = new ContextMenu();
							
							MenuItem mi = new MenuItem("Copy Concept");
							mi.setOnAction(new EventHandler<ActionEvent>()
							{
								@Override
								public void handle(ActionEvent event)
								{
									CustomClipboard.set(item.getUuid());
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
									LegoGUI.getInstance().showAddPendingConcept(item);
								}
							});
							mi.setGraphic(Images.CONCEPT_VIEW.createImageView());
							cm.getItems().add(mi);
							
							
							mi = new MenuItem("Delete Concept");
							mi.setOnAction(new EventHandler<ActionEvent>()
							{
								@Override
								public void handle(ActionEvent event)
								{
									if(Answer.NO == LegoGUI.getInstance().showYesNoDialog("Delete Pending Concept?", 
											"Deleting this pending concept will cause any Legos that utilize the concept to report errors.  Are you sure you want to delete this?"))
									{
										return;
									}
									try
									{
										PendingConcepts.getInstance().deleteConcept(item.getSctid());
									}
									catch (Exception e)
									{
										logger.error("delete pending error", e);
										LegoGUI.getInstance().showErrorDialog("Error", "Unexpected error deleting concept", null);
									}
								}
							});
							mi.setGraphic(Images.DELETE.createImageView());
							cm.getItems().add(mi);

							setContextMenu(cm);
						}
						else
						{
							setText("");
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

		PendingConcepts.getInstance().addListener(new InvalidationListener()
		{
			@Override
			public void invalidated(Observable arg0)
			{
				search();
			}
		});
		
		filterResults.setOnDragDetected(new EventHandler<MouseEvent>()
		{
			public void handle(MouseEvent event)
			{
				/* drag was detected, start a drag-and-drop gesture */
				/* allow any transfer mode */
				Dragboard db = filterResults.startDragAndDrop(TransferMode.COPY);

				/* Put a string on a dragboard */
				Concept dragItem = filterResults.getSelectionModel().getSelectedItem();

				ClipboardContent content = new ClipboardContent();
				content.putString(dragItem.getSctid().toString() + " ");  //the space is ugly hack to help out the conceptNode...  
				db.setContent(content);
				LegoGUI.getInstance().getLegoGUIController().snomedDragStarted();
				event.consume();
			}
		});

		filterResults.setOnDragDone(new EventHandler<DragEvent>()
		{
			public void handle(DragEvent event)
			{
				LegoGUI.getInstance().getLegoGUIController().snomedDragCompleted();
			}
		});
		
		search();
	}
	
	public void searchFor(String searchString)
	{
		filterText.setText(searchString);
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
		Utility.tpe.submit(new Searcher(filterText.getText()));
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
				final ArrayList<PendingConcept> results = new ArrayList<>();
				List<PendingConcept> concepts = PendingConcepts.getInstance().getPendingConcepts();
				Collections.sort(concepts, new SchemaConceptComparator());
				
				if (searchString_.length() > 0)
				{
					for (PendingConcept c : concepts )
					{
						if (c.getDesc().toLowerCase().indexOf(searchString_.toLowerCase()) >= 0)
						{
							results.add(c);
						}
						if (cancelSearch)
						{
							break;
						}
					}
				}
				else
				{
					results.addAll(concepts);
				}

				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						filterResults.getItems().addAll(results);
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
