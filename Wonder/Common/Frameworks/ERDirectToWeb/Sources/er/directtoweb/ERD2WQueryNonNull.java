/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;

/**
 * Query component for null or non-null.<br />
 * 
 */

public class ERD2WQueryNonNull extends QueryComponent {

    public ERD2WQueryNonNull(WOContext context) { super(context); }
    
    public Object item;
    public int index;

    private final static String[] _queryValues={ "don't care", "yes", "no" };
    private final static Object YES_VALUE=new Object(); 
    private final static Object NO_VALUE=new Object(); 
    private final static Object DONT_CARE_VALUE=new Object(); 
    private final static Object[] _queryNumbers={ DONT_CARE_VALUE, YES_VALUE, NO_VALUE };
    protected final static NSArray _queryNumbersArray=new NSArray(_queryNumbers);


    public NSArray queryNumbers() { return _queryNumbersArray; }
    public String displayString() { return _queryValues[index]; }
    public Object value() { return _queryNumbers[0]; }

    public void setValue(Object newValue) {
        if (newValue==DONT_CARE_VALUE) {
            displayGroup().queryMatch().takeValueForKey(null, propertyKey());
            displayGroup().queryOperator().takeValueForKey(null, propertyKey());            
        } else {
            displayGroup().queryMatch().takeValueForKey(NSKeyValueCoding.NullValue, propertyKey());
            displayGroup().queryOperator().takeValueForKey(newValue==YES_VALUE ? "<>" : "=", propertyKey());            
        }
    }
}
