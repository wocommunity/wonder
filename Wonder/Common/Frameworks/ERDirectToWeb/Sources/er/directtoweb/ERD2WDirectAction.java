//
// ERD2WDirectAction.java: Class file for WO Component 'ERD2WDirectAction'
// Project ERDirectToWeb
//
// Created by ak on Mon Apr 22 2002
//
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;
import java.util.Enumeration;

/**
 * Cool class to automatically create page configurations from URLs.<br />
 * Examples:
 *   QueryArticle
 * will create an query page for articles.
 *
 *   QueryArticle?__fs=findNewArticles
 * will create an query page for fetch spec "findNewArticles". This will only work if your rules return a ERD2WQueryPageWithFetchSpecification.
 *
 *   InspectArticle?__key=<articleid>
 * will create an inpect page for the given article.
 *
 *   EditArticle?__key=<articleid>
 * will create an edit page for the given article.
 *
 *   CreateArticle
 * will create an edit page for a newly created article.
 *
 *   ListArticle?__key=<userid>&__keypath=User.articles
 * will list the articles of the given user.
 *
 *   ListArticle?__fs=recentArticles&authorName=*foo*
 * will list the articles by calling the fetch spec "recentArticles". When the
 * fetch spec has an "authorName" binding, it is set to "*foo*".
 * 
 */

public abstract class ERD2WDirectAction extends ERXDirectAction {

    /** logging support */
    protected static final ERXLogger log = ERXLogger.getERXLogger(ERD2WDirectAction.class);
    protected final ERXLogger actionLog = ERXLogger.getERXLogger(ERD2WDirectAction.class.getName() + ".actions");

    /**
     * Public constructor
     * @param r current request
     */
    public ERD2WDirectAction(WORequest r) { super(r); }

    /**
     * primaryKeyKey is used to identity a given object via it's primary key.
     */
    static final String primaryKeyKey = "__key";

    /**
     * keyPathKey is used to get relationships of a given object.
     */
    static final String keyPathKey = "__keypath";

    /**
     * fetchSpecificationKey is used to get relationships of a given object.
     */
    static final String fetchSpecificationKey = "__fs";

    /** denotes the context ID for the previous page */
    static final String contextIDKey = "__cid";

    
    static final String createPrefix = "Create";

    /** For edit pages, we always use a fresh editing context. */
    protected EOEditingContext newEditingContext() {
        return ERXEC.newEditingContext(session().defaultEditingContext().parentObjectStore());
    }

    /** Retrieves and executes the fetch specification given in the request. */
    public EOFetchSpecification fetchSpecificationFromRequest(String entityName) {
        String fsName = context().request().stringFormValueForKey(fetchSpecificationKey);
        if(fsName != null) {
            EOFetchSpecification fs = EOFetchSpecification.fetchSpecificationNamed(fsName, entityName);
            NSMutableDictionary bindings = new NSMutableDictionary();
            Enumeration e = fs.qualifier().bindingKeys().objectEnumerator();
            while(e.hasMoreElements()) {
                String key = (String)e.nextElement();
                String formValue = context().request().stringFormValueForKey(key);
                if(formValue != null)
                    bindings.setObjectForKey(formValue,key);
            }

            if(bindings.count() > 0) {
                return fs.fetchSpecificationWithQualifierBindings(bindings);
            } else {
                return fs;
            }
        }
        return null;
    }

    public Number primaryKeyFromRequest() {
        return context().request().numericFormValueForKey(primaryKeyKey, new NSNumberFormatter("#"));
    }

    public WOComponent previousPageFromRequest() {
        String cid = context().request().stringFormValueForKey(contextIDKey);
        if(cid == null) return context().page();
        WOComponent comp = session().restorePageForContextID(cid);
        // (ak) we need to put the component to sleep again
        if(comp != null)
            comp._sleepInContext(comp.context());
        return comp;
    }

    public String keyPathFromRequest() {
        return context().request().stringFormValueForKey(keyPathKey);
    }

    public EOArrayDataSource relationshipArrayFromRequest(EOEditingContext ec, EOClassDescription cd) {
        String keyPath = context().request().stringFormValueForKey(keyPathKey);
        if(keyPath != null) {
            int indexOfDot = keyPath.indexOf(".");
            if(indexOfDot > 0) {
                String entityName = keyPath.substring(0, indexOfDot);
                String relationshipPath = keyPath.substring(indexOfDot+1, keyPath.length());
                EOEnterpriseObject eo = EOUtilities.objectWithPrimaryKeyValue(ec, entityName, primaryKeyFromRequest());
                EOArrayDataSource ds = new EOArrayDataSource(cd, ec);
                ds.setArray((NSArray)eo.valueForKeyPath(relationshipPath));
                return ds;
            }
        }
        return null;
    }

