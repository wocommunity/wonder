package com.webobjects.monitor.application;

/*
 (c) Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

 IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

 In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
 SUCH DAMAGE.
 */
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.MSiteConfig;
import com.webobjects.monitor._private.String_Extensions;

public class AppConfigurePage extends MonitorComponent {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private static MSiteConfig _sc = new MSiteConfig(null);

    public boolean isNewInstanceSectionVisible = false;

    public boolean isAppConfigureSectionVisible = false;

    public boolean isEmailSectionVisible = false;

    public boolean isSchedulingSectionVisible = false;

    public boolean isAdaptorSettingsSectionVisible = false;

    public MApplication appDefaults;

    public AppConfigurePage(WOContext aWocontext) {
        super(aWocontext);
    }

    public WOComponent detailPageClicked() {
        return AppDetailPage.create(context(), myApplication());
    }

    public WOComponent configurePageClicked() {
        ConfigurePage aPage = ConfigurePage.create(context());
        return aPage;
    }

    /* ******** New Instance Defaults ******** */
    public WOComponent defaultsUpdateClicked() {
        handler().startReading();
        try {
            myApplication().setValues(appDefaults.values());
            handler().sendUpdateApplicationToWotaskds(myApplication(), allHosts());
        } finally {
            handler().endReading();
        }

        AppConfigurePage aPage =  AppConfigurePage.create(context(), myApplication());
        aPage.isNewInstanceSectionVisible = true;
        return aPage;
    }

    public WOComponent updateAppDefaultsOnly() {
        handler().startReading();
        try {
            myApplication().setStartingPort(appDefaults.startingPort());
            myApplication().setTimeForStartup(appDefaults.timeForStartup());
            myApplication().setPhasedStartup(appDefaults.phasedStartup());
            myApplication().setAdaptor(appDefaults.adaptor());
            myApplication().setAdaptorThreads(appDefaults.adaptorThreads());
            myApplication().setListenQueueSize(appDefaults.listenQueueSize());
            myApplication().setAdaptorThreadsMin(appDefaults.adaptorThreadsMin());
            myApplication().setAdaptorThreadsMax(appDefaults.adaptorThreadsMax());
            myApplication().setProjectSearchPath(appDefaults.projectSearchPath());
            myApplication().setSessionTimeOut(appDefaults.sessionTimeOut());
            myApplication().setStatisticsPassword(appDefaults.statisticsPassword());

            boolean pushAppOnly = true;

            if (myApplication().isStopped_M()) {
                String defaultsName = appDefaults.name();
                if (!defaultsName.equals(myApplication().name())) {
                    MApplication app = myApplication().siteConfig().applicationWithName(appDefaults.name());
                    if (app == null) {
                        pushAppOnly = false;
                        myApplication().setName(defaultsName);
                        NSArray _instanceArray = myApplication().instanceArray();
                        int instanceArrayCount = _instanceArray.count();
                        for (int i = 0; i < instanceArrayCount; i++) {
                            MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
                            anInstance._takeNameFromApplication();
                        }
                    }
                }
            }

            if (pushAppOnly) {
                handler().sendUpdateApplicationToWotaskds(myApplication(), allHosts());
            } else {
                _defaultsPush();
            }
        } finally {
            handler().endReading();
        }

        AppConfigurePage aPage =  AppConfigurePage.create(context(), myApplication());
        aPage.isAppConfigureSectionVisible = true;
        return aPage;
    }

    private void _defaultsPush() {
        if (allHosts().count() != 0) {
            handler().sendUpdateApplicationAndInstancesToWotaskds(myApplication(), allHosts());
        }
    }

    private WOComponent _defaultPage() {
        AppConfigurePage aPage = AppConfigurePage.create(context(), myApplication());
        aPage.isNewInstanceSectionVisible = true;
        return aPage;
    }

    public WOComponent defaultsPushClicked() {
        handler().startReading();
        try {
            myApplication().setValues(appDefaults.values());
            myApplication().pushValuesToInstances();
            _defaultsPush();
        } finally {
            handler().endReading();
        }
        return _defaultPage();
    }

