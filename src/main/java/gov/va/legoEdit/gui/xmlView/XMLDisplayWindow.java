package gov.va.legoEdit.gui.xmlView;

import gov.va.legoEdit.gui.util.Images;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class XMLDisplayWindow extends Stage
{
    private WebView wv_;
    ProgressIndicator pi_;
    

    public XMLDisplayWindow(Window owner, String title)
    {
        super(StageStyle.DECORATED);
        initModality(Modality.NONE);
        initOwner(owner);
        setWidth(800);
        setHeight(600);
        setTitle(title);
        getIcons().add(Images.XML_VIEW_16.getImage());
        getIcons().add(Images.XML_VIEW_32.getImage());

        BorderPane bp = new BorderPane();
        bp.setCursor(Cursor.WAIT);
        
        pi_ = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        pi_.setMaxHeight(Region.USE_PREF_SIZE);
        pi_.setMaxWidth(Region.USE_PREF_SIZE);
        bp.setCenter(pi_);
        
        wv_ = new WebView();

        Scene scene = new Scene(bp);
        setScene(scene);

        addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent event)
            {
                wv_ = null;
            }
        });

    }

    public void setContent(String content)
    {
        wv_.getEngine().loadContent(content);
        getScene().getRoot().setCursor(Cursor.DEFAULT);
        ((BorderPane)getScene().getRoot()).setCenter(wv_);
        ((BorderPane)getScene().getRoot()).getChildren().remove(pi_);
        pi_ = null;
    }
}