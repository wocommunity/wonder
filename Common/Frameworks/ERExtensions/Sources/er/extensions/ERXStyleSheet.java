/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;

/**
 * Simple stateless component used for adding a style sheet
 * and/or a favicon link to a page. Note the way this component
 * currently works all of the urls are cached for the life of the
 * application. This will be configurable in the future.
 * @binding styleSheetName name of the style sheet
 * @binding styleSheetFrameworkName name of the framework for the style sheet
 * @binding styleSheetUrl url to the style sheet
 * @binding favIconLink url to the fav icon used for bookmarking
 */
// ENHANCEME: Should support having the favIcon as a WebServerResource, like we do for the style sheet
// ENHANCEME: Should support direct binding of a styleSheetLink
// CHECKME: Might want to think about having an ERXFavIcon component so people know it is here.
public class ERXStyleSheet extends ERXStatelessComponent {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXStyleSheet.class);

    /** holds the url of the fav icon */
    private String _favIconLink;

    /**
     * Public constructor
     * @param aContext a context
     */
    public ERXStyleSheet(WOContext aContext) {
        super(aContext);
    }

	public void reset(){
        super.reset();
        _favIconLink = null;
    }

    /**
     * returns the complete url to the style sheet.
     * @return style sheet url
     */
    public String styleSheetUrl() {
    	String url = (String) valueForBinding("styleSheetUrl");
    	if(url == null) {
    		url = application().resourceManager().urlForResourceNamed(styleSheetName(),
                    styleSheetFrameworkName(),languages(),context().request());
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
     * Returns the favIcon url link.
     * @return favIcon url
     */
    public String favIconLink() {
        if (_favIconLink == null)
            _favIconLink = (String)valueForBinding("favIconLink");
        return _favIconLink;
    }    

    private NSArray languages() {
    	if(hasSession())
    		return session().languages();
    	WORequest request = context().request();
    	if (request != null)
    		return request.browserLanguages();
    	return null;
    }

}
