/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import java.text.*;

/**
 * A simple extension to the number formatter that
 * will strip out the characters '%$,' when parsing
 * a string.
 */
public class ERXNumberFormatter extends NSNumberFormatter {

    /** holds a reference to the shared instance */
    protected static ERXNumberFormatter _sharedInstance;
    /**
     * Returns the shared instance
     * @return shared instance
     */
    public static ERXNumberFormatter sharedInstance() {
        if (_sharedInstance == null)
            _sharedInstance = new ERXNumberFormatter();
        return _sharedInstance;
    }

    /**
     * Public constructor
     */
    public ERXNumberFormatter() {
    }

    /**
     * Strips out the characters '$%,' from the string to
     * be parsed.
     * @param aString to be parsed
     * @return the parsed object
     */
    // FIXME: Should provide api to allow for additions to the list of ignored characters
    // 		also ignoring the , character is not good for localization.
    // CHECKME: Is this really needed now that we can form locales?
    public Object parseObject(String aString) throws java.text.ParseException {
        char[] chars = aString.toCharArray();
        char[] filteredChars = new char[chars.length];
        String ignoredChars = "$%,";
        int count = 0;
        for (int i = 0; i < chars.length; i++) {
            if (ignoredChars.indexOf((int)chars[i]) < 0) {
                filteredChars[count++] = chars[i];
            }
        }
        String filteredString = new String(filteredChars, 0, count);
        return super.parseObject(filteredString);
    }
}
