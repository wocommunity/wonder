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
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver._private.WOProperties;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.StatsUtilities;
import com.webobjects.monitor.application.WOTaskdHandler.ErrorCollector;

public class AppDetailPage extends MonitorComponent {

    public AppDetailPage(WOContext aWocontext) {
        super(aWocontext);
        displayGroup = new WODisplayGroup();
        displayGroup.setFetchesOnLoad(false);
        NSArray instancesArray = currentApplication().instanceArray();
        if (instancesArray == null) {
            instancesArray = NSArray.EmptyArray;
        }
        // AK: the MInstances don't really support equals()...
        if (!displayGroup.allObjects().equals(instancesArray)) {
            displayGroup.setObjectArray(instancesArray);
        }
        displayGroup.setSelectedObjects(displayGroup.allObjects());
    }

    private MApplication currentApplication() {
        return mySession().mApplication;
    }

    private static final long serialVersionUID = 438829612215550387L;

    public MInstance currentInstance;

    public boolean isClearDeathSectionVisible = false;

    public boolean showDetailStatistics = false;

    public WODisplayGroup displayGroup;

    public WOComponent showStatisticsClicked() {
        showDetailStatistics = !showDetailStatistics;
        return context().page();
    }

    public WOComponent refreshClicked() {
        return newDetailPage();
    }

    /**
     * Bounces an application. It does so by staring at least one new instance
     * per active host (or 10 % of the total active instance count), waiting
     * until they have started, then refusing sessions for all old instances and
     * turning scheduling on for all but the number of instances we started
     * originally. The next effect should be that the new users get the new app,
     * old instances die in due time and then restart when the sessions stop.
     * You may or may not need to set ERKillTimer to prevent totally
     * long-running sessions to keep the app from dying.
     * 
     * @author ak
     */
    public static class Bouncer extends Thread implements ErrorCollector {

        private MApplication _app;

        private WOTaskdHandler _handler;

        private NSMutableSet<String> _errors;

        private String _status;

        public Bouncer(MApplication app) {
            _app = app;
            _handler = new WOTaskdHandler(this);
            setName("Bouncer: " + app.name());
        }

