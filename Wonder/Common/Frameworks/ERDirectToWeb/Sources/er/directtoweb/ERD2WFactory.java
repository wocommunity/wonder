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
import er.extensions.*;

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

    private EOEnterpriseObject _newObjectWithEntityNamed(String entityName, EOEditingContext ec) {
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        if (entity.isReadOnly()) {
            throw new IllegalArgumentException(" You can't create a new instance of " + entityName + ". It is a read-only entity.  It is marked read-only in the model.");
        }
        if (entity.isAbstractEntity()) {
            throw new IllegalArgumentException(" You can't create a new instance of " + entityName + ". It is an abstract entity");
        }
        ec.lock();
        EOEnterpriseObject eo;
        try {
            EOClassDescription cd = entity.classDescriptionForInstances();
            eo = (cd.createInstanceWithEditingContext(ec, null));
            ec.insertObject(eo);
        } finally {
            ec.unlock();
        }
        return eo;
    }
    public EditPageInterface editPageForNewObjectWithEntityNamed(String string, WOSession wosession) {
            EditPageInterface editpageinterface = editPageForEntityNamed(string, wosession);
            EOEditingContext eoeditingcontext
                = ERXExtensions.newEditingContext(wosession.defaultEditingContext()
                                       .parentObjectStore());
            EOEnterpriseObject eoenterpriseobject
                = _newObjectWithEntityNamed(string, eoeditingcontext);
            editpageinterface.setObject(eoenterpriseobject);
            eoeditingcontext.hasChanges();
            return editpageinterface;
        }
    public EditPageInterface editPageForNewObjectWithConfigurationNamed
        (String string, WOSession wosession) {
            EditPageInterface editpageinterface
            = (EditPageInterface) pageForConfigurationNamed(string, wosession);
            EOEditingContext eoeditingcontext
                = ERXExtensions.newEditingContext(wosession.defaultEditingContext()
                                       .parentObjectStore());
            D2WContext d2wcontext = ((D2WPage) editpageinterface).d2wContext();
            String entityName = d2wcontext.entity().name();
            EOEnterpriseObject eoenterpriseobject
                = _newObjectWithEntityNamed(entityName,                                            eoeditingcontext);
            editpageinterface.setObject(eoenterpriseobject);
            eoeditingcontext.hasChanges();
            return editpageinterface;
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

