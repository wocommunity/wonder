/*
© Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (“Apple”) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple’s copyrights in this original Apple software (the “Apple Software”), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */
package com.webobjects.monitor._private;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOHTTPConnection;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.xml._JavaMonitorCoder;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSThreadsafeMutableArray;

public class MHost extends MObject {
    /*
     * NSString name; NSString type; // WINDOWS | UNIX | MACOSX
     */

    /** ******** 'values' accessors ********* */
    public String name() {
        return (String) values.valueForKey("name");
    }

    public void setName(String value) {
        values.takeValueForKey(value, "name");
        _siteConfig.dataHasChanged();
    }

    public String type() {
        return (String) values.valueForKey("type");
    }

    public void setType(String value) {
        values.takeValueForKey(MObject.validatedHostType(value), "type");
        _siteConfig.dataHasChanged();
    }

    /** ******* */

    /** ******** Object Graph ********* */
    NSMutableArray _instanceArray;

    NSMutableArray _applicationArray = new NSMutableArray();

    public NSMutableArray instanceArray() {
        return _instanceArray;
    }

    public NSArray applicationArray() {
        return _applicationArray;
    }

    /** ******* */

    /** ******** Constructors ********* */
    // From the UI
    public MHost(MSiteConfig aConfig, String name, String type) {
        this(new NSDictionary<Object, Object>(new Object[] { name, type }, new Object[] { "name", "type" }), aConfig);
    }

    // Unarchiving or Monitor Update
    public MHost(NSDictionary aValuesDict, MSiteConfig aConfig) {
        values = new NSMutableDictionary(aValuesDict);
        _siteConfig = aConfig;
        _instanceArray = new NSMutableArray();

        try {
            _address = InetAddress.getByName(name());
        } catch (UnknownHostException anException) {
            NSLog.err.appendln("Error getting address for Host: " + name());
        }

        // This is just for caching purposes
        errorResponse = (new _JavaMonitorCoder()).encodeRootObjectForKey(new NSDictionary<String, NSArray>(new NSArray(
                "Failed to contact " + name() + "-" + WOApplication.application().lifebeatDestinationPort()),
                "errorResponse"), "instanceResponse");

    }

    /** ******* */

    /** ******** Adding and Removing Instance Primitives ********* */
    public void _addInstancePrimitive(MInstance anInstance) {
        _instanceArray.addObject(anInstance);
        if (!_applicationArray.containsObject(anInstance._application)) {
            _applicationArray.addObject(anInstance._application);
        }
    }

    public void _removeInstancePrimitive(MInstance anInstance) {
        _instanceArray.removeObject(anInstance);
        // get the instances's host - check all the other instances that this
        // application has to see if any other ones have that host
        // if not, remove it.
        boolean uniqueApplication = true;
        for (Enumeration e = _instanceArray.objectEnumerator(); e.hasMoreElements();) {
            MInstance anInst = (MInstance) e.nextElement();
            if (anInstance._application == anInst._application) {
                uniqueApplication = false;
                break;
            }
        }
        if (uniqueApplication) {
            _applicationArray.removeObject(anInstance._application);
        }
    }

    /** ******* */

    /** ******** InetAddress stuff ********* */
    private InetAddress _address = null;

    public InetAddress address() {
        return _address;
    }

    public String addressAsString() {
        if (_address != null) {
            return _address.getHostAddress();
        }
        return "Unknown";
    }

    /** ******* */

    @Override
    public boolean equals(Object other) {
        return (other instanceof MHost) && (((MHost) other)._address.equals(_address));
    }

    @Override
    public int hashCode() {
        return _address.hashCode();
    }

    /** ******** Archiving Support ********* */
    public NSDictionary dictionaryForArchive() {
        return values;
    }

    public String toString() {
        if (false) {
            return values.toString() + " " + "address = " + _address + " " + "runningInstances = " + runningInstances
                    + " " + "operatingSystem = " + operatingSystem + " " + "processorType = " + processorType + " ";
        }
        return "MHost@" + _address;
    }

    /** ******* */

    public Integer runningInstancesCount_W() {
        int runningInstances = 0;
        int numInstances = _instanceArray.count();
        for (int i = 0; i < numInstances; i++) {
            MInstance anInstance = (MInstance) _instanceArray.objectAtIndex(i);
            if (anInstance.isRunning_W()) {
                runningInstances++;
            }
        }
        return new Integer(runningInstances);
    }

    public boolean isPortInUse(Integer port) {
        if (instanceWithPort(port) == null)
            return false;
        else
            return true;
    }

    // KH - this is probably slow :)
    public Integer nextAvailablePort(Integer startingPort) {
        Integer retVal = null;
        do {
            if (isPortInUse(startingPort)) {
                startingPort = new Integer(startingPort.intValue() + 1);
            } else {
                retVal = startingPort;
            }
        } while (retVal == null);
        return retVal;
    }

    public MInstance instanceWithPort(Integer port) {
        int instanceArrayCount = _instanceArray.count();
        for (int i = 0; i < instanceArrayCount; i++) {
            MInstance anInst = (MInstance) _instanceArray.objectAtIndex(i);
            if (anInst.port().equals(port)) {
                return anInst;
            }
        }
        return null;
    }

    /**
     * ******** Machine Information and Availability Check (Used by MONITOR)
     * *********
     */
    public String runningInstances = "?";

    public String operatingSystem = "?";

    public String processorType = "?";

