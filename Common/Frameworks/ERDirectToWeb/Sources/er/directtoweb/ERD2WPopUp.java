/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXConstant;

/**
 * Popup used for picking a number or some other value by 
 * using the key restrictedChoiceKey.<br />
 * You should use ERD2WEditToOneRelationship, though.
 * 
 */

public class ERD2WPopUp extends ERD2WStatelessComponent {

	public ERD2WPopUp(WOContext context) { super(context); }

	public NSArray list(){
		NSArray result = null;
		String restrictedChoiceKey=(String)d2wContext().valueForKey("restrictedChoiceKey");
		if( restrictedChoiceKey!=null &&  restrictedChoiceKey.length()>0 ) {
			result = (NSArray) valueForKeyPath(restrictedChoiceKey);
		} else {
			NSMutableArray arr = new NSMutableArray(ERXConstant.OneInteger);
			arr.addObject(ERXConstant.TwoInteger);
			arr.addObject(ERXConstant.integerForInt(3));
			arr.addObject(ERXConstant.integerForInt(4));
			arr.addObject(ERXConstant.integerForInt(5));
			result = arr;
		}
		return (NSArray)result;
	}

	public Object choice() {
		return object().valueForKey(propertyKey());
    }

    public void setChoice(Object newChoice) {
        object().validateTakeValueForKeyPath(newChoice, propertyKey());
    }
}
