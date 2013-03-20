//-javaagent:C:/Users/jefron/Desktop/ScenicView.jar -Xmx2g 

package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.gui.sctSearch.SnomedSearchResult;
import gov.va.legoEdit.gui.sctSearch.SnomedSearchResultComparator;
import gov.va.legoEdit.gui.util.TaskCompleteCallback;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.storage.wb.SnomedSearchHandle;
import gov.va.legoEdit.storage.wb.WBDataStore;
import gov.va.legoEdit.storage.wb.WBUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import org.ihtsdo.tk.api.ContradictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jefron
 */
class LookAheadConceptPopup extends Popup implements TaskCompleteCallback {

    Logger logger = LoggerFactory.getLogger(LookAheadConceptPopup.class);
    private VBox searchResults = new VBox();
    private ComboBox<ComboBoxConcept> comboBox;
    private AnchorPane anchorpane = new AnchorPane();
    private double layoutY;
    private double layoutX;
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
    private HashMap<Integer, SnomedSearchHandle> runningSearches = new HashMap<>();
    private List<String> uuidArray = new ArrayList<>();
    private int currentSelection;
    private boolean isDisplaying = false;

    public LookAheadConceptPopup() {
        setAutoFix(false);
        setAutoHide(true);
        searchResults.setMaxWidth(Double.MAX_VALUE);
        searchResults.addEventHandler(KeyEvent.ANY, new LookAheadScrollEvent());
    }

    private void search(String searchText) throws IOException, IOException, ContradictionException {
        currentSelection = -1;

        for (SnomedSearchHandle ssh : runningSearches.values())
        {
            ssh.cancel();
        }
        searchResults.setPrefWidth(comboBox.getWidth());

        if (searchText.trim().length() > 0) {
            activeSearchCount.incrementAndGet();
            searchRunning.invalidate();
            synchronized (runningSearches)
            {
            	int id = searchCounter++;
                SnomedSearchHandle ssh = WBDataStore.getInstance().prefixSearch(searchText, 5, this, id);
                runningSearches.put(id, ssh);
            }
        } else {
            closeLookAheadPanel();
        }
    }

