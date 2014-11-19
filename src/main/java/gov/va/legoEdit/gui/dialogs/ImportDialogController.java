package gov.va.legoEdit.gui.dialogs;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.LegoGUIModel;
import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.model.LegoCreationFromScriptFile;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.model.schemaModel.Value;
import gov.va.legoEdit.model.userPrefs.UserPreferences;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.LegoStatus;
import gov.va.legoEdit.storage.wb.WBUtility;
import gov.va.legoEdit.util.TimeConvert;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * ImportDialogController
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class ImportDialogController implements Initializable
{
	Logger logger = LoggerFactory.getLogger(ImportDialogController.class);
	@FXML // fx:id="detailedMessage"
	private TextArea detailedMessage; // Value injected by FXMLLoader
	@FXML // fx:id="importName"
	private Label importName; // Value injected by FXMLLoader
	@FXML // fx:id="okButton"
	private Button okButton; // Value injected by FXMLLoader
	@FXML // fx:id="progress"
	private ProgressBar progress; // Value injected by FXMLLoader
	@FXML // fx:id="rootPane"
	private AnchorPane rootPane; // Value injected by FXMLLoader

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert detailedMessage != null : "fx:id=\"detailedMessage\" was not injected: check your FXML file 'ImportDialog.fxml'.";
		assert importName != null : "fx:id=\"importName\" was not injected: check your FXML file 'ImportDialog.fxml'.";
		assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'ImportDialog.fxml'.";
		assert progress != null : "fx:id=\"progress\" was not injected: check your FXML file 'ImportDialog.fxml'.";
		assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'ImportDialog.fxml'.";

		// initialize your logic here: all @FXML variables will have been injected
		okButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				((Stage) rootPane.getScene().getWindow()).close();
			}
		});
	}

	public void importFiles(List<File> files)
	{
		okButton.setDisable(true);
		progress.setProgress(0.0);
		importName.setText("Importing initializing");
		((Stage) rootPane.getScene().getWindow()).setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				((Stage) rootPane.getScene().getWindow()).show();
				event.consume();
			}
		});
		ImportRunnable r = new ImportRunnable(files);
		Thread t = new Thread(r, "Import Thread");
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();

	}

	private class ImportRunnable implements Runnable
	{
		private List<File> files;
		int count = 0;
		StringBuilder status = new StringBuilder();
		HashMap<String, Concept> missingConcepts = new HashMap<>();

		protected ImportRunnable(List<File> files)
		{
			this.files = files;
		}

		@Override
		public void run()
		{
			try
			{
				for (final File f : files)
				{
					final String temp = status.toString();
					status.setLength(0);
					Platform.runLater(new Runnable()
					{
						@Override
						public void run()
						{
							((Stage) rootPane.getScene().getWindow()).show();
							importName.setText("Importing " + f.getName() + "...");
							progress.setProgress((double) count / (double) files.size());
							if (temp.length() > 0)
							{
								detailedMessage.appendText(temp);
							}
						}
					});
	
					if (f.exists() && f.isFile())
					{
						try
						{
							String fileName = f.getName().toLowerCase();
							if (fileName.endsWith(".xml"))
							{
								try
								{
									LegoXMLUtils.validate((f));
								}
								catch (Exception e)
								{
									status.append("Warning - The file '" + f.getName() + "' is not schema valid.  Will attempt to import, but may fail.");
									status.append("  The schema error was: " + e.getMessage());
									status.append(System.getProperty("line.separator"));
									status.append(System.getProperty("line.separator"));
								}
								LegoList ll = LegoXMLUtils.readLegoList(f);
								List<Concept> failures = WBUtility.lookupAllConcepts(ll);
								BDBDataStoreImpl.getInstance().importLegoList(ll);
								for (Concept c : failures)
								{
									if (c.getSctid() != null)
									{
										missingConcepts.put(c.getSctid() + "", c);
									}
									else if (c.getUuid() != null && c.getUuid().length() > 0)
									{
										missingConcepts.put(c.getUuid(), c);
									}
									else
									{
										missingConcepts.put(c.getDesc(), c);
									}
								}
							}
							else if (fileName.endsWith(".csv") || fileName.endsWith(".tsv"))
							{
								//This one is different - many Lego Lists and Legos within a single file.
								HashMap<String, ArrayList<LegoCreationFromScriptFile>> legosToCreate =  LegoCreationFromScriptFile.readFile(f, 
										(fileName.endsWith(".csv") ? "," : "\t"));
								
								int t = 0;
								for (ArrayList<LegoCreationFromScriptFile> x : legosToCreate.values())
								{
									t = t + x.size();
								}
								final int total = t;
								
								for (Entry<String, ArrayList<LegoCreationFromScriptFile>> item : legosToCreate.entrySet())
								{
									String legoListName = item.getKey();
									
									LegoList ll = BDBDataStoreImpl.getInstance().getLegoListByName(legoListName);
									if (ll == null)
									{
										ll = BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), legoListName, 
												item.getValue().get(0).getLegoListDescription(), "");
									}
									
									for (LegoCreationFromScriptFile legoInfo : item.getValue())
									{
										Platform.runLater(new Runnable()
										{
											@Override
											public void run()
											{
												((Stage) rootPane.getScene().getWindow()).show();
												progress.setProgress((double) count / (double) total);
											}
										});
										
										Lego l= new Lego();
										Pncs pncs = new Pncs();
										pncs.setName(legoInfo.getPncsName());
										pncs.setValue(legoInfo.getPncsValue());
										pncs.setId(legoInfo.getPncsId());
										l.setPncs(pncs);

										Stamp s = new Stamp();
										UserPreferences up = LegoGUIModel.getInstance().getUserPreferences();
										s.setAuthor(up.getAuthor());
										s.setModule(up.getModule());
										s.setPath(up.getPath());
										s.setStatus(LegoStatus.Active.name());
										s.setTime(TimeConvert.convert(System.currentTimeMillis()));
										s.setUuid(UUID.randomUUID().toString());
										l.setStamp(s);

										l.setLegoUUID(UUID.randomUUID().toString());

										Assertion a = new Assertion();
										a.setAssertionUUID(UUID.randomUUID().toString());
										
										a.setDiscernible(new Discernible());
										Qualifier q = new Qualifier();
										a.setQualifier(q);
										Value value = new Value();
										a.setValue(value);
										
										if (legoInfo.getQualifierConcept() != null)
										{
											Expression expression = new Expression();
											Concept concept = new Concept();
											concept.setSctid(legoInfo.getQualifierConcept());
											expression.setConcept(concept);
											q.setExpression(expression);
										}
										if (StringUtils.isNotBlank(legoInfo.getValueItem()))
										{
											try
											{
												Long valueConceptId = Long.parseLong(legoInfo.getValueItem());
												Expression expression = new Expression();
												Concept valueConcept = new Concept();
												valueConcept.setSctid(valueConceptId);
												expression.setConcept(valueConcept);
												value.setExpression(expression);
											}
											catch (NumberFormatException e)
											{
												value.setText(legoInfo.getValueItem());
											}
										}
										l.getAssertion().add(a);
										BDBDataStoreImpl.getInstance().commitLego(l, ll.getLegoListUUID());
										count++;
									}
									List<Concept> failures = WBUtility.lookupAllConcepts(BDBDataStoreImpl.getInstance().getLegoListByID(ll.getLegoListUUID()));
									for (Concept c : failures)
									{
										if (c.getSctid() != null)
										{
											missingConcepts.put(c.getSctid() + "", c);
										}
										else if (c.getUuid() != null && c.getUuid().length() > 0)
										{
											missingConcepts.put(c.getUuid(), c);
										}
										else
										{
											missingConcepts.put(c.getDesc(), c);
										}
									}
								}
							}
							else
							{
								status.append("Warning - The file '" + f.getName() + "' is does not have a supported file extension.  Must be .xml, .csv or .tsv.  "
										+ "File has been skipped.");
								status.append(System.getProperty("line.separator"));
								status.append(System.getProperty("line.separator"));
							}
							status.append("Completed " + f.getName());
						}
						catch (Exception ex)
						{
							logger.info("Error loading file " + f.getName(), ex);
							status.append("Error loading file " + f.getName() + ": ");
							status.append((ex.getLocalizedMessage() == null ? ex.toString() : ex.getLocalizedMessage()));
						}
					}
					else
					{
						status.append("Skipped " + f.getName());
					}
	
					status.append(System.getProperty("line.separator"));
					status.append(System.getProperty("line.separator"));
					count++;
				}
				
				LegoGUI.getInstance().getLegoGUIController().getCommonlyUsedConcept().rebuildDBStats();
				
				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						detailedMessage.appendText(status.toString());
						importName.setText("Updating Editor");
						progress.setProgress(99.0);
						LegoGUIModel.getInstance().updateLegoLists();
						progress.setProgress(100.0);
						importName.setText("Import Complete");
	
						if (missingConcepts.size() > 0)
						{
							detailedMessage.appendText("Some concepts specified in the imported Legos do not exist in the SCT DB or the pending concepts file:");
							detailedMessage.appendText(System.getProperty("line.separator"));
							for (Concept c : missingConcepts.values())
							{
								detailedMessage.appendText(c.getSctid() + "\t" + c.getDesc() + (c.getUuid() != null ? "\t" + c.getUuid() : ""));
								detailedMessage.appendText(System.getProperty("line.separator"));
							}
						}
					}
				});
			}
			finally
			{
				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						okButton.setDisable(false);
						okButton.requestFocus();
					}
				});
			}
		}
	}
}
