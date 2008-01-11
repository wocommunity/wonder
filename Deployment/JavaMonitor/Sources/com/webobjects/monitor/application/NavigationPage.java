package com.webobjects.monitor.application;

/*
 © Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

 IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (“Apple”) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

 In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple’s copyrights in this original Apple software (the “Apple Software”), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
 SUCH DAMAGE.
 */
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResourceManager;

public class NavigationPage extends MonitorComponent {
    private static final long serialVersionUID = 338657623393333855L;

    private static final String _appBarImageName = "applications_bar.gif";

    private static final String _hostBarImageName = "hosts_bar.gif";

    private static final String _siteBarImageName = "site_bar.gif";

    private static final String _prefBarImageName = "preferences_bar.gif";

    private static final String _helpBarImageName = "help_bar.gif";

    private static final String _migrationBarImageName = "migration_bar.gif";

    private static final String[] _barImageNames = new String[] { _appBarImageName, _hostBarImageName,
            _siteBarImageName, _prefBarImageName, _helpBarImageName, _migrationBarImageName };

    private static final String _appActiveImageName = "applications_tab_active.gif";

    private static final String _hostActiveImageName = "hosts_tab_active.gif";

    private static final String _siteActiveImageName = "site_tab_active.gif";

    private static final String _prefActiveImageName = "preferences_tab_active.gif";

    private static final String _helpActiveImageName = "help_tab_active.gif";

    private static final String _migrationActiveImageName = "migration_tab_active.gif";

    private static final String _appInactiveImageName = "applications_tab_inactive.gif";

    private static final String _hostInactiveImageName = "hosts_tab_inactive.gif";

    private static final String _siteInactiveImageName = "site_tab_inactive.gif";

    private static final String _prefInactiveImageName = "preferences_tab_inactive.gif";

    private static final String _helpInactiveImageName = "help_tab_inactive.gif";

    private static final String _migrationInActiveImageName = "migration_tab_inactive.gif";

    public String appleImageName = "AppleLogo.gif";

    public String backgroundImageName = "background.gif";

    public int currentPage = APP_PAGE;

    public String title;

    public NavigationPage(WOContext aWocontext) {
        super(aWocontext);
    }

    public String pageTitle() {
        return "WOMonitor: " + title;
    }

    public String barImageName() {
        return _barImageNames[currentPage];
    }

    public String appImageName() {
        return (currentPage == APP_PAGE) ? _appActiveImageName : _appInactiveImageName;
    }

    public String hostImageName() {
        return (currentPage == HOST_PAGE) ? _hostActiveImageName : _hostInactiveImageName;
    }

    public String siteImageName() {
        return (currentPage == SITE_PAGE) ? _siteActiveImageName : _siteInactiveImageName;
    }

    public String prefImageName() {
        return (currentPage == PREF_PAGE) ? _prefActiveImageName : _prefInactiveImageName;
    }

    public String helpImageName() {
        return (currentPage == HELP_PAGE) ? _helpActiveImageName : _helpInactiveImageName;
    }

    public String migrationImageName() {
        return (currentPage == MIGRATION_PAGE) ? _migrationActiveImageName : _migrationInActiveImageName;
    }

    public String backgroundImageSrc() {
        WOResourceManager aResourceManager = application().resourceManager();
        return aResourceManager.urlForResourceNamed(backgroundImageName, null, null, context().request());
    }

    public WOComponent ApplicationsPageClicked() {
        return pageWithName("ApplicationsPage");
    }

    public WOComponent HostsPageClicked() {
        return pageWithName("HostsPage");
    }

    public WOComponent ConfigurePageClicked() {
        return pageWithName("ConfigurePage");
    }

    public WOComponent PrefsPageClicked() {
        return pageWithName("PrefsPage");
    }

    public WOComponent HelpPageClicked() {
        return pageWithName("HelpPage");
    }

    public WOComponent MigrationPageClicked() {
        return pageWithName("MigrationPage");
    }
}