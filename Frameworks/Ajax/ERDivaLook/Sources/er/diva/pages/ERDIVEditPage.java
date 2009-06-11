package er.diva.pages;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;
import er.directtoweb.pages.ERD2WTabInspectPage;
import er.diva.ERDIVPageInterface;

/**
 * Edit page template for Diva
 * 
 * @author mendis
 *
 */
public class ERDIVEditPage extends ERD2WTabInspectPage implements ERDIVPageInterface {	
    public ERDIVEditPage(WOContext context) {
        super(context);
    }
    
    // accessors
    public String stylesheet() {
    	return (String) d2wContext().valueForKey(ERDIVPageInterface.Keys.Stylesheet);
    }
    
    // actions
    @Override
    public WOComponent cancelAction() {
    	String subTask = (String) d2wContext().valueForKey("subTask");
    	
        if (subTask != null && subTask.equals("wizard")) {
        	clearValidationFailed();
        } return super.cancelAction();
    }

    public boolean showConfirmationPanel;
    
    @Override
    public WOComponent nextPage(boolean doConfirm) {
        Object inspectConfirmConfigurationName = d2wContext().valueForKey("inspectConfirmConfigurationName");
        if(doConfirm && inspectConfirmConfigurationName != null && ! "".equals(inspectConfirmConfigurationName)) {
        	showConfirmationPanel = true;
        	return context().page();
        } else {
        	showConfirmationPanel = false;
        	return super.nextPage(false);
        } 
    }
    
    // R/R
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);

    	// add page style sheet
    	if (stylesheet() != null) {
    		AjaxUtils.addStylesheetResourceInHead(context, response, "app", stylesheet());
    	}
    }
}