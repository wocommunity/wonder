/*
 * WOParsedErrorLine.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (“Apple”) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under Apple’s copyrights in this original Apple 
 * software (the “Apple Software”), to use, reproduce, modify and 
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

/* WOParsedErrorLine.java created on Thu 29-Apr-1999 */

/**
 * WOParsedErrorLine is the class that will parse an exception line. After
 * parsing a line (see format in the constructor comment), each instance
 * will be able to get information about the line, class, method where
 * the error occurs.
 *
 * Evolution : should rewrite the parsing stuff... And verify the real format
 * of java exception... Be careful, apparently it could happen that the latest
 * ")" on a line is not present. This is why in the parsing stuff I try to get
 * the index of this closing parenthesis.
 */
import com.webobjects.foundation.*;

public class WOParsedErrorLine extends Object {
    protected String _packageName;
    protected String _className;
    protected String _methodName;
    protected String _fileName;
    protected int _line;
    protected boolean _ignorePackage; // if true, then it will not be possible to display an hyperlink

    public WOParsedErrorLine(String line) {
        // line should have the format of an exception, which is normally (below the index value)
        //        at my.package.name.MyClass.myMethod(FileName.java:lineNumber)
        //           ^                       ^        ^             ^
        //        atIndex                    I     classIndex     lineIndex
        //                                 methodIndex
        int atIndex, methodIndex, classIndex, lineIndex, index;
        String string;
        atIndex = line.indexOf("at ") + 3;
        classIndex = line.indexOf('(') + 1;
        methodIndex = line.lastIndexOf('.', classIndex - 2) + 1;
        lineIndex = line.lastIndexOf(':');
        if (lineIndex < 0) { // We could potentially do not have the info if we use a JIT
            _line = -1;
            _fileName = null;
        } else {
            lineIndex++;
            // Parse the line number
            index = line.indexOf(')', lineIndex);
            if (index < 0) {
                index = line.length();
            }

            string = line.substring(lineIndex, index);              // Remove the last ")"

            try {
                _line = Integer.parseInt(string);                   // Parse the fileName
                _fileName = line.substring( classIndex, lineIndex - 1);
            } catch (NumberFormatException ex) {
                _line = -1;
                _fileName = null;
            }
        }
        _methodName = line.substring( methodIndex, classIndex - 1);
        _packageName = line.substring( atIndex, methodIndex - 1);
        index = _packageName.lastIndexOf('.');
        if (index >= 0) {
            _className = _packageName.substring( index + 1);
            _packageName = _packageName.substring(0, index);
        } else _className = _packageName;
        if (_line < 0) {
            // JIT Activated so we don't have the class name... we can guess it by using the package info\
            _fileName = _className + ".java";
        }
        _ignorePackage = false; // By default we handle all packages
    }

    public String packageName() {
        return _packageName;
    }

    public String className() {
        return _className;
    }

    public String packageClassPath() {
        if (_packageName == _className)
            return _className;
        return _packageName + "." + _className;
    }

    public String methodName() {
        return _methodName;
    }

    public boolean isDisable() {
        return (_line < 0 || _ignorePackage);
    }

    protected void setIgnorePackage(boolean yn) { _ignorePackage = yn; }
    
    public String fileName() {
        return _fileName;
/*        if (_line >= 0)
            return _fileName;
        int index = _packageName.lastIndexOf(".");
        if (index >= 0)
            return _packageName.substring(index + 1) + ".java";
        return _packageName + ".java";*/
    }

    public String lineNumber() {
        if (_line >= 0)
            return String.valueOf(_line);
        return "NA";
    }

    public int line() {
        return _line;
    }
    
    public String toString() {
        String lineInfo = (_line >= 0) ? String.valueOf( _line) : "No line info due to compiled code";
        String fileInfo = (_line >= 0) ? _fileName : "Compiled code no file info";
        if (_packageName == _className)
            return "class : " + _className + ": " + _methodName + " in file :" + fileInfo + " - line :" + lineInfo;
        return "In package : " + _packageName + ", class : " + _className + " method : " + _methodName + " in file :" + fileInfo + " - line :" + lineInfo;
    }
}
