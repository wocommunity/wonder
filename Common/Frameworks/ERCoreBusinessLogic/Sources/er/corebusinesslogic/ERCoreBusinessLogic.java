/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import er.extensions.*;
import java.util.*;
import java.lang.reflect.*;

public class ERCoreBusinessLogic extends ERXFrameworkPrincipal {

    //	===========================================================================
    //	Class constant(s)
    //	---------------------------------------------------------------------------    
    
    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERCoreBusinessLogic.class);
    
    /** property key that holds the email domain of the generated from email */
    public static final String ProblemEmailDomainPropertyKey = "er.corebusinesslogic.ERCoreBusinessLogic.ProblemEmailDomain";

    /** property key that holds the emails of those who recieve problem emails */
    public static final String ProblemEmailRecipientsPropertyKey = "er.corebusinesslogic.ERCoreBusinessLogic.ProblemEmailRecipients";

    //	===========================================================================
    //	Class variable(s)
    //	---------------------------------------------------------------------------    
    
    /** holds the shared instance reference */
    protected static ERCoreBusinessLogic sharedInstance;

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
            sharedInstance = (ERCoreBusinessLogic)ERXFrameworkPrincipal.sharedInstance(ERCoreBusinessLogic.class);
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
        if (actor!=null)
            ERXThreadStorage.takeValueForKey(actor, "actor");
        else
            ERXThreadStorage.removeValueForKey("actor");
    }

    /**
     * Gets the actor as a local instance in the given context.
     * @param ec editing context to pull a local copy of the actor
     *		into
     * @return actor instance in the given editing context
     */
    public static EOEnterpriseObject actor(EOEditingContext ec) {
        EOEnterpriseObject result = actor();
        if (result != null && result.editingContext() != ec)
            result = (EOEnterpriseObject)ERXEOControlUtilities.localInstanceOfObject(ec,result);
        return result;
    }

    /**
     * Gets the actor.
     * @return current actor for the thread
     */
    public static EOEnterpriseObject actor() {
        return (EOEnterpriseObject) ERXThreadStorage.valueForKey( "actor");
    }

    public static String staticStoredValueForKey(String key) {
        return ERCStatic.staticClazz().staticStoredValueForKey(key);
    }
    public static int staticStoredIntValueForKey(String key) {
        return ERCStatic.staticClazz().staticStoredIntValueForKey(key);
    }

    public static String staticStoredValueForKey(String key, boolean noCache) {
        return ERCStatic.staticClazz().staticStoredValueForKey(key, noCache);
    }
    
    public static int staticStoredIntValueForKey(String key, boolean noCache) {
        return ERCStatic.staticClazz().staticStoredIntValueForKey(key, noCache);
    }    

    public static String staticStoredValueForKey(String key, EOEditingContext ec) {
        return ERCStatic.staticClazz().staticStoredValueForKey(ec, key);
    }
    public static int staticStoredIntValueForKey(String key, EOEditingContext ec) {
        return ERCStatic.staticClazz().staticStoredIntValueForKey(ec, key);
    }

    public static void takeStaticStoredValueForKey(String value, String key, EOEditingContext editingContext) {
        ERCStatic.staticClazz().takeStaticStoredValueForKey(editingContext, value, key);
    }

    public static void takeStaticStoredValueForKey(String value, String key) {
        ERCStatic.staticClazz().takeStaticStoredValueForKey(value, key);
    }    

    public static void invalidateStaticValueForKeyCache() {
        ERCStatic.staticClazz().invalidateCache();
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
    public void finishInitialization() {
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
        ERCMailState.mailStateClazz().initializeSharedData();
    }

    public boolean shouldMailReportedExceptions() {
        return ERXProperties.booleanForKey("er.corebusinesslogic.ERCoreBusinessLogic.ShouldMailExceptions");
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
        StringBuffer s = new StringBuffer();
        try {
            s.append(" **** Caught: "+exception + "\n");
            s.append("      Actor: " + (actor() != null ? actor().toString() : "No Actor") + "\n");
            if (extraInfo != null && extraInfo.count() > 0) {
                s.append("         Extra Information: \n");
                for (Enumeration keyEnumerator = extraInfo.keyEnumerator(); keyEnumerator.hasMoreElements();) {
                    String key = (String)keyEnumerator.nextElement();
                    s.append("         " + key + " = " + extraInfo.objectForKey(key) + "\n");
                }
            }
            
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

                    EOEditingContext ec = ERXExtensions.newEditingContext();
                    try {
                        ec.lock();
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
}
