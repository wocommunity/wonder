package er.diva.pages;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;
import er.directtoweb.pages.ERD2WTabInspectPage;
import er.diva.ERDIVPageInterface;
import er.extensions.foundation.ERXProperties;

/**
 * Edit page template for Diva.
 * 
 * @property er.prototaculous.useUnobtrusively Support for Unobtrusive Javascript programming.
 *
 * @author mendis
 */
public class ERDIVEditPage extends ERD2WTabInspectPage implements ERDIVPageInterface {	
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.prototaculous.useUnobtrusively", true);
	
    public ERDIVEditPage(WOContext context) {
        super(context);
    }
    
    // accessors
    public String stylesheet() {
    	return (String) d2wContext().valueForKey(ERDIVPageInterface.Keys.Stylesheet);
    }
    
    /*
     * To avoid validation when switching tabs
     */
    @Override
    public boolean switchTabAction() {
    	return true;
    }
    
    // actions
    @Override
    public WOComponent cancelAction() {
    	String subTask = (String) d2wContext().valueForKey("subTask");
    	
        if (subTask != null && subTask.equals("wizard")) {
        	clearValidationFailed();
        } return super.cancelAction();
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
