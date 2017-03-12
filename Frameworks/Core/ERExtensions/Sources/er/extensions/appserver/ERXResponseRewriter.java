package er.extensions.appserver;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.components.ERXStyleSheet;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXSelectorUtilities;
import er.extensions.foundation.ERXStringUtilities;

/**
 * ERXResponseRewriter provides several utilities for manipulating a WOResponse
 * after it has already been "drawn" by previous components.
 * 
 * @author mschrag
 * @property er.extensions.loadOnDemand if <code>true</code>, javascript files included in Ajax responses will be loaded on-demand (defaults to <code>true</code>) 
 * @property er.ajax.secureResources if <code>true</code>, load all resources with https (default false) 
 * @property er.ajax.AJComponent.htmlCloseHead the tag to insert in front of (defaults to &lt;/head&gt;)
 * @property er.extensions.ERXResponseRewriter.javascriptTypeAttribute if <code>true</code>, <i>type="text/javascript"</i>
 *           will be added to injected script tags (defaults <code>false</code>). For valid HTML you have to set this to
 *           <code>true</code> for HTML4 and XHTML but HTML5 will default to <i>text/javascript</i> if that attribute
 *           is missing.
 */
public class ERXResponseRewriter {
	private static final Logger log = LoggerFactory.getLogger(ERXResponseRewriter.class);

	private static final String ADDED_RESOURCES_KEY = "ERXResponseRewriter.addedResources";

	private static final String PENDING_INSERTS_KEY = "ERXResponseRewriter.pendingInserts";

	private static final String SECURE_RESOURCES_KEY = "er.ajax.secureResources";

	private static final String ORIGINAL_CONTEXT_ID_KEY = "_originalContextID";

	private static final String TOP_INDEX_KEY = "ERXResponseRewriter.topIndex";

	private static final String CONTEXT_OBSERVER_KEY = "ERXResponseRewriter.contextObserver";

	private static Map<WOComponent, NSMutableDictionary<String, Object>> _ajaxPageUserInfos;

	private static Map<WOComponent, NSMutableDictionary<String, Object>> _pageUserInfos;

	private static Delegate _delagate;
	
	/**
	 * Represents a resource in a framework, or a fully-qualified URL if
	 * fileName starts with a / or contains :// .
	 * 
	 * @author mschrag
	 */
	public static class Resource {
		private String _framework;
		private String _fileName;

		public Resource(String url) {
			_framework = null;
			_fileName = url;
		}

		public Resource(String framework, String fileName) {
			_framework = framework;
			_fileName = fileName;
		}

		public String framework() {
			return _framework;
		}

		public String fileName() {
			return _fileName;
		}

		@Override
		public String toString() {
			return "[Resource: framework = " + _framework + "; name = " + _fileName + "]";
		}
	}
	
	/**
	 * ERXResponseRewriter uses the ContextObserver to reset the topIndex value at the end 
	 * of the request. You should not need to invoke this class directly.
	 */
	public static class ContextObserver {
		public void didHandleRequest(NSNotification n) {
			WOContext context = (WOContext)n.object();
			NSMutableDictionary<String, Object> pageInfo = ERXResponseRewriter.ajaxPageUserInfo(context);
			pageInfo.removeObjectForKey(TOP_INDEX_KEY);
			pageInfo.removeObjectForKey(CONTEXT_OBSERVER_KEY);
			NSNotificationCenter.defaultCenter().removeObserver(this, WORequestHandler.DidHandleRequestNotification, context);
		}
	}

	/**
	 * The delegate that is called prior to adding resources into the page,
	 * which gives you a chance to deny the addition, or rewrite the addition to
	 * a custom resource.
	 * 
	 * @author mschrag
	 */
	public static interface Delegate {
		/**
		 * Called prior to adding resources at all. Returning false will skip
		 * the addition completely.
		 * 
		 * @param framework
		 *            the requested framework of the addition (can be <code>null</code>)
		 * @param fileName
		 *            the requested fileName of the addition (can be a URL,
		 *            absolute path, or relative resource path)
		 * @return <code>true</code> if the resource should be added
		 */
		public boolean responseRewriterShouldAddResource(String framework, String fileName);

