package er.ajax._private;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.ajax.AjaxAccordion;
import er.ajax.AjaxUtils;

/**
 * WAS: Encapsulation of http://www.stickmanlabs.com/accordion
 * NOW: http://nettuts.com/javascript-ajax/create-a-simple-intelligent-accordion-effect-using-prototype-and-scriptaculous/
 * 
 * Extends the api of AjaxAccordion. i.e:
 * @see er.ajax.AjaxAccordion
 * 
 * @author mendis
 *
 */
public class AjaxAccordion2 extends AjaxAccordion {
    public AjaxAccordion2(WOContext context) {
        super(context);
    }
    
    // accessors
    private String _script() {
    	return "document.observe('dom:loaded', function() { if (null == " + accordionVar() + ") { var " + accordionVar() + " = new Accordion(\"" + accordionID() + "\", 1); } })";
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
    
    @Override
    public NSDictionary createAjaxOptions() { return null; }
    
    // R&R
    @Override
    protected void addRequiredWebResources(WOResponse response) {
        addScriptResourceInHead(response, "prototype.js");
        addScriptResourceInHead(response, "scriptaculous.js");
        addScriptResourceInHead(response, "WO2", "accordion.js");
        addStylesheetResourceInHead(response, "WO2", "AjaxAccordion2.css");
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
        if (!disabled()) 
        	AjaxUtils.addScriptCodeInHead(response, context, _script());	
    }
}