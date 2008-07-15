package er.ajax;

import java.util.Enumeration;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXResponse;
import er.extensions.ERXAjaxApplication.ERXAjaxResponseDelegate;
import er.extensions.ERXKeyValueCodingUtilities;

/**
 * AjaxResponse provides support for performing an AjaxUpdate in the same response
 * as an ajax action.
 * 
 * @author mschrag
 */
public class AjaxResponse extends ERXResponse {
	public static final String AJAX_UPDATE_PASS = "_ajaxUpdatePass";
	private static NSMutableArray _responseAppenders; 
	
	/**
	 * Add a response appender to the list of response appender.  At the end of
	 * every AjaxResponse, the AjaxResponseAppenders are given an opportunity to
	 * tag along. For instance, if you have an area at the top of your pages that
	 * show errors or notifications, you may want all of your ajax responses to have
	 * a chance to trigger an update of this area, so you could register an 
	 * AjaxResponseAppender that renders a javascript block that calls 
	 * MyNotificationsUpdate() only if there are notifications to be shown. Without
	 * response appenders, you would have to include a check in all of your 
	 * components to do this. 
	 * 
	 * @param responseAppender the appender to add
	 */
	public static void addAjaxResponseAppender(AjaxResponseAppender responseAppender) {
		if (_responseAppenders == null) {
			_responseAppenders = new NSMutableArray();
		}
		_responseAppenders.addObject(responseAppender);
	}
	
	private WORequest _request;
	private WOContext _context;

	public AjaxResponse(WORequest request, WOContext context) {
		_request = request;
		_context = context;
	}

	public WOResponse generateResponse() {
		if (AjaxUpdateContainer.hasUpdateContainerID(_request)) {
			String originalSenderID = _context.senderID();
			_context._setSenderID("");
			try {
				StringBuffer content;
				//AK: don't ask...
				if (((Object)_content) instanceof StringBuffer) {
					content = (StringBuffer)(Object)_content;
					ERXKeyValueCodingUtilities.takePrivateValueForKey(this, new StringBuffer(),  "_content");
				} else {
					StringBuilder builder = (StringBuilder)(Object) _content;
					content = new StringBuffer();
					ERXKeyValueCodingUtilities.takePrivateValueForKey(this, new StringBuilder(),  "_content");
				}
				NSMutableDictionary userInfo = AjaxUtils.mutableUserInfo(_request);
				userInfo.setObjectForKey(Boolean.TRUE, AjaxResponse.AJAX_UPDATE_PASS);
				WOActionResults woactionresults = WOApplication.application().invokeAction(_request, _context);
				_content.append(content);
				if (_responseAppenders != null) {
					Enumeration responseAppendersEnum = _responseAppenders.objectEnumerator();
					while (responseAppendersEnum.hasMoreElements()) {
						AjaxResponseAppender responseAppender = (AjaxResponseAppender) responseAppendersEnum.nextElement();
						responseAppender.appendToResponse(this, _context);
					}
				}
				int length;
				if (((Object)_content) instanceof StringBuffer) {
					StringBuffer buffer = (StringBuffer)(Object)_content;
					length = buffer.length();
				} else {
					StringBuilder builder = (StringBuilder)(Object) _content;
					length = builder.length();
				}
				if (length == 0) {
					Ajax.log.warn("You performed an Ajax update, but no response was generated. A common cause of this is that you spelled your updateContainerID wrong.  You specified a container ID '" + AjaxUpdateContainer.updateContainerID(_request) + "'."); 
				}
			}
			finally {
				_context._setSenderID(originalSenderID);
			}
		}
		return this;
	}
	
	public static boolean isAjaxUpdatePass(WORequest request) {
		NSDictionary userInfo = AjaxUtils.mutableUserInfo(request);
		return userInfo != null && userInfo.valueForKey(AjaxResponse.AJAX_UPDATE_PASS) != null;
	}
	
	/**
	 * If you click on, for instance, an AjaxInPlace, that sends a request to the server that
	 * you want to be in edit mode.  The server flips its state so it is now in edit mode, but
	 * if you click a second time before the response comes back, the state has changed, so
	 * your click doesn't actually get delivered to an Ajax component.  If there was no
	 * recipient of your click, it means that there also is no update container specified
	 * to be updated and your response was not turned into an AjaxResponse.  This means that
	 * the second click causes the contents to be replaced with a blank.
	 * 
	 * AjaxResponseDelegate looks for the confluence of all of these events and uses its 
	 * fallback of pulling the update container ID out of the query parameters (generated on 
	 * the Javascript side) and forces an AjaxResponse process to occur.
	 * 
	 * @author mschrag
	 */
	public static class AjaxResponseDelegate implements ERXAjaxResponseDelegate {
		public WOActionResults handleNullActionResults(WORequest request, WOResponse response, WOContext context) {
			WOActionResults finalActionResults = response;
			// If it's not an AjaxResponse, it's suspect ... It means it did not
			// go through an Ajax update pass, so it might be messed up
			if (!(response instanceof AjaxResponse)) {
				// This check is pretty much trickery. It turns out that when the problem
				// that we're looking for happens, you end up with a null content length, and
				// it seems to be the fastest way to determine that we're in this state.
				String contentLength = response.headerForKey("content-length");
				if (contentLength == null) {
					// So updateContainerID never got set by the triggered action, so let's
					// pull it out of the query parameters.  This isn't ideal, but it's
					// better than sending you back a big blank component to replace whatever
					// it is you double-clicked on.
					String updateContainerID = request.stringFormValueForKey(AjaxUpdateContainer.UPDATE_CONTAINER_ID_KEY);
					if (updateContainerID != null) {
						// .. .and let's make an AjaxResponse so we get our update container
						// to be evaluated
						AjaxUpdateContainer.setUpdateContainerID(request, updateContainerID);
						finalActionResults = AjaxUtils.createResponse(request, context);
					}
				}
			}
			return finalActionResults;
		}
	}
}
