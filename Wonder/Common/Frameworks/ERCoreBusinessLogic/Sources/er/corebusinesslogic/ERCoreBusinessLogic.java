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

public class ERCoreBusinessLogic extends ERXFrameworkPrincipal {
    
    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERCoreBusinessLogic.class);

    static {
        setUpFrameworkPrincipalClass(ERCoreBusinessLogic.class);
    }

    static ERCoreBusinessLogic sharedInstance;
    public static ERCoreBusinessLogic sharedInstance() {
        if(sharedInstance == null) {
            sharedInstance = (ERCoreBusinessLogic)ERXFrameworkPrincipal.sharedInstance(ERCoreBusinessLogic.class);
        }
        return sharedInstance;
    }

    public void finishInitialization() {
        initializeSharedData();
        // Register handlers for user preferences.
        ERCoreUserPreferences.userPreferences().registerHandlers();
        log.debug("ERCoreBusinessLogic: finishInitialization");
    }

    // Shared Data Init Point.  Keep alphabetical
    public void initializeSharedData() {
        ERCMailState.mailStateClazz().initializeSharedData();
        //MailMessage.initializeSharedData();
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
    }

    private static EOEnterpriseObject _contactInfo;
    public static EOEnterpriseObject contactInfo() {return _contactInfo;}
    public static void setContactInfo(EOEnterpriseObject info) {
        log.debug("setContactInfo: Registered "+info);
        _contactInfo=info;
    }

    private static NSArray _emailsForProblemRecipients;
    public static NSArray emailsForProblemRecipients() {
        return _emailsForProblemRecipients;
    }
    public static void setEmailsForProblemRecipients(NSArray a) {
        log.debug("setEmailsForProblemRecipients: Registered "+a);
        _emailsForProblemRecipients=a;
    }
    private static String _originatorForProblemEmails;
    public static String originatorForProblemEmails() {
        return _originatorForProblemEmails;
    }
    public static void setOriginatorForProblemEmails(String email) {
        log.debug("setOriginatorForProblemEmails: Registered "+email);
        _originatorForProblemEmails=email;
    }

    //------------------------------------------------------------------

    public static String staticStoredValueForKey(String key) {
        return ERCStatic.staticClazz().staticStoredValueForKey(key);
    }
    public static int staticStoredIntValueForKey(String key) {
        return ERCStatic.staticClazz().staticStoredIntValueForKey(key);
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

    public static void invalidateStaticValueForKeyCache() {
        ERCStatic.staticClazz().invalidateCache();
    }
    

    public static void setActor(EOEnterpriseObject actor) {
        if (log.isDebugEnabled())
            log.debug("Setting actor to : "+actor);
        if (actor!=null)
            ERXThreadStorage.takeValueForKey(actor, "actor");
        else
            ERXThreadStorage.removeValueForKey("actor");
    }
    
    public static EOEnterpriseObject actor(EOEditingContext ec) {
        EOEnterpriseObject result = actor();
        if (result != null && result.editingContext() != ec)
            result = (EOEnterpriseObject)ERXUtilities.localInstanceOfObject(ec,result);
        return result;
    }
    
    public static EOEnterpriseObject actor() {
         return (EOEnterpriseObject) ERXThreadStorage.valueForKey( "actor");
    }

    // Logging support
    public static ERCLogEntry createLogEntryLinkedToEO(EOEnterpriseObject type,
                                                       String text,
                                                       EOEnterpriseObject eo,
                                                       String relationshipKey) {
        EOEditingContext editingContext=eo.editingContext();
        ERCLogEntry logEntry = (ERCLogEntry)ERCLogEntry.logEntryClazz().createAndInsertObject(editingContext);
        if(type != null) {
            // CHECKME: (ak) what's type supposed to do??
            // logEntry.addObjectToBothSidesOfRelationshipWithKey(type,"type");
        }
        if(relationshipKey != null) {
            // CHECKME: (ak) what's relationshipKey supposed to do??
            // logEntry.addObjectToBothSidesOfRelationshipWithKey(eo,relationshipKey);
        }
        logEntry.setText(text);
        return logEntry;
    }
    
    protected static String _defaultHostName;
    public static String defaultHostName(){
        if (_defaultHostName == null) {
            try {
                _defaultHostName = java.net.InetAddress.getLocalHost().getHostName();
            } catch (java.net.UnknownHostException ehe) {
                WOApplication.application().logString("Caught unknown host: " + ehe.getMessage());
                _defaultHostName = "UnknownHost";
            }
        }
        return _defaultHostName;
    }

    // ----------------------------------------------------------------------------------------
    // Exception reporting
    public static void reportException(Throwable exception, NSDictionary extraInfo) {
        StringBuffer s = new StringBuffer();
        try {
            s.append("    **** Caught: "+exception + "\n");
            s.append("         Actor: "+actor() + "\n");
            s.append("         Extra Information "+extraInfo + "\n");
            if (exception instanceof EOGeneralAdaptorException) {
                EOGeneralAdaptorException  e=(EOGeneralAdaptorException)exception;
                if (e.userInfo()!=null) {
                    Object userInfo=e.userInfo();
                    if (userInfo instanceof NSDictionary) {
                        NSDictionary uid=(NSDictionary)userInfo;
                        for (Enumeration e2=uid.keyEnumerator(); e2.hasMoreElements();) {
                            String key=(String)e2.nextElement();
                            Object value=uid.objectForKey(key);
                            s.append(key + " = " + value + ";\n");
                        }
                    } else
                        s.append(e.userInfo().toString());
                    log.warn(s.toString());
                }
            } else {
                exception.printStackTrace();
            }
            if (WOApplication.application().isCachingEnabled()) {// we take this to mean we are in deployment
                ERCMailableExceptionPage standardExceptionPage=(ERCMailableExceptionPage)WOApplication.application().pageWithName("ERCMailableExceptionPage",new WOContext(new WORequest(null, null, null, null, null, null)));
                standardExceptionPage.setException(exception);
                standardExceptionPage.setActor(actor());
                standardExceptionPage.setExtraInfo(extraInfo);

                EOEditingContext ec = ERXExtensions.newEditingContext();
                NSData d = standardExceptionPage.generateResponse().content();
                String exceptionString = new String(d.bytes(0, d.length())); // FIXME inefficient?
                String exceptionName = exception.getClass().getName();
                String hostName = java.net.InetAddress.getLocalHost().getHostName();

                ERCMailDelivery.sharedInstance().composeEmail(WOApplication.application().name()+"-"+hostName+"@netstruxr.com",
                                                              (NSArray)emailsForProblemRecipients(),
                                                              null,
                                                              null,
                                                              exceptionName+" in "+WOApplication.application().name(),
                                                              exceptionString,
                                                              ec);
                ec.saveChanges();
            }
        } catch (Throwable u) {
            try {
                s.append("************ Caught exception "+u+" trying to report another one: "+exception);
                s.append("** Original exception ");
                s.append(ERXUtilities.stackTrace(exception));
                s.append("** Second exception ");
                s.append(ERXUtilities.stackTrace(u));
                WOApplication.application().logString(s.toString());
            } catch (Throwable u2) {} // WE DON'T WANT ANYTHING TO GO WRONG IN HERE as it would cause the app to instantly exit
        }
    }
}
