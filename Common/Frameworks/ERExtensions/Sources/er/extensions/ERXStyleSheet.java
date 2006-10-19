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
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Simple stateless component used for adding a style sheet to a page. 
 * @binding styleSheetName name of the style sheet
 * @binding styleSheetFrameworkName name of the framework for the style sheet
 * @binding styleSheetUrl url to the style sheet
 * @binding styleSheetKey key to cache the style sheet under
 */
//FIXME: cache should be cleared once in a while
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

    private static NSMutableDictionary cache = (NSMutableDictionary) ERXMutableDictionary.synchronizedDictionary();
    
    public static class Sheet extends WODirectAction {

    	public Sheet(WORequest worequest) {
			super(worequest);
		}
    	
		public WOActionResults performActionNamed(String name) {
			WOResponse response = (WOResponse) cache.objectForKey(name);
			return response;
		}
    }
    
    /**
     * returns the complete url to the style sheet.
     * @return style sheet url
     */
    public String styleSheetUrl() {
    	String url = (String) valueForBinding("styleSheetUrl");
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
     * via the binding <b>styleSheetFrameworkName</b>.
     * @return style sheet framework name
     */
    public String styleSheetFrameworkName() {
        return (String)valueForBinding("styleSheetFrameworkName");
    }

    /**
     * Returns the style sheet name either resolved
     * via the binding <b>styleSheetName</b>.
     * @return style sheet name
     */
    public String styleSheetName() {
        return (String)valueForBinding("styleSheetName");
    }
    
    /**
     * Returns the style sheet framework name either resolved
     * via the binding <b>styleSheetFrameworkName</b>.
     * @return style sheet framework name
     */
    public String styleSheetKey() {
        return (String)valueForBinding("styleSheetKey");
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
	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		String href = styleSheetUrl();
		woresponse._appendContentAsciiString("<link ");
		woresponse._appendTagAttributeAndValue("rel", "stylesheet", false);
		woresponse._appendTagAttributeAndValue("type", "text/css", false);
		if(href == null) {
			String key = styleSheetKey();
			if(key == null) {
				key = wocontext.session().sessionID();
			}
			if(cache.objectForKey(key) == null) {
				WOResponse newresponse = new WOResponse();
				super.appendToResponse(newresponse, wocontext);
				newresponse.setHeader("text/css", "content-type");
				cache.setObjectForKey(newresponse, key);
			}
			href = wocontext.directActionURLForActionNamed(Sheet.class.getName() + "/" + key, null);
		}
		woresponse._appendTagAttributeAndValue("href", href, false);
		woresponse._appendContentAsciiString("></link>");
	}

}
