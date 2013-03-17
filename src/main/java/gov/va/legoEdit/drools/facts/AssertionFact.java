/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.legoEdit.drools.facts;

/**
 *
 * @author jefron
 */

import gov.va.legoEdit.gui.legoTreeView.ConceptUsageType;

public class AssertionFact <T extends ConceptUsageType> extends Fact<T>{
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

