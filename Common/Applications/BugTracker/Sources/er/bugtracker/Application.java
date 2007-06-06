/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.bugtracker;

import com.webobjects.directtoweb.D2W;
import com.webobjects.foundation.NSLog;

import er.bugtracker.mail.MailReader;
import er.extensions.ERXApplication;
import er.extensions.ERXNavigationManager;
import er.extensions.ERXPatcher;
import er.extensions.ERXProperties;
import er.extensions.ERXSubmitButton;
import er.rest.ERXRestRequestHandler;

public class Application extends ERXApplication {

    public String databaseName = "BugTracker";
    
    private MailReader reader;

    public static void main(String argv[]) {
        ERXApplication.main(argv, Application.class);
    }

    public Application() {
        ERXNavigationManager.manager().configureNavigation();
        setContextClassName("er.extensions.ERXWOContext");
        registerRequestHandler(ERXRestRequestHandler.createUnsafeRequestHandler(false, false), "rest");
        setPageRefreshOnBacktrackEnabled(true);
        ERXPatcher.setClassForName(ERXSubmitButton.class, "WOSubmitButton");
        // ERXPatcher.setClassForName(WOSubmitButton.class, "WOSubmitButton");

        // http://myhost:aPort/cgi-bin/WebObjects/MyApp.woa/wa/WOEventSetup
        setDefaultRequestHandler(requestHandlerForKey(directActionRequestHandlerKey()));
        setTimeOut(8 * 60 * 60); // set the timeout to 8 hours.
        D2W.setFactory(new Factory());
    }

    public void finishInitialization() {
        if(ERXProperties.booleanForKeyWithDefault("BugReporter.processMails", false)) {
            reader = new MailReader(null);
            reader.startReader();
        }
        NSLog.debug.appendln("finishInitialization called.");
    }
}
