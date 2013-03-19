package gov.va.legoEdit.drools.facts;

/**
 *
 * @author jefron
 */

import gov.va.legoEdit.gui.legoTreeView.ConceptUsageType;

public class AssertionFact extends Fact<ConceptUsageType>{
    private ConceptUsageType type;
    
    public AssertionFact(ConceptUsageType type) {
        super();
        this.type = type;
    }
    
    public ConceptUsageType getType() {
        return type;
    }
    
    @Override 
    public String toString() {
        if (type == ConceptUsageType.DISCERNIBLE) {
            return "ConceptUsageType : Discernible";
        } else {
            return "ConceptUsageType : NOT Discernible";
        }
    }
}