        private void bounce() throws InterruptedException {

            _errors = new NSMutableSet<String>();

            NSArray<MInstance> instances = _app.instanceArray().immutableClone();
            NSMutableArray<MInstance> runningInstances = new NSMutableArray<MInstance>();
            NSMutableSet<MHost> activeHosts = new NSMutableSet<MHost>();
            NSMutableDictionary<MHost, NSMutableArray<MInstance>> inactiveInstancesByHost = new NSMutableDictionary<MHost, NSMutableArray<MInstance>>();
            NSMutableDictionary<MHost, NSMutableArray<MInstance>> activeInstancesByHost = new NSMutableDictionary<MHost, NSMutableArray<MInstance>>();
            for (MInstance instance : instances) {
                MHost host = instance.host();
                if (instance.isRunning_M()) {
                    runningInstances.addObject(instance);
                    activeHosts.addObject(host);
                    NSMutableArray<MInstance> currentInstances = activeInstancesByHost.objectForKey(host);
                    if (currentInstances == null) {
                        currentInstances = new NSMutableArray<MInstance>();
                        activeInstancesByHost.setObjectForKey(currentInstances, host);
                    }
                    activeInstancesByHost.setObjectForKey(currentInstances, host);
                } else {
                    NSMutableArray<MInstance> currentInstances = inactiveInstancesByHost.objectForKey(host);
                    if (currentInstances == null) {
                        currentInstances = new NSMutableArray<MInstance>();
                        inactiveInstancesByHost.setObjectForKey(currentInstances, host);
                    }
                    currentInstances.addObject(instance);
                }
            }
            int numToStartPerHost = 1;
            if (activeHosts.count() > 0) {
                numToStartPerHost = (int) (runningInstances.count() / activeHosts.count() * .1);
            }
            if (numToStartPerHost < 1) {
                numToStartPerHost = 1;
            }
            boolean useScheduling = true;

            for (MInstance instance : runningInstances) {
                useScheduling &= instance.schedulingEnabled() != null && instance.schedulingEnabled().booleanValue();
            }

            NSMutableArray<MInstance> startingInstances = new NSMutableArray<MInstance>();
            for (int i = 0; i < numToStartPerHost; i++) {
                for (MHost host : activeHosts) {
                    NSArray<MInstance> inactiveInstances = inactiveInstancesByHost.objectForKey(host);
                    if (inactiveInstances.count() >= i) {
                        MInstance instance = inactiveInstances.objectAtIndex(i);
                        log("Adding instance " + instance + " on host " + host);
                        startingInstances.addObject(instance);
                    } else {
                        log("Not enough instances on host: " + host);
                    }
                }
            }
            for (MInstance instance : startingInstances) {
                if (useScheduling) {
                    instance.setSchedulingEnabled(Boolean.TRUE);
                } else {
                    instance.setAutoRecover(Boolean.TRUE);
                }
            }
            handler().sendUpdateInstancesToWotaskds(startingInstances, activeHosts.allObjects());
            handler().sendStartInstancesToWotaskds(startingInstances, activeHosts.allObjects());
            boolean waiting = true;

            // wait until apps have started
            while (waiting) {
                handler().startReading();
                try {
                    log("Checking for started instances");
                    handler().getInstanceStatusForHosts(activeHosts.allObjects());
                    boolean allStarted = true;
                    for (MInstance instance : startingInstances) {
                        allStarted &= instance.isRunning_M();
                    }
                    if (allStarted) {
                        waiting = false;
                    } else {
                        sleep(10 * 1000);
                    }
                } finally {
                    handler().endReading();
                }
            }
            log("Started instances sucessfully");

            // turn scheduling off
            for (MHost host : activeHosts) {
                NSArray<MInstance> currentInstances = activeInstancesByHost.objectForKey(host);
                for (MInstance instance : currentInstances) {
                    if (useScheduling) {
                        instance.setSchedulingEnabled(Boolean.FALSE);
                    } else {
                        instance.setAutoRecover(Boolean.FALSE);
                    }
                }
            }

            handler().sendUpdateInstancesToWotaskds(runningInstances, activeHosts.allObjects());

            // then start to refuse new sessions
            for (MHost host : activeHosts) {
                NSArray<MInstance> currentInstances = activeInstancesByHost.objectForKey(host);
                for (MInstance instance : currentInstances) {
                    instance.isRefusingNewSessions = true;
                }
            }
            handler().sendRefuseSessionToWotaskds(runningInstances, activeHosts.allObjects(), true);
            log("Refused new sessions: " + runningInstances);

            // turn scheduling on again, but only
            for (MHost host : activeHosts) {
                NSArray<MInstance> currentInstances = activeInstancesByHost.objectForKey(host);
                for (int i = 0; i < currentInstances.count() - numToStartPerHost; i++) {
                    MInstance instance = currentInstances.objectAtIndex(i);
                    if (useScheduling) {
                        instance.setSchedulingEnabled(Boolean.TRUE);
                    } else {
                        instance.setAutoRecover(Boolean.TRUE);
                    }
                }
            }
            log("Started scheduling again: " + runningInstances);
            handler().getInstanceStatusForHosts(activeHosts.allObjects());
            log("Finished");
        }

        private void log(Object msg) {
            NSLog.out.appendln(msg);
            _status = msg != null ? msg.toString() : "No status";
        }

        public WOTaskdHandler handler() {
            return _handler;
        }

        @Override
        public void run() {
            try {
                bounce();
            } catch (InterruptedException e) {
                log(e);
            }
        }

        public void addObjectsFromArrayIfAbsentToErrorMessageArray(NSArray<String> aErrors) {
            _errors.addObjectsFromArray(aErrors);
        }

        public NSArray<String> errors() {
            return _errors.allObjects();
        }

        @Override
        public String toString() {
            return "Bouncer: " + _app.name() + "->" + _status;
        }
    }

    public Bouncer currentBouncer() {
        return (Bouncer) session().objectForKey("Bouncer." + currentApplication().name());
    }

    public WOComponent bounceClicked() {
        Bouncer old = currentBouncer();
        if (old != null) {
            old.interrupt();
        }
        Bouncer bouncer = new Bouncer(currentApplication());
        bouncer.start();
        return newDetailPage();
    }

