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
        _d2wContext = null;
    }

    protected D2WContext _d2wContext;
    public void setD2WContext(D2WContext value) {
        _d2wContext = value;
    }
    public void setLocalContext(D2WContext value) {
        _d2wContext = value;
    }
    public D2WContext localContext() { return d2wContext();}
    public D2WContext d2wContext() {
        if (!synchronizesVariablesWithBindings()) {
            _d2wContext = (D2WContext)super.valueForBinding("localContext");
            if(_d2wContext == null) {
                _d2wContext = (D2WContext)super.valueForBinding("d2wContext");
            }
        }
        return _d2wContext;
    }
}
