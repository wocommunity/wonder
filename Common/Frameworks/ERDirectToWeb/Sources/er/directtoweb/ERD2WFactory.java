/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.ConfirmPageInterface;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.D2WUtils;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.EditRelationshipPageInterface;
import com.webobjects.directtoweb.ErrorPageInterface;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.directtoweb.QueryAllPageInterface;
import com.webobjects.directtoweb.QueryPageInterface;
import com.webobjects.directtoweb.SelectPageInterface;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXEC;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXStringUtilities;
import er.extensions.ERXUtilities;

/**
 * Not used at the moment, but shows how it might be used in the future. <br />
 *  
 */

public class ERD2WFactory extends D2W {
    protected void init() {
        D2WModel model = D2WModel.defaultModel();
        if (!(model instanceof ERD2WModel)) {
            ERD2WModel erModel = ERD2WModel.erDefaultModel();
            D2WModel.setDefaultModel(erModel);
        }
        super.init();
    }
    
    public EditPageInterface editPageForEntityNamed(String entityName, WOSession session) {
		return (EditPageInterface) pageForConfigurationNamed("Edit" + entityName, session);
	}

	public EditRelationshipPageInterface editRelationshipPageForEntityNamed(String entityName, WOSession session) {
		return (EditRelationshipPageInterface) pageForConfigurationNamed("EditRelationship" + entityName, session);
	}

	public InspectPageInterface inspectPageForEntityNamed(String entityName, WOSession session) {
		return (InspectPageInterface) pageForConfigurationNamed("Inspect" + entityName, session);
	}

	public ConfirmPageInterface confirmPageForEntityNamed(String entityName, WOSession session) {
		return (ConfirmPageInterface) pageForConfigurationNamed("Confirm" + entityName, session);
	}

	public ListPageInterface listPageForEntityNamed(String entityName, WOSession session) {
		return (ListPageInterface) pageForConfigurationNamed("List" + entityName, session);
	}

	public QueryAllPageInterface queryAllPage(WOSession session) {
		return (QueryAllPageInterface) pageForConfigurationNamed("QueryAll", session);
	}

	public QueryPageInterface queryPageForEntityNamed(String entityName, WOSession session) {
		return (QueryPageInterface) pageForConfigurationNamed("Query" + entityName, session);
	}

	public SelectPageInterface selectPageForEntityNamed(String entityName, WOSession session) {
		return (SelectPageInterface) pageForConfigurationNamed("Select" + entityName, session);
	}

	/** logging support */
    protected static final Logger log = Logger.getLogger(ERD2WFactory.class);

    /**
     * Gets the D2W factory cast as an ERD2WFactory objects.
     * 
     * @return the singleton factory
     */
    public static ERD2WFactory erFactory() {
        return (ERD2WFactory) D2W.factory();
    }

    private D2WContext _privateContext;

    /** holds a reference to the default delegate */
    protected Object   defaultListPageDisplayGroupDelegate;

    public D2WContext privateContext(WOSession s) {
        if (_privateContext == null) {
            _privateContext = ERD2WContext.newContext(s);
        }
        _privateContext.takeValueForKey(s, "session");
        return _privateContext;
    }

    public NSArray visibleEntityNames(WOSession s) {
        return D2WUtils.visibleEntityNames(privateContext(s));
    }

    /**
     * Gets the default list page delegate for display groups
     * 
     * @return default list page display group delegate
     */
    public Object defaultListPageDisplayGroupDelegate() {
        return defaultListPageDisplayGroupDelegate;
    }

    public WOComponent defaultPage(WOSession wosession) {
        D2WContext d2wcontext = ERD2WContext.newContext(wosession);
        return pageWithContextTaskEntity(d2wcontext, d2wcontext.startupTask(), d2wcontext.startupEntityName(), wosession.context());
    }

    protected WOComponent pageWithContextTaskEntity(D2WContext d2wcontext, String s, String s1, WOContext wocontext) {
        myCheckRules();
        d2wcontext.setTask(s);
        EOEntity eoentity = s1 != null ? EOModelGroup.defaultGroup().entityNamed(s1) : null;
        if (eoentity == null && s1 != null && !s1.equals("") && !s1.equals("*all*")) { throw new IllegalArgumentException(
                "Could not find entity named " + s1); }
        d2wcontext.setEntity(eoentity);
        WOComponent wocomponent = WOApplication.application().pageWithName(d2wcontext.pageName(), wocontext);
        if (wocomponent instanceof D2WComponent) {
            ((D2WComponent) wocomponent).setLocalContext(d2wcontext);
        }
        return wocomponent;
    }

    /**
     * Sets the default display group delegate for list pages
     * 
     * @param delegate
     *            object
     */
    public void setDefaultListPageDisplayGroupDelegate(Object delegate) {
        defaultListPageDisplayGroupDelegate = delegate;
    }

    public void myCheckRules() {
        init();
        boolean checkRules = !WOApplication.application().isCachingEnabled();
        if (checkRules) {
            ERD2WModel.erDefaultModel().checkRules();
        }
    }

