package er.extensions.components._ajax;

import com.webobjects.appserver.*;

import er.ajax.AjaxUtils;
import er.extensions.components.ERXErrorDictionaryPanel;
import er.extensions.foundation.ERXProperties;

/**
 * Prototype event notification for errors
 * FIXME: will have to modify for Ajax pageconfigs
 * 
 * @author mendis
 *
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
    		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
    	}
    }
}