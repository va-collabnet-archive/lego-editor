package gov.va.legoEdit.gui.dialogs;

import gov.va.legoEdit.LegoGUIModel;
import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.wb.WBUtility;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportDialogController implements Initializable
{
    Logger logger = LoggerFactory.getLogger(ImportDialogController.class);
    @FXML //  fx:id="detailedMessage"
    private TextArea detailedMessage; // Value injected by FXMLLoader
    @FXML //  fx:id="importName"
    private Label importName; // Value injected by FXMLLoader
    @FXML //  fx:id="okButton"
    private Button okButton; // Value injected by FXMLLoader
    @FXML //  fx:id="progress"
    private ProgressBar progress; // Value injected by FXMLLoader
    @FXML //  fx:id="rootPane"
    private AnchorPane rootPane; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
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
                        progress.setProgress((double)count / (double)files.size());
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
                        LegoXMLUtils.validate((f));
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
                        
                        status.append("Loaded " + f.getName());
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
                
                try
                {
                    //TODO BUG this helps with http://javafx-jira.kenai.com/browse/RT-27827
                    //remove when we find a solution
                    Thread.sleep(250);
                }
                catch (InterruptedException e)
                {
                    // noop
                }
                
                status.append(System.getProperty("line.separator"));
                status.append(System.getProperty("line.separator"));
                count++;
            }
            
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
                    
                    okButton.setDisable(false);
                    okButton.requestFocus();
                }
            });
        }
    }
}
