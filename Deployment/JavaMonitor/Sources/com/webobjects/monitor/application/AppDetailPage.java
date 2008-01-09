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
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.StatsUtilities;

public class AppDetailPage extends MonitorComponent {

    public AppDetailPage(WOContext aWocontext) {
        super(aWocontext);
        displayGroup = new WODisplayGroup();
        displayGroup.setFetchesOnLoad(false);
        NSArray instancesArray = mySession().mApplication.instanceArray();
        if (instancesArray == null) {
            instancesArray = NSArray.EmptyArray;
        }
        // AK: the MInstances don't really support equals()...
        if (!displayGroup.allObjects().equals(instancesArray)) {
            displayGroup.setObjectArray(instancesArray);
        }
        displayGroup.setSelectedObjects(displayGroup.allObjects());
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

    public WOComponent bounceClicked() {
        /*
         * there are two ways to do things: <br> installing a new releases assume
         * we need the same instancesActive of apps as before out of
         * instancesMax assume we have instancesPerHost instances for
         * hostsActive hosts out of hostsMax start newInstancesPerHost = max(1,
         * instancesActive/hostsActive*.10) wail until they are ready for former
         * instances,
         * 
         */
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
        NSArray instancesArray = mySession().mApplication.instanceArray();
        if (instancesArray == null || instancesArray.count() == 0)
            return false;
        return true;
    }

    public boolean isRefreshEnabled() {
        NSArray instancesArray = mySession().mApplication.instanceArray();
        if (instancesArray == null || instancesArray.count() == 0)
            return false;
        return theApplication.siteConfig().viewRefreshEnabled().booleanValue();
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
        String adaptorURL = theApplication.siteConfig().woAdaptor();
        StringBuffer aURL = null;
        if (adaptorURL != null) {
            // using adaptor URL
            aURL = new StringBuffer(hrefToInst());
        } else {
            // direct connect
            aURL = new StringBuffer(hrefToInstDirect());
            aURL = aURL.append("/cgi-bin/WebObjects/");
            aURL = aURL.append(mySession().mApplication.name());
            aURL = aURL.append(".woa");
        }
        aURL = aURL.append("/wa/WOStats");
        return aURL.toString();
    }

    public String _hrefToApp = null;

    public String hrefToApp() {
        if (_hrefToApp == null) {
            String adaptorURL = theApplication.siteConfig().woAdaptor();
            if (adaptorURL == null) {
                adaptorURL = WOApplication.application().cgiAdaptorURL();
            }
            if (adaptorURL.charAt(adaptorURL.length() - 1) == '/') {
                _hrefToApp = adaptorURL + mySession().mApplication.name();
            } else {
                _hrefToApp = adaptorURL + "/" + mySession().mApplication.name();
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
        theApplication._lock.startReading();
        try {
            if (mySession().mApplication.hostArray().count() != 0) {
                handler().sendCommandInstancesToWotaskds("CLEAR", mySession().mApplication.instanceArray(),
                        mySession().mApplication.hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

        return newDetailPage();
    }

    /** ******* */

    /** ******** Individual Controls ********* */
    public WOComponent startInstance() {
        if ((currentInstance.state == MObject.DEAD) || (currentInstance.state == MObject.STOPPING)
                || (currentInstance.state == MObject.CRASHING) || (currentInstance.state == MObject.UNKNOWN)) {

            handler().sendCommandInstancesToWotaskds("START", new NSArray(currentInstance),
                    new NSArray(currentInstance.host()));
            currentInstance.state = MObject.STARTING;
        }
        return newDetailPage();
    }

    public WOComponent stopInstance() {
        if ((currentInstance.state == MObject.ALIVE) || (currentInstance.state == MObject.STARTING)) {

            handler().sendCommandInstancesToWotaskds("STOP", new NSArray(currentInstance),
                    new NSArray(currentInstance.host()));
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
        theApplication._lock.startReading();
        try {
            if (theApplication.siteConfig().hostArray().count() != 0) {
                handler().sendUpdateInstancesToWotaskds(new NSArray(currentInstance),
                        theApplication.siteConfig().hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

        return newDetailPage();
    }

    public WOComponent toggleRefuseNewSessions() {
        if (currentInstance.isRefusingNewSessions) {
            currentInstance.isRefusingNewSessions = false;
            handler().sendCommandInstancesToWotaskds("ACCEPT", new NSArray(currentInstance),
                    new NSArray(currentInstance.host()));
        } else {
            currentInstance.isRefusingNewSessions = true;
            handler().sendCommandInstancesToWotaskds("REFUSE", new NSArray(currentInstance),
                    new NSArray(currentInstance.host()));
        }

        return newDetailPage();
    }

    public WOComponent toggleScheduling() {
        if ((currentInstance.schedulingEnabled() != null) && (currentInstance.schedulingEnabled().booleanValue())) {
            currentInstance.setSchedulingEnabled(Boolean.FALSE);
        } else {
            currentInstance.setSchedulingEnabled(Boolean.TRUE);
        }
        theApplication._lock.startReading();
        try {
            if (theApplication.siteConfig().hostArray().count() != 0) {
                handler().sendUpdateInstancesToWotaskds(new NSArray(currentInstance),
                        theApplication.siteConfig().hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

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
        theApplication._lock.startReading();
        try {
            NSArray instancesArray = selectedInstances();
            if (mySession().mApplication.hostArray().count() != 0) {
                handler().sendCommandInstancesToWotaskds("START", instancesArray, mySession().mApplication.hostArray());
            }

            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                if (anInst.state != MObject.ALIVE) {
                    anInst.state = MObject.STARTING;
                }
            }
        } finally {
            theApplication._lock.endReading();
        }

        return newDetailPage();
    }

    public WOComponent stopAllClicked() {
        return pageWithName("StopAllConfirmPage");
    }

    public WOComponent autoRecoverEnableAllClicked() {
        theApplication._lock.startReading();
        try {
            NSArray instancesArray = selectedInstances();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                anInst.setAutoRecover(Boolean.TRUE);
            }
            if (theApplication.siteConfig().hostArray().count() != 0) {
                handler().sendUpdateInstancesToWotaskds(instancesArray, theApplication.siteConfig().hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

        return newDetailPage();
    }

    public WOComponent autoRecoverDisableAllClicked() {
        theApplication._lock.startReading();
        try {
            NSArray instancesArray = selectedInstances();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                anInst.setAutoRecover(Boolean.FALSE);
            }
            if (theApplication.siteConfig().hostArray().count() != 0) {
                handler().sendUpdateInstancesToWotaskds(instancesArray, theApplication.siteConfig().hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

        return newDetailPage();
    }

    public WOComponent acceptNewSessionsAllClicked() {
        theApplication._lock.startReading();
        try {
            NSArray instancesArray = selectedInstances();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                anInst.isRefusingNewSessions = false;
            }
            if (mySession().mApplication.hostArray().count() != 0) {
                handler()
                        .sendCommandInstancesToWotaskds("ACCEPT", instancesArray, mySession().mApplication.hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

        return newDetailPage();
    }

    public WOComponent refuseNewSessionsAllClicked() {
        NSArray instancesArray = selectedInstances();
        for (int i = 0; i < instancesArray.count(); i++) {
            MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
            anInst.isRefusingNewSessions = true;
        }
        if (mySession().mApplication.hostArray().count() != 0) {
            handler().sendCommandInstancesToWotaskds("REFUSE", instancesArray, mySession().mApplication.hostArray());
        }

        return newDetailPage();
    }

    public WOComponent schedulingEnableAllClicked() {
        theApplication._lock.startReading();
        try {
            NSArray instancesArray = selectedInstances();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                anInst.setSchedulingEnabled(Boolean.TRUE);
            }
            if (theApplication.siteConfig().hostArray().count() != 0) {
                handler().sendUpdateInstancesToWotaskds(instancesArray, theApplication.siteConfig().hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

        return newDetailPage();
    }

    private WOComponent newDetailPage() {
        AppDetailPage nextPage = (AppDetailPage) pageWithName("AppDetailPage");
        nextPage.displayGroup.setSelectedObjects(displayGroup.selectedObjects());
        return nextPage;
    }

    public WOComponent schedulingDisableAllClicked() {
        theApplication._lock.startReading();
        try {
            NSArray instancesArray = selectedInstances();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                anInst.setSchedulingEnabled(Boolean.FALSE);
            }
            if (theApplication.siteConfig().hostArray().count() != 0) {
                handler().sendUpdateInstancesToWotaskds(instancesArray, theApplication.siteConfig().hostArray());
            }
        } finally {
            theApplication._lock.endReading();
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
        return StatsUtilities.totalTransactionsForApplication(mySession().mApplication);
    }

    public Integer totalActiveSessions() {
        return StatsUtilities.totalActiveSessionsForApplication(mySession().mApplication);
    }

    public Float totalAverageTransaction() {
        return StatsUtilities.totalAverageTransactionForApplication(mySession().mApplication);
    }

    public Float totalAverageIdleTime() {
        return StatsUtilities.totalAverageIdleTimeForApplication(mySession().mApplication);
    }

    public Float actualRatePerSecond() {
        return StatsUtilities.actualTransactionsPerSecondForApplication(mySession().mApplication);
    }

    public Float actualRatePerMinute() {
        Float aNumber = StatsUtilities.actualTransactionsPerSecondForApplication(mySession().mApplication);
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

        theApplication._lock.startWriting();
        try {
            NSMutableArray newInstanceArray = new NSMutableArray(numberToAdd);

            for (int i = 0; i < numberToAdd; i++) {
                Integer aUniqueID = mySession().mApplication.nextID();
                MInstance newInstance = new MInstance(selectedHost, mySession().mApplication, aUniqueID, theApplication
                        .siteConfig());
                theApplication.siteConfig().addInstance_M(newInstance);
                newInstanceArray.addObject(newInstance);
            }

            if (theApplication.siteConfig().hostArray().count() != 0) {
                handler().sendAddInstancesToWotaskds(newInstanceArray, theApplication.siteConfig().hostArray());
            }
        } finally {
            theApplication._lock.endWriting();
        }

        return newDetailPage();
    }

    public boolean hasHosts() {
        theApplication._lock.startReading();
        try {
            NSArray hosts = theApplication.siteConfig().hostArray();
            return (hosts != null && (hosts.count() > 0));
        } finally {
            theApplication._lock.endReading();
        }
    }

}
