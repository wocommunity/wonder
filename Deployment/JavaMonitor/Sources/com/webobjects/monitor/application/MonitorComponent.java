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
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.xml.WOXMLException;
import com.webobjects.appserver.xml._JavaMonitorCoder;
import com.webobjects.appserver.xml._JavaMonitorDecoder;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.MSiteConfig;
import com.webobjects.monitor._private.String_Extensions;

public class MonitorComponent extends WOComponent {
	private static final long	serialVersionUID	= -1880897151494772932L;
	final int APP_PAGE = 0;
    final int HOST_PAGE = 1;
    final int SITE_PAGE = 2;
    final int PREF_PAGE = 3;
    final int HELP_PAGE = 4;
    final int MIGRATION_PAGE = 5;
	
    public Application theApplication = (Application)WOApplication.application();

    public Session mySession() {
        return (Session) super.session();
    }

    /********** Common Functionality **********/
    protected NSMutableDictionary createUpdateRequestDictionary(MSiteConfig _Config, MHost _Host, MApplication _Application, NSArray _InstanceArray, String requestType) {
        NSMutableDictionary monitorRequest = new NSMutableDictionary(1);
        NSMutableDictionary updateWotaskd = new NSMutableDictionary(1);
        NSMutableDictionary requestTypeDict = new NSMutableDictionary();

        if (_Config != null) {
            NSDictionary site = new NSDictionary(_Config.values());
            requestTypeDict.takeValueForKey(site, "site");
        }
        if (_Host != null) {
            NSArray hostArray = new NSArray(_Host.values());
            requestTypeDict.takeValueForKey(hostArray, "hostArray");
        }
        if (_Application != null) {
            NSArray applicationArray = new NSArray(_Application.values());
            requestTypeDict.takeValueForKey(applicationArray, "applicationArray");
        }
        if (_InstanceArray != null) {
            int instanceCount = _InstanceArray.count();
            NSMutableArray instanceArray = new NSMutableArray(instanceCount);
            for (int i=0; i<instanceCount; i++) {
                MInstance anInst = (MInstance) _InstanceArray.objectAtIndex(i);
                instanceArray.addObject(anInst.values());
            }
            requestTypeDict.takeValueForKey(instanceArray, "instanceArray");
        }

        updateWotaskd.takeValueForKey(requestTypeDict, requestType);
        monitorRequest.takeValueForKey(updateWotaskd, "updateWotaskd");

        return monitorRequest;
    }

    protected WOResponse[] sendRequest(NSDictionary monitorRequest, NSArray wotaskdArray, boolean willChange) {
        NSData content = new NSData( (new _JavaMonitorCoder()).encodeRootObjectForKey(monitorRequest, "monitorRequest") );
        return MHost.sendRequestToWotaskdArray(content, wotaskdArray, willChange);
    }
    /**********/
    
    
    /********** ADDING (UPDATE) **********/
    protected void sendAddInstancesToWotaskds(NSArray newInstancesArray, NSArray wotaskdArray) {
        WOResponse[] responses = sendRequest( createUpdateRequestDictionary(null, null, null, newInstancesArray, "add"), wotaskdArray, true);
        NSDictionary[] responseDicts = generateResponseDictionaries(responses);
        getUpdateErrors(responseDicts, "add", false, false, true, false);
    }

    protected void sendAddApplicationToWotaskds(MApplication newApplication, NSArray wotaskdArray) {
        WOResponse[] responses = sendRequest( createUpdateRequestDictionary(null, null, newApplication, null, "add"), wotaskdArray, true);
        NSDictionary[] responseDicts = generateResponseDictionaries(responses);
        getUpdateErrors(responseDicts, "add", false, true, false, false);
     }

    protected void sendAddHostToWotaskds(MHost newHost, NSArray wotaskdArray) {
        WOResponse[] responses = sendRequest( createUpdateRequestDictionary(null, newHost, null, null, "add"), wotaskdArray, true);
        NSDictionary[] responseDicts = generateResponseDictionaries(responses);
        getUpdateErrors(responseDicts, "add", true, false, false, false);
    }
    /**********/


