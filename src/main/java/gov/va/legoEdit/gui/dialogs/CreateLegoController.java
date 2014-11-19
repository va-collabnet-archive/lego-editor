package gov.va.legoEdit.gui.dialogs;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.LegoGUIModel;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeNodeType;
import gov.va.legoEdit.gui.util.CustomClipboard;
import gov.va.legoEdit.gui.util.ExpandedNode;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.gui.util.Utility;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.model.userPrefs.UserPreferences;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.LegoStatus;
import gov.va.legoEdit.util.TimeConvert;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * CreateLegoController
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class CreateLegoController implements Initializable
{
	Logger logger = LoggerFactory.getLogger(CreateLegoController.class);
	
	@FXML // fx:id="cancelButton"
	private Button cancelButton; // Value injected by FXMLLoader
	@FXML // fx:id="legoListName"
	private Label legoListName; // Value injected by FXMLLoader
	@FXML // fx:id="okButton"
	private Button okButton; // Value injected by FXMLLoader
	@FXML // fx:id="pncsID"
	private TextField pncsID; // Value injected by FXMLLoader
	@FXML // fx:id="pncsName"
	private TextField pncsName; // Value injected by FXMLLoader
	@FXML // fx:id="pncsValue"
	private TextField pncsValue; // Value injected by FXMLLoader
	@FXML // fx:id="rootPane"
	private AnchorPane rootPane; // Value injected by FXMLLoader
	@FXML //  fx:id="pncsIDStack"
	private StackPane pncsIDStack; // Value injected by FXMLLoader
	@FXML //  fx:id="pncsNameStack"
	private StackPane pncsNameStack; // Value injected by FXMLLoader
	@FXML //  fx:id="pncsValueStack"
	private StackPane pncsValueStack; // Value injected by FXMLLoader
	@FXML //  fx:id="okStack"
	private StackPane okStack; // Value injected by FXMLLoader
	
	private Tooltip pncsNameInvalidReason, pncsIDInvalidReason, pncsValueInvalidReason, okInvalidReason;
	private BooleanProperty formValid, pncsNameValid, pncsIDValid, pncsValueValid;
	private LegoListByReference llbr;
	private LegoTreeItem legoTreeItem;
	private Lego legoFromPaste;
	private ImageView okInvalidImage;

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'CreateLego.fxml'.";
		assert legoListName != null : "fx:id=\"legoListName\" was not injected: check your FXML file 'CreateLego.fxml'.";
		assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'CreateLego.fxml'.";
		assert pncsID != null : "fx:id=\"pncsID\" was not injected: check your FXML file 'CreateLego.fxml'.";
		assert pncsIDStack != null : "fx:id=\"pncsIDStack\" was not injected: check your FXML file 'CreateLego.fxml'.";
		assert pncsName != null : "fx:id=\"pncsName\" was not injected: check your FXML file 'CreateLego.fxml'.";
		assert pncsNameStack != null : "fx:id=\"pncsNameStack\" was not injected: check your FXML file 'CreateLego.fxml'.";
		assert pncsValue != null : "fx:id=\"pncsValue\" was not injected: check your FXML file 'CreateLego.fxml'.";
		assert pncsValueStack != null : "fx:id=\"pncsValueStack\" was not injected: check your FXML file 'CreateLego.fxml'.";
		assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'CreateLego.fxml'.";

		pncsName.setPromptText("The PNCS Name");
		ImageView pncsNameInvalidImage = Images.EXCLAMATION.createImageView();
		pncsNameInvalidReason = new Tooltip("The PNCS Name is required");
		Tooltip.install(pncsNameInvalidImage, pncsNameInvalidReason);
		pncsNameStack.getChildren().add(pncsNameInvalidImage);
		StackPane.setAlignment(pncsNameInvalidImage, Pos.CENTER_RIGHT);
		StackPane.setMargin(pncsNameInvalidImage, new Insets(0.0, 5.0, 0.0, 0.0));

		pncsNameValid = new SimpleBooleanProperty(false);
		pncsNameInvalidImage.visibleProperty().bind(pncsNameValid.not());
		pncsName.textProperty().addListener(new ChangeListener<String>()
		{
			
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				if (newValue.length() > 0)
				{
					pncsNameValid.set(true);
				}
				else
				{
					pncsNameValid.set(false);
					pncsNameInvalidReason.setText("The PNCS Name is required");
				}
				checkForm();
			}
		});
		
		
		pncsValue.setPromptText("The PNCS Value");
		ImageView pncsValueInvalidImage = Images.EXCLAMATION.createImageView();
		pncsValueInvalidReason = new Tooltip("The PNCS Value is required");
		Tooltip.install(pncsValueInvalidImage, pncsValueInvalidReason);
		
		pncsValueStack.getChildren().add(pncsValueInvalidImage);
		StackPane.setAlignment(pncsValueInvalidImage, Pos.CENTER_RIGHT);
		StackPane.setMargin(pncsValueInvalidImage, new Insets(0.0, 5.0, 0.0, 0.0));

		pncsValueValid = new SimpleBooleanProperty(false);
		pncsValueInvalidImage.visibleProperty().bind(pncsValueValid.not());
		pncsValue.textProperty().addListener(new ChangeListener<String>()
		{
			
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				if (newValue.length() > 0)
				{
					pncsValueValid.set(true);
				}
				else
				{
					pncsValueValid.set(false);
					pncsValueInvalidReason.setText("The PNCS Value is required");
				}
				checkForm();
			}
		});
		
		
		
		pncsID.setPromptText("The PNCS ID (must be an integer)");
		ImageView pncsIDInvalidImage = Images.EXCLAMATION.createImageView();
		pncsIDInvalidReason = new Tooltip("The PNCS ID is required");
		Tooltip.install(pncsIDInvalidImage, pncsIDInvalidReason);

		pncsIDStack.getChildren().add(pncsIDInvalidImage);
		StackPane.setAlignment(pncsIDInvalidImage, Pos.CENTER_RIGHT);
		StackPane.setMargin(pncsIDInvalidImage, new Insets(0.0, 5.0, 0.0, 0.0));

		pncsIDValid = new SimpleBooleanProperty(false);
		pncsIDInvalidImage.visibleProperty().bind(pncsIDValid.not());
		pncsID.textProperty().addListener(new ChangeListener<String>()
		{
			
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				if (newValue.length() > 0)
				{
					try
					{
						Integer.parseInt(newValue);
						pncsIDValid.set(true);
					}
					catch (NumberFormatException e)
					{
						pncsIDValid.set(false);
						pncsIDInvalidReason.setText("The PNCS ID must be an integer number");
					}
				}
				else
				{
					pncsIDValid.set(false);
					pncsIDInvalidReason.setText("The PNCS ID is required");
				}
				checkForm();
			}
		});
		
		
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
				Lego l;
				if (legoFromPaste == null)
				{
					l = new Lego();
				}
				else
				{
					l = legoFromPaste;
				}
				Pncs pncs = new Pncs();
				pncs.setName(pncsName.getText());
				pncs.setValue(pncsValue.getText());
				pncs.setId(Integer.parseInt(pncsID.getText()));
				l.setPncs(pncs);

				Stamp s;
				if (legoFromPaste == null)
				{
					s = new Stamp();
				}
				else
				{
					s = legoFromPaste.getStamp();
				}
				UserPreferences up = LegoGUIModel.getInstance().getUserPreferences();
				s.setAuthor(up.getAuthor());
				if (legoFromPaste == null)
				{
					s.setModule(up.getModule());
					s.setPath(up.getPath());
					s.setStatus(LegoStatus.Active.name());
				}
				s.setTime(TimeConvert.convert(System.currentTimeMillis()));
				s.setUuid(UUID.randomUUID().toString());
				l.setStamp(s);

				l.setLegoUUID(UUID.randomUUID().toString());

				if (legoFromPaste == null)
				{
					Assertion a = new Assertion();
					a.setAssertionUUID(UUID.randomUUID().toString());
					l.getAssertion().add(a);
				}
				else
				{
					// Change all the assertion UUIDs
					for (Assertion a : l.getAssertion())
					{
						a.setAssertionUUID(UUID.randomUUID().toString());
					}
				}

				LegoReference lr = new LegoReference(l);
				lr.setIsNew(true);
				llbr.getLegoReference().add(lr);
				LegoGUI.getInstance().getLegoGUIController().addNewLego(llbr.getLegoListUUID(), l);
				
				ExpandedNode before = Utility.buildExpandedNodeHierarchy(legoTreeItem);
				
				legoTreeItem.getChildren().clear();
				legoTreeItem.buildPNCSChildren();
				
				Utility.setExpandedStates(before, legoTreeItem);
				
				boolean found = false;
				for (TreeItem<String> nameItem : legoTreeItem.getChildren())
				{
					if (found)
					{
						break;
					}
					if (nameItem.getValue().equals(pncsName.getText()))
					{
						for (TreeItem<String> valueItem : nameItem.getChildren())
						{
							if (valueItem.getValue().equals(pncsValue.getText()))
							{
								Utility.expandParents(valueItem);	
								valueItem.setExpanded(true);
								found = true;
								break;
							}
						}
					}
				}
				
				if (!found)
				{
					logger.error("Couldn't find new item in tree?");
				}

				llbr = null;
				legoTreeItem = null;
				((Stage) rootPane.getScene().getWindow()).close();
			}
		});
		
		okInvalidImage = Images.EXCLAMATION.createImageView();
		okInvalidReason = new Tooltip("");
		Tooltip.install(okInvalidImage, okInvalidReason);

		okStack.getChildren().add(okInvalidImage);
		StackPane.setAlignment(okInvalidImage, Pos.CENTER_RIGHT);
		StackPane.setMargin(okInvalidImage, new Insets(0.0, 32.0, 0.0, 0.0));
		okInvalidImage.setVisible(false);
		
		formValid = new SimpleBooleanProperty(false);
		okButton.disableProperty().bind(formValid.not());
	}
	
	private void checkForm()
	{
		if (pncsIDValid.get() && pncsValueValid.get() && pncsNameValid.get())
		{
			Pncs pncs = BDBDataStoreImpl.getInstance().getPncs(Integer.parseInt(pncsID.getText()), pncsValue.getText());
			if (pncs != null && !pncs.getName().equals(pncsName.getText()))
			{
				formValid.set(false);
				okInvalidImage.setVisible(true);
				okInvalidReason.setText("The PNCS ID '" + pncs.getId() + "' and Value '" + pncs.getValue() 
						+ "' are already associated with the name '" + pncs.getName() + "'." + System.getProperty("line.separator") 
						+ "They cannot be associated with the name '" + pncsName.getText() + "'" + System.getProperty("line.separator")
						+ System.getProperty("line.separator") + "Either fix the name, or change the ID to a different (unused) value");
			}
			else
			{
				okInvalidImage.setVisible(false);
				formValid.set(true);
			}
		}
		else
		{
			okInvalidImage.setVisible(false);  //Don't need this, one of the others is already showing why it is invalid
			formValid.set(false);
		}
	}

	public void init(LegoListByReference llbr, LegoTreeItem lti, boolean fromPaste)
	{
		this.llbr = llbr;
		this.legoTreeItem = lti;
		legoListName.setText(llbr.getGroupDescription());
		if (fromPaste)
		{
			legoFromPaste = CustomClipboard.getLego();
			if (legoFromPaste == null)
			{
				LegoGUI.getInstance().showErrorDialog("Not a Lego", "The Clipboard does not contain a Lego", "A blank Lego will be created instead.");
			}
		}
		else
		{
			legoFromPaste = null;
		}
		
		String defaultName = "";
		Integer defaultId = null;
		if (lti.getNodeType() == LegoTreeNodeType.pncsName)
		{
			defaultName = lti.getValue();
			defaultId = (Integer)lti.getExtraData();
			this.legoTreeItem = (LegoTreeItem)lti.getParent();
		}
		
		pncsID.setText(legoFromPaste == null ? (defaultId != null ? defaultId.toString() : "") : legoFromPaste.getPncs().getId() + "");
		pncsName.setText(legoFromPaste == null ? defaultName : legoFromPaste.getPncs().getName());
		pncsValue.setText(legoFromPaste == null ? "" : legoFromPaste.getPncs().getValue());
	}
	
	public void init(LegoListByReference llbr, LegoTreeItem lti, String suggestedName, String suggestedValue)
	{
		this.llbr = llbr;
		this.legoTreeItem = lti;
		legoListName.setText(llbr.getGroupDescription());
		legoFromPaste = null;
		
		pncsID.setText("");
		pncsName.setText(suggestedName == null ? "" : suggestedName);
		pncsValue.setText(suggestedValue == null ? "" : suggestedValue);
	}
}
