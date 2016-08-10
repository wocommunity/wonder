package er.diva.pages;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2WContext;

import er.ajax.AjaxUtils;
import er.directtoweb.pages.ERD2WQueryPage;
import er.diva.ERDIVPageInterface;
import er.extensions.foundation.ERXProperties;

/**
 *
 * @property er.prototaculous.useUnobtrusively Support for Unobtrusive Javascript programming.
 */
public class ERDIVQueryPage extends ERD2WQueryPage implements ERDIVPageInterface {
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.prototaculous.useUnobtrusively", true);

	public ERDIVQueryPage(WOContext context) {
        super(context);
    }
    
    // accessors
    public String stylesheet() {
    	return (String) d2wContext().valueForKey(ERDIVPageInterface.Keys.Stylesheet);
    }
    
	public String contentContainerID() {
		return subContext().valueForKey("id") + "_container";
	}
    
	protected D2WContext _subContext;
	
	public D2WContext subContext() {
		return _subContext;
	}
	
	public void setSubContext(D2WContext aContext) {
		_subContext = aContext;
	}
	
	/**
	 * Gives each property its own d2wContext rather than sharing one
	 * Necessary for ajax or dyanmic D2W
	 */
	@Override
	public void setPropertyKey(String propertyKey) {
		_subContext = new D2WContext(d2wContext());
		_subContext.takeValueForKey(propertyKey, "propertyKey");
	}
    
    // R/R
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);

    	// add page style sheet
    	if (!useUnobtrusively && stylesheet() != null) {
    		AjaxUtils.addStylesheetResourceInHead(context, response, "app", stylesheet());
    	}
    }
}
