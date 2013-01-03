package gov.va.legoEdit.gui.util;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.dialogs.YesNoDialogController.Answer;
import gov.va.legoEdit.gui.legoInfoPanel.LegoInfoPanel;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeNodeType;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeView;
import gov.va.legoEdit.model.ModelUtil;
import gov.va.legoEdit.model.SchemaEquals;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.util.TimeConvert;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import com.sun.javafx.scene.control.skin.TabPaneSkin;

public class LegoTab extends Tab
{
    private Lego displayedLego;
    private LegoInfoPanel lip;
    private BooleanBinding legoNeedsSaving;
    private ImageView open = Images.LEGO.createImageView();
    private ImageView openEdited = Images.LEGO_EDIT.createImageView();
    
    public LegoTab(String tabName, Lego displayedLego)
    {
        super();
        this.displayedLego = displayedLego;
        this.setClosable(false); // Don't show the native close button
        HBox hbox = new HBox();
        final Label titleLabel = new Label(tabName);
        titleLabel.setAlignment(Pos.CENTER_LEFT);
        titleLabel.setGraphic(open);
        titleLabel.getStyleClass().clear();
        titleLabel.getStyleClass().add("tab-label");
        titleLabel.setMaxHeight(Double.MAX_VALUE);
        hbox.getChildren().add(titleLabel);
        
        final StackPane closeBtn = new StackPane();
        closeBtn.getStyleClass().setAll(new String[] { "tab-close-button" });
        closeBtn.setStyle("-fx-cursor:hand;");
        closeBtn.setPadding(new Insets(0, 7, 0, 7));
        closeBtn.visibleProperty().bind(this.selectedProperty());

        final EventHandler<ActionEvent> closeEvent = new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent paramT)
            {
                ((TabPaneSkin) LegoTab.this.getTabPane().getSkin()).getBehavior().closeTab(LegoTab.this);
            }
        };

        // Handler for the close button.
        closeBtn.setOnMouseReleased(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent paramT)
            {
                if (legoNeedsSaving.get())
                {
                    Answer answer = LegoGUI.getInstance().showYesNoDialog("Unsaved Changes", "This Lego has unsaved changes.  Do you want to close anyway?");
                    if (answer == null || answer == Answer.NO)
                    {
                        //don't close
                        return;
                    }
                }
                
                closeEvent.handle(null);
            }
        });
        
        hbox.getChildren().add(closeBtn);
        setGraphic(hbox);
       
        legoNeedsSaving = new BooleanBinding()
        {
            {
                invalidate();
            }
            @Override
            protected boolean computeValue()
            {
                Lego storedLego = BDBDataStoreImpl.getInstance().getLego(LegoTab.this.displayedLego.getLegoUUID(), LegoTab.this.displayedLego.getStamp().getUuid());
                return !SchemaEquals.equals(storedLego, LegoTab.this.displayedLego);
            }
        };
        
        legoNeedsSaving.addListener(new ChangeListener<Boolean>()
        {
            
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (newValue)
                {
                    titleLabel.setGraphic(openEdited);
                }
                else
                {
                    titleLabel.setGraphic(open);
                }
            }
        });
        
        lip = new LegoInfoPanel(displayedLego.getPncs().getName(), displayedLego.getPncs().getValue(), displayedLego.getPncs().getId() + "",
                displayedLego.getLegoUUID(), displayedLego.getStamp().getAuthor(), displayedLego.getStamp().getModule(), 
                TimeConvert.format(displayedLego.getStamp().getTime()), displayedLego.getStamp().getPath());
        
        LegoTreeView legoTree = new LegoTreeView();
        legoTree.setEditable(false);
        legoTree.setLegoTab(this);
        
        BorderPane bp = new BorderPane();
        bp.setTop(lip.getPane());
        bp.setCenter(legoTree.wrapInScrollPane());
        this.setContent(bp);
        
        legoTree.getRoot().getChildren().add(new LegoTreeItem(displayedLego.getStamp(), LegoTreeNodeType.status));
        for (Assertion a : displayedLego.getAssertion())
        {
            legoTree.getRoot().getChildren().add(new LegoTreeItem(a));
        }
        legoTree.getRoot().getChildren().add(new LegoTreeItem(LegoTreeNodeType.blankLegoEndNode));
        recursiveSort(legoTree.getRoot().getChildren());
        expandAll(legoTree.getRoot());
    }
    
    public String getDisplayedLegoID()
    {
        return ModelUtil.makeUniqueLegoID(displayedLego);
    }
    
    public Lego getLego()
    {
        return displayedLego;
    }
    
    public BooleanBinding hasUnsavedChangesProperty()
    {
        return legoNeedsSaving;
    }
    
    public void updateInfoPanel(Stamp stamp)
    {
        lip.update(stamp.getAuthor(), stamp.getModule(), TimeConvert.format(stamp.getTime()), stamp.getPath());
    }
    
    private void recursiveSort(ObservableList<TreeItem<String>> items)
    {
        FXCollections.sort(items, new LegoTreeItemComparator(true));
        for (TreeItem<String> item : items)
        {
            recursiveSort(item.getChildren());
        }
    }
    
    private void expandAll(TreeItem<String> ti)
    {
        ti.setExpanded(true);
        for (TreeItem<String> tiChild : ti.getChildren())
        {
            expandAll(tiChild);
        }
    }
}
