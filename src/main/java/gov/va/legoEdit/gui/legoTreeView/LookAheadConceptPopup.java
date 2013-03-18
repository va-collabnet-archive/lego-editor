//-javaagent:C:/Users/jefron/Desktop/ScenicView.jar -Xmx2g 
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.sctSearch.SearchResult;
import gov.va.legoEdit.gui.sctSearch.SearchResultComparator;
import gov.va.legoEdit.storage.DataStoreException;
import gov.va.legoEdit.storage.wb.WBDataStore;
import gov.va.legoEdit.storage.wb.WBUtility;
import gov.va.legoEdit.util.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jefron
 */
class LookAheadConceptPopup extends Popup {

    Logger logger = LoggerFactory.getLogger(LookAheadConceptPopup.class);
    private VBox searchResults = new VBox();
    private ComboBox<ComboBoxConcept> comboBox;
    private AnchorPane anchorpane = new AnchorPane();
    private double layoutY;
    private double layoutX;
    private boolean cancelSearch = false;
    private BooleanProperty searchRunning = new SimpleBooleanProperty(false);
    private List<String> prefTermArray = new ArrayList();
    private List<String> uuidArray = new ArrayList();
    private int currentSelection;
    private boolean isDisplaying = false;

    public LookAheadConceptPopup() {
        setAutoFix(false);
        setAutoHide(true);
        searchResults.setMaxWidth(Double.MAX_VALUE);
        //Another hack to fix strange behavior in javafx... left arrow key in the combobox editor doesn't work as expected unless you filter it..

        searchResults.addEventHandler(KeyEvent.ANY, new LookAheadScrollEvent());
    }

