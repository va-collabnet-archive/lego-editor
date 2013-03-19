package gov.va.legoEdit.drools.actions;

/**
 * Action
 * @author Dan Armbrust 
 * Copyright 2013
 * 
 */
public abstract class DroolsLegoAction
{
	public abstract String getFailureReason();
	
	@Override
	public String toString()
	{
		return getFailureReason();
	}
}
