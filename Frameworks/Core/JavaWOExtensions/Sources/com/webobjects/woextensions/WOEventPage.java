/*
 * WOEventPage.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;


public class WOEventPage extends WOComponent {
    public String password;
    public String userName;

    public WOEventPage(WOContext aContext)  {
        super(aContext);
    }

    public boolean isEventLoggingEnabled() {
        return false;
    }

    public String password() {
        // we need to do this so that the page always requires
        // explicit password input (and not recycles old input)
        return null;
    }

    public String userName() {
        // we need to do this so that the page always requires
        // explicit username input (and not recycles old input)
        return null;
    }

    public WOComponent submit() {
        session().validateEventsLogin(password, userName);
        return null;
    }
}
