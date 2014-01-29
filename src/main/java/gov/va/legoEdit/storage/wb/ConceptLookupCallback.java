package gov.va.legoEdit.storage.wb;

import gov.va.legoEdit.model.schemaModel.Concept;

/**
 * 
 * ConceptLookupCallback
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public interface ConceptLookupCallback
{
	public void lookupComplete(Concept concept, long submitTime, Integer callId);
}
