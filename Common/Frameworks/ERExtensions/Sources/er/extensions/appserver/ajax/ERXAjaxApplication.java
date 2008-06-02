package er.extensions.appserver.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;

import er.extensions.appserver.ERXWOContext;

/**
 * ERXAjaxApplication is the part of ERXApplication that handles Ajax requests.
 * If you want to use the Ajax framework without using other parts of Project
 * Wonder (i.e. ERXSession or ERXApplication), you should steal all of the code
 * in ERXAjaxSession, ERXAjaxApplication, and ERXAjaxContext.
 * 
 * @author mschrag
 */
public abstract class ERXAjaxApplication extends WOApplication {
	public static final String KEY_AJAX_SUBMIT_BUTTON = "AJAX_SUBMIT_BUTTON_NAME";
	public static final String KEY_PARTIAL_FORM_SENDER_ID = "_partialSenderID";

	private ERXAjaxResponseDelegate _responseDelegate;

	/**
	 * Sets the response delegate for this application.
	 * 
	 * @param responseDelegate
	 *            the response delegate
	 */
	public void setResponseDelegate(ERXAjaxResponseDelegate responseDelegate) {
		_responseDelegate = responseDelegate;
	}

	/**
	 * Overridden to allow for redirected responses.
	 * 
	 * @param request
	 *            object
	 * @param context
	 *            object
	 */
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults results = super.invokeAction(request, context);
		// MS: This is to support AjaxUpdateContainer.
		// MS: Note that if results == context.page() something probably went
		// wrong
		if (ERXAjaxApplication.shouldNotStorePage(context)) {
			if (results == context.page()) {
				NSLog.out.appendln("ERXAjaxApplication.invokeAction: An Ajax response returned context.page(), which is almost certainly an error.");
				results = null;
			}
			if (results == null) {
				WOResponse response = context.response();

				if (_responseDelegate != null) {
					results = _responseDelegate.handleNullActionResults(request, response, context);
					response = context.response();
				}

				ERXAjaxApplication.cleanUpHeaders(response);
				results = response;
			}
		}
		return results;
	}

	/**
	 * Checks if the page should not be stored in the cache
	 */
	public static boolean shouldNotStorePage(WOMessage message) {
		NSDictionary userInfo = NSDictionary.EmptyDictionary;
		if (message != null) {
			userInfo = ERXWOContext.contextDictionary();
		}
		return (message != null && (message.headerForKey(ERXAjaxSession.DONT_STORE_PAGE) != null || (userInfo.objectForKey(ERXAjaxSession.DONT_STORE_PAGE) != null)));
	}

	/**
	 * Removes Ajax response headers that are no longer necessary.
	 * 
	 * @param response
	 *            the response to clean up
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
		// MS: The "AJAX_SUBMIT_BUTTON_NAME" check is a total hack, but if your
		// page structure changes such that the form that
		// is being submitted to is hidden, it ends up not notifying the system
		// not to cache the page.
		boolean shouldNotStorePage = (shouldNotStorePage(response) || shouldNotStorePage(request) || isAjaxSubmit(request));
		return shouldNotStorePage;
	}

	/**
	 * Return whether or not the given request is an Ajax request.
	 * 
	 * @param request
	 *            the request the check
	 */
	public static boolean isAjaxRequest(WORequest request) {
		String requestedWith = request.headerForKey("x-requested-with");
		return "XMLHttpRequest".equals(requestedWith);
	}

	/**
	 * Returns the form name of the partial form submission.
	 * 
	 * @param request
	 *            the request
	 * @return the form name of the partial form submission
	 */
	public static String partialFormSenderID(WORequest request) {
		return request.stringFormValueForKey(ERXAjaxApplication.KEY_PARTIAL_FORM_SENDER_ID);
	}

	/**
	 * Returns the form name of the submitting ajax button.
	 * 
	 * @param request
	 *            the request
	 * @return the form name of the submitting ajax button
	 */
	public static String ajaxSubmitButtonName(WORequest request) {
		return request.stringFormValueForKey(ERXAjaxApplication.KEY_AJAX_SUBMIT_BUTTON);
	}

	/**
	 * Returns true if this is an ajax submit.
	 */
	public static boolean isAjaxSubmit(WORequest request) {
		return (ERXAjaxApplication.ajaxSubmitButtonName(request) != null);
	}

	/**
	 * ERXAjaxResponseDelegate receives callbacks from within the R-R loop when
	 * certain situations occur.
	 * 
	 * @author mschrag
	 */
	public static interface ERXAjaxResponseDelegate {
		/**
		 * When an Ajax request generates a null result, this method is called
		 * to provide an alternative response.
		 * 
		 * @param request
		 *            the request
		 * @param response
		 *            the response
		 * @param context
		 *            the context
		 * @return the replacement results to use
		 */
		public WOActionResults handleNullActionResults(WORequest request, WOResponse response, WOContext context);
	}
}
