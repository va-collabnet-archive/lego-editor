/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.legoEdit.drools;

import gov.va.legoEdit.drools.facts.AssertionFact;
import gov.va.legoEdit.drools.facts.ConceptFact;
import gov.va.legoEdit.drools.facts.Context;
import gov.va.legoEdit.drools.manager.DroolsExecutionManager;
import gov.va.legoEdit.gui.legoTreeView.ConceptUsageType;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import org.ihtsdo.bdb.temp.AceLog;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.StandardViewCoordinates;

/**
 *
 * @author jefron
 */
public class ConceptNodeDroolsHandler {

    private Set<File> kbFiles = new HashSet();
    Map<String, Object> globals = new HashMap();
    Collection<Object> facts = new ArrayList();

    public ConceptNodeDroolsHandler() {
        File f = new File("src/main/resources/drools-rules/ConceptNode.drl");
        this.kbFiles.add(f);
    }

    public void processConceptNodeRule(ConceptVersionBI concept, ConceptUsageType usageType, LegoTreeItem lti) {

        try {
            DroolsExecutionManager.setup(ConceptNodeDroolsHandler.class.getCanonicalName(), kbFiles);
            ArrayList<Action> actions = new ArrayList<Action>();
            globals.put("actions", actions);
            globals.put("item", lti);

            facts.add(new ConceptFact(Context.DROP_OBJECT, concept, StandardViewCoordinates.getSnomedLatest()));
            facts.add(new AssertionFact(usageType));

            DroolsExecutionManager.fireAllRules(ConceptNodeDroolsHandler.class.getCanonicalName(), kbFiles, globals, facts, false);
            
            
            facts.clear();
            globals.clear();
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } 

    }
}
