package gov.va.legoEdit.drools.facts;

/**
 *
 * @author jefron
 */

import gov.va.legoEdit.model.schemaModel.Concept;

public class PendingConceptFact extends Fact<Concept>{


	public PendingConceptFact(Context context, Concept component) {
		super(context, component);
	}

	public Concept getConcept() {
		return component;
	}
	
	public String getId()	{
		if (component.getUuid() != null){
			return component.getUuid();
		}
		else if (component.getSctid() != null){
			return component.getSctid().toString();
		}
		else{
			return "";
		}
	}
}

