/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import er.directtoweb.ERDirectToWeb;
import er.extensions.*;
import er.wrox.User;
import java.util.Enumeration;

public class Application extends ERXApplication { 

    public static void main(String argv[]) {
        WOApplication.main(argv, Application.class);
    }
    
    public static final NSArray AllLooks = new NSArray("Ugly");
    public NSArray looks() { return AllLooks; }
    
    public Application() {
        // http://myhost:aPort/cgi-bin/WebObjects/MyApp.woa/wa/WOStats
        statisticsStore().setPassword("4AppStats");
        // Make direct actions be the default request handler.
        setDefaultRequestHandler(requestHandlerForKey(directActionRequestHandlerKey()));
        User.registerUserPreferenceHandler();
        //ERXEntityClassDescription.registerDescription();
        //ERXValidationFactory.defaultFactory().configureFactory();
    }

    public WOResponse handleException(Exception exception, WOContext context) {
        NSMutableDictionary extraInfo=new NSMutableDictionary();
        if (context != null && context.page()!=null) {
            extraInfo.setObjectForKey(context.page().toString(), "currentPage");
            extraInfo.setObjectForKey(context.request().uri(),"uri");
            if (context.page() instanceof D2WComponent) {
                D2WContext c=((D2WComponent)context.page()).d2wContext();
                String pageConfiguration=(String)c.valueForKey("pageConfiguration");
                if (pageConfiguration!=null)
                    extraInfo.setObjectForKey(pageConfiguration,"pageConfiguration");
            }
            if (context.session() != null && context.session().statistics() != null) {
                extraInfo.setObjectForKey(context.session().statistics(), "previousPageList");
            }
        }
        // Report the exception in some fashion other than just printing it out to the screen.
        reportException(exception, extraInfo);
        //return debuggingEnabled() ? super.handleException(exception, context) : pageWithName("ErrorPage",context).generateResponse();
        return super.handleException(exception, context);
    }

    public WOResponse reportException(Throwable exception, NSDictionary extraInfo) {
        try {
             NSLog.out.appendln("    **** Caught: "+exception);
             //NSLog.out.appendln("         Actor: "+User.actor());
             NSLog.out.appendln("         Extra Information "+extraInfo);
            exception.printStackTrace();
            if (WOApplication.application().isCachingEnabled()) {// we take this to mean we are in deployment
                // Report the exception in someway.
            }
        } catch (Throwable u) { // WE DON'T WANT ANYTHING TO GO WRONG IN HERE as it would cause the app to exit
             NSLog.out.appendln("************ Caught exception "+u+" trying to report another one: "+exception);
             NSLog.out.appendln("** Original exception ");
            exception.printStackTrace();
             NSLog.out.appendln("** Second exception ");
            u.printStackTrace();
        }
        return null;
    }
}
