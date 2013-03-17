/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.legoEdit.drools.manager;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.drools.runtime.rule.Activation;
import org.drools.runtime.rule.ConsequenceExceptionHandler;
import org.drools.runtime.rule.WorkingMemory;

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

    @Override
    public void handleException(Activation actvtn, 
                            WorkingMemory wm, Exception ex) {
        throw new DroolsException( ex, wm, actvtn);
    }

}