		/**
		 * Provides the ability to override the requested framework and fileName
		 * with a custom alternative. For example, if you want to replace all
		 * "Ajax" "prototype.js" imports, you can provide your own alternative
		 * "app" "prototype.js".
		 * 
		 * @param framework
		 *            the requested framework of the addition (can be <code>null</code>)
		 * @param fileName
		 *            the requested fileName of the addition (can be a URL,
		 *            absolute path, or relative resource path)
		 * @return an alternative Resource, or <code>null</code> to use the requested
		 *         resource
		 */
		public ERXResponseRewriter.Resource responseRewriterWillAddResource(String framework, String fileName);
	}

	/**
	 * TagMissingBehavior specifies several ways the response rewriter should
	 * handle the case of having a missing tag that you attempted to insert in
	 * front of (for instance, if you ask to insert in the head tag and the head
	 * tag does not exist).
	 * 
	 * @author mschrag
	 */
	public static enum TagMissingBehavior {
		/**
		 * Top tries to behave like head-insertion by maintaining the same
		 * ordering as head would (first added, first printed). If an Ajax
		 * response, top would push to the front of the response.
		 */
		Top,

		/**
		 * Inline just renders the content at the current location in the
		 * response.
		 */
		Inline,

		/**
		 * Skip does not render the content at all and silently ignores the
		 * missing tag.
		 */
		Skip,

		/**
		 * Like skip, no content will be rendered into the response, but a
		 * warning will be printed onto the console.
		 */
		SkipAndWarn
	}

	static {
		ERXResponseRewriter._pageUserInfos = Collections.synchronizedMap(new WeakHashMap<WOComponent, NSMutableDictionary<String, Object>>());
		ERXResponseRewriter._ajaxPageUserInfos = Collections.synchronizedMap(new WeakHashMap<WOComponent, NSMutableDictionary<String, Object>>());
	}

	/**
	 * Sets the response rewriter delegate to be used by this Application.
	 * 
	 * @param delegate
	 *            the response rewriter delegate to be used by this Application,
	 *            or <code>null</code> to use the default
	 */
	public static void setDelegate(ERXResponseRewriter.Delegate delegate) {
		ERXResponseRewriter._delagate = delegate;
	}

	/**
	 * Returns the page userInfo for the page component of the given context. If
	 * this is the first request for the page user info for a non-ajax request,
	 * the user info will be cleared (so that reloading a page doesn't make the
	 * system believe it has already rendered script and CSS tags, for
	 * instance). If you do not want this behavior, use pageUserInfo(WOContext)
	 * instead.
	 * 
	 * @param context
	 *            the context to lookup
	 * @return the user info for the page component of the given context
	 */
	public static NSMutableDictionary<String, Object> ajaxPageUserInfo(WOContext context) {
		WOComponent page = context.page();
		ERXSession session = ERXSession.session();
		boolean sessionStoresPageInfo = session != null && session.storesPageInfo();
		@SuppressWarnings("null")
		Map<WOComponent, NSMutableDictionary<String, Object>> pageInfoDict =
				sessionStoresPageInfo ? session.pageInfoDictionary() : ERXResponseRewriter._ajaxPageUserInfos;
		NSMutableDictionary<String, Object> pageInfo = pageInfoDict.get(page);
		String contextID = context.contextID();
		if (contextID == null) {
			contextID = "none";
		}
		if (pageInfo != null && !ERXAjaxApplication.isAjaxRequest(context.request()) && !contextID.equals(pageInfo.objectForKey(ERXResponseRewriter.ORIGINAL_CONTEXT_ID_KEY))) {
			pageInfo = null;
		}
		if (pageInfo == null) {
			pageInfo = new NSMutableDictionary<>();
			pageInfo.setObjectForKey(contextID, ERXResponseRewriter.ORIGINAL_CONTEXT_ID_KEY);
			pageInfoDict.put(page, pageInfo);
		}
		return pageInfo;
	}

