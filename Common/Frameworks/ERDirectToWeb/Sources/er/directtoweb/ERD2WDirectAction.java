//
// ERD2WDirectAction.java: Class file for WO Component 'ERD2WDirectAction'
// Project ERDirectToWeb
//
// Created by ak on Mon Apr 22 2002
//
package er.directtoweb;

import java.text.ParseException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.ErrorPageInterface;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.directtoweb.QueryPageInterface;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNumberFormatter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.ERXDirectAction;
import er.extensions.ERXEC;
import er.extensions.ERXEOAccessUtilities;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXStringUtilities;
import er.extensions.ERXValueUtilities;

/**
 * Automatically creates page configurations from URLs.<br />
 * Examples:
 * <ul>
 *   <li><code>http://localhost/cgi-bin/WebObjects/MyApp.woa/wa/QueryArticle</code><br >
 * will create an query page for articles.
 *
 *   <li><code>http://localhost/cgi-bin/WebObjects/MyApp.woa/wa/QueryArticle?__fs=findNewArticles</code><br >
 * will create an query page for fetch spec "findNewArticles". This will only work if your rules return a ERD2WQueryPageWithFetchSpecification.
 *
 *   <li><code>http://localhost/cgi-bin/WebObjects/MyApp.woa/wa/InspectArticle?__key=&lt;articleid&gt;</code><br >
 * will create an inpect page for the given article.
 *
 *   <li><code>http://localhost/cgi-bin/WebObjects/MyApp.woa/wa/EditArticle?__key=&lt;articleid&gt;</code><br >
 * will create an edit page for the given article.
 *
 *   <li><code>http://localhost/cgi-bin/WebObjects/MyApp.woa/wa/CreateArticle</code><br >
 * will create an edit page for a newly created article.
 *
 *   <li><code>http://localhost/cgi-bin/WebObjects/MyApp.woa/wa/ListArticle?__key=&lt;userid&gt;&__keypath=User.articles</code><br >
 * will list the articles of the given user.
 *
 *   <li><code>http://localhost/cgi-bin/WebObjects/MyApp.woa/wa/ListArticle?__fs=recentArticles&authorName=*foo*</code><br >
 * will list the articles by calling the fetch spec "recentArticles". When the
 * fetch spec has an "authorName" binding, it is set to "*foo*".
 *
 *   <li><code>http://localhost/cgi-bin/WebObjects/MyApp.woa/wa/ListArticle?__fs=&author.name=*foo*&__fs_fetchLimit=0</code><br >
 * will list the articles by creating a fetch spec with the supplied attributes. 
 * When the value contains "*", then it will be regarded as a LIKE query, otherwise as a EQUAL
 * </ul>
 * To provide some security, you should override {@link #allowPageConfiguration(String)}. Also, this
 * class is abstract, so you need to subclass it.
 */

public abstract class ERD2WDirectAction extends ERXDirectAction {

    /** logging support */
    protected static final Logger log = Logger.getLogger(ERD2WDirectAction.class);
    protected final Logger actionLog = Logger.getLogger(ERD2WDirectAction.class.getName() + ".actions");

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

    /**
     * fetchLimit for the fetchSpec.
     */
    static final String fetchLimitKey = "__fs_fetchLimit";

    /**
     * fetchLimit for the fetchSpec.
     */
    static final String usesDistinctKey = "__fs_usesDistinct";

    /** denotes the context ID for the previous page */
    static final String contextIDKey = "__cid";

    
    static final String createPrefix = "Create";

    /** For edit pages, we always use a fresh editing context. */
    protected EOEditingContext newEditingContext() {
        return ERXEC.newEditingContext(session().defaultEditingContext().parentObjectStore());
    }

