/*
 * WOMetaRefresh.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class WOMetaRefresh extends WOComponent
{
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;


    public WOMetaRefresh(WOContext aContext)  {
        super(aContext);
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    // ** This component will insert a meta-refresh tag into your page.  You can set the number of seconds delay before refresh and either a pageName to transition to or action which is fired after the delay.

    public String contentString()  {
        // contentString = aSeconds+";url="+aUrl;
        Object aSeconds = valueForBinding("seconds");
        String aUrl = context().componentActionURL();
        StringBuilder buffer = new StringBuilder(40); //reasonable value
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
