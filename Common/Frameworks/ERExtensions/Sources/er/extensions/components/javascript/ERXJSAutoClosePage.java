/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components.javascript;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/**
 * When returned will close the current page by calling <code>onload=window.close()</code>.
 * Useful for server side determining if a window should be closed.<br/>
 * <br/>
 * No Bindings.
 */
public class ERXJSAutoClosePage extends WOComponent {

    /** Public constructor */
    public ERXJSAutoClosePage(WOContext aContext) {
        super(aContext);
    }
    
    /** component is stateless */
    public boolean isStateless() { return true; }
}
