package gov.va.legoEdit.gui.cem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * CEMXmlReader
 * 
 * @author Dan Armbrust
 *         Copyright 2013
 * 
 */
public class CIMIXmlReader
{
	public static CIMIXML buildNode(File file) throws XMLStreamException, FileNotFoundException
	{
		CIMIXML result = new CIMIXML();
		VBox dataHolder = new VBox();
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		InputStream in = new FileInputStream(file);
		XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

		int indent = 0;
		while (eventReader.hasNext())
		{
			XMLEvent event = eventReader.nextEvent();
			if (event.isStartElement())
			{
				indent++;
				StartElement se = event.asStartElement();
				
				String line = "";
				boolean highlight = false;
				if (se.getName().getLocalPart().equals("Domain") && "code".equals(se.getAttributeByName(new QName("constraintPath")).getValue()))
				{
					result.valuesetIDs.add(se.getAttributeByName(new QName("ecid")).getValue());
					highlight = true;
				}
				
				line += se.getName().getLocalPart();
				@SuppressWarnings("unchecked")
				Iterator<Attribute> i = se.getAttributes();
				while (i.hasNext())
				{
					Attribute a = i.next();
					line += " " + a.getName() + ":" + a.getValue();
				}
				
				dataHolder.getChildren().add(makeNode(line, 5*indent, highlight));
			}
			if (event.isCharacters())
			{
				Characters c = event.asCharacters();
				String value = c.getData().trim();
				if (value.length() > 0)
				{
					dataHolder.getChildren().add(makeNode(value, indent * 5 + 5, false));
				}
			}
			if (event.isEndElement())
			{
				indent--;
			}
		}
		result.node = dataHolder;
		return result;
	}
	
	private static Node makeNode(String value, double indent, boolean highlight)
	{
		Label l = new Label(value);
		l.setWrapText(true);
		if (highlight)
		{
			l.setStyle("-fx-background-color: yellow");
		}
		HBox hbox = new HBox();
		hbox.getChildren().add(l);
		VBox.setMargin(hbox, new Insets(0,0,0, indent));
		return hbox;
	}
	
	public static class CIMIXML
	{
		Node node;
		List<String> valuesetIDs;
	
		public CIMIXML()
		{
			valuesetIDs = new ArrayList<>();
		}
	}
}
