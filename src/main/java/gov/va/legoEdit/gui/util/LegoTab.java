package gov.va.legoEdit.gui.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import com.sun.javafx.scene.control.skin.LabelSkin;
import com.sun.javafx.scene.control.skin.TabPaneSkin;

public class LegoTab extends Tab
{
    private String displayedLegoID;
    
    public LegoTab(String tabName, String displayedLegoID)
    {
        super(tabName);
        this.displayedLegoID = displayedLegoID;
        this.setClosable(false); // Don't show the native close button
        final StackPane closeBtn = new StackPane()
        {
            @Override
            protected void layoutChildren()
            {
                super.layoutChildren();
                // Setting the orientation of graphic(button) to the right side.
                ((Label) ((LabelSkin) getParent()).getSkinnable()).setStyle("-fx-content-display:right;");
            }
        };
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
                // My logic to handle the close event or not.
                if (true)
                {
                    //TODO write optional close logic
                    closeEvent.handle(null);
                }
                //else don't close... 
            }
        });

        // Showing the close button if the tab is selected.
        this.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> paramObservableValue, Boolean paramT1,
                    Boolean isSelected)
            {
                if (isSelected)
                {
                    LegoTab.this.setGraphic(closeBtn);
                }
                else
                {
                    LegoTab.this.setGraphic(null);
                }
            }
        });
    }
    
    public String getDisplayedLegoID()
    {
        return this.displayedLegoID;
    }
}
