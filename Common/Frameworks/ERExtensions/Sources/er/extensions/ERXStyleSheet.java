/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/**
 * Simple stateless component used for adding a style sheet
 * and/or a favicon link to a page. Note the way this component
 * currently works all of the urls are cached for the life of the
 * application. This will be configurable in the future.
 * <br/>
 * Synopsis:<br/>
 * styleSheetName=<i>aString</i>;[styleSheetFrameworkName=<i>aString</i>;][favIconLink=<i>aBoolean</i>;]
 *
 * @binding styleSheetName name of the style sheet
 * @binding styleSheetFrameworkName name of the framework for the style sheet
 * @binding favIconLink url to the fav icon used for bookmarking
 */
// ENHANCEME: Should support having the favIcon as a WebServerResource, like we do for the style sheet
// ENHANCEME: Should support direct binding of a styleSheetLink
// CHECKME: Might want to think about having an ERXFavIcon component so people know it is here.
public class ERXStyleSheet extends WOComponent {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXStyleSheet.class);

    /** holds the calculated style sheet url */
    private String _styleSheetUrl;
    /** holds the style sheet framework name */
    private String _styleSheetFrameworkName;
    /** flags if the framework name has been initialized */
    private boolean _frameworkNameInitialized = false;
    /** holds the name of the style sheet */
    private String _styleSheetName;
    /** flags if the style sheet name has been initialized */
    private boolean _styleSheetNameInitialized = false;
    /** holds the url of the fav icon */
    private String _favIconLink;

    /**
     * Public constructor
     * @param aContext a context
     */
    public ERXStyleSheet(WOContext aContext) {
        super(aContext);
    }
        
    /**
     * Component is stateless
     * @return true
     */
    public boolean isStateless() { return true; }

    // no reset
    // since this is cached for the life of the app..
    // FIXME: This should be controlled by a system property
    /* public void reset(){
        super.reset();
        _styleSheetUrl = null;
    } */

    /**
     * returns the complete url to the style sheet.
     * @return style sheet url
     */
    public String styleSheetUrl() {
        if (_styleSheetUrl==null) {
            _styleSheetUrl=application().resourceManager().urlForResourceNamed(styleSheetName(),
                                        styleSheetFrameworkName(),_languages(),context().request());
        }
        return _styleSheetUrl;
    }
    /**
     * Returns the style sheet framework name either resolved
     * via the binding <b>styleSheetFrameworkName</b>.
     * @return style sheet framework name
     */
    public String styleSheetFrameworkName() {
        if (!_frameworkNameInitialized) {
            _styleSheetFrameworkName = (String)valueForBinding("styleSheetFrameworkName");
            _frameworkNameInitialized = true;
            if (log.isDebugEnabled())
                log.debug("Style sheet framework name: " + _styleSheetFrameworkName);
        }
        return _styleSheetFrameworkName;
    }

    /**
     * Returns the style sheet name either resolved
     * via the binding <b>styleSheetName</b>.
     * @return style sheet name
     */
    public String styleSheetName() {
        if (!_styleSheetNameInitialized) {
            _styleSheetName = (String)valueForBinding("styleSheetName");                                      
            _styleSheetNameInitialized = true;
            if (log.isDebugEnabled())
                log.debug("Style sheet name: " + _styleSheetName);
        }
        return _styleSheetName;
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

    private NSArray _languages() {
        WOSession session = session();
	if (session != null)
	    return session.languages();
	WORequest request = context().request();
	if (request != null)
	    return request.browserLanguages();
	return null;
    }

}