	/**
	 * Returns the page userInfo for the page component of the given context.
	 * Unlike ajaxPageUserInfo, information put into pageUserInfo will stay
	 * associated with the page as long as the page exists.
	 * 
	 * @param context
	 *            the context to lookup
	 * @return the user info for the page component of the given context
	 */
	public static NSMutableDictionary<String, Object> pageUserInfo(WOContext context) {
		return pageUserInfo(context.page());
	}

	/**
	 * Returns the page userInfo for the given page component.
	 * Unlike ajaxPageUserInfo, information put into pageUserInfo will stay
	 * associated with the page as long as the page exists.
	 * 
	 * @param page
	 *            the component to lookup
	 * @return the user info for the page component of the given context
	 */
	public static NSMutableDictionary<String, Object> pageUserInfo(WOComponent page) {
		NSMutableDictionary<String, Object> pageInfo = ERXResponseRewriter._pageUserInfos.get(page);
		if (pageInfo == null) {
			pageInfo = new NSMutableDictionary<>();
			ERXResponseRewriter._pageUserInfos.put(page, pageInfo);
		}
		return pageInfo;
	}

	/**
	 * Returns the tag name that scripts and resources should be inserted above.
	 * Defaults to &lt;/head&gt;, but this can be overridden by setting the
	 * property er.ajax.AJComponent.htmlCloseHead.
	 * 
	 * @return string that closes the part where resources are inserted into
	 */
	public static String _htmlCloseHeadTag() {
		String closeHeadTag = ERXProperties.stringForKeyWithDefault("er.ajax.AJComponent.htmlCloseHead", "</head>");
		return closeHeadTag;
	}

	/**
	 * Utility to add the given content into the response before the close of
	 * the head tag.
	 * 
	 * @param response
	 *            the WOResponse
	 * @param context
	 *            the WOContext
	 * @param content
	 *            the content to insert.
	 * @param tagMissingBehavior
	 *            how to handle the case where the tag is missing
	 * @return whether or not the content was inserted
	 */
	public static boolean insertInResponseBeforeHead(WOResponse response, WOContext context, String content, TagMissingBehavior tagMissingBehavior) {
		return ERXResponseRewriter.insertInResponseBeforeTag(response, context, content, ERXResponseRewriter._htmlCloseHeadTag(), tagMissingBehavior);
	}

	/**
	 * Replaces all occurrences of the given pattern in the response with the replacement string.
	 * 
	 * @param response the response
	 * @param context the context
	 * @param pattern the pattern to match
	 * @param replacement the replacement value
	 */
	public static void replaceAllInResponse(WOResponse response, WOContext context, Pattern pattern, String replacement) {
		String responseContent = response.contentString();
		if (responseContent != null) {
			String responseReplaced = pattern.matcher(responseContent).replaceAll(replacement);
			response.setContent(responseReplaced);
		}
	}

	/**
	 * Replaces the first occurrence of the given pattern in the response with the replacement string.
	 * 
	 * @param response the response
	 * @param context the context
	 * @param pattern the pattern to match
	 * @param replacement the replacement value
	 */
	public static void replaceFirstInResponse(WOResponse response, WOContext context, Pattern pattern, String replacement) {
		String responseContent = response.contentString();
		if (responseContent != null) {
			String responseReplaced = pattern.matcher(responseContent).replaceFirst(replacement);
			response.setContent(responseReplaced);
		}
	}
	
