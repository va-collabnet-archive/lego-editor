/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.legoEdit.drools.actions;

import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;

/**
 *
 * @author jefron
 */
public class FailValidatorAction {
    public enum ReasonFailed  { DISCERNIBLE_OBSERVABLE};

    public FailValidatorAction(LegoTreeItem legoTreeItem, ReasonFailed reason) {
        if (reason == ReasonFailed.DISCERNIBLE_OBSERVABLE) {
            legoTreeItem.invalideDroolsRule("Discernible Concept must be an Observable");
        }
    }
}