    public WOComponent pageForConfigurationNamed(String name, WOSession s) {
        myCheckRules();
        D2WContext d2wcontext = ERD2WContext.newContext(s.context().session());
        d2wcontext.setDynamicPage(name);
        if (d2wcontext.task() == null || d2wcontext.entity() == null) {
            String reason = null;
            if (d2wcontext.task() == null && d2wcontext.entity() == null) {
                reason = "task and entity is null, it seems that one model, maybe ERDirectToWeb d2w.d2wmodel is not loaded!";
            } else if (d2wcontext.task() == null) {
                reason = "task is null, it seems that one model, maybe ERDirectToWeb d2w.d2wmodel is not loaded!";
            } else if (d2wcontext.entity() == null) {
                reason = "entity is null, it seems that one model, maybe ERDirectToWeb d2w.d2wmodel is not loaded!";
            }
            throw new IllegalStateException("Couldn't find the dynamic page named " + name + " in your DirectToWeb model."+
                    reason);
        } 
        return pageWithContextTaskEntity(d2wcontext, d2wcontext.task(), d2wcontext.entity().name(), s.context());
//      return super.pageForConfigurationNamed(name, s);
       
    }

    private EOEntity _entityNamed(String entityName, WOSession session) {
        EOEditingContext ec = (session != null ? session.defaultEditingContext() : null);
        EOModelGroup group = (ec == null) ? EOModelGroup.defaultGroup() : EOUtilities.modelGroup(ec);
        return entityName != null ? group.entityNamed(entityName) : null;
    }

    private EOEnterpriseObject _newObjectWithEntity(EOEntity entity, EOEditingContext ec) {
        if (entity.isReadOnly()) { throw new IllegalArgumentException(" You can't create a new instance of " + entity.name()
                + ". It is a read-only entity.  It is marked read-only in the model."); }
        if (entity.isAbstractEntity()) { throw new IllegalArgumentException(" You can't create a new instance of " + entity.name()
                + ". It is an abstract entity"); }
        EOEnterpriseObject eo;
        try {
            ec.lock();
            eo = ERXEOControlUtilities.createAndInsertObject(ec, entity.name());
        } finally {
            ec.unlock();
        }
        return eo;
    }

    public EditPageInterface editPageForNewObjectWithEntityNamed(String entityName, WOSession session) {
        EditPageInterface epi = (EditPageInterface) pageForConfigurationNamed("Create" + entityName, session);
        EOEditingContext peerContext = ERXEC.newEditingContext(session.defaultEditingContext().parentObjectStore());
        EOEnterpriseObject newObject = _newObjectWithEntity(_entityNamed(entityName, session), peerContext);
        epi.setObject(newObject);
        peerContext.hasChanges();
        return epi;
    }

    public EditPageInterface editPageForNewObjectWithConfigurationNamed(String configurationName, WOSession session) {
        EditPageInterface epi = (EditPageInterface) pageForConfigurationNamed(configurationName, session);
        EOEditingContext peerContext = ERXEC.newEditingContext(session.defaultEditingContext().parentObjectStore());
        D2WContext d2wcontext = ((D2WPage) epi).d2wContext();
        EOEnterpriseObject newObject = _newObjectWithEntity(d2wcontext.entity(), peerContext);
        epi.setObject(newObject);
        peerContext.hasChanges();
        return epi;
    }

    public WOComponent pageForTaskAndEntityNamed(String task, String entityName, WOSession session) {
    	String pageConfiguration = ERXStringUtilities.capitalize(task) + (entityName == null ? ""  : entityName);
        return pageForConfigurationNamed(pageConfiguration, session);
    }

    public WOComponent printerFriendlyPageForD2WContext(D2WContext context, WOSession session) {
        myCheckRules();
        D2WContext newContext = ERD2WContext.newContext(session);
        String newTask = context.task().equals("edit") ? "inspect" : context.task();
        newContext.takeValueForKey(newTask, "task");
        // not using subTask directly here because the cache mechanism relies on
        // being able to compute wether this key
        // is 'computable' (subTask is since a rule can fire to give a default)
        // or an external output
        //        newContext.takeValueForKey("printerFriendly","subTask");
        newContext.takeValueForKey("printerFriendly", "forcedSubTask");
        newContext.takeValueForKey(context.valueForKey("pageName"), "existingPageName");
        newContext.takeValueForKey(context.valueForKey("subTask"), "existingSubTask");
        newContext.takeValueForKey(context.valueForKey("pageConfiguration"), "pageConfiguration");
        newContext.takeValueForKey(context.entity(), "entity");
        WOComponent result = WOApplication.application().pageWithName((String) newContext.valueForKey("pageName"), session.context());
        ((D2WPage) result).setLocalContext(newContext);
        return result;
    }

