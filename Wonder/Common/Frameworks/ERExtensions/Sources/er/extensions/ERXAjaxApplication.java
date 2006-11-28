package er.extensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

/**
 * The ERXAjaxApplication is the part of ERXApplication that is modified to
 * handle Ajax requests.  If you want to use the Ajax framework without
 * using other parts of Project Wonder (i.e. ERXSession or ERXApplication),
 * you should steal all of the code in ERXAjaxSession and ERXAjaxApplication.
 * 
 * You should also steal ERXWOForm (or at least the force form submit stuff)
 * if you want partial form submits to work properly (i.e. the dependent lists
 * example).
 * 
 * @author mschrag
 */
public abstract class ERXAjaxApplication extends WOApplication {
    /**
     * Checks if the page should not be stored in the cache 
     */
    public static boolean shouldNotStorePage(WOMessage message) {
  	  return (message != null && (message.headerForKey(ERXAjaxSession.DONT_STORE_PAGE) != null || (message.userInfo() != null && message.userInfo().objectForKey(ERXAjaxSession.DONT_STORE_PAGE) != null)));
    }

    /**
     * Removes Ajax response headers that are no longer necessary.
     * @param response the response to clean up
     */
    public static void cleanUpHeaders(WOResponse response) {
    	if (response != null) {
	    	response.removeHeadersForKey(ERXAjaxSession.DONT_STORE_PAGE);
	    	response.removeHeadersForKey(ERXAjaxSession.PAGE_REPLACEMENT_CACHE_LOOKUP_KEY);
    	}
    }
    
    /**
     * Checks if the page should not be stored in the cache 
     */
    public static boolean shouldNotStorePage(WOContext context) {
      WORequest request = context.request();
      WOResponse response = context.response();
      // MS: The "AJAX_SUBMIT_BUTTON_NAME" check is a total hack, but if your page structure changes such that the form that
      // is being submitted to is hidden, it ends up not notifying the system not to cache the page.
      boolean shouldNotStorePage = (shouldNotStorePage(response) || shouldNotStorePage(request) || request.formValueForKey("AJAX_SUBMIT_BUTTON_NAME") != null);
      return shouldNotStorePage;
    }

    /**
     * Overridden to allow for redirected responses.
     * @param request object
     * @param context object
     */
    public WOActionResults invokeAction(WORequest request, WOContext context) {
        WOActionResults results = super.invokeAction(request, context);
        // MS: This is to support AjaxUpdateContainer.
        if (results == null && ERXAjaxApplication.shouldNotStorePage(context)) {
        	WOResponse response = context.response();
        	ERXAjaxApplication.cleanUpHeaders(response);
        	results = response;
        }
        return results;
    }
}