	/**
	 * Utility to add the given content into the response before a particular
	 * HTML tag.
	 * 
	 * @param response
	 *            the response
	 * @param context
	 *            the context
	 * @param content
	 *            the content to insert
	 * @param tag
	 *            the tag to insert before (in HTML syntax)
	 * @param tagMissingBehavior
	 *            how to handle the case where the tag is missing
	 * @return whether or not the content was inserted
	 */
	public static boolean insertInResponseBeforeTag(WOResponse response, WOContext context, String content, String tag, TagMissingBehavior tagMissingBehavior) {
		boolean inserted = false;
		String responseContent = response.contentString();
		int tagIndex;
		if (tag != null) {
			tagIndex = responseContent.indexOf(tag);
			if (tagIndex < 0) {
				tagIndex = responseContent.toLowerCase().indexOf(tag.toLowerCase());
			}
		}
		else {
			tagIndex = -1;
		}
		if (tagIndex >= 0) {
			int insertIndex = tagIndex;
			if (content.toLowerCase().startsWith("<link") || content.toLowerCase().startsWith("<style")) {
				int scriptIndex = responseContent.toLowerCase().indexOf("<script");
				if (scriptIndex > 0 && scriptIndex < insertIndex) {
					insertIndex = scriptIndex;
				}
			}
			response.setContent(ERXStringUtilities.insertString(responseContent, content, insertIndex));
			inserted = true;
		}
		else if (tagMissingBehavior == TagMissingBehavior.Inline) {
			response.appendContentString(content);
			inserted = true;
		}
		else if (tagMissingBehavior == TagMissingBehavior.Top) {
			NSMutableDictionary<String, Object> pageInfo = ERXResponseRewriter.ajaxPageUserInfo(context);
			Integer topIndex = (Integer) pageInfo.objectForKey(ERXResponseRewriter.TOP_INDEX_KEY);
			if (topIndex == null) {
				topIndex = Integer.valueOf(0);
				//Create an observer to reset the topIndex at the end of the request
				ContextObserver contextObserver = new ContextObserver();
				NSNotificationCenter.defaultCenter().addObserver(
						contextObserver, 
						ERXSelectorUtilities.notificationSelector("didHandleRequest"), 
						WORequestHandler.DidHandleRequestNotification, 
						context);
				//Stick the observer in the pageInfo dictionary so it isn't garbage collected
				pageInfo.setObjectForKey(contextObserver, CONTEXT_OBSERVER_KEY);
			}
			response.setContent(ERXStringUtilities.insertString(responseContent, content, topIndex));
			pageInfo.setObjectForKey(Integer.valueOf(topIndex.intValue() + content.length()), ERXResponseRewriter.TOP_INDEX_KEY);
			inserted = true;
		}
		else if (tagMissingBehavior == TagMissingBehavior.Skip) {
			// IGNORE
		}
		else if (tagMissingBehavior == TagMissingBehavior.SkipAndWarn) {
			log.warn("There was no {}, so your content did not get added: {}", tag, content);
		}
		else {
			throw new IllegalArgumentException("Unknown tag missing missing: " + tagMissingBehavior + ".");
		}
		return inserted;
	}
	
	/**
	 * Adds a script tag with a correct resource URL into the HTML head tag if
	 * it isn't already present in the response, or inserts an Ajax OnDemand tag
	 * if the current request is an Ajax request.
	 * 
	 * @param response
	 *            the response
	 * @param context
	 *            the context
	 * @param framework
	 *            the framework that contains the file
	 * @param fileName
	 *            the name of the javascript file to add
	 */
	public static void addScriptResourceInHead(WOResponse response, WOContext context, String framework, String fileName) {
		boolean appendTypeAttribute = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXResponseRewriter.javascriptTypeAttribute", false);
		String scriptStartTag;
		if (appendTypeAttribute) {
			scriptStartTag = "<script type=\"text/javascript\" src=\"";
		}
		else {
			scriptStartTag = "<script src=\"";
		}
		String scriptEndTag = "\"></script>";
		String fallbackStartTag;
		String fallbackEndTag;
		if (ERXAjaxApplication.isAjaxRequest(context.request()) && ERXProperties.booleanForKeyWithDefault("er.extensions.loadOnDemand", true)) {
			if (!ERXAjaxApplication.isAjaxReplacement(context.request()) || ERXProperties.booleanForKeyWithDefault("er.extensions.loadOnDemandDuringReplace", false)) {
				if (appendTypeAttribute) {
					fallbackStartTag = "<script type=\"text/javascript\">AOD.loadScript('";
				}
				else {
					fallbackStartTag = "<script>AOD.loadScript('";
				}
				fallbackEndTag = "')</script>";
			}
			else {
				fallbackStartTag = null;
				fallbackEndTag = null;
			}
		}
		else {
			fallbackStartTag = null;
			fallbackEndTag = null;
		}
		ERXResponseRewriter.addResourceInHead(response, context, framework, fileName, scriptStartTag, scriptEndTag, fallbackStartTag, fallbackEndTag, TagMissingBehavior.Inline);
	}