    public WOComponent updatePathOnly() {
        handler().startReading();
        try {
            myApplication().setUnixPath(appDefaults.unixPath());
            myApplication().setWinPath(appDefaults.winPath());
            myApplication().setMacPath(appDefaults.macPath());

            NSArray _instanceArray = myApplication().instanceArray();
            int instanceArrayCount = _instanceArray.count();
            for (int i = 0; i < instanceArrayCount; i++) {
                MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
                anInstance._takePathFromApplication();
            }
            _defaultsPush();
        } finally {
            handler().endReading();
        }
        return _defaultPage();
    }

    public WOComponent updateAutoRecoverOnly() {
        handler().startReading();
        try {
            myApplication().setAutoRecover(appDefaults.autoRecover());

            NSArray _instanceArray = myApplication().instanceArray();
            int instanceArrayCount = _instanceArray.count();
            for (int i = 0; i < instanceArrayCount; i++) {
                MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
                anInstance._takeValueFromApplication("autoRecover");
            }
            _defaultsPush();
        } finally {
            handler().endReading();
        }
        return _defaultPage();
    }

    public WOComponent updateMinimumOnly() {
        handler().startReading();
        try {
            myApplication().setMinimumActiveSessionsCount(appDefaults.minimumActiveSessionsCount());

            NSArray _instanceArray = myApplication().instanceArray();
            int instanceArrayCount = _instanceArray.count();
            for (int i = 0; i < instanceArrayCount; i++) {
                MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
                anInstance._takeValueFromApplication("minimumActiveSessionsCount");
            }
            _defaultsPush();
        } finally {
            handler().endReading();
        }
        return _defaultPage();
    }

    public WOComponent updateCachingOnly() {
        handler().startReading();
        try {
            myApplication().setCachingEnabled(appDefaults.cachingEnabled());

            NSArray _instanceArray = myApplication().instanceArray();
            int instanceArrayCount = _instanceArray.count();
            for (int i = 0; i < instanceArrayCount; i++) {
                MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
                anInstance._takeValueFromApplication("cachingEnabled");
            }
            _defaultsPush();
        } finally {
            handler().endReading();
        }
        return _defaultPage();
    }

    public WOComponent updateDebuggingOnly() {
        handler().startReading();
        try {
            myApplication().setDebuggingEnabled(appDefaults.debuggingEnabled());

            NSArray _instanceArray = myApplication().instanceArray();
            int instanceArrayCount = _instanceArray.count();
            for (int i = 0; i < instanceArrayCount; i++) {
                MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
                anInstance._takeValueFromApplication("debuggingEnabled");
            }
            _defaultsPush();
        } finally {
            handler().endReading();
        }
        return _defaultPage();
    }

    public WOComponent updateOutputOnly() {
        handler().startReading();
        try {
            myApplication().setUnixOutputPath(appDefaults.unixOutputPath());
            myApplication().setWinOutputPath(appDefaults.winOutputPath());
            myApplication().setMacOutputPath(appDefaults.macOutputPath());

            NSArray _instanceArray = myApplication().instanceArray();
            int instanceArrayCount = _instanceArray.count();
            for (int i = 0; i < instanceArrayCount; i++) {
                MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
                anInstance._takeOutputPathFromApplication();
            }
            _defaultsPush();
        } finally {
            handler().endReading();
        }
        return _defaultPage();
    }

    public WOComponent updateAutoOpenOnly() {
        handler().startReading();
        try {
            myApplication().setAutoOpenInBrowser(appDefaults.autoOpenInBrowser());

            NSArray _instanceArray = myApplication().instanceArray();
            int instanceArrayCount = _instanceArray.count();
            for (int i = 0; i < instanceArrayCount; i++) {
                MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
                anInstance._takeValueFromApplication("autoOpenInBrowser");
            }
            _defaultsPush();
        } finally {
            handler().endReading();
        }
        return _defaultPage();
    }

