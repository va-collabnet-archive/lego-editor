package gov.va.legoEdit.gui.cem;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.cem.CIMIXmlReader.CIMIXML;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeNodeType;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.model.LegoListByReference;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CimiImportViewController implements Initializable
{
	private static Logger logger = LoggerFactory.getLogger(CimiImportViewController.class);

	@FXML// fx:id="chooseCimiFile"
	private Button chooseCimiFile; // Value injected by FXMLLoader
	@FXML// fx:id="chooseValuesetFile"
	private Button chooseValuesetFile; // Value injected by FXMLLoader
	@FXML// fx:id="cimiFileName"
	private Label cimiFileName; // Value injected by FXMLLoader
	@FXML// fx:id="cimiFilePreview"
	private ScrollPane cimiFilePreview; // Value injected by FXMLLoader
	@FXML// fx:id="createLego"
	private Button createLego; // Value injected by FXMLLoader
	@FXML// fx:id="findLego"
	private Button findLego; // Value injected by FXMLLoader
	@FXML// fx:id="valueSetList"
	private ListView<String> valueSetList; // Value injected by FXMLLoader
	@FXML// fx:id="valuesetFileName"
	private Label valuesetFileName; // Value injected by FXMLLoader
	@FXML// fx:id="valuesetMembers"
	private ListView<String> valuesetMembers; // Value injected by FXMLLoader
	@FXML// fx:id="rootPane"
	private AnchorPane rootPane; // Value injected by FXMLLoader

	private Stage stage;
	private File cimiXMLFile = null;
	private File valuesetFile = null;
	private HashMap<String, ValueSet> valuesets = null;
	private CIMIXML cimiXML = null;

	private BooleanProperty enableButtons = new SimpleBooleanProperty(false);

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert chooseCimiFile != null : "fx:id=\"chooseCimiFile\" was not injected: check your FXML file 'cimiImport.fxml'.";
		assert chooseValuesetFile != null : "fx:id=\"chooseValuesetFile\" was not injected: check your FXML file 'cimiImport.fxml'.";
		assert cimiFileName != null : "fx:id=\"cimiFileName\" was not injected: check your FXML file 'cimiImport.fxml'.";
		assert cimiFilePreview != null : "fx:id=\"cimiFilePreview\" was not injected: check your FXML file 'cimiImport.fxml'.";
		assert createLego != null : "fx:id=\"createLego\" was not injected: check your FXML file 'cimiImport.fxml'.";
		assert findLego != null : "fx:id=\"findLego\" was not injected: check your FXML file 'cimiImport.fxml'.";
		assert valueSetList != null : "fx:id=\"valueSetList\" was not injected: check your FXML file 'cimiImport.fxml'.";
		assert valuesetFileName != null : "fx:id=\"valuesetFileName\" was not injected: check your FXML file 'cimiImport.fxml'.";
		assert valuesetMembers != null : "fx:id=\"valuesetMembers\" was not injected: check your FXML file 'cimiImport.fxml'.";

		chooseCimiFile.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent t)
			{
				FileChooser fc = new FileChooser();
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Cimi xml Files (*.xml)", "*.xml"));
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*"));
				cimiXMLFile = fc.showOpenDialog(rootPane.getScene().getWindow());
				if (cimiXMLFile != null)
				{
					cimiFileName.setText(cimiXMLFile.getName());
					valueSetList.getItems().clear();
					valuesetMembers.getItems().clear();
					processCIMI();
				}
				stage.toFront();
			}
		});

		chooseValuesetFile.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent t)
			{
				FileChooser fc = new FileChooser();
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Valueset Spreadsheet(*.xls)", "*.xls"));
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*"));
				valuesetFile = fc.showOpenDialog(rootPane.getScene().getWindow());
				if (valuesetFile != null)
				{
					valuesetMembers.getItems().clear();
					valuesetFileName.setText(valuesetFile.getName());
					try
					{
						valuesets = CEMValueSetReader.read(valuesetFile);
					}
					catch (Exception e)
					{
						LegoGUI.getInstance().showErrorDialog("Error reading file", "Error reading the Valueset file", e.toString(), stage);
						valuesetFileName.setText("<no file selected>");
						valuesetFile = null;
						valuesets = null;
					}
					processCIMI();
				}
				stage.toFront();
			}
		});

		createLego.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent t)
			{
				LegoTreeItem lti = LegoGUI.getInstance().getLegoGUIController().getLegoFilterPaneController().getCurrentSelection();
				LegoListByReference llbr = null;
				while (lti != null)
				{
					if (lti.getNodeType() == LegoTreeNodeType.legoListByReference)
					{
						llbr = (LegoListByReference) lti.getExtraData();
						break;
					}
					lti = lti.getLegoParent();
				}
				if (llbr == null)
				{
					LegoGUI.getInstance().showErrorDialog("Please select a Lego List", "Please select a Lego List to create the lego under in the main Lego GUI", null, stage);
				}
				else
				{
					String name = valueSetList.getSelectionModel().getSelectedItem();
					name = name.substring(0, name.indexOf(" : "));
					LegoGUI.getInstance().showCreateLegoDialog(llbr, lti, name,
							valuesetMembers.getSelectionModel().getSelectedItem());
				}
			}
		});
		createLego.disableProperty().bind(enableButtons.not());

		findLego.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent t)
			{
				String name = valueSetList.getSelectionModel().getSelectedItem();
				name = name.substring(0, name.indexOf(" : "));
				LegoGUI.getInstance().getLegoGUIController().getLegoFilterPaneController()
						.filterOnPncs(name, valuesetMembers.getSelectionModel().getSelectedItem());
			}
		});
		findLego.disableProperty().bind(enableButtons.not());

		cimiFilePreview.setFitToWidth(true);

		valueSetList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
		{
			public void changed(ObservableValue<? extends String> ov, String old_val, String new_val)
			{
				valuesetMembers.getItems().clear();
				if (new_val != null)
				{
					String id = new_val.substring(new_val.indexOf(" : ") + 3);
					ValueSet vs = valuesets.get(id);
					for (String s : vs.getValues())
					{
						valuesetMembers.getItems().add(s);
					}
				}
			}
		});

		valuesetMembers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
		{
			public void changed(ObservableValue<? extends String> ov, String old_val, String new_val)
			{
				enableButtons.set(valuesetMembers.getSelectionModel().getSelectedItem() != null);
			}
		});
	}

	private void processCIMI()
	{
		valueSetList.getItems().clear();
		if (cimiXMLFile != null && valuesetFile != null)
		{
			try
			{
				cimiXML = CIMIXmlReader.buildNode(cimiXMLFile);
				cimiFilePreview.setContent(cimiXML.node);
				for (String s : cimiXML.valuesetIDs)
				{
					ValueSet vs = valuesets.get(s.toLowerCase());
					if (vs != null)
					{
						valueSetList.getItems().add(vs.getName() + " : " + vs.getId());
					}
				}
			}
			catch (Exception e)
			{
				LegoGUI.getInstance().showErrorDialog("Error reading file", "Error processing the XML file", e.toString(), stage);
				cimiFilePreview.setContent(new Label(""));
				cimiFileName.setText("<no file selected>");
				cimiXMLFile = null;
				valueSetList.getItems().clear();
			}
		}
		else
		{
			cimiFilePreview.setContent(new Label(""));
		}
	}

	private void setStage(Stage stage)
	{
		this.stage = stage;
	}

	public static void show(Stage mainStage)
	{
		try
		{
			Stage stage = new Stage();
			stage.initModality(Modality.NONE);
			stage.initOwner(mainStage);
			stage.initStyle(StageStyle.DECORATED);
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(CimiImportViewController.class.getResource("CimiImport.fxml"));
			Scene scene = new Scene((Parent) loader.load(CimiImportViewController.class.getResourceAsStream("CimiImport.fxml")));
			scene.getStylesheets().add(CimiImportViewController.class.getResource("/styles.css").toString());
			stage.setScene(scene);
			stage.setTitle("CIMI Import/Search Tool");
			stage.getIcons().add(Images.XML_VIEW_32.getImage());
			stage.show();
			((CimiImportViewController) loader.getController()).setStage(stage);
		}
		catch (Exception e)
		{
			logger.error("Unexpected error displaying about dialog", e);
			LegoGUI.getInstance().showErrorDialog("Unexpected Error", "Unexpected Error displaying about dialog", e.toString(), null);
		}
	}
}
