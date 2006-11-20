package er.ajax;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

public class AjaxUtils {
	private static String HTML_CLOSE_HEAD = System.getProperty("er.ajax.AJComponent.htmlCloseHead");

	/**
	 * Key that tells the session not to store the current page. Checks both the response userInfo and the response
	 * headers if this key is present. The value doesn't matter, but you need to update the corresponding value in
	 * ERXSession. This is to keep the dependencies between the two frameworks independent.
	 */
	public static final String DONT_STORE_PAGE = "ERXSession.DontStorePage";

	/*
	 * Key that is used to specify that a page should go in the replacement cache instead of the backtrack cache. This
	 * is used for Ajax components that actually generate component actions in their output. The value doesn't matter,
	 * but you need to update the corresponding value in ERXSession. This is to keep the dependencies between the two
	 * frameworks independent.
	 */
	public static final String PAGE_REPLACEMENT_CACHE_LOOKUP_KEY = "pageCacheKey";

	/*
	 * Key that is used during an Ajax form posting so that WOContext gets _wasFormSubmitted set to true. If this value
	 * is changed, you must also change ERXWOForm.
	 */
	public static final String FORCE_FORM_SUBMITTED_KEY = "_forceFormSubmitted";

	public static void setPageReplacementCacheKey(WOContext _context, String _key) {
		_context.response().setHeader(_key, AjaxUtils.PAGE_REPLACEMENT_CACHE_LOOKUP_KEY);
	}

	/**
	 * Creates a response for the given context (which can be null), sets the charset to UTF-8, the connection to
	 * keep-alive and flags it as a Ajax request by adding an AJAX_REQUEST_KEY header. You can check this header in the
	 * session to decide if you want to save the request or not.
	 * 
	 * @param context
	 * @return
	 */
	public static AjaxResponse createResponse(WORequest request, WOContext context) {
		WOApplication app = WOApplication.application();
		AjaxResponse response = null;
		if (context != null) {
			WOResponse existingResponse = context.response();
			if (existingResponse instanceof AjaxResponse) {
				response = (AjaxResponse) existingResponse;
			}
			else {
				response = new AjaxResponse(request, context);
				response.setHeaders(existingResponse.headers());
				response.setUserInfo(existingResponse.userInfo());
				response.appendContentString(existingResponse.contentString());
			}
		}
		if (response == null) {
			response = new AjaxResponse(request, context);
		}
		if (context != null) {
			context._setResponse(response);
		}
		// Encode using UTF-8, although We are actually ASCII clean as all
		// unicode data is JSON escaped using backslash u. This is less data
		// efficient for foreign character sets but it is needed to support
		// naughty browsers such as Konqueror and Safari which do not honour the
		// charset set in the response
		response.setHeader("text/plain; charset=utf-8", "content-type");
		response.setHeader("Connection", "keep-alive");
		response.setHeader(AjaxUtils.DONT_STORE_PAGE, AjaxUtils.DONT_STORE_PAGE);
		return response;
	}

	/**
	 * Returns the userInfo dictionary if the supplied message and replaces it with a mutable version if it isn't
	 * already one.
	 * 
	 * @param message
	 * @return
	 */
	public static NSMutableDictionary mutableUserInfo(WOMessage message) {
		NSDictionary dict = message.userInfo();
		NSMutableDictionary result = null;
		if (dict == null) {
			result = new NSMutableDictionary();
			message.setUserInfo(result);
		}
		else {
			if (dict instanceof NSMutableDictionary) {
				result = (NSMutableDictionary) dict;
			}
			else {
				result = dict.mutableClone();
				message.setUserInfo(result);
			}
		}
		return result;
	}

	public static String htmlCloseHead() {
		String head = AjaxUtils.HTML_CLOSE_HEAD;
		return (head == null ? "</head>" : head);
	}

	/**
	 * Utility to add the given text before the given tag. Used to add stuff in the HEAD.
	 * 
	 * @param response
	 * @param content
	 * @param tag
	 */
	public static void insertInResponseBeforeTag(WOResponse response, String content, String tag) {
		String stream = response.contentString();
		int idx = stream.indexOf(tag);
		if (idx < 0) {
			idx = stream.toLowerCase().indexOf(tag.toLowerCase());
		}
		if (idx >= 0) {
			String pre = stream.substring(0, idx);
			String post = stream.substring(idx, stream.length());
			response.setContent(pre + content + post);
		}
	}

	/**
	 * Adds a script tag with a correct resource url in the html head tag if it isn't already present in the response.
	 * 
	 * @param response
	 * @param fileName
	 */
	public static void addScriptResourceInHead(WOContext context, WOResponse response, String framework, String fileName) {
		String startTag = "<script type=\"text/javascript\" src=\"";
		String endTag = "\"></script>";
		addResourceInHead(context, response, framework, fileName, startTag, endTag);
	}

	public static void addScriptResourceInHead(WOContext context, WOResponse response, String fileName) {
		addScriptResourceInHead(context, response, "Ajax", fileName);
	}

