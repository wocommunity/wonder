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

import er.extensions.*;

/**
 * Popup used for picking a number.<br />
 * 
 */

public class ERD2WPopUp extends D2WStatelessComponent {

    public ERD2WPopUp(WOContext context) { super(context); }
    
    protected Number choice;

    // FIXME: Should be more dynamic here.
    public NSArray list(){
        NSMutableArray result = new NSMutableArray(ERXConstant.OneInteger);
        result.addObject(ERXConstant.TwoInteger);
        result.addObject(ERXConstant.integerForInt(3));
        result.addObject(ERXConstant.integerForInt(4));
        result.addObject(ERXConstant.integerForInt(5));
        return (NSArray)result;
    }
                         
    public Number choice() {
        return (Number)object().valueForKey(propertyKey());
    }

    public void setChoice(Number newChoice) {
        object().validateTakeValueForKeyPath(newChoice, propertyKey());
    }
}
