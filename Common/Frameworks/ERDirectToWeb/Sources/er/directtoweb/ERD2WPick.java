/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import java.lang.reflect.*;
import er.extensions.ERXConstant;

/**
 * Used to select multiple items from a list.<br />
 * 
 * @binding dataSource
 * @binding list
 * @binding selectedObjects
 * @binding action
 * @binding displayKeys
 * @binding entityName
 * @binding pageConfiguration
 * @binding branchDelegate
 */

public class ERD2WPick extends ERDCustomEditComponent {

    /**
     * Public constructor.
     * @param context current context
     */
    public ERD2WPick(WOContext context) { super(context); }
    
    // Keeps kvc happy
    public EODataSource _datasource;

    public boolean synchronizesVariablesWithBindings() { return false; }

    // This is fine because we only use the D2WPick component if list() > 0;
    public EODataSource datasource() { return er.extensions.ERXEOControlUtilities.dataSourceForArray(list()); }

    public NSArray list() { return (NSArray)objectKeyPathValue(); }

    public String entityName() { return list().count() > 0 ? ((EOEnterpriseObject)list().objectAtIndex(0)).entityName() : null; }
    
    public ERDBranchDelegateInterface branchDelegate() {
        ERDBranchDelegateInterface branchDelegate = (ERDBranchDelegateInterface)valueForBinding("branchDelegate");
        if (branchDelegate == null) {
            String branchDelegateMethod = (String)valueForBinding("branchDelegateMethod");
            if (branchDelegateMethod != null) {
                branchDelegate = (ERDBranchDelegateInterface)session().valueForKeyPath(branchDelegateMethod);
            }
        }
        return branchDelegate;
    }

    public boolean erD2WListOmitCenterTag() {
        return hasBinding("erD2WListOmitCenterTag") ? booleanValueForBinding("erD2WListOmitCenterTag") : false;
    }

    public D2WContext d2wContext() { return D2WUtils.makeSubContextForDynamicPageNamed((String)valueForBinding("listConfigurationName"), session()); }
    public void setD2wContext(Object value) { }

    public NextPageDelegate actionPageDelegate() { return _D2WPickActionDelegate.instance; }
    
    static class _D2WPickActionDelegate implements NextPageDelegate {
        public static NextPageDelegate instance=new _D2WPickActionDelegate ();
        
        public WOComponent nextPage(WOComponent sender) {
            WOComponent target = (WOComponent)D2WEmbeddedComponent.findTarget(sender);
            return ((ERDBranchDelegate)target.valueForBinding("branchDelegate")).nextPage(sender);
        }
    }

}
