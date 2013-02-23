package er.mootools.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;

import er.ajax.AjaxFlickrBatchNavigation;

public class ERMTD2WBatchNavigationBar extends AjaxFlickrBatchNavigation {
    
	private D2WContext _d2wContext;
	
	public ERMTD2WBatchNavigationBar(WOContext context) {
        super(context);
    }

	public D2WContext d2wContext() {
		return _d2wContext;
	}

	public void setD2wContext(D2WContext d2wContext) {
		_d2wContext = d2wContext;
	}

	public String batchNavigationListItemClassName() {
		return isCurrentPageNumber() 
				? (String)d2wContext().valueForKey("batchNavigationCurrentListItemClassName") 
				: null;
	}
	
}