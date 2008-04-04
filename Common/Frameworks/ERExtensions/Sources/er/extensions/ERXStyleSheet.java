/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSArray;

/**
 * Adds a style sheet to a page. You can either supply a complete URL, a file
 * and framework name or put something in the component content. The content of
 * the component is cached under a "key" binding and then delivered via a direct
 * action, so it doesn't need to get re-rendered too often.
 * 
 * @binding filename name of the style sheet
 * @binding framework name of the framework for the style sheet
 * @binding href url to the style sheet
 * @binding key key to cache the style sheet under when using the component
 *          content. Default is the sessionID. That means, you should *really*
 *          explicitly set a key, when you use more than one ERXStyleSheet using
 *          the component content method within one session
 * @binding inline when true, the generated link tag will be appended inline,
 *          when false it'll be placed in the head of the page, when unset it
 *          will be placed inline for ajax requests and in the head for regular
 *          requests
 * @property er.extensions.ERXStyleSheet.xhtml (defaults true) if false, link
 *           tags are not closed, which is compatible with older HTML
 */
// FIXME: cache should be able to cache on calues of bindings, not a single key
public class ERXStyleSheet extends ERXStatelessComponent {

	/** logging support */
	public static final Logger log = Logger.getLogger(ERXStyleSheet.class);

	/**
	 * Public constructor
	 * 
	 * @param aContext
	 *            a context
	 */
	public ERXStyleSheet(WOContext aContext) {
		super(aContext);
	}

	@SuppressWarnings("unchecked")
	private static ERXExpiringCache<String, WOResponse> cache(WOSession session) {
		ERXExpiringCache<String, WOResponse> cache = (ERXExpiringCache<String, WOResponse>) session.objectForKey("ERXStylesheet.cache");
		if (cache == null) {
			cache = new ERXExpiringCache<String, WOResponse>(60);
			cache.startBackgroundExpiration();
			session.setObjectForKey(cache, "ERXStylesheet.cache");
		}
		return cache;
	}

	public static class Sheet extends WODirectAction {
		public Sheet(WORequest worequest) {
			super(worequest);
		}

		@Override
		public WOActionResults performActionNamed(String name) {
			WOResponse response = ERXStyleSheet.cache(session()).objectForKey(name);
			return response;
		}
	}

	/**
	 * returns the complete url to the style sheet.
	 * 
	 * @return style sheet url
	 */
	public String styleSheetUrl() {
		String url = (String) valueForBinding("styleSheetUrl");
		url = (url == null ? (String) valueForBinding("href") : url);
		if (url == null) {
			String name = styleSheetName();
			if (name != null) {
				url = application().resourceManager().urlForResourceNamed(styleSheetName(), styleSheetFrameworkName(), languages(), context().request());
			}
		}
		return url;
	}

	/**
	 * Returns the style sheet framework name either resolved via the binding
	 * <b>framework</b>.
	 * 
	 * @return style sheet framework name
	 */
	public String styleSheetFrameworkName() {
		String result = (String) valueForBinding("styleSheetFrameworkName");
		result = (result == null ? (String) valueForBinding("framework") : result);
		return result;
	}

	/**
	 * Returns the style sheet name either resolved via the binding <b>filename</b>.
	 * 
	 * @return style sheet name
	 */
	public String styleSheetName() {
		String result = (String) valueForBinding("styleSheetName");
		result = (result == null ? (String) valueForBinding("filename") : result);
		return result;
	}

	/**
	 * Returns key under which the stylesheet should be placed in the cache. If
	 * no key is given, the session id is used.
	 * 
	 * @return style sheet framework name
	 */
	public String styleSheetKey() {
		String result = (String) valueForBinding("key");
		if (result == null) {
			result = context().session().sessionID();
		}
		return result;
	}

	/**
	 * Returns the media type for this stylesheet
	 */
	public String mediaType() {
		return stringValueForBinding("media");
	}

	/**
	 * Returns the languages for the request.
	 */
	private NSArray<String> languages() {
		if (hasSession())
			return session().languages();
		WORequest request = context().request();
		if (request != null)
			return request.browserLanguages();
		return null;
	}

	/**
	 * Appends the &ltlink&gt; tag, either by using the style sheet name and
	 * framework or by using the component content and then generating a link to
	 * it.
	 */
	@Override
	public void appendToResponse(WOResponse r, WOContext wocontext) {
		String href = styleSheetUrl();
		// default to inline for ajax requests
		boolean inline = booleanValueForBinding("inline", ERXAjaxApplication.isAjaxRequest(wocontext.request()));
		WOResponse response = inline ? r : new WOResponse();
		response._appendContentAsciiString("<link ");
		response._appendTagAttributeAndValue("rel", "stylesheet", false);
		response._appendTagAttributeAndValue("type", "text/css", false);

		if (href == null) {
			String key = styleSheetKey();
			ERXExpiringCache<String, WOResponse> cache = cache(session());
			if (cache.isStale(key) || ERXApplication.isDevelopmentModeSafe()) {
				WOResponse newresponse = new WOResponse();
				super.appendToResponse(newresponse, wocontext);
				// appendToResponse above will change the response of
				// "wocontext" to "newresponse". When this happens during an
				// Ajax request, it will lead to backtracking errors on
				// subsequent requests, so restore the original response "r"
				wocontext._setResponse(r);  
				newresponse.setHeader("text/css", "content-type");
				cache.setObjectForKey(newresponse, key);
			}
			href = wocontext.directActionURLForActionNamed(Sheet.class.getName() + "/" + key, null);
		}
		response._appendTagAttributeAndValue("href", href, false);

		String media = mediaType();
		if (media != null) {
			response._appendTagAttributeAndValue("media", media, false);
		}

		response._appendContentAsciiString(">");
		if (ERXStyleSheet.shouldCloseLinkTags()) {
			response._appendContentAsciiString("</link>");
		}
		if (!inline) {
			ERXWOContext.insertInResponseBeforeTag(r, response.contentString(), ERXWOContext._htmlCloseHeadTag(), false, true);
		}
	}

	/**
	 * Returns whether or not XHTML link tags should be used. If false, then
	 * link tags will not be closed, which is more compatible with certain
	 * browser parsers. Set the 'er.extensions.ERXStyleSheet.xhtml' to control
	 * this property.
	 * 
	 * @return true of link tags should be closed, false otherwise
	 */
	public static boolean shouldCloseLinkTags() {
		return ERXProperties.booleanForKeyWithDefault("er.extensions.ERXStyleSheet.xhtml", true);
	}
}