    /********** REMOVING (UPDATE) **********/
    protected void sendRemoveInstancesToWotaskds(NSArray exInstanceArray, NSArray wotaskdArray) {
        WOResponse[] responses = sendRequest( createUpdateRequestDictionary(null, null, null, exInstanceArray, "remove"), wotaskdArray, true);
        NSDictionary[] responseDicts = generateResponseDictionaries(responses);
        getUpdateErrors(responseDicts, "remove", false, false, true, false);
    }

    protected void sendRemoveApplicationToWotaskds(MApplication exApplication, NSArray wotaskdArray) {
        WOResponse[] responses = sendRequest( createUpdateRequestDictionary(null, null, exApplication, null, "remove"), wotaskdArray, true);
        NSDictionary[] responseDicts = generateResponseDictionaries(responses);
        getUpdateErrors(responseDicts, "remove", false, true, false, false);
    }

    protected void sendRemoveHostToWotaskds(MHost exHost, NSArray wotaskdArray) {
        WOResponse[] responses = sendRequest( createUpdateRequestDictionary(null, exHost, null, null, "remove"), wotaskdArray, true);
        NSDictionary[] responseDicts = generateResponseDictionaries(responses);
        getUpdateErrors(responseDicts, "remove", true, false, false, false);
    }
    /**********/


    /********** CONFIGURE (UPDATE) **********/
    protected void sendUpdateInstancesToWotaskds(NSArray changedInstanceArray, NSArray wotaskdArray) {
        WOResponse[] responses = sendRequest( createUpdateRequestDictionary(null, null, null, changedInstanceArray, "configure"), wotaskdArray, true);
        NSDictionary[] responseDicts = generateResponseDictionaries(responses);
        getUpdateErrors(responseDicts, "configure", false, false, true, false);
    }

    protected void sendUpdateApplicationToWotaskds(MApplication changedApplication, NSArray wotaskdArray) {
        WOResponse[] responses = sendRequest( createUpdateRequestDictionary(null, null, changedApplication, null, "configure"), wotaskdArray, true);
        NSDictionary[] responseDicts = generateResponseDictionaries(responses);
        getUpdateErrors(responseDicts, "configure", false, true, false, false);
    }

    protected void sendUpdateApplicationAndInstancesToWotaskds(MApplication changedApplication, NSArray wotaskdArray) {
        WOResponse[] responses = sendRequest( createUpdateRequestDictionary(null, null, changedApplication, changedApplication.instanceArray(), "configure"), wotaskdArray, true);
        NSDictionary[] responseDicts = generateResponseDictionaries(responses);
        getUpdateErrors(responseDicts, "configure", false, true, true, false);
    }

    protected void sendUpdateHostToWotaskds(MHost changedHost, NSArray wotaskdArray) {
        WOResponse[] responses =  sendRequest( createUpdateRequestDictionary(null, changedHost, null, null, "configure"), wotaskdArray, true);
        NSDictionary[] responseDicts = generateResponseDictionaries(responses);
        getUpdateErrors(responseDicts, "configure", true, false, false, false);
    }

    protected void sendUpdateSiteToWotaskds() {
        WOResponse[] responses = sendRequest( createUpdateRequestDictionary(theApplication.siteConfig(), null, null, null, "configure"), theApplication.siteConfig().hostArray(), true);
        NSDictionary[] responseDicts = generateResponseDictionaries(responses);
        getUpdateErrors(responseDicts, "configure", false, false, false, true);
    }

    /**********/


    /********** OVERWRITE / CLEAR (UPDATE) **********/
    protected void sendOverwriteToWotaskd(MHost aHost) {
        NSDictionary SiteConfig = theApplication.siteConfig().dictionaryForArchive();
        NSMutableDictionary data = new NSMutableDictionary(SiteConfig, "SiteConfig");
        _sendOverwriteClearToWotaskd(aHost, "overwrite", data);
    }
    protected void sendClearToWotaskd(MHost aHost) {
        String data = new String("SITE");
        _sendOverwriteClearToWotaskd(aHost, "clear", data);
    }

