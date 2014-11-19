package gov.va.legoEdit.gui.legoTreeView;

import gov.va.isaac.search.CompositeSearchResult;
import gov.va.isaac.search.SearchHandle;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.storage.wb.WBDataStore;
import gov.va.legoEdit.storage.wb.WBUtility;
import gov.va.legoEdit.util.Utility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jefron
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class LookAheadConceptPopup extends Popup implements TaskCompleteCallback
{
	//TODO use ISAAC version?
	Logger logger = LoggerFactory.getLogger(LookAheadConceptPopup.class);
	private TextField sourceTextField;
	VBox popupContent = new VBox();
	private VBox displayedSearchResults = new VBox();
	private List<String> uuidArray = new ArrayList<>();
	private int currentSelection = -1;
	private boolean enableMouseHover = false;

	private AtomicInteger activeSearchCount = new AtomicInteger(0);
	private BooleanBinding searchRunning = new BooleanBinding()
	{
		@Override
		protected boolean computeValue()
		{
			return activeSearchCount.get() > 0;
		}
	};
	private int searchCounter = 0;
	private volatile int lastProcessedId = -1;
	private HashMap<Integer, SearchHandle> runningSearches = new HashMap<>();
	private boolean above = false;

	public LookAheadConceptPopup(Control field)
	{
		if (field instanceof ComboBox)
		{
			this.sourceTextField = ((ComboBox<?>) field).getEditor();
		}
		else if (field instanceof TextField)
		{
			this.sourceTextField = (TextField) field;
		}
		else
		{
			throw new RuntimeException("Unsupported control type");
		}

		setAutoFix(false);
		setAutoHide(true);
		displayedSearchResults.addEventHandler(KeyEvent.ANY, new LookAheadScrollEvent());

		// Disable up/down if we are nested in a combobox - we intercept and deal with them ourselves.
		// also intercept enter.
		field.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>()
		{
			KeyCode previous = null;
			@Override
			public void handle(KeyEvent event)
			{
				if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.ENTER)
				{
					handleScroll(event);
					event.consume();
				}
				else if (event.getCode() == KeyCode.ESCAPE)
				{
					closeLookAheadPanel();
				}
				else if (event.getCode() == KeyCode.TAB || (event.getCode() == KeyCode.SHIFT && previous == KeyCode.TAB))
				{
					previous = event.getCode();
					//If they arrived here via tab, do nothing
					closeLookAheadPanel();
				}
				else
				{
					showOrHidePopupForTextChange();
				}
			}
		});
		field.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.ENTER)
				{
					event.consume();
				}
				else if (event.getCode() == KeyCode.TAB)
				{
					//If they are leaving via tab, close the panel
					closeLookAheadPanel();
				}
			}
		});
		
		StackPane sp = new StackPane();
		sp.getStyleClass().add("headerBackground");
		
		final Label header = new Label("");
		header.setPrefHeight(24.0);
		header.getStyleClass().add("boldLabel");
		header.setPrefWidth(Double.MAX_VALUE);
		sp.getChildren().add(header);
		StackPane.setMargin(header, new Insets(3, 3, 3, 3));
		
		final ProgressBar pb = new ProgressBar(-1);
		pb.setPrefWidth(Double.MAX_VALUE);
		pb.setPrefHeight(18.0);
		pb.visibleProperty().bind(searchRunning);
		pb.setOpacity(0.5);
		sp.getChildren().add(pb);
		StackPane.setMargin(pb, new Insets(6, 3, 6, 3));
		
		popupContent.getChildren().add(sp);
		
		searchRunning.addListener(new InvalidationListener()
		{
			@Override
			public void invalidated(Observable observable)
			{
				if (searchRunning.get())
				{
					header.setText("Searching...");
				}
				else
				{
					if (uuidArray.size() > 0)
					{
						header.setText("Suggested Concepts");
					}
					else
					{
						header.setText("No Suggestions");
					}
				}
			}
		});

		popupContent.getChildren().add(displayedSearchResults);
		popupContent.prefWidthProperty().bind(sourceTextField.widthProperty());
		popupContent.getStyleClass().add("itemBorder");
		popupContent.getStyleClass().add("dialogBackground");
		this.getContent().add(popupContent);
		
		heightProperty().addListener(new InvalidationListener()
		{
			@Override
			public void invalidated(Observable observable)
			{
				moveUpIfNecessary();
			}
		});
		
		//There is a nasty bug in javaFX, where, if we do a select on a drop down item, and then, later, 
		//bring up an entirely new box - it will continue resending mouse_enter events on whatever the last 
		//one was that was selected.  It even sends the complete wrong X and Y values with the mouse event.
		//This workaround seems to work - disable our hover style code until the mouse actually moves over the popup.
		//Note - I can't catch onMouseEntered here either, because it suffers the same problem.  It will randomly 
		//fire mouse entered with the cordinates of the last click - even though the mouse is not being moved.
		popupContent.setOnMouseMoved(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				enableMouseHover = true;
			}
		});
	}

	private synchronized void showOrHidePopupForTextChange()
	{
		for (SearchHandle ssh : runningSearches.values())
		{
			ssh.cancel();
		}

		String text = sourceTextField.getText();
		if (text.length() > 0 && !Utility.isLong(text) && !Utility.isUUID(text))
		{
			try
			{
				activeSearchCount.incrementAndGet();
				searchRunning.invalidate();
				synchronized (runningSearches)
				{
					int id = searchCounter++;
					SearchHandle ssh = WBDataStore.prefixSearch(text, 5, this, id);
					runningSearches.put(id, ssh);
				}
			}
			catch (Exception e)
			{
				logger.error("Unexpected error during lookahead search", e);
			}
			if (!isShowing())
			{
				Point2D p = sourceTextField.localToScene(0.0, 0.0);
				double layoutX = p.getX() + sourceTextField.getScene().getX() + sourceTextField.getScene().getWindow().getX();
				double layoutY = p.getY() + sourceTextField.getHeight() + sourceTextField.getScene().getY() + sourceTextField.getScene().getWindow().getY();
				above = false;
				show(sourceTextField, layoutX, layoutY);
			}
		}
		else
		{
			closeLookAheadPanel();
		}
	}

	private void moveUpIfNecessary()
	{
		if (above || (getY() + getHeight()) > (sourceTextField.getScene().getWindow().getY() + sourceTextField.getScene().getWindow().getHeight()))
		{
			Point2D p = sourceTextField.localToScene(0.0, 0.0);
			setY(p.getY() + sourceTextField.getScene().getY() + sourceTextField.getScene().getWindow().getY() - getHeight());
			above = true;
		}
	}

	private void handleScroll(KeyEvent event)
	{
		displayedSearchResults.fireEvent(event);
	}

	private VBox processResult(CompositeSearchResult result, final int idx)
	{
		VBox box = new VBox();

		Concept c = WBUtility.convertConcept(result.getContainingConcept());

		Label concept = new Label(c.getDesc());
		concept.getStyleClass().add("boldLabel");
		box.getChildren().add(concept);

		for (String s : result.getMatchingStrings())
		{
			if (s.equals(c.getDesc()))
			{
				continue;
			}
			Label matchString = new Label(s);
			VBox.setMargin(matchString, new Insets(0.0, 0.0, 0.0, 10.0));
			box.getChildren().add(matchString);
		}

		uuidArray.add(idx, c.getUuid());
		box.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				sourceTextField.setText(uuidArray.get(idx));
				sourceTextField.getParent().requestFocus();
				closeLookAheadPanel();
			}
		});

		setBoxStyle(box, idx);
		return box;
	}

	private void setBoxStyle(VBox box, int index)
	{
		if (index == 0 || index % 2 == 0)
		{
			box.getStyleClass().add("lookupSearchResultsStyle-A");
			box.setOnMouseEntered(new LookAheadEnterHandler(box, "lookupSearchResultsStyle-Selected"));
			box.setOnMouseExited(new LookAheadEnterHandler(box, "lookupSearchResultsStyle-A"));
		}
		else
		{
			box.getStyleClass().add("lookupSearchResultsStyle-B");
			box.setOnMouseEntered(new LookAheadEnterHandler(box, "lookupSearchResultsStyle-Selected"));
			box.setOnMouseExited(new LookAheadEnterHandler(box, "lookupSearchResultsStyle-B"));
		}
	}

	private void closeLookAheadPanel()
	{
		hide();
		enableMouseHover = false;
		displayedSearchResults.getChildren().clear();
		uuidArray.clear();
		currentSelection = -1;
	}

	private class LookAheadScrollEvent implements EventHandler<KeyEvent>
	{
		@Override
		public void handle(KeyEvent event)
		{
			if (above && currentSelection == -1)
			{
				currentSelection = displayedSearchResults.getChildren().size();
			}
			int oldSelection = currentSelection;

			if (event.getCode() == KeyCode.ENTER)
			{
				if (currentSelection >= 0 && currentSelection < displayedSearchResults.getChildren().size())
				{
					//There is a bug in this mechanism - there doesn't seem to be any way to tell the combo box to ignore the enter key.
					//It processes it even if it is consumed - firing the changelistener.  So, when we set the UUID like this, lookup gets 
					//called twice.  Once with whatever letters they had typed before arrowing down, and once again when the UUID hits.
					//In practice, its fairly harmless.
					sourceTextField.setText(uuidArray.get(currentSelection));
					sourceTextField.getParent().requestFocus();
					closeLookAheadPanel();
					return;
				}
			}
			else if (event.getCode() == KeyCode.UP)
			{
				if (currentSelection > 0)
				{
					currentSelection--;
				}
			}
			else if (event.getCode() == KeyCode.DOWN)
			{
				if (currentSelection < displayedSearchResults.getChildren().size() - 1)
				{
					currentSelection++;
				}
			}
			else
			{
				logger.warn("Unexpected key event to lookahead scroll event");
				return;
			}

			if (oldSelection != currentSelection)
			{
				if (oldSelection >= 0 && oldSelection < displayedSearchResults.getChildren().size())
				{
					VBox oldBox = (VBox) displayedSearchResults.getChildren().get(oldSelection);
					oldBox.getStyleClass().clear();
					setBoxStyle(oldBox, oldSelection);
				}

				if (currentSelection >= 0)
				{
					VBox newBox = (VBox) displayedSearchResults.getChildren().get(currentSelection);
					newBox.getStyleClass().clear();
					newBox.getStyleClass().add("lookupSearchResultsStyle-Selected");
				}
			}
			event.consume();
		}
	}

	private class LookAheadEnterHandler implements EventHandler<MouseEvent>
	{
		private VBox box;
		private String style;

		private LookAheadEnterHandler(VBox b, String style)
		{
			this.box = b;
			this.style = style;
		}

		@Override
		public void handle(MouseEvent t)
		{
			if (enableMouseHover)
			{
				box.getStyleClass().clear();
				box.getStyleClass().add(style);
			}
		}
	}

	@Override
	public void taskComplete(long taskStartTime, Integer taskId)
	{
		try
		{
			SearchHandle ssh = null;
			synchronized (runningSearches)
			{
				ssh = runningSearches.remove(taskId);
			}

			if (ssh == null)
			{
				logger.error("Can't find the proper search handle!");
				return;
			}

			if (ssh.isCancelled() || taskId <= lastProcessedId)
			{
				logger.debug("Skipping out of date search result");
			}
			else
			{
				final Collection<CompositeSearchResult> sortedResults = ssh.getResults();
				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						displayedSearchResults.getChildren().clear();
						uuidArray.clear();
						currentSelection = -1;
						for (CompositeSearchResult result : sortedResults)
						{
							int idx = displayedSearchResults.getChildren().size();
							displayedSearchResults.getChildren().add(processResult(result, idx));
						}
					}
				});
			}
		}
		catch (Exception e)
		{
			logger.error("Unexpected error processing search result", e);
		}
		finally
		{
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					activeSearchCount.decrementAndGet();
					searchRunning.invalidate();
				}
			});
		}
	}
}
