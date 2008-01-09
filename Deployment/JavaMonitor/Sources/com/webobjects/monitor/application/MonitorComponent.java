package com.webobjects.monitor.application;

/*
 © Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

 IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (“Apple”) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

 In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple’s copyrights in this original Apple software (the “Apple Software”), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
 SUCH DAMAGE.
 */
import java.util.Enumeration;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.xml.WOXMLException;
import com.webobjects.appserver.xml._JavaMonitorDecoder;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.String_Extensions;

public class MonitorComponent extends WOComponent {

    private static final long serialVersionUID = -1880897151494772932L;

    public final int APP_PAGE = 0;

    public final int HOST_PAGE = 1;

    public final int SITE_PAGE = 2;

    public final int PREF_PAGE = 3;

    public final int HELP_PAGE = 4;

    public final int MIGRATION_PAGE = 5;

    public Application theApplication = (Application) WOApplication.application();

    private WOTaskdHandler _handler;

    public MonitorComponent(WOContext aWocontext) {
        super(aWocontext);
        _handler = new WOTaskdHandler(mySession());
    }

    public Session mySession() {
        return (Session) super.session();
    }

    public WOTaskdHandler handler() {
        return _handler;
    }

    public WOComponent pageWithName(String aName) {
        theApplication._lock.startReading();
        try {
            _cacheState(aName);
        } finally {
            theApplication._lock.endReading();
        }
        return super.pageWithName(aName);
    }

