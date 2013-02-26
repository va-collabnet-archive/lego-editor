package gov.va.legoEdit.gui.dialogs;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.util.Utility;
import gov.va.legoEdit.model.PendingConcepts;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.storage.wb.WBUtility;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreatePendingConceptController implements Initializable
{
	Logger logger = LoggerFactory.getLogger(CreatePendingConceptController.class);

	@FXML// fx:id="cancelButton"
	private Button cancelButton; // Value injected by FXMLLoader
	@FXML// fx:id="conceptDescription"
	private TextField conceptDescription; // Value injected by FXMLLoader
	@FXML// fx:id="conceptDescriptionStack"
	private StackPane conceptDescriptionStack; // Value injected by FXMLLoader
	@FXML// fx:id="conceptId"
	private TextField conceptId; // Value injected by FXMLLoader
	@FXML// fx:id="conceptIdStack"
	private StackPane conceptIdStack; // Value injected by FXMLLoader
	@FXML// fx:id="okButton"
	private Button okButton; // Value injected by FXMLLoader
	@FXML// fx:id="parentConcept"
	private TextField parentConcept; // Value injected by FXMLLoader
	@FXML// fx:id="parentConceptStack"
	private StackPane parentConceptStack; // Value injected by FXMLLoader
	@FXML// fx:id="rootPane"
	private AnchorPane rootPane; // Value injected by FXMLLoader

	private Tooltip conceptDescriptionInvalidReason = new Tooltip();
	private Tooltip conceptIdInvalidReason = new Tooltip();
	private Tooltip parentConceptInvalidReason = new Tooltip();
	
	private BooleanBinding conceptDescriptionValid, conceptIdValid, parentConceptValid;
	private Concept selectedParentConcept = null;

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'CreatePendingConcept.fxml'.";
		assert conceptDescription != null : "fx:id=\"conceptDescription\" was not injected: check your FXML file 'CreatePendingConcept.fxml'.";
		assert conceptDescriptionStack != null : "fx:id=\"conceptDescriptionStack\" was not injected: check your FXML file 'CreatePendingConcept.fxml'.";
		assert conceptId != null : "fx:id=\"conceptId\" was not injected: check your FXML file 'CreatePendingConcept.fxml'.";
		assert conceptIdStack != null : "fx:id=\"conceptIdStack\" was not injected: check your FXML file 'CreatePendingConcept.fxml'.";
		assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'CreatePendingConcept.fxml'.";
		assert parentConcept != null : "fx:id=\"parentConcept\" was not injected: check your FXML file 'CreatePendingConcept.fxml'.";
		assert parentConceptStack != null : "fx:id=\"parentConceptStack\" was not injected: check your FXML file 'CreatePendingConcept.fxml'.";
		assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'CreatePendingConcept.fxml'.";

		conceptDescriptionValid = new BooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				if (conceptDescription.getText().length() > 0)
				{
					return true;
				}
				else
				{
					conceptDescriptionInvalidReason.setText("The concept description is required");
					return false;
				}
			}
		};

		conceptIdValid = new BooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				if (conceptId.getText().length() > 0)
				{
					try
					{
						Long.parseLong(conceptId.getText().trim());
						Concept currentConcept = PendingConcepts.getInstance().getConcept(conceptId.getText());
						if (currentConcept != null)
						{
							conceptIdInvalidReason.setText("The specified concept ID already exists as a pending concept");
							return false;
						}
						if (null != WBUtility.lookupSnomedIdentifierAsCV(conceptId.getText()))
						{
							conceptIdInvalidReason.setText("The specified concept ID already exists in snomed");
							return false;
						}
						return true;
					}
					catch (NumberFormatException e)
					{
						conceptIdInvalidReason.setText("The concept ID must be a number");
						return false;
					}
				}
				conceptIdInvalidReason.setText("The concept ID is required");
				return false;
			}
		};

		parentConceptValid = new BooleanBinding()
		{
			@Override
			protected boolean computeValue()
			{
				if (parentConcept.getText().length() > 0)
				{
					if (selectedParentConcept != null && parentConcept.getText().equals(selectedParentConcept.getDesc()))
					{
						//A result of us changing the value to the description.  If they match, we are fine.
						return true;
					}
					
					ConceptVersionBI cv = WBUtility.lookupSnomedIdentifierAsCV(parentConcept.getText());
					if (cv != null)
					{
						selectedParentConcept = WBUtility.convertConcept(cv);
						parentConcept.setText(selectedParentConcept.getDesc());
						return true;
					}
					else
					{
						parentConceptInvalidReason.setText("The specified identifer doesn't appear to be a valid concept");
						return false;
					}
					
				}
				else
				{
					selectedParentConcept = null;
					return true;
				}
			}
		};

		Utility.setupErrorImage(conceptDescriptionStack, conceptDescriptionValid.not(), conceptDescriptionInvalidReason, 5.0);
		Utility.setupErrorImage(conceptIdStack, conceptIdValid.not(), conceptIdInvalidReason, 5.0);
		Utility.setupErrorImage(parentConceptStack, parentConceptValid.not(), parentConceptInvalidReason, 5.0);

		conceptId.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				conceptIdValid.invalidate();
			}
		});

		conceptDescription.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				conceptDescriptionValid.invalidate();
			}
		});

		parentConcept.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				parentConceptValid.invalidate();
			}
		});
		
		LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(parentConcept);

		cancelButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				((Stage) rootPane.getScene().getWindow()).close();
			}
		});

		okButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				try
				{
					PendingConcepts.getInstance().addConcept(Long.parseLong(conceptId.getText()), conceptDescription.getText(), 
							(null == selectedParentConcept ? null : selectedParentConcept.getSctid()));
					((Stage) rootPane.getScene().getWindow()).close();
				}
				catch (Exception e)
				{
					logger.error("Unexpected error", e);
					LegoGUI.getInstance().showErrorDialog("Unexpected Error", "Error storing Pending Concept", e.toString());
				}
				
			}
		});
		
		conceptId.setText(PendingConcepts.getInstance().getUnusedId() + "");

		okButton.disableProperty().bind(conceptIdValid.not().or(conceptDescriptionValid.not()).or(parentConceptValid.not()));
	}
}
