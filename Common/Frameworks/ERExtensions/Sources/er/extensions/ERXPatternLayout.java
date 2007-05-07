/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.util.Enumeration;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

import com.webobjects.appserver.WOAdaptor;
import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * The ERXPatternLayout adds some additional (and needed) layout options. The
 * first is by specifing an '@' character a full backtrace will be logged as part
 * of the log event. The second is by specifing an '$' char the current application
 * name of the WOApplication will be logged as part of the log event.
 * Finally by specifing an '#' char the current port number on which the  
 * primary adaptor listens to will be logged as part of the log event. 
 * 
 * <pre>
 * WebObjects Applicaion Info Patterns
 * Example: %W{n[i:p s]} -- MyApp[9300:2001 28] 
 * 
 * n: application name
 * i: pid (process ID, provided through Java system property "com.webobjects.pid") 
 * p: primary adaptor's port number
 * s: active session count
 * 
 * Java VM (Virtual Machine) Info Patterns
 * Example: %V{u used/f free} -- 75.22 MB used/12.86 MB free
 * 
 * t: total memory
 * u: used memory 
 * f: free memory in the current heap size (not max)
 * 
 * </pre>
 * 
 */
// ENHANCEME: Need access to ERXThreadStorage, also need more WO stuff, could opt for a WO char
//	      and then specify all of the things to log as formatting info for that converter.
public class ERXPatternLayout extends PatternLayout {


    /**
    *
     *  Used to update the layout pattern at runtime from the log4j configuration page
     *
     */
    private static ERXPatternLayout _instance;
    public static ERXPatternLayout instance() {
    	if(_instance == null) {
    		_instance = new ERXPatternLayout();
    	}
        return _instance;
    }


    /**
     * Default constructor. Uses the default conversion
     * pattern.
     */
    public ERXPatternLayout() {
        this(DEFAULT_CONVERSION_PATTERN);
    }

    /**
     * Default constructor. Uses the specified conversion
     * pattern.
     * @param pattern layout to be used.
     */
    public ERXPatternLayout(String pattern) {
        super(pattern);
	_instance=this; // log4j will create one of these at runtime, and instance() will be used to find it from the log4j config page
    }

    /**
     * Creates a pattern parser for the given pattern.
     * This method is called implicitly by the log4j
     * logging system.
     * @param pattern to create the pattern parser for
     * @return an ERXPatternParser for the given pattern
     */
    public PatternParser createPatternParser(String pattern) {
        return new ERXPatternParser(pattern == null ? DEFAULT_CONVERSION_PATTERN : pattern);
  }
}

/**
 * Pattern parser extension that adds support for WebObjects
 * specific patterns.
 */
class ERXPatternParser extends PatternParser {

    /**
     * Default constructor for a given pattern
     * @param pattern to construct the parser for
     */
    public ERXPatternParser(String pattern) {
        super(pattern);
    }

    /**
     * Creates a converter for a particular
     * character. This is the method that
     * adds the custom converters for WO.
     * @param c char to add the converter for
     */
    public void finalizeConverter(char c) {
        switch (c) {
            case '$': 
                addConverter(new AppNamePatternConverter(formattingInfo));
                currentLiteral.setLength(0);
                break;
            case '#': 
                addConverter(new AdaptorPortNumberConverter(formattingInfo));
                currentLiteral.setLength(0);
                break;
            case '@':
                addConverter(new StackTracePatternConverter(formattingInfo));
                currentLiteral.setLength(0);
                break;
            case 'W':
                addConverter(new AppInfoPatternConverter(formattingInfo, extractOption()));
                currentLiteral.setLength(0);
                break;
            case 'V':
                addConverter(new JavaVMInfoPatternConverter(formattingInfo, extractOption()));
                currentLiteral.setLength(0);
                break;
            case 'T':
                addConverter(new ThreadStoragePatternConverter(formattingInfo, extractOption()));
                currentLiteral.setLength(0);
                break;                
            default: 
                super.finalizeConverter(c);            
                break;
        }
    }

    /**
     * The stack trace pattern is useful for logging full stack traces
     * when a log event occurs.
     */
    private class StackTracePatternConverter extends PatternConverter {
        /**
         * Package access level constructor.
         * @param formattingInfo that is currently being used for this
         *		pattern converter.
         */
        StackTracePatternConverter(FormattingInfo formattingInfo) {
            super(formattingInfo);
        }

        /**
         * For a given log event returns the string representation
         * of a stack trace for the current logging call minus all
         * of the log4j stack.
         * @param event current logging event
         * @return string representation of the current backtrace.
         */
        public String convert(LoggingEvent event) {
            NSArray parts = NSArray.componentsSeparatedByString(ERXUtilities.stackTrace(), "\n\t");
            NSMutableArray subParts = new NSMutableArray();
            boolean first = true;
            for (Enumeration e = parts.reverseObjectEnumerator(); e.hasMoreElements();) {
                String element = (String)e.nextElement();
                if (element.indexOf("org.apache.log4j") != -1)
                    break;
                if (!first)
                    subParts.insertObjectAtIndex(element, 0);
                else
                    first = false;                    
            }
            return "\t" + subParts.componentsJoinedByString("\n\t") + "\n";
        }
    }