    void showPopup(ComboBox<ComboBoxConcept> callingComboBox) {
        comboBox = callingComboBox;

        try {
            search((callingComboBox.getEditor().getText()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Parent parent = comboBox.getParent();
        Bounds childBounds = comboBox.getBoundsInParent();
        Bounds parentBounds = parent.localToScene(parent.getBoundsInLocal());

        layoutX = childBounds.getMinX() + parentBounds.getMinX()
                + parent.getScene().getX() + parent.getScene().getWindow().getX();

        layoutY = childBounds.getMaxY() + parentBounds.getMinY()
                + parent.getScene().getY() + parent.getScene().getWindow().getY();

    }

    void handleScroll(KeyEvent event) {
        searchResults.fireEvent(event);
    }

    private VBox processResult(SnomedSearchResult result, int idx) {
        VBox box = new VBox();

        Concept c = WBUtility.convertConcept(result.getConcept());

        Label concept = new Label(c.getDesc());
        concept.getStyleClass().add("boldLabel");
        box.getChildren().add(concept);

        for (String s : result.getMatchStrings()) {
            if (s.equals(c.getDesc())) {
                continue;
            }
            Label matchString = new Label(s);
            VBox.setMargin(matchString, new Insets(0.0, 0.0, 0.0, 10.0));
            box.getChildren().add(matchString);
        }

        uuidArray.add(idx, c.getUuid());
        box.setOnMouseClicked(new LookAheadConceptPopup.LookAheadSelectHandler(c.getDesc(), c.getUuid()));

        return box;
    }

    private void setBoxStyle(VBox box, int index) {
        if (index == 0 || index % 2 == 0) {
            box.getStyleClass().add("lookupSearchResultsStyle-A");
        } else {
            box.getStyleClass().add("lookupSearchResultsStyle-B");
        }
    }

    private void closeLookAheadPanel() {
        searchResults.getChildren().clear();
        anchorpane.getChildren().clear();
        anchorpane.getStyleClass().clear();
        hide();
        isDisplaying = false;
    }

    private class LookAheadSelectHandler implements EventHandler<MouseEvent> {
        String uuid;

        private LookAheadSelectHandler(String term, String id) {
            uuid = id;
        }

        @Override
        public void handle(MouseEvent t) {
            if (uuid == null) {
                return;
            }
            comboBox.getEditor().setText(uuid);
            closeLookAheadPanel();
        }
    }

    private class LookAheadScrollEvent implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent event) {
            int oldSelection = currentSelection;
            boolean noAction = false;

            if (event.getCode() == KeyCode.ENTER) {
                if (currentSelection >= 0 && currentSelection < searchResults.getChildren().size()) {
                    comboBox.getEditor().setText(uuidArray.get(currentSelection));
                    closeLookAheadPanel();
                    noAction = true;
                }
            } else if (event.getCode() == KeyCode.UP) {
                if (currentSelection > 0) {
                    currentSelection--;
                }
            } else if (event.getCode() == KeyCode.DOWN) {
                if (currentSelection < searchResults.getChildren().size() - 1) {
                    currentSelection++;
                }
            } else {
                noAction = true;
            }

            if (!noAction) {
                if (oldSelection >= 0) {
                    VBox oldBox = (VBox) searchResults.getChildren().get(oldSelection);
                    oldBox.getStyleClass().clear();
                    setBoxStyle(oldBox, oldSelection);
                }

                if (currentSelection >= 0)
                {
	                VBox newBox = (VBox) searchResults.getChildren().get(currentSelection);
	                newBox.getStyleClass().clear();
	                newBox.getStyleClass().add("lookupSearchResultsStyle-Selected");
                }
            }

            event.consume();
        }
    }

    private class LookAheadEnterHandler implements EventHandler<MouseEvent> {

        VBox box;
        boolean isEnterCase;
        private String originalCase;

        private LookAheadEnterHandler(VBox b, boolean isEnter) {
            box = b;
            isEnterCase = isEnter;
            originalCase = box.getStyleClass().get(0);
        }

        @Override
        public void handle(MouseEvent t) {
            if (box == null) {
                return;
            }

            if (isEnterCase) {
                box.getStyleClass().clear();
                box.getStyleClass().add("lookupSearchResultsStyle-Selected");
            } else {
                box.getStyleClass().clear();
                box.getStyleClass().add(originalCase);
            }
        }
    }

    @Override
    public void taskComplete(long taskStartTime, Integer taskId) {
        SnomedSearchHandle ssh = null;
        synchronized (runningSearches) {
            ssh = runningSearches.remove(taskId);
        }

        if (ssh == null) {
            logger.error("Can't find the proper search handle!");
            return;
        }

        final SnomedSearchHandle finalSSH = ssh;

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    SortedSet<SnomedSearchResult> sortedResults = new TreeSet<SnomedSearchResult>(new SnomedSearchResultComparator());
                    if (!finalSSH.isCancelled()) {
                        sortedResults.addAll(finalSSH.getResults());
                    }

                    searchResults.getChildren().clear();
                    anchorpane.getChildren().clear();
                    anchorpane.getStyleClass().clear();

                    for (SnomedSearchResult result : sortedResults) {
                        if (finalSSH.isCancelled()) {
                            break;
                        }
                        int idx = searchResults.getChildren().size();

                        VBox box = processResult(result, idx);
                        setBoxStyle(box, idx);

                        box.setOnMouseEntered(new LookAheadConceptPopup.LookAheadEnterHandler(box, true));
                        box.setOnMouseExited(new LookAheadConceptPopup.LookAheadEnterHandler(box, false));

                        searchResults.getChildren().add(box);
                    }

                    if (!finalSSH.isCancelled()) {
                        anchorpane.getChildren().add(searchResults);
                        anchorpane.getStyleClass().add("lookupSearchResultsStyle-A");

                        AnchorPane.setTopAnchor(searchResults, 0.0);
                        AnchorPane.setBottomAnchor(searchResults, 0.0);
                        AnchorPane.setLeftAnchor(searchResults, 0.0);
                        AnchorPane.setRightAnchor(searchResults, 0.0);

                        getContent().clear();
                        getContent().add(anchorpane);
                    }
                } catch (Exception e) {
                    logger.error("Unexpected Search Error", e);
                } finally {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            activeSearchCount.decrementAndGet();
                            searchRunning.invalidate();
                            if (!finalSSH.isCancelled()) {
                                Parent p = comboBox.getParent();

                                while (p.getParent() != null) {
                                    p = p.getParent();
                                }

                                if (searchResults.getChildren().size() > 0) {
                                    if (!isDisplaying || !isShowing()) {
                                        show(comboBox, layoutX, layoutY);
                                        isDisplaying = true;
                                    }

                                    if (layoutY + anchorpane.getHeight() >= p.getLayoutBounds().getHeight()
                                            + comboBox.getParent().getScene().getWindow().getY()) {
                                        layoutY = layoutY - comboBox.getHeight() - anchorpane.getHeight();
                                        setY(layoutY);
                                    }
                                } else {
                                    closeLookAheadPanel();
                                }
                            }
                        }
                    });
                }
            }
        });
    }
}
