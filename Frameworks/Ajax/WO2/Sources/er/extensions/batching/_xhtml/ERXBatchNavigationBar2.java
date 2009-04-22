package er.extensions.batching._xhtml;

import com.webobjects.appserver.*;
import er.extensions.batching.ERXBatchNavigationBar;

/**
 * ERXBatchNavigationBar less presentation features
 * 
 * @binding d2wContext the D2W context that this component is in
 * @binding displayGroup the WODisplayGroup that is being controlled
 * @binding objectName the name of the type of object that is contained in the WODisplayGroup
 * @binding sortKeyList an NSArray of sort key paths that will be displayed in a popup button
 * @binding clearSelection boolean that indicates if the selection should be reset on paging (default false)
 */
public class ERXBatchNavigationBar2 extends ERXBatchNavigationBar {

	public ERXBatchNavigationBar2(WOContext context) {
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
 }