    /** Retrieves and executes the fetch specification given in the request. */
    public EOFetchSpecification fetchSpecificationFromRequest(String entityName) {
    	EOFetchSpecification fs = null;
    	if(context().request().formValueKeys().containsObject(fetchSpecificationKey)) {
    		String fsName = context().request().stringFormValueForKey(fetchSpecificationKey);
    		if(ERXStringUtilities.stringIsNullOrEmpty(fsName)) {
    			EOEntity rootEntity = ERXEOAccessUtilities.entityNamed(session().defaultEditingContext(), entityName);
    			
    			NSMutableArray qualifiers = new NSMutableArray();
    			for(Enumeration e = context().request().formValueKeys().objectEnumerator(); e.hasMoreElements(); ) {
    				String key = (String)e.nextElement();
    				EOEntity entity = rootEntity;
    				EOAttribute attribute = null;
    				String attributeName = key;
    				if(key.indexOf(".") > 0) {
    					String path = ERXStringUtilities.keyPathWithoutLastProperty(key);
    					attributeName = ERXStringUtilities.lastPropertyKeyInKeyPath(key);
    					entity = ERXEOAccessUtilities.destinationEntityForKeyPath(rootEntity, path);
    				}
    				if(entity != null) {
    					attribute = entity.attributeNamed(attributeName);
    					if(attribute != null) {
    						String stringValue = context().request().stringFormValueForKey(key);
    						if(stringValue != null) {
    							Object value;
    							if(attribute.adaptorValueType() == EOAttribute.AdaptorDateType) {
    								try {
    									value = (new NSTimestampFormatter()).parseObject(stringValue);
    								} catch (ParseException e1) {
										throw NSForwardException._runtimeExceptionForThrowable(e1);
									}
    							} else {
          							 value = attribute.newValueForString(stringValue);
    							}
    							NSSelector selector = EOKeyValueQualifier.QualifierOperatorEqual;
    							if(stringValue.indexOf('*') >= 0) {
    								selector = EOKeyValueQualifier.QualifierOperatorCaseInsensitiveLike;
    							}
    							qualifiers.addObject(new EOKeyValueQualifier(key, selector, value));
    						}
    					}
    				}
    			}
    			EOQualifier qualifier = null;
    			if(qualifiers.count() > 0) {
    				qualifier = new EOAndQualifier(qualifiers);
    			}
    			fs = new EOFetchSpecification(entityName, qualifier, null);
    			
    			boolean usesDictinct = ERXValueUtilities.booleanValueWithDefault(context().request().stringFormValueForKey(usesDistinctKey), true);
    			fs.setUsesDistinct(usesDictinct);
    			
    			int limit = ERXValueUtilities.intValueWithDefault(context().request().stringFormValueForKey(fetchLimitKey), 200);
    			fs.setFetchLimit(limit);
    		} else {
    			fs = EOFetchSpecification.fetchSpecificationNamed(fsName, entityName);
    			NSMutableDictionary bindings = new NSMutableDictionary();
    			Enumeration e = fs.qualifier().bindingKeys().objectEnumerator();
    			while(e.hasMoreElements()) {
    				String key = (String)e.nextElement();
    				String formValue = context().request().stringFormValueForKey(key);
    				if(formValue != null) {
    					bindings.setObjectForKey(formValue, key);
    				}
    			}
    			
    			if(bindings.count() > 0) {
    				fs = fs.fetchSpecificationWithQualifierBindings(bindings);
    			}
    		}
    	}
    	return fs;
    }

    /** @deprecated use primaryKeyFromRequest(EOEditingContext ec, String entityName) */
    public Number primaryKeyFromRequest() {
        return context().request().numericFormValueForKey(primaryKeyKey, new NSNumberFormatter("#"));
    }

