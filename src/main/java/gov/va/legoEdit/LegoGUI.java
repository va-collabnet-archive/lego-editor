package gov.va.legoEdit;

import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.gui.cem.CimiImportViewController;
import gov.va.legoEdit.gui.dialogs.AboutDialogController;
import gov.va.legoEdit.gui.dialogs.CreateLegoController;
import gov.va.legoEdit.gui.dialogs.CreateTemplateController;
import gov.va.legoEdit.gui.dialogs.ErrorDialogController;
import gov.va.legoEdit.gui.dialogs.ExportDialogController;
import gov.va.legoEdit.gui.dialogs.ImportDialogController;
import gov.va.legoEdit.gui.dialogs.LegoListPropertiesController;
import gov.va.legoEdit.gui.dialogs.SnomedConceptViewController;
import gov.va.legoEdit.gui.dialogs.UserPreferencesController;
import gov.va.legoEdit.gui.dialogs.YesNoDialogController;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.gui.xmlView.XMLDisplayWindow;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.wb.WBDataStore;
import gov.va.legoEdit.util.Utility;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.ihtsdo.fxmodel.concept.FxConcept;
import org.ihtsdo.fxmodel.fetchpolicy.RefexPolicy;
import org.ihtsdo.fxmodel.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.fxmodel.fetchpolicy.VersionPolicy;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.coordinate.StandardViewCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import com.sun.javafx.tk.Toolkit;

public class LegoGUI extends Application
{
	private static Logger logger = LoggerFactory.getLogger(LegoGUI.class);
	private static LegoGUI instance_;

	private Stage mainStage_;
	private Stage errorDialogStage_;
	private Stage legoListPropertiesStage_;
	private Stage createLegoStage_;
	private Stage yesNoStage_;
	private Stage userPrefsStage_;
	private Stage templateStage_;

	private ErrorDialogController edc_;
	private LegoGUIController lgc_;
	private LegoListPropertiesController llpc_;
	private CreateLegoController clc_;
	private YesNoDialogController yndc_;
	private CreateTemplateController ctc_;

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
		// TODO LOGGER there are some performance implications of the above... that can be mitigated by using logback.
		// Need to get switched over to logback (but it isn't in the repo at the moment)
		// http://www.slf4j.org/legacy.html
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception
	{
		mainStage_ = stage;
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(LegoGUI.class.getResource("LegoGUI.fxml"));
		Scene scene = new Scene((Parent) loader.load(LegoGUI.class.getResourceAsStream("LegoGUI.fxml")));
		scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
		mainStage_.setScene(scene);
		lgc_ = loader.getController();
		mainStage_.getIcons().add(Images.APPLICATION.getImage());
		mainStage_.setTitle("Lego Editor");
		lgc_.finishInit();
		mainStage_.show();

		// Init error dialog
		errorDialogStage_ = new Stage();
		errorDialogStage_.initModality(Modality.WINDOW_MODAL);
		errorDialogStage_.initOwner(mainStage_);
		errorDialogStage_.initStyle(StageStyle.UTILITY);
		loader = new FXMLLoader();
		loader.setLocation(ErrorDialogController.class.getResource("ErrorDialog.fxml"));
		scene = new Scene((Parent) loader.load(ErrorDialogController.class.getResourceAsStream("ErrorDialog.fxml")));
		edc_ = loader.getController();
		scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
		errorDialogStage_.setScene(scene);

		// init legoPropertiesDialog
		legoListPropertiesStage_ = new Stage();
		legoListPropertiesStage_.initModality(Modality.WINDOW_MODAL);
		legoListPropertiesStage_.initOwner(mainStage_);
		legoListPropertiesStage_.initStyle(StageStyle.UTILITY);
		loader = new FXMLLoader();
		scene = new Scene((Parent) loader.load(ErrorDialogController.class.getResourceAsStream("LegoListProperties.fxml")));
		llpc_ = loader.getController();
		scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
		legoListPropertiesStage_.setScene(scene);

		// init createLegoDialog
		createLegoStage_ = new Stage();
		createLegoStage_.initModality(Modality.WINDOW_MODAL);
		createLegoStage_.initOwner(mainStage_);
		createLegoStage_.initStyle(StageStyle.UTILITY);
		loader = new FXMLLoader();
		scene = new Scene((Parent) loader.load(CreateLegoController.class.getResourceAsStream("CreateLego.fxml")));
		clc_ = loader.getController();
		scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
		createLegoStage_.setScene(scene);

		// init yesNoDialog
		yesNoStage_ = new Stage();
		yesNoStage_.initModality(Modality.WINDOW_MODAL);
		yesNoStage_.initOwner(mainStage_);
		yesNoStage_.initStyle(StageStyle.UTILITY);
		loader = new FXMLLoader();
		loader.setLocation(YesNoDialogController.class.getResource("YesNoDialog.fxml"));
		scene = new Scene((Parent) loader.load(YesNoDialogController.class.getResourceAsStream("YesNoDialog.fxml")));
		yndc_ = loader.getController();
		scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
		yesNoStage_.setScene(scene);

		// init userPrefs dialog
		userPrefsStage_ = new Stage();
		userPrefsStage_.initModality(Modality.WINDOW_MODAL);
		userPrefsStage_.initOwner(mainStage_);
		userPrefsStage_.initStyle(StageStyle.UTILITY);
		loader = new FXMLLoader();
		scene = new Scene((Parent) loader.load(UserPreferencesController.class.getResourceAsStream("UserPreferences.fxml")));
		scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
		userPrefsStage_.setScene(scene);

		// init create template dialog
		templateStage_ = new Stage();
		templateStage_.initModality(Modality.WINDOW_MODAL);
		templateStage_.initOwner(mainStage_);
		templateStage_.initStyle(StageStyle.UTILITY);
		loader = new FXMLLoader();
		loader.setLocation(CreateTemplateController.class.getResource("CreateTemplate.fxml"));
		scene = new Scene((Parent) loader.load(CreateTemplateController.class.getResourceAsStream("CreateTemplate.fxml")));
		scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
		ctc_ = loader.getController();
		templateStage_.setScene(scene);
	}

