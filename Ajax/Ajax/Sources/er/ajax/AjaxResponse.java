package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * AjaxResponse provides support for performing an AjaxUpdate in the same response
 * as an ajax action.
 * 
 * @author mschrag
 */
public class AjaxResponse extends WOResponse {
	public static final String AJAX_UPDATE_PASS = "_ajaxUpdatePass";
	private WORequest _request;
	private WOContext _context;

	public AjaxResponse(WORequest request, WOContext context) {
		_request = request;
		_context = context;
	}

	public WOResponse generateResponse() {
		if (_request.stringFormValueForKey(AjaxUpdateContainer.UPDATE_CONTAINER_ID_KEY) != null) {
			String originalSenderID = _context.senderID();
			_context._setSenderID("");
			try {
				StringBuffer content = _content;
				_content = new StringBuffer();
				NSMutableDictionary userInfo = AjaxUtils.mutableUserInfo(_request);
				userInfo.setObjectForKey(Boolean.TRUE, AjaxResponse.AJAX_UPDATE_PASS);
				WOActionResults woactionresults = WOApplication.application().invokeAction(_request, _context);
				_content.append(content);
			}
			finally {
				_context._setSenderID(originalSenderID);
			}
		}
		return this;
	}
}