	/**
	 * Adds a stylesheet link tag with a correct resource URL in the HTML head
	 * tag if it isn't already present in the response.
	 * 
	 * @param context
	 *            the context
	 * @param response
	 *            the response
	 * @param framework
	 *            the framework that contains the file
	 * @param fileName
	 *            the name of the CSS file to add
	 */
	public static void addStylesheetResourceInHead(WOResponse response, WOContext context, String framework, String fileName) {
		ERXResponseRewriter.addStylesheetResourceInHead(response, context, framework, fileName, null);
	}

	/**
	 * Adds a stylesheet link tag with a correct resource URL in the HTML head
	 * tag if it isn't already present in the response.
	 * 
	 * @param context
	 *            the context
	 * @param response
	 *            the response
	 * @param framework
	 *            the framework that contains the file
	 * @param fileName
	 *            the name of the CSS file to add
	 * @param media
	 *            the media type of the stylesheet (or <code>null</code> for default)
	 */
	public static void addStylesheetResourceInHead(WOResponse response, WOContext context, String framework, String fileName, String media) {
		String cssStartTag;
		if (media == null) {
			cssStartTag = "<link rel=\"stylesheet\" type=\"text/css\" href=\"";
		}
		else {
			cssStartTag = "<link rel=\"stylesheet\" type=\"text/css\" media=\"" + media + "\" href=\"";
		}
		String cssEndTag;
		if (ERXStyleSheet.shouldCloseLinkTags()) {
			cssEndTag = "\"/>";
		}
		else {
			cssEndTag = "\">";
		}
		String fallbackStartTag = null;
		String fallbackEndTag = null;

		if (ERXAjaxApplication.isAjaxRequest(context.request()) && ERXProperties.booleanForKeyWithDefault("er.extensions.loadOnDemand", true)) {
			if (ERXProperties.booleanForKeyWithDefault("er.extensions.loadOnDemandDuringReplace", false)) {
				boolean appendTypeAttribute = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXResponseRewriter.javascriptTypeAttribute", false);
				fallbackStartTag = (appendTypeAttribute ? "<script type=\"text/javascript\">AOD.loadCSS('" : "<script>AOD.loadCSS('");
				fallbackEndTag = "')</script>";
			}
		}
		ERXResponseRewriter.addResourceInHead(response, context, framework, fileName, cssStartTag, cssEndTag, fallbackStartTag, fallbackEndTag, TagMissingBehavior.Inline);
		
		// Q: We use TagMissingBehaviour.Inline in case this is called from inside the 
		// HEAD tag and there is no close tag yet
//		ERXResponseRewriter.addResourceInHead(response, context, framework, fileName, cssStartTag, cssEndTag, null, null, TagMissingBehavior.Inline);
	}

	/**
	 * Adds javascript code in a script tag in the HTML head tag (without a
	 * name). If you call this method multiple times with the same script code,
	 * it will add multiple times. To prevent this, call
	 * addScriptCodeInHead(WOResponse, String, String) passing in a name for
	 * your script.
	 * 
	 * @param response
	 *            the response to write into
	 * @param context
	 *            the context
	 * @param script
	 *            the javascript code to insert
	 */
	public static void addScriptCodeInHead(WOResponse response, WOContext context, String script) {
		ERXResponseRewriter.addScriptCodeInHead(response, context, script, null);
	}

	/**
	 * Adds javascript code in a script tag in the HTML head tag or inline if
	 * the request is an Ajax request.
	 * 
	 * @param response
	 *            the response to write into
	 * @param context
	 *            the context
	 * @param script
	 *            the javascript code to insert
	 * @param scriptName
	 *            the name of the script to insert (for duplicate checking)
	 */
	public static void addScriptCodeInHead(WOResponse response, WOContext context, String script, String scriptName) {
		if (scriptName == null || !ERXResponseRewriter.isResourceAddedToHead(context, null, scriptName)) {
			String js = "<script type=\"text/javascript\">\n" + script + "\n</script>";
			boolean inserted = ERXResponseRewriter.insertInResponseBeforeHead(response, context, js, TagMissingBehavior.Top);
			if (inserted && scriptName != null) {
				ERXResponseRewriter.resourceAddedToHead(context, null, scriptName);
			}
		}
	}

