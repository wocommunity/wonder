package er.ajax;

import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSRange;

import er.extensions.appserver.ERXResponse;
import er.extensions.appserver.ERXWOContext;
import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.appserver.ajax.ERXAjaxApplication.ERXAjaxResponseDelegate;

/**
 * AjaxResponse provides support for performing an AjaxUpdate in the same response
 * as an ajax action.
 * 
 * @author mschrag
 */
public class AjaxResponse extends ERXResponse {
	private static final Logger log = LoggerFactory.getLogger(Ajax.class);
	public static final String AJAX_UPDATE_PASS = "_ajaxUpdatePass";
	private static NSMutableArray _responseAppenders; 
	
	/**
	 * Add a response appender to the list of response appender.  At the end of
	 * every AjaxResponse, the AjaxResponseAppenders are given an opportunity to
	 * tag along. For instance, if you have an area at the top of your pages that
	 * show errors or notifications, you may want all of your ajax responses to have
	 * a chance to trigger an update of this area, so you could register an 
	 * {@link er.ajax.AjaxResponseAppender} that renders a javascript block that calls
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
		super(context);
		_request = request;
		_context = context;
	}

	@SuppressWarnings("cast")
	public WOResponse generateResponse() {
		if (AjaxUpdateContainer.hasUpdateContainerID(_request)) {
			String originalSenderID = _context.senderID();
			_context._setSenderID("");
			try {
				CharSequence originalContent = _content;
				_content = new StringBuilder();
				NSMutableDictionary userInfo = ERXWOContext.contextDictionary();
				userInfo.setObjectForKey(Boolean.TRUE, AjaxResponse.AJAX_UPDATE_PASS);
				WOApplication.application().invokeAction(_request, _context);
				_content.append(originalContent);
				if (_responseAppenders != null) {
					Enumeration responseAppendersEnum = _responseAppenders.objectEnumerator();
					while (responseAppendersEnum.hasMoreElements()) {
						AjaxResponseAppender responseAppender = (AjaxResponseAppender) responseAppendersEnum.nextElement();
						responseAppender.appendToResponse(this, _context);
					}
				}
				
				int length = ((CharSequence)_content).length();
				if (length == 0) {
					setStatus(HTTP_STATUS_INTERNAL_ERROR);
					log.warn("You performed an Ajax update, but no response was generated. A common cause of this is that you spelled your updateContainerID wrong.  You specified a container ID '" + AjaxUpdateContainer.updateContainerID(_request) + "'."); 
				}
				
			}
			finally {
				_context._setSenderID(originalSenderID);
			}
		} else
		{
			List<String> updateContainerIDList = AjaxUpdateContainer.updateContainerIDList(_context.request());

			if(updateContainerIDList != null)
			{
				WOApplication.application().appendToResponse(this, _context);

				StringBuilder c2 = new StringBuilder();
				boolean firstUC = true;

				for(String id : updateContainerIDList)
				{
					NSRange r = AjaxUpdateContainer.rangeForContainerID(_request, id);

					if(r != null)
					{
						StringBuilder c = new StringBuilder(_content.substring(r.location(), r.location()+r.length()));
						fixLeadingWhiteSpaces(c);

						if(firstUC)
						{
							c2.append(c);
							firstUC = false;
						} else
						{
							c2.append("\n<script>\n");
							c2.append("Element.update('" + id + "'," + AjaxValue.javaScriptAndHTMLEscaped(c) + ");");
							c2.append("\n</script>\n");
						}
					}
				}

				if (((Object)_content) instanceof StringBuffer)
					ERXKeyValueCodingUtilities.takePrivateValueForKey(this, new StringBuffer(c2),  "_content");
				else
					ERXKeyValueCodingUtilities.takePrivateValueForKey(this, c2,  "_content");
			}
		}

		return this;
	}

	public int contentLength()
	{
		return _content.length();
	}
	
	public static boolean isAjaxUpdatePass(WORequest request) {
		return ERXWOContext.contextDictionary().valueForKey(AjaxResponse.AJAX_UPDATE_PASS) != null;
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
					String updateContainerID = request.stringFormValueForKey(ERXAjaxApplication.KEY_UPDATE_CONTAINER_ID);
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
	
	/**
	 * Convenience method that calls <code>AjaxUtils.appendScriptHeaderIfNecessary</code> with this request. 
	 * @see er.ajax.AjaxUtils#appendScriptHeaderIfNecessary(WORequest, WOResponse)
	 */
	public void appendScriptHeaderIfNecessary() {
		AjaxUtils.appendScriptHeaderIfNecessary(_request, this);
	}
	
	/**
	 * Convenience method that calls <code>AjaxUtils.appendScriptFooterIfNecessary</code> with this request. 
	 * @see er.ajax.AjaxUtils#appendScriptFooterIfNecessary(WORequest, WOResponse)
	 */
	public void appendScriptFooterIfNecessary() {
		AjaxUtils.appendScriptFooterIfNecessary(_request, this);
	}
	
	/**
	 * Convenience method that calls <code>AjaxUtils.updateDomElement</code> with this request. 
	 * @param id 
	 * @param value 
	 * @param numberFormat 
	 * @param dateFormat 
	 * @param valueWhenEmpty 
	 * @see AjaxUtils#updateDomElement
	 */
	public void updateDomElement(String id, Object value, String numberFormat, String dateFormat, String valueWhenEmpty) {
		AjaxUtils.updateDomElement(this, id, value, numberFormat, dateFormat, valueWhenEmpty);
	}

	/**
	 * Convenience method that calls <code>updateDomElement</code> with no formatters and no valueWhenEmpty string. 
	 * @param id 
	 * @param value 
	 */
	public void updateDomElement(String id, Object value) {
		updateDomElement(id, value, null, null, null);
	}
}
