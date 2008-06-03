/*
 * WOMetaRefresh.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class WOMetaRefresh extends WOComponent
{

    public WOMetaRefresh(WOContext aContext)  {
        super(aContext);
    }

    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    // ** This component will insert a meta-refresh tag into your page.  You can set the number of seconds delay before refresh and either a pageName to transition to or action which is fired after the delay.

    public String contentString()  {
        // contentString = aSeconds+";url="+aUrl;
        Object aSeconds = valueForBinding("seconds");
        String aUrl = context().componentActionURL();
        StringBuffer buffer = new StringBuffer(40); //reasonable value
        if (aSeconds != null) {
            buffer.append(aSeconds.toString());
        } else {
            throw new IllegalArgumentException("'seconds' is a required binding");
        }
        buffer.append(";url=");
        buffer.append(aUrl);
        return buffer.toString();
    }

    public WOComponent invokeAction()  {
        WOComponent aComponent = null;
        if (hasBinding("pageName")) {
            String aPageName = (String)_WOJExtensionsUtil.valueForBindingOrNull("pageName",this);
            aComponent = pageWithName(aPageName);
        } else {
            aComponent = (WOComponent)_WOJExtensionsUtil.valueForBindingOrNull("action",this);
        }
        return aComponent;
    }  
}
