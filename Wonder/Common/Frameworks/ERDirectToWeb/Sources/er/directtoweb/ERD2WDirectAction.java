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

public abstract class ERD2WDirectAction extends ERXDirectAction {

    /** logging support */
    static final ERXLogger log = ERXLogger.getERXLogger(ERD2WDirectAction.class);

    /**
     * Public constructor
     * @param r current request
     */
    public ERD2WDirectAction(WORequest r) { super(r); }

    /** primaryKeyKey is used to identity a given object via it's primary key.
    *   EditArticle?__key=<userid>&__keypath=User.articles
    * will list the articles of the given user.
    */
    static final String primaryKeyKey = "__key";

    /** keyPathKey is used to get relationships of a given object.
     *   ListArticle?__key=<userid>&__keypath=User.articles
     * will list the articles of the given user.
     */
    static final String keyPathKey = "__keypath";

    static final String contextIDKey = "__cid";
    static final String fetchSpecificationKey = "__fs";
    static final String createPrefix = "Create";

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

    public WOActionResults performActionNamed(String anActionName) {
        WOComponent newPage;
        try {
            return super.performActionNamed(anActionName);
        } catch(Exception ex) {
            if(log.isDebugEnabled())
                ex.printStackTrace();
            try {
                newPage = D2W.factory().pageForConfigurationNamed(anActionName, session());
                String entityName = (String)newPage.valueForKeyPath("d2wContext.entity.name");
                String taskName = (String)newPage.valueForKeyPath("d2wContext.task");
                log.debug(entityName + "-" + taskName);
                if(newPage instanceof EditPageInterface && taskName.equals("edit")) {
                    EditPageInterface epi=(EditPageInterface)newPage;
                    EOEditingContext ec = ERXExtensions.newEditingContext(session().defaultEditingContext().parentObjectStore());
                    EOEnterpriseObject eo = null;

                    if(anActionName.startsWith(createPrefix)) {
                        eo = EOUtilities.createAndInsertInstance(ec,entityName);
                    } else {
                        eo = EOUtilities.objectWithPrimaryKeyValue(ec, entityName, primaryKeyFromRequest());
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
                    /*(ak) Use a branch delegate? But from where?
                    String nextPageConfiguration = (String)newPage.valueForKeyPath("d2wContext.nextPageConfiguration");
                    if(nextPageConfiguration == null)
                        nextPageConfiguration = (String)newPage.valueForKeyPath("d2wContext.listConfigurationNameForEntity");
                    qpi.setNextPageDelegate(new ERDNextPageConfigurationDelegate(nextPageConfiguration));
                    log.info(nextPageConfiguration);
                    or use this???
                     WOComponent previousPage = previousPageFromRequest();
                    if(previousPage != null)
                        qpi.setNextPageDelegate(new ERDNextPageDelegate(previousPage));
                    */
                 } else if(newPage instanceof ListPageInterface) {
                    ListPageInterface lpi=(ListPageInterface)newPage;
                    EOEditingContext ec = session().defaultEditingContext();
                    EOEntity entity = (EOEntity)newPage.valueForKeyPath("d2wContext.entity");
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
            } catch(Exception ex1) {
                ErrorPageInterface epf=D2W.factory().errorPage(session());
                epf.setMessage(ex1.toString());
                epf.setNextPage(previousPageFromRequest());
                log.error("Error with action " + anActionName + ":" + ex1 + ", formValues:" + context().request().formValues());
                if(log.isDebugEnabled())
                    ex1.printStackTrace();
                newPage = (WOComponent)epf;
            }
        }
        return newPage;
    }
}
