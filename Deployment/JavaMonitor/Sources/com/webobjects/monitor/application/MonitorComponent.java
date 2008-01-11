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
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.MSiteConfig;

public class MonitorComponent extends WOComponent {

    private static final long serialVersionUID = -1880897151494772932L;

    public final int APP_PAGE = 0;

    public final int HOST_PAGE = 1;

    public final int SITE_PAGE = 2;

    public final int PREF_PAGE = 3;

    public final int HELP_PAGE = 4;

    public final int MIGRATION_PAGE = 5;

    public Application theApplication = (Application) WOApplication.application();

    private WOTaskdHandler _handler;

    public MonitorComponent(WOContext aWocontext) {
        super(aWocontext);
        _handler = new WOTaskdHandler(mySession());
    }

    protected NSMutableArray allHosts() {
        return siteConfig().hostArray();
    }

    protected MSiteConfig siteConfig() {
        return WOTaskdHandler.siteConfig();
    }

    public Session mySession() {
        return (Session) super.session();
    }

    public WOTaskdHandler handler() {
        return _handler;
    }

    public WOComponent pageWithName(String aName) {
        handler().startReading();
        try {
            _cacheState(aName);
        } finally {
            handler().endReading();
        }
        return super.pageWithName(aName);
    }

    // KH - we should probably set the instance information as we get the
    // responses, to avoid waiting, then doing it in serial! (not that it's
    // _that_ slow)
    private void _cacheState(String aName) {
        MApplication appForDetailPage = mySession().mApplication;

        if (siteConfig().hostArray().count() != 0) {
            if (aName.equals("ApplicationsPage") && (siteConfig().applicationArray().count() != 0)) {

                for (Enumeration e = siteConfig().applicationArray().objectEnumerator(); e.hasMoreElements();) {
                    MApplication anApp = (MApplication) e.nextElement();
                    anApp.runningInstancesCount = MObject._zeroInteger;
                }
                NSArray<MHost> hostArray = siteConfig().hostArray();
                handler().getApplicationStatusForHosts(hostArray);
            } else if (aName.equals("AppDetailPage")) {
                NSArray<MHost> hostArray = appForDetailPage.hostArray();

                handler().getInstanceStatusForHosts(hostArray);
            } else if (aName.equals("HostsPage")) {
                NSArray<MHost> hostArray = siteConfig().hostArray();

                handler().getHostStatusForHosts(hostArray);
            }
        }
    }
}
