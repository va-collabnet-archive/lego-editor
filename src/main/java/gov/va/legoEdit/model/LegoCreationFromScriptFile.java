package gov.va.legoEdit.model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;

/**
 * LegoCreationFromTSV
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 * 
 */
public class LegoCreationFromScriptFile
{
	private String legoListName, legoListDescription, pncsName, pncsValue, valueItem;
	private int pncsId;
	private Long qualifierConcept = null;
	
	private LegoCreationFromScriptFile()
	{
		
	}
	
	public String getLegoListName()
	{
		return legoListName;
	}
	
	public String getLegoListDescription()
	{
		return legoListDescription;
	}
	public String getPncsName()
	{
		return pncsName;
	}
	public String getPncsValue()
	{
		return pncsValue;
	}
	public Long getQualifierConcept()
	{
		return qualifierConcept;
	}
	public String getValueItem()
	{
		return valueItem;
	}
	public int getPncsId()
	{
		return pncsId;
	}
	
	public static HashMap<String, ArrayList<LegoCreationFromScriptFile>> readFile(File f, char sepChar) throws IOException
	{
		HashMap<String, ArrayList<LegoCreationFromScriptFile>> results = new HashMap<>();
		
		CSVFormat format = CSVFormat.newFormat(sepChar).withAllowMissingColumnNames().withQuote('"').withRecordSeparator("\r\n")
				.withIgnoreEmptyLines(true).withCommentMarker('#').withEscape('\\').withIgnoreSurroundingSpaces();
		
		CSVParser parser = null;
		try
		{
			parser = new CSVParser(new FileReader(f), format);
			
			Iterator<CSVRecord> it = parser.iterator();
			while (it.hasNext())
			{
				try
				{
					CSVRecord parts = it.next();
					LegoCreationFromScriptFile item = new LegoCreationFromScriptFile();
					item.legoListName = parts.get(0);
					item.legoListDescription = parts.get(1);
					item.pncsName = parts.get(2);
					item.pncsValue = parts.get(3);
					item.pncsId = Integer.parseInt(parts.get(4));
					if (parts.size() > 5 && StringUtils.isNotBlank(parts.get(5)))
					{
						item.qualifierConcept = Long.parseLong(parts.get(5));
					}
					if (parts.size() > 6)
					{
						item.valueItem = parts.get(6);
					}
					
					ArrayList<LegoCreationFromScriptFile> group = results.get(item.legoListName);
					if (group == null)
					{
						group = new ArrayList<>();
						results.put(item.legoListName, group);
					}
					
					if (StringUtils.isBlank(item.legoListName) || StringUtils.isBlank(item.legoListDescription) || StringUtils.isBlank(item.pncsName) ||
							StringUtils.isBlank(item.pncsValue))
					{
						throw new IOException("Missing a required value on line " + parser.getCurrentLineNumber());
					}
					group.add(item);
				}
				catch (IOException e)
				{
					throw e;
				}
				catch (Exception e) 
				{
					throw new IOException("Invalid format on line " + parser.getCurrentLineNumber() + " - " + e);
				}
			}
		}
		finally
		{
			if (parser != null)
			{
				parser.close();
			}
		}
		return results;
	}
	
	
}