    /**
     * The application name pattern converter is useful for logging
     * the current application name in log statements.
     * 
     * @deprecated 
     */
    private class AppNamePatternConverter extends PatternConverter {
        /** holds a reference to the app name */
        String _appName;

        /**
         * Default package level constructor
         * @param formattingInfo current pattern formatting information
         */
        AppNamePatternConverter(FormattingInfo formattingInfo) {
            super(formattingInfo);
        }

        /**
         * Returns the current application name for the
         * current logging event. If the application instance
         * has not been created yet then "N/A" is logged.
         * @param event a given logging event
         * @return the current application name
         */
        public String convert(LoggingEvent event) {
            if (_appName == null) {
                if (WOApplication.application() != null)
                    _appName = WOApplication.application().name();
            }
            return _appName != null ? _appName : "N/A";
        }
    }

    private class ThreadStoragePatternConverter extends PatternConverter {
        private String key;
        private NSArray keyParts;
        private boolean isKeyPath;

        ThreadStoragePatternConverter(FormattingInfo formattingInfo, String key) {
            super(formattingInfo);
            this.key = key;
            isKeyPath = key != null && key.indexOf(".") != -1;
            if (isKeyPath)
                keyParts = NSArray.componentsSeparatedByString(key, ".");
        }

        public String convert(LoggingEvent event) {
            Object value = null;
            if (!isKeyPath) {
                value = key != null ? ERXThreadStorage.valueForKey(key) : ERXThreadStorage.map();
            } else {
                value = ERXThreadStorage.map();
                for (int j = 0; j < keyParts.count(); j++) {
                    String part = (String)keyParts.objectAtIndex(j);
                    if (j == 0) {
                        value = ERXThreadStorage.valueForKey(part);
                    } else {
                        value = NSKeyValueCoding.Utility.valueForKey(value, part);
                    }
                    if (value == null)
                        break;
                }                
            }
            return value != null ? value.toString() : null;
        }        
    }
    
    /**
     * The adaptor port number pattern converter is useful for logging
     * the current primary adaptor port in log statements.
     *
     * @deprecated 
     */
    private class AdaptorPortNumberConverter extends PatternConverter {
        /** holds a reference to the primary adaptor port */
        String _portNumber;
        
        /**
         * Default package level constructor
         * @param formattingInfo current pattern formatting information
         */
        AdaptorPortNumberConverter(FormattingInfo formattingInfo) {
            super(formattingInfo);
        }
        
        /**
         * Returns the current port number on which the  
         * primary adaptor listens to. 
         * This will be the same number specified
         * by WOPort launch argument.
         * <p> 
         * If the application or adapter instance 
         * has not been created yet then "N/A" is logged.
         * @param event a given logging event
         * @return the current application name
         */
        public String convert(LoggingEvent event) {
            if (_portNumber == null) {
                if (WOApplication.application() != null) {
                    //_portNumber = WOApplication.application().port().toString();

                    // WO 5.1.x -- Apple Ref# 2260519
                    NSArray adaptors = WOApplication.application().adaptors();
                    if (adaptors != null  &&  adaptors.count() > 0) {
                        WOAdaptor primaryAdaptor = (WOAdaptor)adaptors.objectAtIndex(0);
                        _portNumber = String.valueOf(primaryAdaptor.port()); 
                    }
                }
            }
            return _portNumber != null ? _portNumber : "N/A";
        }
    }

    /**
     * The <code>AppInfoPatternConverter</code> is useful for logging
     * various info about the WebObjects applicaiton instance. 
     * See {@link ERXPatternLayout} for example/supported partterns. 
     */
    private class AppInfoPatternConverter extends PatternConverter {

        /** Template parser to format logging events */
        private ERXSimpleTemplateParser _templateParser;

        /** Template used by _templateParser */
        private String _template;
        
        /** 
         * Flag to indicate if the constant values are set. 
         * The constant values are the part of application info that 
         * shouldn't change during the application's life span. 
         */
        private boolean _constantsInitialized = false;
        
        /** Holds the values for the application info. Used by the template parser */ 
        private NSMutableDictionary _appInfo;
        
        /** 
         * Holds the default labels for the values. 
         * Note that the template parser will put "-" for undefined 
         * values by defauilt. 
         */
        private final NSDictionary _defaultLabels = 
            ERXDictionaryUtilities.dictionaryWithObjectsAndKeys(new Object[] {"@@sessionCount@@", "sessionCount"});
        
