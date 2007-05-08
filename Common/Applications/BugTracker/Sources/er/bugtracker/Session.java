/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.corebusinesslogic.ERCoreBusinessLogic;
import er.directtoweb.ERD2WInspectPage;
import er.extensions.ERXSession;
import er.extensions.ERXStringUtilities;

public class Session extends ERXSession {

    public class Handler implements NSKeyValueCoding, NSKeyValueCoding.ErrorHandling {

        public void takeValueForKey(Object value, String key) {
            throw new UnsupportedOperationException("Can't takeValueForKey");
        }

        public Object valueForKey(String key) {
            key = ERXStringUtilities.uncapitalize(key);
            return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
        }

        public Object handleQueryWithUnboundKey(String key) {
            return NSKeyValueCoding.Utility.valueForKey(Factory.bugTracker(), key);
        }

        public void handleTakeValueForUnboundKey(Object value, String key) {
            throw new UnsupportedOperationException("Can't handleTakeValueForUnboundKey");
        }

        public void unableToSetNullForKey(String key) {
            throw new UnsupportedOperationException("Can't unableToSetNullForKey");
        }

    }

    public class ComponentHandler extends Handler {
 
    }

    public class PeopleHandler extends Handler {

    }

    public class FrameworkHandler extends Handler {
 
    }

    public class RequirementHandler extends Handler {

    }

    public class TestItemHandler extends Handler {

    }

    public class ReleaseHandler extends Handler {
    }

    public class BugHandler extends Handler {

    }

    public NSMutableDictionary handlers = new NSMutableDictionary();

    public Session() {
        super();
        handlers.setObjectForKey(new ReleaseHandler(), "releases");
        handlers.setObjectForKey(new BugHandler(), "bugs");
        handlers.setObjectForKey(new ComponentHandler(), "components");
        handlers.setObjectForKey(new PeopleHandler(), "peoples");
        handlers.setObjectForKey(new FrameworkHandler(), "frameworks");
        handlers.setObjectForKey(new TestItemHandler(), "testItems");
        handlers.setObjectForKey(new RequirementHandler(), "requirements");
    }

    public void setDefaultEditingContext(EOEditingContext newEc) {
        super.setDefaultEditingContext(newEc);
    }

    public NSArray indentedComponents() {
    	return Component.clazz.orderedComponents(defaultEditingContext());
    }
 
    private String _lastname;

    private String _firstname;

    private NSArray _activeUsers;

    public NSArray activeUsers() {
        if (_activeUsers == null) {
            _activeUsers = People.clazz.activeUsers(defaultEditingContext());
        }
        return _activeUsers;
    }

    protected People _user;

    public People user() {
        return _user;
    }

    public void setUser(People user) {
        _user = user;
        ERCoreBusinessLogic.setActor(user());
    }
    
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    	if(context().page() instanceof ERD2WInspectPage) {
    		EOEnterpriseObject eo = ((ERD2WInspectPage)context().page()).object();
    		if (eo instanceof Markable) {
				Markable markable = (Markable) eo;
				markable.markAsRead();
			}
    	}
    	super.appendToResponse(aResponse, aContext);
    }
    
    public void awake() {
        super.awake();
        if (user() != null) {
            ERCoreBusinessLogic.setActor(user());
        }
    }

    public void sleep() {
        ERCoreBusinessLogic.setActor(null);
        super.sleep();
    }

    public String navigationRootChoice() {
    	
    	People user = (People) user();
    	
    	if(user != null && user.isActiveAsBoolean()) {
    		if(user.isAdminAsBoolean()) {
    			return "admin";
    		}
    		if(user.isEngineeringAsBoolean()) {
    			return "engineer";
    		}
    	}
    	return "none";
    }
}
