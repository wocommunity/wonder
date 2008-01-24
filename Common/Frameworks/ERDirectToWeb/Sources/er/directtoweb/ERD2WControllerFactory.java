package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.directtoweb.QueryPageInterface;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSForwardException;

import er.extensions.ERXEC;
import er.extensions.ERXPatcher;

/**
 * ERD2WControllerFactory a an enhancement of the D2W factory class with the notion of "Processes". <br />
 * A Process - or controller is an abstraction of a sequence of pages. For example, when you want to edit an object, you start at the edit page, make your change, save, get a confirmation page and finally go back to where you started. <br />
 * The major benefit against simply using pageConfigurations and NextPageDelegates is that it is very confusing to link them together in a flow of pages. The second benefit is that it inherits from branchDelegate, which lets you make up a very flexible UI, you can have page-level actions mixed into the property repetitions for example. Also, you get much more control of the flow of complex tasks where you simply would get lost when you use bare NextPageDelegates and you can more easily create custom pages in between the flow. And finally, they make for great re-use and testing, because you can simply fake the actions the user took.<br />
 * Controllers are instatiated via something like:<br /><code><pre>
 public WOActionResults TestEditAction() {
     ERD2WControllerFactory.ERCCreate erc = (ERD2WControllerFactory.ERCCreate)ERD2WControllerFactory.controllerFactory().controllerForName("CreateUser", session());
     erc.setFinalPage(previousPageFromRequest());
     erc.setPrimaryKeyValue(primaryKeyFromRequest());
     return (WOActionResults)erc.firstPage();
 }
 * </pre></code>
 * They can be subclassed and you can change the flow of your app without the need to create subclasses of your pages - which spares you the hassle to deal with the duplicated HTML.
 * A controller gets instantiated via a D2W rule like:<br />
 <code>(controllerName = "EditDocument") => controllerClassName = "er.directtoweb.ERD2WControllerFactory$ERCEdit"</code><br />
 and you might need supporting wildcard rules like the one for the pageConfigurations. Also, rules like:<br />
 <code>controllerName <> null => pageConfiguration = controllerName [KeyValueAssignment] [0]</code><br />
will spare you a lot of work.

 * The ERD2WControllerFactory is not heavily tested and the API might change. Especially that the controller subclasses are inner classes of this factory is subject to change. Feedback would be very welcome. 
 * @created ak on Tue Apr 08 2003
 * @project AHApp
 */

