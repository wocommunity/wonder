/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.bugtracker.Session;
import er.extensions.ERXNavigationManager;
import er.extensions.ERXNavigationState;

public class MenuHeader extends WOComponent {

    public MenuHeader(WOContext aContext) {
        super(aContext);
    }

    public String item;

    public NSArray mainMenuItems() {
        return ERXNavigationManager.manager().navigationItemForName("MainMenu").children();
    }

    public NSKeyValueCoding navigationContext() {
        NSKeyValueCoding context = (NSKeyValueCoding)session().objectForKey("navigationContext");

        if (context().page() instanceof D2WPage) {
            context = ((D2WPage)context().page()).d2wContext();
        }

        //log.debug(ERXNavigationManager.manager().navigationStateForSession(session()));
        if(context == null) {
            context = new NSMutableDictionary();
            session().setObjectForKey(context, "navigationContext");
        }
        ERXNavigationState state = ERXNavigationManager.manager().navigationStateForSession(session());
        // log.debug("NavigationState:" + state + "," + state.state() + "," + state.stateAsString());
        //log.info("navigationContext:" + session().objectForKey("navigationContext"));
        return context;
    }

    public WOComponent homeAction() {
        return pageWithName("HomePage");
    }
    
    public boolean quickSearchDisabled() {
    	return entityName() == null;
    }

    public String entityName() {
        return (String) parent().valueForKeyPath("d2wContext.entity.name");
    }
 
    public WOComponent editMyInfo() {
        EOEnterpriseObject user = ((Session) session()).user();
        EditPageInterface epi = (EditPageInterface) D2W.factory().pageForConfigurationNamed("EditMyPeople", session());
        epi.setObject(user);
        epi.setNextPage(context().page());
        return (WOComponent) epi;
    }

    public WOComponent freeQuery() {
        return pageWithName("FreeQuery");
    }

    protected Integer bugNumber;

    public WOComponent findBugByNumber() {
        EOEntity entity = EOUtilities.entityNamed(session().defaultEditingContext(), "Bug");
        EOQualifier q = new EOKeyValueQualifier((String) entity.primaryKeyAttributeNames().lastObject(), EOQualifier.QualifierOperatorEqual, bugNumber);
        WOComponent result = null;
        EODatabaseDataSource ds = new EODatabaseDataSource(session().defaultEditingContext(), "Bug");
        EOFetchSpecification fs = new EOFetchSpecification("Bug", q, null);
        NSArray bugs = session().defaultEditingContext().objectsWithFetchSpecification(fs);
        if (bugs != null && bugs.count() == 1) {
            InspectPageInterface ipi = D2W.factory().inspectPageForEntityNamed("Bug", session());
            ipi.setObject((EOEnterpriseObject) bugs.objectAtIndex(0));
            ipi.setNextPage(context().page());
            result = (WOComponent) ipi;
        } else {
            ds.setFetchSpecification(fs);
            ListPageInterface lpi = (ListPageInterface) D2W.factory().listPageForEntityNamed("Bug", session());
            lpi.setDataSource(ds);
            lpi.setNextPage(context().page());
            result = (WOComponent) lpi;
        }
        return result;
    }
}