	public LegoGUIController getLegoGUIController()
	{
		return lgc_;
	}
	
	public void showErrorDialog(final String title, final String errorMessage, final String detailedErrorMessage)
	{
		showErrorDialog(title, errorMessage, detailedErrorMessage, null);
	}

	public void showErrorDialog(final String title, final String errorMessage, final String detailedErrorMessage, final Window owner)
	{
		while (edc_ == null)
		{
			//If we have an error during startup, the GUI might not be up yet.  Of course, it may never come up either... but 
			//the app is so hosed at that point... not going to worry about it.
			try
			{
				Thread.sleep(50);
			}
			catch (InterruptedException e)
			{
				//noop
			}
		}
		try
		{
			Toolkit.getToolkit().checkFxUserThread();
			edc_.setVariables(errorMessage, detailedErrorMessage);
			errorDialogStage_.setTitle(title);
			errorDialogStage_.initOwner(owner == null ? mainStage_ : owner);
			errorDialogStage_.show();
		}
		catch (IllegalStateException e)
		{
			Platform.runLater(new Runnable()
			{
				
				@Override
				public void run()
				{
					edc_.setVariables(errorMessage, detailedErrorMessage);
					errorDialogStage_.setTitle(title);
					errorDialogStage_.initOwner(owner == null ? mainStage_ : owner);
					errorDialogStage_.show();
				}
			});
		}
	}

	public void showImportDialog(List<File> filesToImport)
	{
		try
		{
			Stage stage = new Stage();
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(mainStage_);
			stage.initStyle(StageStyle.UTILITY);
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(ErrorDialogController.class.getResource("ImportDialog.fxml"));
			Scene scene = new Scene((Parent) loader.load(ImportDialogController.class.getResourceAsStream("ImportDialog.fxml")));
			ImportDialogController idc = loader.getController();
			scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
			stage.setScene(scene);
			idc.importFiles(filesToImport);
		}
		catch (IOException e)
		{
			logger.error("Unexpected", e);
			showErrorDialog("Unexpected error", "Unexpected error importing files", e.toString());
		}
	}

	public void showCreateLegoDialog(LegoListByReference llbr, LegoTreeItem ti, boolean fromPaste)
	{
		clc_.init(llbr, ti, fromPaste);
		createLegoStage_.show();
	}
	
	public void showCreateLegoDialog(LegoListByReference llbr, LegoTreeItem ti, String suggestedName, String suggestedValue)
	{
		clc_.init(llbr, ti, suggestedName, suggestedValue);
		createLegoStage_.show();
	}

	public void showUserPreferences()
	{
		userPrefsStage_.show();
	}
	
	public void showCimiTool()
	{
		CimiImportViewController.show(mainStage_);
	}

	public YesNoDialogController.Answer showYesNoDialog(String title, String question)
	{
		yndc_.init(question);
		yesNoStage_.setTitle(title);
		yesNoStage_.showAndWait();
		return yndc_.getAnswer();
	}