    // KH - we should probably set the instance information as we get the
    // responses, to avoid waiting, then doing it in serial! (not that it's
    // _that_ slow)
    private void _cacheState(String aName) {
        MApplication appForDetailPage = mySession().mApplication;

        if (theApplication.siteConfig().hostArray().count() != 0) {
            if (aName.equals("ApplicationsPage") && (theApplication.siteConfig().applicationArray().count() != 0)) {

                for (Enumeration e = theApplication.siteConfig().applicationArray().objectEnumerator(); e
                        .hasMoreElements();) {
                    MApplication anApp = (MApplication) e.nextElement();
                    anApp.runningInstancesCount = MObject._zeroInteger;
                }

                WOResponse[] responses = handler().sendQueryToWotaskds("APPLICATION",
                        theApplication.siteConfig().hostArray());

                NSMutableArray errorArray = new NSMutableArray();
                NSDictionary applicationResponseDictionary;
                NSDictionary queryResponseDictionary;
                NSArray responseArray = null;
                NSDictionary responseDictionary = null;
                for (int i = 0; i < responses.length; i++) {
                    if ((responses[i] == null) || (responses[i].content() == null)) {
                        queryResponseDictionary = handler().emptyResponse;
                    } else {
                        try {
                            queryResponseDictionary = (NSDictionary) new _JavaMonitorDecoder()
                                    .decodeRootObject(responses[i].content());
                        } catch (WOXMLException wxe) {
                            NSLog.err
                                    .appendln("MonitorComponent pageWithName(ApplicationsPage) Error decoding response: "
                                            + responses[i].contentString());
                            queryResponseDictionary = handler().responseParsingFailed;
                        }
                    }
                    handler().getGlobalErrorFromResponse(queryResponseDictionary, errorArray);

                    applicationResponseDictionary = (NSDictionary) queryResponseDictionary
                            .valueForKey("queryWotaskdResponse");
                    if (applicationResponseDictionary != null) {
                        responseArray = (NSArray) applicationResponseDictionary.valueForKey("applicationResponse");
                        if (responseArray != null) {
                            for (int j = 0; j < responseArray.count(); j++) {
                                responseDictionary = (NSDictionary) responseArray.objectAtIndex(j);
                                String appName = (String) responseDictionary.valueForKey("name");
                                Integer runningInstances = (Integer) responseDictionary.valueForKey("runningInstances");
                                MApplication anApplication = theApplication.siteConfig().applicationWithName(appName);
                                if (anApplication != null) {
                                    // KH - this is massively suboptimal
                                    anApplication.runningInstancesCount = new Integer(
                                            anApplication.runningInstancesCount.intValue()
                                                    + runningInstances.intValue());
                                }
                            }
                        }
                    }
                } // for
                if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
                    NSLog.debug.appendln("##### pageWithName(ApplicationsPage) errors: " + errorArray);
                mySession().addObjectsFromArrayIfAbsentToErrorMessageArray(errorArray);
            } else if (aName.equals("AppDetailPage")) {
                NSArray hostArray = appForDetailPage.hostArray();
                if (hostArray.count() != 0) {

                    WOResponse[] responses = handler().sendQueryToWotaskds("INSTANCE", hostArray);

                    NSMutableArray errorArray = new NSMutableArray();
                    NSArray responseArray = null;
                    NSDictionary responseDictionary = null;
                    NSDictionary queryResponseDictionary = null;
                    for (int i = 0; i < responses.length; i++) {
                        if ((responses[i] == null) || (responses[i].content() == null)) {
                            responseDictionary = handler().emptyResponse;
                        } else {
                            try {
                                responseDictionary = (NSDictionary) new _JavaMonitorDecoder()
                                        .decodeRootObject(responses[i].content());
                            } catch (WOXMLException wxe) {
                                NSLog.err
                                        .appendln("MonitorComponent pageWithName(AppDetailPage) Error decoding response: "
                                                + responses[i].contentString());
                                responseDictionary = handler().responseParsingFailed;
                            }
                        }
                        handler().getGlobalErrorFromResponse(responseDictionary, errorArray);

                        queryResponseDictionary = (NSDictionary) responseDictionary.valueForKey("queryWotaskdResponse");
                        if (queryResponseDictionary != null) {
                            responseArray = (NSArray) queryResponseDictionary.valueForKey("instanceResponse");
                            if (responseArray != null) {
                                for (int j = 0; j < responseArray.count(); j++) {
                                    responseDictionary = (NSDictionary) responseArray.objectAtIndex(j);

                                    String host = (String) responseDictionary.valueForKey("host");
                                    Integer port = (Integer) responseDictionary.valueForKey("port");
                                    String runningState = (String) responseDictionary.valueForKey("runningState");
                                    Boolean refusingNewSessions = (Boolean) responseDictionary
                                            .valueForKey("refusingNewSessions");
                                    NSDictionary statistics = (NSDictionary) responseDictionary
                                            .valueForKey("statistics");
                                    NSArray deaths = (NSArray) responseDictionary.valueForKey("deaths");
                                    String nextShutdown = (String) responseDictionary.valueForKey("nextShutdown");

                                    MInstance anInstance = theApplication.siteConfig().instanceWithHostnameAndPort(
                                            host, port);
                                    if (anInstance != null) {
                                        for (int k = 0; k < MObject.stateArray.length; k++) {
                                            if (MObject.stateArray[k].equals(runningState)) {
                                                anInstance.state = k;
                                                break;
                                            }
                                        }
                                        anInstance.isRefusingNewSessions = String_Extensions
                                                .boolValue(refusingNewSessions);
                                        anInstance.setStatistics(statistics);
                                        anInstance.setDeaths(new NSMutableArray(deaths));
                                        anInstance.setNextScheduledShutdownString_M(nextShutdown);
                                    }
                                }
                            }
                        }
                    } // For Loop
                    if (NSLog
                            .debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
                        NSLog.debug.appendln("##### pageWithName(AppDetailPage) errors: " + errorArray);
                    mySession().addObjectsFromArrayIfAbsentToErrorMessageArray(errorArray);
                }
            } else if (aName.equals("HostsPage")) {
                WOResponse[] responses = handler().sendQueryToWotaskds("HOST", theApplication.siteConfig().hostArray());

                NSMutableArray errorArray = new NSMutableArray();
                NSDictionary responseDict = null;
                for (int i = 0; i < responses.length; i++) {
                    MHost aHost = (MHost) theApplication.siteConfig().hostArray().objectAtIndex(i);

                    if ((responses[i] == null) || (responses[i].content() == null)) {
                        responseDict = handler().emptyResponse;
                    } else {
                        try {
                            responseDict = (NSDictionary) new _JavaMonitorDecoder().decodeRootObject(responses[i]
                                    .content());
                        } catch (WOXMLException wxe) {
                            NSLog.err.appendln("MonitorComponent pageWithName(HostsPage) Error decoding response: "
                                    + responses[i].contentString());
                            responseDict = handler().responseParsingFailed;
                        }
                    }
                    handler().getGlobalErrorFromResponse(responseDict, errorArray);

                    NSDictionary queryResponse = (NSDictionary) responseDict.valueForKey("queryWotaskdResponse");
                    if (queryResponse != null) {
                        NSDictionary hostResponse = (NSDictionary) queryResponse.valueForKey("hostResponse");
                        aHost._setHostInfo(hostResponse);
                        aHost.isAvailable = true;
                    } else {
                        aHost.isAvailable = false;
                    }
                } // for
                if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
                    NSLog.debug.appendln("##### pageWithName(HostsPage) errors: " + errorArray);
                mySession().addObjectsFromArrayIfAbsentToErrorMessageArray(errorArray);
            }
        }
    }

}
