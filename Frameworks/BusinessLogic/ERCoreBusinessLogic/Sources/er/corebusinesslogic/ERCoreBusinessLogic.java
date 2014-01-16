/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSLog;

import er.corebusinesslogic.audittrail.ERCAuditTrailHandler;
import er.directtoweb.ERDirectToWeb;
import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;
import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXConfigurationManager;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXThreadStorage;
import er.extensions.foundation.ERXUtilities;
import er.extensions.jdbc.ERXJDBCUtilities;
import er.extensions.jdbc.ERXSQLHelper;
import er.javamail.ERJavaMail;

/**
 *
 * @property er.corebusinesslogic.ERCoreBusinessLogic.ProblemEmailDomain
 * @property er.corebusinesslogic.ERCoreBusinessLogic.ProblemEmailRecipients
 * @property er.corebusinesslogic.ERCoreBusinessLogic.ShouldMailExceptions
 */
public class ERCoreBusinessLogic extends ERXFrameworkPrincipal {

    //	===========================================================================
    //	Class constant(s)
    //	---------------------------------------------------------------------------    
    
    /** logging support */
    public static final Logger log = Logger.getLogger(ERCoreBusinessLogic.class);
    
    /** property key that holds the email domain of the generated from email */
    public static final String ProblemEmailDomainPropertyKey = "er.corebusinesslogic.ERCoreBusinessLogic.ProblemEmailDomain";

    /** property key that holds the emails of those who recieve problem emails */
    public static final String ProblemEmailRecipientsPropertyKey = "er.corebusinesslogic.ERCoreBusinessLogic.ProblemEmailRecipients";

    //	===========================================================================
    //	Class variable(s)
    //	---------------------------------------------------------------------------    
    
    /** holds the shared instance reference */
    protected static ERCoreBusinessLogic sharedInstance;
    
    public final static Class REQUIRES[] = new Class[] {ERXExtensions.class, ERDirectToWeb.class, ERJavaMail.class};

    /**
     * Registers the class as the framework principal
     */
    static {
        setUpFrameworkPrincipalClass(ERCoreBusinessLogic.class);
    }

    //	===========================================================================
    //	Class method(s)
    //	---------------------------------------------------------------------------    
    
