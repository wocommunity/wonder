/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import er.extensions.*;
import er.corebusinesslogic.ERCoreBusinessLogic;

public class Session extends ERXSession {
    public Session() {
        super();
        /* ** Put your per-session initialization code here ** */
        ERXExtensions.setDefaultDelegate(defaultEditingContext());
        //defaultEditingContext().setTimestamp();
    }

    public void  setDefaultEditingContext(EOEditingContext newEc) {
        super.setDefaultEditingContext(newEc);
        log.debug("Set default EditingContext: " + newEc.getClass().getName());
    }

    
    protected String _lastname;
    protected String _firstname;

    private NSArray _activeUsers;
    public NSArray activeUsers() {
        if (_activeUsers==null) {
            _activeUsers=People.peopleClazz().activeUsers(defaultEditingContext());
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

