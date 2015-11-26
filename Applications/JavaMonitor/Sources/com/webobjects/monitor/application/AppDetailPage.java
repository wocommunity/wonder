package com.webobjects.monitor.application;

/*
 © Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

 IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

 In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.

 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.
 */
import java.util.Enumeration;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver._private.WOProperties;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.StatsUtilities;
import com.webobjects.monitor.application.starter.ApplicationStarter;
import com.webobjects.monitor.application.starter.GracefulBouncer;
import com.webobjects.monitor.application.starter.RollingShutdownBouncer;
import com.webobjects.monitor.application.starter.ShutdownBouncer;

public class AppDetailPage extends MonitorComponent {

    public AppDetailPage(WOContext aWocontext) {
        super(aWocontext);
        handler().updateForPage(name());

        displayGroup = new WODisplayGroup();
        displayGroup.setFetchesOnLoad(false);
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

    private String bouncerName() {
        return "Bouncer." + myApplication().name();
    }

    public ApplicationStarter currentBouncer() {
        return (ApplicationStarter) session().objectForKey(bouncerName());
    }

    public WOComponent bounceClicked() {
       return bounceClickedWithGracefulBouncer();
    }
    
    public WOComponent bounceClickedWithGracefulBouncer() {
        return bounceClickedWithBouncer(new GracefulBouncer(myApplication()));
    }
    
    public WOComponent bounceClickedWithShutdownBouncer(int maxwait) {
        return bounceClickedWithBouncer(new ShutdownBouncer(myApplication(), maxwait));
    }
    
    public WOComponent bounceClickedWithRollingBouncer() {
        return bounceClickedWithBouncer(new RollingShutdownBouncer(myApplication()));
    }
    
    private WOComponent bounceClickedWithBouncer(ApplicationStarter bouncer) {
        ApplicationStarter old = currentBouncer();
        if (old != null) {
            old.interrupt();
        }
        session().setObjectForKey(bouncer, bouncerName());
        bouncer.start();
        return newDetailPage();
    }

    public MInstance currentInstance() {
        return currentInstance;
    }

    public void selectAll() {
        if ("on".equals(context().request().stringFormValueForKey("deselectall"))) {
            displayGroup.setSelectedObjects(new NSMutableArray());
        } else {
            displayGroup.setSelectedObjects(displayGroup.allObjects());
        }
    }
    
    public WOActionResults selectAllAction() {
    		displayGroup.setSelectedObjects(displayGroup.allObjects());
    		return null;
    }
    
    public WOActionResults selectNoneAction() {
    		displayGroup.setSelectedObjects(new NSMutableArray());
    		return null;
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
        NSArray instancesArray = myApplication().instanceArray();
        if (instancesArray == null || instancesArray.count() == 0)
            return false;
        return true;
    }

    public boolean isRefreshEnabled() {
        NSArray instancesArray = myApplication().instanceArray();
        if (instancesArray == null || instancesArray.count() == 0)
            return false;
        return siteConfig().viewRefreshEnabled().booleanValue();
    }

    public WOComponent configureApplicationClicked() {
        AppConfigurePage aPage = AppConfigurePage.create(context(), myApplication());
        aPage.isNewInstanceSectionVisible = true;
        return aPage;
    }

    public WOComponent configureInstanceClicked() {
        InstConfigurePage aPage = InstConfigurePage.create(context(), currentInstance);
        return aPage;
    }

    public WOComponent deleteInstanceClicked() {

        final MInstance instance = currentInstance;

        return ConfirmationPage.create(context(), new ConfirmationPage.Delegate() {

            public WOComponent cancel() {
                return AppDetailPage.create(context(), instance.application());
            }

            public WOComponent confirm() {
                handler().startWriting();
                try {
                    siteConfig().removeInstance_M(instance);

                    if (siteConfig().hostArray().count() != 0) {
                        handler().sendRemoveInstancesToWotaskds(new NSArray(instance), siteConfig().hostArray());
                    }
                } finally {
                    handler().endWriting();
                }
                return AppDetailPage.create(context(), instance.application());
            }

            public String explaination() {
                return "Selecting 'Yes' will shutdown the selected instance of this application and delete its instance configuration.";
            }

            public int pageType() {
                return APP_PAGE;
            }

            public String question() {
                return "Are you sure you want to delete this instance (" + instance.displayName() + " running on " + instance.hostName() + ")";
            }

        });
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
            aURL = aURL.append(myApplication().name());
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
                _hrefToApp = adaptorURL + myApplication().name();
            } else {
                _hrefToApp = adaptorURL + "/" + myApplication().name();
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

    /* ******** Deaths ********* */
    public boolean shouldDisplayDeathDetailLink() {
        if (currentInstance.deathCount() > 0) {
            return true;
        }
        return false;
    }

    public WOComponent instanceDeathDetailClicked() {
        AppDeathPage aPage = AppDeathPage.create(context(), currentInstance);
        return aPage;
    }

    public WOComponent clearAllDeathsClicked() {
        handler().startReading();
        try {
            if (myApplication().hostArray().count() != 0) {
                handler().sendClearDeathsToWotaskds(myApplication().instanceArray(), myApplication().hostArray());
            }
        } finally {
            handler().endReading();
        }

        return newDetailPage();
    }

    /* ******* */

    /* ******** Individual Controls ********* */
    public WOComponent startInstance() {
        if ((currentInstance.state == MObject.DEAD) || (currentInstance.state == MObject.STOPPING) || (currentInstance.state == MObject.CRASHING)
                || (currentInstance.state == MObject.UNKNOWN)) {
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
        handler().sendRefuseSessionToWotaskds(new NSArray(currentInstance), new NSArray(currentInstance.host()), !currentInstance.isRefusingNewSessions());

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

    /* ******* */

    public NSArray<MInstance> selectedInstances() {
        return displayGroup.selectedObjects();
    }

    public NSArray<MInstance> runningInstances() {
        return myApplication().runningInstances_M();
    }

    /* ******** Group Controls ********* */
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
        for (MInstance anInstance : possibleInstances) {
            if ((anInstance.state == MObject.DEAD) || (anInstance.state == MObject.STOPPING) || (anInstance.state == MObject.CRASHING)
                    || (anInstance.state == MObject.UNKNOWN)) {

                instances.addObject(anInstance);
            }
        }
        if (instances.count() != 0) {
            handler().sendStartInstancesToWotaskds(instances, myApplication().hostArray());
            for (MInstance anInstance : instances) {
                if (anInstance.state != MObject.ALIVE) {
                	anInstance.state = MObject.STARTING;
                }
            }
        }
    }

    public WOComponent stopAllClicked() {

        final NSArray instances = selectedInstances().immutableClone();
        final MApplication application = myApplication();

        return ConfirmationPage.create(context(), new ConfirmationPage.Delegate() {

            public WOComponent cancel() {
                return AppDetailPage.create(context(), application, instances);
            }

            public WOComponent confirm() {
                handler().startReading();
                try {
                    if (application.hostArray().count() != 0) {
                        handler().sendStopInstancesToWotaskds(instances, application.hostArray());
                    }

                    for (int i = 0; i < instances.count(); i++) {
                        MInstance anInst = (MInstance) instances.objectAtIndex(i);
                        if (anInst.state != MObject.DEAD) {
                            anInst.state = MObject.STOPPING;
                        }
                    }
                } finally {
                    handler().endReading();
                }
                return AppDetailPage.create(context(), application, instances);
            }

            public String explaination() {
                return "Selecting 'Yes' will shutdown the selected instances of this application.";
            }

            public int pageType() {
                return APP_PAGE;
            }

            public String question() {
                return "Are you sure you want to stop the " + instances.count() + " instances of " + application.name() + "?";
            }

        });
    }

    public WOComponent deleteAllInstancesClicked() {

        final NSArray instances = selectedInstances().immutableClone();
        final MApplication application = myApplication();

        return ConfirmationPage.create(context(), new ConfirmationPage.Delegate() {

            public WOComponent cancel() {
                return AppDetailPage.create(context(), application, instances);
            }

            public WOComponent confirm() {
                handler().startWriting();
                try {
                    siteConfig().removeInstances_M(application, instances);

                    if (siteConfig().hostArray().count() != 0) {
                        handler().sendRemoveInstancesToWotaskds(instances, siteConfig().hostArray());
                    }
                } finally {
                    handler().endWriting();
                }
                return AppDetailPage.create(context(), application, instances);
            }

            public String explaination() {
                return "Selecting 'Yes' will shutdown any shutdown the selected instances of this application, and delete all matching instance configurations.";
            }

            public int pageType() {
                return APP_PAGE;
            }

            public String question() {
                return "Are you sure you want to delete the selected <i>" + instances.count() + "</i> instances of application " + application.name() + "?";
            }

        });
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
            handler().sendRefuseSessionToWotaskds(selectedInstances(), myApplication().hostArray(), false);
        } finally {
            handler().endReading();
        }
        return newDetailPage();
    }

    public WOComponent refuseNewSessionsAllClicked() {
        handler().startReading();
        try {
            handler().sendRefuseSessionToWotaskds(selectedInstances(), myApplication().hostArray(), true);
            
            @SuppressWarnings("unused")
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
        AppDetailPage nextPage = AppDetailPage.create(context(), myApplication());
        nextPage.displayGroup.setSelectedObjects(displayGroup.selectedObjects());
        nextPage.showDetailStatistics = showDetailStatistics;
        if (currentBouncer() != null &&
        		!"Finished".equals(currentBouncer().status()) && 
        		!currentBouncer().errors().isEmpty()) {
        	mySession().addObjectsFromArrayIfAbsentToErrorMessageArray(currentBouncer().errors());
        	session().removeObjectForKey(bouncerName());
        }
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

    /* ******* */

    /* ******** Display Methods ********* */
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
    
    public String autoRecoverLabel() {
    	String results = "Off";
      if ((currentInstance.autoRecover() != null) && (currentInstance.autoRecover().booleanValue())) {
          results = "On";
      } 
      return results;
    }
    
    public String autoRecoverDivClass() {
    	String base = "AppControl";
    	String results = base + " " + base+"AutoRecoverOff";
      if ((currentInstance.autoRecover() != null) && (currentInstance.autoRecover().booleanValue())) {
          results = base + " " + base+"AutoRecoverOn";
      } 
      return results;
  }

    public String refuseNewSessionsClass() {
    	String base = "AppControl";
    	String result = base + " " + base+"NotRefusingNewSessions";
    	if ((currentInstance.schedulingEnabled() != null) && (currentInstance.schedulingEnabled().booleanValue())) {
    		if (currentInstance.isRefusingNewSessions()) {
    			result = base + " " + base+"ScheduleEnabledRefusingNewSessions";
    		} else {
    			result = base + " " + base+"ScheduleEnabledNotRefusingNewSessions";
    		}
    	} else {
    		if (currentInstance.isRefusingNewSessions()) {
    			result = base + " " + base+"RefusingNewSessions";
    		} 
    	}
    	return result;
    }
    
    public String refuseNewSessionsLabel() {
    	String results = "Off";
      if (currentInstance.isRefusingNewSessions()) {
          results = "On";
      } 
      return results;
  }
    
    public String schedulingLabel() {
    	String result = "Off";
      if ((currentInstance.schedulingEnabled() != null) && (currentInstance.schedulingEnabled().booleanValue())) {
         result = "On";
      } 
      return result;
    }
    
    public String schedulingDivClass() {
    	String base = "AppControl";
    	String result = base + " " + base+"ScheduleOff";
      if ((currentInstance.schedulingEnabled() != null) && (currentInstance.schedulingEnabled().booleanValue())) {
         result = base + " " + base+"ScheduleOn";
      } 
      return result;
    }
    

    public String nextShutdown() {
    		String result = "N/A";
        if ((currentInstance.schedulingEnabled() != null) && (currentInstance.schedulingEnabled().booleanValue())) {
            result = currentInstance.nextScheduledShutdownString();
        } 
        return result;
    }

    /* ******* */

    /* ******** Statistics Display ********* */
    public Integer totalTransactions() {
        return StatsUtilities.totalTransactionsForApplication(myApplication());
    }

    public Integer totalTransactionsForActiveInstances(){
    	return StatsUtilities.totalTransactionsForActiveInstancesOfApplication(myApplication());
    }
    
    
    public Integer totalActiveSessions() {
        return StatsUtilities.totalActiveSessionsForApplication(myApplication());
    }

    public Integer totalActiveSessionsForActiveInstances(){
    	return StatsUtilities.totalActiveSessionsForActiveInstancesOfApplication(myApplication());
    }
 
    
    public Float totalAverageTransaction() {
        return StatsUtilities.totalAverageTransactionForApplication(myApplication());
    }

    public Float totalAverageIdleTime() {
        return StatsUtilities.totalAverageIdleTimeForApplication(myApplication());
    }

    public Float actualRatePerSecond() {
        return StatsUtilities.actualTransactionsPerSecondForApplication(myApplication());
    }

    public Float actualRatePerMinute() {
        Float aNumber = StatsUtilities.actualTransactionsPerSecondForApplication(myApplication());
        return Float.valueOf((aNumber.floatValue() * 60));
    }

    /** ******* */

    // Start of Add Instance Stuff
    public MHost aHost;

    public MHost selectedHost;

    public int numberToAdd = 1;

    public WOComponent hostsPageClicked() {
        return HostsPage.create(context());
    }

    public WOComponent addInstanceClicked() {
        if (numberToAdd < 1)
            return newDetailPage();

        handler().startWriting();
        try {
            NSMutableArray newInstanceArray = siteConfig().addInstances_M(selectedHost, myApplication(), numberToAdd);

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

	public static AppDetailPage create(WOContext context, MApplication currentApplication, NSArray<MInstance> selected) {
        AppDetailPage page = (AppDetailPage) WOApplication.application().pageWithName(AppDetailPage.class.getName(), context);
        page.setMyApplication(currentApplication);
        NSArray instancesArray = currentApplication.instanceArray();
        if (instancesArray == null) {
            instancesArray = NSArray.EmptyArray;
        }
        NSMutableArray<MInstance> result = new NSMutableArray<MInstance>();
        result.addObjectsFromArray(currentApplication.instanceArray());
        EOSortOrdering order = new EOSortOrdering("id", EOSortOrdering.CompareAscending);
        EOSortOrdering.sortArrayUsingKeyOrderArray(result, new NSArray(order));
        instancesArray = result;
        // AK: the MInstances don't really support equals()...
        if (!page.displayGroup.allObjects().equals(instancesArray)) {
            page.displayGroup.setObjectArray(instancesArray);
        }
        if (selected != null) {
            NSMutableArray<MInstance> active = new NSMutableArray<MInstance>();
            for (MInstance instance : selected) {
                if (instancesArray.containsObject(instance)) {
                    active.addObject(instance);
                }
            }
            page.displayGroup.setSelectedObjects(active);
        } else {
            page.displayGroup.setSelectedObjects(page.displayGroup.allObjects());
        }
        return page;
    }

    public static AppDetailPage create(WOContext context, MApplication currentApplication) {
        NSArray selected = (context.page() instanceof AppDetailPage ? ((AppDetailPage) context.page()).selectedInstances() : null);
        return create(context, currentApplication, selected);
    }

}