    public MInstance currentInstance() {
        return currentInstance;
    }

    public void awake() {
    }

    public void selectAll() {
        if ("on".equals(context().request().stringFormValueForKey("deselectall"))) {
            displayGroup.setSelectedObjects(new NSMutableArray());
        } else {
            displayGroup.setSelectedObjects(displayGroup.allObjects());
        }
    }

    public void selectRunning() {
        NSMutableArray selected = new NSMutableArray<MInstance>();
        for (Enumeration enumerator = displayGroup.allObjects().objectEnumerator(); enumerator.hasMoreElements();) {
            MInstance instance = (MInstance) enumerator.nextElement();
            if (instance.isRunning_M()) {
                selected.addObject(instance);
            }
        }
        displayGroup.setSelectedObjects(selected);
    }

    public void selectNotRunning() {
        NSMutableArray selected = new NSMutableArray<MInstance>();
        for (Enumeration enumerator = displayGroup.allObjects().objectEnumerator(); enumerator.hasMoreElements();) {
            MInstance instance = (MInstance) enumerator.nextElement();
            if (!instance.isRunning_M()) {
                selected.addObject(instance);
            }
        }
        displayGroup.setSelectedObjects(selected);
    }

    public void selectOne() {
        _setIsSelectedInstance(!isSelectedInstance());
    }

    public void _setIsSelectedInstance(boolean selected) {
        NSMutableArray selectedObjects = displayGroup.selectedObjects().mutableClone();
        if (selected && !selectedObjects.containsObject(currentInstance)) {
            selectedObjects.addObject(currentInstance);
        } else if (!selected && selectedObjects.containsObject(currentInstance)) {
            selectedObjects.removeObject(currentInstance);
        }
        displayGroup.setSelectedObjects(selectedObjects);
    }

    public void setIsSelectedInstance(boolean selected) {

    }

    public boolean isSelectedInstance() {
        return displayGroup.selectedObjects().contains(currentInstance);
    }

    public boolean hasInstances() {
        NSArray instancesArray = currentApplication().instanceArray();
        if (instancesArray == null || instancesArray.count() == 0)
            return false;
        return true;
    }

    public boolean isRefreshEnabled() {
        NSArray instancesArray = currentApplication().instanceArray();
        if (instancesArray == null || instancesArray.count() == 0)
            return false;
        return siteConfig().viewRefreshEnabled().booleanValue();
    }

    public WOComponent configureApplicationClicked() {
        AppConfigurePage aPage = (AppConfigurePage) pageWithName("AppConfigurePage");
        aPage.isNewInstanceSectionVisible = true;
        return aPage;
    }

    public WOComponent configureInstanceClicked() {
        mySession().mInstance = currentInstance;
        InstConfigurePage aPage = (InstConfigurePage) pageWithName("InstConfigurePage");
        return aPage;
    }

    public WOComponent deleteInstanceClicked() {
        mySession().mInstance = currentInstance;
        InstConfirmDeletePage aPage = (InstConfirmDeletePage) pageWithName("InstConfirmDeletePage");
        return aPage;
    }

    public String linkToWOStats() {
        String adaptorURL = siteConfig().woAdaptor();
        StringBuffer aURL = null;
        if (adaptorURL != null) {
            // using adaptor URL
            aURL = new StringBuffer(hrefToInst());
        } else {
            // direct connect
            aURL = new StringBuffer(hrefToInstDirect());
            aURL = aURL.append("/cgi-bin/WebObjects/");
            aURL = aURL.append(currentApplication().name());
            aURL = aURL.append(".woa");
        }
        aURL = aURL.append("/wa/ERXDirectAction/stats?pw=" + WOProperties.TheStatisticsStorePassword);
        return aURL.toString();
    }

    public String _hrefToApp = null;

    public String hrefToApp() {
        if (_hrefToApp == null) {
            String adaptorURL = siteConfig().woAdaptor();
            if (adaptorURL == null) {
                adaptorURL = WOApplication.application().cgiAdaptorURL();
            }
            if (adaptorURL.charAt(adaptorURL.length() - 1) == '/') {
                _hrefToApp = adaptorURL + currentApplication().name();
            } else {
                _hrefToApp = adaptorURL + "/" + currentApplication().name();
            }
        }
        return _hrefToApp;
    }

