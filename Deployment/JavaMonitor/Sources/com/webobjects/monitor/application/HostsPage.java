package com.webobjects.monitor.application;
/*
© Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (ÒAppleÓ) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under AppleÕs copyrights in this original Apple software (the ÒApple SoftwareÓ), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOHTTPConnection;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.String_Extensions;

public class HostsPage extends MonitorComponent  {

    public HostsPage(WOContext aWocontext) {
        super(aWocontext);
    }
    /**
	 * serialVersionUID
	 */
	private static final long	serialVersionUID	= -4009657723964398636L;
	public MHost currentHost;
    public String newHostName;
    public String hostTypeSelection;

    public NSArray hostTypeList = MObject.hostTypeArray;

    public WOComponent addHostClicked() {
        String nullOrError = null;
        
        if (newHostName != null && (newHostName.length() > 0) && (String_Extensions.isValidXMLString(newHostName)) ) {
            try {
                InetAddress anAddress = InetAddress.getByName(newHostName);
                
                theApplication._lock.startWriting();
                try {
                    if (newHostName.equalsIgnoreCase("localhost") || newHostName.equals("127.0.0.1")) {
                        // only allow this to happen if we have no other hosts!
                        if ( (theApplication.siteConfig().hostArray() != null) && (theApplication.siteConfig().hostArray().count() == 0) ) {
                            //we're OK to add localhost.
                            nullOrError = null;
                        } else {
                            nullOrError = "Hosts named localhost or 127.0.0.1 may not be added while other hosts are configured.";
                        }
                    } else {
                        // this is for non-localhost hosts
                        // only allow this to happen if localhost/127.0.0.1 doesn't already exist!
                        if (theApplication.siteConfig().localhostOrLoopbackHostExists()) {
                            nullOrError = "Additional hosts may not be added while a host named localhost or 127.0.0.1 is configured.";
                        } else {
                            //we're OK to add localhost.
                            nullOrError = null;
                        }
                    }

                    if ( (nullOrError == null) && (theApplication.siteConfig().hostWithAddress(anAddress) == null) ) {
                        if (hostMeetsMinimumVersion(anAddress)) {

                            MHost host = new MHost(theApplication.siteConfig(), newHostName, hostTypeSelection.toUpperCase());

                            // To avoid overwriting hosts
                            NSArray tempHostArray = new NSArray(theApplication.siteConfig().hostArray());
                            theApplication.siteConfig().addHost_M(host);

                            sendOverwriteToWotaskd(host);

                            if (tempHostArray.count() != 0) {
                                sendAddHostToWotaskds(host, tempHostArray);
                            }

                        } else {
                            mySession().errorMessageArray.addObjectIfAbsent("The wotaskd on " + newHostName + " is an older version, please upgrade before adding...");
                        }

                    } else {
                        if (nullOrError != null) {
			    mySession().errorMessageArray.addObjectIfAbsent(nullOrError);
                        } else {
                            mySession().errorMessageArray.addObjectIfAbsent("The host " + newHostName + " has already been added");
                        }
                    }
                } finally {
                    theApplication._lock.endWriting();
                }
            } catch (UnknownHostException ex) {
                mySession().errorMessageArray.addObjectIfAbsent("ERROR: Cannot find IP address for hostname: " + newHostName);
            }
        } else {
            mySession().errorMessageArray.addObjectIfAbsent(newHostName + " is not a valid hostname");
        }
        newHostName = null;

        return pageWithName("HostsPage");
    }


    public WOComponent removeHostClicked() {
        mySession().mHost = currentHost;
        return pageWithName("HostConfirmDeletePage");
    }

    public WOComponent configureHostClicked() {
        mySession().mHost = currentHost;
        return pageWithName("HostConfigurePage");
    }

    public WOComponent displayWotaskdInfoClicked() {
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("!@#$!@#$ displayWotaskdInfoClicked creates a WOHTTPConnection");
        WotaskdInfoPage aPage = (WotaskdInfoPage) pageWithName("WotaskdInfoPage");
        WORequest aRequest = new WORequest(MObject._POST, "/", MObject._HTTP1, theApplication.siteConfig().passwordDictionary(), null, null);

        WOResponse aResponse = null;

        try {
            WOHTTPConnection anHTTPConnection = new WOHTTPConnection(currentHost.name(), theApplication.lifebeatDestinationPort());
            anHTTPConnection.setReceiveTimeout(10000);

            if (anHTTPConnection.sendRequest(aRequest)) {
                aResponse = anHTTPConnection.readResponse();
            }
        } catch(Throwable localException) {
            NSLog._conditionallyLogPrivateException(localException);
        }
        if (aResponse == null) {
            aPage.wotaskdText = "Failed to get response from wotaskd " + currentHost.name() + ": " + WOApplication.application().lifebeatDestinationPort();
        } else {
            aPage.wotaskdText = aResponse.contentString();
        }
        return aPage;
    }

    
    private boolean hostMeetsMinimumVersion(InetAddress anAddress) {
        byte[] versionRequest;
        
        try {
            versionRequest = ("womp://queryVersion").getBytes("UTF8");
        } catch (UnsupportedEncodingException uee) {
            versionRequest = ("womp://queryVersion").getBytes();
        }
        DatagramPacket outgoingPacket = new DatagramPacket(versionRequest, versionRequest.length, anAddress, WOApplication.application().lifebeatDestinationPort());

        byte[] mbuffer = new byte[1000];
        DatagramPacket incomingPacket = new DatagramPacket(mbuffer, mbuffer.length);
        DatagramSocket socket = null;
        
        try {
            socket = new DatagramSocket();
            socket.send(outgoingPacket);
            incomingPacket.setLength(mbuffer.length);
            socket.setSoTimeout(2000);
            socket.receive(incomingPacket);
            String reply = new String(incomingPacket.getData());
            if (reply.startsWith("womp://replyVersion/")) {
                int lastIndex = reply.lastIndexOf(":webObjects");
                lastIndex += 11;
                String version = reply.substring(lastIndex);
                if (version.equals("4.5")) {
                    return false;
                }
            } else {
                return false;
            }
        } catch (InterruptedIOException iioe) {
            return true;
        } catch (SocketException se) {
            return true;
        } catch(Throwable e) {
            return false;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
        
        return true;
    }

}