public class ERD2WControllerFactory extends ERD2WFactory {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERD2WControllerFactory.class);
	
    /**
     * Public constructor
     */
    public ERD2WControllerFactory() {
        super();
    }

    /**
     * Gets the D2W factory cast as an ERD2WControllerFactory object.
     * @return the singleton factory
     */
    public static ERD2WControllerFactory controllerFactory() {
        return (ERD2WControllerFactory)D2W.factory();
    }

    protected ERD2WController controllerInstanceWithContext(D2WContext d2wContext) {
        ERD2WController c = (ERD2WController)d2wContext.valueForKey("controller");
        try {
            String controllerClassName = (String)d2wContext.valueForKey("controllerClassName");
            if(controllerClassName != null)
                c = (ERD2WController)ERXPatcher.classForName(controllerClassName).newInstance();
        } catch(Exception ex) {
            throw new NSForwardException(ex);
        }
        if(c != null) {
            c.setD2WContext(d2wContext);
            c.setSession((WOSession)d2wContext.valueForKey("session"));
            c.setControllerName((String)d2wContext.valueForKey("controllerName"));
            c.d2wContext().setDynamicPage((String)d2wContext.valueForKey("pageConfiguration"));
        }
        return c;
    }

    public ERD2WController controllerForName(String controllerName, WOSession session) {
        myCheckRules();
        D2WContext newContext=ERD2WContext.newContext(session);
        newContext.takeValueForKey(controllerName, "controllerName");
        // give an InstanceCreationAssigment a chance
        ERD2WController c = controllerInstanceWithContext(newContext);

        return c;
    }

    public ERD2WController controllerForTaskAndEntityNamed(String task, String entityName, WOSession session) {
        myCheckRules();
        D2WContext newContext=ERD2WContext.newContext(session);
        newContext.setTask(task);
        EOEntity newEntity=entityName!=null ? EOModelGroup.defaultGroup().entityNamed(entityName) : null;
        if (newEntity!=null) newContext.setEntity(newEntity);
        String controllerName="__"+task+"__"+entityName;
        // saves 2 significant keys, task and entity!
        newContext.takeValueForKey(controllerName,"controllerName");

        ERD2WController c = controllerInstanceWithContext(newContext);

        return c;
    }
    
    public static class ERD2WController extends ERDBranchDelegate {
        protected WOSession session;
        protected WOComponent finalPage;
        protected D2WContext d2wContext;
        protected String controllerName;
        
        public ERD2WController(D2WContext c) {
            d2wContext = c;
        }
        public ERD2WController() {
        }
        
        public String controllerName() {
            return controllerName;
        }
        public void setControllerName(String value) {
            controllerName = value;
        }

        public WOComponent finalPage() {
            return finalPage;
        }
        public void setFinalPage(WOComponent c) {
            finalPage = c;
        }

        public D2WContext d2wContext() {
            if(d2wContext == null) {
                d2wContext = ERD2WContext.newContext(session());
            }
            return d2wContext;
        }
        public void setD2wContext(D2WContext value) {
            setD2WContext(value);
        }
        public void setD2WContext(D2WContext value) {
            d2wContext = value;
        }
        
        public WOSession session() {
            if(session == null) {
                session = finalPage() == null ? null : finalPage().session();
            }
            return session;
        }
        public void setSession(WOSession s) {
            session = s;
        }
        
        public WOComponent firstPage() {
            return finalPage;
        }
        public void setFirstPage(WOComponent value) {
            finalPage = value;
        }
        
        public WOContext context() {
            return session().context();
        }
    }
    
    public static class ERCCore extends ERD2WController {        
        public ERCCore() {
        }

        protected WOComponent pageWithContextTaskEntity(String task, String entityName) {
            D2WModel.defaultModel().checkRules();
            EOEntity eoentity = (entityName == null ? null : EOModelGroup.defaultGroup().entityNamed(entityName));
            if (eoentity == null && entityName != null && !entityName.equals("")
                && !entityName.equals("*all*"))
                throw new IllegalArgumentException("Could not find entity named " + entityName);
            d2wContext().setTask(task);
            d2wContext().setEntity(eoentity);
            WOComponent wocomponent = WOApplication.application().pageWithName(d2wContext().pageName(), session().context());
            if (wocomponent instanceof D2WComponent)
                ((D2WComponent) wocomponent).setLocalContext(d2wContext());
            return wocomponent;
        }

        protected WOComponent pageForConfigurationNamed(String pageConfiguration) {
            D2WModel.defaultModel().checkRules();
            d2wContext().setDynamicPage(pageConfiguration);
            if (d2wContext().entity() == null || d2wContext().task() == null)
                throw new IllegalArgumentException("Either no entity or task for pc: " + pageConfiguration);
            WOComponent wocomponent = WOApplication.application().pageWithName(d2wContext().pageName(), session().context());
            if (wocomponent instanceof D2WComponent)
                ((D2WComponent) wocomponent).setLocalContext(d2wContext());
            return wocomponent;
        }

        public WOComponent firstPage() {
            return runWithPageConfiguration((String)d2wContext().valueForKey("pageConfiguration"));
        }
        public WOComponent runWithPageConfiguration(String value) {
            WOComponent page = pageForConfigurationNamed(value);
            page.takeValueForKey(this, "nextPageDelegate");
            return page;
        }

        public WOComponent returnAction(WOComponent sender) {
            return returnPage();
        }

        protected WOComponent returnPage() {
            return finalPage;
        }
    }

    public static class ERCSingleObject extends ERCCore {
        protected Object pk;
        protected EOEnterpriseObject object;
        protected EOEditingContext editingContext;
        
        public ERCSingleObject() {
            super();
        }
        
        public WOComponent runWithPageConfiguration(String value) {
            WOComponent start = super.runWithPageConfiguration(value);
            start.takeValueForKey(object(), "object");
            return start;
        }

        public EOEnterpriseObject object() {
            if(object == null && pk != null) {
                object = EOUtilities.objectWithPrimaryKeyValue(editingContext(), d2wContext().entity().name(), pk);
            }
            return object;
        }
        
        public void setObject(EOEnterpriseObject value) {
            object = value;
        }

        public EOEditingContext editingContext() {
            if(editingContext == null) {
                editingContext = object == null ? null : object.editingContext();
                if(editingContext == null) {
                    editingContext = session().defaultEditingContext();
                }
            }
            return editingContext;
        }

        public void setEditingContext(EOEditingContext value) {
            editingContext = value;
        }

        public void setPrimaryKeyValue(Object value) {
            pk = value;
        }

    }

    public static class ERCInspect extends ERCSingleObject {
        public ERCInspect() {
            super();
        }

        public WOComponent deleteObjectAction(WOComponent sender) {
            if(editingContext() != null) {
                log.info("Deleting Object: " + object());
                editingContext().deleteObject(object());
                editingContext().saveChanges();
            }
            return returnPage();
        }
    }
    
    public static class ERCEdit extends ERCSingleObject {
        public ERCEdit() {
            super();
        }

        public WOComponent cancelChangesAction(WOComponent sender) {
            if(editingContext().hasChanges()) {
                log.info("Reverting changes: " + object());
                editingContext().revert();
            }
            return returnPage();
        }

        public WOComponent saveChangesAction(WOComponent sender) {
            if(editingContext().hasChanges()) {
                log.info("Has changes: " + object());
                editingContext().saveChanges();
            }
            return returnPage();
        }
    }
    
    public static class ERCCreate extends ERCEdit {
        public EOEditingContext ec;
        
        public ERCCreate() {
            super();
        }
        
        public WOComponent runWithPageConfiguration(String value) {
            WOComponent start = super.runWithPageConfiguration(value);
            EOEnterpriseObject eo;
            EOEditingContext ec = ERXEC.newEditingContext();
            //this.log.debug("runWithPageConfiguration: pc <" + value + "> - " + d2wContext().entity());
            eo = EOUtilities.createAndInsertInstance(ec, d2wContext().entity().name());
            setObject(eo);
            start.takeValueForKey(object(), "object");
            return start;
        }
    }
    
    public static class ERCQuery extends ERCCore { // implements firstPage()
        protected EODataSource dataSource;

        public ERCQuery() {
            super();
        }

        public WOComponent searchAction(WOComponent currentPage) {
            D2WContext d2wContext = ((D2WComponent)currentPage.parent()).d2wContext();
            EODataSource ds = ((QueryPageInterface)currentPage.parent()).queryDataSource();
            log.info("searchAction: " + ds);
            ListPageInterface listpageinterface = null;
            String listConfigurationName=(String)d2wContext.valueForKey("listConfigurationName");
            if(listConfigurationName==null) {
                listConfigurationName = "__list__" + d2wContext().entity().name();
            }
            listpageinterface = (ListPageInterface)pageForConfigurationNamed(listConfigurationName);
            listpageinterface.setDataSource(ds);
            listpageinterface.setNextPage(returnPage());
            return (WOComponent) listpageinterface;
        }
        
        public void setDataSource(EODataSource value) {
            dataSource = value;
        }

        public WOComponent firstPage(String pageConfiguration) {
            QueryPageInterface qpi = (QueryPageInterface)pageForConfigurationNamed(pageConfiguration);
            qpi.setNextPageDelegate(this);
            return (WOComponent) qpi;
        }
    }
}