    private void search(String searchText) throws IOException, IOException, ContradictionException {
        currentSelection = -1;

        if (searchRunning.get()) {
            cancelSearch = true;
        } else {
            searchResults.setPrefWidth(comboBox.getWidth());

            if (searchText.trim().length() > 0) {
                searchRunning.set(true);
                Utility.tpe.submit(new LookAheadConceptPopup.Searcher(searchText));
            } else {
                searchResults.getChildren().clear();
                anchorpane.getChildren().clear();
                anchorpane.getStyleClass().clear();
                hide();
                isDisplaying = false;
            }
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

    private class Searcher implements Runnable {

        private String searchString_;

        public Searcher(String searchText) {
            searchString_ = searchText;
        }

        @Override
        public void run() {
            int counter = 0;

            try {
                List<ComponentChroncileBI<?>> result = WBDataStore.getInstance().prefixSearch(searchString_);

                final HashMap<Integer, SearchResult> viewableResult = new HashMap<>();

                if (result == null) {
                    LegoGUI.getInstance().showErrorDialog("Search Not Supported", "Search not yet supported", "Search currently only works with a local database.");
                    logger.error("Search not yet supported with FxConcept API");
                    return;
                }

                for (ComponentChroncileBI<?> cc : result) {
                    if (cancelSearch) {
                        break;
                    }

                    counter++;
                    SearchResult sr = viewableResult.get(cc.getConceptNid());
                    if (sr == null) {
                        sr = new SearchResult(cc.getConceptNid());
                        viewableResult.put(cc.getConceptNid(), sr);
                    }
                    if (cc instanceof DescriptionAnalogBI) {
                        sr.addMatchingString(((DescriptionAnalogBI<?>) cc).getText());
                    } else if (cc instanceof ConceptVersionBI) {
                        //This is the type returned when the do a UUID or SCTID search
                        sr.addMatchingString(searchString_.trim());
                    } else {
                        logger.error("Unexpected type returned from search: " + cc.getClass().getName());
                        sr.addMatchingString("oops");
                    }
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!cancelSearch) {
                            SortedSet<SearchResult> sortedResults = new TreeSet<SearchResult>(new SearchResultComparator());

                            for (SearchResult result : viewableResult.values()) {
                                if (cancelSearch) {
                                    break;
                                }
                                sortedResults.add(result);
                            }
                            searchResults.getChildren().clear();
                            anchorpane.getChildren().clear();
                            anchorpane.getStyleClass().clear();

                            if (!cancelSearch) {
                                for (SearchResult result : sortedResults) {
                                    if (cancelSearch) {
                                        break;
                                    }
                                    int idx = searchResults.getChildren().size();

                                    VBox box = processResult(result, idx);
                                    setBoxStyle(box, idx);

                                    box.setOnMouseEntered(new LookAheadConceptPopup.LookAheadEnterHandler(box, true));
                                    box.setOnMouseExited(new LookAheadConceptPopup.LookAheadEnterHandler(box, false));


                                    searchResults.getChildren().add(box);
                                }
                            }

                            if (!cancelSearch) {
                                anchorpane.getChildren().add(searchResults);
                                anchorpane.getStyleClass().add("lookupSearchResultsStyle-A");

                                AnchorPane.setTopAnchor(searchResults, 0.0);
                                AnchorPane.setBottomAnchor(searchResults, 0.0);
                                AnchorPane.setLeftAnchor(searchResults, 0.0);
                                AnchorPane.setRightAnchor(searchResults, 0.0);
                                
                                getContent().clear();
                                getContent().add(anchorpane);

                                setAutoFix(false);
                            }
                        }
                    }
                });
            } catch (DataStoreException | IOException e) {
                logger.error("Unexpected Search Error", e);
            } finally {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!cancelSearch) {
                            Parent p = comboBox.getParent();

                            while (p.getParent() != null) {
                                p = p.getParent();
                            }

                            if (searchResults.getChildren().size() > 0) {
                                if (!isDisplaying) {
                                    show(comboBox, layoutX, layoutY);
                                    isDisplaying = true;
                                } 

                                if (layoutY + anchorpane.getHeight()
                                        >= p.getLayoutBounds().getHeight() + comboBox.getParent().getScene().getWindow().getY()) {
                                    layoutY = layoutY - comboBox.getHeight() - anchorpane.getHeight();

                                    setY(layoutY);
                                }
                            }
                        }

                        cancelSearch = false;
                        searchRunning.set(false);
                    }
                });
            }
        }
    }

    private VBox processResult(SearchResult result, int idx) {
        VBox box = new VBox();

        final ConceptVersionBI wbConcept = result.getConcept();
        String preferredText = (wbConcept != null ? WBUtility.getDescription(wbConcept) : "error - see log");

        Label concept = new Label(preferredText);
        concept.getStyleClass().add("boldLabel");
        box.getChildren().add(concept);


        for (String s : result.getMatchStrings()) {
            if (s.equals(preferredText)) {
                continue;
            }
            Label matchString = new Label(s);
            VBox.setMargin(matchString, new Insets(0.0, 0.0, 0.0, 10.0));
            box.getChildren().add(matchString);

        }

        String uuid = wbConcept.getUUIDs().get(0).toString();
        prefTermArray.add(idx, preferredText);
        uuidArray.add(idx, uuid);
        box.setOnMouseClicked(new LookAheadConceptPopup.LookAheadSelectHandler(preferredText, uuid));

        return box;
    }

    private void setBoxStyle(VBox box, int index) {
        if (index == 0 || index % 2 == 0) {
            box.getStyleClass().add("lookupSearchResultsStyle-A");
        } else {
            box.getStyleClass().add("lookupSearchResultsStyle-B");
        }
    }

    private class LookAheadSelectHandler implements EventHandler {

        String prefTerm;
        String uuid;

        private LookAheadSelectHandler(String term, String id) {
            prefTerm = term;
            uuid = id;
        }

        @Override
        public void handle(Event t) {
            if (uuid == null) {
                return;
            }

            ComboBoxConcept con = new ComboBoxConcept(prefTerm, uuid);

            comboBox.setValue(con);
            hide();
        }
    }

    private class LookAheadScrollEvent implements EventHandler {

        @Override
        public void handle(Event t) {
            KeyEvent event = (KeyEvent) t;
            int oldSelection = currentSelection;
            boolean noAction = false;

            if (event.getCode() == KeyCode.ENTER) {
                if (currentSelection >= 0 && currentSelection < searchResults.getChildren().size()) {
                    ComboBoxConcept con = new ComboBoxConcept(prefTermArray.get(currentSelection), uuidArray.get(currentSelection));

                    comboBox.getEditor().setText(uuidArray.get(currentSelection));
                    hide();
                }
            }
            if (event.getCode() == KeyCode.UP) {
                if (currentSelection > 0) {
                    currentSelection--;
                }
            } else if (event.getCode() == KeyCode.DOWN) {
                if (currentSelection < searchResults.getChildren().size() - 1) {
                    currentSelection++;
                } else {
                    noAction = true;
                }
            }

            if (!noAction) {
                if (oldSelection >= 0) {
                    VBox oldBox = (VBox) searchResults.getChildren().get(oldSelection);
                    oldBox.getStyleClass().clear();
                    setBoxStyle(oldBox, oldSelection);
                }

                VBox newBox = (VBox) searchResults.getChildren().get(currentSelection);
                newBox.getStyleClass().clear();
                newBox.getStyleClass().add("lookupSearchResultsStyle-Selected");
            }

            event.consume();
        }
    }

    private class LookAheadEnterHandler implements EventHandler {

        VBox box;
        boolean isEnterCase;
        private String originalCase;

        private LookAheadEnterHandler(VBox b, boolean isEnter) {
            box = b;
            isEnterCase = isEnter;
            originalCase = box.getStyleClass().get(0);
        }

        @Override
        public void handle(Event t) {
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
}
