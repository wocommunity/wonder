package er.extensions.appserver.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXWOContext;
import er.extensions.foundation.ERXProperties;

/**
 * ERXAjaxApplication is the part of ERXApplication that handles Ajax requests.
 * If you want to use the Ajax framework without using other parts of Project
 * Wonder (i.e. ERXSession or ERXApplication), you should steal all of the code
 * in ERXAjaxSession, ERXAjaxApplication, and ERXAjaxContext.
 * 
 * @property er.extensions.ERXAjaxApplication.allowContextPageResponse
 *
 * @author mschrag
 */
public abstract class ERXAjaxApplication extends WOApplication {
	public static final String KEY_AJAX_SUBMIT_BUTTON = "AJAX_SUBMIT_BUTTON_NAME";
	public static final String KEY_PARTIAL_FORM_SENDER_ID = "_partialSenderID";
	public static final String KEY_UPDATE_CONTAINER_ID = "_u";
	public static final String KEY_REPLACED = "_r";

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

	public static boolean shouldIgnoreResults(WORequest request, WOContext context, WOActionResults results) {
		boolean shouldIgnoreResults = false;
		if (results == context.page() && !ERXAjaxApplication.isAjaxReplacement(request)) {
			WOApplication application = WOApplication.application();
			if (application instanceof ERXAjaxApplication) {
				shouldIgnoreResults = !((ERXAjaxApplication)application).allowContextPageResponse(); 
			}
			else {
				shouldIgnoreResults = true;
			}
		}
		return shouldIgnoreResults;
	}
	
	/**
	 * Ajax links have a ?_u=xxx&2309482093 in the url which makes it look like a form submission to WebObjects.
	 * Therefore takeValues is called on every update even though many many updates aren't submits.  This method
	 * checks to see if all you have is a _u or _r and an ismap (the #) param for form values.  If so, it's not 
	 * a form submission and takeValues can be skipped.
	 *
	 * @see com.webobjects.appserver.WOApplication#takeValuesFromRequest(com.webobjects.appserver.WORequest, com.webobjects.appserver.WOContext)
	 *
	 * @param request
	 * @param context
	 */
	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		boolean shouldTakeValuesFromRequest = true;
		if (!request.isMultipartFormData() && ERXAjaxApplication.isAjaxRequest(request)) {
			NSDictionary formValues = request.formValues();
			int formValuesCount = formValues.count();
			if (formValuesCount == 2 && (formValues.containsKey(ERXAjaxApplication.KEY_UPDATE_CONTAINER_ID) || 
					                     formValues.containsKey(ERXAjaxApplication.KEY_REPLACED)) && 
					                     formValues.containsKey(WORequest._IsmapCoords)) {
				shouldTakeValuesFromRequest = false;
			}
		}
		if (shouldTakeValuesFromRequest) {
			super.takeValuesFromRequest(request, context);
		}
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
			if (shouldIgnoreResults(request, context, results)) {
				results = null;
			}
			if (results == null && !ERXAjaxApplication.isAjaxReplacement(request)) {
				WOResponse response = context.response();

				if (_responseDelegate != null) {
					results = _responseDelegate.handleNullActionResults(request, response, context);
					response = context.response();
				}

				// MS: We were removing headers, and I really don't know WHY.  It was causing AUC's
				// inside of AjaxModalDialogs to break ... ERXAjaxSession is already cleaning up 
				// headers at the end, so this seems very odd to me.  If you remove headers here,
				// it causes ERXAjaxSession to not treat the response like an Ajax response.
				//ERXAjaxApplication.cleanUpHeaders(response);
				results = response;
			}
		}
		return results;
	}
	
	/**
	 * Allow for context.page() as a result to an ajax call. Currently for debugging.
	 */
	// AK: REMOVEME if WOGWT doesn't work out...
	private Boolean _allowContextPageResponse;
	private boolean allowContextPageResponse() {
		if(_allowContextPageResponse == null) {
			_allowContextPageResponse = ERXProperties.booleanForKey("er.extensions.ERXAjaxApplication.allowContextPageResponse");
		}
		return _allowContextPageResponse;
	}

	public static void setForceStorePage(WOMessage message) {
		ERXWOContext.contextDictionary().setObjectForKey(Boolean.TRUE, ERXAjaxSession.FORCE_STORE_PAGE);
	}
	
	/**
	 * Checks if the page should not be stored in the cache
	 */
	public static boolean forceStorePage(WOMessage message) {
		NSDictionary userInfo = NSDictionary.EmptyDictionary;
		if (message != null) {
			userInfo = ERXWOContext.contextDictionary();
		}
		return (message != null && (message.headerForKey(ERXAjaxSession.FORCE_STORE_PAGE) != null || (userInfo.objectForKey(ERXAjaxSession.FORCE_STORE_PAGE) != null)));
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
		boolean shouldNotStorePage = (shouldNotStorePage(response) || shouldNotStorePage(request) || isAjaxSubmit(request)) && !forceStorePage(response);
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
	 * Returns true if this is an ajax replacement (_r key is set).
	 */
	public static boolean isAjaxReplacement(WORequest request) {
		return request.formValueForKey(ERXAjaxApplication.KEY_REPLACED) != null;
	}
	
	/**
	 * Returns true if this request will update an AjaxUpdateContainer.
	 */
	public static boolean isAjaxUpdate(WORequest request) {
		return request.formValueForKey(KEY_UPDATE_CONTAINER_ID) != null;
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
