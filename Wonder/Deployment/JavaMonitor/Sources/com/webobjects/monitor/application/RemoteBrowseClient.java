package com.webobjects.monitor.application;
/*
© Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (“Apple”) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple’s copyrights in this original Apple software (the “Apple Software”), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */
import java.util.List;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOHTTPConnection;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.xml.WOXMLException;
import com.webobjects.appserver.xml._JavaMonitorDecoder;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.MonitorException;

public class RemoteBrowseClient extends MonitorComponent {
	private static final long	serialVersionUID	= 3929193699509459110L;
	static private byte[] evilHack = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>".getBytes();

    static public NSDictionary _getFileListOutOfResponse(WOResponse aResponse, String thePath) throws MonitorException {
        NSData responseContent = aResponse.content();
        NSArray anArray = NSArray.EmptyArray;
        if (responseContent != null) {
            byte[] responseContentBytes = responseContent.bytes();
            String responseContentString = new String(responseContentBytes);
            if (responseContentString.startsWith("ERROR")) {
                throw new MonitorException("Path " + thePath + " does not exist");
            } else {
                _JavaMonitorDecoder aDecoder = new _JavaMonitorDecoder();
                try {
                	byte[] evilHackCombined = new byte[responseContentBytes.length + evilHack.length];
                    //System.arraycopy(src, src_pos, dst, dst_pos, length);
                    System.arraycopy(evilHack, 0, evilHackCombined, 0, evilHack.length);
                    System.arraycopy(responseContentBytes, 0, evilHackCombined, evilHack.length, responseContentBytes.length);
                    anArray = (NSArray)aDecoder.decodeRootObject(new NSData(evilHackCombined));
                } catch (WOXMLException wxe) {
                    NSLog.err.appendln("RemoteBrowseClient _getFileListOutOfResponse Error decoding response: " + responseContentString);
                    throw new MonitorException("Host returned bad response for path " + thePath);
                }
            }
        } else {
            NSLog.err.appendln("RemoteBrowseClient _getFileListOutOfResponse Error decoding null response");
            throw new MonitorException("Host returned null response for path " + thePath);
        }

        String isRoots = (String) aResponse.headerForKey("isRoots");
        String filepath = (String) aResponse.headerForKey("filepath");

        NSMutableDictionary aDict = new NSMutableDictionary();
        aDict.takeValueForKey(isRoots, "isRoots");
        aDict.takeValueForKey(filepath, "filepath");
        aDict.takeValueForKey(anArray, "fileArray");
        
        return aDict;
    }

    private static String getPathString = "/cgi-bin/WebObjects/wotaskd.woa/wa/RemoteBrowse/getPath";

    static public NSDictionary fileListForStartingPathHost(String aString, MHost aHost, boolean showFiles) throws MonitorException {
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("!@#$!@#$ fileListForStartingPathHost creates a WOHTTPConnection");
        NSDictionary aFileListDictionary = null;
        try {
            Application theApplication = (Application) WOApplication.application();
            WOHTTPConnection anHTTPConnection = new WOHTTPConnection(aHost.name(), theApplication.lifebeatDestinationPort());
            NSMutableDictionary<String, NSMutableArray<String>> aHeadersDict = (NSMutableDictionary<String, NSMutableArray<String>>)theApplication.siteConfig().passwordDictionary().mutableClone();
            WORequest aRequest = null;
            WOResponse aResponse = null;
            boolean requestSucceeded = false;
        	aHeadersDict.setObjectForKey(new NSMutableArray <String>(aString != null && aString.length() > 0 ? aString : "/Library/WebObjects"), "filepath");
        	if (showFiles) {
                aHeadersDict.setObjectForKey(new NSMutableArray <String>("YES"), "showFiles");
            }

            aRequest = new WORequest(MObject._GET, RemoteBrowseClient.getPathString, MObject._HTTP1, aHeadersDict, null, null);
            anHTTPConnection.setReceiveTimeout(5000);

            requestSucceeded = anHTTPConnection.sendRequest(aRequest);

            if (requestSucceeded) {
                aResponse = anHTTPConnection.readResponse();
            }

            if ( (aResponse == null) || (!requestSucceeded) || (aResponse.status() != 200) ) {
                throw new MonitorException("Error requesting directory listing for " + aString + " from " + aHost.name());
            } else {
                try {
                    aFileListDictionary = _getFileListOutOfResponse(aResponse, aString);
                } catch (MonitorException me) {
                    if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelCritical, NSLog.DebugGroupDeployment))
                        NSLog.debug.appendln("caught exception: " + me);
                    throw me;
                }
            }
            aHost.isAvailable = true;
        } catch (MonitorException me) {
            aHost.isAvailable = true;
            throw me;
        } catch (Exception localException) {
            aHost.isAvailable = false;
            NSLog.err.appendln("Exception requesting directory listing: ");
            localException.printStackTrace();
            throw new MonitorException("Exception requesting directory listing for " + aString + " from " + aHost.name() + ": " + localException.toString());
        }
        return aFileListDictionary;
    }
}
