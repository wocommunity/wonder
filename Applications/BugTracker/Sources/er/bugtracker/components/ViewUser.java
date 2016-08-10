/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.components;

import com.webobjects.appserver.WOContext;

import er.bugtracker.People;
import er.bugtracker.Session;
import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.eof.ERXEOControlUtilities;

public class ViewUser extends ERDCustomEditComponent {

    public ViewUser(WOContext aContext) {
        super(aContext);
    }

    @Override
    public boolean isStateless() {
        return true;
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    private People _user;

    public People user() {
        if (_user == null)
            _user = (People)objectPropertyValue();
        return _user;
    }

    @Override
    public void reset() {
        super.reset();
        _user = null;
    }

    public boolean userIsNotEngineering() {
        return user() != null && !user().isEngineering();
    }

    public boolean userIsNotSelf() {
        return user() != null && !ERXEOControlUtilities.eoEquals(user(), ((Session) session()).user());
    }
}
