package gov.va.legoEdit.drools.facts;

/**
 *
 * @author jefron
 */

import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public class ConceptFact extends ComponentFact<ConceptVersionBI> {

	public ConceptFact(Context context, ConceptVersionBI component, ViewCoordinate vc) {
		super(context, component, vc);
	}

	public ConceptVersionBI getConcept() {
		return component;
	}
	
}

