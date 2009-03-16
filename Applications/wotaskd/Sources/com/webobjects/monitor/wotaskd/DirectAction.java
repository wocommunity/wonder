package com.webobjects.monitor.wotaskd;
/*
© Copyright 2006 - 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (“Apple”) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple’s copyrights in this original Apple software (the “Apple Software”), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */

import java.util.Enumeration;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOHostUtilities;
import com.webobjects.appserver.xml.WOXMLException;
import com.webobjects.appserver.xml._JavaMonitorCoder;
import com.webobjects.appserver.xml._JavaMonitorDecoder;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;
import com.webobjects.foundation._NSThreadsafeMutableDictionary;
import com.webobjects.foundation._NSUtilities;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.MSiteConfig;
import com.webobjects.monitor._private.MonitorException;

public class DirectAction extends WODirectAction  {
    private NSMutableDictionary hostResponse;
    private NSDictionary element;

    static private String _hostName;
    static private Object[] hostQueryKeys;
    static private Object[] appQueryKeys;
    static private Object[] instanceQueryKeys;
    static private NSDictionary successElement;
    static private Object[] errorKeys;
    static private String _accessDenied;
    static private String _invalidPassword;
    static private String _invalidXML;
    static private String _emptyXML;
    static private NSDictionary _argumentNumberCommandError;
    static private NSTimestampFormatter aFormat = null;

