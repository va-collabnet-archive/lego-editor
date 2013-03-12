package gov.va.legoEdit.storage.wb;

import gov.va.legoEdit.model.schemaModel.Concept;

public interface ConceptLookupCallback
{
	public void lookupComplete(Concept concept, long submitTime, Integer callId);
}