    public WOComponent updateLifebeatOnly() {
        handler().startReading();
        try {
            myApplication().setLifebeatInterval(appDefaults.lifebeatInterval());

            NSArray _instanceArray = myApplication().instanceArray();
            int instanceArrayCount = _instanceArray.count();
            for (int i = 0; i < instanceArrayCount; i++) {
                MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
                anInstance._takeValueFromApplication("lifebeatInterval");
            }
            _defaultsPush();
        } finally {
            handler().endReading();
        }
        return _defaultPage();
    }

    public WOComponent updateAddArgsOnly() {
        handler().startReading();
        try {
            myApplication().setAdditionalArgs(appDefaults.additionalArgs());

            NSArray _instanceArray = myApplication().instanceArray();
            int instanceArrayCount = _instanceArray.count();
            for (int i = 0; i < instanceArrayCount; i++) {
                MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
                anInstance._takeValueFromApplication("additionalArgs");
            }
            _defaultsPush();
        } finally {
            handler().endReading();
        }
        return _defaultPage();
    }

    /* ******** Path Wizard ******** */
    private WOComponent _pathPickerWizardClicked(String callbackKeyPath, boolean showFiles) {
        PathWizardPage1 aPage = PathWizardPage1.create(context(), myApplication());
        aPage.setCallbackKeypath(callbackKeyPath);
        aPage.setCallbackExpand("isNewInstanceSectionVisible");
        aPage.setCallbackPage(this);
        aPage.setShowFiles(showFiles);
        return aPage;
    }

    public WOComponent pathPickerWizardClickedUnix() {
        return _pathPickerWizardClicked("appDefaults.unixPath", true);
    }

    public WOComponent pathPickerWizardClickedWindows() {
        return _pathPickerWizardClicked("appDefaults.winPath", true);
    }

    public WOComponent pathPickerWizardClickedMac() {
        return _pathPickerWizardClicked("appDefaults.macPath", true);
    }

    public WOComponent pathPickerWizardClickedUnixOutput() {
        return _pathPickerWizardClicked("appDefaults.unixOutputPath", false);
    }

    public WOComponent pathPickerWizardClickedWindowsOutput() {
        return _pathPickerWizardClicked("appDefaults.winOutputPath", false);
    }

    public WOComponent pathPickerWizardClickedMacOutput() {
        return _pathPickerWizardClicked("appDefaults.macOutputPath", false);
    }

    /* ******* */

    /* ******** Email Section ******** */
    public boolean isMailingConfigured() {
        String aHost = siteConfig().SMTPhost();
        String anAddress = siteConfig().emailReturnAddr();
        if (aHost != null && aHost.length() > 0 && anAddress != null && anAddress.length() > 0) {
            return true;
        }
        return false;
    }

    public WOComponent emailUpdateClicked() {
        handler().startReading();
        try {
            handler().sendUpdateApplicationToWotaskds(myApplication(), allHosts());
        } finally {
            handler().endReading();
        }

        AppConfigurePage aPage = AppConfigurePage.create(context(), myApplication());
        aPage.isEmailSectionVisible = true;
        return aPage;
    }

    /* ******* */

    /* ******** Scheduling Section ******** */
    public boolean shouldSchedule() {
        if (myApplication().instanceArray().count() != 0)
            return true;
        return false;
    }

    public MInstance currentScheduledInstance;

    public NSArray weekList = MObject.weekArray;

    public NSArray timeOfDayList = MObject.timeOfDayArray;

    public NSArray schedulingTypeList = MObject.schedulingTypeArray;

    public NSArray schedulingIntervalList = MObject.schedulingIntervalArray;

    public String weekSelection() {
        return MObject.morphedSchedulingStartDay(currentScheduledInstance.schedulingStartDay());
    }

    public void setWeekSelection(String value) {
        currentScheduledInstance.setSchedulingStartDay(MObject.morphedSchedulingStartDay(value));
    }

    public String timeHourlySelection() {
        return MObject.morphedSchedulingStartTime(currentScheduledInstance.schedulingHourlyStartTime());
    }

