//
// ERXDHTMLComponent.java: Class file for WO Component 'ERXDHTMLComponent'
// Project simple
//
// Created by ak on Tue Mar 19 2002
//
package er.extensions.components;

import org.apache.commons.lang.StringUtils;

import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXStringUtilities;

/** ERXDHTMLComponent covers a textarea with a DHTMLEdit control (IE,PC only)
  * It is pretty cool as it can be used as a replacement for WOText, since it works no matter is JS is enabled or not.
  * @deprecated use ERDEditHTML instead
  */
@Deprecated
public class ERXDHTMLComponent extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXDHTMLComponent(WOContext context) {
        super(context);
    }

    @Override
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
                varName = "dhtml_" + StringUtils.replace("" + context().elementID().hashCode(), "-", "_");
            varName = StringUtils.replace(varName, ".", "_");
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
