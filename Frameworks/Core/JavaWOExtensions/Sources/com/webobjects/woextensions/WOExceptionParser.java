/*
 * WOExceptionParser.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

/**
 * WOExceptionParser parse the stack trace of a Java exception (in fact the parse is really
 * made in WOParsedErrorLine).
 * 
 * The stack trace is set in an NSArray that will be used in the UI in the exception page.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;

public class WOExceptionParser {
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
        NSBundle bundle;
        String path, content;
        NSDictionary dic = null;
        NSMutableArray<NSBundle> allBundles = new NSMutableArray<NSBundle>(NSBundle.frameworkBundles());
        NSMutableArray<String> ignored = new NSMutableArray<String>();

        for (Enumeration enumerator = allBundles.objectEnumerator(); enumerator.hasMoreElements(); ) {
            bundle = (NSBundle) enumerator.nextElement();
            path = WOApplication.application().resourceManager().pathForResourceNamed("WOIgnoredPackage.plist",bundle.name(),null);
            if (path != null) {
                content = _stringFromFileSafely(path);
                if (content != null) {
                    dic = (NSDictionary) NSPropertyListSerialization.propertyListFromString(content);
                    if (dic != null && dic.containsKey("ignoredPackages")) {
                        @SuppressWarnings("unchecked")
						NSArray<String> tmpArray = (NSArray<String>) dic.objectForKey("ignoredPackages");
                        if (tmpArray != null && tmpArray.count() > 0) {
                            ignored.addObjectsFromArray(tmpArray);
                        }
                    }
                }
            }
        }
        System.out.println("_ignoredPackages:: "+ignored);
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

        int i, size;
        try {
            _exception.printStackTrace(pWriter);
            pWriter.close();
            sWriter.close();					// Added the try/catch as this throws in JDK 1.2 aB.
            string = sWriter.toString();
            i = _exception.toString().length(); // We skip the name of the exception and the message for our parse
            if(string.length() > i+2) { // certain errors don't contain a stack trace
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

    /**
     * Return a string from the contents of a file, returning null
     * instead of any possible exception.
     * 
     * TODO I wonder if this has been done somewhere else in the
     * frameworks....
     */
    private static String _stringFromFileSafely(String path) {
        File f = new File(path);

        if (!f.exists()) { return null; }

        FileInputStream fis = null;
        byte[] data = null;

        int bytesRead = 0;

        try {
            int size = (int) f.length();
            fis = new FileInputStream(f);
            data = new byte[size];

            while (bytesRead < size) {
                bytesRead += fis.read(data, bytesRead, size - bytesRead);
            }

        } catch (java.io.IOException e) {
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (java.io.IOException e) {
                    if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupIO)) {
                        NSLog.debug.appendln("Exception while closing file input stream: " + e.getMessage());
                        NSLog.debug.appendln(e);
                    }
                }
            }
        }

        if (bytesRead == 0)
            return null;
        return new String(data);
    }

    /**
     * Return a string for a file, or return an exception.
     */
    // TODO Are any sub-classes using this? Do they catch exceptions?
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
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupIO)) {
                        NSLog.debug.appendln("Exception while closing file input stream: " + e.getMessage());
                        NSLog.debug.appendln(e);
                    }
                }
            }

        }

        return new String(data);
    }
}
