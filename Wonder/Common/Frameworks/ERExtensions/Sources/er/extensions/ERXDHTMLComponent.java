//
// ERXDHTMLComponent.java: Class file for WO Component 'ERXDHTMLComponent'
// Project simple
//
// Created by ak on Tue Mar 19 2002
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import org.apache.log4j.Category;

/** ERXDHTMLComponent covers a textarea with a DHTMLEdit control (IE,PC only)
  * It is pretty cool as it can be used as a replacement for WOText, since it works no matter is JS is enabled or not.
  */
public class ERXDHTMLComponent extends ERXStatelessComponent {
    static final Category cat = Category.getInstance(ERXDHTMLComponent.class);
    
    public ERXDHTMLComponent(WOContext context) {
        super(context);
    }

    public String spanName() {
	return "span_" + varName();
    }
    
    public String varName()  {
	String varName = (String)valueForBinding("varName");
	if(varName == null) {
	    varName = "dhtml_" + ERXExtensions.replaceStringByStringInString("-", "_", "" + context().elementID().hashCode());
	}
	return varName;
    }
}
