/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* CustomQueryComponent.java created by patrice on Fri 04-Jan-2002 */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import org.apache.log4j.Category;

public class ERDCustomQueryComponent extends ERDCustomEditComponent {

    public ERDCustomQueryComponent(WOContext context) {
        super(context);
    }
    
    //////////////////////////////////////////////  log4j category  //////////////////////////////////////////
    public final static Category cat = Category.getInstance("er.directtoweb.CustomQueryComponent");    
    
    private WODisplayGroup _displayGroup;
    public WODisplayGroup displayGroup() {
        if (_displayGroup==null && !synchronizesVariablesWithBindings())
            _displayGroup=(WODisplayGroup)super.valueForBinding("displayGroup");
        return _displayGroup;
    }

    public Object displayGroupQueryMatchValue() {
        return key() != null && displayGroup() != null ? displayGroup().queryMatch().objectForKey(key()) : null;
    }
    public void setDisplayGroupQueryMatchValue (Object newValue) {
       
        if (key() != null && displayGroup () != null && displayGroup().queryMatch()!=null )
            displayGroup().queryMatch().setObjectForKey(newValue,key());
    }

    
}
