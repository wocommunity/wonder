/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class ERXFixedLengthString extends ERXStatelessComponent {

    public ERXFixedLengthString(WOContext context) {
        super(context);
    }
    
    public int length() {
        Integer l=(Integer)valueForBinding("length");
        return l!=null ? l.intValue() : 0;
    }

    public void reset() {
        super.reset();
        valueWasTrimmed = false;
        _fixedLengthString = null;
    }
    private boolean valueWasTrimmed = false;
    private String _fixedLengthString;
    public String value() {
        if (_fixedLengthString == null) {
            String result=(String)valueForBinding("value");
            int l=length();
            if (l!=0 && result!=null) {
                int sl=result.length();
                if (sl!=l) {
                    if (sl<l) {
                        StringBuffer sb=new StringBuffer(result);
                        if (valueForBooleanBinding("padToLength", true)) {
                            for (int i=sl; i<l; i++) sb.append(' ');
                        }
                        result=sb.toString();
                    } else {
                        valueWasTrimmed = true;
                        result=result.substring(0,l-1);
                    }
                }
            }
            _fixedLengthString = result;
        }
        return _fixedLengthString;
    }
    
    public String suffixWhenTrimmed() {
        value();
        String result = null;
        if ((value() != null && valueWasTrimmed))
            result = (String)valueForObjectBinding("suffixWhenTrimmed");
        return result;
    }
}
