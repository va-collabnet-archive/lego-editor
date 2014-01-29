package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.util.Utility;

/**
 * 
 * ComboBoxConcept
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class ComboBoxConcept
{
	private String description_, id_;
	private boolean ignoreChange = false;

	public ComboBoxConcept(String description, String id, boolean ignoreChange)
	{
		this(description, id);
		this.ignoreChange = ignoreChange;
	}

	public ComboBoxConcept(String description, String id)
	{
		description_ = description;
		id_ = id;
	}

	public String getDescription()
	{
		return description_;
	}

	public String getId()
	{
		return id_;
	}
	
	/**
	 * Note - this can only be read once - if it returns true after the first call, 
	 * it resets itself to false for every subsequent call.
	 */
	public boolean shouldIgnoreChange()
	{
		boolean temp = ignoreChange;
		ignoreChange = false;
		return temp;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (obj instanceof ComboBoxConcept)
		{
			ComboBoxConcept other = (ComboBoxConcept) obj;
			return Utility.isEqual(id_, other.id_) && Utility.isEqual(description_, other.description_);
		}
		return false;
	}

	@Override
	public String toString()
	{
		return id_ + ":" + description_;
	}
}