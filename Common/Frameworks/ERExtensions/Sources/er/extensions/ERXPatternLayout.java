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
import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.directtoweb.*;

// This guy allows you to specify '#' when using a log4j pattern layout to get the ProcessId of the
// current WOApp or '$' to get the name of the WOApp.  Note that processId is a category off of 
// WOApplication that can be found in the file: ProcessIdApplicationCategory.m.  Need a better solution for 5.0
public class ERXPatternLayout extends PatternLayout {

    /**
     * Default constructor. Uses the default conversion
     * pattern.
     */
    public ERXPatternLayout() {
        this(DEFAULT_CONVERSION_PATTERN);
    }

    public ERXPatternLayout(String pattern) {
        super(pattern);
    }

    public PatternParser createPatternParser(String pattern) {
        return new ERXPatternParser(pattern == null ? DEFAULT_CONVERSION_PATTERN : pattern);
  }
}

class ERXPatternParser extends PatternParser {

    public ERXPatternParser(String pattern) {
        super(pattern);
    }
    
    public void finalizeConverter(char c) {
        if (c == '$') {
            addConverter(new AppNamePatternConverter(formattingInfo));
            currentLiteral.setLength(0);
        } else if (c == '@') {
            addConverter(new StackTracePatternConverter(formattingInfo));
            currentLiteral.setLength(0);
        } else {
            super.finalizeConverter(c);            
        }
    }
    
    private class StackTracePatternConverter extends PatternConverter {
        StackTracePatternConverter(FormattingInfo formattingInfo) {
            super(formattingInfo);
        }
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
    
    private class AppNamePatternConverter extends PatternConverter {
        String _appName;
        AppNamePatternConverter (FormattingInfo formattingInfo) {
            super(formattingInfo);
        }

        public String convert(LoggingEvent event) {
            if (_appName == null) {
                if (WOApplication.application() != null) _appName = WOApplication.application().name();
            }
            return _appName != null ? _appName : "N/A";
        }
    }
}