    public WOActionResults dynamicPageForActionNamed(String anActionName) {
        WOComponent newPage = null;
        try {
            newPage = D2W.factory().pageForConfigurationNamed(anActionName, session());
        } catch (IllegalStateException ex) {
            // this will get thrown when a page simply isn't found. We don't really need to report it
            actionLog.debug("dynamicPageForActionNamed failed for Action:" + anActionName, ex);
            return null;
        }

        String entityName = null;
        String taskName = null;
        D2WContext context = null; 
        if(newPage instanceof D2WPage) {
            context = ((D2WPage)newPage).d2wContext();
        } else {
            context = new D2WContext(session());
            context.setDynamicPage(anActionName);
        }
        EOEntity entity = (EOEntity)context.entity();
        taskName = (String)context.task();
        
        if(entity != null) {
            entityName = ((EOEntity)context.entity()).name();

            if(newPage instanceof EditPageInterface && taskName.equals("edit")) {
                EditPageInterface epi=(EditPageInterface)newPage;
                EOEditingContext ec = newEditingContext();
                EOEnterpriseObject eo = null;

                ec.lock();
                try {
                    if(anActionName.startsWith(createPrefix) || primaryKeyFromRequest() == null) {
                        eo = EOUtilities.createAndInsertInstance(ec,entityName);
                    } else {
                        eo = EOUtilities.objectWithPrimaryKeyValue(ec, entityName, primaryKeyFromRequest());
                    }
                } finally {
                    ec.unlock();
                }
                epi.setObject(eo);
                epi.setNextPage(previousPageFromRequest());
            } else if(newPage instanceof InspectPageInterface) {
                InspectPageInterface ipi=(InspectPageInterface)newPage;
                EOEditingContext ec = session().defaultEditingContext();
                EOEnterpriseObject eo = EOUtilities.objectWithPrimaryKeyValue(ec, entityName, primaryKeyFromRequest());
                ipi.setObject(eo);
                ipi.setNextPage(previousPageFromRequest());
            } else if(newPage instanceof QueryPageInterface) {
                QueryPageInterface qpi=(QueryPageInterface)newPage;
                EOEditingContext ec = session().defaultEditingContext();
                EOFetchSpecification fs = fetchSpecificationFromRequest(entityName);
                if(qpi instanceof ERD2WQueryPageWithFetchSpecification) {
                    if(fs != null)
                        ((ERD2WQueryPageWithFetchSpecification)qpi).setFetchSpecification(fs);
                }
            } else if(newPage instanceof ListPageInterface) {
                ListPageInterface lpi=(ListPageInterface)newPage;
                EOEditingContext ec = session().defaultEditingContext();
                EODataSource ds = relationshipArrayFromRequest(ec, entity.classDescriptionForInstances());
                if(ds == null) {
                    ds = new EODatabaseDataSource(ec, entityName);
                    EOFetchSpecification fs = fetchSpecificationFromRequest(entityName);
                    if(fs != null)
                        ((EODatabaseDataSource)ds).setFetchSpecification(fs);
                }
                lpi.setDataSource(ds);
                lpi.setNextPage(previousPageFromRequest());
            }
        }
        return (WOActionResults)newPage;
    }

    public WOActionResults reportException(Exception ex) {
        WOActionResults newPage = null;
        try {
            ErrorPageInterface epf=D2W.factory().errorPage(session());
            epf.setMessage(ex.toString());
            epf.setNextPage(previousPageFromRequest());
            newPage = (WOActionResults)epf;
        } catch (Exception otherException) {
            log.error("Exception while trying to report exception!", otherException);
        }
        return newPage;
    }
    
    /** Overrides the default implementation to try to look up the action as a page configuration if there is no method with the wanted name. This implementation catches NoSuchMethodException more or less silently, so be sure to turn on logging. */
    public WOActionResults performActionNamed(String anActionName) {
        WOActionResults newPage = null;
        try {
            try {
                if(false) throw new NoSuchMethodException(); //keep the compiler happy
                newPage = super.performActionNamed(anActionName);
            } catch (NSForwardException fwe) {
                if(!(fwe.originalException() instanceof NoSuchMethodException))
                    throw fwe;
                actionLog.debug("performActionNamed for action: " + anActionName, fwe);
            } catch (NoSuchMethodException nsm) {
                // this will get thrown when an action isn't found. We don't really need to report it.
                actionLog.debug("performActionNamed for action: " + anActionName, nsm);
            }
            if(newPage == null)
                newPage = dynamicPageForActionNamed(anActionName);
        } catch(Exception ex) {
            log.error("Error with action " + anActionName + ":" + ex + ", formValues:" + context().request().formValues(), ex);
            newPage = reportException(ex);
        }
        return newPage;
    }
}