    public String hrefToInst() {
        return hrefToApp() + ".woa/" + currentInstance.id();
    }

    public String hrefToInstDirect() {
        return "http://" + currentInstance.hostName() + ":" + currentInstance.port();
    }

    /** ******** Deaths ********* */
    public boolean shouldDisplayDeathDetailLink() {
        if (currentInstance.deathCount() > 0) {
            return true;
        }
        return false;
    }

    public WOComponent instanceDeathDetailClicked() {
        mySession().mInstance = currentInstance;
        AppDeathPage aPage = (AppDeathPage) pageWithName("AppDeathPage");
        return aPage;
    }

    public WOComponent clearAllDeathsClicked() {
        handler().startReading();
        try {
            if (currentApplication().hostArray().count() != 0) {
                handler().sendClearDeathsToWotaskds(currentApplication().instanceArray(),
                        currentApplication().hostArray());
            }
        } finally {
            handler().endReading();
        }

        return newDetailPage();
    }

    /** ******* */

    /** ******** Individual Controls ********* */
    public WOComponent startInstance() {
        if ((currentInstance.state == MObject.DEAD) || (currentInstance.state == MObject.STOPPING)
                || (currentInstance.state == MObject.CRASHING) || (currentInstance.state == MObject.UNKNOWN)) {
            handler().sendStartInstancesToWotaskds(new NSArray(currentInstance), new NSArray(currentInstance.host()));
            currentInstance.state = MObject.STARTING;
        }
        return newDetailPage();
    }

    public WOComponent stopInstance() {
        if ((currentInstance.state == MObject.ALIVE) || (currentInstance.state == MObject.STARTING)) {

            handler().sendStopInstancesToWotaskds(new NSArray(currentInstance), new NSArray(currentInstance.host()));
            currentInstance.state = MObject.STOPPING;
        }
        return newDetailPage();
    }

    public WOComponent toggleAutoRecover() {
        if ((currentInstance.autoRecover() != null) && (currentInstance.autoRecover().booleanValue())) {
            currentInstance.setAutoRecover(Boolean.FALSE);
        } else {
            currentInstance.setAutoRecover(Boolean.TRUE);
        }
        sendUpdateInstances(new NSArray(currentInstance));

        return newDetailPage();
    }

    private void sendUpdateInstances(NSArray<MInstance> instances) {
        handler().startReading();
        try {
            NSMutableSet<MHost> hosts = new NSMutableSet<MHost>();
            for (MInstance instance : instances) {
                hosts.addObject(instance.host());
            }
            handler().sendUpdateInstancesToWotaskds(instances, hosts.allObjects());
        } finally {
            handler().endReading();
        }
    }

    public WOComponent toggleRefuseNewSessions() {
        handler().sendRefuseSessionToWotaskds(new NSArray(currentInstance), new NSArray(currentInstance.host()),
                !currentInstance.isRefusingNewSessions);

        return newDetailPage();
    }

    public WOComponent toggleScheduling() {
        if ((currentInstance.schedulingEnabled() != null) && (currentInstance.schedulingEnabled().booleanValue())) {
            currentInstance.setSchedulingEnabled(Boolean.FALSE);
        } else {
            currentInstance.setSchedulingEnabled(Boolean.TRUE);
        }
        sendUpdateInstances(new NSArray(currentInstance));

        return newDetailPage();
    }

    /** ******* */

    public NSArray<MInstance> selectedInstances() {
        return displayGroup.selectedObjects();
        // AK: uncomment for old behaviour
        // return mySession().mApplication.instanceArray();
    }

    /** ******** Group Controls ********* */
    public WOComponent startAllClicked() {
        handler().startReading();
        try {
            startInstances(selectedInstances());
        } finally {
            handler().endReading();
        }

        return newDetailPage();
    }