	/**
	 * Adds a stylesheet link tag with a correct resource url in the html head tag if it isn't already present in the
	 * response.
	 * 
	 * @param response
	 * @param fileName
	 */
	public static void addStylesheetResourceInHead(WOContext context, WOResponse response, String framework, String fileName) {
		String startTag = "<link rel = \"stylesheet\" type = \"text/css\" href = \"";
		String endTag = "\"/>";
		addResourceInHead(context, response, framework, fileName, startTag, endTag);
	}

	public static void addStylesheetResourceInHead(WOContext context, WOResponse response, String fileName) {
		addStylesheetResourceInHead(context, response, "Ajax", fileName);
	}

	/**
	 * Adds a reference to an arbitrary file with a correct resource url wrapped between startTag and endTag in the html
	 * head tag if it isn't already present in the response.
	 * 
	 * @param response
	 * @param fileName
	 * @param startTag
	 * @param endTag
	 */
	public static void addResourceInHead(WOContext context, WOResponse response, String framework, String fileName, String startTag, String endTag) {
		NSMutableDictionary userInfo = AjaxUtils.mutableUserInfo(context.response());
		if (userInfo.objectForKey(fileName) == null) {
			userInfo.setObjectForKey(fileName, fileName);
			WOResourceManager rm = WOApplication.application().resourceManager();
			String url = rm.urlForResourceNamed(fileName, framework, context.session().languages(), context.request());
			String html = startTag + url + endTag + "\n";
			AjaxUtils.insertInResponseBeforeTag(response, html, AjaxUtils.htmlCloseHead());
		}
	}

	/**
	 * Adds javascript code in a script tag in the html head tag.
	 * 
	 * @param response
	 * @param script
	 */
	public static void addScriptCodeInHead(WOResponse response, String script) {
		String js = "<script type=\"text/javascript\">\n" + script + "\n</script>";
		AjaxUtils.insertInResponseBeforeTag(response, js, AjaxUtils.htmlCloseHead());
	}

	public static String toSafeElementID(String elementID) {
		return "wo_" + elementID.replace('.', '_');
	}

	public static boolean shouldHandleRequest(WORequest request, WOContext context, String containerID) {
		String elementID = context.elementID();
		String senderID = context.senderID();
		String updateContainerID = null;
		if (containerID != null) {
			updateContainerID = AjaxUpdateContainer.updateContainerID(request);
		}
		boolean shouldHandleRequest = elementID != null && (elementID.equals(senderID) || (containerID != null && containerID.equals(updateContainerID)));
		return shouldHandleRequest;
	}

	public static void updateMutableUserInfoWithAjaxInfo(WOContext context) {
		AjaxUtils.updateMutableUserInfoWithAjaxInfo(context.response());
	}

	public static void updateMutableUserInfoWithAjaxInfo(WOMessage message) {
		NSMutableDictionary dict = AjaxUtils.mutableUserInfo(message);
		dict.takeValueForKey(AjaxUtils.DONT_STORE_PAGE, AjaxUtils.DONT_STORE_PAGE);
	}

	public static void appendScriptHeader(WOResponse response) {
		response.appendContentString("<script type = \"text/javascript\" language = \"javascript\">\n");
	}

	public static void appendScriptFooter(WOResponse response) {
		response.appendContentString("\n</script>");
	}

	public static Object valueForBinding(String name, Object defaultValue, NSDictionary associations, WOComponent component) {
		Object value = AjaxUtils.valueForBinding(name, associations, component);
		if (value != null) {
			return value;
		}
		return defaultValue;
	}

	public static String stringValueForBinding(String name, NSDictionary associations, WOComponent component) {
		WOAssociation association = (WOAssociation) associations.objectForKey(name);
		if (association != null) {
			return (String) association.valueInComponent(component);
		}
		return null;
	}

	public static Object valueForBinding(String name, NSDictionary associations, WOComponent component) {
		WOAssociation association = (WOAssociation) associations.objectForKey(name);
		if (association != null) {
			return association.valueInComponent(component);
		}
		return null;
	}

	public static boolean booleanValueForBinding(String name, boolean defaultValue, NSDictionary associations, WOComponent component) {
		WOAssociation association = (WOAssociation) associations.objectForKey(name);
		if (association != null) {
			return association.booleanValueInComponent(component);
		}
		return defaultValue;
	}

	public static void setValueForBinding(Object value, String name, NSDictionary associations, WOComponent component) {
		WOAssociation association = (WOAssociation) associations.objectForKey(name);
		if (association != null) {
			association.setValue(value, component);
		}
	}

	/**
	 * Returns an Ajax component action url. Using an ajax component action urls guarantees that caching during your
	 * ajax request will be handled appropriately.
	 * 
	 * @param context
	 *            the context of the request
	 * @return an ajax request url.
	 */
	public static String ajaxComponentActionUrl(WOContext context) {
		String actionUrl = context.componentActionURL();
		if (AjaxRequestHandler.useAjaxRequestHandler()) {
			actionUrl = actionUrl.replaceFirst("/" + WOApplication.application().componentRequestHandlerKey() + "/", "/" + AjaxRequestHandler.AjaxRequestHandlerKey + "/");
		}
		return actionUrl;
	}
}
