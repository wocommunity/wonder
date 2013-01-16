package com.webobjects.directtoweb._ajax;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WDisplayToOne;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.foundation.ERXStringUtilities;

public class D2WAjaxDisplayToOne extends D2WDisplayToOne {
    public D2WAjaxDisplayToOne(WOContext context) {
        super(context);
    }
    
    // accessors 
    public String classString() {
    	String classString = (String) d2wContext().valueForKey("class");
    	return classString != null ? ERXStringUtilities.safeIdentifierName(classString) : null;
    }
    
    private EOEnterpriseObject _eo() {
        if(object() == null) {
            return null;
        } else {
            String aPropertyToDisplay = propertyKey();
            EOEnterpriseObject anEO = (EOEnterpriseObject)object().valueForKeyPath(aPropertyToDisplay);
            return anEO;
        }
    }

    // actions
    @Override
    public WOComponent toOneAction() {
        EOEnterpriseObject anEO = _eo();
        if(anEO == null) {
            return null;
        } else {
            InspectPageInterface inspectPage = (InspectPageInterface) D2W.factory().pageForConfigurationNamed("AjaxInspect" + anEO.entityName(), session());
            inspectPage.setObject(anEO);
            inspectPage.setNextPage(context().page());
            return (WOComponent)inspectPage;
        }
    }
}
