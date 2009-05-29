package er.diva.pages;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;
import er.directtoweb.pages.ERD2WTabInspectPage;
import er.diva.ERDIVPageInterface;


/**
 * Inspect page template for Diva 'look'
 * 
 * @author mendis
 *
 */
public class ERDIVInspectPage extends ERD2WTabInspectPage implements ERDIVPageInterface {
    public ERDIVInspectPage(WOContext context) {
        super(context);
    }
    
    // accessors
    public String stylesheet() {
    	return (String) d2wContext().valueForKey(ERDIVPageInterface.Keys.Stylesheet);
    }
}