	/**
	 * Adds a reference to an arbitrary file with a correct resource URL wrapped
	 * between startTag and endTag in the HTML head tag if it isn't already
	 * present in the response.
	 * 
	 * @param response
	 *            the response
	 * @param context
	 *            the context
	 * @param framework
	 *            the framework that contains the file
	 * @param fileName
	 *            the name of the file to add
	 * @param startTag
	 *            the HTML to prepend before the URL
	 * @param endTag
	 *            the HTML to append after the URL
	 */
	public static void addResourceInHead(WOResponse response, WOContext context, String framework, String fileName, String startTag, String endTag) {
		ERXResponseRewriter.addResourceInHead(response, context, framework, fileName, startTag, endTag, TagMissingBehavior.Skip);
	}

	/**
	 * Returns the resources that have been added to the head of this page.
	 * 
	 * @param context
	 *            the context
	 * @return the resources that have been added to the head of this page.
	 */
	@SuppressWarnings("unchecked")
	public static NSMutableSet<String> resourcesAddedToHead(WOContext context) {
		NSMutableDictionary<String, Object> userInfo = ERXResponseRewriter.ajaxPageUserInfo(context);
		NSMutableSet<String> addedResources = (NSMutableSet<String>) userInfo.objectForKey(ERXResponseRewriter.ADDED_RESOURCES_KEY);
		if (addedResources == null) {
			addedResources = new NSMutableSet<>();
			userInfo.setObjectForKey(addedResources, ERXResponseRewriter.ADDED_RESOURCES_KEY);
		}
		return addedResources;
	}

	/**
	 * Returns whether or not the given resource has been added to the HEAD tag.
	 * 
	 * @param context
	 *            the context
	 * @param frameworkName
	 *            the framework name of the resource
	 * @param resourceName
	 *            the name of the resource to check
	 * @return <code>true</code> if the resource has been added to head
	 */
	public static boolean isResourceAddedToHead(WOContext context, String frameworkName, String resourceName) {
		NSMutableSet<String> addedResources = ERXResponseRewriter.resourcesAddedToHead(context);
		return addedResources.containsObject(frameworkName + "." + resourceName);
	}

	/**
	 * Records that the given resource (within the given framework) has been
	 * added to the head of this page.
	 * 
	 * @param context
	 *            the context
	 * @param frameworkName
	 *            the framework name of the resource
	 * @param resourceName
	 *            the name of the resource
	 */
	public static void resourceAddedToHead(WOContext context, String frameworkName, String resourceName) {
		NSMutableSet<String> addedResources = ERXResponseRewriter.resourcesAddedToHead(context);
		addedResources.addObject(frameworkName + "." + resourceName);
	}

	/**
	 * Adds a reference to an arbitrary file with a correct resource URL wrapped
	 * between startTag and endTag in the HTML head tag if it isn't already
	 * present in the response.
	 * 
	 * @param response
	 *            the response
	 * @param context
	 *            the context
	 * @param framework
	 *            the framework that contains the file
	 * @param fileName
	 *            the name of the file to add
	 * @param startTag
	 *            the HTML to prepend before the URL
	 * @param endTag
	 *            the HTML to append after the URL
	 * @param tagMissingBehavior
	 *            how to handle the case where the tag is missing
	 * 
	 * @return whether or not the content was added
	 */
	public static boolean addResourceInHead(WOResponse response, WOContext context, String framework, String fileName, String startTag, String endTag, TagMissingBehavior tagMissingBehavior) {
		return ERXResponseRewriter.addResourceInHead(response, context, framework, fileName, startTag, endTag, null, null, tagMissingBehavior);
	}

