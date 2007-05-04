package er.ajax;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXAjaxSession;
import er.extensions.ERXResourceManager;
import er.extensions.ERXWOContext;

public class AjaxUtils {
	private static String HTML_CLOSE_HEAD = System.getProperty("er.ajax.AJComponent.htmlCloseHead");
	private static final String SECURE_RESOURCES_KEY = "er.ajax.secureResources";
	
	/*
	 * Key that is used during an Ajax form posting so that WOContext gets _wasFormSubmitted set to true. If this value
	 * is changed, you must also change ERXWOForm.
	 */
	public static final String FORCE_FORM_SUBMITTED_KEY = "_forceFormSubmitted";

	/**
	 * Return whether or not the given request is an Ajax request.
	 * @param request the request the check
	 */
	public static boolean isAjaxRequest(WORequest request) {
		String requestedWith = request.headerForKey("x-requested-with");
		return "XMLHttpRequest".equals(requestedWith);
	}
	
	public static void setPageReplacementCacheKey(WOContext _context, String _key) {
		_context.response().setHeader(_key, ERXAjaxSession.PAGE_REPLACEMENT_CACHE_LOOKUP_KEY);
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
		response.setHeader(ERXAjaxSession.DONT_STORE_PAGE, ERXAjaxSession.DONT_STORE_PAGE);
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
		return ERXWOContext.contextDictionary();
	}

	public static String htmlCloseHead() {
		return ERXWOContext.htmlCloseHead(AjaxUtils.HTML_CLOSE_HEAD);
	}

	/**
	 * Utility to add the given text before the given tag. Used to add stuff in the HEAD.
	 * 
	 * @param response
	 * @param content
	 * @param tag
	 */
	public static void insertInResponseBeforeTag(WOResponse response, String content, String tag, boolean ignoreIfTagMissing) {
		ERXWOContext.insertInResponseBeforeTag(response, content, tag, ignoreIfTagMissing);
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
		ERXWOContext.addResourceInHead(context, response, framework, fileName, startTag, endTag, true);
		
		// MS: OK ... Sheesh.  If you're not using Wonder's ERXResourceManager #1, you're a bad person, but #2 in development mode
		// you have a lame resource URL that does not act like a path (wr/wodata=/path/to/your/resource), rather it acts like a query string
		// (wr?wodata=/path/to/your/resource).  This means that relative resource references won't work and also only previously cached resources
		// will load (i.e. ones coming from something that made an explicit WOResourceURL, etc, reference).  This explodes when scriptaculous tries 
		// to load its required resources dynamically (like builder.js, effects.js, etc).
		//
		// So we have to check for this condition -- you asked to load scriptaculous.js from Ajax framework and you don't have ERXResourceManager
		// and you're in development mode (as far as your lame WOResourceManager is concerned), so we need to do Scriptaculous' job and manually
		// load the dependent js files on its behalf.  You really should just suck it up and use ERXResourceManager because it really is just
		// better.  But if you're holding out and scared like a child, then we'll do this for you. 
		if (!(WOApplication.application().resourceManager() instanceof ERXResourceManager) && "Ajax".equals(framework) && "scriptaculous.js".equals(fileName) && !(context.request() == null || context.request() != null && context.request().isUsingWebServer() && !WOApplication.application()._rapidTurnaroundActiveForAnyProject())) {
			ERXWOContext.addResourceInHead(context, response, framework, "builder.js", startTag, endTag, true);
			ERXWOContext.addResourceInHead(context, response, framework, "effects.js", startTag, endTag, true);
			ERXWOContext.addResourceInHead(context, response, framework, "dragdrop.js", startTag, endTag, true);
			ERXWOContext.addResourceInHead(context, response, framework, "controls.js", startTag, endTag, true);
			ERXWOContext.addResourceInHead(context, response, framework, "slider.js", startTag, endTag, true);
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
		AjaxUtils.insertInResponseBeforeTag(response, js, AjaxUtils.htmlCloseHead(), true);
	}

	public static String toSafeElementID(String elementID) {
		return "wo_" + elementID.replace('.', '_');
	}

	public static boolean shouldHandleRequest(WORequest request, WOContext context, String containerID) {
		String elementID = context.elementID();
		String senderID = context.senderID();
		String updateContainerID = null;
		if (containerID != null) {
			if (AjaxResponse.isAjaxUpdatePass(request)) {
				updateContainerID = AjaxUpdateContainer.updateContainerID(request);
			}
		}
		boolean shouldHandleRequest = elementID != null && (elementID.equals(senderID) || (containerID != null && containerID.equals(updateContainerID)));
		return shouldHandleRequest;
	}

	public static void updateMutableUserInfoWithAjaxInfo(WOContext context) {
		AjaxUtils.updateMutableUserInfoWithAjaxInfo(context.response());
	}

	public static void updateMutableUserInfoWithAjaxInfo(WOMessage message) {
		NSMutableDictionary dict = AjaxUtils.mutableUserInfo(message);
		dict.takeValueForKey(ERXAjaxSession.DONT_STORE_PAGE, ERXAjaxSession.DONT_STORE_PAGE);
	}

	public static void appendScriptHeaderIfNecessary(WORequest request, WOResponse response) {
		if (AjaxUpdateContainer.hasUpdateContainerID(request)) {
			AjaxUtils.appendScriptHeader(response);
		}
		else {
			response.setHeader("text/javascript", "Content-Type");
		}
	}

	public static void appendScriptHeader(WOResponse response) {
		response.appendContentString("<script>");
	}

	public static void appendScriptFooterIfNecessary(WORequest request, WOResponse response) {
		if (AjaxUpdateContainer.hasUpdateContainerID(request)) {
			AjaxUtils.appendScriptFooter(response);
		}
	}
	
	public static void appendScriptFooter(WOResponse response) {
		response.appendContentString("</script>");
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

	public static void appendTagAttributeAndValue(WOResponse response, WOContext context, WOComponent component, NSDictionary associations, String name) {
		AjaxUtils.appendTagAttributeAndValue(response, context, component, name, (WOAssociation)associations.objectForKey(name));
	}

	public static void appendTagAttributeAndValue(WOResponse response, WOContext context, WOComponent component, String name, WOAssociation association) {
		if (association != null) {
			String value = (String) association.valueInComponent(component);
			if (value != null) {
				response._appendTagAttributeAndValue(name, value, true);
			}
		}
	}

}
