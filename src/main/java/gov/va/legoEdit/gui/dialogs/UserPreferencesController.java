package gov.va.legoEdit.gui.dialogs;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.LegoGUIModel;
import gov.va.legoEdit.formats.UserPrefsXMLUtils;
import gov.va.legoEdit.gui.util.Utility;
import gov.va.legoEdit.model.userPrefs.UserPreferences;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserPreferencesController implements Initializable
{
	private static Logger logger = LoggerFactory.getLogger(UserPreferencesController.class);

	@FXML// fx:id="rootPane"
	private AnchorPane rootPane; // Value injected by FXMLLoader
	@FXML// fx:id="cancelButton"
	private Button cancelButton; // Value injected by FXMLLoader
	@FXML// fx:id="legoListDescription"
	private TextField author; // Value injected by FXMLLoader
	@FXML// fx:id="legoListName"
	private TextField module; // Value injected by FXMLLoader
	@FXML// fx:id="legoListUUID"
	private TextField path; // Value injected by FXMLLoader
	@FXML// fx:id="okButton"
	private Button okButton; // Value injected by FXMLLoader
	@FXML// fx:id="summaryView"
	private ChoiceBox<String> summaryView; // Value injected by FXMLLoader

	private UserPreferences up;
	private BooleanProperty authorValid = new SimpleBooleanProperty(true);
	private BooleanProperty moduleValid = new SimpleBooleanProperty(true);
	private BooleanProperty pathValid = new SimpleBooleanProperty(true);

	private BooleanBinding formValid;

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'LegoListProperties.fxml'.";
		assert author != null : "fx:id=\"legoListDescription\" was not injected: check your FXML file 'LegoListProperties.fxml'.";
		assert module != null : "fx:id=\"legoListName\" was not injected: check your FXML file 'LegoListProperties.fxml'.";
		assert path != null : "fx:id=\"legoListUUID\" was not injected: check your FXML file 'LegoListProperties.fxml'.";
		assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'LegoListProperties.fxml'.";

		// initialize your logic here: all @FXML variables will have been injected

		up = LegoGUIModel.getInstance().getUserPreferences();

		author.setText(up.getAuthor());
		module.setText(up.getModule());
		path.setText(up.getPath());
		summaryView.getSelectionModel().select(up.getShowSummary() ? 0 : 1);

		formValid = new BooleanBinding()
		{
			{
				super.bind(authorValid, moduleValid, pathValid);
			}

			@Override
			protected boolean computeValue()
			{
				return authorValid.get() && moduleValid.get() && pathValid.get();
			}
		};

		cancelButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				author.setText(up.getAuthor());
				module.setText(up.getModule());
				path.setText(up.getPath());
				((Stage) rootPane.getScene().getWindow()).close();
			}
		});

		okButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				up.setAuthor(author.getText());
				up.setModule(module.getText());
				up.setPath(path.getText());
				up.setShowSummary(summaryView.getSelectionModel().getSelectedIndex() == 0 ? true : false);
				try
				{
					UserPrefsXMLUtils.writeUserPreferences(up);
				}
				catch (JAXBException e)
				{
					logger.error("Unexpected error storing preferences", e);
					LegoGUI.getInstance().showErrorDialog("Error storing preferences", "Failed to store the user preferences", e.toString());
				}

				((Stage) rootPane.getScene().getWindow()).close();
			}
		});

		okButton.disableProperty().bind(formValid.not());

		author.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				authorValid.set(newValue.length() > 0);
				if (authorValid.get())
				{
					author.setEffect(null);
				}
				else
				{
					author.setEffect(Utility.redDropShadow);
				}
			}
		});

		module.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				moduleValid.set(newValue.length() > 0);
				if (moduleValid.get())
				{
					module.setEffect(null);
				}
				else
				{
					module.setEffect(Utility.redDropShadow);
				}
			}
		});

		path.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				pathValid.set(newValue.length() > 0);
				if (pathValid.get())
				{
					path.setEffect(null);
				}
				else
				{
					path.setEffect(Utility.redDropShadow);
				}
			}
		});
	}
}
