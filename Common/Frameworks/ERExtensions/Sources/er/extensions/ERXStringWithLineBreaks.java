/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class ERXStringWithLineBreaks extends ERXStatelessComponent {

    public ERXStringWithLineBreaks(WOContext context) {
        super(context);
    }
    
    public String _value;

    public void reset() {
        super.reset();
        _value = null;
    }
    public String value() {
        if (_value == null) {
            Object value = valueForObjectBinding("value");
            String result = null;
            if (value != null) {
                result = (value instanceof String) ? (String)value : value.toString();
                result = WOResponse.stringByEscapingHTMLString(result);
                result = ERXExtensions.replaceStringByStringInString("\r\n", "\r", result);
                result = ERXExtensions.replaceStringByStringInString("\n", "\r", result);
                result = ERXExtensions.replaceStringByStringInString("\r", "<BR>", result);
                result = ERXExtensions.replaceStringByStringInString("\t",
                                                                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
                                                                    result);
            }
            _value = result;
        }
        return _value;
    }
    public Object valueWhenEmpty() {
        return valueForObjectBinding("valueWhenEmpty");
    }
}
