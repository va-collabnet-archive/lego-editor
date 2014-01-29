package gov.va.legoEdit.gui.cem;

import gov.va.legoEdit.util.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * 
 * CEMValueSetReader
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class CEMValueSetReader
{
	public static HashMap<String, ValueSet> read(File file) throws FileNotFoundException, IOException
	{
		HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(file));

		HashMap<String, ValueSet> results = new HashMap<>();
		
		HSSFSheet sheet = wb.getSheetAt(0);

		int idCol = -1;
		int nameCol = -1;
		int valueCol = -1;
		
		HSSFRow row = sheet.getRow(0);
		
		for (int i = 0; i < row.getLastCellNum(); i++)
		{
			if (row.getCell(i) != null)
			{
				String value = row.getCell(i).getStringCellValue();
				if (value.toLowerCase().equals("value_set_display"))
				{
					nameCol = i;
				}
				else if (value.toLowerCase().equals("member"))
				{
					valueCol = i;
				}
				else if (value.toLowerCase().equals("value_set_ecid"))
				{
					idCol = i;
				}
				if (idCol >= 0 && valueCol >= 0 && nameCol >= 0)
				{
					break;
				}
			}
		}
		
		if (idCol < 0 || valueCol < 0 || nameCol < 0)
		{
			throw new IOException("Didn't find expected data in the value set spreadsheet");
		}
		
		
		Iterator<Row> rowIter = sheet.rowIterator();
		while (rowIter.hasNext())
		{
			//skip first row
			
			Row r = rowIter.next();
			
			String id = null, name = null, value = null;
			
			Cell cell = r.getCell(idCol);
			if (cell != null)
			{
				id = cell.getStringCellValue();
			}
			
			cell = r.getCell(nameCol);
			if (cell != null)
			{
				name = cell.getStringCellValue();
			}
			
			cell = r.getCell(valueCol);
			if (cell != null)
			{
				value = cell.getStringCellValue();
			}
			
			if (Utility.isEmpty(name) || Utility.isEmpty(value) || Utility.isEmpty(id))
			{
				continue;
			}
			
			ValueSet vs = results.get(id.toLowerCase());
			if (vs == null)
			{
				vs = new ValueSet(name, id, value);
				results.put(id.toLowerCase(), vs);
			}
			else
			{
				if (!name.equals(vs.getName()))
				{
					throw new IOException("Unexpected data");
				}
				vs.addValue(value);
			}
		}
		return results;
	}
}
