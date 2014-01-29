package gov.va.legoEdit.gui.cem;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * ValueSet
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class ValueSet
{
	private String name;
	private String id;
	private List<String> values;
	
	public ValueSet(String name, String id, String value)
	{
		this.name = name;
		this.id = id;
		values = new ArrayList<String>();
		values.add(value);
	}
	
	public void addValue(String value)
	{
		this.values.add(value);
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getId()
	{
		return id;
	}
	
	public List<String> getValues()
	{
		return values;
	}
	
}
