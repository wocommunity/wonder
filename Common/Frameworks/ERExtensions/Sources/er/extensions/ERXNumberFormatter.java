/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import org.apache.log4j.Category;
import java.text.*;

// An extension to the simple number formatter that will ignore characters ($%)
public class ERXNumberFormatter extends NSNumberFormatter {

    //////////////////////////////////////////////  log4j category  //////////////////////////////////////////
    public final static Category cat = Category.getInstance(ERXNumberFormatter.class);
    public ERXNumberFormatter() {
    }
    
    protected static ERXNumberFormatter _sharedInstance;
    public static ERXNumberFormatter sharedInstance() {
        if (_sharedInstance == null)
            _sharedInstance = new ERXNumberFormatter();
        return _sharedInstance;
    }

    // FIXME: SHould provide api to allow for additions to the list of ignored characters
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
