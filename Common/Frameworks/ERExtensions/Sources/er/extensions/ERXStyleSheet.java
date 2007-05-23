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
 * Adds a style sheet to a page. You can either supply
 * a complete URL, a file and framework name or put something in the component content.
 * The content of the component is cached under a "key" binding and then delivered 
 * via a direct action, so it doesn't need to get re-rendered to often.
 * @binding filename name of the style sheet
 * @binding framework name of the framework for the style sheet
 * @binding href url to the style sheet
 * @binding key key to cache the style sheet under. Default is the 
 *   sessionID
 */
//FIXME: cache should be able to cache on calues of bindings, not a single key
public class ERXStyleSheet extends ERXStatelessComponent {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXStyleSheet.class);

    /**
     * Public constructor
     * @param aContext a context
     */
    public ERXStyleSheet(WOContext aContext) {
        super(aContext);
    }

    private static ERXExpiringCache cache(WOSession session) {
    	ERXExpiringCache cache = (ERXExpiringCache) session.objectForKey("ERXStylesheet.cache");
    	if(cache == null) {
    		cache = new ERXExpiringCache(60);
    		session.setObjectForKey(cache, "ERXStylesheet.cache");
    	}
    	return cache;
    }
    
    public static class Sheet extends WODirectAction {

    	public Sheet(WORequest worequest) {
			super(worequest);
		}
    	
		public WOActionResults performActionNamed(String name) {
			WOResponse response = (WOResponse) cache(session()).objectForKey(name);
			return response;
		}
    }
    
    /**
     * returns the complete url to the style sheet.
     * @return style sheet url
     */
    public String styleSheetUrl() {
    	String url = (String) valueForBinding("styleSheetUrl");
    	url = ( url == null ? (String) valueForBinding("href") : url);
    	if(url == null) {
    		String name = styleSheetName();
    		if(name != null) {
    			url = application().resourceManager().urlForResourceNamed(styleSheetName(),
    					styleSheetFrameworkName(),languages(),context().request());
    		}
    	}
        return url;
    }
    
    /**
     * Returns the style sheet framework name either resolved
     * via the binding <b>framework</b>.
     * @return style sheet framework name
     */
    public String styleSheetFrameworkName() {
    	String result = (String)valueForBinding("styleSheetFrameworkName");
    	result = (result == null ? (String) valueForBinding("framework") : result);
    	return result;
    }

    /**
     * Returns the style sheet name either resolved
     * via the binding <b>filename</b>.
     * @return style sheet name
     */
    public String styleSheetName() {
    	String result = (String)valueForBinding("styleSheetName");
    	result = (result == null ? (String) valueForBinding("filename") : result);
    	return result;
    }

    /**
     * Returns key under whic the stylesheet should be placed in the cache.
     * If no key is given, the session id is used.
     * @return style sheet framework name
     */
    public String styleSheetKey() {
    	String result = (String)valueForBinding("key");
		if(result == null)  {
			result = context().session().sessionID();
		}
    	return result;
    }

    /**
     * Returns the languages for the request.
     * @return
     */
    private NSArray languages() {
    	if(hasSession())
    		return session().languages();
    	WORequest request = context().request();
    	if (request != null)
    		return request.browserLanguages();
    	return null;
    }

    /**
     * Appends the &ltlink&gt; tag, either by using the style sheet name and framework or
     * by using the component content and then generating a link to it.
     */
    public void appendToResponse(WOResponse r, WOContext wocontext) {
    	String href = styleSheetUrl();
    	WOResponse woresponse = new WOResponse();
    	woresponse._appendContentAsciiString("<link ");
    	woresponse._appendTagAttributeAndValue("rel", "stylesheet", false);
    	woresponse._appendTagAttributeAndValue("type", "text/css", false);
    	if(href == null) {
    		String key = styleSheetKey();
    		ERXExpiringCache cache = cache(session());
    		if(cache.isStale(key) || ERXApplication.isDevelopmentModeSafe()) {
    			WOResponse newresponse = new WOResponse();
    			super.appendToResponse(newresponse, wocontext);
    			newresponse.setHeader("text/css", "content-type");
    			cache.setObjectForKey(newresponse, key);
    		}
    		href = wocontext.directActionURLForActionNamed(Sheet.class.getName() + "/" + key, null);
    	}
    	woresponse._appendTagAttributeAndValue("href", href, false);
    	woresponse._appendContentAsciiString("></link>");
    	ERXWOContext.insertInResponseBeforeTag(r, woresponse.contentString(), ERXWOContext._htmlCloseHeadTag(), false, true);
    }
}
