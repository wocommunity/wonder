package com.webobjects.monitor.application;
/*
© Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (ÒAppleÓ) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under AppleÕs copyrights in this original Apple software (the ÒApple SoftwareÓ), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.StatsUtilities;

public class AppDetailPage extends MonitorComponent  {
	private static final long	serialVersionUID	= 438829612215550387L;
	public MInstance selectedInstance;
    public boolean isClearDeathSectionVisible = false;
    public NSMutableArray _appInstancesArray = null;

    
    public WOComponent refreshClicked() {
        return pageWithName("AppDetailPage");
    }

    public boolean hasInstances() {
        NSArray instancesArray = mySession().mApplication.instanceArray();
        if (instancesArray == null || instancesArray.count() == 0) return false;
        return true;
    }

    public boolean isRefreshEnabled() {
        NSArray instancesArray = mySession().mApplication.instanceArray();
        if (instancesArray == null || instancesArray.count() == 0) return false;
        return theApplication.siteConfig().viewRefreshEnabled().booleanValue();
    }

    public WOComponent configureApplicationClicked() {
        AppConfigurePage aPage = (AppConfigurePage) pageWithName("AppConfigurePage");
        aPage.isNewInstanceSectionVisible = true;
        return aPage;
    }

    public WOComponent configureInstanceClicked() {
        mySession().mInstance = selectedInstance;
        InstConfigurePage aPage = (InstConfigurePage) pageWithName("InstConfigurePage");
        return aPage;
    }

    public WOComponent deleteInstanceClicked() {
        mySession().mInstance = selectedInstance;
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
            if (adaptorURL.charAt(adaptorURL.length()-1) == '/') {
                _hrefToApp = adaptorURL + mySession().mApplication.name();
            } else {
                _hrefToApp = adaptorURL + "/" + mySession().mApplication.name();
            }
        }
        return _hrefToApp;
    }

    public String hrefToInst() {
        return hrefToApp() + ".woa/" + selectedInstance.id();
    }

    public String hrefToInstDirect() {
        return "http://" + selectedInstance.hostName() + ":" + selectedInstance.port();
    }


    /********** Deaths **********/
    public boolean shouldDisplayDeathDetailLink() {
        if ( selectedInstance.deathCount() > 0) {
            return true;
        }
        return false;
    }

    public WOComponent instanceDeathDetailClicked() {
        mySession().mInstance = selectedInstance;
        AppDeathPage aPage = (AppDeathPage) pageWithName("AppDeathPage");
        return aPage;
    }

    public WOComponent clearAllDeathsClicked() {
        theApplication._lock.startReading();
        try {
            if (mySession().mApplication.hostArray().count() != 0) {
                sendCommandInstancesToWotaskds("CLEAR", mySession().mApplication.instanceArray(), mySession().mApplication.hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

        return pageWithName("AppDetailPage");
    }
    /**********/


    /********** Individual Controls **********/
    public WOComponent startInstance() {
        if ( (selectedInstance.state == MObject.DEAD) ||
             (selectedInstance.state == MObject.STOPPING) ||
             (selectedInstance.state == MObject.CRASHING) ||
             (selectedInstance.state == MObject.UNKNOWN) ) {

            sendCommandInstancesToWotaskds("START", new NSArray(selectedInstance), new NSArray(selectedInstance.host()));
            selectedInstance.state = MObject.STARTING;
        }
        return pageWithName("AppDetailPage");
    }


    public WOComponent stopInstance() {
        if ( (selectedInstance.state == MObject.ALIVE) ||
             (selectedInstance.state == MObject.STARTING) ) {

            sendCommandInstancesToWotaskds("STOP", new NSArray(selectedInstance), new NSArray(selectedInstance.host()));
            selectedInstance.state = MObject.STOPPING;
        }
        return pageWithName("AppDetailPage");
    }

    public WOComponent toggleAutoRecover() {
        if ( (selectedInstance.autoRecover() != null) && (selectedInstance.autoRecover().booleanValue()) ) {
            selectedInstance.setAutoRecover(Boolean.FALSE);
        } else {
            selectedInstance.setAutoRecover(Boolean.TRUE);
        }
        theApplication._lock.startReading();
        try {
            if (theApplication.siteConfig().hostArray().count() != 0) {
                sendUpdateInstancesToWotaskds(new NSArray(selectedInstance), theApplication.siteConfig().hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

        return pageWithName("AppDetailPage");
    }

    public WOComponent toggleRefuseNewSessions() {
        if (selectedInstance.isRefusingNewSessions) {
            selectedInstance.isRefusingNewSessions = false;
            sendCommandInstancesToWotaskds("ACCEPT", new NSArray(selectedInstance), new NSArray(selectedInstance.host()));
        } else {
            selectedInstance.isRefusingNewSessions = true;
            sendCommandInstancesToWotaskds("REFUSE", new NSArray(selectedInstance), new NSArray(selectedInstance.host()));
        }

        return pageWithName("AppDetailPage");
    }

    public WOComponent toggleScheduling() {
        if ( (selectedInstance.schedulingEnabled() != null) && (selectedInstance.schedulingEnabled().booleanValue()) ) {
            selectedInstance.setSchedulingEnabled(Boolean.FALSE);
        } else {
            selectedInstance.setSchedulingEnabled(Boolean.TRUE);
        }
        theApplication._lock.startReading();
        try {
            if (theApplication.siteConfig().hostArray().count() != 0) {
                sendUpdateInstancesToWotaskds(new NSArray(selectedInstance), theApplication.siteConfig().hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

        return pageWithName("AppDetailPage");
    }
    /**********/


    /********** Group Controls **********/
    public WOComponent startAllClicked() {
        theApplication._lock.startReading();
        try {
            if (mySession().mApplication.hostArray().count() != 0) {
                sendCommandInstancesToWotaskds("START", mySession().mApplication.instanceArray(), mySession().mApplication.hostArray());
            }

            NSArray instancesArray = mySession().mApplication.instanceArray();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                if (anInst.state != MObject.ALIVE) {
                    anInst.state = MObject.STARTING;
                }
            }
        } finally {
            theApplication._lock.endReading();
        }

        return pageWithName("AppDetailPage");
    }

    public WOComponent stopAllClicked() {
        return pageWithName("StopAllConfirmPage");
    }

    public WOComponent autoRecoverEnableAllClicked() {
        theApplication._lock.startReading();
        try {
            NSArray instancesArray = mySession().mApplication.instanceArray();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                anInst.setAutoRecover(Boolean.TRUE);
            }
            if (theApplication.siteConfig().hostArray().count() != 0) {
                sendUpdateInstancesToWotaskds(mySession().mApplication.instanceArray(), theApplication.siteConfig().hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

        return pageWithName("AppDetailPage");
    }

    public WOComponent autoRecoverDisableAllClicked() {
        theApplication._lock.startReading();
        try {
            NSArray instancesArray = mySession().mApplication.instanceArray();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                anInst.setAutoRecover(Boolean.FALSE);
            }
            if (theApplication.siteConfig().hostArray().count() != 0) {
                sendUpdateInstancesToWotaskds(mySession().mApplication.instanceArray(), theApplication.siteConfig().hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

        return pageWithName("AppDetailPage");
    }

    public WOComponent acceptNewSessionsAllClicked() {
        theApplication._lock.startReading();
        try {
            NSArray instancesArray = mySession().mApplication.instanceArray();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                anInst.isRefusingNewSessions = false;
            }
            if (mySession().mApplication.hostArray().count() != 0) {
                sendCommandInstancesToWotaskds("ACCEPT", mySession().mApplication.instanceArray(), mySession().mApplication.hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

        return pageWithName("AppDetailPage");
    }

    public WOComponent refuseNewSessionsAllClicked() {
        NSArray instancesArray = mySession().mApplication.instanceArray();
        for (int i = 0; i < instancesArray.count(); i++) {
            MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
            anInst.isRefusingNewSessions = true;
        }
        if (mySession().mApplication.hostArray().count() != 0) {
            sendCommandInstancesToWotaskds("REFUSE", mySession().mApplication.instanceArray(), mySession().mApplication.hostArray());
        }

        return pageWithName("AppDetailPage");
    }

    public WOComponent schedulingEnableAllClicked() {
        theApplication._lock.startReading();
        try {
            NSArray instancesArray = mySession().mApplication.instanceArray();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                anInst.setSchedulingEnabled(Boolean.TRUE);
            }
            if (theApplication.siteConfig().hostArray().count() != 0) {
                sendUpdateInstancesToWotaskds(mySession().mApplication.instanceArray(), theApplication.siteConfig().hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

        return pageWithName("AppDetailPage");
    }

    public WOComponent schedulingDisableAllClicked() {
        theApplication._lock.startReading();
        try {
            NSArray instancesArray = mySession().mApplication.instanceArray();
            for (int i = 0; i < instancesArray.count(); i++) {
                MInstance anInst = (MInstance) instancesArray.objectAtIndex(i);
                anInst.setSchedulingEnabled(Boolean.FALSE);
            }
            if (theApplication.siteConfig().hostArray().count() != 0) {
                sendUpdateInstancesToWotaskds(mySession().mApplication.instanceArray(), theApplication.siteConfig().hostArray());
            }
        } finally {
            theApplication._lock.endReading();
        }

        return pageWithName("AppDetailPage");
    }

    /**********/



    /********** Display Methods **********/
    public String instanceStatusImage() {
        if (selectedInstance.state == MObject.DEAD) return "PowerSwitch_Off.gif";
        else if (selectedInstance.state == MObject.ALIVE) return "PowerSwitch_On.gif";
        else if (selectedInstance.state == MObject.STOPPING) return "Turning_Off.gif";
        else if (selectedInstance.state == MObject.CRASHING) return "Turning_Off.gif";
        else if (selectedInstance.state == MObject.STARTING) return "Turning_On.gif";
        else return "PowerSwitch_Off.gif";
    }

    public String instanceStatusImageText() {
        if (selectedInstance.state == MObject.DEAD) return "OFF";
        else if (selectedInstance.state == MObject.ALIVE) return "ON";
        else if (selectedInstance.state == MObject.STOPPING) return "STOPPING";
        else if (selectedInstance.state == MObject.CRASHING) return "CRASHING";
        else if (selectedInstance.state == MObject.STARTING) return "STARTING";
        else return "UNKNOWN";
    }

    public String autoRecoverImage() {
        if ( (selectedInstance.autoRecover() != null) && (selectedInstance.autoRecover().booleanValue()) ) {
            return "Panel_On_Green.gif";
        } else {
            return "Panel_Off.gif";
        }
    }

    public String autoRecoverImageText() {
        if ( (selectedInstance.autoRecover() != null) && (selectedInstance.autoRecover().booleanValue()) ) {
            return "AutoRecover ON";
        } else {
            return "AutoRecover OFF";
        }
    }

    public String refuseNewSessionsImage() {
        if ( (selectedInstance.schedulingEnabled() != null) && (selectedInstance.schedulingEnabled().booleanValue()) ) {
            if (selectedInstance.isRefusingNewSessions) {
                return "Panel_On_Yellow.gif";
            } else {
                return "Panel_Off_Yellow.gif";
            }
        } else {
            if (selectedInstance.isRefusingNewSessions) {
                return "Panel_On_Green.gif";
            } else {
                return "Panel_Off.gif";
            }
        }
    }

    public String refuseNewSessionsImageText() {
        if (selectedInstance.isRefusingNewSessions) {
            return "Refusing New Sessions";
        } else {
            return "Accepting New Sessions";
        }
    }

    public String schedulingImage() {
        if ( (selectedInstance.schedulingEnabled() != null) && (selectedInstance.schedulingEnabled().booleanValue()) ) {
            return "Panel_On_Green.gif";
        } else {
            return "Panel_Off.gif";
        }
    }

    public String schedulingImageText() {
        if ( (selectedInstance.schedulingEnabled() != null) && (selectedInstance.schedulingEnabled().booleanValue()) ) {
            return "Scheduling ON";
        } else {
            return "Scheduling OFF";
        }
    }

    public String nextShutdown() {
        if ( (selectedInstance.schedulingEnabled() != null) && (selectedInstance.schedulingEnabled().booleanValue()) ) {
            return selectedInstance.nextScheduledShutdownString();
        } else {
            return "-";
        }
    }
    /**********/



    /********** Statistics Display **********/
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
    /**********/

    // Start of Add Instance Stuff
    public MHost aHost;
    public MHost selectedHost;
    public int numberToAdd = 1;

    public WOComponent hostsPageClicked() {
        return pageWithName("HostsPage");
    }

    public WOComponent addInstanceClicked() {
        if (numberToAdd < 1) return pageWithName("AppDetailPage");

        theApplication._lock.startWriting();
        try {
            NSMutableArray newInstanceArray = new NSMutableArray(numberToAdd);

            for (int i = 0; i < numberToAdd; i++ ) {
                Integer aUniqueID = mySession().mApplication.nextID();
                MInstance newInstance = new MInstance(selectedHost, mySession().mApplication, aUniqueID, theApplication.siteConfig());
                theApplication.siteConfig().addInstance_M(newInstance);
                newInstanceArray.addObject(newInstance);
            }

            if (theApplication.siteConfig().hostArray().count() != 0) {
                sendAddInstancesToWotaskds(newInstanceArray, theApplication.siteConfig().hostArray());
            }
        } finally {
            theApplication._lock.endWriting();
        }

        return pageWithName("AppDetailPage");
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
