package er.prototaculous;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxAccordion;
import er.ajax.AjaxUtils;
import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.foundation.ERXProperties;

/**
 * WAS: Encapsulation of http://www.stickmanlabs.com/accordion
 * NOW: http://nettuts.com/javascript-ajax/create-a-simple-intelligent-accordion-effect-using-prototype-and-scriptaculous/
 * 
 * Extends the api of AjaxAccordion. i.e:
 * @see er.ajax.AjaxAccordion
 * 
 * @property er.prototculous.useUnobtrusively	If you want the component to include its JavaScripts and CSS set to false. (This is the default).
 * 												This is to support Unobtrusive Javascript programming.
 * 
 * @author mendis
 *
 */
public class AjaxAccordion2 extends AjaxAccordion {
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.prototculous.useUnobtrusively", false);
	
    public AjaxAccordion2(WOContext context) {
        super(context);
    }
    
    // accessors
    private String script() {
    	return isAjaxRequest() ? _script() : "document.observe(\"dom:loaded\", function() {" + _script() + "});";
    }
    
    private String _script() {
    	return "if (null == " + accordionVar() + ") { var " + accordionVar() + " = new Accordion(\"" + accordionID() + "\"); }";
    }
    
    private String accordionVar() {
    	return "accordion" + accordionID();
    }
    
    public String elementName() {
    	String _elementName = (String) valueForBinding("elementName");
    	return (_elementName != null) ? _elementName : "div";
    }
    
    public boolean disabled() {
    	return booleanValueForBinding("disabled", false);
    }
    
    /*
     * Checks the existance of the header 'x-requested-with'
     */
    public boolean isAjaxRequest() {
		return ERXAjaxApplication.isAjaxRequest(context().request());		
    }
    
	@Override
    public NSDictionary<?,?> createAjaxOptions() { return null; }
    
    // R&R
    @Override
    protected void addRequiredWebResources(WOResponse response) {
		// include javascripts if not being used unobtrusively
    	if (!useUnobtrusively) {
    		addScriptResourceInHead(response, "prototype.js");
    		addScriptResourceInHead(response, "scriptaculous.js");
    		addScriptResourceInHead(response, "WO2", "accordion.js");
    	} addStylesheetResourceInHead(response, "WO2", "AjaxAccordion2.css");
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
        if (!disabled()) 
        	AjaxUtils.addScriptCodeInHead(response, context, script());	
    }
}