    private void _sendOverwriteClearToWotaskd(MHost aHost, String type, Object data) {
        NSMutableDictionary updateWotaskd = new NSMutableDictionary(data, type);
        NSMutableDictionary monitorRequest = new NSMutableDictionary(updateWotaskd, "updateWotaskd");

        WOResponse[] responses = sendRequest( monitorRequest, new NSArray(aHost), true );
        NSDictionary[] responseDicts = generateResponseDictionaries(responses);
        getUpdateErrors(responseDicts, type, false, false, false, false);
    }
    /**********/

    
    /********** COMMANDING **********/
    private Object[] commandInstanceKeys = new Object[]{"applicationName", "id", "hostName", "port"};

    protected void sendCommandInstancesToWotaskds(String command, NSArray instanceArray, NSArray wotaskdArray) {
        int instanceCount = instanceArray.count();

        NSMutableDictionary monitorRequest = new NSMutableDictionary(1);
        NSMutableArray commandWotaskd = new NSMutableArray(instanceArray.count() + 1);

        commandWotaskd.addObject(command);

        NSMutableArray instanceValueArray = new NSMutableArray(instanceCount);
        for (int i=0; i<instanceCount; i++) {
            MInstance anInst = (MInstance) instanceArray.objectAtIndex(i);
            commandWotaskd.addObject(new NSDictionary(new Object[]{anInst.applicationName(), anInst.id(), anInst.hostName(), anInst.port()},
                                                      commandInstanceKeys) );
        }
        monitorRequest.takeValueForKey(commandWotaskd, "commandWotaskd");

        WOResponse[] responses = sendRequest( monitorRequest, wotaskdArray, false );
        NSDictionary[] responseDicts = generateResponseDictionaries(responses);
        getCommandErrors(responseDicts);
    }
    /**********/


    /********** QUERIES **********/
    protected NSMutableDictionary createQuery(String queryString) {
        NSMutableDictionary monitorRequest = new NSMutableDictionary(queryString, "queryWotaskd");
        return monitorRequest;
    }

    protected WOResponse[] sendQueryToWotaskds(String queryString, NSArray wotaskdArray) {
        return sendRequest( createQuery(queryString), wotaskdArray, false );
    }
    /**********/


    /********** Response Handling **********/
    NSDictionary _responseParsingFailed = new NSDictionary(new NSDictionary(new NSArray("INTERNAL ERROR: Failed to parse response XML"), "errorResponse"), "monitorResponse");
    NSDictionary _emptyResponse = new NSDictionary(new NSDictionary(new NSArray("INTERNAL ERROR: Response returned was null or empty"), "errorResponse"), "monitorResponse");

    protected NSDictionary[] generateResponseDictionaries(WOResponse[] responses) {
        NSDictionary[] responseDicts = new NSDictionary[responses.length];
        for (int i=0; i< responses.length; i++) {
            if ( (responses[i] != null) && (responses[i].content() != null) ) {
                try {
                    responseDicts[i] = (NSDictionary) (new _JavaMonitorDecoder()).decodeRootObject(responses[i].content());
                } catch (WOXMLException wxe) {
                    responseDicts[i] = _responseParsingFailed;
                }
            } else {
                responseDicts[i] = _emptyResponse;
            }
        }
        return responseDicts;
    }
    /**********/


