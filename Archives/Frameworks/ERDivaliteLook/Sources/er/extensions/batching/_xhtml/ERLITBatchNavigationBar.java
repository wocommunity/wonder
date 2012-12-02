package er.extensions.batching._xhtml;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.extensions.batching.ERXBatchNavigationBar;

/**
 * ERXBatchNavigationBar less presentation features
 * 
 * @binding container the update container to update on batch actions
 * @binding showForm whether to use a form or not
 * @binding displayGroup the WODisplayGroup that is being controlled
 * @binding objectName the name of the type of object that is contained in the WODisplayGroup
 * @binding sortKeyList an NSArray of sort key paths that will be displayed in a popup button
 * @binding clearSelection boolean that indicates if the selection should be reset on paging (default false)
 */
public class ERLITBatchNavigationBar extends ERXBatchNavigationBar {

	public ERLITBatchNavigationBar(WOContext context) {
		super(context);
	}
	 
	// actions
	public WOComponent submit() {
		return null;
	}
    
	/*
	 * Workaround for when there are more than one batch nav bar on a list page
	 */
    public boolean isStateless() {
    	return false;
    }
    
    /*
     * @see ERXPluralString value()
     */
    public String objectName() {
        Number c=(Number)valueForKey("objectCount");
        String value = (String)valueForBinding("objectName");
        return localizer().plurifiedString(value, c!=null ? c.intValue() : 0);
    }
 }