    static {
        // get the hostname for the error messages
        _hostName = WOApplication.application().host();

        // pre-cache dictionary keys
        hostQueryKeys = new Object[]{"runningInstances", "processorType", "operatingSystem"};
        appQueryKeys = new Object[]{"name", "runningInstances"};
        instanceQueryKeys = new Object[]{"applicationName", "id", "host", "port", "runningState", "refusingNewSessions", "statistics", "deaths", "nextShutdown"};
        successElement = new NSDictionary(new Object[]{Boolean.TRUE}, new Object[]{"success"});
        errorKeys = new Object[]{"success", "errorMessage"};
        
        // Pre-cache error messages
        _accessDenied = (new _JavaMonitorCoder()).encodeRootObjectForKey(new NSDictionary(new NSArray(_hostName + ": wotaskd may not be accessed through a Web server - Access Denied"), "errorResponse"), "monitorResponse");
        _invalidPassword = (new _JavaMonitorCoder()).encodeRootObjectForKey(new NSDictionary(new NSArray(_hostName + ": Invalid Password - Access Denied"), "errorResponse"), "monitorResponse");
        _invalidXML = (new _JavaMonitorCoder()).encodeRootObjectForKey(new NSDictionary(new NSArray(_hostName + " - INTERNAL ERROR: Request from Monitor was Invalid"), "errorResponse"), "monitorResponse");
        _emptyXML = (new _JavaMonitorCoder()).encodeRootObjectForKey(new NSDictionary(new NSArray(_hostName + " - INTERNAL ERROR: Request from Monitor was Empty"), "errorResponse"), "monitorResponse");
        _argumentNumberCommandError = new NSDictionary(new Object[]{Boolean.FALSE, _hostName + " - INTERNAL ERROR: Not enough elements: Need 'commandString' + 'arrayOfInstances'"}, errorKeys);

        // get the formatter setup
        aFormat = new NSTimestampFormatter("%a, %d %b %Y %H:%M:%S GMT");
        aFormat.setDefaultFormatTimeZone(NSTimeZone.timeZoneWithName("GMT", true));
    }
    
    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }


    // This is the biggie - this processes all requests from Monitor
    public WOActionResults monitorRequestAction() {
        Application theApplication = (Application) WOApplication.application();
        MSiteConfig aConfig = theApplication.siteConfig();

        WORequest aRequest = request();
        WOResponse aResponse = theApplication.createResponseInContext(null);

        // Aren't allowed to call this through the Web server.
        if (aRequest.isUsingWebServer()) {
            NSLog.debug.appendln("Attempt to call DirectAction: monitorRequestAction through Web server");
            if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelCritical, NSLog.DebugGroupDeployment))
                NSLog.debug.appendln(aRequest.contentString());
            aResponse.setStatus(WOMessage.HTTP_STATUS_FORBIDDEN);
            aResponse.appendContentString(_accessDenied);
            return aResponse;
        }

        // Checking to see if the password was corrent
        theApplication._lock.startReading();
        try {
            String passwordHeader = aRequest.headerForKey("password");
            if (!aConfig.comparePasswordWithPassword(passwordHeader)) {
                NSLog.debug.appendln("Attempt to call DirectAction: monitorRequestAction with incorrect password.");
                aResponse.setStatus(WOMessage.HTTP_STATUS_FORBIDDEN);
                aResponse.appendContentString(_invalidPassword);
                // we endReading at the finally block
                return aResponse;
            }
        } finally {
            theApplication._lock.endReading();
        }

        NSDictionary requestDict;
        try {
            requestDict = (NSDictionary) new _JavaMonitorDecoder().decodeRootObject(aRequest.content());
        } catch (WOXMLException wxe) {
            NSLog.err.appendln("Wotaskd monitorRequestAction: Error parsing request");
            if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDeployment))
                NSLog.debug.appendln("Wotaskd monitorRequestAction: " + aRequest.contentString());
            aResponse.appendContentString(_invalidXML);
            return aResponse;
        }

        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("\n@@@@@ monitorRequestAction received request from Monitor");
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("@@@@@ monitorRequestAction requestDict: " + requestDict + "\n");

        // These 2 get used for everything else - the global response object and the global error object.
        NSMutableDictionary monitorResponse = new NSMutableDictionary();
        NSMutableArray errorResponse = new NSMutableArray();

        NSDictionary updateWotaskdDict = (NSDictionary) requestDict.valueForKey("updateWotaskd");
        NSArray commandWotaskdArray = (NSArray) requestDict.valueForKey("commandWotaskd");
        String queryWotaskdString = (String) requestDict.valueForKey("queryWotaskd");

        // Checking for Updates
        if (updateWotaskdDict != null) {
            theApplication._lock.startWriting();
            try {
                NSMutableDictionary updateWotaskdResponse = new NSMutableDictionary(2);

                String clearString = (String) updateWotaskdDict.valueForKey("clear");
                NSDictionary overwriteDict = (NSDictionary) updateWotaskdDict.valueForKey("overwrite");
                NSDictionary syncDict = (NSDictionary) updateWotaskdDict.valueForKey("sync");
                NSDictionary removeDict = (NSDictionary) updateWotaskdDict.valueForKey("remove");
                NSDictionary addDict = (NSDictionary) updateWotaskdDict.valueForKey("add");
                NSDictionary configureDict = (NSDictionary) updateWotaskdDict.valueForKey("configure");

                if (clearString != null) {
                    stopAllInstances();
                    ( (Application) WOApplication.application()).setSiteConfig(new MSiteConfig(null));
                    updateWotaskdResponse.takeValueForKey(successElement, "clear");
                } else if (overwriteDict != null) {
                    stopAllInstances();
                    ( (Application) WOApplication.application()).setSiteConfig(new MSiteConfig((NSDictionary) overwriteDict.valueForKey("SiteConfig")));
                    updateWotaskdResponse.takeValueForKey(successElement, "overwrite");
                } else if (syncDict != null) {
                    NSDictionary newConfig = (NSDictionary) syncDict.valueForKey("SiteConfig");
                    syncSiteConfig(newConfig);
                } else {
                    if (removeDict != null) {
                        NSMutableDictionary removeResponse = new NSMutableDictionary(1);

                        NSArray hostArray = (NSArray) removeDict.valueForKey("hostArray");
                        NSArray applicationArray = (NSArray) removeDict.valueForKey("applicationArray");
                        NSArray instanceArray = (NSArray) removeDict.valueForKey("instanceArray");

                        if (hostArray != null) {
                            NSMutableArray hostArrayResponse = new NSMutableArray(hostArray.count());

                            // update-remove - for each host listed - hostWithName + (stopAllInstances/new siteConfig) | removeHost_W
                            for (Enumeration e = hostArray.objectEnumerator(); e.hasMoreElements(); ) {
                                NSDictionary aHost = (NSDictionary) e.nextElement();
                                String name = (String) aHost.valueForKey("name");
                                MHost anMHost = aConfig.hostWithName(name);
                                if (anMHost == null) {
                                    element = new NSDictionary(new Object[]{Boolean.FALSE, _hostName + ": Host " +name+ " not found; REMOVE failed"}, errorKeys);
                                    hostArrayResponse.addObject(element);
                                } else {
                                    if ( anMHost == aConfig.localHost()) {
                                        stopAllInstances();
                                        ( (Application) WOApplication.application()).setSiteConfig(new MSiteConfig(null));
                                    } else {
                                        aConfig.removeHost_W(anMHost);
                                    }
                                    hostArrayResponse.addObject(successElement);
                                }
                            }
                            removeResponse.takeValueForKey(hostArrayResponse, "hostArray");
                        }
                        if (applicationArray != null) {
                            NSMutableArray applicationArrayResponse = new NSMutableArray(applicationArray.count());

                            // update-remove - for each application listed - applicationWithName + removeApplication_W
                            for (Enumeration e = applicationArray.objectEnumerator(); e.hasMoreElements(); ) {
                                NSDictionary anApp = (NSDictionary) e.nextElement();
                                String name = (String) anApp.valueForKey("name");
                                MApplication anMApplication = aConfig.applicationWithName(name);
                                if (anMApplication == null) {
                                    element = new NSDictionary(new Object[]{Boolean.FALSE, _hostName + ": Application " +name+ " not found; REMOVE failed"}, errorKeys);
                                    applicationArrayResponse.addObject(element);
                                } else {
                                    aConfig.removeApplication_W(aConfig.applicationWithName(name));
                                    applicationArrayResponse.addObject(successElement);
                                }
                            }
                            removeResponse.takeValueForKey(applicationArrayResponse, "applicationArray");
                        }
                        if (instanceArray != null) {
                            NSMutableArray instanceArrayResponse = new NSMutableArray(instanceArray.count());

                            // update-remove - for each instance listed - instanceWithHostnameAndPort + removeInstance_W
                            for (Enumeration e = instanceArray.objectEnumerator(); e.hasMoreElements(); ) {
                                NSDictionary anInst = (NSDictionary) e.nextElement();
                                String hostName = (String) anInst.valueForKey("hostName");
                                Integer port = (Integer) anInst.valueForKey("port");
                                MInstance anMInstance = aConfig.instanceWithHostnameAndPort(hostName, port);
                                if (anMInstance == null) {
                                    element = new NSDictionary(new Object[]{Boolean.FALSE, _hostName + ": Instance " +hostName+"-"+port+ " not found; REMOVE failed"}, errorKeys);
                                    instanceArrayResponse.addObject(element);
                                } else {
                                    aConfig.removeInstance_W(anMInstance);
                                    instanceArrayResponse.addObject(successElement);
                                }
                            }
                            removeResponse.takeValueForKey(instanceArrayResponse, "instanceArray");
                        }
                        updateWotaskdResponse.takeValueForKey(removeResponse, "remove");
                    }

                    if (addDict != null) {
                        NSMutableDictionary addResponse = new NSMutableDictionary(1);

                        NSArray hostArray = (NSArray) addDict.valueForKey("hostArray");
                        NSArray applicationArray = (NSArray) addDict.valueForKey("applicationArray");
                        NSArray instanceArray = (NSArray) addDict.valueForKey("instanceArray");

                        if (hostArray != null) {
                            NSMutableArray hostArrayResponse = new NSMutableArray(hostArray.count());

                            // update-add - for each host listed - addHost_W
                            for (Enumeration e = hostArray.objectEnumerator(); e.hasMoreElements(); ) {
                                NSDictionary aHost = (NSDictionary) e.nextElement();
                                aConfig.addHost_W(new MHost(aHost, aConfig));
                                hostArrayResponse.addObject(successElement);
                            }
                            addResponse.takeValueForKey(hostArrayResponse, "hostArray");
                        }
                        if (applicationArray != null) {
                            NSMutableArray applicationArrayResponse = new NSMutableArray(applicationArray.count());

                            // update-add - for each application listed - addApplication_W
                            for (Enumeration e = applicationArray.objectEnumerator(); e.hasMoreElements(); ) {
                                NSDictionary anApp = (NSDictionary) e.nextElement();
                                aConfig.addApplication_W(new MApplication(anApp, aConfig));
                                applicationArrayResponse.addObject(successElement);
                            }
                            addResponse.takeValueForKey(applicationArrayResponse, "applicationArray");
                        }
                        if (instanceArray != null) {
                            NSMutableArray instanceArrayResponse = new NSMutableArray(instanceArray.count());

                            //  update-add - for each instance listed - addInstance_W
                            for (Enumeration e = instanceArray.objectEnumerator(); e.hasMoreElements(); ) {
                                NSDictionary anInst = (NSDictionary) e.nextElement();
                                aConfig.addInstance_W(new MInstance(anInst, aConfig));
                                instanceArrayResponse.addObject(successElement);
                            }
                            addResponse.takeValueForKey(instanceArrayResponse, "instanceArray");
                        }
                        updateWotaskdResponse.takeValueForKey(addResponse, "add");
                    }

                    if (configureDict != null) {
                        NSMutableDictionary configureResponse = new NSMutableDictionary(2);

                        NSDictionary siteDict = (NSDictionary) configureDict.valueForKey("site");
                        NSArray hostArray = (NSArray) configureDict.valueForKey("hostArray");
                        NSArray applicationArray = (NSArray) configureDict.valueForKey("applicationArray");
                        NSArray instanceArray = (NSArray) configureDict.valueForKey("instanceArray");

                        if (siteDict != null) {
                            // update-configure - siteConfig.updateValues
                            aConfig.updateValues(siteDict);
                            configureResponse.takeValueForKey(successElement, "site");
                        }
                        if (hostArray != null) {
                            NSMutableArray hostArrayResponse = new NSMutableArray(hostArray.count());

                            // update-configure - for each host listed - hostWithName + updateValues
                            for (Enumeration e = hostArray.objectEnumerator(); e.hasMoreElements(); ) {
                                NSDictionary aHost = (NSDictionary) e.nextElement();
                                String name = (String) aHost.valueForKey("name");
                                MHost anMHost = aConfig.hostWithName(name);
                                if (anMHost == null) {
                                    element = new NSDictionary(new Object[]{Boolean.FALSE, _hostName + ": Host " +name+ " not found; UPDATE failed"}, errorKeys);
                                    hostArrayResponse.addObject(element);
                                } else {
                                    anMHost.updateValues(aHost);
                                    hostArrayResponse.addObject(successElement);
                                }
                            }
                            configureResponse.takeValueForKey(hostArrayResponse, "hostArray");
                        }
                        if (applicationArray != null) {
                            NSMutableArray applicationArrayResponse = new NSMutableArray(applicationArray.count());

                            // update-configure - for each application listed - applicationWithName + updateValues
                            for (Enumeration e = applicationArray.objectEnumerator(); e.hasMoreElements(); ) {
                                NSDictionary anApp = (NSDictionary) e.nextElement();
                                String name = (String) anApp.valueForKey("name");
                                MApplication anMApplication = aConfig.applicationWithName(name);
                                // if I can't find the application, I might be updating the name - in that case, look under the oldname.
                                if (anMApplication == null) {
                                    name = (String) anApp.valueForKey("oldname");
                                    anMApplication = aConfig.applicationWithName(name);
                                }

                                if (anMApplication == null) {
                                    element = new NSDictionary(new Object[]{Boolean.FALSE, _hostName + ": Application " +name+ " not found; UPDATE failed"}, errorKeys);
                                    applicationArrayResponse.addObject(element);
                                } else {
                                    anMApplication.updateValues(anApp);
                                    applicationArrayResponse.addObject(successElement);
                                }
                            }
                            configureResponse.takeValueForKey(applicationArrayResponse, "applicationArray");
                        }
                        if (instanceArray != null) {
                            NSMutableArray instanceArrayResponse = new NSMutableArray(instanceArray.count());

                            // update-configure - for each instance listed - instanceWithHostnameAndPort + updateValues
                            for (Enumeration e = instanceArray.objectEnumerator(); e.hasMoreElements(); ) {
                                NSDictionary anInst = (NSDictionary) e.nextElement();
                                String hostName = (String) anInst.valueForKey("hostName");
                                Integer port = (Integer) anInst.valueForKey("port");
                                MInstance anMInstance = aConfig.instanceWithHostnameAndPort(hostName, port);
                                // if I can't find the instance, I might be updating the port - in that case, look under the oldport number.
                                if (anMInstance == null) {
                                    port = (Integer) anInst.valueForKey("oldport");
                                    anMInstance = aConfig.instanceWithHostnameAndPort(hostName, port);
                                }
                                if (anMInstance == null) {
                                    element = new NSDictionary(new Object[]{Boolean.FALSE, _hostName + ": Instance " +hostName+"-"+port+ " not found; UPDATE failed"}, errorKeys);
                                    instanceArrayResponse.addObject(element);
                                } else {
                                    anMInstance.updateValues(anInst);
                                    instanceArrayResponse.addObject(successElement);
                                }
                            }
                            configureResponse.takeValueForKey(instanceArrayResponse, "instanceArray");
                        }
                        updateWotaskdResponse.takeValueForKey(configureResponse, "configure");
                    }
                }
                monitorResponse.takeValueForKey(updateWotaskdResponse, "updateWotaskdResponse");
            } finally {
                theApplication._lock.endWriting();
            }
        }


        // Checking for Commands
        if (commandWotaskdArray != null) {
            int instArrayCount = commandWotaskdArray.count();
            NSMutableArray commandWotaskdResponse = new NSMutableArray(instArrayCount);

            if (instArrayCount < 2) {
                commandWotaskdResponse.addObject(_argumentNumberCommandError);
            } else {
                String command = (String) commandWotaskdArray.objectAtIndex(0);

                if ( (command.equals("START")) || (command.equals("CLEAR")) ||
                     (command.equals("STOP"))  || (command.equals("REFUSE")) ||
                     (command.equals("ACCEPT"))|| (command.equals("QUIT")) ) {
                    commandWotaskdResponse.addObject(successElement);
                } else {
                    element = new NSDictionary(new Object[]{Boolean.FALSE, _hostName + " - INTERNAL ERROR: Invalid Command " + command}, errorKeys);
                    commandWotaskdResponse.addObject(element);
                }

                // Go through each instance and do whatever it is that we do
                for (int i=1; i<instArrayCount; i++) {
                    NSDictionary instDict = (NSDictionary) commandWotaskdArray.objectAtIndex(i);
                    String hostName = (String) instDict.valueForKey("hostName");
                    Integer port = (Integer) instDict.valueForKey("port");
                    theApplication._lock.startReading();
                    try {
                        MInstance anInstance = aConfig.instanceWithHostnameAndPort(hostName, port);
                        if (anInstance != null) {
                            if (anInstance.isLocal_W()) {
                                if (command.equals("START")) {
                                    String errorMsg = theApplication.localMonitor().startInstance(anInstance);
                                    if (errorMsg != null) {
                                        element = new NSDictionary(new Object[]{Boolean.FALSE, errorMsg}, errorKeys);
                                        commandWotaskdResponse.addObject(element);
                                    }
                                } else if (command.equals("CLEAR")) {
                                    anInstance.removeAllDeaths();
                                    commandWotaskdResponse.addObject(successElement);
                                } else {
                                    try {
                                        if (command.equals("STOP")) {
                                            theApplication.localMonitor().terminateInstance(anInstance);
                                        } else if (command.equals("REFUSE")) {
                                            theApplication.localMonitor().stopInstance(anInstance);
                                        } else if (command.equals("ACCEPT")) {
                                            theApplication.localMonitor().setAcceptInstance(anInstance);
                                        } else if (command.equals("QUIT")) {
                                            anInstance.setShouldDie(true);
                                        }
                                        commandWotaskdResponse.addObject(successElement);
                                    } catch (MonitorException me) {
                                        element = new NSDictionary(new Object[]{Boolean.FALSE, me.getMessage()}, errorKeys);
                                        commandWotaskdResponse.addObject(element);
                                    }
                                }
                            } else {
                                //element = new NSDictionary(new Object[]{Boolean.FALSE, anInstance.displayName() + " does not exist on " + _hostName + "; " + command + " failed"}, errorKeys);
                                //commandWotaskdResponse.addObject(element);
                                commandWotaskdResponse.addObject(successElement);
                            }
                        } else {
                            element = new NSDictionary(new Object[]{Boolean.FALSE, _hostName + ": No instance found for Host " + hostName + " and Port: " + port + "; " + command + " failed"}, errorKeys);
                            commandWotaskdResponse.addObject(element);
                        }
                    } finally {
                        theApplication._lock.endReading();
                    }
                }
            }
            monitorResponse.takeValueForKey(commandWotaskdResponse, "commandWotaskdResponse");
        }

        
        // Checking for a Query
        if (queryWotaskdString != null) {
            NSMutableDictionary queryWotaskdResponse = new NSMutableDictionary(1);

            if (queryWotaskdString.equals("SITE")) {
                theApplication._lock.startReading();
                try {
                    queryWotaskdResponse.takeValueForKey(aConfig.dictionaryForArchive(), "SiteConfig");
                } finally {
                    theApplication._lock.endReading();
                }
            } else if (queryWotaskdString.equals("HOST")) {
                // query - host.runningInstancesCount_W
                if (hostResponse == null) {
                    Integer runningInstances = new Integer(0);
                    String processorType = System.getProperties().getProperty("os.arch");
                    String operatingSystem = System.getProperties().getProperty("os.name") + " " + System.getProperties().getProperty("os.version");

                    hostResponse = new NSMutableDictionary(new Object[]{runningInstances, processorType, operatingSystem}, hostQueryKeys);
                }
                theApplication._lock.startReading();
                try {
                    if (aConfig.localHost() != null) {
                        hostResponse.takeValueForKey(aConfig.localHost().runningInstancesCount_W(), "runningInstances");
                    } else {
                        hostResponse.takeValueForKey(_NSUtilities.IntegerForInt(0), "runningInstances");
                    }
                } finally {
                    theApplication._lock.endReading();
                }
                queryWotaskdResponse.takeValueForKey(hostResponse, "hostResponse");
            } else if (queryWotaskdString.equals("APPLICATION")) {
                NSMutableArray applicationResponse = null;
                theApplication._lock.startReading();
                try {
                    NSArray appArray = aConfig.applicationArray();
                    int appArrayCount = appArray.count();
                    MApplication anApp;
                    String name;
                    Integer runningInstances;
                    NSDictionary elementApp;

                    applicationResponse = new NSMutableArray(appArrayCount);

                    // query - for each application - runningInstancesCount_W();
                    for (int i=0; i<appArrayCount; i++) {
                        anApp = (MApplication) appArray.objectAtIndex(i);
                        name = anApp.name();
                        runningInstances = anApp.runningInstancesCount_W();
                        elementApp = new NSDictionary(new Object[]{name, runningInstances}, appQueryKeys);
                        applicationResponse.addObject(elementApp);
                    }
                } finally {
                    theApplication._lock.endReading();
                }

                queryWotaskdResponse.takeValueForKey(applicationResponse, "applicationResponse");
            } else if (queryWotaskdString.equals("INSTANCE")) {
                NSMutableArray instanceResponse = null;
                theApplication._lock.startReading();
                try {
                    NSArray instanceArray = (aConfig.localHost() != null) ? aConfig.localHost().instanceArray() : NSArray.EmptyArray;
                    int instanceArrayCount = instanceArray.count();

                    MInstance anInstance;
                    String applicationName;
                    Integer id;
                    String host;
                    Integer port;
                    String runningState;
                    Boolean refusingNewSessions;
                    NSDictionary statistics;
                    NSArray deaths;
                    String nextShutdown;
                    NSDictionary elementInst;

                    instanceResponse = new NSMutableArray(instanceArrayCount);

                    NSMutableArray runningInstanceArray = new NSMutableArray();
                    for (Enumeration e = instanceArray.objectEnumerator(); e.hasMoreElements(); ) {
                        MInstance anInst = (MInstance) e.nextElement();
                        if (anInst.isRunning_W()) {
                            runningInstanceArray.addObject(anInst);
                        }
                    }
                    getStatisticsForInstanceArray(runningInstanceArray, errorResponse);

                    for (int i=0; i<instanceArrayCount; i++) {
                        anInstance = (MInstance) instanceArray.objectAtIndex(i);

                        String error = anInstance.statisticsError();
                        if (error != null) {
                            errorResponse.addObject(error);
                        }
                        // Continue, because wotaskd is expecting a response here.

                        applicationName = anInstance.applicationName();
                        id = anInstance.id();
                        host = anInstance.hostName();
                        port = anInstance.port();
                        runningState = MObject.stateArray[anInstance.state];
                        statistics = anInstance.statistics();
                        refusingNewSessions = (anInstance.isRefusingNewSessions()) ? Boolean.TRUE : Boolean.FALSE;
                        deaths = anInstance.deaths();
                        nextShutdown = anInstance.nextScheduledShutdownString();

                        elementInst = new NSDictionary(new Object[]{applicationName, id, host, port, runningState, refusingNewSessions, statistics, deaths, nextShutdown}, instanceQueryKeys);
                        instanceResponse.addObject(elementInst);
                    }
                } finally {
                    theApplication._lock.endReading();
                }

                queryWotaskdResponse.takeValueForKey(instanceResponse, "instanceResponse");
            } else {
                errorResponse.addObject(_hostName + ": Unrecognized Query: " + queryWotaskdString);
            }
            monitorResponse.takeValueForKey(queryWotaskdResponse, "queryWotaskdResponse");
        }

        // getting the errors
        NSArray globalArray = theApplication.siteConfig().globalErrorDictionary.allValues();
        if ( (globalArray != null) && (globalArray.count() > 0) ) {
            errorResponse.addObjectsFromArray(globalArray);
            theApplication.siteConfig().globalErrorDictionary = new _NSThreadsafeMutableDictionary(new NSMutableDictionary());
        }
        if (errorResponse.count() != 0) {
            monitorResponse.takeValueForKey(errorResponse, "errorResponse");
        }

        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("@@@@@ monitorRequestAction returning response to Monitor");
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("@@@@@ monitorRequestAction responseDict: " + monitorResponse + "\n");
        aResponse.appendContentString((new _JavaMonitorCoder()).encodeRootObjectForKey(monitorResponse, "monitorResponse"));
        return aResponse;
    }

    private void getStatisticsForInstanceArray(NSArray instArray, NSMutableArray errorResponse) {
        final LocalMonitor localMonitor = ((Application) WOApplication.application()).localMonitor();

        final NSArray instanceArray = instArray;
        int theCount = instanceArray.count();

        if (theCount == 0) return;

        Thread[] workers = new Thread[theCount];
        final WOResponse[] responses = new WOResponse[theCount];

        for (int i=0; i<theCount; i++) {
            final int j = i;
            Runnable work = new Runnable() {
                public void run() {
                    try {
                        responses[j] = localMonitor.queryInstance((MInstance) instanceArray.objectAtIndex(j));
                    } catch (MonitorException me) {
                        MInstance badInstance = ((MInstance) instanceArray.objectAtIndex(j));
                        if ( (!badInstance.isRunning_W()) &&
                             (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelCritical, NSLog.DebugGroupDeployment)) ) {
                            NSLog.debug.appendln("Exception getting Statistics for instance: " + ((MInstance) instanceArray.objectAtIndex(j)).displayName());
                        }
                        responses[j] =  null;
                    }
                }
            };
            workers[j] = new Thread(work);
            workers[j].start();
        }

        try {
            for (int i=0; i<theCount; i++) {
                workers[i].join();
            }
        } catch (InterruptedException ie) {}

        for (int i=0; i<theCount; i++) {
            WOResponse aResponse = responses[i];
            if (aResponse != null) {
                MInstance anInstance = (MInstance) instArray.objectAtIndex(i);
                anInstance.updateRegistration(new NSTimestamp());
                if (aResponse.headerForKey("x-webobjects-refusenewsessions") != null) {
                    anInstance.setRefusingNewSessions(true);
                } else {
                    anInstance.setRefusingNewSessions(false);
                }

                NSDictionary instanceResponse = null;
                NSData responseContent = aResponse.content();
                try {
                    instanceResponse = (NSDictionary) new _JavaMonitorDecoder().decodeRootObject(responseContent);
                } catch (WOXMLException wxe) {
                    try {
                        Object o = NSPropertyListSerialization.propertyListFromString(new String(responseContent.bytes()));
                        errorResponse.addObject(anInstance.displayName() + " is probably an older application that doesn't conform to the current Monitor Protocol. Please update and restart the instance.");
                        NSLog.err.appendln("Got old-style response from instance: " + anInstance.displayName());
                    } catch (Throwable t) {
                        NSLog.err.appendln("Wotaskd getStatisticsForInstanceArray: Error parsing: " + new String(responseContent.bytes()) + " from " + anInstance.displayName());
                    }
                    continue;
                } catch (NullPointerException npe) {
                    NSLog.err.appendln("Wotaskd getStatisticsForInstanceArray: No content returned from " + anInstance.displayName());
                    continue;
                }

                NSArray queryInstanceError = (NSArray) instanceResponse.valueForKey("errorResponse");
                if (queryInstanceError != null) {
                    anInstance.setStatisticsError(queryInstanceError.componentsJoinedByString(", "));
                    continue;
                }

                String queryInstanceResponse = (String) instanceResponse.valueForKey("queryInstanceResponse");
                if (queryInstanceResponse == null) continue;

                try {
                    NSDictionary statistics = (NSDictionary) NSPropertyListSerialization.propertyListFromString(queryInstanceResponse);

                    NSMutableDictionary newStats = new NSMutableDictionary(5);

                    newStats.takeValueForKey((String) statistics.valueForKey("StartedAt"), "startedAt");

                    NSDictionary tempDict = (NSDictionary) statistics.valueForKey("Transactions");
                    newStats.takeValueForKey((String) tempDict.valueForKey("Transactions"), "transactions");
                    newStats.takeValueForKey((String) tempDict.valueForKey("Avg. Transaction Time"), "avgTransactionTime");
                    newStats.takeValueForKey((String) tempDict.valueForKey("Avg. Idle Time"), "averageIdlePeriod");

                    tempDict = (NSDictionary) statistics.valueForKey("Sessions");
                    newStats.takeValueForKey((String) tempDict.valueForKey("Current Active Sessions"), "activeSessions");

                    anInstance.setStatistics(newStats);
                } catch (Exception e) {
                    // Do nothing - assume we died trying to parse the plist
                    NSLog.err.appendln("Wotaskd getStatisticsForInstanceArray: Error parsing PList: " + queryInstanceResponse + " from " + anInstance.displayName());
                }
            }
        }
    }


    private void syncSiteConfig(NSDictionary config) {
        NSDictionary newConfig = config;
        Application theApplication = (Application) WOApplication.application();
        MSiteConfig aConfig = theApplication.siteConfig();

        NSDictionary siteDict = (NSDictionary) config.valueForKey("site");
        NSArray hostArray = (NSArray) config.valueForKey("hostArray");
        NSArray applicationArray = (NSArray) config.valueForKey("applicationArray");
        NSArray instanceArray = (NSArray) config.valueForKey("instanceArray");

        // Configure the site
        if (siteDict != null) aConfig.updateValues(siteDict);

        // Look through the array of hosts, and see if we need to add/remove any - configure the rest
        NSMutableArray currentHosts = new NSMutableArray(aConfig.hostArray());
        if (hostArray != null) {
            for (Enumeration e = hostArray.objectEnumerator(); e.hasMoreElements(); ) {
                NSDictionary aHost = (NSDictionary) e.nextElement();
                String name = (String) aHost.valueForKey("name");
                MHost anMHost = aConfig.hostWithName(name);
                if (anMHost == null) {
                    // we have to add it
                    aConfig.addHost_W(new MHost(aHost, aConfig));
                } else {
                    // configure and remove from currentHosts
                    anMHost.updateValues(aHost);
                    currentHosts.removeObject(anMHost);
                }
            }
        }
        // remove all hosts remaining in currentHosts
        for (Enumeration e = currentHosts.objectEnumerator(); e.hasMoreElements(); ) {
            MHost anMHost = (MHost) e.nextElement();
            if ( anMHost == aConfig.localHost()) {
                stopAllInstances();
                ( (Application) WOApplication.application()).setSiteConfig(new MSiteConfig(null));
                break;
            }
            aConfig.removeHost_W(anMHost);
        }

        // Look through the array of applications, and see if we need to add/remove any - configure the rest
        NSMutableArray currentApplications = new NSMutableArray(aConfig.applicationArray());
        if (applicationArray != null) {
            for (Enumeration e = applicationArray.objectEnumerator(); e.hasMoreElements(); ) {
                NSDictionary anApp = (NSDictionary) e.nextElement();
                String name = (String) anApp.valueForKey("name");
                MApplication anMApplication = aConfig.applicationWithName(name);
                // if I can't find the application, I might be updating the name - in that case, look under the oldname.
                if (anMApplication == null) {
                    name = (String) anApp.valueForKey("oldname");
                    anMApplication = aConfig.applicationWithName(name);
                }
                if (anMApplication == null) {
                    // we have to add it
                    aConfig.addApplication_W(new MApplication(anApp, aConfig));
                } else {
                    // configure and remove from currentHosts
                    anMApplication.updateValues(anApp);
                    currentApplications.removeObject(anMApplication);
                }
            }
        }
        // remove all hosts remaining in currentHosts
        for (Enumeration e = currentApplications.objectEnumerator(); e.hasMoreElements(); ) {
            aConfig.removeApplication_W((MApplication) e.nextElement());
        }

        // Look through the array of instances, and see if we need to add/remove any - configure the rest
        NSMutableArray currentInstances = new NSMutableArray(aConfig.instanceArray());
        if (instanceArray != null) {
            for (Enumeration e = instanceArray.objectEnumerator(); e.hasMoreElements(); ) {
                NSDictionary anInst = (NSDictionary) e.nextElement();
                String hostName = (String) anInst.valueForKey("hostName");
                Integer port = (Integer) anInst.valueForKey("port");
                MInstance anMInstance = aConfig.instanceWithHostnameAndPort(hostName, port);
                // if I can't find the instance, I might be updating the port - in that case, look under the oldport number.
                if (anMInstance == null) {
                    port = (Integer) anInst.valueForKey("oldport");
                    anMInstance = aConfig.instanceWithHostnameAndPort(hostName, port);
                }
                if (anMInstance == null) {
                    // we have to add it
                    aConfig.addInstance_W(new MInstance(anInst, aConfig));
                } else {
                    // configure and remove from currentHosts
                    anMInstance.updateValues(anInst);
                    currentInstances.removeObject(anMInstance);
                }
            }
        }
        // remove all hosts remaining in currentHosts
        for (Enumeration e = currentInstances.objectEnumerator(); e.hasMoreElements(); ) {
            aConfig.removeInstance_W((MInstance) e.nextElement());
        }
    }


    
    // This will stop all instances in parallel, and return after each stopInstance call has returned.
    private void stopAllInstances() {
        final LocalMonitor localMonitor = ((Application) WOApplication.application()).localMonitor();

        final NSArray instanceArray = ((Application) WOApplication.application()).siteConfig().instanceArray();
        int theCount = instanceArray.count();

        if (theCount == 0) return;

        Thread[] workers = new Thread[theCount];

        for (int i=0; i<theCount; i++) {
            final int j = i;
            Runnable work = new Runnable() {
                public void run() {
                    try {
                        localMonitor.stopInstance((MInstance) instanceArray.objectAtIndex(j));
                    } catch (MonitorException me) {}
                }
            };
            workers[j] = new Thread(work);
            workers[j].start();
        }

        try {
            for (int i=0; i<theCount; i++) {
                workers[i].join();
            }
        } catch (InterruptedException ie) {}
    }

    public WOActionResults defaultAction() {
        // KH - make this faster as well :)
        Application theApplication = (Application) WOApplication.application();
        WOResponse aResponse = theApplication.createResponseInContext(null);
        WORequest aRequest = request();
        MSiteConfig aConfig = theApplication.siteConfig();

        theApplication._lock.startReading();
        try {

            // Check for correct password
            String passwordHeader = aRequest.headerForKey("password");
            if (!aConfig.comparePasswordWithPassword(passwordHeader)) {
                NSLog.debug.appendln("Attempt to call Direct Action: defaultAction with incorrect password.");
                aResponse.setStatus(WOMessage.HTTP_STATUS_FORBIDDEN);
                aResponse.appendContentString("Attempt to call Direct Action: defaultAction on wotaskd with incorrect password.");
                // we endReading at the finally block
                return aResponse;
            }

            aResponse.appendContentString("<html><head><title>Wotaskd for WebObjects 5</title></head><body>");
            aResponse.appendContentString("<center><b>Wotaskd for WebObjects 5: " + _hostName + "</b></center>");
            aResponse.appendContentString("<br><br><hr><br>Site Config as written to disk<br><hr><br><pre>");
            aResponse.appendContentString(WOMessage.stringByEscapingHTMLString(aConfig.generateSiteConfigXML()));
            aResponse.appendContentString("</pre><br><br><hr><br>Adaptor Config as sent to Local WOAdaptors - All Running Applications and Instances<br><hr><br><pre>");
            aResponse.appendContentString(WOMessage.stringByEscapingHTMLString(aConfig.generateAdaptorConfigXML(true, true)));
            aResponse.appendContentString("</pre><br><br><br><br>Adaptor Config as sent to remote WOAdaptors - All Registered and Running Applications and Instances<br><hr><br><pre>");
            aResponse.appendContentString(WOMessage.stringByEscapingHTMLString(aConfig.generateAdaptorConfigXML(true, false)));
            aResponse.appendContentString("</pre><br><br><hr><br>Adaptor Config as written to disk - All Registered Applications and Instances<br><hr><br><pre>");
            aResponse.appendContentString(WOMessage.stringByEscapingHTMLString(aConfig.generateAdaptorConfigXML(false, false)));
            aResponse.appendContentString("</pre><br><br><hr><br>Properties of this wotaskd<br><hr><br><pre>");

            aResponse.appendContentString("The Configuration Directory is: " + MSiteConfig.configDirectoryPath());
            aResponse.appendContentString("<br>");
            if (((Application)WOApplication.application()).shouldWriteAdaptorConfig()) {
                aResponse.appendContentString("Wotaskd is writing WOConfig.xml to disk");
            } else {
                aResponse.appendContentString("Wotaskd is NOT writing WOConfig.xml to disk");
            }
            aResponse.appendContentString("<br>");
            aResponse.appendContentString("The multicast address is: " + ((Application)WOApplication.application()).multicastAddress());
            aResponse.appendContentString("<br>");
            aResponse.appendContentString("This wotaskd is running on Port: " + WOApplication.application().port());
            aResponse.appendContentString("<br>");
            if (((Application)WOApplication.application()).shouldRespondToMulticast()) {
                aResponse.appendContentString("Wotaskd is responding to Multicast");
            } else {
                aResponse.appendContentString("Wotaskd is NOT responding to Multicast");
            }
            aResponse.appendContentString("<br>");
            aResponse.appendContentString("WOAssumeApplicationIsDeadMultiplier is " + (aConfig._appIsDeadMultiplier / 1000));
            aResponse.appendContentString("<br>");
            aResponse.appendContentString("The System Properties are: ");
            aResponse.appendContentString(WOMessage.stringByEscapingHTMLString(System.getProperties().toString()));
            aResponse.appendContentString("</pre><br><br></body></html>");
        } finally {
            theApplication._lock.endReading();
        }

        return aResponse;
    }


    // Adaptor Config Response
    public WOResponse woconfigAction() {
        Application theApplication = (Application) WOApplication.application();
        WORequest aRequest = request();

        // This will return true if we match either WOHost or any known local address
        // We aren't going to regenerate the list, though, since this gets called a lot.
        boolean shouldIncludeUnregisteredInstances = WOHostUtilities.isAnyLocalInetAddress(aRequest._originatingAddress(), false);

        theApplication._lock.startReading();
        String xml;
        try {
            xml = ((Application)WOApplication.application()).siteConfig().generateAdaptorConfigXML(true, shouldIncludeUnregisteredInstances);
        } finally {
            theApplication._lock.endReading();
        }
        WOResponse aResponse = WOApplication.application().createResponseInContext(null);
        aResponse.appendContentString(xml);
        aResponse.setHeader("text/xml", "content-type");
        aResponse.setHeader(aFormat.format(new NSTimestamp()), "Last-Modified");
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("woConfigAction returned: " + xml);

        return aResponse;
    }
    /**********/


    // used by WOInfoCenter and perhaps others
    public WOActionResults findPortAction() {
        Application theApplication = (Application) WOApplication.application();
        WOResponse aResponse = theApplication.createResponseInContext(null);
        WORequest aRequest = request();
        String portString = null;

        // We wouldn't have registered it in the first place, so we don't regenerate
        if (WOHostUtilities.isAnyLocalInetAddress(aRequest._originatingAddress(), false) ) {
            String anAppName = request().stringFormValueForKey("appName");
            portString = theApplication.localMonitor().portForUnregisteredAppNamed(anAppName);
        }

        if (portString == null) {
            portString = "-1";
        }
        aResponse.appendContentString(portString);
        return aResponse;
    }
}
