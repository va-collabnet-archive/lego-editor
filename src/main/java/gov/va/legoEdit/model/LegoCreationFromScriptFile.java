package gov.va.legoEdit.model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	
	public static HashMap<String, ArrayList<LegoCreationFromScriptFile>> readFile(File f, String sepChar) throws IOException
	{
		HashMap<String, ArrayList<LegoCreationFromScriptFile>> results = new HashMap<>();
		
		List<String> lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
		int lineNumber = 0;
		for (String line : lines)
		{
			lineNumber++;
			if (line.startsWith("#") || line.length() == 0)
			{
				continue;
			}
			else
			{
				try
				{
					String[] parts = line.split(sepChar);
					LegoCreationFromScriptFile item = new LegoCreationFromScriptFile();
					item.legoListName = parts[0].trim();
					item.legoListDescription = parts[1].trim();
					item.pncsName = parts[2].trim();
					item.pncsValue = parts[3].trim();
					item.pncsId = Integer.parseInt(parts[4].trim());
					if (parts.length > 5 && StringUtils.isNotBlank(parts[5]))
					{
						item.qualifierConcept = Long.parseLong(parts[5]);
					}
					if (parts.length > 6)
					{
						item.valueItem = parts[6];
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
						throw new IOException("Missing a required value on line " + lineNumber);
					}
					group.add(item);
				}
				catch (IOException e)
				{
					throw e;
				}
				catch (Exception e) 
				{
					throw new IOException("Invalid format on line " + lineNumber + " '" + line + "' - " + e);
				}
			}
		}
		return results;
	}
	
	
}
