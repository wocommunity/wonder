/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.bugtracker;

import java.util.Enumeration;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOMailDelivery;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;

import er.extensions.ERXApplication;
import er.extensions.ERXEC;
import er.extensions.ERXNavigationManager;
import er.extensions.ERXProperties;
import er.extensions.ERXWOContext;

public class Application extends ERXApplication {

    public String databaseName;

    public static void main(String argv[]) {
        ERXApplication.main(argv, Application.class);
    }

    private String _userDefaultName;

    public String name() {
        if (_userDefaultName == null) {
            _userDefaultName = System.getProperty("BTApplicationName");
            if (_userDefaultName == null)
                _userDefaultName = super.name();
        }
        return _userDefaultName;
    }

    public Application() {
        ERXNavigationManager.manager().configureNavigation();
        setContextClassName("er.extensions.ERXWOContext");
        setPageRefreshOnBacktrackEnabled(true);
        // http://myhost:aPort/cgi-bin/WebObjects/MyApp.woa/wa/WOEventSetup
        setDefaultRequestHandler(requestHandlerForKey(directActionRequestHandlerKey()));
        setTimeOut(8 * 60 * 60); // set the timeout to 8 hours.
    }

    public void finishInitialization() {
        NSLog.debug.appendln("finishInitialization called.");
        try {
            adjustConnectionDictionary(EOModelGroup.defaultGroup().modelNamed("BugTracker"));
            boolean runBatchReport = ERXProperties.booleanForKey("BTRunBatchReport");
            if (runBatchReport) {
                runBatchReport();
                System.exit(0);
            }
        } catch (ExceptionInInitializerError e) {
            NSLog.err.appendln("Original exception " + e.getException());
        }
    }

    public void adjustConnectionDictionary(EOModel model) {
        try { 
            databaseName = (String) model.connectionDictionary().objectForKey("databaseName");
            EOEntity release = model.entityNamed("Release");
            if(model.connectionDictionary().toString().toLowerCase().contains("mysql")) {
            	release.setExternalName("`RELEASE`");
            }
        } catch (Exception ex) {
            NSLog.err.appendln("Original exception : " + ex);
        }
    }

    /**
     * we run over all people in the DB and send them a summary email if they
     * have unread bugs
     */
    public void runBatchReport() {
        EOEditingContext ec = ERXEC.newEditingContext();
        try {
            NSArray everybody = People.clazz.allObjects(ec);
            for (Enumeration e = everybody.objectEnumerator(); e.hasMoreElements();) {
                People person = (People) e.nextElement();
                NSDictionary bindings = new NSDictionary(new Object[] { person }, new Object[] { "user" });
                NSArray unreadBugs = person.unreadBugs();
                String email = person.email();
                if (unreadBugs.count() > 0 && email != null && email.length() != 0) {
                    WOComponent emailBody = pageWithName("BugReportEmail", ERXWOContext.newContext());
                    emailBody.takeValueForKey(unreadBugs, "unreadBugs");
                    emailBody.takeValueForKey(person, "owner");
                    WOMailDelivery.sharedInstance().composeComponentEmail("bugtracker@netstruxr.com", new NSArray(email), null,
                            "You have " + unreadBugs.count() + " unread bug(s)", emailBody, true);
                    NSLog.debug.appendln("Sending report to " + email + ": " + unreadBugs.count() + " unread bugs");
                }
            }
        } finally {
            ec.unlock();
        }
    }
}
