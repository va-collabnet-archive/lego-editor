/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.legoEdit.drools;

import java.util.UUID;
import org.ihtsdo.tk.binding.Snomed;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 *
 * @author jefron
 */
public class LegoSpec  extends Snomed {
     public static ConceptSpec PROCESS_TYPE =
            new ConceptSpec("Process (observable entity)",
            UUID.fromString("bcd964a8-77a0-393d-87c0-13ba16c9a7bc"));

     public static ConceptSpec ACTION_TYPE =
            new ConceptSpec("Action (qualifier value)",
            UUID.fromString("f611bc15-3455-30a9-9399-d3d8471656c1"));
}
