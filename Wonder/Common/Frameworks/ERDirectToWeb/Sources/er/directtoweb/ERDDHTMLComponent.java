//
// ERDDHTMLComponent.java: Class file for WO Component 'ERDDHTMLComponent'
// Project simple
//
// Created by ak on Wed Mar 20 2002
//
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

public class ERDDHTMLComponent extends ERDCustomEditComponent {
    static final ERXLogger log = ERXLogger.getERXLogger(ERDDHTMLComponent.class);

    String varName = null;

    public Object objectKeyPathValue() {
//        log.warn("current:" + super.objectKeyPathValue());
       return super.objectKeyPathValue();
    }
    public void setObjectKeyPathValue(Object newValue) {
        super.setObjectKeyPathValue(newValue);
//        log.warn("new:" + newValue + " - "  + object() + " - " + key());
    }

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
	    varName = ERXExtensions.replaceStringByStringInString("-", "_", "dhtml-" + context().elementID().hashCode() + "-" + key());
	    varName = ERXExtensions.replaceStringByStringInString(".", "_", varName);
	    log.debug(varName);
	}
	return varName;
    }
}
