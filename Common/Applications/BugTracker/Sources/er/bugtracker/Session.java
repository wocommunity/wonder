/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.corebusinesslogic.ERCoreBusinessLogic;
import er.extensions.ERXNavigation;
import er.extensions.ERXNavigationManager;
import er.extensions.ERXSession;
import er.extensions.ERXStringUtilities;

public class Session extends ERXSession {

    public class Handler implements NSKeyValueCoding, NSKeyValueCoding.ErrorHandling {

        private String _key;
        
        public Handler(String key) {
            _key = key;
            handlers.setObjectForKey(this, key);
        }

        public void takeValueForKey(Object value, String key) {
            throw new UnsupportedOperationException("Can't takeValueForKey");
        }

        public Object valueForKey(String key) {
            String keyPath = ERXStringUtilities.uncapitalize(key);
            WOActionResults result = (WOActionResults) NSKeyValueCoding.DefaultImplementation.valueForKey(this, keyPath);
            if (result instanceof D2WPage) {
                D2WPage page = (D2WPage) result;
                page.d2wContext().takeValueForKey(ERXStringUtilities.capitalize(_key) + "." + key, "navigationState");
            }
            return result;
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

        public ComponentHandler() {
            super("components");
        }
 
    }

    public class PeopleHandler extends Handler {

        public PeopleHandler() {
            super("peoples");
        }

    }

    public class FrameworkHandler extends Handler {

        public FrameworkHandler() {
            super("frameworks");
        }
 
    }

    public class RequirementHandler extends Handler {

        public RequirementHandler() {
            super("requirements");
        }

    }

    public class TestItemHandler extends Handler {

        public TestItemHandler() {
            super("testItems");
        }

    }

    public class ReleaseHandler extends Handler {

        public ReleaseHandler() {
            super("releases");
        }
    }

    public class BugHandler extends Handler {

        public BugHandler() {
            super("bugs");
        }

    }

    public NSMutableDictionary handlers = new NSMutableDictionary();

    public Session() {
        super();
        new ReleaseHandler();
        new BugHandler();
        new ComponentHandler();
        new PeopleHandler();
        new FrameworkHandler();
        new TestItemHandler();
        new RequirementHandler();
        setStoresIDsInCookies(true);
    }

    public void setDefaultEditingContext(EOEditingContext newEc) {
        super.setDefaultEditingContext(newEc);
    }
    
    
    public WOComponent editMyInfo() {
        EOEnterpriseObject user = user();
        EditPageInterface epi = (EditPageInterface) D2W.factory().pageForConfigurationNamed("EditMyPeople", this);
        epi.setObject(user);
        epi.setNextPage(context().page());
        return (WOComponent) epi;
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
    	
    	if(user != null && user.isActive()) {
    		if(user.isAdmin()) {
    			return "admin";
    		}
    		if(user.isEngineering()) {
    			return "engineer";
    		}
    	}
    	return "none";
    }
}