    public NSDictionary primaryKeyFromRequest(EOEditingContext ec, String entityName) {
        String pkString = context().request().stringFormValueForKey(primaryKeyKey);
        return ERXEOControlUtilities.primaryKeyDictionaryForString(ec, entityName, pkString);
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
                EOEnterpriseObject eo = EOUtilities.objectWithPrimaryKey(ec, entityName, primaryKeyFromRequest(ec, entityName));
                EOArrayDataSource ds = new EOArrayDataSource(cd, ec);
                ds.setArray((NSArray)eo.valueForKeyPath(relationshipPath));
                return ds;
            }
        }
        return null;
    }

    protected void prepareEditPage(D2WContext context, EditPageInterface epi, String entityName) {
        EOEditingContext ec = newEditingContext();
        EOEnterpriseObject eo = null;

        ec.lock();
        try {
            if(context.dynamicPage().startsWith(createPrefix) || primaryKeyFromRequest(ec, entityName) == null) {
                eo = EOUtilities.createAndInsertInstance(ec,entityName);
            } else {
                eo = ERXEOControlUtilities.objectWithPrimaryKeyValue(ec, entityName, primaryKeyFromRequest(ec, entityName), null);
            }
        } finally {
            ec.unlock();
        }
        epi.setObject(eo);
        epi.setNextPage(previousPageFromRequest());
    }

    protected void prepareInspectPage(D2WContext context, InspectPageInterface ipi, String entityName) {
        EOEditingContext ec = session().defaultEditingContext();
        EOEnterpriseObject eo = null;

        ec.lock();
        try {
            eo = EOUtilities.objectWithPrimaryKey(ec, entityName, primaryKeyFromRequest(ec, entityName));
        } finally {
            ec.unlock();
        }
        ipi.setObject(eo);
        ipi.setNextPage(previousPageFromRequest());
    }

    protected void prepareQueryPage(D2WContext context, QueryPageInterface qpi, String entityName) {
        EOFetchSpecification fs = fetchSpecificationFromRequest(entityName);
        if(qpi instanceof ERD2WQueryPage) {
            if(fs != null)
                ((ERD2WQueryPage)qpi).setFetchSpecification(fs);
        }
    }

    protected void prepareListPage(D2WContext context, ListPageInterface lpi, String entityName) {
        EOEditingContext ec = session().defaultEditingContext();
        //ak: this check could be better...but anyway, as we edit, we should get a peer context
        if(lpi instanceof ERD2WEditableListPage) {
            ec = ERXEC.newEditingContext(session().defaultEditingContext().parentObjectStore());
        }
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
        EODataSource ds = relationshipArrayFromRequest(ec, entity.classDescriptionForInstances());
        if(ds == null) {
            ds = new EODatabaseDataSource(ec, entityName);
            EOFetchSpecification fs = fetchSpecificationFromRequest(entityName);
            if(fs == null) {
                fs = new EOFetchSpecification(entityName, null, null);
            }
            int fetchLimit = ERXValueUtilities.intValueWithDefault(context.valueForKey("fetchLimit"), 200);
            fs.setFetchLimit(fetchLimit);
            boolean refresh = ERXValueUtilities.booleanValueWithDefault(context.valueForKey("refreshRefetchedObjects"), false);
            fs.setRefreshesRefetchedObjects(refresh);
            ((EODatabaseDataSource)ds).setFetchSpecification(fs);
        }
        lpi.setDataSource(ds);
        lpi.setNextPage(previousPageFromRequest());
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

        D2WContext context = null; 
        if(newPage instanceof D2WPage) {
            context = ((D2WPage)newPage).d2wContext();
        } else {
            context = ERD2WContext.newContext(session());
            context.setDynamicPage(anActionName);
        }
        EOEntity entity = (EOEntity)context.entity();
        
        if(entity != null) {
            String entityName = entity.name();
            String taskName = (String)context.task();

            if(newPage instanceof EditPageInterface && taskName.equals("edit")) {
                prepareEditPage(context, (EditPageInterface)newPage, entityName);
            } else if(newPage instanceof InspectPageInterface) {
                prepareInspectPage(context, (InspectPageInterface)newPage, entityName);
            } else if(newPage instanceof QueryPageInterface) {
                prepareQueryPage(context, (QueryPageInterface)newPage, entityName);
            } else if(newPage instanceof ListPageInterface) {
                prepareListPage(context, (ListPageInterface)newPage, entityName);
            }
        }
        return (WOActionResults)newPage;
    }

    public WOActionResults reportException(Exception ex) {
        WOActionResults newPage = null;
        try {
            ErrorPageInterface epf=D2W.factory().errorPage(session());
            if(epf instanceof ERDErrorPageInterface) {
            	((ERDErrorPageInterface)epf).setException(ex);
            }
            epf.setMessage(ex.toString());
            epf.setNextPage(previousPageFromRequest());
            newPage = (WOActionResults)epf;
        } catch (Exception otherException) {
            log.error("Exception while trying to report exception!", otherException);
        }
        return newPage;
    }
    
    /**
     * Checks if a page configuration is allowed to render.
     * Override for a more intelligent access scheme as the default just returns true.
     * @param pageConfiguration
     * @return
     */
    protected boolean allowPageConfiguration(String pageConfiguration) {
        return true;
    }
    
    /**
     * Returns a response with a 401 (access denied) message. Override this for something more user friendly.
     * @return
     */
    public WOActionResults forbiddenAction() {
        WOResponse response = new WOResponse();
        response.setStatus(401);
        response.setContent("Access denied");
        return response;
    }
    
    /**
     * Overrides the default implementation to try to look up the action as a
     * page configuration if there is no method with the wanted name. This
     * implementation catches NoSuchMethodException more or less silently, so be
     * sure to turn on logging.
     */
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
            if(newPage == null) {
                if(allowPageConfiguration(anActionName)) {
                    newPage = dynamicPageForActionNamed(anActionName);
                } else {
                    newPage = forbiddenAction();
                }
            }
        } catch(Exception ex) {
            log.error("Error with action " + anActionName + ":" + ex + ", formValues:" + context().request().formValues(), ex);
            newPage = reportException(ex);
        }
        return newPage;
    }
}
