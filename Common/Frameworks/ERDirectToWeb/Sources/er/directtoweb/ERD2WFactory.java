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


/**
 * Not used at the moment, but shows how it might be used in the future.<br />
 * 
 */

public class ERD2WFactory extends D2W {

    /** logging support */
    protected static final ERXLogger log = ERXLogger.getERXLogger(ERD2WFactory.class);

    /**
     * Gets the D2W facotry cast as an ERD2WFactory objects.
     * @return the singleton factory
     */
    public static ERD2WFactory erFactory() {
        return (ERD2WFactory)D2W.factory();
    }
    
    private D2WContext _privateContext;

    /** holds a reference to the default delegate */
    protected Object defaultListPageDisplayGroupDelegate;

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


    /**
     * Gets the default list page delegate for
     * display groups
     * @return default list page display group delegate
     */
    public Object defaultListPageDisplayGroupDelegate() {
        return defaultListPageDisplayGroupDelegate;
    }


    /**
     * Sets the default display group delegate for list
     * pages
     * @param delegate object
     */    
    public void setDefaultListPageDisplayGroupDelegate(Object delegate) {
        defaultListPageDisplayGroupDelegate = delegate;
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
        EOEnterpriseObject eo;
        try {
	    ec.lock();
            eo = ERXUtilities.createEO(entityName, ec);
        } finally {
            ec.unlock();
        }
        return eo;
    }


    public EditPageInterface editPageForNewObjectWithEntityNamed(String entityName, WOSession session) {
        EditPageInterface epi = editPageForEntityNamed(entityName, session);
        EOEditingContext peerContext = ERXExtensions.newEditingContext(session.defaultEditingContext().parentObjectStore());
	EOEnterpriseObject newObject = _newObjectWithEntityNamed(entityName, peerContext);
	epi.setObject(newObject);
	peerContext.hasChanges();
	return epi;
    }

    public EditPageInterface editPageForNewObjectWithConfigurationNamed(String configurationName, WOSession session) {
	EditPageInterface epi = (EditPageInterface) pageForConfigurationNamed(configurationName, session);
	EOEditingContext peerContext = ERXExtensions.newEditingContext(session.defaultEditingContext()
								       .parentObjectStore());
	D2WContext d2wcontext = ((D2WPage)epi).d2wContext();
	String entityName = d2wcontext.entity().name();
	EOEnterpriseObject newObject = _newObjectWithEntityNamed(entityName, peerContext);
	epi.setObject(newObject);
	peerContext.hasChanges();
	return epi;
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

    /**
     * Gets the <code>pageConfiguration</code> from the current page.
     */
    // FIXME ak Actually, we don't need to be static
    public static String pageConfigurationFromPage(WOComponent page) {
        String pageConfiguration = null;
        if(page instanceof D2WPage) {
            if(((D2WPage)page).d2wContext() != null) {
                pageConfiguration = ((D2WPage)page).d2wContext().dynamicPage();
            }
        }
        if(pageConfiguration == null) {
            String task = ERD2WFactory.taskFromPage(page);
            String entityName = ERD2WFactory.entityNameFromPage(page);
            if(task != null) {
                task = ERXStringUtilities.capitalize(task);
            } else {
                task = "";
            }
            if(entityName != null) {
                entityName = ERXStringUtilities.capitalize(entityName);
            } else {
                entityName = "";
            }
            pageConfiguration = task + entityName;
        }
        return pageConfiguration;
    }

    /**
     * Gets the task from the current page. Currently we have this class
     * because the corresponding method in D2W is protected. But it will be enhanced to
     * take the ERD2W interfaces into account.
     */

    // FIXME ak Actually, we don't need to be static
    // FIXME ak We need to take the ERD2W interfaces into account
    public static String taskFromPage(WOComponent page) {
        if(page == null)
            return null;
        if(page instanceof D2WPage)
            return ((D2WPage)page).task();
        if(page instanceof EditRelationshipPageInterface)
            return "editRelationship";
        if(page instanceof QueryPageInterface)
            return "query";
        if(page instanceof ListPageInterface)
            return "list";
        if(page instanceof EditPageInterface)
            return "edit";
        if(page instanceof InspectPageInterface)
            return "inspect";
        if(page instanceof SelectPageInterface)
            return "select";
        return "";
    }

    /**
     * Gets the entity name from the current page. Not that this does not
     * go up the component tree, but rather calls<code>entityName()</code>
     * and tries the "super" implementation if that fails.
     */
    // FIXME ak Actually, we don't need to be static
    public static String entityNameFromPage(WOComponent page) {
        if(page instanceof D2WPage) {
            try {
                return ((D2WPage)page).entityName();
            } catch(Exception ex) {
                log.warn("Page " + page.getClass().getName() + " does not return an entityName(), please implement the method entityName() correctly");
            }
        }
        return D2W.entityNameFromPage(page);
    }
}
