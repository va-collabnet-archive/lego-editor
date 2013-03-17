/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.legoEdit.drools.facts;

/**
 *
 * @author jefron
 */
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;

public class LegoTreeItemFact<T extends LegoTreeItem> extends Fact<T> {

    private final LegoTreeItem item;

    public LegoTreeItemFact(LegoTreeItem lti) {
        item = lti;
    }

    public LegoTreeItem getLegoTreeItem() {
        return item;
    }
}