    private void startInstances(NSArray<MInstance> possibleInstances) {
        NSMutableArray<MInstance> instances = new NSMutableArray<MInstance>();
        for (MInstance currentInstance : possibleInstances) {
            if ((currentInstance.state == MObject.DEAD) || (currentInstance.state == MObject.STOPPING)
                    || (currentInstance.state == MObject.CRASHING) || (currentInstance.state == MObject.UNKNOWN)) {

                instances.addObject(currentInstance);
            }
        }
        if (instances.count() != 0) {
            handler().sendStartInstancesToWotaskds(instances, currentApplication().hostArray());
            for (MInstance currentInstance : instances) {
                if (currentInstance.state != MObject.ALIVE) {
                    currentInstance.state = MObject.STARTING;
                }
            }
        }
    }

    public WOComponent stopAllClicked() {
        return pageWithName("StopAllConfirmPage");
    }

    public WOComponent autoRecoverEnableAllClicked() {
        handler().startReading();
        try {
            NSArray instancesArray = selectedInstances();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                anInst.setAutoRecover(Boolean.TRUE);
            }
            handler().sendUpdateInstancesToWotaskds(instancesArray, allHosts());
        } finally {
            handler().endReading();
        }

        return newDetailPage();
    }

    public WOComponent autoRecoverDisableAllClicked() {
        handler().startReading();
        try {
            NSArray instancesArray = selectedInstances();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                anInst.setAutoRecover(Boolean.FALSE);
            }
            handler().sendUpdateInstancesToWotaskds(instancesArray, allHosts());
        } finally {
            handler().endReading();
        }

        return newDetailPage();
    }

    public WOComponent acceptNewSessionsAllClicked() {
        handler().startReading();
        try {
            handler().sendRefuseSessionToWotaskds(selectedInstances(), currentApplication().hostArray(), false);
        } finally {
            handler().endReading();
        }
        return newDetailPage();
    }

    public WOComponent refuseNewSessionsAllClicked() {
        handler().startReading();
        try {
            handler().sendRefuseSessionToWotaskds(selectedInstances(), currentApplication().hostArray(), true);
            NSArray instancesArray = selectedInstances();
        } finally {
            handler().endReading();
        }
        return newDetailPage();
    }

    public WOComponent schedulingEnableAllClicked() {
        handler().startReading();
        try {
            NSArray instancesArray = selectedInstances();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                anInst.setSchedulingEnabled(Boolean.TRUE);
            }
            if (allHosts().count() != 0) {
                handler().sendUpdateInstancesToWotaskds(instancesArray, allHosts());
            }
        } finally {
            handler().endReading();
        }

        return newDetailPage();
    }

    private WOComponent newDetailPage() {
        AppDetailPage nextPage = (AppDetailPage) pageWithName("AppDetailPage");
        nextPage.displayGroup.setSelectedObjects(displayGroup.selectedObjects());
        return nextPage;
    }

    public WOComponent schedulingDisableAllClicked() {
        handler().startReading();
        try {
            NSArray instancesArray = selectedInstances();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                anInst.setSchedulingEnabled(Boolean.FALSE);
            }
            handler().sendUpdateInstancesToWotaskds(instancesArray, allHosts());
        } finally {
            handler().endReading();
        }

        return newDetailPage();
    }

    /** ******* */

    /** ******** Display Methods ********* */
    public String instanceStatusImage() {
        if (currentInstance.state == MObject.DEAD)
            return "PowerSwitch_Off.gif";
        else if (currentInstance.state == MObject.ALIVE)
            return "PowerSwitch_On.gif";
        else if (currentInstance.state == MObject.STOPPING)
            return "Turning_Off.gif";
        else if (currentInstance.state == MObject.CRASHING)
            return "Turning_Off.gif";
        else if (currentInstance.state == MObject.STARTING)
            return "Turning_On.gif";
        else
            return "PowerSwitch_Off.gif";
    }

    public String instanceStatusImageText() {
        if (currentInstance.state == MObject.DEAD)
            return "OFF";
        else if (currentInstance.state == MObject.ALIVE)
            return "ON";
        else if (currentInstance.state == MObject.STOPPING)
            return "STOPPING";
        else if (currentInstance.state == MObject.CRASHING)
            return "CRASHING";
        else if (currentInstance.state == MObject.STARTING)
            return "STARTING";
        else
            return "UNKNOWN";
    }

    public String autoRecoverImage() {
        if ((currentInstance.autoRecover() != null) && (currentInstance.autoRecover().booleanValue())) {
            return "Panel_On_Green.gif";
        } else {
            return "Panel_Off.gif";
        }
    }

    public String autoRecoverImageText() {
        if ((currentInstance.autoRecover() != null) && (currentInstance.autoRecover().booleanValue())) {
            return "AutoRecover ON";
        } else {
            return "AutoRecover OFF";
        }
    }

    public String refuseNewSessionsImage() {
        if ((currentInstance.schedulingEnabled() != null) && (currentInstance.schedulingEnabled().booleanValue())) {
            if (currentInstance.isRefusingNewSessions) {
                return "Panel_On_Yellow.gif";
            } else {
                return "Panel_Off_Yellow.gif";
            }
        } else {
            if (currentInstance.isRefusingNewSessions) {
                return "Panel_On_Green.gif";
            } else {
                return "Panel_Off.gif";
            }
        }
    }

    public String refuseNewSessionsImageText() {
        if (currentInstance.isRefusingNewSessions) {
            return "Refusing New Sessions";
        } else {
            return "Accepting New Sessions";
        }
    }

    public String schedulingImage() {
        if ((currentInstance.schedulingEnabled() != null) && (currentInstance.schedulingEnabled().booleanValue())) {
            return "Panel_On_Green.gif";
        } else {
            return "Panel_Off.gif";
        }
    }

    public String schedulingImageText() {
        if ((currentInstance.schedulingEnabled() != null) && (currentInstance.schedulingEnabled().booleanValue())) {
            return "Scheduling ON";
        } else {
            return "Scheduling OFF";
        }
    }

    public String nextShutdown() {
        if ((currentInstance.schedulingEnabled() != null) && (currentInstance.schedulingEnabled().booleanValue())) {
            return currentInstance.nextScheduledShutdownString();
        } else {
            return "-";
        }
    }

    /** ******* */

    /** ******** Statistics Display ********* */
    public Integer totalTransactions() {
        return StatsUtilities.totalTransactionsForApplication(currentApplication());
    }

    public Integer totalActiveSessions() {
        return StatsUtilities.totalActiveSessionsForApplication(currentApplication());
    }

    public Float totalAverageTransaction() {
        return StatsUtilities.totalAverageTransactionForApplication(currentApplication());
    }

    public Float totalAverageIdleTime() {
        return StatsUtilities.totalAverageIdleTimeForApplication(currentApplication());
    }

    public Float actualRatePerSecond() {
        return StatsUtilities.actualTransactionsPerSecondForApplication(currentApplication());
    }

    public Float actualRatePerMinute() {
        Float aNumber = StatsUtilities.actualTransactionsPerSecondForApplication(currentApplication());
        return new Float((aNumber.floatValue() * 60));
    }

    /** ******* */

    // Start of Add Instance Stuff
    public MHost aHost;

    public MHost selectedHost;

    public int numberToAdd = 1;

    public WOComponent hostsPageClicked() {
        return pageWithName("HostsPage");
    }

    public WOComponent addInstanceClicked() {
        if (numberToAdd < 1)
            return newDetailPage();

        handler().startWriting();
        try {
            NSMutableArray newInstanceArray = new NSMutableArray(numberToAdd);

            for (int i = 0; i < numberToAdd; i++) {
                Integer aUniqueID = currentApplication().nextID();
                MInstance newInstance = new MInstance(selectedHost, currentApplication(), aUniqueID, siteConfig());
                siteConfig().addInstance_M(newInstance);
                newInstanceArray.addObject(newInstance);
            }

            if (allHosts().count() != 0) {
                handler().sendAddInstancesToWotaskds(newInstanceArray, allHosts());
            }
        } finally {
            handler().endWriting();
        }

        return newDetailPage();
    }

    public boolean hasHosts() {
        handler().startReading();
        try {
            NSArray hosts = allHosts();
            return (hosts != null && (hosts.count() > 0));
        } finally {
            handler().endReading();
        }
    }

}
