/*
© Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (“Apple”) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple’s copyrights in this original Apple software (the “Apple Software”), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */
package com.webobjects.monitor._private;

import java.util.Enumeration;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;


public class MApplication extends MObject {
    /*
    String name;
    Integer startingPort;
    Integer timeForStartup;
    Boolean phasedStartup;
    Boolean autoRecover;
    Integer minimumActiveSessionsCount;
    String unixPath;
    String winPath;
    String macPath;
    Boolean cachingEnabled;
    String adaptor;
    Integer adaptorThreads;
    Integer listenQueueSize;
    Integer adaptorThreadsMin;
    Integer adaptorThreadsMax;
    String projectSearchPath;
    Integer sessionTimeOut;
    String statisticsPassword;
    Boolean debuggingEnabled;
    String unixOutputPath;
    String winOutputPath;
    String macOutputPath;
    Boolean autoOpenInBrowser;
    Integer lifebeatInterval;
    String additionalArgs;
    Boolean notificationEmailEnabled;
    String notificationEmailAddr;
    Integer retries;
    String scheduler;	// "RANDOM" | "ROUNDROBIN" | "LOADAVERAGE"
    Integer dormant;
    String redir;
    Integer sendTimeout;
    Integer recvTimeout;
    Integer cnctTimeout;
    Integer sendBufSize;
    Integer recvBufSize;
    Integer poolsize;
    Integer urlVersion;	// 3 | 4
     */

    /********** 'values' accessors **********/
    public String name() { return (String) values.valueForKey("name"); }
    public void setName(String value) {
        if (!value.equals(name())) {
            setOldname(name());
            values.takeValueForKey(value, "name");
            _siteConfig.dataHasChanged();
        }
    }

