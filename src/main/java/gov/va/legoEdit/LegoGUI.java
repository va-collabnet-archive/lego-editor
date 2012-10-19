package gov.va.legoEdit;

import gov.va.legoEdit.search.PNCS.PncsSearchDialogController;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegoGUI extends Application
{
    private static Logger logger = LoggerFactory.getLogger(LegoGUI.class);
    private static Stage mainStage_;
    private static Stage errorDialogStage_;
    private static Stage pncsSearchDialogStage_;
    private static ErrorDialogController edc_;
    private static PncsSearchDialogController psc_;

    public static void main(String[] args)
    {
        logger.info("Lego Editor Startup");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        LegoGUI.mainStage_ = stage;
        Parent root = FXMLLoader.load(getClass().getResource("LegoGUI.fxml"));

        stage.setScene(new Scene(root));
        stage.getScene().getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
        stage.getIcons().add(new Image(LegoGUI.class.getResourceAsStream("/fugue/16x16/icons/application-block.png")));
        stage.setTitle("Lego Editor");
        stage.show();
        
        errorDialogStage_ = new Stage();
        errorDialogStage_.initModality(Modality.WINDOW_MODAL);
        errorDialogStage_.initOwner(mainStage_);
        errorDialogStage_.initStyle(StageStyle.UTILITY);
        
        Parent errorDialog = FXMLLoader.load(getClass().getResource("ErrorDialog.fxml"));
        errorDialogStage_.setScene(new Scene(errorDialog));

        pncsSearchDialogStage_ = new Stage();
        pncsSearchDialogStage_.initModality(Modality.WINDOW_MODAL);
        pncsSearchDialogStage_.initOwner(mainStage_);
        pncsSearchDialogStage_.initStyle(StageStyle.UTILITY);
        
        Parent pncsSearchDialog = FXMLLoader.load(getClass().getResource("/gov/va/legoEdit/search/PNCS/PNCSSearchDialog.fxml"));
        pncsSearchDialogStage_.setScene(new Scene(pncsSearchDialog));
}

    public static Stage getMainStage()
    {
        return mainStage_;
    }

    protected static Stage getErrorDialogStage()
    {
        return errorDialogStage_;
    }
    
    public static Stage getPNCSSearchDialogStage()
    {
        return pncsSearchDialogStage_;
    }
    
    protected static void setErrorDialogController(ErrorDialogController edc)
    {
        edc_ = edc;
    }
    
    public static void setPNCSSearchDialogController(PncsSearchDialogController edc)
    {
        psc_ = edc;
    }
    
    public static void showErrorDialog(String title, String errorMessage, String detailedErrorMessage)
    {
        edc_.setVariables(errorMessage, detailedErrorMessage);
        errorDialogStage_.setTitle(title);
        errorDialogStage_.showAndWait();
    }

    public static boolean showPNCSSearchDialog(List<String> pncsIdList) 
    {
        psc_.setVariables(pncsIdList);
        pncsSearchDialogStage_.showAndWait();
        return psc_.isDisplaying();
    }
}
