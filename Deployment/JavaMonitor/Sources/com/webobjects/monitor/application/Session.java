package com.webobjects.monitor.application;
/*
© Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (“Apple”) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple’s copyrights in this original Apple software (the “Apple Software”), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSThreadsafeMutableArray;
import com.webobjects.foundation._NSThreadsafeMutableDictionary;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MSiteConfig;

public class Session extends WOSession  {
	private static final long	serialVersionUID	= 8067267944038698356L;
	public boolean  _isLoggedIn;
    private Application theApplication;

    public MApplication mApplication;
    public MInstance mInstance;
    public MSiteConfig mSiteConfig;
    public MHost mHost;

    public Session() {
        super();
        _isLoggedIn = false;
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("\nSystem Properties are: " + System.getProperties() + "\n");

        theApplication = (Application)WOApplication.application();
        return;
    }

    public boolean  isLoggedIn() {
        return _isLoggedIn;
    }

    public void  setIsLoggedIn(boolean  aBOOL) {
        _isLoggedIn = aBOOL;
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
        // Check to make sure they have logged in if it is required
        MSiteConfig aMonitorConfig = theApplication.siteConfig();

        if ( (aMonitorConfig == null) || (aMonitorConfig.isPasswordRequired()) ) {
            if (_isLoggedIn) {
                super.appendToResponse(aResponse, aContext);
            } else {
                if (aContext.page().getClass().getName().equals("Main")) {
                    // needs to login on Main page.
                    super.appendToResponse(aResponse, aContext);
                } else {
                    NSLog.err.appendln("Tried to access " + (aContext.page()) + " while not logged in.");
                }
            }
        } else {
            super.appendToResponse(aResponse, aContext);
        }
    }


    
    /********** Error/Informational Messages **********/
    _NSThreadsafeMutableArray errorMessageArray = new _NSThreadsafeMutableArray(new NSMutableArray<Object>());
    public String message() {
        String _message = null;
        if (theApplication.siteConfig() != null) {
            NSArray globalArray = theApplication.siteConfig().globalErrorDictionary.allValues();
            if ( (globalArray != null) && (globalArray.count() > 0) ) {
                addObjectsFromArrayIfAbsentToErrorMessageArray(globalArray);
                theApplication.siteConfig().globalErrorDictionary = new _NSThreadsafeMutableDictionary(new NSMutableDictionary<Object, Object>());
            }
        }
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("message(): " + errorMessageArray.array());
        if ( (errorMessageArray != null) && (errorMessageArray.count() > 0) ) {
            _message = errorMessageArray.componentsJoinedByString(", ");
            errorMessageArray = new _NSThreadsafeMutableArray(new NSMutableArray<Object>());
        }
        return _message;
    }
    public void addObjectsFromArrayIfAbsentToErrorMessageArray(NSArray anArray) {
        if (anArray != null && anArray.count() > 0) {
            int arrayCount = anArray.count();
            for (int i=0; i < arrayCount; i++) {
                errorMessageArray.addObjectIfAbsent(anArray.objectAtIndex(i));
            }
        }
    }
    /**********/
}
