package gov.va.legoEdit;

import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.gui.dialogs.CreateLegoController;
import gov.va.legoEdit.gui.dialogs.ErrorDialogController;
import gov.va.legoEdit.gui.dialogs.LegoListPropertiesController;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.xmlView.XMLDisplayWindow;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.schemaModel.LegoList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class LegoGUI extends Application
{
	private static Logger logger = LoggerFactory.getLogger(LegoGUI.class);
	private static LegoGUI instance_;
	
	private Stage mainStage_;
	private Stage errorDialogStage_;
	private Stage legoListPropertiesStage_;
	private Stage createLegoStage_;
	
	private ErrorDialogController edc_;
	private LegoGUIController lgc_;
	private LegoListPropertiesController llpc_;
	private CreateLegoController clc_;

	public LegoGUI()
	{
		instance_ = this;
	}

	public static LegoGUI getInstance()
	{
		return instance_;
	}

	public static void main(String[] args)
	{
		logger.info("Lego Editor Startup");

		// Redirect the unconfigured java.util logging to our logger.
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		// TODO there are some performance implications of the above... that can be mitigated by using logback.
		// Need to get switched over to logback (but it isn't in the repo at the moment)
		// http://www.slf4j.org/legacy.html
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception
	{
		mainStage_ = stage;
		FXMLLoader loader = new FXMLLoader();
		Scene scene = new Scene((Parent)loader.load(LegoGUI.class.getResourceAsStream("LegoGUI.fxml")));
		scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
		mainStage_.setScene(scene);
		lgc_ = loader.getController();
		mainStage_.getIcons().add(new Image(LegoGUI.class.getResourceAsStream("/fugue/16x16/icons/application-block.png")));
		mainStage_.setTitle("Lego Editor");
		lgc_.finishInit();
		mainStage_.show();
		
		//Init error dialog
		errorDialogStage_ = new Stage();
		errorDialogStage_.initModality(Modality.WINDOW_MODAL);
		errorDialogStage_.initOwner(mainStage_);
		errorDialogStage_.initStyle(StageStyle.UTILITY);
		
		loader = new FXMLLoader();
		scene = new Scene((Parent)loader.load(ErrorDialogController.class.getResourceAsStream("ErrorDialog.fxml")));
		edc_ = loader.getController();
		scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
		errorDialogStage_.setScene(scene);
		
		//init legoPropertiesDialog
		legoListPropertiesStage_ = new Stage();
		legoListPropertiesStage_.initModality(Modality.WINDOW_MODAL);
		legoListPropertiesStage_.initOwner(mainStage_);
		legoListPropertiesStage_.initStyle(StageStyle.UTILITY);
		loader = new FXMLLoader();
		scene = new Scene((Parent)loader.load(ErrorDialogController.class.getResourceAsStream("LegoListProperties.fxml")));
		llpc_ = loader.getController();
		scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
		legoListPropertiesStage_.setScene(scene);
		
		//init createLegoDialog
		createLegoStage_ = new Stage();
		createLegoStage_.initModality(Modality.WINDOW_MODAL);
		createLegoStage_.initOwner(mainStage_);
		createLegoStage_.initStyle(StageStyle.UTILITY);
		loader = new FXMLLoader();
        scene = new Scene((Parent)loader.load(CreateLegoController.class.getResourceAsStream("CreateLego.fxml")));
        clc_ = loader.getController();
        scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
        createLegoStage_.setScene(scene);
	}

	public LegoGUIController getLegoGUIController()
	{
	    return lgc_;
	}
	
	public void showErrorDialog(String title, String errorMessage, String detailedErrorMessage)
	{
		edc_.setVariables(title, errorMessage, detailedErrorMessage);
		errorDialogStage_.setTitle(title);
		errorDialogStage_.showAndWait();
	}
	
	public void showCreateLegoDialog(LegoListByReference llbr, LegoTreeItem ti)
    {
	    clc_.init(llbr, ti);
	    createLegoStage_.show();
    }
	
	/**
	 * Returns true if they made changes to the properties, false otherwise
	 */
	public void showLegoListPropertiesDialog(String name, String uuid, StringProperty description)
	{
	    llpc_.setVariables(name, uuid, description);
	    legoListPropertiesStage_.show();
	}
	
	protected Stage getMainStage()
	{
	    return mainStage_;
	}

	public void showXMLViewWindow(final LegoList ll)
    {
        final XMLDisplayWindow xdw = new XMLDisplayWindow(mainStage_, ll.getGroupName());
        xdw.show();
        Runnable r = new Runnable()
        {
            String htmlContent;

            @Override
            public void run()
            {
                try
                {
                    htmlContent = LegoXMLUtils.toHTML(ll);
                }
                catch (Exception e)
                {
                    logger.error("There was an error formatting the lego as XML", e);
                    htmlContent = "There was an error formatting the lego as XML";
                }
                Platform.runLater(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        xdw.setContent(htmlContent);
                    }
                });
            }
        };
        Thread t = new Thread(r, "XMLViewThread");
        t.setDaemon(true);
        t.start();
    }
}
