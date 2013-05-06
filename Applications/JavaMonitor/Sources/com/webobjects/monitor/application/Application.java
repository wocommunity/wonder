package com.webobjects.monitor.application;

/*
 © Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

 IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

 In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
 SUCH DAMAGE.
 */
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver._private.WODirectActionRequestHandler;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.monitor._private.MSiteConfig;
import com.webobjects.monitor.rest.MApplicationController;
import com.webobjects.monitor.rest.MHostController;
import com.webobjects.monitor.rest.MSiteConfigController;

import er.extensions.appserver.ERXApplication;
import er.rest.routes.ERXRoute;
import er.rest.routes.ERXRouteRequestHandler;

public class Application extends ERXApplication {

    static public void main(String argv[]) {
    	ERXApplication.main(argv, Application.class);
    }

    public Application() {
        super();
        String dd = System.getProperties().getProperty("_DeploymentDebugging");
        if (dd != null) {
            NSLog.debug.setIsVerbose(true);
            NSLog.out.setIsVerbose(true);
            NSLog.err.setIsVerbose(true);
            NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupDeployment);
            NSLog.debug.setAllowedDebugLevel(NSLog.DebugLevelDetailed);
        }
        WOTaskdHandler.createSiteConfig();
        registerRequestHandler(new WODirectActionRequestHandler() {
            @Override
            public NSArray getRequestHandlerPathForRequest(WORequest worequest) {
                NSArray nsarray = new NSArray(AdminAction.class.getName());
                return nsarray.arrayByAddingObject(worequest.requestHandlerPath());
            }

        }, "admin");
        setAllowsConcurrentRequestHandling(true);
        ERXRouteRequestHandler restHandler = new ERXRouteRequestHandler();
        restHandler.addDefaultRoutes("MApplication", false, MApplicationController.class);
        // Old code. The two lines below are replaced by the following line.  The addInstanceOnAllHosts action throws an exception if the host is not localhost. 
        // The addInstance action now handles any missing host as well as a host passed in as a key/value pair. kib 20110622
        //		restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/{name:MApplication}/addInstance", ERXRoute.Method.Get, MApplicationController.class, "addInstanceOnAllHosts"));
        //		restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/{name:MApplication}/addInstance/{host:MHost}", ERXRoute.Method.Get, MApplicationController.class, "addInstance"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/{name:MApplication}/addInstance", ERXRoute.Method.Get, MApplicationController.class, "addInstance"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/{name:MApplication}/deleteInstance", ERXRoute.Method.Get, MApplicationController.class, "deleteInstance"));
        restHandler.addDefaultRoutes("MHost", false, MHostController.class);
        restHandler.addDefaultRoutes("MSiteConfig", false, MSiteConfigController.class);
        restHandler.insertRoute(new ERXRoute("MSiteConfig","/mSiteConfig", ERXRoute.Method.Put, MSiteConfigController.class, "update"));

        ERXRouteRequestHandler.register(restHandler);
    }

    @Override
    public NSMutableDictionary handleMalformedCookieString(RuntimeException arg0, String arg1, NSMutableDictionary arg2) {
        NSLog.err.appendln("Malformed cookies: " + arg1);
        return arg2 == null ? new NSMutableDictionary() : arg2;
    }
    
    public MSiteConfig _siteConfig() {
        return WOTaskdHandler.siteConfig();
    }
}
