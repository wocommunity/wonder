/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.directtoweb;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

public class ERD2WFactory extends D2W {


    private D2WContext _privateContext;
    public D2WContext privateContext(WOSession s) {
        if (_privateContext==null) {
            _privateContext=new D2WContext(s);
        }
        _privateContext.takeValueForKey(s,"session");
        return _privateContext;
    }
    
    public NSArray visibleEntityNames(WOSession s) {        
        return D2WUtils.visibleEntityNames(privateContext(s));
    }

    
    public void myCheckRules() {
        if (!WOApplication.application().isCachingEnabled()) {
            ERD2WModel.erDefaultModel().checkRules();
        }
    }
    
    public WOComponent pageForConfigurationNamed(String name, WOSession s) {
        myCheckRules();
        return super. pageForConfigurationNamed(name, s);
    }
    
    public WOComponent pageForTaskAndEntityNamed(String task, String entityName, WOSession session) {
        myCheckRules();
        D2WContext newContext=new D2WContext(session);
        newContext.setTask(task);
        EOEntity newEntity=entityName!=null ? EOModelGroup.defaultGroup().entityNamed(entityName) : null;
        if (newEntity!=null) newContext.setEntity(newEntity);
        String config="__"+task+"__"+entityName;
        // saves 2 significant keys, task and entity!
        newContext.takeValueForKey(config,"pageConfiguration");        
        WOComponent newPage=WOApplication.application().pageWithName(newContext.pageName(),session.context());
        if (newPage instanceof D2WComponent) {
            ((D2WComponent)newPage).setLocalContext(newContext);
        }
        return newPage;
    }    
}