    /**
     * Gets the shared instance of the ERCoreBusinessLogic.
     * @return shared instance.
     */
    public static ERCoreBusinessLogic sharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = ERXFrameworkPrincipal.sharedInstance(ERCoreBusinessLogic.class);
        }
        return sharedInstance;
    }

    /**
     * Sets the actor in the current thread storage.
     * @param actor current user for this thread
     */
    public static void setActor(EOEnterpriseObject actor) {
        if (log.isDebugEnabled())
            log.debug("Setting actor to : "+actor);
        if (actor != null) {
            ERXThreadStorage.takeValueForKey(actor, "actor");
        } else {
            ERXThreadStorage.removeValueForKey("actor");
        }
    }

    /**
     * Gets the actor as a local instance in the given context.
     * @param ec editing context to pull a local copy of the actor
     *		into
     * @return actor instance in the given editing context
     */
    public static EOEnterpriseObject actor(EOEditingContext ec) {
        EOEnterpriseObject actor = actor();
        if (actor != null && actor.editingContext() != ec) {
            EOEditingContext actorEc = actor.editingContext();
            actorEc.lock();
            try {
            	EOEnterpriseObject localActor = ERXEOControlUtilities.localInstanceOfObject(ec, actor);
            	try {
            		if(actor instanceof ERCoreUserInterface) {
            			NSArray prefs = ((ERCoreUserInterface)actor).preferences();
            			prefs = ERXEOControlUtilities.localInstancesOfObjects(ec, prefs);
            			((ERCoreUserInterface)localActor).setPreferences(prefs);
            		}
                } catch(RuntimeException ex) {
                	log.error("Error while setting getting actor's preferences: " + ex, ex);
            	}
        		actor = localActor;
            } finally {
                actorEc.unlock();                
            }
        }
        return actor;
    }

    /**
     * Gets the actor.
     * @return current actor for the thread
     */
    public static EOEnterpriseObject actor() {
        return (EOEnterpriseObject)ERXThreadStorage.valueForKey( "actor");
    }

    public static String staticStoredValueForKey(String key) {
        return ERCStatic.clazz.staticStoredValueForKey(key);
    }
    
    public static int staticStoredIntValueForKey(String key) {
        return ERCStatic.clazz.staticStoredIntValueForKey(key);
    }

    public static String staticStoredValueForKey(String key, boolean noCache) {
        return ERCStatic.clazz.staticStoredValueForKey(key, noCache);
    }
    
    public static int staticStoredIntValueForKey(String key, boolean noCache) {
        return ERCStatic.clazz.staticStoredIntValueForKey(key, noCache);
    }    

    public static String staticStoredValueForKey(String key, EOEditingContext ec) {
        return ERCStatic.clazz.staticStoredValueForKey(ec, key);
    }
    public static int staticStoredIntValueForKey(String key, EOEditingContext ec) {
        return ERCStatic.clazz.staticStoredIntValueForKey(ec, key);
    }

    public static void takeStaticStoredValueForKey(String value, String key, EOEditingContext editingContext) {
        ERCStatic.clazz.takeStaticStoredValueForKey(editingContext, value, key);
    }

    public static void takeStaticStoredValueForKey(String value, String key) {
        ERCStatic.clazz.takeStaticStoredValueForKey(value, key);
    }    

    public static void invalidateStaticValueForKeyCache() {
        ERCStatic.clazz.invalidateCache();
    }

    //	===========================================================================
    //	Instance variable(s)
    //	---------------------------------------------------------------------------

    /** caches the email addresses to send to in the case of problems */
    protected NSArray _emailsForProblemRecipients;

    /** caches the problem email address domain */
    protected String _problemEmailDomain;
    
    //	===========================================================================
    //	Instance method(s)
    //	---------------------------------------------------------------------------    
    
    /**
     * Called when it is time to finish the
     * initialization of the framework.
     */
    @Override
    public void finishInitialization() {
        ERCAuditTrailHandler.initialize();
        ERCStampedEnterpriseObject.initialize();
        // Initialized shared data
        initializeSharedData();
        // Register handlers for user preferences.
        ERCoreUserPreferences.userPreferences().registerHandlers();
        log.debug("ERCoreBusinessLogic: finishInitialization");
    }

    /**
     * Initializes the shared eof data.
     */
    public void initializeSharedData() {
        // Shared Data Init Point.  Keep alphabetical
        Class c = ERCMailState.class;
    }

    public boolean shouldMailReportedExceptions() {
        return ERXProperties.booleanForKey("er.corebusinesslogic.ERCoreBusinessLogic.ShouldMailExceptions");
    }
    
    public void addPreferenceRelationshipToActorEntity(String entityName) {
        EOEntity entity  = EOModelGroup.defaultGroup().entityNamed(entityName);
        if(entity != null && entity.primaryKeyAttributeNames().count() == 1) {
            addPreferenceRelationshipToActorEntity(entityName, entity.primaryKeyAttributeNames().lastObject());
        } else {
            throw new IllegalArgumentException("Entity is not suitable: " + entityName);
        }
    }
    
    /**
     * Registers a run-time relationship called "preferences" on the actor 
     * entity of your business logic. The framework needs preferences 
     * relationship to access user preferences for a specific actor. 
     * Call this method when you initialize your business logic layer. 
     * (Check BTBusinessLogic class as an example.)
     * 
     * @param  entityName  String name for your actor entity
     * @param  attributeNameToJoin  String attribute name on the actor
     *         entity; used by the relationship and typically it's the 
     *         primary key. 
     */
    public void addPreferenceRelationshipToActorEntity(String entityName, String attributeNameToJoin) {
        EOEntity actor = EOModelGroup.defaultGroup().entityNamed(entityName);
        EOEntity preference = EOModelGroup.defaultGroup().entityNamed("ERCPreference");

        EOJoin preferencesJoin = new EOJoin(actor.attributeNamed(attributeNameToJoin), preference.attributeNamed("userID"));
        EORelationship preferencesRelationship = new EORelationship();

        preferencesRelationship.setName("preferences");
        actor.addRelationship(preferencesRelationship);
        preferencesRelationship.addJoin(preferencesJoin);
        preferencesRelationship.setToMany(true);
        preferencesRelationship.setJoinSemantic(EORelationship.InnerJoin);
        preferencesRelationship.setDeleteRule(EOEntityClassDescription.DeleteRuleCascade);
        
        EOJoin userJoin = new EOJoin(preference.attributeNamed("userID"), actor.attributeNamed(attributeNameToJoin) );
        EORelationship userRelationship = new EORelationship();
        userRelationship.setName("user");
        preference.addRelationship(userRelationship);
        userRelationship.addJoin(userJoin);
        userRelationship.setToMany(false);
        userRelationship.setJoinSemantic(EORelationship.InnerJoin);
    }

    /**
     * Gets the array of email addresses to send emails
     * about problems to.
     * @return array of email addresses
     */
    public NSArray emailsForProblemRecipients() {
        if (_emailsForProblemRecipients == null) {
            _emailsForProblemRecipients = ERXProperties.arrayForKeyWithDefault(ProblemEmailRecipientsPropertyKey,
                                                                               NSArray.EmptyArray);
        }
        return _emailsForProblemRecipients;
    }

    /**
     * Sets the emails for problem recipients. Should
     * be an array of email addresses to report exceptions
     * to in production applications.
     * @param a array of email addresses
     */
    public void setEmailsForProblemRecipients(NSArray a) {
        _emailsForProblemRecipients=a;
    }

    /**
     * Gets the problem email domain. This is used for constructing the
     * from address when reporting an exception. Should be of the form:
     * foo.com.
     * @return problem email address domain
     */
    public String problemEmailDomain() {
        if (_problemEmailDomain == null) {
            _problemEmailDomain = System.getProperty(ProblemEmailDomainPropertyKey);
        }
        return _problemEmailDomain;
    }

    /**
     * Sets the problem email domain.
     * @param value to set problem domain to
     */
    public void setProblemEmailDomain(String value) {
        _problemEmailDomain = value;
    }
    
    public synchronized String extraInfoString(NSDictionary extraInfo, int indent) {
		StringBuffer s = new StringBuffer();
		ERXStringUtilities.indent(s, indent);
	    s.append("Extra Information: \n");
		ERXStringUtilities.indent(s, indent);
	    s.append("    Actor = " + (actor() != null ? actor().toString() : "No Actor") + "\n");
	    if (extraInfo != null && extraInfo.count() > 0) {
	        for (Enumeration keyEnumerator = extraInfo.keyEnumerator(); keyEnumerator.hasMoreElements();) {
	            String key = (String)keyEnumerator.nextElement();
	            Object value = extraInfo.objectForKey(key);
	            if (value instanceof NSDictionary) {
	                String valueStr = String.valueOf(value);
	                StringBuffer valueIndent = new StringBuffer();
	                valueIndent.append("\n         ");
	            	ERXStringUtilities.indent(valueIndent, indent);
	                for (int i = 0; i < key.length(); i ++) {
	                	valueIndent.append(' ');
	                }
	                value = valueStr.replaceAll("\n", valueIndent.toString());
	            }
	        	ERXStringUtilities.indent(s, indent);
	            s.append("    " + key + " = " + value + "\n");
	        }
	    }
	    return s.toString();
	}
    	

    /**
     * Reports an exception. If caching is enabled then the exception
     * will also be emailed to the problem mail recipients.
     * @param exception to be reported
     * @param extraInfo dictionary of extra information about what was
     *		happening when the exception was thrown.
     */
    public synchronized void reportException(Throwable exception, NSDictionary extraInfo) {
        if (exception instanceof NSForwardException) {
            exception = ((NSForwardException)exception).originalException();
        }
        StringBuilder s = new StringBuilder();
        try {
            s.append(" **** Caught: "+exception + "\n");
            s.append(extraInfoString(extraInfo, 3));
            
            if (exception instanceof EOGeneralAdaptorException) {
                EOGeneralAdaptorException  e= (EOGeneralAdaptorException)exception;
                if (e.userInfo()!=null) {
                    Object userInfo=e.userInfo();
                    if (userInfo instanceof NSDictionary) {
                        NSDictionary uid=(NSDictionary)userInfo;
                        for (Enumeration e2=uid.keyEnumerator(); e2.hasMoreElements();) {
                            String key=(String)e2.nextElement();
                            Object value=uid.objectForKey(key);
                            s.append(key + " = " + value + ";\n");
                        }
                    } else {
                        s.append(e.userInfo().toString());
                    }
                }
            } else {
                s.append(ERXUtilities.stackTrace(exception));
            }
            if (!WOApplication.application().isCachingEnabled() ||
                !ERCMailDelivery.usesMail() ||
                !shouldMailReportedExceptions()) {
                log.error(s.toString());
            } else {
                // Usually the Mail appender is set to Threshold ERROR
                log.warn(s.toString());
                if (emailsForProblemRecipients().count() == 0 || problemEmailDomain() == null) {
                    log.error("Unable to log problem due to misconfiguration: recipients: "
                              + emailsForProblemRecipients() + " email domain: " + problemEmailDomain());
                } else {
                    ERCMailableExceptionPage standardExceptionPage = (ERCMailableExceptionPage)ERXApplication.instantiatePage("ERCMailableExceptionPage");
                    standardExceptionPage.setException(exception);
                    standardExceptionPage.setActor(actor());
                    standardExceptionPage.setExtraInfo(extraInfo);

                    EOEditingContext ec = ERXEC.newEditingContext();
                    ec.lock();
                    try {
                        String shortExceptionName;
                        Throwable exceptionForTitle = exception;
                        if (exception instanceof InvocationTargetException) {
                            exceptionForTitle = ((InvocationTargetException)exception).getTargetException();
                        }
                        shortExceptionName = ERXStringUtilities.lastPropertyKeyInKeyPath(exceptionForTitle.getClass().getName());

                        String hostName = ERXConfigurationManager.defaultManager().hostName();

                        ERCMailDelivery.sharedInstance().composeEmail(WOApplication.application().name()+"-"+hostName+"@"+problemEmailDomain(),
                                                                      emailsForProblemRecipients(),
                                                                      null,
                                                                      null,
                                                                      WOApplication.application().name() + ": " + shortExceptionName
                                                                      + ": " + exceptionForTitle.getMessage(),
                                                                      standardExceptionPage.generateResponse().contentString(),
                                                                      ec);
                        ec.saveChanges();
                    } finally {
                        ec.unlock();
                    }
                    ec.dispose();
                }
            }
        } catch (Throwable u) {
            try {
                s.append("************ Caught exception "+u+" trying to report another one: "+exception);
                s.append("** Original exception ");
                s.append(ERXUtilities.stackTrace(exception));
                s.append("** Second exception ");
                s.append(ERXUtilities.stackTrace(u));
                NSLog.err.appendln(s.toString());
                log.error(s.toString());
            } catch (Throwable u2) {} // WE DON'T WANT ANYTHING TO GO WRONG IN HERE as it would cause the app to instantly exit
        }
    }

    public void createTables(EOEditingContext ec, String modelName) throws SQLException {
        // AK: FIXME we should try with the DROP options enabled and re-try with the options disabled
        // so we catch the case when we 
        EODatabaseContext databaseContext = EOUtilities.databaseContextForModelNamed(ec, modelName);
        ERXSQLHelper helper = ERXSQLHelper.newSQLHelper(databaseContext);
        String sql = helper.createSchemaSQLForEntitiesInModelWithName(null, modelName);
        NSArray sqls = helper.splitSQLStatements(sql);
        EOAdaptorChannel channel = databaseContext.availableChannel().adaptorChannel();
        ERXJDBCUtilities.executeUpdateScript(channel, sql);
    }

    public void createTables(EOEditingContext ec) throws SQLException {
        createTables(ec, "ERMail");
        createTables(ec, "ERCoreBusinessLogic");
    }
}