    public boolean isAvailable = false;

    public void _setHostInfo(NSDictionary _hostStats) {
        Object aValue = null;

        aValue = _hostStats.valueForKey("runningInstances");
        if (aValue != null)
            runningInstances = aValue.toString();

        aValue = _hostStats.valueForKey("operatingSystem");
        if (aValue != null)
            operatingSystem = aValue.toString();

        aValue = _hostStats.valueForKey("processorType");
        if (aValue != null)
            processorType = aValue.toString();
    }

    /** ******* */

    /** ******** Communications Goop ********* */
    public static WOResponse[] sendRequestToWotaskdArray(NSData content, NSArray wotaskdArray, boolean willChange) {
        MSiteConfig aConfig;
        MHost aHost = (MHost) wotaskdArray.objectAtIndex(0);
        if (aHost != null) {
            aConfig = aHost.siteConfig();
        } else {
            return null;
        }

        // we had errors reaching a host last time - do it again!
        if (aConfig.hostErrorArray.count() > 0) {
            _syncRequest = null;
            final WORequest aSyncRequest = syncRequest(aConfig);
            final _NSThreadsafeMutableArray syncHosts = aConfig.hostErrorArray;
            if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
                NSLog.debug.appendln("Sending sync requests to: " + syncHosts.array());
            // final MSiteConfig finalConfig = aConfig;
            Thread[] workers = new Thread[syncHosts.count()];
            for (int i = 0; i < workers.length; i++) {
                final int j = i;
                Runnable work = new Runnable() {
                    public void run() {
                        MHost aHost = (MHost) syncHosts.objectAtIndex(j);
                        aHost.sendRequestToWotaskd(aSyncRequest, true, true);
                    }
                };
                workers[j] = new Thread(work);
                workers[j].start();
            }

            try {
                for (int i = 0; i < workers.length; i++) {
                    workers[i].join();
                }
            } catch (InterruptedException ie) {
            }
        }

        final WORequest aRequest = new WORequest(MObject._POST, MObject.directActionString, MObject._HTTP1, aConfig
                .passwordDictionary(), content, null);
        final NSArray finalWotaskdArray = wotaskdArray;
        final boolean wc = willChange;

        Thread[] workers = new Thread[wotaskdArray.count()];
        final WOResponse[] responses = new WOResponse[workers.length];

        for (int i = 0; i < workers.length; i++) {
            final int j = i;
            Runnable work = new Runnable() {
                public void run() {
                    responses[j] = ((MHost) finalWotaskdArray.objectAtIndex(j)).sendRequestToWotaskd(aRequest, wc,
                            false);
                }
            };
            workers[j] = new Thread(work);
            workers[j].start();
        }

        try {
            for (int i = 0; i < workers.length; i++) {
                workers[i].join();
            }
        } catch (InterruptedException ie) {
            // might be bad?
        }

        return responses;
    }

    private static WORequest _syncRequest = null;

    private static WORequest syncRequest(MSiteConfig aConfig) {
        if (_syncRequest == null) {
            NSMutableDictionary<String, NSDictionary> data = new NSMutableDictionary<String, NSDictionary>(aConfig
                    .dictionaryForArchive(), "SiteConfig");
            NSMutableDictionary<String, NSMutableDictionary<String, NSDictionary>> updateWotaskd = new NSMutableDictionary<String, NSMutableDictionary<String, NSDictionary>>(
                    data, "sync");
            NSMutableDictionary<String, NSMutableDictionary<String, NSMutableDictionary<String, NSDictionary>>> monitorRequest = new NSMutableDictionary<String, NSMutableDictionary<String, NSMutableDictionary<String, NSDictionary>>>(
                    updateWotaskd, "updateWotaskd");
            NSData content = new NSData((new _JavaMonitorCoder()).encodeRootObjectForKey(monitorRequest,
                    "monitorRequest"));
            _syncRequest = new WORequest(MObject._POST, MObject.directActionString, MObject._HTTP1, aConfig
                    .passwordDictionary(), content, null);
        }
        return _syncRequest;
    }

    private String errorResponse = null;

    public WOResponse sendRequestToWotaskd(WORequest aRequest, boolean willChange, boolean isSync) {
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("!@#$!@#$ sendRequestToWotaskd creates a WOHTTPConnection");
        WOResponse aResponse = null;

        try {
            WOHTTPConnection anHTTPConnection = new WOHTTPConnection(name(), WOApplication.application()
                    .lifebeatDestinationPort());
            anHTTPConnection.setReceiveTimeout(10000);

            boolean requestSucceeded = anHTTPConnection.sendRequest(aRequest);

            isAvailable = true;

            if (requestSucceeded) {
                aResponse = anHTTPConnection.readResponse();
            } else {
                isAvailable = false;
            }

            if (aResponse == null) {
                isAvailable = false;
            }
        } catch (Throwable localException) {
            if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
                NSLog.err.appendln(localException);
            isAvailable = false;
        }

        // For error handling
        if (aResponse == null) {
            if (willChange) {
                _siteConfig.hostErrorArray.addObjectIfAbsent(this);
            }
            aResponse = new WOResponse();
            aResponse.setContent(errorResponse);
        } else {
            // if we successfully synced, clear the error dictionary
            if (isSync && isAvailable) {
                if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
                    NSLog.debug.appendln("Cleared sync request for host " + name());
                _siteConfig.hostErrorArray.removeObject(this);
            }
        }

        return aResponse;
    }
}