    public void setTimeHourlySelection(String value) {
        currentScheduledInstance.setSchedulingHourlyStartTime(MObject.morphedSchedulingStartTime(value));
    }

    public String timeDailySelection() {
        return MObject.morphedSchedulingStartTime(currentScheduledInstance.schedulingDailyStartTime());
    }

    public void setTimeDailySelection(String value) {
        currentScheduledInstance.setSchedulingDailyStartTime(MObject.morphedSchedulingStartTime(value));
    }

    public String timeWeeklySelection() {
        return MObject.morphedSchedulingStartTime(currentScheduledInstance.schedulingWeeklyStartTime());
    }

    public void setTimeWeeklySelection(String value) {
        currentScheduledInstance.setSchedulingWeeklyStartTime(MObject.morphedSchedulingStartTime(value));
    }

    public WOComponent schedulingUpdateClicked() {
        handler().startReading();
        try {
            if ((myApplication().instanceArray().count() != 0) && (allHosts().count() != 0)) {
                handler().sendUpdateInstancesToWotaskds(myApplication().instanceArray(), allHosts());
            }
        } finally {
            handler().endReading();
        }

        AppConfigurePage aPage = AppConfigurePage.create(context(), myApplication());
        aPage.isSchedulingSectionVisible = true;
        return aPage;
    }

    /** ******* */

    /** ******** Adaptor Settings Section ******** */
    public String _loadSchedulerSelection = null;

    public String loadSchedulerItem;

    public NSArray loadSchedulerList = MObject.loadSchedulerArray;

    public Integer urlVersionItem;

    public NSArray urlVersionList = MObject.urlVersionArray;

    public String customSchedulerName;

    public String loadSchedulerSelection() {
        if (myApplication().scheduler() != null) {
            int indexOfScheduler = MObject.loadSchedulerArrayValues.indexOfObject(myApplication().scheduler());
            if (indexOfScheduler != -1) {
                _loadSchedulerSelection = (String) loadSchedulerList.objectAtIndex(indexOfScheduler);
            } else {
                // Custom scheduler
                _loadSchedulerSelection = (String) loadSchedulerList.objectAtIndex(loadSchedulerList.count() - 1);
                customSchedulerName = myApplication().scheduler();
            }
        }
        return _loadSchedulerSelection;
    }

    public void setLoadSchedulerSelection(String value) {
        _loadSchedulerSelection = value;
    }

    public Integer urlVersionSelection() {
        return myApplication().urlVersion();
    }

    public void setUrlVersionSelection(Integer value) {
        myApplication().setUrlVersion(value);
    }

    public WOComponent adaptorUpdateClicked() {
        handler().startReading();
        try {
            String newValue;
            int i = loadSchedulerList.indexOfObject(_loadSchedulerSelection);
            if (i == 0) {
                newValue = null;
            } else if (i == (loadSchedulerList.count() - 1)) {
                newValue = customSchedulerName;
                if (!String_Extensions.isValidXMLString(newValue)) {
                    newValue = null;
                }
            } else {
                newValue = (String) MObject.loadSchedulerArrayValues.objectAtIndex(i);
            }
            myApplication().setScheduler(newValue);

            handler().sendUpdateApplicationToWotaskds(myApplication(), allHosts());
        } finally {
            handler().endReading();
        }

        AppConfigurePage aPage = AppConfigurePage.create(context(), myApplication());
        aPage.isAdaptorSettingsSectionVisible = true;
        return aPage;
    }

    /**
     * Create an ApplicationConfigurePage instance for the given MApplication
     * 
     * @param context the current context
     * @param application the application object to configure
     * @return ApplicationConfigurePage
     */
	public static AppConfigurePage create(WOContext context, MApplication application) {
		AppConfigurePage page = (AppConfigurePage) context.page().pageWithName(AppConfigurePage.class.getName());
		page.setMyApplication(application);
		page.appDefaults = new MApplication(application.values(), _sc, null);
		return page;
	}

}
