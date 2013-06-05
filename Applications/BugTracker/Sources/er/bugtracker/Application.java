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
import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.ERXDelayedRequestHandler;
import er.extensions.appserver.navigation.ERXNavigationManager;
import er.extensions.components._private.ERXSubmitButton;
import er.extensions.foundation.ERXPatcher;
import er.extensions.foundation.ERXProperties;

public class Application extends ERXApplication {

    public String databaseName = "BugTracker";
    
    private MailReader reader;

    public static void main(String argv[]) {
        ERXApplication.main(argv, Application.class);
    }

   /**
    * @deprecated We need to remove the call to ERXRestRequestHandler
    */
    @Deprecated
    public Application() {
        ERXNavigationManager.manager().configureNavigation();
        setContextClassName("er.extensions.appserver.ERXWOContext");
        registerRequestHandler(new ERXDelayedRequestHandler(), ERXDelayedRequestHandler.KEY);
        setPageRefreshOnBacktrackEnabled(true);
        ERXPatcher.setClassForName(ERXSubmitButton.class, "WOSubmitButton");
        // ERXPatcher.setClassForName(WOSubmitButton.class, "WOSubmitButton");

        // http://myhost:aPort/cgi-bin/WebObjects/MyApp.woa/wa/WOEventSetup
        setDefaultRequestHandler(requestHandlerForKey(directActionRequestHandlerKey()));
        setTimeOut(8 * 60 * 60); // set the timeout to 8 hours.
        D2W.setFactory(new Factory());
    }

    @Override
    public void finishInitialization() {
        if(ERXProperties.booleanForKeyWithDefault("BugTracker.processMails", false)) {
            reader = new MailReader(null);
            reader.startReader();
        }
        NSLog.debug.appendln("finishInitialization called.");
    }
}
