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

public class ERD2WPick extends ERDCustomEditComponent {

    public ERD2WPick(WOContext context) { super(context); }
    
    // Keeps kvc happy
    public EODataSource _datasource;

    public boolean synchronizesVariablesWithBindings() { return false; }

    // This is fine because we only use the D2WPick component if list() > 0;
    public EODataSource datasource() { return er.extensions.ERXExtensions.dataSourceForArray(list()); }

    public NSArray list() { return (NSArray)objectKeyPathValue(); }

    public String entityName() { return list().count() > 0 ? ((EOEnterpriseObject)list().objectAtIndex(0)).entityName() : null; }

    public String pickPageConfiguration() { return (String)valueForBinding("pickPageConfiguration"); }
    
    // Gets a little freaky here.  We basicly use a bit of reflection to get the correct branch delegate from the session object.
    public ERDBranchDelegate branchDelegate() {
        String branchDelegateMethod = (String)valueForBinding("branchDelegateMethod");
        ERDBranchDelegate delegate = null;
        if (branchDelegateMethod != null) {
            try {
                Method m = session().getClass().getMethod(branchDelegateMethod, ERXConstant.EmptyClassArray);
                delegate = (ERDBranchDelegate)m.invoke(session(), ERXConstant.EmptyObjectArray);
            } catch (Exception e) {
                WOApplication.application().logString("EXCEPTION:  Unable to find branch delegate for method named: "
                                        + branchDelegateMethod + " on session.");
            }            
        }
        return delegate;
    }

    public boolean erD2WListOmitCenterTag() {
        return hasBinding("erD2WListOmitCenterTag") ? booleanForBinding("erD2WListOmitCenterTag") : false;
    }

    public D2WContext d2wContext() { return D2WUtils.makeSubContextForDynamicPageNamed((String)valueForBinding("pickPageConfiguration"), session()); }
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
