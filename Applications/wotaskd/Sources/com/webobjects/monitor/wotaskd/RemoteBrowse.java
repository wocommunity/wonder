package com.webobjects.monitor.wotaskd;
/*
© Copyright 2006 - 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */

import java.io.File;

import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.xml._JavaMonitorCoder;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPathUtilities;

import er.extensions.appserver.ERXResponse;

public class RemoteBrowse extends WODirectAction  {
    private Object[] fileKeys = new Object[]{"file" , "fileType" , "fileSize"};
    private File[] roots;
    private String[] rootStrings;
    private boolean singleRoot = false;
    private String xmlRoots;
    
    public RemoteBrowse(WORequest aRequest) {
        super(aRequest);

        File[] roots = File.listRoots();
        if (roots.length <= 1) {
            singleRoot = true;
        }
        rootStrings = new String[roots.length];
        for (int i=0; i<roots.length; i++) {
            rootStrings[i] = NSPathUtilities._standardizedPath(roots[i].getAbsolutePath());
        }

        int anArrayCount = rootStrings.length;
        NSMutableArray rootArray = new NSMutableArray(anArrayCount);
        for (int i = 0; i < anArrayCount; i++) {
            NSDictionary aFileDict = new NSDictionary(new Object[]{rootStrings[i] , "NSFileTypeDirectory" , Long.valueOf(0)}, fileKeys);
            rootArray.addObject(aFileDict);
        }

        xmlRoots = ((new _JavaMonitorCoder()).encodeRootObjectForKey(rootArray, "pathArray")) + " \r\n";
    }

    public NSArray fileListForStartingPath(String aStartingPath, boolean showFiles) {
        File startingPathAsFile = new File(aStartingPath);
        NSMutableArray aDirectoryArray = new NSMutableArray();
        NSMutableArray aFileArray = new NSMutableArray();
        NSArray tempArray = null;
        Object[] contentsOfStartingPath = null;

        if ( !(startingPathAsFile.exists()) ) {
            return null;
        }

        contentsOfStartingPath = startingPathAsFile.list();
        tempArray = new NSArray(contentsOfStartingPath);

        try {
            tempArray = tempArray.sortedArrayUsingComparator(NSComparator.AscendingStringComparator);
            contentsOfStartingPath = tempArray.objects();
        } catch (NSComparator.ComparisonException e) {
            // do nothing
        }

        int anArrayCount = contentsOfStartingPath.length;

        for (int i = 0; i < anArrayCount; i++) {
            String aFile = (String)contentsOfStartingPath[i];
            String aFileType;
            Long aFileSize;
            Object aFileDict;

            String fullPath = NSPathUtilities.stringByAppendingPathComponent(aStartingPath, aFile);
            fullPath = NSPathUtilities._standardizedPath(fullPath);
            File subfile = new File(fullPath);

            if (subfile.isDirectory()) {
                aFileType = "NSFileTypeDirectory";
                aFileSize = Long.valueOf(0);
            } else {
                aFileType = "NSFileTypeRegular";
                aFileSize = Long.valueOf(subfile.length());
            }

            aFileDict = new NSDictionary(new Object[]{aFile , aFileType , aFileSize}, fileKeys);

            if (aFileType.equals("NSFileTypeDirectory")) {
                aDirectoryArray.addObject(aFileDict);
            } else {
                aFileArray.addObject(aFileDict);
            }
        }
        if (showFiles) {
            aDirectoryArray.addObjectsFromArray(aFileArray);
        }
        return aDirectoryArray;
    }


    public WOResponse getPathAction() {
        WORequest aRequest = request();
        ERXResponse aResponse = new ERXResponse();

        if (aRequest.isUsingWebServer())  {
            aResponse.setStatus(WOMessage.HTTP_STATUS_FORBIDDEN);
            aResponse.appendContentString("Access Denied");
            return aResponse;
        }
        String aPath = aRequest.headerForKey("filepath");
        boolean showFiles = (aRequest.headerForKey("showFiles") != null) ? true : false;

        // looking for roots, or root listing of only 1 root
        if (aPath == null && !singleRoot) {
            aResponse.appendContentString(xmlRoots);
            aResponse.setHeader("YES", "isRoots");
        } else {
            if (aPath == null) aPath = rootStrings[0];
            NSArray anArray = fileListForStartingPath(aPath, showFiles);

            if (anArray == null) {
                aResponse.appendContentString("ERROR");
            } else {
                _JavaMonitorCoder aCoder = new _JavaMonitorCoder();
                String anXMLString = null;
                anXMLString = aCoder.encodeRootObjectForKey(anArray, "pathArray");
                anXMLString = (anXMLString) + " \r\n";
                aResponse.appendContentString(anXMLString);
                aResponse.setHeader(aPath, "filepath");
            }
        }
        return aResponse;
    }

}