    public WOComponent csvExportPageForD2WContext(D2WContext context, WOSession session) {
        myCheckRules();
        D2WContext newContext = ERD2WContext.newContext(session);
        newContext.takeValueForKey(context.task(), "task");
        // not using subTask directly here because the cache mechanism relies on
        // being able to compute wether this key
        // is 'computable' (subTask is since a rule can fire to give a default)
        // or an external output
        newContext.takeValueForKey("csv", "forcedSubTask");
        newContext.takeValueForKey(context.valueForKey("pageName"), "existingPageName");
        newContext.takeValueForKey(context.valueForKey("subTask"), "existingSubTask");
        newContext.takeValueForKey(context.valueForKey("pageConfiguration"), "pageConfiguration");
        newContext.takeValueForKey(context.entity(), "entity");
        WOComponent result = WOApplication.application().pageWithName((String) newContext.valueForKey("pageName"), session.context());
        ((D2WPage) result).setLocalContext(newContext);
        return result;
    }

    public WOComponent pageForTaskSubTaskAndEntityNamed(String task, String subtask, String entityName, WOSession session) {
        myCheckRules();
        D2WContext newContext = ERD2WContext.newContext(session);
        newContext.setTask(task);
        newContext.setEntity(_entityNamed(entityName, session));
        newContext.takeValueForKey(subtask, "subTask");
        WOComponent result = WOApplication.application().pageWithName((String) newContext.valueForKey("pageName"), session.context());
        ((D2WPage) result).setLocalContext(newContext);
        return result;
    }

    public QueryPageInterface queryPageWithFetchSpecificationForEntityNamed(String fsName, String entityName, WOSession s) {
        WOComponent result = pageForTaskSubTaskAndEntityNamed("query", "fetchSpecification", entityName, s);
        result.takeValueForKey(fsName, "fetchSpecificationName");
        return (QueryPageInterface) result;
    }

    public WOComponent errorPageForException(Throwable e, WOSession s) {
        myCheckRules();
        ErrorPageInterface epi = D2W.factory().errorPage(s);
        if (epi instanceof ERDErrorPageInterface && e instanceof Exception) {
            ((ERDErrorPageInterface) epi).setException((Exception) e);
        }
        epi.setMessage(ERXUtilities.stackTrace(e));
        epi.setNextPage(s.context().page());
        return (WOComponent) epi;
    }

    // ak: These next set of methods are intented to be overidden and extended
    // however, the java compiler refuses to create a object method with the
    // same name as a static one, thus the "_" prefix
    /**
     * Gets the <code>pageConfiguration</code> from the current page.
     */
    protected String _pageConfigurationFromPage(WOComponent page) {
        String pageConfiguration = null;
        if (page instanceof D2WPage) {
            if (((D2WPage) page).d2wContext() != null) {
                pageConfiguration = ((D2WPage) page).d2wContext().dynamicPage();
            }
        }
        if (pageConfiguration == null) {
            String task = taskFromPage(page);
            String entityName = entityNameFromPage(page);
            if (task != null) {
                task = ERXStringUtilities.capitalize(task);
            } else {
                task = "";
            }
            if (entityName != null) {
                entityName = ERXStringUtilities.capitalize(entityName);
            } else {
                entityName = "";
            }
            pageConfiguration = task + entityName;
        }
        return pageConfiguration;
    }

    /**
     * Gets the task from the current page. Currently we have this class because
     * the corresponding method in D2W is protected. But it will be enhanced to
     * take the ERD2W interfaces into account.
     */

    // FIXME ak We need to take the ERD2W interfaces into account
    protected String _taskFromPage(WOComponent page) {
        if (page == null) return null;
        if (page instanceof D2WPage) return ((D2WPage) page).task();
        if (page instanceof EditRelationshipPageInterface) return "editRelationship";
        if (page instanceof QueryPageInterface) return "query";
        if (page instanceof ListPageInterface) return "list";
        if (page instanceof EditPageInterface) return "edit";
        if (page instanceof InspectPageInterface) return "inspect";
        if (page instanceof SelectPageInterface) return "select";
        return "";
    }

    /**
     * Gets the entity name from the current page. Not that this does not go up
     * the component tree, but rather calls <code>entityName()</code> and
     * tries the "super" implementation if that fails.
     */
    protected String _entityNameFromPage(WOComponent page) {
        if (page instanceof D2WPage) {
            try {
                return ((D2WPage) page).entityName();
            } catch (Exception ex) {
                log.warn("Page " + page.getClass().getName()
                        + " does not return an entityName(), please implement the method entityName() correctly");
            }
        }
        return D2W.entityNameFromPage(page);
    }

    /**
     * Gets the <code>entityName</code> from the current page. Simply wrap the
     * factory method {@link #_entityNameFromPage(WOComponent)}.
     */
    public static String entityNameFromPage(WOComponent page) {
        return ERD2WFactory.erFactory()._entityNameFromPage(page);
    }

    /**
     * Gets the <code>task</code> from the current page. Simply wrap the
     * factory method {@link #_taskFromPage(WOComponent)}.
     */
    public static String taskFromPage(WOComponent page) {
        return ERD2WFactory.erFactory()._taskFromPage(page);
    }

    /**
     * Gets the <code>pageConfiguration</code> from the current page. Simply
     * wrap the factory method {@link #_pageConfigurationFromPage(WOComponent)}.
     */
    public static String pageConfigurationFromPage(WOComponent page) {
        return ERD2WFactory.erFactory()._pageConfigurationFromPage(page);
    }
}
