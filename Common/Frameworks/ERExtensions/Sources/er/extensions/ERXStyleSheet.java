/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

/* ERStyleSheet.java created by patrice on Thu 10-Aug-2000 */
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import org.apache.log4j.Category;

public class ERXStyleSheet extends WOComponent {

    public ERXStyleSheet(WOContext aContext) {
        super(aContext);
    }

    /////////////////////////////////////  log4j category  ////////////////////////////////////
    public static Category cat = Category.getInstance(ERXStyleSheet.class);
    
    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBinding() { return false; }

    // no reset
    // since this is cached for the life of the app..
    /* public void reset(){
        super.reset();
        _styleSheetUrl = null;
    } */
    
    private String _styleSheetUrl;
    public String styleSheetUrl() {
        if (_styleSheetUrl==null) {
            _styleSheetUrl=application().resourceManager().urlForResourceNamed(styleSheetName(), styleSheetFrameworkName(),null,context().request());
        }
        return _styleSheetUrl;
    }

    // ASSUME: This is cached for the life of the app.  If you want to change style sheets mid-app you will want to place this ivar in the reset
    //		method and change the check.
    private String _styleSheetFrameworkName;
    private boolean _frameworkNameInitialized = false;
    public String styleSheetFrameworkName() {
        if (!_frameworkNameInitialized) {
            _styleSheetFrameworkName = (String)(hasBinding("styleSheetFrameworkName") ? valueForBinding("styleSheetFrameworkName") :
                                                ERXExtensions.configurationForKey("styleSheetFrameworkName"));
            _frameworkNameInitialized = true;
            if (cat.isDebugEnabled())
                cat.debug("Style sheet framework name: " + _styleSheetFrameworkName);
        }
        return _styleSheetFrameworkName;
    }

    // ASSUME: This is cached for the life of the app.  If you want to change style sheet's name mid-app you will want to place this ivar in the reset
    //		method and change the check.
    private String _styleSheetName;
    private boolean _styleSheetNameInitialized = false;
    public String styleSheetName() {
        if (!_styleSheetNameInitialized) {
            _styleSheetName = (String)(hasBinding("styleSheetName") ? valueForBinding("styleSheetName") :
                                       ERXExtensions.configurationForKey("styleSheetName"));
            _styleSheetNameInitialized = true;
            if (cat.isDebugEnabled())
                cat.debug("Style sheet name: " + _styleSheetName);
        }
        return _styleSheetName;
    }

    private String _favIconLink;
    public String favIconLink() {
        if (_favIconLink == null) {
            _favIconLink = (String)valueForBinding("favIconLink");
        }
        return _favIconLink;
    }    
}
