//
// ERD2WDirectAction.java: Class file for WO Component 'ERD2WDirectAction'
// Project ERDirectToWeb
//
// Created by ak on Mon Apr 22 2002
//
package er.directtoweb;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.EditRelationshipPageInterface;
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
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNumberFormatter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestampFormatter;

import er.directtoweb.interfaces.ERDErrorPageInterface;
import er.directtoweb.pages.ERD2WEditableListPage;
import er.directtoweb.pages.ERD2WQueryPage;
import er.extensions.appserver.ERXDirectAction;
import er.extensions.appserver.ERXHttpStatusCodes;
import er.extensions.appserver.ERXResponse;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Automatically creates page configurations from URLs.
 * <h3>Examples:</h3>
 * <ul>
 *   <li><code>http://localhost/cgi-bin/WebObjects/MyApp.woa/wa/QueryAll</code><br >
 * will create an query page all entities.
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
 *   <li><code>http://localhost/cgi-bin/WebObjects/MyApp.woa/wa/ListArticle?__key=&lt;userid&gt;&amp;__keypath=User.articles</code><br >
 * will list the articles of the given user.
 *
 *   <li><code>http://localhost/cgi-bin/WebObjects/MyApp.woa/wa/ListArticle?__fs=recentArticles&amp;authorName=*foo*</code><br >
 * will list the articles by calling the fetch spec "recentArticles". When the
 * fetch spec has an "authorName" binding, it is set to "*foo*".
 *
 *   <li><code>http://localhost/cgi-bin/WebObjects/MyApp.woa/wa/ListArticle?__fs=&amp;author.name=*foo*&amp;__fs_fetchLimit=0</code><br >
 * will list the articles by creating a fetch spec with the supplied attributes. 
 * When the value contains "*", then it will be regarded as a LIKE query, otherwise as a EQUAL
 *   <li><code>http://localhost/cgi-bin/WebObjects/MyApp.woa/wa/ErrorSomeStuff?__message=Some+Test</code><br >
 * will create an error page with the title "Error Some Stuff" (or whatever your localizer does with it) and the message "Some Test".
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
    public static final String primaryKeyKey = "__key";

    /**
     * keyPathKey is used to get relationships of a given object.
     */
    public static final String keyPathKey = "__keypath";

    /**
     * fetchSpecificationKey is used to get the named fetchspec of a given object.
     */
    public static final String fetchSpecificationKey = "__fs";

    /**
     * fetchLimit for the fetchSpec.
     */
    public static final String fetchLimitKey = "__fs_fetchLimit";

    /**
     * fetchLimit for the fetchSpec.
     */
    public static final String usesDistinctKey = "__fs_usesDistinct";

    /** denotes the context ID for the previous page */
    public static final String contextIDKey = "__cid";

    
    public static final String createPrefix = "Create";

    /** For edit pages, we always use a fresh editing context. */
    protected EOEditingContext newEditingContext() {
        return ERXEC.newEditingContext(session().defaultEditingContext().parentObjectStore());
    }
    
    /**
     * Overwrite for custom value conversion.
     * @param attribute
     * @param stringValue
     */
    protected Object qualifierValueForAttribute(EOAttribute attribute, String stringValue) {
        //AK: I still don't like this...in particular the new NSTimestampFormatter() which would be totally arbitrary...
        return ERXStringUtilities.attributeValueFromString(attribute, stringValue, context().request().formValueEncoding(), new NSTimestampFormatter());
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
    							Object value = null;
    							NSSelector selector = EOKeyValueQualifier.QualifierOperatorEqual;
    							if(stringValue.indexOf('*') >= 0) {
    								selector = EOKeyValueQualifier.QualifierOperatorCaseInsensitiveLike;
    							}
    							if(!NSKeyValueCoding.NullValue.toString().equals(stringValue)) {
                                    //AK: I still don't like this...in particular the new NSTimestampFormatter() which would be totally arbitrary...
    								value = qualifierValueForAttribute(attribute, stringValue);
    								if(value!=null) {
        								qualifiers.addObject(new EOKeyValueQualifier(key, selector, value));
    								}
    							} else {
    								qualifiers.addObject(new EOKeyValueQualifier(key, selector, value));
    							}
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

    public NSDictionary primaryKeyFromRequest(EOEditingContext ec, String entityName) {
        String pkString = context().request().stringFormValueForKey(primaryKeyKey);
        return ERXEOControlUtilities.primaryKeyDictionaryForString(ec, entityName, pkString);
    }

    public WOComponent previousPageFromRequest() {
        String cid = context().request().stringFormValueForKey(contextIDKey);
        if (cid == null) return context().page();
        return session().restorePageForContextID(cid);
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
    
    protected void prepareEditRelationshipPage(D2WContext context, EditRelationshipPageInterface erpi, String entityName) {
    	EOEditingContext ec = ERXEC.newEditingContext(session().defaultEditingContext().parentObjectStore());
    	String keypath = keyPathFromRequest();
    	String masterEntityName = ERXStringUtilities.firstPropertyKeyInKeyPath(keypath);
    	String relationshipKey = ERXStringUtilities.keyPathWithoutFirstProperty(keypath);
    	NSDictionary pk = primaryKeyFromRequest(ec, masterEntityName);
    	EOEnterpriseObject masterObject = ERXEOControlUtilities.objectWithPrimaryKeyValue(ec, masterEntityName, pk, null);
    	erpi.setMasterObjectAndRelationshipKey(masterObject, relationshipKey);
    	erpi.setNextPage(previousPageFromRequest());
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
            if(!context().request().formValueKeys().contains(fetchLimitKey)) {
                int fetchLimit = ERXValueUtilities.intValueWithDefault(context.valueForKey("fetchLimit"), 200);
                fs.setFetchLimit(fetchLimit);
            }
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
        EOEntity entity = context.entity();

        if(entity != null) {
            String entityName = entity.name();
            String taskName = context.task();

            if(newPage instanceof EditPageInterface && taskName.equals("edit")) {
                prepareEditPage(context, (EditPageInterface)newPage, entityName);
            } else if(newPage instanceof InspectPageInterface) {
                prepareInspectPage(context, (InspectPageInterface)newPage, entityName);
            } else if(newPage instanceof QueryPageInterface) {
                prepareQueryPage(context, (QueryPageInterface)newPage, entityName);
            } else if(newPage instanceof EditRelationshipPageInterface) {
            	prepareEditRelationshipPage(context, (EditRelationshipPageInterface)newPage, entityName);
            } else if(newPage instanceof ListPageInterface) {
                prepareListPage(context, (ListPageInterface)newPage, entityName);
            } else if(newPage instanceof ErrorPageInterface) {
                prepareErrorPage(context, (ErrorPageInterface)newPage);
            }
        } else if(newPage instanceof ErrorPageInterface) {
            prepareErrorPage(context, (ErrorPageInterface)newPage);
        }
        return newPage;
    }

    /**
     * Returns an error page and sets the message to the key<code> __message</code>.
     */
    protected WOActionResults prepareErrorPage(D2WContext d2wContext, ErrorPageInterface epi) {
        WOActionResults newPage = null;
        try {
            String message = context().request().stringFormValueForKey("__message");
            // AK: actually, this isn't enough to prevent hacks, as you might also be able 
            // to social-engineer your way around. We should simply use a key into the localizer.
            if(message != null) {
                message = message.replaceAll("<.*?>", "");
            }
            epi.setMessage(message);
            epi.setNextPage(previousPageFromRequest());
            newPage = (WOActionResults)epi;
        } catch (Exception otherException) {
            log.error("Exception while trying to report exception!", otherException);
        }
        return newPage;
    }
    
    /**
     * Creates an error page with the given exception.
     * @param ex
     */
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
     */
    protected boolean allowPageConfiguration(String pageConfiguration) {
        return true;
    }
    
    /**
     * Returns a response with a 401 (access denied) message. Override this for something more user friendly.
     */
    public WOActionResults forbiddenAction() {
    	return new ERXResponse("Access denied", ERXHttpStatusCodes.UNAUTHORIZED);
    }
    
    /**
     * Overrides the default implementation to try to look up the action as a
     * page configuration if there is no method with the wanted name. This
     * implementation catches NoSuchMethodException more or less silently, so be
     * sure to turn on logging.
     */
    @Override
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
