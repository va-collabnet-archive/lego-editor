package gov.va.legoEdit.drools.manager;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.kie.api.runtime.rule.ConsequenceExceptionHandler;
import org.kie.api.runtime.rule.Match;
import org.kie.api.runtime.rule.RuleRuntime;

/**
 *
 * @author kec
 */
public class DroolsExceptionHandler 
    implements ConsequenceExceptionHandler, Externalizable {


    @Override
    public void writeExternal(ObjectOutput oo) 
            throws IOException {
        // nothing to do
    }

    @Override
    public void readExternal(ObjectInput oi) 
            throws IOException, ClassNotFoundException {
        // nothing to do. 
    }

	/**
	 * @see org.kie.api.runtime.rule.ConsequenceExceptionHandler#handleException(org.kie.api.runtime.rule.Match, org.kie.api.runtime.rule.RuleRuntime, java.lang.Exception)
	 */
	@Override
	public void handleException(Match match, RuleRuntime workingMemory, Exception exception)
	{
		throw new DroolsException(match, workingMemory, exception);
		
	}

}