    public Integer startingPort() { return (Integer) values.valueForKey("startingPort"); }
    public void setStartingPort(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "startingPort"); _siteConfig.dataHasChanged(); }

    public Integer timeForStartup() { return (Integer) values.valueForKey("timeForStartup"); }
    public void setTimeForStartup(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "timeForStartup"); _siteConfig.dataHasChanged(); }

    public Boolean phasedStartup() { return (Boolean) values.valueForKey("phasedStartup"); }
    public void setPhasedStartup(Boolean value) { values.takeValueForKey(value, "phasedStartup"); _siteConfig.dataHasChanged(); }

    public Boolean autoRecover() { return (Boolean) values.valueForKey("autoRecover"); }
    public void setAutoRecover(Boolean value) { values.takeValueForKey(value, "autoRecover"); _siteConfig.dataHasChanged(); }

    public Integer minimumActiveSessionsCount() { return (Integer) values.valueForKey("minimumActiveSessionsCount"); }
    public void setMinimumActiveSessionsCount(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "minimumActiveSessionsCount"); _siteConfig.dataHasChanged(); }

    public String unixPath() { return (String) values.valueForKey("unixPath"); }
    public void setUnixPath(String value) { values.takeValueForKey(value, "unixPath"); _siteConfig.dataHasChanged(); }

    public String winPath() { return (String) values.valueForKey("winPath"); }
    public void setWinPath(String value) { values.takeValueForKey(value, "winPath"); _siteConfig.dataHasChanged(); }

    public String macPath() { return (String) values.valueForKey("macPath"); }
    public void setMacPath(String value) { values.takeValueForKey(value, "macPath"); _siteConfig.dataHasChanged(); }

    public Boolean cachingEnabled() { return (Boolean) values.valueForKey("cachingEnabled"); }
    public void setCachingEnabled(Boolean value) { values.takeValueForKey(value, "cachingEnabled"); _siteConfig.dataHasChanged(); }

    public String adaptor() { return (String) values.valueForKey("adaptor"); }
    public void setAdaptor(String value) { values.takeValueForKey(value, "adaptor"); _siteConfig.dataHasChanged(); }

    public Integer adaptorThreads() { return (Integer) values.valueForKey("adaptorThreads"); }
    public void setAdaptorThreads(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "adaptorThreads"); _siteConfig.dataHasChanged(); }

    public Integer listenQueueSize() { return (Integer) values.valueForKey("listenQueueSize"); }
    public void setListenQueueSize(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "listenQueueSize"); _siteConfig.dataHasChanged(); }

    public Integer adaptorThreadsMin() { return (Integer) values.valueForKey("adaptorThreadsMin"); }
    public void setAdaptorThreadsMin(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "adaptorThreadsMin"); _siteConfig.dataHasChanged(); }

    public Integer adaptorThreadsMax() { return (Integer) values.valueForKey("adaptorThreadsMax"); }
    public void setAdaptorThreadsMax(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "adaptorThreadsMax"); _siteConfig.dataHasChanged(); }

    public String projectSearchPath() { return (String) values.valueForKey("projectSearchPath"); }
    public void setProjectSearchPath(String value) { values.takeValueForKey(value, "projectSearchPath"); _siteConfig.dataHasChanged(); }

    public Integer sessionTimeOut() { return (Integer) values.valueForKey("sessionTimeOut"); }
    public void setSessionTimeOut(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "sessionTimeOut"); _siteConfig.dataHasChanged(); }

    public String statisticsPassword() { return (String) values.valueForKey("statisticsPassword"); }
    public void setStatisticsPassword(String value) { values.takeValueForKey(value, "statisticsPassword"); _siteConfig.dataHasChanged(); }
    
    public Boolean debuggingEnabled() { return (Boolean) values.valueForKey("debuggingEnabled"); }
    public void setDebuggingEnabled(Boolean value) { values.takeValueForKey(value, "debuggingEnabled"); _siteConfig.dataHasChanged(); }

    public String unixOutputPath() { return (String) values.valueForKey("unixOutputPath"); }
    public void setUnixOutputPath(String value) { values.takeValueForKey(value, "unixOutputPath"); _siteConfig.dataHasChanged(); }

    public String winOutputPath() { return (String) values.valueForKey("winOutputPath"); }
    public void setWinOutputPath(String value) { values.takeValueForKey(value, "winOutputPath"); _siteConfig.dataHasChanged(); }

    public String macOutputPath() { return (String) values.valueForKey("macOutputPath"); }
    public void setMacOutputPath(String value) { values.takeValueForKey(value, "macOutputPath"); _siteConfig.dataHasChanged(); }

    public Boolean autoOpenInBrowser() { return (Boolean) values.valueForKey("autoOpenInBrowser"); }
    public void setAutoOpenInBrowser(Boolean value) { values.takeValueForKey(value, "autoOpenInBrowser"); _siteConfig.dataHasChanged(); }

    public Integer lifebeatInterval() { return (Integer) values.valueForKey("lifebeatInterval"); }
    public void setLifebeatInterval(Integer value) { values.takeValueForKey(MObject.validatedLifebeatInterval(value), "lifebeatInterval"); _siteConfig.dataHasChanged(); }

    public String additionalArgs() { return (String) values.valueForKey("additionalArgs"); }
    public void setAdditionalArgs(String value) { values.takeValueForKey(value, "additionalArgs"); _siteConfig.dataHasChanged(); }

    public Boolean notificationEmailEnabled() { return (Boolean) values.valueForKey("notificationEmailEnabled"); }
    public void setNotificationEmailEnabled(Boolean value) { values.takeValueForKey(value, "notificationEmailEnabled"); _siteConfig.dataHasChanged(); }

    public String notificationEmailAddr() { return (String) values.valueForKey("notificationEmailAddr"); }
    public void setNotificationEmailAddr(String value) { values.takeValueForKey(value, "notificationEmailAddr"); _siteConfig.dataHasChanged(); }

    public Integer retries() { return (Integer) values.valueForKey("retries"); }
    public void setRetries(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "retries"); _siteConfig.dataHasChanged(); }

    public String scheduler() { return (String) values.valueForKey("scheduler"); }
    public void setScheduler(String value) { values.takeValueForKey(value, "scheduler"); _siteConfig.dataHasChanged(); }

    public Integer dormant() { return (Integer) values.valueForKey("dormant"); }
    public void setDormant(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "dormant"); _siteConfig.dataHasChanged(); }

    public String redir() { return (String) values.valueForKey("redir"); }
    public void setRedir(String value) { values.takeValueForKey(value, "redir"); _siteConfig.dataHasChanged(); }

    public Integer sendTimeout() { return (Integer) values.valueForKey("sendTimeout"); }
    public void setSendTimeout(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "sendTimeout"); _siteConfig.dataHasChanged(); }

    public Integer recvTimeout() { return (Integer) values.valueForKey("recvTimeout"); }
    public void setRecvTimeout(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "recvTimeout"); _siteConfig.dataHasChanged(); }

    public Integer cnctTimeout() { return (Integer) values.valueForKey("cnctTimeout"); }
    public void setCnctTimeout(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "cnctTimeout"); _siteConfig.dataHasChanged(); }

    public Integer sendBufSize() { return (Integer) values.valueForKey("sendBufSize"); }
    public void setSendBufSize(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "sendBufSize"); _siteConfig.dataHasChanged(); }

    public Integer recvBufSize() { return (Integer) values.valueForKey("recvBufSize"); }
    public void setRecvBufSize(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "recvBufSize"); _siteConfig.dataHasChanged(); }

    public Integer poolsize() { return (Integer) values.valueForKey("poolsize"); }
    public void setPoolsize(Integer value) { values.takeValueForKey(MObject.validatedInteger(value), "poolsize"); _siteConfig.dataHasChanged(); }

    public Integer urlVersion() { return (Integer) values.valueForKey("urlVersion"); }
    public void setUrlVersion(Integer value) { values.takeValueForKey(MObject.validatedUrlVersion(value), "urlVersion"); _siteConfig.dataHasChanged(); }
    /**********/

    
    /********** 'values' accessors **********/
    public String oldname() { return (String) values.valueForKey("oldname"); }
    public void setOldname(String value) { values.takeValueForKey(value, "oldname"); _siteConfig.dataHasChanged(); }
    /**********/


    
    /********** Adding and Removing Instance primitives **********/
    public void _addInstancePrimitive(MInstance anInstance) {
        _instanceArray.addObject(anInstance);
        if (!_hostArray.containsObject(anInstance._host)) {
            _hostArray.addObject(anInstance._host);
        }
    }
    
    public void _removeInstancePrimitive(MInstance anInstance) {
        _instanceArray.removeObject(anInstance);
        boolean uniqueHost = true;
        for (Enumeration e = _instanceArray.objectEnumerator(); e.hasMoreElements(); ) {
            MInstance anInst = (MInstance) e.nextElement();
            if (anInstance._host == anInst._host) {
                uniqueHost = false;
                break;
            }
        }
        if (uniqueHost) {
            _hostArray.removeObject(anInstance._host);
        }
    }
    /**********/



    /********** Object Graph **********/
    NSMutableArray _instanceArray = new NSMutableArray();
    NSMutableArray _hostArray = new NSMutableArray();

    public NSMutableArray instanceArray() { return _instanceArray; }
    public NSArray hostArray() { return _hostArray; }
    /**********/


    
    /********** Constructors **********/
    // For UI
    public MApplication (String aName, MSiteConfig aConfig) {
        this(new NSDictionary<Object, Object>(new Object[]{aName}, new Object[]{"name"} ), aConfig);
        takeValuesFromDefaults();
    }

    // For Unarchiving
    public MApplication (NSDictionary aDict, MSiteConfig aConfig) {
        _siteConfig = aConfig;
        updateValues(aDict);
    }

    // For Cheating on the AppConfigurePage
    public MApplication (NSMutableDictionary aDict, MSiteConfig aConfig, Object o) {
        _siteConfig = aConfig;
        values = aDict.mutableClone();
    }
    
    private static NSDictionary _defaults = new NSDictionary <Object, Object>(new Object[]{
                                                                new Integer(2001),
                                                                new Integer(30),
                                                                Boolean.TRUE,
                                                                Boolean.TRUE,
                                                                new Integer(0),
                                                                Boolean.TRUE,
                                                                "WODefaultAdaptor",
                                                                new Integer(8),
                                                                new Integer(128),
                                                                new Integer(16),
                                                                new Integer(256),
								"()",
								new Integer(3600),
								"",
                                                                Boolean.FALSE,
                                                                Boolean.FALSE,
                                                                new Integer(30),
                                                                "",
                                                                Boolean.FALSE,
                                                                "/Library/WebObjects/Logs",
                                                                "/Library/WebObjects/Applications"},
                                                             new Object[]{
                                                                 "startingPort",
                                                                 "timeForStartup",
                                                                 "phasedStartup",
                                                                 "autoRecover",
                                                                 "minimumActiveSessionsCount",
                                                                 "cachingEnabled",
                                                                 "adaptor",
                                                                 "adaptorThreads",
                                                                 "listenQueueSize",
                                                                 "adaptorThreadsMin",
                                                                 "adaptorThreadsMax",
                                                                 "projectSearchPath",
                                                                 "sessionTimeOut",
                                                                 "statisticsPassword",
                                                                 "debuggingEnabled",
                                                                 "autoOpenInBrowser",
                                                                 "lifebeatInterval",
                                                                 "additionalArgs",
                                                                 "notificationEmailEnabled",
                                                                 "macOutputPath",
                                                                 "macPath"});

    public void takeValuesFromDefaults() {
        values.addEntriesFromDictionary(_defaults);
    }

    public void pushValuesToInstances() {
        int instanceArrayCount = _instanceArray.count();
        for (int i=0; i<instanceArrayCount; i++) {
            MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
            anInstance.takeValuesFromApplication();
        }
    }
    /**********/



    /********** Archiving Support **********/
    public NSDictionary dictionaryForArchive() { return values; }

    public String toString() {
        return values.toString();
    }

    public void extractAdaptorValuesFromSiteConfig() {
        // get my application settings
        adaptorValues.takeValueForKey(values.valueForKey("retries"), "retries");
        adaptorValues.takeValueForKey(values.valueForKey("scheduler"), "scheduler");
        adaptorValues.takeValueForKey(values.valueForKey("dormant"), "dormant");
        adaptorValues.takeValueForKey(values.valueForKey("redir"), "redir");
        adaptorValues.takeValueForKey(values.valueForKey("poolsize"), "poolsize");
        adaptorValues.takeValueForKey(values.valueForKey("urlVersion"), "urlVersion");

        // get MSiteConfig application settings for settings that are still not set
        if (adaptorValues.valueForKey("retries") == null)
            adaptorValues.takeValueForKey(_siteConfig.values.valueForKey("retries"), "retries");
        if (adaptorValues.valueForKey("scheduler") == null)
            adaptorValues.takeValueForKey(_siteConfig.values.valueForKey("scheduler"), "scheduler");
        if (adaptorValues.valueForKey("dormant") == null)
            adaptorValues.takeValueForKey(_siteConfig.values.valueForKey("dormant"), "dormant");
        if (adaptorValues.valueForKey("redir") == null)
            adaptorValues.takeValueForKey(_siteConfig.values.valueForKey("redir"), "redir");
        if (adaptorValues.valueForKey("poolsize") == null)
            adaptorValues.takeValueForKey(_siteConfig.values.valueForKey("poolsize"), "poolsize");
        if (adaptorValues.valueForKey("urlVersion") == null)
            adaptorValues.takeValueForKey(_siteConfig.values.valueForKey("urlVersion"), "urlVersion");
    }
    /**********/

    public Integer nextID() {
        int instanceArrayCount = _instanceArray.count();
        int lastSequence = 0;
        for (int i=0; i<instanceArrayCount; i++) {
            MInstance anInst = (MInstance) _instanceArray.objectAtIndex(i);
            int thisSequence = anInst.id().intValue();
            if (thisSequence > lastSequence) {
                lastSequence = thisSequence;
            }
        }
        return new Integer(lastSequence+1);
    }

    public boolean isIDInUse(Integer ID) {
        if (instanceWithID(ID) == null) {
            return false;
        }
        return true;
    }

    public MInstance instanceWithID(Integer ID) {
        int instanceArrayCount = _instanceArray.count();
        for (int i=0; i<instanceArrayCount; i++) {
            MInstance anInst = (MInstance) _instanceArray.objectAtIndex(i);
            if (anInst.id().equals(ID)) {
                return anInst;
            }
        }
        return null;
    }

    public Integer runningInstancesCount_W() {
        int runningInstances = 0;
        int numInstances = _instanceArray.count();
        for (int i=0; i<numInstances; i++) {
            MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
            if (anInstance.isRunning_W()) {
                runningInstances++;
            }
        }
        return new Integer(runningInstances);
    }

    public boolean isRunning_W() {
        if (runningInstancesCount_W().intValue() > 0) {
            return true;
        }
        return false;
    }

    // Used for the ApplicationsPage
    public Integer runningInstancesCount = MObject._zeroInteger;
    public boolean isRunning() {
        if (runningInstancesCount.intValue() > 0) {
            return true;
        }
        return false;
    }

    // Used for the AppDetailPage
    public Integer runningInstancesCount_M() {
        int runningInstances = 0;
        int numInstances = _instanceArray.count();
        for (int i=0; i<numInstances; i++) {
            MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
            if (anInstance.isRunning_M()) {
                runningInstances++;
            }
        }
        Integer riInt = new Integer(runningInstances);
        runningInstancesCount = riInt;
        return riInt;
    }

    public boolean isRunning_M() {
        if (runningInstancesCount_M().intValue() > 0) {
            return true;
        }
        return false;
    }

    public boolean isStopped_M() {
        int numInstances = _instanceArray.count();
        for (int i=0; i<numInstances; i++) {
            MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
            if (anInstance.state != MObject.DEAD) {
                return false;
            }
        }
        return true;
    }
    
}