    /********** Error Handling **********/
    public NSMutableArray getUpdateErrors(NSDictionary[] responseDicts, String updateType, boolean hasHosts, boolean hasApplications, boolean hasInstances, boolean hasSite) {
        NSMutableArray errorArray = new NSMutableArray();

        boolean clearOverwrite = false;
        if ( (updateType.equals("overwrite")) || (updateType.equals("clear")) ) clearOverwrite = true;
            
        for (int i=0; i< responseDicts.length; i++) {
            if (responseDicts[i] != null) {
                NSDictionary responseDict = responseDicts[i];
                getGlobalErrorFromResponse(responseDict, errorArray);

                NSDictionary updateWotaskdResponseDict = (NSDictionary) responseDict.valueForKey("updateWotaskdResponse");

                if (updateWotaskdResponseDict != null) {
                    NSDictionary updateTypeResponse = (NSDictionary) updateWotaskdResponseDict.valueForKey(updateType);
                    if (updateTypeResponse != null) {
                        if (clearOverwrite) {
                            String errorMessage = (String) updateTypeResponse.valueForKey("errorMessage");
                            if (errorMessage != null) {
                                errorArray.addObject(errorMessage);
                            }
                        } else {
                            if (hasSite) {
                                NSDictionary aDict = (NSDictionary) updateTypeResponse.valueForKey("site");
                                String errorMessage = (String) aDict.valueForKey("errorMessage");
                                if (errorMessage != null) {
                                    errorArray.addObject(errorMessage);
                                }
                            }
                            if (hasHosts) _addUpdateResponseToErrorArray(updateTypeResponse, "hostArray", errorArray);
                            if (hasApplications) _addUpdateResponseToErrorArray(updateTypeResponse, "applicationArray", errorArray);
                            if (hasInstances) _addUpdateResponseToErrorArray(updateTypeResponse, "instanceArray", errorArray);
                        }
                    }
                }
            }
        }
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("##### getUpdateErrors: " + errorArray);
        mySession().addObjectsFromArrayIfAbsentToErrorMessageArray(errorArray);
        return errorArray;
    }

    protected void _addUpdateResponseToErrorArray(NSDictionary updateTypeResponse, String responseKey, NSMutableArray errorArray) {
        NSArray aResponse = (NSArray) updateTypeResponse.valueForKey(responseKey);
        if (aResponse != null) {
            for (Enumeration e = aResponse.objectEnumerator(); e.hasMoreElements(); ) {
                NSDictionary aDict = (NSDictionary) e.nextElement();
                String errorMessage = (String) aDict.valueForKey("errorMessage");
                if (errorMessage != null) {
                    errorArray.addObject(errorMessage);
                }
            }
        }
    }

    protected NSMutableArray getCommandErrors(NSDictionary[] responseDicts) {
        NSMutableArray errorArray = new NSMutableArray();

        for (int i=0; i< responseDicts.length; i++) {
            if (responseDicts[i] != null) {
                NSDictionary responseDict = responseDicts[i];
                getGlobalErrorFromResponse(responseDict, errorArray);

                NSArray commandWotaskdResponse = (NSArray) responseDict.valueForKey("commandWotaskdResponse");
                if ( (commandWotaskdResponse != null) && (commandWotaskdResponse.count() > 0) ) {
                    int count = commandWotaskdResponse.count();
                    for (int j=1; j<count; j++) {
                        NSDictionary aDict = (NSDictionary) commandWotaskdResponse.objectAtIndex(j);
                        String errorMessage = (String) aDict.valueForKey("errorMessage");
                        if (errorMessage != null) {
                            errorArray.addObject(errorMessage);
                            if (j==0) break;	// the command produced an error, parsing didn't finish
                        }
                    }
                }
            }
        }
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("##### getCommandErrors: " + errorArray);
        mySession().addObjectsFromArrayIfAbsentToErrorMessageArray(errorArray);
        return errorArray;
    }


