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

/**
 *  ERXApplication is the abstract superclass of WebObjects applications
 *  built with the ER frameworks.<br/>
 *  <br/>
 *  Useful enhancements include the ability to change the deployed name of
 *  the application, support for automatic application restarting at given intervals
 *  and more context information when handling exceptions.
 */

public abstract class ERXApplication extends WOApplication {

    /** logging support */
    public static final ERXLogger cat = ERXLogger.getLogger(ERXApplication.class);

    // FIXME: Should correct all references to WORequestHandler.DidHandleRequestNotification and then delete this ivar. 
    public static final String WORequestHandlerDidHandleRequestNotification = "WORequestHandlerDidHandleRequestNotification";

    /**
     * The ERXApplication singleton.
     * @return returns the <code>WOApplication.application()</code> cast as an ERXApplication
     */
    public static ERXApplication erxApplication() { return (ERXApplication)WOApplication.application(); }
    
    /**
     *  Adds support for automatic application cycling. Applications can be configured
     *  to cycle in two ways:<br/>
     *  <br/>
     *  The first way is by setting the System property <b>ERTimeToLive</b> to the number
     *  of seconds that the application should be up before terminating. Note that when
     *  the application's time to live is up it will quit calling the method <code>killInstance</code>.<br/>
     *  <br/>
     *  The second way is by setting the System property <b>ERTimeToDie</b> to the number
     *  of seconds that the application should be up before starting to refuse new sessions.
     *  In this case when the application starts to refuse new sessions it will also register
     *  a kill timer that will terminate the application between 30 minutes and 1:30 minutes.<br/>
     */
    public void run() {
        int timeToLive=ERXProperties.intForKey("ERTimeToLive");
        if (timeToLive > 0) {
            cat.info("Instance will live "+timeToLive+" seconds.");
            NSLog.out.appendln("Instance will live "+timeToLive+" seconds.");
            NSTimestamp now=new NSTimestamp();
            NSTimestamp exitDate=(new NSTimestamp()).timestampByAddingGregorianUnits(0, 0, 0, 0, 0, timeToLive);
            WOTimer t=new WOTimer(exitDate, 0, this, "killInstance", null, null, false);
            t.schedule();
        }
        int timeToDie=ERXProperties.intForKey("ERTimeToDie");
        if (timeToDie > 0) {
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

    /**
     *  Stops the application from handling any new requests. Will still handle
     *  requests from existing sessions. Also registers a kill timer that will
     *  terminate the application thirty minutes from the time this method is
     *  called
     */
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

    /**
     *  Killing the instance will log a 'Forcing exit' message and then call <code>System.exit(1)</code>
     */
    public void killInstance() {
        cat.info("Forcing exit");
        NSLog.out.appendln("Forcing exit");
        System.exit(1);
    }
    /** cached name suffix */
    private String _nameSuffix;
    /** has the name suffix been cached? */
    private boolean _nameSuffixLookedUp=false;
    /**
     *  The name suffix is appended to the current name of the application. This adds the ability to
     *  add a useful suffix to differentuate between different sets of applications on the same machine.<br/>
     *  <br/>
     *  The name suffix is set via the System property <b>ERApplicationNameSuffix</b>.<br/>
     *  <br/>
     *  For example if the name of an application is Buyer and you want to have a training instance
     *  appear with the name BuyerTraining then you would set the ERApplicationNameSuffix to Training.<br/>
     *  <br/>
     *  @return the System property <b>ERApplicationNameSuffix</b> or <code>null</code>
     */
    public String nameSuffix() {
        if (!_nameSuffixLookedUp) {
            _nameSuffix=System.getProperty("ERApplicationNameSuffix");
            _nameSuffix=_nameSuffix==null ? "" : _nameSuffix;
            _nameSuffixLookedUp=true;
        }
        return _nameSuffix;
    }
    /** cached computed name */
    private String _userDefaultName;
    /**
     *  Adds the ability to completely change the applications name by setting the System property
     *  <b>ERApplicationName</b>. Will also append the <code>nameSuffix</code> if one is set.<br/>
     *  <br/>
     *  @return the computed name of the application.
     */
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

    /**
     *  This method returns {@link WOApplication}'s <code>name</code> method.<br/>
     *  @return the name of the application executable. 
     */
    public String rawName() { return super.name(); }

    /**
     *  Adds a bunch of useful information relative to the current state when the exception
     *  occurried. Potentially added information:<br/>
     * <ol>
     * <li>the current page name</li>
     * <li>the current component</li>
     * <li>the complete hierarchy of nested components</li>
     * <li>the requested uri</li>
     * <li>the D2W page configuration</li>
     * <li>the previous page list (from the WOStatisticsStore)</li>
     * </ol>
     * <br/>
     * @return the WOResponse of the generated exception page.
     */
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

    // DELETE ME
     public String mailWrapperName() { return "PageWrapper"; }

    // DELETE ME
    protected D2WContext _d2wContext;
    // DELETE ME
    public D2WContext d2wContext() {
        if (_d2wContext == null)
            _d2wContext = new D2WContext();
        return _d2wContext;
    }

    // DELETE ME
    public void resetSignificantKeys() {}
    // DELETE ME
    public void warmUpRuleCache() {
        // Moved to ERDirectToWeb
        //ERD2WModel.erDefaultModel().prepareDataStructures();
    }

    // DELETE ME
    public ERXSimpleHTMLFormatter formatter() { return ERXSimpleHTMLFormatter.formatter(); }
   
    // DELETE ME
    public WOComponent pageWithName(String name) {
        // Was: pageWithName(name, new WOContext());
        return pageWithName(name, null);
    }
}
