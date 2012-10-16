package gov.va.legoEdit;

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
    private static ErrorDialogController edc_;

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
    }

    public static Stage getMainStage()
    {
        return mainStage_;
    }

    protected static Stage getErrorDialogStage()
    {
        return errorDialogStage_;
    }
    
    protected static void setErrorDialogController(ErrorDialogController edc)
    {
        edc_ = edc;
    }
    
    public static void showErrorDialog(String title, String errorMessage, String detailedErrorMessage)
    {
        edc_.setVariables(errorMessage, detailedErrorMessage);
        errorDialogStage_.setTitle(title);
        errorDialogStage_.showAndWait();
    }
}