        /**
         * Default package level constructor
         * 
         * @param  formattingInfo current pattern formatting information
         * @param  format  string for the logging event format 
         */
        // FIXME: Work in progress - fixed template; format parameter will be ignored for now. 
        AppInfoPatternConverter(FormattingInfo formattingInfo, String format) {
            super(formattingInfo);
            _templateParser = new ERXSimpleTemplateParser("-");
            // This will prevent the convert method to get into an infinite loop 
            // when debug level logging is enabled for the perser. 
            _templateParser.isLoggingDisabled = true;
            _appInfo = new NSMutableDictionary();
            // work in progress; this is the fixed template.
            _template = "@@appName@@[@@pid@@:@@portNumber@@ @@sessionCount@@]";
        }
        
        /**
         * Returns ...
         * <p> 
         * 
         * ... has not been created yet then "-" is logged.
         * 
         * @param event a given logging event
         * @return the current application name
         */
        public String convert(LoggingEvent event) {
            WOApplication app = WOApplication.application();
            if (app != null) {
                
                if (! _constantsInitialized) {
                    String pid = System.getProperty("com.webobjects.pid");
                    if (pid != null)
                        _appInfo.setObjectForKey(pid, "pid");

                    String appName = app.name();
                    if (appName != null) 
                        _appInfo.setObjectForKey(appName, "appName");

                    if (app.port() != null && app.port().intValue() > 0) {
                        _appInfo.setObjectForKey(app.port().toString(), "portNumber");
                    } else {
                        // WO 5.1.x -- Apple Ref# 2260519
                        NSArray adaptors = app.adaptors();
                        if (adaptors != null  &&  adaptors.count() > 0) {
                            WOAdaptor primaryAdaptor = (WOAdaptor)adaptors.objectAtIndex(0);
                            String portNumber = String.valueOf(primaryAdaptor.port());
                            if (portNumber != null)
                                _appInfo.setObjectForKey(portNumber, "portNumber");
                        }                        
                    }
                    
                    _template = _templateParser.parseTemplateWithObject(_template, "@@", _appInfo, _defaultLabels);
                    _constantsInitialized = true;
                }
                
                _appInfo.setObjectForKey(String.valueOf(app.activeSessionsCount()), "sessionCount");
            }
            return _templateParser.parseTemplateWithObject(_template, "@@", _appInfo);
        }
    }

    /**
     * The <code>JavaVMInfoPatternConverter</code> is useful for logging
     * various info about the Java runtime and Virtual Machine that 
     * is running the application instance. 
     * See {@link ERXPatternLayout} for example/supported partterns. 
     */
    private class JavaVMInfoPatternConverter extends PatternConverter {

        /** */
        private Runtime _runtime;

        /** */
        private ERXUnitAwareDecimalFormat _decimalFormatter;

        /** Template parser to format logging events */
        private ERXSimpleTemplateParser _templateParser;

        /** Template used by _templateParser */
        private String _template;
        
        /** 
         * Flag to indicate if the constant values are set. 
         * The constant values are the part of application info that 
         * shouldn't change during the application's life span. 
         */
        private boolean _constantsInitialized = false;
        
        /** Holds the values for the JavaVM info. Used by the template parser */ 
        private NSMutableDictionary _jvmInfo;
        
        /** 
         * Holds the default labels for the values. 
         * Note that the template parser will put "-" for undefined 
         * values by defauilt. 
         */
        private final NSDictionary _defaultLabels = null;
        
        /**
         * Default package level constructor
         * 
         * @param  formattingInfo current pattern formatting information
         * @param  format  string for the logging event format 
         */
        // FIXME: Work in progress - fixed template; format parameter will be ignored for now. 
        JavaVMInfoPatternConverter(FormattingInfo formattingInfo, String format) {
            super(formattingInfo);
            _runtime = Runtime.getRuntime();
            _decimalFormatter = new ERXUnitAwareDecimalFormat(ERXUnitAwareDecimalFormat.BYTE);
            _decimalFormatter.setMaximumFractionDigits(2);
            _templateParser = new ERXSimpleTemplateParser("-");
            // This will prevent the convert method to get into an infinite loop 
            // when debug level logging is enabled for the perser. 
            _templateParser.isLoggingDisabled = true;
            _jvmInfo = new NSMutableDictionary();
            // work in progress; this is the fixed template.
            _template = "@@usedMemory@@ used/@@freeMemory@@ free";
        }
        
        /**
         * Returns ...
         * <p> 
         * 
         * ... has not been created yet then "-" is logged.
         * 
         * @param event a given logging event
         * @return the current application name
         */
        public String convert(LoggingEvent event) {
            if (! _constantsInitialized) {
                // Initialize constants here
                _constantsInitialized = true;
            }
            
            long totalMemory = _runtime.totalMemory();
            long freeMemory  = _runtime.freeMemory();
            long usedMemory  = totalMemory - freeMemory;
            _jvmInfo.setObjectForKey(_decimalFormatter.format(totalMemory), "totalMemory");
            _jvmInfo.setObjectForKey(_decimalFormatter.format(freeMemory),  "freeMemory" );
            _jvmInfo.setObjectForKey(_decimalFormatter.format(usedMemory),  "usedMemory" );

            return _templateParser.parseTemplateWithObject(_template, "@@", _jvmInfo);
        }
    }

}
