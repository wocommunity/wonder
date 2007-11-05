package com.webobjects.monitor.application;
/*
© Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (ÒAppleÓ) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under AppleÕs copyrights in this original Apple software (the ÒApple SoftwareÓ), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSPathUtilities;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MonitorException;

public class FileBrowser extends MonitorComponent  {

    public FileBrowser(WOContext aWocontext) {
        super(aWocontext);
    }
	private static final long	serialVersionUID	= 7523872426979817711L;

    String startingPath;  // passed in
    String callbackUpdateAction;  // passed in
    String callbackSelectionAction;  // passed in
    MHost host; // passed in
    boolean showFiles = true;
    boolean isRoots = false;
    String errorMsg;

    boolean shouldShowError() {
        if ( (errorMsg != null) && (errorMsg.length() > 0) ) return true;
        return false;
    }

    NSDictionary aCurrentFile;
    NSArray _fileList = null;
    
    public NSArray fileList() {
        if (_fileList == null)
            retrieveFileList();
        return _fileList;
    }

    private String retrieveFileList() {
        try {
            NSDictionary aDict = RemoteBrowseClient.fileListForStartingPathHost(startingPath, host, showFiles);
            _fileList = (NSArray) aDict.valueForKey("fileArray");
            isRoots = (aDict.valueForKey("isRoots") != null) ? true : false;
            startingPath = (String) aDict.valueForKey("filepath");
            errorMsg = null;
        } catch (MonitorException me) {
            if (isRoots) startingPath = null;
            NSLog.err.appendln("Path Wizard Error: " + me.getMessage());
            me.printStackTrace();
            errorMsg = me.getMessage();
        }
        return errorMsg;
    }

    public boolean isCurrentFileDirectory() {
        String aString = (String)aCurrentFile.valueForKey("fileType");
        if (aString.equals("NSFileTypeDirectory")) {
            return true;
        }
        return false;
    }

    public Object backClicked() {
        String originalPath = startingPath;
        startingPath = NSPathUtilities.stringByDeletingLastPathComponent(startingPath);
        startingPath = NSPathUtilities._standardizedPath(startingPath);
        if (startingPath.equals("") || (originalPath.equals(startingPath)) ) {
            startingPath = null;
        }
        if (retrieveFileList() != null) {
            startingPath = originalPath;
        }
        return performParentAction(callbackUpdateAction);
    }

    public Object directoryClicked() {
        String originalPath = startingPath;
        String aFile = (String)aCurrentFile.valueForKey("file");
        startingPath = NSPathUtilities.stringByAppendingPathComponent(startingPath, aFile);
        startingPath = NSPathUtilities._standardizedPath(startingPath);
        retrieveFileList();
        if (retrieveFileList() != null) {
            startingPath = originalPath;
        }
        return performParentAction(callbackUpdateAction);
    }

    public Object jumpToClicked() {
        String originalPath = startingPath;
        startingPath = NSPathUtilities._standardizedPath(startingPath);
        retrieveFileList();
        if (retrieveFileList() != null) {
            startingPath = originalPath;
        }
        return performParentAction(callbackUpdateAction);
    }


    public Object selectClicked() {
        String aFile = (String)aCurrentFile.valueForKey("file");
        startingPath = NSPathUtilities.stringByAppendingPathComponent(startingPath, aFile);
        startingPath = NSPathUtilities._standardizedPath(startingPath);
        return performParentAction(callbackSelectionAction);
    }

    public Object selectCurrentDirClicked() {
        startingPath = NSPathUtilities._standardizedPath(startingPath);
        return performParentAction(callbackSelectionAction);
    }

}


