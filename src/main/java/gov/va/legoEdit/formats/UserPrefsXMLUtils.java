package gov.va.legoEdit.formats;

import gov.va.legoEdit.model.userPrefs.UserPreferences;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author darmbrust
 */
public class UserPrefsXMLUtils
{
    protected static File userPrefsPath = new File("UserPreferences.xml");
    
    static Logger logger = LoggerFactory.getLogger(UserPrefsXMLUtils.class);
    static JAXBContext jc;

    static
    {
        try
        {
            jc = JAXBContext.newInstance(UserPreferences.class);
        }
        catch (JAXBException e)
        {
            throw new RuntimeException("Build Error", e);
        }
    }

    public static UserPreferences readUserPreferences() throws JAXBException, FileNotFoundException
    {
        Unmarshaller um = jc.createUnmarshaller();
        return (UserPreferences) um.unmarshal(new FileReader(userPrefsPath));
    }

    public static void writeUserPreferences(UserPreferences userPreferences) throws PropertyException, JAXBException
    {
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(userPreferences, userPrefsPath);
    }
}
