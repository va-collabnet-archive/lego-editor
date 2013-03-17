/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.legoEdit.drools.facts;

import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 *
 * @author jefron
 */
public class ComponentFact <T extends ComponentVersionBI> extends Fact<T>{
    

	private ViewCoordinate vc;
	
	protected ComponentFact(Context context, T component, ViewCoordinate vc) {
		super(context, component);
		this.vc = vc;
	}
	
	public ViewCoordinate getVc() {
		return vc;
	}

	@Override
	public String toString() {
		return "Fact context: " + context + " component: " + component;
	}

}

