package gov.va.legoEdit.formats;

/**
 * 
 * LegoValidateCallback
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public interface LegoValidateCallback
{
	public void validateComplete(boolean valid, String errorMessage);
}
