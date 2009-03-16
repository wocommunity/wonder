package com.webobjects.monitor.wotaskd;
/*
© Copyright 2006 - 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (ÒAppleÓ) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under AppleÕs copyrights in this original Apple software (the ÒApple SoftwareÓ), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOHostUtilities;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.monitor._private.MInstance;

public class LifebeatRequestHandler extends WORequestHandler {
    InetAddress myInetAddress;
    String myName;
    Application theApplication;
    WOResponse BadLifebeatResponse, GoodResponse, DieResponse;

    public LifebeatRequestHandler() {
        super();
        theApplication = ((Application)WOApplication.application());

        myInetAddress = theApplication.hostAddress();
        myName = myInetAddress.getHostName();

        GoodResponse = theApplication.createResponseInContext(null);
        GoodResponse.setStatus(200); // OK
        GoodResponse.setHTTPVersion("HTTP/1.1");
        
        BadLifebeatResponse = theApplication.createResponseInContext(null);
        BadLifebeatResponse.setStatus(400); // Bad Request
        BadLifebeatResponse.setHTTPVersion("HTTP/1.0");
            
        DieResponse = theApplication.createResponseInContext(null);
        DieResponse.setStatus(500); // InternalServerError -> Die Immediately
        DieResponse.setHTTPVersion("HTTP/1.0");
    }

    public WOResponse handleRequest(WORequest aRequest) {
        WOResponse aResponse  = null;

        // Sadly, we do regenerate in the case of random lifebeats. Hopefully this won't be too often.
        // Didn't pull this out so that we can rely on isUsingWebServer to catch some bad requests
        if ( (!aRequest.isUsingWebServer()) && (WOHostUtilities.isLocalInetAddress(aRequest._originatingAddress(), true)) ) {
            Object lock = WOApplication.application().requestHandlingLock();
            if (lock!=null) {
                synchronized(lock) {
                    return _handleRequest(aRequest);
                }
            } else {
                return _handleRequest(aRequest);
            }
        }
        return null;
    }

    private WOResponse _handleRequest(WORequest aRequest) {
        WOResponse aResponse = BadLifebeatResponse;

        // http://localhost:1085/cgi-bin/WebObjects/wotaskd.woa/wlb?<notification name>&<instance name>&<hostname>&<port>
        // <notification name> = "hasStarted", "lifebeat", "willStop", "willCrash"

        NSArray values = NSArray.componentsSeparatedByString(aRequest.queryString(), "&");
        if ( (values == null) || (values.count() != 4) ) {
            theApplication.siteConfig().globalErrorDictionary.takeValueForKey((myName + ": Received bad lifebeat: " + aRequest.queryString()), aRequest.queryString());
            NSLog.err.appendln(myName + ": Received bad lifebeat: " + aRequest.queryString());
        } else {
            String notificationType = (String)values.objectAtIndex(0);
            String instanceName = (String)values.objectAtIndex(1);
            String host = (String)values.objectAtIndex(2);
            String port = (String)values.objectAtIndex(3);

            if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDeployment))
                NSLog.debug.appendln("@@@@@ Received Lifebeat: " + notificationType + " " + instanceName + " " + host + " " + port);

            if (notificationType.equals("lifebeat")) {
                // app is still alive - update registration
                // if app is not yet registered, register
                // if the instance should die, return DieResponse
                if (registerLifebeat(instanceName, host, port) == false) {
                    aResponse = DieResponse;
                } else {
		    aResponse = GoodResponse;
                }
            } else if (notificationType.equals("hasStarted")) {
                // app has just started - register instance
                registerStart(instanceName, host, port);
                aResponse = GoodResponse;
            } else if (notificationType.equals("willStop")) {
                // app will stop - mark as dead
                registerStop(instanceName, host, port);
                aResponse = null;
            } else if (notificationType.equals("willCrash")) {
                // app will crash - mark as dead, email notification
                registerCrash(instanceName, host, port);
                aResponse = null;
            } else {
                theApplication.siteConfig().globalErrorDictionary.takeValueForKey((myName + ": Received bad lifebeat: " + aRequest.queryString()), aRequest.queryString());
                NSLog.err.appendln(myName + ": Received bad lifebeat: " + aRequest.queryString());
            }
        }
        if ("HTTP/1.0".equals(aRequest.httpVersion())) {
            aResponse = null;
        }

        return aResponse;
    }


    private void registerStart(String instanceName, String host, String port) {
        // KH - can we cache this for better speed?
        InetAddress hostAddress = null;
        try {
            hostAddress = InetAddress.getByName(host);
        } catch (UnknownHostException uhe) {
        }

        theApplication._lock.startReading();
        try {
            MInstance instance = ((Application) WOApplication.application()).siteConfig().instanceWithHostAndPort(instanceName, hostAddress, port);

            if (instance != null) {
                instance.startRegistration(new NSTimestamp());
                instance.setShouldDie(false);
            } else {
                ((Application) WOApplication.application()).localMonitor().registerUnknownInstance(instanceName, host, port);
            }
        } finally {
            theApplication._lock.endReading();
        }
    }

    private boolean registerLifebeat(String instanceName, String host, String port) {
        // KH - can we cache this for better speed?
        InetAddress hostAddress = null;
        try {
            hostAddress = InetAddress.getByName(host);
        } catch (UnknownHostException uhe) {
        }

        theApplication._lock.startReading();
        try {
            MInstance instance = ((Application) WOApplication.application()).siteConfig().instanceWithHostAndPort(instanceName, hostAddress, port);

            if (instance != null) {
                instance.updateRegistration(new NSTimestamp());
                // This call will reset shouldDie status!;
                return !instance.shouldDieAndReset();
            } else {
                ((Application) WOApplication.application()).localMonitor().registerUnknownInstance(instanceName, host, port);
            }
        } finally {
            theApplication._lock.endReading();
        }
        return true;
    }

    private void registerStop(String instanceName, String host, String port) {
        // app will stop in a good way - we requested it.
        InetAddress hostAddress = null;
        try {
            hostAddress = InetAddress.getByName(host);
        } catch (UnknownHostException uhe) {
        }
        theApplication._lock.startReading();
        try {
            MInstance instance = ((Application) WOApplication.application()).siteConfig().instanceWithHostAndPort(instanceName, hostAddress, port);
            if (instance != null) {
                instance.registerStop(new NSTimestamp());
                instance.setShouldDie(false);
            }
        } finally {
            theApplication._lock.endReading();
        }
    }

    private void registerCrash(String instanceName, String host, String port) {
        // app will stop in a bad way - notify if necessary
        InetAddress hostAddress = null;
        try {
            hostAddress = InetAddress.getByName(host);
        } catch (UnknownHostException uhe) {
        }
        theApplication._lock.startReading();
        try {
            MInstance instance = ((Application) WOApplication.application()).siteConfig().instanceWithHostAndPort(instanceName, hostAddress, port);

            if (instance != null) {
                instance.registerCrash(new NSTimestamp());
                instance.setShouldDie(false);
            }
        } finally {
            theApplication._lock.endReading();
        }
    }
}
