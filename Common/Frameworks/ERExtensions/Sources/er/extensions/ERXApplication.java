/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERXApplication.java created by max on Fri 29-Sep-2000 */

package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import com.webobjects.appserver.*;
import java.util.*;
import org.apache.log4j.Category;

public abstract class ERXApplication extends WOApplication {

    //////////////////////////////////////////////  log4j category  ////////////////////////////////////////////
    public static final Category cat = Category.getInstance(ERXApplication.class);

    ////////////////////////////////////////////// Notification Hooks //////////////////////////////////////////
    // A few undocumented notifications that are nice ;)
    public static final String WORequestHandlerDidHandleRequestNotification = "WORequestHandlerDidHandleRequestNotification";
    public static final String ApplicationWillDispatchRequestNotification = "WOApplicationWillDispatchRequestNotification";
    public static final String ApplicationDidDispatchRequestNotification = "WOApplicationDidDispatchRequestNotification";
    
    // We want this to happen early.
    static {
        // This will configure the Log4j system.
        // This is OK to call multiple times as it will only be configured the first time.
        ERXLog4j.configureLogging();
    }

    //------------------------------------------------------
    // Application Cycling
    public void run() {
        int timeToLive=NSProperties.integerForKey("ERTimeToLive");
        if (timeToLive!=0) {
            cat.info("Instance will live "+timeToLive+" seconds.");
            NSLog.out.appendln("Instance will live "+timeToLive+" seconds.");
            NSTimestamp now=new NSTimestamp();
            NSTimestamp exitDate=(new NSTimestamp()).timestampByAddingGregorianUnits(0, 0, 0, 0, 0, timeToLive);
            WOTimer t=new WOTimer(exitDate, 0, this, "killInstance", null, null, false);
            t.schedule();
        }
        int timeToDie=NSProperties.integerForKey("ERTimeToDie");
        if (timeToDie!=0) {
            // FIXME we could randomize this (30mn?) to help with restart related downtime problems?
            cat.info("Instance will not live past "+timeToDie+":00.");
            NSLog.out.appendln("Instance will not live past "+timeToDie+":00.");
            NSTimestamp now=new NSTimestamp();
            int s=(timeToDie-now.hourOfDay())*3600-now.minuteOfHour()*60;
            if (s<0) s+=24*3600; // how many seconds to the deadline

            // deliberately randomize this so that not all instances restart at the same time
            // adding up to 1 hour
            s+=(new Random()).nextFloat()*3600;

            NSTimestamp stopDate=now.timestampByAddingGregorianUnits(0, 0, 0, 0, 0, s);
            WOTimer t=new WOTimer(stopDate, 0, this, "startRefusingSessions", null, null, false);
            t.schedule();        }
        super.run();
    }

    public void startRefusingSessions() {
        cat.info("Refusing new sessions");
        NSLog.out.appendln("Refusing new sessions");
        refuseNewSessions(true);
        cat.info("Registering kill timer");
        NSTimestamp now=new NSTimestamp();
        NSTimestamp exitDate=(new NSTimestamp()).timestampByAddingGregorianUnits(0, 0, 0, 0, 0, 1800);
        WOTimer t=new WOTimer(exitDate, 0, this, "killInstance", null, null, false);
        t.schedule();
    }

    public void killInstance() {
        cat.info("Forcing exit");
        NSLog.out.appendln("Forcing exit");
        System.exit(1);
    }

    //-------------------------------------------------------------------------------------------------
    // ability to change your name -- useful for training instances
    // this may not even be needed if you don't use Monitor
    private String _nameSuffix;
    private boolean _nameSuffixLookedUp=false;
    public String nameSuffix() {
        if (!_nameSuffixLookedUp) {
            _nameSuffix=NSProperties.stringForKey("ERApplicationNameSuffix");
            _nameSuffix=_nameSuffix==null ? "" : _nameSuffix;
            _nameSuffixLookedUp=true;
        }
        return _nameSuffix;
    }
    
    private String _userDefaultName;
    public String name() {
        if (_userDefaultName==null) {
            _userDefaultName=NSProperties.stringForKey("ERApplicationName");
            if (_userDefaultName==null) _userDefaultName=super.name();
            if (_userDefaultName!=null) {
                String suffix=nameSuffix();
                if (suffix!=null && suffix.length()>0)
                    _userDefaultName+=suffix;
            }
        }
        return _userDefaultName;
    }

    public String rawName() { return super.name(); }

    public static ERXApplication erxApplication() { return (ERXApplication)WOApplication.application(); }

    // backstop wrapper for notifications
    public String mailWrapperName() { return "PageWrapper"; }

    // FIXME: Need to get rid of these now.
    protected D2WContext _d2wContext;
    public D2WContext d2wContext() {
        if (_d2wContext == null)
            _d2wContext = new D2WContext();
        return _d2wContext;
    }

    // for subclasses to override
    public void resetSignificantKeys() {}
    public void warmUpRuleCache() {
        // Moved to ERDirectToWeb
        //ERD2WModel.erDefaultModel().prepareDataStructures();
    }

    public ERXSimpleHTMLFormatter formatter() { return ERXSimpleHTMLFormatter.formatter(); }
   
    public WOComponent pageWithName(String name) {
        // Was: pageWithName(name, new WOContext());
        return pageWithName(name, null);
    }

}
