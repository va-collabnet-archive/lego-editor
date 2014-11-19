package gov.va.legoEdit.drools.facts;

import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;

/**
 *
 * @author jefron
 */
public class DescFact extends ComponentFact<DescriptionVersionBI<?>> {

    public DescFact(Context context, DescriptionVersionBI<?> component, ViewCoordinate vc) {
        super(context, component, vc);
    }

    public DescriptionVersionBI<?> getDesc() {
        return component;
    }
}
