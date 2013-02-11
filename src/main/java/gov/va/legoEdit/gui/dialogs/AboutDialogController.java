package gov.va.legoEdit.gui.dialogs;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AboutDialogController implements Initializable
{
    private static Logger logger = LoggerFactory.getLogger(AboutDialogController.class);
    
    @FXML // fx:id="rootPane"
    private AnchorPane rootPane; // Value injected by FXMLLoader
    @FXML // fx:id="detailedMessage"
    private TextArea message; // Value injected by FXMLLoader
    @FXML // fx:id="okButton"
    private Button okButton; // Value injected by FXMLLoader
    @FXML //  fx:id="title"
    private Label title; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources)
    {
        assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'AboutDialog.fxml'.";
        assert message != null : "fx:id=\"detailedMessage\" was not injected: check your FXML file 'AboutDialog.fxml'.";
        assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'AboutDialog.fxml'.";

        // initialize your logic here: all @FXML variables will have been injected

        okButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                ((Stage) rootPane.getScene().getWindow()).close();
            }
        });
        
        String version;
        try
        {
            InputStream is = AboutDialogController.class.getClassLoader().getResourceAsStream("version.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            version = br.readLine();
            br.close();
        }
        catch (Exception e)
        {
            version = "unknown";
        }
        
        String svnInfo = "";
        try
        {
            InputStream is = AboutDialogController.class.getClassLoader().getResourceAsStream("svnVersion.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            String prevLine = null;
            while (line != null)
            {
                if (line.startsWith("URL: "))
                {
                    svnInfo ="SVN Info: " +  line.substring("URL: ".length());
                }
                prevLine = line;
                line = br.readLine();
            }
            br.close();
           
            //last line has the true revision
            if (prevLine != null)
            {
                svnInfo += " " + prevLine;
            }
        }
        catch (Exception e)
        {
            logger.warn("Could not read the svn revision information", e);
        }
        
        title.setText("Lego Editor " + version);

        message.setText("Enhancment requests and defect reports can be filed at:"
                + System.getProperty("line.separator")
                + "https://csfe.aceworkspace.net/sf/planning/do/viewPlanningFolder/projects.veterans_administration_project/planning.lego_software_toolset"
                + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "New releases can be found at:"
                + System.getProperty("line.separator")
                + "https://csfe.aceworkspace.net/sf/frs/do/listReleases/projects.veterans_administration_project/frs.lego_editor "
                + System.getProperty("line.separator")
                + System.getProperty("line.separator") 
                + "Documentation can be found at:"
                + System.getProperty("line.separator")
                + "https://csfe.aceworkspace.net/sf/docman/do/listDocuments/projects.veterans_administration_project/docman.root.training.el_classifier_lego_software_tool"
                + System.getProperty("line.separator")
                + System.getProperty("line.separator") 
                + (svnInfo.length() > 0 ? svnInfo : ""));
    }
}
