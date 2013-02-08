package gov.va.legoEdit.formats;

import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.LegoList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author darmbrust
 */
public class LegoXMLUtils
{
    static Logger logger = LoggerFactory.getLogger(LegoXMLUtils.class);
    static Schema schema;
    static JAXBContext jc;
    static Transformer xmlToHTMLTransformer;

    static
    {
        try
        {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = schemaFactory.newSchema(LegoXMLUtils.class.getResource("/LEGO.xsd"));
            jc = JAXBContext.newInstance(LegoList.class);
            TransformerFactory tf = TransformerFactory.newInstance();
            xmlToHTMLTransformer = tf.newTransformer(new StreamSource(LegoXMLUtils.class.getResourceAsStream("/xmlToHTML.xslt")));
        }
        catch (SAXException | JAXBException | TransformerConfigurationException e)
        {
            throw new RuntimeException("Build Error", e);
        }
    }

    public static void validate(File path) throws IOException, SAXException
    {
        logger.debug("Validating the XML file {}", path);
        try
        {
            Source xmlFile = new StreamSource(path);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
            logger.debug("The XML file {} is valid", path);
        }
        catch (SAXException | IOException e)
        {
            logger.debug("The XML file {} is invalid: {}", path, e.getLocalizedMessage());
            throw e;
        }
    }

    public static LegoList readLegoList(File path) throws JAXBException, FileNotFoundException
    {
        Unmarshaller um = jc.createUnmarshaller();
        return (LegoList) um.unmarshal(new FileReader(path));
    }

    public static String toXML(LegoList ll) throws PropertyException, JAXBException
    {
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "LEGO.xsd");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        m.marshal(ll, baos);
        return new String(baos.toByteArray());
    }
    
    public static String toXML(Assertion a) throws PropertyException, JAXBException
    {
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.setProperty(Marshaller.JAXB_FRAGMENT, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        m.marshal(a, baos);
        return new String(baos.toByteArray());
    }
    
    public static Assertion readAssertion(String xmlAssertion) throws JAXBException
    {
        Unmarshaller um = jc.createUnmarshaller();
        return (Assertion) um.unmarshal(new ByteArrayInputStream(xmlAssertion.getBytes()));
    }
    
    public static String toHTML(LegoList ll) throws PropertyException, JAXBException, TransformerConfigurationException, TransformerException
    {
        String asXML = toXML(ll);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
        xmlToHTMLTransformer.transform(new StreamSource(new ByteArrayInputStream(asXML.getBytes())), result);
        return baos.toString();
    }
}
