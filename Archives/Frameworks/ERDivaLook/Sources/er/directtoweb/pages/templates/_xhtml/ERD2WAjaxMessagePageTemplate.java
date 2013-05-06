package er.directtoweb.pages.templates._xhtml;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;
import er.directtoweb.pages.templates.ERD2WMessagePageTemplate;
import er.diva.ERDIVPageInterface;

public class ERD2WAjaxMessagePageTemplate extends ERD2WMessagePageTemplate implements ERDIVPageInterface {
    public ERD2WAjaxMessagePageTemplate(WOContext context) {
        super(context);
    }
    
    // accessors
    @Override
    public String message() {
    	return super.message().trim();
    }
    
    // accessors
    public String stylesheet() {
    	return (String) d2wContext().valueForKey(ERDIVPageInterface.Keys.Stylesheet);
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