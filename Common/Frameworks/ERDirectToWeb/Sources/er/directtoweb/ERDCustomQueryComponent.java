/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import er.extensions.ERXLogger;

/**
 * Superclass for most custom query components.<br />
 * 
 */

public class ERDCustomQueryComponent extends ERDCustomEditComponent {

    public ERDCustomQueryComponent(WOContext context) {
        super(context);
    }
    
    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERDCustomQueryComponent.class);    
    
    private WODisplayGroup _displayGroup;
    public WODisplayGroup displayGroup() {
        if (_displayGroup==null && !synchronizesVariablesWithBindings())
            _displayGroup=(WODisplayGroup)super.valueForBinding("displayGroup");
        return _displayGroup;
    }
    public void setDisplayGroup(WODisplayGroup value) {_displayGroup = value; }

    public Object displayGroupQueryMatchValue() {
        return key() != null && displayGroup() != null ? displayGroup().queryMatch().objectForKey(key()) : null;
    }
    public void setDisplayGroupQueryMatchValue (Object newValue) {
        if (key() != null && displayGroup () != null && displayGroup().queryMatch()!=null )
            displayGroup().queryMatch().setObjectForKey(newValue,key());
    }

    public void reset() {
        super.reset();
        _displayGroup = null;
    }
}
