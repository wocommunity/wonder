/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import com.webobjects.appserver.*;
import java.util.*;
import org.apache.log4j.Category;

public abstract class ERXApplication extends WOApplication {
    // We want this to happen early.
    static {
        // This will configure the Log4j system.
        // This is OK to call multiple times as it will only be configured the first time.
        ERXLog4j.configureLogging();
    }
    
    //////////////////////////////////////////////  log4j category  ////////////////////////////////////////////
    // public static final ERXLogger cat = ERXLogger.getLogger(ERXApplication.class);
    public static final Category cat = Category.getInstance(ERXApplication.class);

    ////////////////////////////////////////////// Notification Hooks //////////////////////////////////////////
    // A few undocumented notifications that are nice ;)
    public static final String WORequestHandlerDidHandleRequestNotification = "WORequestHandlerDidHandleRequestNotification";
    public static final String ApplicationWillDispatchRequestNotification = "WOApplicationWillDispatchRequestNotification";
    public static final String ApplicationDidDispatchRequestNotification = "WOApplicationDidDispatchRequestNotification";
    

    //------------------------------------------------------
    // Application Cycling
    public void run() {
        int timeToLive=ERXProperties.intForKey("ERTimeToLive");
        if (timeToLive!=0) {
            cat.info("Instance will live "+timeToLive+" seconds.");
            NSLog.out.appendln("Instance will live "+timeToLive+" seconds.");
            NSTimestamp now=new NSTimestamp();
            NSTimestamp exitDate=(new NSTimestamp()).timestampByAddingGregorianUnits(0, 0, 0, 0, 0, timeToLive);
            WOTimer t=new WOTimer(exitDate, 0, this, "killInstance", null, null, false);
            t.schedule();
        }
        int timeToDie=ERXProperties.intForKey("ERTimeToDie");
        if (timeToDie!=0) {
            // FIXME we could randomize this (30mn?) to help with restart related downtime problems?
            cat.info("Instance will not live past "+timeToDie+":00.");
            NSLog.out.appendln("Instance will not live past "+timeToDie+":00.");
            NSTimestamp now=new NSTimestamp();
            int s=(timeToDie-ERXTimestampUtility.hourOfDay(now))*3600-ERXTimestampUtility.minuteOfHour(now)*60;
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
            _nameSuffix=System.getProperty("ERApplicationNameSuffix");
            _nameSuffix=_nameSuffix==null ? "" : _nameSuffix;
            _nameSuffixLookedUp=true;
        }
        return _nameSuffix;
    }
    
    private String _userDefaultName;
    public String name() {
        if (_userDefaultName==null) {
            _userDefaultName=System.getProperty("ERApplicationName");
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

    public WOResponse handleException(Exception exception, WOContext context) {
        NSMutableDictionary extraInfo=new NSMutableDictionary();
        if (context!=null && context.page()!=null) {
            extraInfo.setObjectForKey(context.page().name(), "CurrentPage");
            if (context.component() != null) {
                extraInfo.setObjectForKey(context.component().name(), "CurrentComponent");
                if (context.component().parent() != null) {
                    WOComponent component = context.component();
                    NSMutableArray hierarchy = new NSMutableArray(component.name());
                    while (component.parent() != null) {
                        component = component.parent();
                        hierarchy.addObject(component.name());
                    }
                    extraInfo.setObjectForKey(hierarchy, "CurrentComponentHierarchy");
                }
            }
            extraInfo.setObjectForKey(context.request().uri(), "uri");
            if (context.page() instanceof D2WComponent) {
                D2WContext c=((D2WComponent)context.page()).d2wContext();
                String pageConfiguration=(String)c.valueForKey("pageConfiguration");
                if (pageConfiguration!=null)
                    extraInfo.setObjectForKey(pageConfiguration, "D2W-PageConfiguration");
            }
            if (context.hasSession())
                if (context.session().statistics() != null)
                    extraInfo.setObjectForKey(context.session().statistics(), "PreviousPageList");
        }
        cat.warn("Exception caught, " + exception.getMessage() + " extra info: " + extraInfo);
        return super.handleException(exception, context);
    }    
    
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
