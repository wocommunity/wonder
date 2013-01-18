package er.extensions.components._ajax;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.components.ERXErrorDictionaryPanel;
import er.extensions.foundation.ERXProperties;

/**
 * Prototype event notification for errors
 * 
 * @property er.prototaculous.useUnobtrusively Support for Unobtrusive Javascript programming.
 *
 * @author mendis
 */
public class ERXAjaxErrorDictionaryPanel extends ERXErrorDictionaryPanel {
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.prototaculous.useUnobtrusively", true);

    public ERXAjaxErrorDictionaryPanel(WOContext context) {
        super(context);
    }
    
    // R/R
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
    	if (!useUnobtrusively) {
    		ERXResponseRewriter.addScriptResourceInHead(response, context, "Ajax", "prototype.js");
    	}
    }
}