    protected NSMutableArray getQueryErrors(NSDictionary[] responseDicts) {
        NSMutableArray errorArray = new NSMutableArray();

        for (int i=0; i< responseDicts.length; i++) {
            if (responseDicts[i] != null) {
                NSDictionary responseDict = responseDicts[i];
                getGlobalErrorFromResponse(responseDict, errorArray);

                NSArray commandWotaskdResponse = (NSArray) responseDict.valueForKey("commandWotaskdResponse");
                if ( (commandWotaskdResponse != null) && (commandWotaskdResponse.count() > 0) ) {
                    int count = commandWotaskdResponse.count();
                    for (int j=1; j<count; j++) {
                        NSDictionary aDict = (NSDictionary) commandWotaskdResponse.objectAtIndex(j);
                        String errorMessage = (String) aDict.valueForKey("errorMessage");
                        if (errorMessage != null) {
                            errorArray.addObject(errorMessage);
                            if (j==0) break;	// the command produced an error, parsing didn't finish
                        }
                    }
                }
            }
        }
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("##### getQueryErrors: " + errorArray);
        mySession().addObjectsFromArrayIfAbsentToErrorMessageArray(errorArray);
        return errorArray;
    }

    
    protected void getGlobalErrorFromResponse(NSDictionary responseDict, NSMutableArray errorArray) {
        NSArray errorResponse = (NSArray) responseDict.valueForKey("errorResponse");
        if (errorResponse != null) {
            errorArray.addObjectsFromArray(errorResponse);
        }
    }
    /**********/
    

    /********** PageWithName caching **********/

    public WOComponent pageWithName(String aName) {
        theApplication._lock.startReading();
        try {
            _cacheState(aName);
        } finally {
            theApplication._lock.endReading();
        }
        return super.pageWithName(aName);
    }

