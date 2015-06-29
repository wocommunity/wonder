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
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;
import er.extensions.foundation.ERXProperties;

public class NavigationPage extends MonitorComponent {
	private static final long serialVersionUID = 338657623393333855L;

	public int currentPage = APP_PAGE;

	public String title;
	public String pageId;

	public NavigationPage(WOContext aWocontext) {
		super(aWocontext);
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		AjaxUtils.addScriptResourceInHead(context, response, "Ajax", "prototype.js");
	}

	public String pageTitle() {
		return "WOMonitor: " + title;
	}

	public WOComponent ApplicationsPageClicked() {
		return ApplicationsPage.create(context());
	}

	public WOComponent HostsPageClicked() {
		return HostsPage.create(context());
	}

	public WOComponent ConfigurePageClicked() {
		return ConfigurePage.create(context());
	}

	public WOComponent PrefsPageClicked() {
		return PrefsPage.create(context());
	}

	public WOComponent HelpPageClicked() {
		return pageWithName("HelpPage");
	}

	@Deprecated
	public WOComponent MigrationPageClicked() {
		return pageWithName("MigrationPage");
	}

	public WOComponent ModProxyPageClicked() {
		return pageWithName("ModProxyPage");
	}

	public boolean logoutRequired() {
		return siteConfig() != null && (mySession().isLoggedIn() && siteConfig().isPasswordRequired());
	}

	public WOComponent logoutClicked() {
		mySession().setIsLoggedIn(false);
		return pageWithName("Main");
	}
	
	@Deprecated
	public boolean showMigrationsTab() {
		return ERXProperties.booleanForKeyWithDefault("er.javamonitor.showMigrationsTab", false);
	}

	public boolean showModProxyTab() {
		return ERXProperties.booleanForKeyWithDefault("er.javamonitor.showModProxyTab", false);
	}
}