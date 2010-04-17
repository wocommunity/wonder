/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import er.corebusinesslogic.ERCoreBusinessLogic;
import er.extensions.appserver.ERXSession;

public class Session extends ERXSession {
    public Session() {
        super();
    }

    public void  setDefaultEditingContext(EOEditingContext newEc) {
        super.setDefaultEditingContext(newEc);
    }

    
    protected String _lastname;
    protected String _firstname;

    private NSArray _activeUsers;
    public NSArray activeUsers() {
        if (_activeUsers==null) {
            _activeUsers=People.clazz.activeUsers(defaultEditingContext());
        }
        return _activeUsers;
    }    
    
    protected EOEnterpriseObject _user;
    public EOEnterpriseObject getUser() { return _user; }
    public void setUser(EOEnterpriseObject user) {
        _user = user;
        ERCoreBusinessLogic.setActor(getUser());
    }
    
    public void awake() {
        super.awake();
        if (getUser() != null) ERCoreBusinessLogic.setActor(getUser());
    }

    public void sleep() {
        ERCoreBusinessLogic.setActor(null);
        super.sleep();
    }
}

