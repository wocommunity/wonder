/*
 * WOExceptionParser.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (ÒAppleÓ) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under AppleÕs copyrights in this original Apple 
 * software (the ÒApple SoftwareÓ), to use, reproduce, modify and 
 * redistribute the Apple Software, with or without modifications, in 
 * source and/or binary forms; provided that if you redistribute the  
 * Apple Software in its entirety and without modifications, you must 
 * retain this notice and the following text and disclaimers in all such
 * redistributions of the Apple Software.  Neither the name, trademarks,
 * service marks or logos of Apple Computer, Inc. may be used to endorse
 * or promote products derived from the Apple Software without specific
 * prior written permission from Apple.  Except as expressly stated in
 * this notice, no other rights or licenses, express or implied, are
 * granted by Apple herein, including but not limited to any patent
 * rights that may be infringed by your derivative works or by other
 * works in which the Apple Software may be incorporated.
 *
 * The Apple Software is provided by Apple on an "AS IS" basis.  APPLE 
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS
 * USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 
 *
 * IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT,
 * INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE,
 * REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE,
 * HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING
 * NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.webobjects.woextensions;

/**
 * WOExceptionParser parse the stack trace of a Java exception (in fact the parse is really
 * made in WOParsedErrorLine).
 * The stack trace is set in an NSArray that will be used in the UI in the exception page.
 *
 */
import java.io.*;
import java.util.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class WOExceptionParser extends Object {
    protected NSMutableArray _stackTrace;
    protected Throwable _exception;
    protected String _message;
    protected String _typeException;

    public WOExceptionParser(Throwable exception) {
        super();
        _stackTrace = new NSMutableArray();
        _exception = NSForwardException._originalThrowable(exception);
        _message = _exception.getMessage();
        _typeException = _exception.getClass().getName();
        _parseException();
    }

    protected NSArray _ignoredPackages() {
        Enumeration enumerator;
        NSBundle bundle;
        String path, content;
        NSArray tmpArray;
        NSDictionary dic;
        NSMutableArray allBundles = new NSMutableArray(NSBundle.frameworkBundles());
        NSMutableArray ignored = new NSMutableArray();
        
        enumerator = allBundles.objectEnumerator();
        while (enumerator.hasMoreElements()) {
            bundle = (NSBundle) enumerator.nextElement();
            path = WOApplication.application().resourceManager().pathForResourceNamed("WOIgnoredPackage.plist",bundle.name(),null);
            if (path != null) {
                content = _stringFromFile(path);
                dic = (NSDictionary) NSPropertyListSerialization.propertyListFromString(content);
                tmpArray = (NSArray) dic.objectForKey("ignoredPackages");
                if (tmpArray != null && tmpArray.count() > 0) {
                    ignored.addObjectsFromArray(tmpArray);
                }
            }
        }
        return ignored;
    }
        
    protected void _verifyPackageForLine(WOParsedErrorLine line, NSArray packages) {
        Enumeration enumerator;
        String ignoredPackageName, linePackageName;
        linePackageName = line.packageName();
        enumerator = packages.objectEnumerator();
        while (enumerator.hasMoreElements()) {
            ignoredPackageName = (String) enumerator.nextElement();
            if (linePackageName.startsWith(ignoredPackageName)) {
                line.setIgnorePackage(true);
                break;
            }
        }
    }

    protected void _parseException() {
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter(sWriter, false);
        String string;
        NSArray lines;
        NSArray ignoredPackage;
        WOParsedErrorLine aLine;
        String line;
        
        int i, index, size;
        StreamTokenizer stream;
        try {
            _exception.printStackTrace(pWriter);
            pWriter.close();
            sWriter.close();					// Added the try/catch as this throws in JDK 1.2 aB.
            string = sWriter.toString();
            i = _exception.toString().length(); // We skip the name of the exception and the message for our parse
            string = string.substring(i+2); // Skip the exception type and message
            lines = NSArray.componentsSeparatedByString(string, "\n");
            ignoredPackage = _ignoredPackages();
            size = lines.count();
            _stackTrace = new NSMutableArray(size);
            for (i = 0; i < size; i++) {
                line = ((String) lines.objectAtIndex(i)).trim();
                if (line.startsWith("at ")) {
                    // If we don't have an open parenthesis it means that we
                    // have probably reach the latest stack trace.
                    aLine = new WOParsedErrorLine(line);
                    _verifyPackageForLine(aLine, ignoredPackage);
                    _stackTrace.addObject(aLine);
                }
            }
        } catch (Throwable e) {
            NSLog.err.appendln("WOExceptionParser - exception collecting backtrace data " + e + " - Empty backtrace.");
            NSLog.err.appendln(e);
        }
        if (_stackTrace == null) {
            _stackTrace = new NSMutableArray();
        }
            
    }

    public NSArray stackTrace() {
        return _stackTrace;
    }

    public String typeException() { return _typeException; }

    public String message() { return _message; }

    protected static String _stringFromFile(String path) {
        File f = new File(path);
        FileInputStream fis = null;
        byte[] data = null;

        if (!f.exists()) {
            return null;
        }
        
        try {
            int size = (int) f.length();
            fis = new FileInputStream(f);
            data = new byte[size];
            int bytesRead = 0;

            while (bytesRead < size) {
                bytesRead += fis.read(data, bytesRead, size - bytesRead);
            }

        } catch (IOException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        } finally {
            if (f != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupIO)) {
                        NSLog.debug.appendln("Exception while closing file input stream: " + e.getMessage());
                        NSLog.debug.appendln(e);
                    }

                }

                f = null;
            }

        }

        return new String(data);
    }
}
