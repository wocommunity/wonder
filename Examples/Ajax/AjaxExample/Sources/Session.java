import com.webobjects.appserver.*;

import er.extensions.appserver.*;

/**
 * Everything related to saving pages is handled now in ERXSession.
 *
 *
 * @author ak
 */

public class Session extends ERXSession {

	public String selectedTab = "InlineContent";
	
	public void savePage(WOComponent page) {
//		NSLog.out.appendln("Session shouldNotStorePage: " + ERXAjaxApplication.shouldNotStorePage(context()));
//		NSLog.out.appendln("rquest key: " + context().request().headerForKey(ERXAjaxSession.PAGE_REPLACEMENT_CACHE_LOOKUP_KEY));
//		NSLog.out.appendln("response key: " + context().response().headerForKey(ERXAjaxSession.PAGE_REPLACEMENT_CACHE_LOOKUP_KEY));
	      
		super.savePage(page);
	}


}