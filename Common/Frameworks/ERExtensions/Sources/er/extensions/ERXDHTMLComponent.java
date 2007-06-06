//
// ERXDHTMLComponent.java: Class file for WO Component 'ERXDHTMLComponent'
// Project simple
//
// Created by ak on Tue Mar 19 2002
//
package er.extensions;

import com.webobjects.appserver.WOContext;

/** ERXDHTMLComponent covers a textarea with a DHTMLEdit control (IE,PC only)
  * It is pretty cool as it can be used as a replacement for WOText, since it works no matter is JS is enabled or not.
  * @deprecated use ERDEditHTML instead
  */
public class ERXDHTMLComponent extends ERXStatelessComponent {
    
    public ERXDHTMLComponent(WOContext context) {
        super(context);
    }

    public void reset() {
        super.reset();
        varName = null;
    }

    public String spanName() {
	return "span_" + varName();
    }
    String varName;
    public String varName()  {
	if(varName == null) {
            varName = (String)valueForBinding("varName");
            if(varName == null)
                varName = "dhtml_" + ERXStringUtilities.replaceStringByStringInString("-", "_", "" + context().elementID().hashCode());
            varName = ERXStringUtilities.replaceStringByStringInString(".", "_", varName);
	}
	return varName;
    }
    
    private static String _dhtmlJavaScriptUrl;
    public String dhtmlJavaScriptUrl() {
        if (_dhtmlJavaScriptUrl==null) {
            _dhtmlJavaScriptUrl= application().resourceManager().urlForResourceNamed("dhtml.js", "ERExtensions", null, context().request());
        }
        return _dhtmlJavaScriptUrl;
    }
}