	/**
	 * Adds a reference to an arbitrary file with a correct resource URL wrapped
	 * between startTag and endTag in the HTML head tag if it isn't already
	 * present in the response.
	 * 
	 * @param response
	 *            the response
	 * @param context
	 *            the context
	 * @param framework
	 *            the framework that contains the file
	 * @param fileName
	 *            the name of the file to add
	 * @param startTag
	 *            the HTML to prepend before the URL
	 * @param endTag
	 *            the HTML to append after the URL
	 * @param fallbackStartTag
	 * @param fallbackEndTag
	 * @param tagMissingBehavior
	 *            how to handle the case where the tag is missing
	 * 
	 * @return whether or not the content was added
	 */
	public static boolean addResourceInHead(WOResponse response, WOContext context, String framework, String fileName, String startTag, String endTag, String fallbackStartTag, String fallbackEndTag, TagMissingBehavior tagMissingBehavior) {
		boolean inserted = true;
		
		String replacementResourceStr = ERXProperties.stringForKey("er.extensions.ERXResponseRewriter.resource." + framework + "." + fileName);
		if (replacementResourceStr != null) {
			int dotIndex = replacementResourceStr.indexOf('.');
			framework = replacementResourceStr.substring(0, dotIndex);
			fileName = replacementResourceStr.substring(dotIndex + 1);
		}
		
		if (!ERXResponseRewriter.isResourceAddedToHead(context, framework, fileName) && (_delagate == null || _delagate.responseRewriterShouldAddResource(framework, fileName))) {
			boolean insert = true;
			
			if (_delagate != null) {
				Resource replacementResource = _delagate.responseRewriterWillAddResource(framework, fileName);
				if (replacementResource != null) {
					framework = replacementResource.framework();
					fileName = replacementResource.fileName();
					
					// double-check that the replacement hasn't already been added
					if (ERXResponseRewriter.isResourceAddedToHead(context, framework, fileName)) {
						insert = false;
					}
				}
			}

			if (insert) {
				String url;
				if (fileName.indexOf("://") != -1 || fileName.startsWith("/")) {
					url = fileName;
				}
				else {
					WOResourceManager rm = WOApplication.application().resourceManager();
					NSArray languages = null;
					if (context.hasSession()) {
						languages = context.session().languages();
					}
					url = rm.urlForResourceNamed(fileName, framework, languages, context.request());
					boolean generateCompleteResourceURLs = ERXResourceManager._shouldGenerateCompleteResourceURL(context);
					boolean secureAllResources = ERXProperties.booleanForKey(ERXResponseRewriter.SECURE_RESOURCES_KEY) && !ERXRequest.isRequestSecure(context.request());
					if (generateCompleteResourceURLs || secureAllResources) {
						url = ERXResourceManager._completeURLForResource(url, secureAllResources ? Boolean.TRUE : null, context);
					}
				}
				String html = startTag + url + endTag + "\n";
				if (fallbackStartTag == null && fallbackEndTag == null) {
					inserted = ERXResponseRewriter.insertInResponseBeforeHead(response, context, html, tagMissingBehavior);
				}
				else {
					inserted = ERXResponseRewriter.insertInResponseBeforeHead(response, context, html, TagMissingBehavior.Skip);
					if (!inserted) {
						String fallbackHtml = fallbackStartTag + url + fallbackEndTag + "\n";
						inserted = ERXResponseRewriter.insertInResponseBeforeTag(response, context, fallbackHtml, null, TagMissingBehavior.Top);
					}
				}
				if (inserted) {
					ERXResponseRewriter.resourceAddedToHead(context, framework, fileName);
				}
			}
		}
		return inserted;
	}
	
	/**
	 * Appends a script tag with or without type attribute depending on the
	 * corresponding property value.
	 * 
	 * @param response response object to add opening script tag to
	 */
	public static void appendScriptTagOpener(WOResponse response) {
		boolean appendTypeAttribute = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXResponseRewriter.javascriptTypeAttribute", false);
		if (appendTypeAttribute) {
			response.appendContentString("<script type=\"text/javascript\">");
		} else {
			response.appendContentString("<script>");
		}
	}
	
	/**
	 * Appends the closing script tag to the given response.
	 * 
	 * @param response response object to add closing script tag to
	 */
	public static void appendScriptTagCloser(WOResponse response) {
		response.appendContentString("</script>");
	}
}