    // KH - we should probably set the instance information as we get the responses, to avoid waiting, then doing it in serial! (not that it's _that_ slow)
    private void _cacheState(String aName) {
        MApplication appForDetailPage = mySession().mApplication;

        if (theApplication.siteConfig().hostArray().count() != 0) {
            if ( aName.equals("ApplicationsPage") && (theApplication.siteConfig().applicationArray().count() != 0) ) {

                for (Enumeration e = theApplication.siteConfig().applicationArray().objectEnumerator(); e.hasMoreElements(); ) {
                    MApplication anApp = (MApplication) e.nextElement();
                    anApp.runningInstancesCount = MObject._zeroInteger;
                }

                WOResponse[] responses = sendQueryToWotaskds("APPLICATION", theApplication.siteConfig().hostArray());

                NSMutableArray errorArray = new NSMutableArray();
                NSDictionary applicationResponseDictionary;
                NSDictionary queryResponseDictionary;
                NSArray responseArray = null;
                NSDictionary responseDictionary = null;
                for (int i=0; i< responses.length; i++) {
                    if ( (responses[i] == null) || (responses[i].content() == null) ) {
                        queryResponseDictionary = _emptyResponse;
                    } else {
                        try {
                            queryResponseDictionary = (NSDictionary) new _JavaMonitorDecoder().decodeRootObject(responses[i].content());
                        } catch (WOXMLException wxe) {
                            NSLog.err.appendln("MonitorComponent pageWithName(ApplicationsPage) Error decoding response: " + responses[i].contentString());
                            queryResponseDictionary = _responseParsingFailed;
                        }
                    }
                    getGlobalErrorFromResponse(queryResponseDictionary, errorArray);

                    applicationResponseDictionary = (NSDictionary) queryResponseDictionary.valueForKey("queryWotaskdResponse");
                    if (applicationResponseDictionary != null) {
                        responseArray = (NSArray) applicationResponseDictionary.valueForKey("applicationResponse");
                        if (responseArray != null) {
                            for (int j=0; j<responseArray.count(); j++) {
                                responseDictionary = (NSDictionary) responseArray.objectAtIndex(j);
                                String appName = (String) responseDictionary.valueForKey("name");
                                Integer runningInstances = (Integer) responseDictionary.valueForKey("runningInstances");
                                MApplication anApplication = theApplication.siteConfig().applicationWithName(appName);
                                if (anApplication != null) {
                                    // KH - this is massively suboptimal
                                    anApplication.runningInstancesCount = new Integer(anApplication.runningInstancesCount.intValue() +
                                                                                    runningInstances.intValue());
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

                    WOResponse[] responses = sendQueryToWotaskds("INSTANCE", hostArray);

                    NSMutableArray errorArray = new NSMutableArray();
                    NSArray responseArray = null;
                    NSDictionary responseDictionary = null;
                    NSDictionary queryResponseDictionary = null;
                    for (int i=0; i< responses.length; i++) {
                        if ( (responses[i] == null) || (responses[i].content() == null) ) {
                            responseDictionary = _emptyResponse;
                        } else {
                            try {
                                responseDictionary = (NSDictionary) new _JavaMonitorDecoder().decodeRootObject(responses[i].content());
                            } catch (WOXMLException wxe) {
                                NSLog.err.appendln("MonitorComponent pageWithName(AppDetailPage) Error decoding response: " + responses[i].contentString());
                                responseDictionary = _responseParsingFailed;
                            }
                        }
                        getGlobalErrorFromResponse(responseDictionary, errorArray);

                        queryResponseDictionary = (NSDictionary) responseDictionary.valueForKey("queryWotaskdResponse");
                        if (queryResponseDictionary != null) {
                            responseArray = (NSArray) queryResponseDictionary.valueForKey("instanceResponse");
                            if (responseArray != null) {
                                for (int j=0; j<responseArray.count(); j++) {
                                    responseDictionary = (NSDictionary) responseArray.objectAtIndex(j);

                                    String host = (String) responseDictionary.valueForKey("host");
                                    Integer port = (Integer) responseDictionary.valueForKey("port");
                                    String runningState = (String) responseDictionary.valueForKey("runningState");
                                    Boolean refusingNewSessions = (Boolean) responseDictionary.valueForKey("refusingNewSessions");
                                    NSDictionary statistics = (NSDictionary) responseDictionary.valueForKey("statistics");
                                    NSArray deaths = (NSArray) responseDictionary.valueForKey("deaths");
                                    String nextShutdown = (String) responseDictionary.valueForKey("nextShutdown");

                                    MInstance anInstance = theApplication.siteConfig().instanceWithHostnameAndPort(host, port);
                                    if (anInstance != null) {
                                        for (int k=0; k<MObject.stateArray.length; k++) {
                                            if (MObject.stateArray[k].equals(runningState)) {
                                                anInstance.state = k;
                                                break;
                                            }
                                        }
                                        anInstance.isRefusingNewSessions = String_Extensions.boolValue(refusingNewSessions);
                                        anInstance.setStatistics(statistics);
                                        anInstance.setDeaths(new NSMutableArray(deaths));
                                        anInstance.setNextScheduledShutdownString_M(nextShutdown);
                                    }
                                }
                            }
                        }
                    } // For Loop
                    if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
                        NSLog.debug.appendln("##### pageWithName(AppDetailPage) errors: " + errorArray);
                    mySession().addObjectsFromArrayIfAbsentToErrorMessageArray(errorArray);
                 }
            } else if (aName.equals("HostsPage")) {
                WOResponse[] responses = sendQueryToWotaskds("HOST", theApplication.siteConfig().hostArray());

                NSMutableArray errorArray = new NSMutableArray();
                NSDictionary responseDict = null;
                for (int i=0; i< responses.length; i++) {
                    MHost aHost = (MHost) theApplication.siteConfig().hostArray().objectAtIndex(i);

                    if ( (responses[i] == null) || (responses[i].content() == null) ) {
                        responseDict = _emptyResponse;
                    } else {
                        try {
                            responseDict = (NSDictionary) new _JavaMonitorDecoder().decodeRootObject(responses[i].content());
                        } catch (WOXMLException wxe) {
                            NSLog.err.appendln("MonitorComponent pageWithName(HostsPage) Error decoding response: " + responses[i].contentString());
                            responseDict = _responseParsingFailed;
                        }
                    }
                    getGlobalErrorFromResponse(responseDict, errorArray);

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
    /**********/
    
}