	public void showSnomedConceptDialog(UUID conceptUUID)
	{
		try
		{
			showSnomedConceptDialog(WBDataStore.Ts().getFxConcept(conceptUUID, StandardViewCoordinates.getSnomedLatest(), VersionPolicy.LAST_VERSIONS,
					RefexPolicy.REFEX_MEMBERS, RelationshipPolicy.ORIGINATING_RELATIONSHIPS));
		}
		catch (IOException | ContradictionException e)
		{
			logger.error("Unexpected error displaying snomed concept view", e);
			showErrorDialog("Unexpected Error", "Unexpected Error displaying snomed concept view", e.toString());
		}
	}

	public void showSnomedConceptDialog(FxConcept concept)
	{
		try
		{
			Stage conceptStage = new Stage();
			conceptStage.initModality(Modality.NONE);
			conceptStage.initOwner(mainStage_);
			conceptStage.initStyle(StageStyle.DECORATED);
			FXMLLoader loader = new FXMLLoader();
			Scene scene = new Scene((Parent) loader.load(SnomedConceptViewController.class.getResourceAsStream("SnomedConceptView.fxml")));
			SnomedConceptViewController controller = loader.getController();
			scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
			conceptStage.setScene(scene);
			controller.init(concept);
			conceptStage.setTitle(controller.getTitle());
			conceptStage.getIcons().add(Images.CONCEPT_VIEW.getImage());
			conceptStage.show();
		}
		catch (Exception e)
		{
			logger.error("Unexpected error displaying snomed concept view", e);
			showErrorDialog("Unexpected Error", "Unexpected Error displaying snomed concept view", e.toString());
		}
	}
	
	public void showAddPendingConcept()
	{
		try
		{
			Stage stage = new Stage();
			stage.initModality(Modality.NONE);
			stage.initOwner(mainStage_);
			stage.initStyle(StageStyle.DECORATED);
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(ErrorDialogController.class.getResource("CreatePendingConcept.fxml"));
			Scene scene = new Scene((Parent) loader.load(ImportDialogController.class.getResourceAsStream("CreatePendingConcept.fxml")));
			scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
			stage.setScene(scene);
			stage.setTitle("Add Pending Concept");
			stage.getIcons().add(Images.CONCEPT_VIEW.getImage());
			stage.show();
		}
		catch (IOException e)
		{
			logger.error("Unexpected", e);
			showErrorDialog("Unexpected error", "Unexpected error", e.toString());
		}
	}


	public void showLegoListPropertiesDialog(LegoListByReference llbr, TreeItem<String> ti)
	{
		llpc_.setVariables(llbr, ti);
		legoListPropertiesStage_.show();
	}

	public void showCreateTemplateDialog(Object template)
	{
		ctc_.setVariables(template);
		templateStage_.show();
	}
	
	/**
	 * Null can be sent in to request an export of all legos...
	 * @param legoLists
	 */
	public void showExportDialog(List<LegoList> legoLists)
	{
		try
		{
			Stage exportStage = new Stage();
			exportStage.initModality(Modality.WINDOW_MODAL);
			exportStage.initOwner(mainStage_);
			exportStage.initStyle(StageStyle.UTILITY);
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(ExportDialogController.class.getResource("ExportDialog.fxml"));
			Scene scene = new Scene((Parent) loader.load(ExportDialogController.class.getResourceAsStream("ExportDialog.fxml")));
			ExportDialogController exportDC = loader.getController();
			scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
			exportStage.setScene(scene);
			exportDC.exportFiles(legoLists);
		}
		catch (IOException e)
		{
			logger.error("Unexpected error", e);
			showErrorDialog("Unexpected Error", "Sorry, unexpected error trying to initialize export",  null);
		}
	}
	
	public void showAboutDialog()
	{
		try
		{
			Stage aboutStage = new Stage();
			aboutStage.initModality(Modality.APPLICATION_MODAL);
			aboutStage.initOwner(mainStage_);
			aboutStage.initStyle(StageStyle.DECORATED);
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(AboutDialogController.class.getResource("AboutDialog.fxml"));
			Scene scene = new Scene((Parent) loader.load(AboutDialogController.class.getResourceAsStream("AboutDialog.fxml")));
			scene.getStylesheets().add(LegoGUI.class.getResource("/styles.css").toString());
			aboutStage.setScene(scene);
			aboutStage.setTitle("About Lego Editor");
			aboutStage.getIcons().add(Images.INFO.getImage());
			aboutStage.show();
		}
		catch (Exception e)
		{
			logger.error("Unexpected error displaying about dialog", e);
			showErrorDialog("Unexpected Error", "Unexpected Error displaying about dialog", e.toString());
		}
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
		Utility.tpe.submit(r);
	}
}
