/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;
import java.util.Enumeration;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAdaptor;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * The ERXPatternLayout adds two additional (and needed) layout options. The
 * first is by specifing an '@' character a full backtrace will be logged as part
 * of the log event. The second is by specifing an '$' char the current application
 * name of the WOApplication will be logged as part of the log event.
 * Finally by specifing an '#' char the current port number on which the  
 * primary adaptor listens to will be logged as part of the log event. 
 */
// ENHANCEME: Need access to ERXThreadStorage, also need more WO stuff, could opt for a WO char
//	      and then specify all of the things to log as formatting info for that converter.
public class ERXPatternLayout extends PatternLayout {

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
     */
    private class AppNamePatternConverter extends PatternConverter {
        /** holds a reference to the app name */
        String _appName;

        /**
         * Default package level constructor
         * @param formattingInfo current pattern formatting information
         */
        AppNamePatternConverter (FormattingInfo formattingInfo) {
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
    
    /**
     * The adaptor port number pattern converter is useful for logging
     * the current primary adaptor port in log statements.
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
}