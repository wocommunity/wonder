package com.webobjects.monitor.wotaskd;
/*
� Copyright 2006 - 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (�Apple�) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple�s copyrights in this original Apple software (the �Apple Software�), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.TimeZone;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOHTTPConnection;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOTimer;
import com.webobjects.appserver._private.WOHostUtilities;
import com.webobjects.appserver.xml._JavaMonitorCoder;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPathUtilities;
import com.webobjects.foundation.NSSocketUtilities;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation._NSCollectionReaderWriterLock;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.MSiteConfig;
import com.webobjects.monitor._private.MonitorException;
import com.webobjects.monitor._private.ProtoLocalMonitor;
import com.webobjects.monitor._private.String_Extensions;

import er.extensions.foundation.ERXProperties;

public class LocalMonitor extends ProtoLocalMonitor  {
    WOTimer aScheduleTimer;
    WOTimer anAutoRecoverTimer;
    WOTimer anAutoRecoverStartupTimer;
    String _hostName;
    boolean _isOnWindows = false;
    boolean _shouldUseSpawn = true;
    String spawningGrounds = null;
    Application theApplication = (Application )WOApplication.application();
    final int _forceQuitDelay = ERXProperties.intForKeyWithDefault("WOTaskd.killTimeout", 120000);
    final int _receiveTimeout = ERXProperties.intForKeyWithDefault("WOTaskd.receiveTimeout", 5000);
    final boolean _forceQuitTaskEnabled = ERXProperties.booleanForKeyWithDefault("WOTaskd.forceQuitTaskEnabled", false);


    public LocalMonitor() {
        MSiteConfig aConfig = theApplication.siteConfig();

        if (System.getProperties().getProperty("os.name").toLowerCase().startsWith("win")) {
            _isOnWindows = true;
        }
        _shouldUseSpawn = String_Extensions.boolValue(System.getProperty("WOShouldUseSpawn"));
        if (_shouldUseSpawn) {
            String appDir = System.getProperties().getProperty("user.dir");
            appDir = NSPathUtilities.stringByAppendingPathComponent(appDir, "Contents");
            appDir = NSPathUtilities.stringByAppendingPathComponent(appDir, "Resources");
            if (_isOnWindows)
                appDir = NSPathUtilities.stringByAppendingPathComponent(appDir, "SpawnOfWotaskd.exe");
            else
                appDir = NSPathUtilities.stringByAppendingPathComponent(appDir, "SpawnOfWotaskd.sh");

            spawningGrounds = appDir + " ";

            File theApp = new File(appDir);

            if (!(theApp.exists() && theApp.isFile())) {
                _shouldUseSpawn = false;
            }
        }

        // Used to do phased startup the first time startup
        anAutoRecoverStartupTimer = WOTimer.scheduledTimer(aConfig.autoRecoverInterval(), this, "_checkAutoRecoverStartup", null, null, false);

        _hostName = theApplication.host();
    }

    private NSTimestamp calculateNearestHour() {
        NSTimestamp currentTime = new NSTimestamp();

        TimeZone currentTimeZone = currentTime.timeZone();
        int currentYear = currentTime.yearOfCommonEra();
        int currentMonth = currentTime.monthOfYear();
        int currentDayOfMonth = currentTime.dayOfMonth();	// [1,31]
        int currentHourOfDay = currentTime.hourOfDay();		// [0,23]

        return new NSTimestamp(currentYear, currentMonth, currentDayOfMonth, currentHourOfDay+1, 0, 0, currentTimeZone);
    }

    /********** Unregistered Applications **********/
    NSMutableDictionary _unknownApplications = new NSMutableDictionary();
    _NSCollectionReaderWriterLock _unknownAppLock = new _NSCollectionReaderWriterLock();

    public void registerUnknownInstance(String name, String host, String port) {
        _unknownAppLock.startWriting();


        try {
            NSTimestamp currentTime = new NSTimestamp();
            // Don't regenerate the localhost list for random applications
            if ( WOHostUtilities.isLocalInetAddress(InetAddress.getByName(host), false) ) {
                NSMutableDictionary appDict = (NSMutableDictionary) _unknownApplications.valueForKey(name);
                if (appDict != null) {
                    appDict.takeValueForKey(currentTime, port);
                } else {
                    _unknownApplications.takeValueForKey(new NSMutableDictionary(currentTime, port), name);
                }
            }
        } catch (Exception e) {
            // Just ignore it - unregistered instances are second class citizens anyway
        } finally {
            _unknownAppLock.endWriting();
        }
    }

    public String portForUnregisteredAppNamed(String name) {
        _unknownAppLock.startReading();

        try {
            NSDictionary appDict = (NSDictionary) _unknownApplications.valueForKey(name);
            if (appDict != null) {
                NSArray keysArray = appDict.allKeys();
                if ( (keysArray != null) && (keysArray.count() > 0) ) {
                    return (String) keysArray.objectAtIndex(0);
                }
            }
            return null;
        } finally {
	    _unknownAppLock.endReading();
        }
    }

    public void triageUnknownInstances() {
        _unknownAppLock.startWriting();

        try {
            NSMutableDictionary unknownApps = _unknownApplications;
            // Should make this configurable?
            NSTimestamp cutOffDate = new NSTimestamp(System.currentTimeMillis() - 45000);

            NSArray unknownAppKeys = unknownApps.allKeys();
            for (Enumeration e = unknownAppKeys.objectEnumerator(); e.hasMoreElements(); ) {
                String unknownAppKey = (String) e.nextElement();
                NSMutableDictionary appDict = (NSMutableDictionary) unknownApps.valueForKey(unknownAppKey);
                if (appDict != null) {
                    NSArray appDictKeys = appDict.allKeys();
                    for (Enumeration e2 = appDictKeys.objectEnumerator(); e2.hasMoreElements(); ) {
                        String appDictKey = (String) e2.nextElement();
                        NSTimestamp lastLifebeat = (NSTimestamp) appDict.valueForKey(appDictKey);
                        if ( (lastLifebeat != null) && (lastLifebeat.before(cutOffDate)) ) {
                            appDict.removeObjectForKey(appDictKey);
                        }
                    }
                    if (appDict.count() == 0) {
                        unknownApps.removeObjectForKey(unknownAppKey);
                    }
                }
            }
        } finally {
            _unknownAppLock.endWriting();
        }
    }

    // this actually only returns unregistered applications
    @Override
    public StringBuffer generateAdaptorConfigXML() {
        StringBuffer sb = null;

        _unknownAppLock.startReading();
        try {
            NSMutableDictionary unknownApps = _unknownApplications;
            sb = new StringBuffer();

            if ( (unknownApps.count() ==0) ) {
                // we endReading in the finally block
                return sb;
            }

            for (Enumeration e = unknownApps.keyEnumerator(); e.hasMoreElements(); ) {
                String appName = (String) e.nextElement();
                NSMutableDictionary appDict = (NSMutableDictionary) unknownApps.valueForKey(appName);

                sb.append("  <application name=\"");
                sb.append(appName);
                sb.append("\">\n");

                for (Enumeration e2 = appDict.keyEnumerator(); e2.hasMoreElements(); ) {
                    String port = (String) e2.nextElement();
                    sb.append("    <instance");

                    sb.append(" id=\"-");
                    sb.append(port);
                    sb.append("\" port=\"");
                    sb.append(port);
                    sb.append("\" host=\"");
                    sb.append(_hostName);

                    sb.append("\"/>\n");
                } // end Instance Enumeration

                sb.append("  </application>\n");
            } // end Application Enumeration
        } finally {
            _unknownAppLock.endReading();
        }
        return sb;
    }
    /**********/


    /********** Timer Targets **********/
    public void _checkAutoRecover() {
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("_checkAutoRecover START");
        theApplication._lock.startReading();
        try {
            MHost theHost = theApplication.siteConfig().localHost();
            if (theHost != null) {
                NSArray instArray = theHost.instanceArray();
                int instArrayCount = instArray.count();

                for (int i=0; i<instArrayCount; i++) {
                    MInstance anInst = (MInstance) instArray.objectAtIndex(i);

                    if ( (!anInst.isRunning_W()) && (anInst.state != MObject.STARTING) &&
                         ( (anInst.isAutoRecovering()) || (anInst.isScheduled()) ) ) {
                        anInst.setRefusingNewSessions(false);
                        startInstance(anInst);
                    }
                }
            }
            triageUnknownInstances();
        } finally {
            theApplication._lock.endReading();
        }
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("_checkAutoRecover STOP");
    }

    // This only runs once, on startup - then it starts the regular timer
    public void _checkAutoRecoverStartup() {
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("_checkAutoRecoverStartup START");
        theApplication._lock.startReading();
        try {
            MSiteConfig aConfig = theApplication.siteConfig();
            final NSArray appArray = aConfig.applicationArray();
            int appArrayCount = appArray.count();
            final LocalMonitor localMonitor = this;

            Thread[] workers = new Thread[appArrayCount];

            for (int i=0; i<workers.length; i++) {
                final int j = i;
                Runnable work = new Runnable() {
                    public void run() {
                        localMonitor._autoRecoverApplication((MApplication) appArray.objectAtIndex(j));
                    }
                };
                workers[j] = new Thread(work);
                workers[j].start();
            }

            try {
                for (int i=0; i<workers.length; i++) {
                    workers[i].join();
                }
            } catch (InterruptedException ie) {}

            // That timer will kick off a repeating, hourly, timer for _checkSchedules every hour on the hour
            NSTimestamp fireDate = calculateNearestHour();

            //NSTimestamp fireDate, long ti, Object aTarget, String aSelectorName, Object userInfo, Class userInfoClass, boolean repeat
            aScheduleTimer = new WOTimer(fireDate, (60 * 60 * 1000), this, "_checkSchedules", null, null, true);
            aScheduleTimer.schedule();

            // This is the regular timer that should do autorecovery
            anAutoRecoverTimer = WOTimer.scheduledTimer(aConfig.autoRecoverInterval(), this, "_checkAutoRecover", null, null, true);

        } finally {
            theApplication._lock.endReading();
        }
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("_checkAutoRecoverStartup STOP");
    }

    private void _autoRecoverApplication(MApplication anApplication) {
        NSArray instArray = anApplication.instanceArray();
        int instArrayCount = instArray.count();

        long timeForStartup;
        Integer tfs = anApplication.timeForStartup();
        if (tfs != null) {
            timeForStartup = tfs.intValue();
        } else {
            timeForStartup = MInstance.TIME_FOR_STARTUP;
        }
        timeForStartup *= 1000;

        boolean phasedStartup = false;
        Boolean pS = anApplication.phasedStartup();
        if (pS != null) {
            phasedStartup = pS.booleanValue();
        }

        for (int i=0; i<instArrayCount; i++) {
            MInstance anInst = (MInstance) instArray.objectAtIndex(i);

            if ( (anInst.isLocal_W()) && (!anInst.isRunning_W()) && (anInst.state != MObject.STARTING) &&
                 ( (anInst.isAutoRecovering()) || (anInst.isScheduled()) ) ) {
                anInst.setRefusingNewSessions(false);
                startInstance(anInst);

                if ( (phasedStartup) && (i < instArrayCount-1) ) {
                    try {
                        Thread.sleep(timeForStartup);
                    } catch (InterruptedException ie) {
                    }
                } // end phased if
            } // end instance if
        } // end for
    }


    public void _checkSchedules() {
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("_checkSchedules START");
        theApplication._lock.startReading();
        try {

            MHost theHost = theApplication.siteConfig().localHost();
            if (theHost != null) {
                final NSArray instArray = theHost.instanceArray();
                int instArrayCount = instArray.count();

                if (instArrayCount == 0) return;

                final NSTimestamp rightNow = new NSTimestamp(System.currentTimeMillis(), java.util.TimeZone.getDefault());
                Thread[] workers = new Thread[instArrayCount];
                final LocalMonitor localMonitor = this;

                for (int i=0; i<instArrayCount; i++) {
                    final int j = i;
                    Runnable work = new Runnable() {
                        public void run() {
                            try {
                                MInstance anInst = (MInstance) instArray.objectAtIndex(j);
                                if ( (anInst.isScheduled()) && (anInst.nearNextScheduledShutdown(rightNow)) ) {
                                    if (anInst.isGracefullyScheduled()) {
                                        localMonitor.stopInstance(anInst);
                                    } else {
                                        localMonitor.terminateInstance(anInst);
                                    }
                                    anInst.calculateNextScheduledShutdown();
                                }
                            } catch (MonitorException me) {
                                NSLog.err.appendln("Exception while scheduling: " + me.getMessage());
                            }
                        }
                    };
                    workers[j] = new Thread(work);
                    workers[j].start();
                }

                try {
                    for (int i=0; i<workers.length; i++) {
                        workers[i].join();
                    }
                } catch (InterruptedException ie) {}

            }
        } finally {
            theApplication._lock.endReading();
        }
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("_checkSchedules STOP");
    }
    /**********/


    /********** Controlling Instances **********/
    // Returns null if success
    @Override
    public String startInstance(MInstance anInstance) {
        MSiteConfig aConfig = theApplication.siteConfig();
        if (anInstance == null)
            return "Attempt to start null instance on " + _hostName;
        if (anInstance.host() != aConfig.localHost())
            return anInstance.displayName() + " does not exist on " + _hostName + "; START instance failed";
        if (anInstance.isRunning_W())
            //            return _hostName + ": " + anInstance.displayName() + " is already running";
            return null;
        if (anInstance.state == MObject.STARTING)
            //            return _hostName + ": " + anInstance.displayName() + " is currently starting";
            return null;
        if (_testConnection(anInstance))
            return _hostName + ": "+ anInstance.displayName() + " cannot be started because port " + anInstance.port() + " is still in use";

        String aFullPath = anInstance.path();

        if ( aFullPath == null ) return _hostName + ": Path for " + anInstance.displayName() + " does not exist";

        aFullPath = anInstance.path().trim();
        String arguments = anInstance.commandLineArguments();
        String aLaunchPath = aFullPath + " " + arguments;

        anInstance.willAttemptToStart();

        File aFile = new File(aFullPath);

        if ( !aFile.exists() ) return _hostName + ": Path '" + aFullPath + "' for " + anInstance.displayName() + " does not exist";
        if ( !aFile.isFile() ) return _hostName + ": Path '" + aFullPath + "' for " + anInstance.displayName() + " is not a file";

        if (_shouldUseSpawn) {
            if (_isOnWindows) {
                aLaunchPath = spawningGrounds + aLaunchPath;
            } else {
                aLaunchPath = spawningGrounds + aLaunchPath;
            }
        }

        try {
            if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelCritical, NSLog.DebugGroupDeployment))
                NSLog.debug.appendln("Starting Instance: " + aLaunchPath);
            Runtime.getRuntime().exec(aLaunchPath);
        } catch (IOException ioe) {
            NSLog.err.appendln("Failed to start " + anInstance.displayName() + ": " + ioe);
            return _hostName + ": Failed to start " + anInstance.displayName() + ": " + ioe;
        }
        return null;
    }

    @Override
    public WOResponse terminateInstance(MInstance anInstance) throws MonitorException {
        if (!anInstance.isRunning_W()) return null;
        
        //if WOTaskd.forceQuitTaskEnabled is true, setup a task to check
        //the instance, if it still doesn't die, then force a QUIT command when
        //the timer elapses, minimum is 60 seconds, default 120 seconds
        if (_forceQuitTaskEnabled) {
            if (_forceQuitDelay >= 60000) {
               	anInstance.scheduleForceQuit(new MInstanceTask.ForceQuit(anInstance), _forceQuitDelay);
            }
            else {
            	NSLog.err.appendln("WOtaskd.killTimeout: " + _forceQuitDelay + " is too small. 60000 milliseconds is the minimum");
            }
        }
        
        catchInstanceErrors(anInstance);
        NSDictionary xmlDict = createInstanceRequestDictionary("TERMINATE", null, anInstance);
        return sendInstanceRequest(anInstance, xmlDict);
    }

    @Override
    public WOResponse stopInstance(MInstance anInstance) throws MonitorException {
        if (!anInstance.isRunning_W()) return null;
        
        //if WOTaskd.forceQuitTaskEnabled is true, setup a task to check the instance, this will retry WOTaskd.refuseNumRetries times
        //the timer elapses minimum is 60 seconds, default 3600 seconds (the default session timeout)
        //a force quit if WOTaskd.refuseNumRetries is reached and the instance is still alive
        //an ACCEPT will cancel the monitoring
        if (_forceQuitTaskEnabled) {
            if (_forceQuitDelay >= 60000) {
               	anInstance.scheduleRefuseTask(new MInstanceTask.Refuse(anInstance, ERXProperties.intForKeyWithDefault("WOTaskd.refuseNumRetries", 3)), _forceQuitDelay, _forceQuitDelay);
            }
            else {
            	NSLog.err.appendln("WOtaskd.killTimeout: " + _forceQuitDelay + " is too small. 60000 milliseconds is the minimum");
            }
        }
        
        catchInstanceErrors(anInstance);
        NSDictionary xmlDict = createInstanceRequestDictionary("REFUSE", null, anInstance);
        return sendInstanceRequest(anInstance, xmlDict);
    }

    public WOResponse setAcceptInstance(MInstance anInstance) throws MonitorException {
        catchInstanceErrors(anInstance);
        NSDictionary xmlDict = createInstanceRequestDictionary("ACCEPT", null, anInstance);
        return sendInstanceRequest(anInstance, xmlDict);
    }

    @Override
    public WOResponse queryInstance(MInstance anInstance) throws MonitorException {
        catchInstanceErrors(anInstance);
        NSDictionary xmlDict = createInstanceRequestDictionary(null, "STATISTICS", anInstance);
        return sendInstanceRequest(anInstance, xmlDict);
    }

    protected void catchInstanceErrors(MInstance anInstance) throws MonitorException {
        MSiteConfig aConfig = theApplication.siteConfig();
        if (anInstance == null)
            throw new MonitorException("Attempt to command null instance on " + _hostName);
        if (anInstance.host() != aConfig.localHost())
            throw new MonitorException(anInstance.displayName() + " does not exist on " + _hostName + "; command failed");
        if (!anInstance.isRunning_W())
            throw new MonitorException(_hostName + ": " + anInstance.displayName() + " is not running");
    }

    protected WOResponse sendInstanceRequest(MInstance anInstance, NSDictionary xmlDict) throws MonitorException {
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("!@#$!@#$ sendInstanceRequest creates a WOHTTPConnection");

        String contentXML = (new _JavaMonitorCoder()).encodeRootObjectForKey(xmlDict, "instanceRequest");
        NSData content = new NSData(contentXML);

        //        String urlString = MObject.adminActionStringPrefix + anInstance.application().realName() + MObject.adminActionStringPostfix;
        String urlString = MObject.adminActionStringPrefix + anInstance.applicationName() + MObject.adminActionStringPostfix;
        WORequest aRequest = new WORequest(MObject._POST, urlString, MObject._HTTP1, null, content, null);
        WOResponse aResponse = null;

        try {
            WOHTTPConnection anHTTPConnection = new WOHTTPConnection(anInstance.host().name(), anInstance.port().intValue());
            anHTTPConnection.setReceiveTimeout(_receiveTimeout);

            boolean requestSucceeded = anHTTPConnection.sendRequest(aRequest);

            if (requestSucceeded) {
                aResponse = anHTTPConnection.readResponse();
            } else {
                throw new MonitorException(_hostName + ": Failed to receive response from " + anInstance.displayName());
            }
            anInstance.succeededInConnection();
        } catch (NSForwardException ne) {
            if (ne.originalException() instanceof IOException) {
                anInstance.failedToConnect();
                throw new MonitorException(_hostName + ": Timeout while connecting to " + anInstance.displayName());
            }
            throw ne;
        } catch (MonitorException me) {
            anInstance.failedToConnect();
            throw me;
        } catch (Exception e) {
            anInstance.failedToConnect();
            throw new MonitorException(_hostName + ": Error while communicating with " + anInstance.displayName() + ": " + e);
        }
        return aResponse;
    }

    protected NSMutableDictionary createInstanceRequestDictionary(String commandString, String queryString, MInstance anInstance) {
        NSMutableDictionary instanceRequest = new NSMutableDictionary(2);

        if (commandString != null) {
            NSMutableDictionary commandInstance = new NSMutableDictionary(2);
            commandInstance.takeValueForKey(commandString, "command");
            if (commandString.equals("REFUSE")) {
                commandInstance.takeValueForKey(anInstance.minimumActiveSessionsCount(), "minimumActiveSessionsCount");
            }
            instanceRequest.takeValueForKey(commandInstance, "commandInstance");
        }

        if (queryString != null) {
            String queryInstance = queryString;
            instanceRequest.takeValueForKey(queryInstance, "queryInstance");
        }

        return instanceRequest;
    }

    private boolean _testConnection(MInstance anInstance) {
        try {
            Socket aSocket = NSSocketUtilities.getSocketWithTimeout(anInstance.host().name(), anInstance.port().intValue(), 1000);
            aSocket.close();
            aSocket = null;
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    /**********/
}
