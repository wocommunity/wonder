/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* WOOgnlAssociation.java created by max on Fri 28-Sep-2001 */
package ognl.webobjects;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.appserver._private.*;
import java.util.Enumeration;

public class WOOgnlAssociation extends WOKeyValueAssociation {

    public WOOgnlAssociation(String s) {
        super(s);
    }

    public Object clone() { return new WOOgnlAssociation(keyPath()); }

    public Object valueInComponent(WOComponent component) {
        WOAssociation.Event event = _markStartOfEventIfNeeded("valueForKeyPath", keyPath(), component);

        Object value = null;
	try {
	    value = WOOgnl.factory().getValue(keyPath(), component);
	} catch(NullPointerException ex) {
	    System.err.println("NullPointerException in WOOgnlAssociation with keyPath '" + keyPath() + "'");
	    // AK@PRNET:DE
     //FIXME: we should have a default that tell us whether to throw or not
	    // throw ex;
	}
        if(event != null)
            EOEventCenter.markEndOfEvent(event);
        if(_debugEnabled)
            _logPullValue(value, component);
        return value;
    }

    // FIXME: Need to implement this one.
    public void setValue(Object obj, WOComponent wocomponent) {
        throw new IllegalStateException(toString() + ": Cannot set value to '" + obj.toString() + "' in component '" + wocomponent.toString() + "' because value is not settable.");
    }

    public boolean isValueSettable() { return false; }
}
