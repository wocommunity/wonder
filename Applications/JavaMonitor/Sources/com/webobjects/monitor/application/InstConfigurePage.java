package com.webobjects.monitor.application;

/*
 © Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

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
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MObject;

public class InstConfigurePage extends MonitorComponent {

    public InstConfigurePage(WOContext aWocontext) {
        super(aWocontext);
    }

    /*
     * serialVersionUID
     */
    private static final long serialVersionUID = 2930097368474314793L;

    public boolean isWindowsHost() {
        return myInstance().host().osType().equals("WINDOWS");
    }

    public WOComponent returnClicked() {
        return AppDetailPage.create(context(), myInstance().application());
    }

    public WOComponent appConfigLinkClicked() {
        AppConfigurePage aPage = AppConfigurePage.create(context(), myApplication());
        aPage.isNewInstanceSectionVisible = true;
        return aPage;
    }

    /* ******** Startup Section ********* */
    private WOComponent _pathPickerWizardClicked(String callbackKeyPath) {
        PathWizardPage1 aPage = PathWizardPage1.create(context(), myApplication());
        aPage.setCallbackKeypath(callbackKeyPath);
        aPage.setCallbackPage(this);
        aPage.setShowFiles(true);
        return aPage;
    }

    public WOComponent pathPickerWizardClicked() {
        return _pathPickerWizardClicked("myInstance.path");
    }

    public WOComponent pathPickerWizardClickedOutput() {
        return _pathPickerWizardClicked("myInstance.outputPath");
    }

    public Integer port() {
        return myInstance().port();
    }

    public void setPort(Integer value) {
        if (value == null)
            return;
        if (!value.equals(myInstance().port())) {
            if (myInstance().state != MObject.DEAD) {
                mySession().addErrorIfAbsent("This instance is still running; unable to change port");
                return;
            }
            if (!myInstance().host().isPortInUse(value)) {
                myInstance().setPort(value);
            } else {
                mySession().addErrorIfAbsent("This port is in use");
            }
        }
    }

    public Integer id() {
        return myInstance().id();
    }
    
    public String displayName() {
        return myInstance().displayName();
    }
    
    public void setDisplayName(Object foo) {
    	// ak: should switch to non-sync
    }

    public void setId(Integer value) {
        if (value == null)
            return;
        if (!value.equals(myInstance().id())) {
            if (!myInstance().application().isIDInUse(value)) {
                myInstance().setId(value);
            } else {
                mySession().addErrorIfAbsent("This ID is in use");
            }
        }
    }

    public WOComponent startupUpdateClicked() {
        handler().startReading();
        try {
            handler().sendUpdateInstancesToWotaskds(new NSArray(myInstance()), allHosts());
        } finally {
            handler().endReading();
        }
        return null;
    }

    /* ******* */

    /* ******** Adaptor Settings Section ********* */
    public WOComponent adaptorSettingsUpdateClicked() {
        handler().startReading();
        try {
            handler().sendUpdateInstancesToWotaskds(new NSArray(myInstance()), allHosts());
        } finally {
            handler().endReading();
        }

        return null;
    }

    /* ******* */

    /* ******** Diff returns ********* */
    private static String _diffString = "<span class=\"Warning\">**</span>";

    private static String _emptyString = "";

    private boolean safeEquals(Object a, Object b) {
        if ((a == null) && (b == null)) {
            return true;
        } else if ((a != null) && (b != null)) {
            return a.equals(b);
        }
        // only 1 of the 2 is null
        if ((a instanceof String) || (b instanceof String)) {
            if ( (a == null && b != null && ((String) b).length() == 0) || (b == null && a != null && ((String) a).length() == 0) ) {
                return true;
            }
        }
        return false;
    }

    public String pathDiff() {
        MInstance myInstance = myInstance();
        MApplication myApplication = myInstance.application();
        MHost myHost = myInstance.host();
        String appPath = null;

        if (myHost.osType().equals("UNIX")) {
            appPath = myApplication.unixPath();
        } else if (myHost.osType().equals("WINDOWS")) {
            appPath = myApplication.winPath();
        } else if (myHost.osType().equals("MACOSX")) {
            appPath = myApplication.macPath();
        }

        if (!safeEquals(myInstance.path(), appPath)) {
            return _diffString;
        }
        return _emptyString;
    }

    public String minDiff() {
        MInstance myInstance = myInstance();
        MApplication myApplication = myInstance.application();
        if (!safeEquals(myInstance.minimumActiveSessionsCount(), myApplication.minimumActiveSessionsCount())) {
            return _diffString;
        }
        return _emptyString;
    }

    public String cachingDiff() {
        MInstance myInstance = myInstance();
        MApplication myApplication = myInstance.application();
        if (!safeEquals(myInstance.cachingEnabled(), myApplication.cachingEnabled())) {
            return _diffString;
        }
        return _emptyString;
    }

    public String outputDiff() {
        MInstance myInstance = myInstance();
        MApplication myApplication = myInstance.application();
        MHost myHost = myInstance.host();
        String appOutputPath = null;

        if (myHost.osType().equals("UNIX")) {
            appOutputPath = myInstance.generateOutputPath(myApplication.unixOutputPath());
        } else if (myHost.osType().equals("WINDOWS")) {
            appOutputPath = myInstance.generateOutputPath(myApplication.winOutputPath());
        } else if (myHost.osType().equals("MACOSX")) {
            appOutputPath = myInstance.generateOutputPath(myApplication.macOutputPath());
        }

        if (!safeEquals(myInstance.outputPath(), appOutputPath)) {
            return _diffString;
        }
        return _emptyString;
    }

    public String browserDiff() {
        MInstance myInstance = myInstance();
        MApplication myApplication = myInstance.application();
        if (!safeEquals(myInstance.autoOpenInBrowser(), myApplication.autoOpenInBrowser())) {
            return _diffString;
        }
        return _emptyString;
    }

    public String debugDiff() {
        MInstance myInstance = myInstance();
        MApplication myApplication = myInstance.application();
        if (!safeEquals(myInstance.debuggingEnabled(), myApplication.debuggingEnabled())) {
            return _diffString;
        }
        return _emptyString;
    }

    public String lifebeatDiff() {
        MInstance myInstance = myInstance();
        MApplication myApplication = myInstance.application();
        if (!safeEquals(myInstance.lifebeatInterval(), myApplication.lifebeatInterval())) {
            return _diffString;
        }
        return _emptyString;
    }

    public String argsDiff() {
        MInstance myInstance = myInstance();
        MApplication myApplication = myInstance.application();
        if (!safeEquals(myInstance.additionalArgs(), myApplication.additionalArgs())) {
            return _diffString;
        }
        return _emptyString;
    }

    /* ******* */

    /* ******** Force Quit support ********* */
    public WOComponent forceQuitClicked() {
        handler().sendQuitInstancesToWotaskds(new NSArray(myInstance()), new NSArray(myInstance().host()));
        return null;
    }

    public String instanceLifebeatInterval() {
        return myInstance().lifebeatInterval().toString();
    }
    /*
     * @param instance TODO ******* */

	public static InstConfigurePage create(WOContext context, MInstance instance) {
		InstConfigurePage page = (InstConfigurePage) context.page().pageWithName(InstConfigurePage.class.getName());
		page.setMyInstance(instance);
		return page;
	}

}
