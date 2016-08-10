/*
 * WOParsedErrorLine.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

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
public class WOParsedErrorLine {
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
        if (_packageName.equals(_className)) {
            return _className;
        }
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
    
    @Override
    public String toString() {
        String lineInfo = (_line >= 0) ? String.valueOf( _line) : "No line info due to compiled code";
        String fileInfo = (_line >= 0) ? _fileName : "Compiled code no file info";
        if (_packageName.equals(_className)) {
            return "class : " + _className + ": " + _methodName + " in file :" + fileInfo + " - line :" + lineInfo;
        }
        return "In package : " + _packageName + ", class : " + _className + " method : " + _methodName + " in file :" + fileInfo + " - line :" + lineInfo;
    }
}
