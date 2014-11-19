package gov.va.legoEdit.drools.facts;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

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

