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
import org.apache.log4j.Category;

public class ERDDHTMLComponent extends ERDCustomEditComponent {
    static final Category cat = Category.getInstance(ERDDHTMLComponent.class);

    String varName = null;

    public Object objectKeyPathValue() {
//        cat.warn("current:" + super.objectKeyPathValue());
       return super.objectKeyPathValue();
    }
    public void setObjectKeyPathValue(Object newValue) {
        super.setObjectKeyPathValue(newValue);
//        cat.warn("new:" + newValue + " - "  + object() + " - " + key());
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
	    cat.debug(varName);
	}
	return varName;
    }
}
