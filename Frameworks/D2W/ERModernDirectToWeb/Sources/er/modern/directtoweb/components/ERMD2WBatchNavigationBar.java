package er.modern.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;

import er.ajax.AjaxFlickrBatchNavigation;

/**
 * D2W Batch navigation bar based on AjaxFlickrBatchNavigation
 * 
 * @author davidleber
 *
 */
public class ERMD2WBatchNavigationBar extends AjaxFlickrBatchNavigation {
	
	private D2WContext _d2wContext;
	
    public ERMD2WBatchNavigationBar(WOContext context) {
        super(context);
    }
    
    // ACCESSORS
    
    public D2WContext d2wContext() {
    	return _d2wContext;
    }
    
    public void setD2wContext(D2WContext c) {
    	_d2wContext = c;
    }
    
}