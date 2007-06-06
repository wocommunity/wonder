//
// ERDDHTMLComponent.java: Class file for WO Component 'ERDDHTMLComponent'
// Project simple
//
// Created by ak on Wed Mar 20 2002
//
package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSValidation;

import er.extensions.ERXStringUtilities;

/**
 * Rich text edit component.<br />
 * @deprecated use ERDEditHTML instead
 */

public class ERDDHTMLComponent extends ERDCustomEditComponent {
    static final Logger log = Logger.getLogger(ERDDHTMLComponent.class);

    String varName = null;

    public ERDDHTMLComponent(WOContext context) {
        super(context);
    }

    public boolean isStateless() {
	return false;
    }

    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public void reset() {
        super.reset();
        varName = null;
    }
    
    public String varName()  {
	if(varName == null) {
	    varName = ERXStringUtilities.replaceStringByStringInString("-", "_", "dhtml-" + context().elementID().hashCode() + "-" + key());
	    varName = ERXStringUtilities.replaceStringByStringInString(".", "_", varName);
	    log.debug(varName);
	}
	return varName;
    }
    
    public void takeValuesFromRequest(WORequest q, WOContext c) throws NSValidation.ValidationException {
        super.takeValuesFromRequest(q,c);
        try {
            object().validateTakeValueForKeyPath(objectKeyPathValue(),key());
        } catch(Throwable e) {
            validationFailedWithException (e, objectKeyPathValue(), key());
        }
    }
}
