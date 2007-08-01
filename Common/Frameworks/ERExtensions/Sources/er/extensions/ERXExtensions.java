/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eoaccess.EOQualifierSQLGeneration.Support;
import com.webobjects.appserver.*;
import com.webobjects.jdbcadaptor.JDBCAdaptorException;
import java.lang.reflect.Method;
import java.lang.*;
import java.util.*;
import java.io.*;
import org.apache.log4j.Logger;

/**
 * Principal class of the ERExtensions framework. This class
 * will be loaded at runtime when the ERExtensions bundle is
 * loaded (even before the Application construcor is called)
 * This class has a boat-load of stuff in it that will hopefully
 * be finding better homes in the future. This class serves as
 * the initilization point of this framework, look in the static
 * initializer to see all the stuff that is initially setup when
 * this class is loaded. This class also has a boat load of
 * string, array and eof utilities as well as the factory methods
 * for creating editing contexts with the default delegates set.
 */
public class ERXExtensions {
    public static Observer observer;
    
    /** Notification name, posted before object will change in an editing context */
    public final static String objectsWillChangeInEditingContext= "ObjectsWillChangeInEditingContext";
    
    /** logging support */
    private static Logger _log;

    /**
     * creates and caches the logging logger
     * @return logging logger
     */
    public static Logger log() {
        if (_log == null)
            _log = Logger.getLogger(ERXExtensions.class);
        return _log;
    }

    /** holds references to default editing context delegate */
    //private static ERXEditingContextDelegate _defaultEditingContextDelegate;
    /** holds references to default editing context delegate without validation */
    //private static ERXECNoValidationDelegate _defaultECNoValidationDelegate;

    /**
     * This public observer is used to perform basic functions in
     * response to notifications. Specifically it handles
     * configuring the adapator context so that SQL debugging can
     * be enabled and disabled on the fly throgh the log4j system.
     * Handling cleanup issues when sessions timeout, i.e. releasing
     * all references to editing contexts created for that session.
     * Handling call all of the <code>did*</code> methods on
     * {@link ERXGenericRecord} subclasses after an editing context
     * has been saved. This delegate is also responsible for configuring
     * the {@link ERXCompilerProxy} and {@link ERXValidationFactory}.
     * This delegate is configured when this framework is loaded.
     */
    public static class Observer {
        /**
         * This method is called everytime the configuration file
         * is changed. This allows for turning SQL debuging on and
         * off at runtime.
         * @param n notification posted when the configuration file
         * 	changes.
         */
        public void configureAdaptorContext(NSNotification n) {
            ERXExtensions.configureAdaptorContext();
        }

        /**
         * This method is called everytime an editingcontext is
         * saved. This allows us to call all of the didInsert,
         * didUpdate and didSave methods on the enterprise objects
         * after the transaction is complete.
         * @param n notification that contains the array of inserted,
         *		updated and deleted objects.
         */
        public void didSave(NSNotification n) {
            ERXGenericRecord.didSave(n);
        }

        /**
         * This method is called when the application has finished
         * launching. Here is where log4j is configured for rapid
         * turn around, the compiler proxy is initialized and the
         * validation template system is configured.
         * @param n notification posted when the app is done launching
         */
        public void finishedLaunchingApp(NSNotification n) {
            ERXProperties.populateSystemProperties();
            ERXConfigurationManager.defaultManager().configureRapidTurnAround();
            // initialize compiler proxy
            ERXCompilerProxy.defaultProxy().initialize();
            ERXLocalizer.initialize();
            ERXValidationFactory.defaultFactory().configureFactory();
            if(!ERXProperties.webObjectsVersionIs522OrHigher()) {
                NSLog.setOut(new ERXNSLogLog4jBridge(ERXNSLogLog4jBridge.OUT));
                NSLog.setErr(new ERXNSLogLog4jBridge(ERXNSLogLog4jBridge.ERR));
                
                ERXNSLogLog4jBridge debugLogger = new ERXNSLogLog4jBridge(ERXNSLogLog4jBridge.DEBUG);
                debugLogger.setAllowedDebugLevel(NSLog.debug.allowedDebugLevel());
                NSLog.setDebug(debugLogger);
            }

            registerSQLSupportForSelector(new NSSelector(ERXPrimaryKeyListQualifier.IsContainedInArraySelectorName),
                EOQualifierSQLGeneration.Support.supportForClass(ERXPrimaryKeyListQualifier.class));
            registerSQLSupportForSelector(new NSSelector(ERXToManyQualifier.MatchesAllInArraySelectorName),
                EOQualifierSQLGeneration.Support.supportForClass(ERXToManyQualifier.class));
            registerSQLSupportForSelector(new NSSelector(ERXToManyQualifier.MatchesAnyInArraySelectorName),
                EOQualifierSQLGeneration.Support.supportForClass(ERXToManyQualifier.class));
        }

        /**
         * This method is called every time a session times
         * out. It allows us to release references to all the
         * editing contexts that were created when that particular
         * session was active.
         * Not used in WO 5.2+
         * @param n notification that contains the session ID.
         */
        public void sessionDidTimeOut(NSNotification n) {
            String sessionID=(String)n.object();
            ERXExtensions.sessionDidTimeOut(sessionID);
        }

        /**
         * This is needed for WO pre-5.2 when ec's were not
         * retained by their eos. Not called in 5.2+ systems.
         * @param n notification posted when an ec is created
         */
        public void editingContextDidCreate(NSNotification n) {
            EOEditingContext ec = (EOEditingContext)n.object();
            ERXExtensions.retainEditingContextForCurrentSession(ec);
        }
    }
    
    /**
     * Configures the framework. All the bits and pieces that need
     * to be configured are configured, those that need to happen
     * later are delayed by registering an observer for notifications
     * that are posted when the application is finished launching.
     */
    static {
            try {
                // This will load any optional configuration files, 
                ERXConfigurationManager.defaultManager().initialize();
                ERXLogger.configureLogging(System.getProperties());

                log().debug("Initializing framework: ERXExtensions");
                ERXArrayUtilities.initialize();
                
                // False by default
                if (ERXValueUtilities.booleanValue(System.getProperty(ERXSharedEOLoader.PatchSharedEOLoadingPropertyKey))) {
                    ERXSharedEOLoader.patchSharedEOLoading();
                }            
            } catch (Exception e) {
                System.out.println("Caught exception: " + e.getMessage() + " stack: ");
                e.printStackTrace();
            }

            observer = new Observer();

            try {
                ERXExtensions.configureAdaptorContextRapidTurnAround(observer);
            } catch (RuntimeException e) {
                System.out.println("Caught exception: " + e.getMessage() + " stack: ");
                e.printStackTrace();
            }
            
            try {
                EODatabaseContext.setDefaultDelegate(ERXDatabaseContextDelegate.defaultDelegate());
                ERXExtensions.setDefaultDelegate(EOSharedEditingContext.defaultSharedEditingContext(), true);

                ERXEntityClassDescription.registerDescription();
                NSNotificationCenter.defaultCenter().addObserver(observer,
                                                                 new NSSelector("didSave", ERXConstant.NotificationClassArray),
                                                                 EOEditingContext.EditingContextDidSaveChangesNotification,
                                                                 null);
                NSNotificationCenter.defaultCenter().addObserver(observer,
                                                                 new NSSelector("finishedLaunchingApp", ERXConstant.NotificationClassArray),
                                                                 WOApplication.ApplicationWillFinishLaunchingNotification,
                                                                 null);
                if (!ERXProperties.webObjectsVersionIs52OrHigher()) {
                    NSNotificationCenter.defaultCenter().addObserver(observer,
                                                                 new NSSelector("sessionDidTimeOut", ERXConstant.NotificationClassArray),
                                                                 WOSession.SessionDidTimeOutNotification,
                                                                 null);
                    NSNotificationCenter.defaultCenter().addObserver(observer,
                                                                     new NSSelector("editingContextDidCreate",
                                                                                    ERXConstant.NotificationClassArray),
                                                                     ERXEC.EditingContextDidCreateNotification,
                                                                     null);                    
                }
            } catch (Exception e) {
                System.out.println("Caught exception: " + e.getMessage() + " stack: ");
                e.printStackTrace();
            }
    }

    private static Map _qualifierKeys;

    public static synchronized void registerSQLSupportForSelector(NSSelector selector, EOQualifierSQLGeneration.Support support) {
        if(_qualifierKeys == null) {
            _qualifierKeys = new HashMap();
            EOQualifierSQLGeneration.Support old = EOQualifierSQLGeneration.Support.supportForClass(EOKeyValueQualifier.class);
            EOQualifierSQLGeneration.Support.setSupportForClass(new KeyValueQualifierSQLGenerationSupport(old), EOKeyValueQualifier.class);
        }
        _qualifierKeys.put(selector.name(), support);
    }

    /**
     * Support class that listens for EOKeyValueQualifiers that have a selector
     * that was registered and uses their support instead.
     * You'll use this mainly to bind queryOperators in display groups.
     * @author ak
     */
    public static class KeyValueQualifierSQLGenerationSupport extends EOQualifierSQLGeneration.Support {

        private EOQualifierSQLGeneration.Support _old;

        public KeyValueQualifierSQLGenerationSupport(EOQualifierSQLGeneration.Support old) {
            _old = old;
        }

        private EOQualifierSQLGeneration.Support supportForQualifier(EOQualifier qualifier) {
            EOQualifierSQLGeneration.Support support = null;
            if(qualifier instanceof EOKeyValueQualifier) {
                synchronized (_qualifierKeys) {
                    support = (Support) _qualifierKeys.get(((EOKeyValueQualifier)qualifier).selector().name());
                }
            }
            if(support == null) {
                support = _old;
            }
            return support;
        }

        public String sqlStringForSQLExpression(EOQualifier eoqualifier, EOSQLExpression e) {
        	try {
        		return supportForQualifier(eoqualifier).sqlStringForSQLExpression(eoqualifier, e);
        	}
        	catch (JDBCAdaptorException ex) {
        		ERXExtensions._log.error("Failed to generate sql string for qualifier " + eoqualifier + " on entity " + e.entity() + ".");
        		throw ex;
        	}
        }

        public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier eoqualifier, EOEntity eoentity) {
            EOQualifier result = supportForQualifier(eoqualifier).schemaBasedQualifierWithRootEntity(eoqualifier, eoentity);
            return result;
        }

        public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier eoqualifier, EOEntity eoentity, String s) {
            return supportForQualifier(eoqualifier).qualifierMigratedFromEntityRelationshipPath(eoqualifier, eoentity, s);
        }
    }

    /** logging support for the adaptor channel */
    public static ERXLogger adaptorLogger;

    /** logging support for shared object loading */
    public static ERXLogger sharedEOadaptorLogger;

    /** flag to indicate if adaptor channel logging is enabled */
    private static Boolean adaptorEnabled;

    /** 
     * flag to inidicate if rapid turn around is enabled for the
     * adaptor channel logging. 
     */
    private static boolean _isConfigureAdaptorContextRapidTurnAround = false;

    /**
     * Configures the passed in observer to register a call back 
     * when the configuration file is changed. This allows one to 
     * change a logger's setting and have that changed value change
     * the NSLog setting to log the generated SQL. This method is
     * called as part of the framework initialization process.
     * @param observer object to register the call back with.
     */
    // FIXME: This shouldn't be enabled when the application is in production.
    // FIXME: Now that all of the logging has been centralized, we should just be able
    //		to do something like this, but much more generic, i.e. have a mapping
    //		between logger names and NSLog groups, for example com.webobjects.logging.DebugGroupSQLGeneration we should
    //		be able to get the last part of the logger name and look up that log group and turn 
    public static void configureAdaptorContextRapidTurnAround(Object observer) {
        if (!_isConfigureAdaptorContextRapidTurnAround) {
            // This allows enabling from the log4j system.
            adaptorLogger = ERXLogger.getERXLogger("er.transaction.adaptor.EOAdaptorDebugEnabled");
            sharedEOadaptorLogger = ERXLogger.getERXLogger("er.transaction.adaptor.EOSharedEOAdaptorDebugEnabled");
            if (adaptorLogger.isDebugEnabled() && !NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess)) {
                NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess);
                NSLog.setAllowedDebugLevel(NSLog.DebugLevelInformational);
            }
            adaptorEnabled = NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess) ? Boolean.TRUE : Boolean.FALSE;
                                          // Allows rapid turn-around of adaptor debugging.
            NSNotificationCenter.defaultCenter().addObserver(observer,
                                                             new NSSelector("configureAdaptorContext", ERXConstant.NotificationClassArray),
                                                             ERXConfigurationManager.ConfigurationDidChangeNotification,
                                                             null);
            _isConfigureAdaptorContextRapidTurnAround = true;
        }
    }

    /**
     * This method is called by the delegate when the configuration
     * file is changed. It's sole purpose is to map a logging logger
     * to a debug group. Hopefully in the future we will have a more
     * generic solution.
     */
    public static void configureAdaptorContext() {
        Boolean targetState = null;
        if (adaptorLogger.isDebugEnabled() && !adaptorEnabled.booleanValue()) {
            targetState = Boolean.TRUE;
        } else if (!adaptorLogger.isDebugEnabled() && adaptorEnabled.booleanValue()) {
            targetState = Boolean.FALSE;
        }
        if (targetState != null) {
            // We set the default, so all future adaptor contexts are either enabled or disabled.
            if (NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess) != targetState.booleanValue())
                if (targetState.booleanValue()) {
                    NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess);
                    NSLog.setAllowedDebugLevel(NSLog.DebugLevelInformational);
                } else {
                    NSLog.refuseDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess);
                    NSLog.setAllowedDebugLevel(NSLog.DebugLevelCritical);
                }
            if (targetState.booleanValue()) {
                adaptorLogger.info("Adaptor debug on");
            } else {
                adaptorLogger.info("Adaptor debug off");
            }
            adaptorEnabled = targetState;
        }
    }

    /**
     * deprecated see {@link ERXEC}
     */
    public static ERXEditingContextDelegate defaultEditingContextDelegate() {
        return (ERXEditingContextDelegate)ERXEC.factory().defaultEditingContextDelegate();
    }

    /**
     * deprecated see {@link ERXEC}
     */
    public static void setDefaultEditingContextDelegate(ERXEditingContextDelegate delegate) {
        ERXEC.factory().setDefaultEditingContextDelegate(delegate);
    }

    /**
     * deprecated see {@link ERXEC}
     */
    public static ERXECNoValidationDelegate defaultECNoValidationDelegate() {
        return (ERXECNoValidationDelegate)ERXEC.factory().defaultNoValidationDelegate();
    }

    /**
     * deprecated see {@link ERXEC}
     */
    public static void setDefaultECNoValidationDelegate(ERXECNoValidationDelegate delegate) {
        ERXEC.factory().setDefaultNoValidationDelegate(delegate);
    }

    /**
     * deprecated see {@link ERXEC}
     */
    public static EOEditingContext newEditingContext() {
        return ERXEC.newEditingContext();
    }

    /**
     * deprecated see {@link ERXEC}
     */    
    public static EOEditingContext newEditingContext(boolean validation) {
        return ERXEC.newEditingContext(validation);
    }

    /**
     * deprecated see {@link ERXEC}
     */
    public static EOEditingContext newEditingContext(EOObjectStore objectStore) {
        return ERXEC.newEditingContext(objectStore);
    }

    /**
     * deprecated see {@link ERXEC}
     */    
    public static EOEditingContext newEditingContext(EOObjectStore objectStore, boolean validation) {
        return ERXEC.newEditingContext(objectStore, validation);
    }

    /**
     * @deprecated see { @link ERXEOAccessUtilities#evaluateSQLWithEntityNamed}
     */
    public static void evaluateSQLWithEntityNamed(String entityName, String exp, EOEditingContext ec) {
        ERXEOAccessUtilities.evaluateSQLWithEntityNamed(ec, entityName, exp);
    }

    /**
     * Retaining the editing contexts explicitly until the session that was active
     * when they were created goes away
     * this hopefully will go some way towards avoiding the 'attempted to send
     * message to EO whose EditingContext is gone. Only used in pre-5.2 systems.
     */
    private static NSMutableDictionary _editingContextsPerSession;
    
    /**
     * This method is called when a session times out.
     * Calling this method will release references to
     * all editing contexts that were created when this
     * session was active. This method is only called in
     * pre-WO 5.2 versions of WebObjects.
     * @param sessionID of the session that timed out
     */
    public static void sessionDidTimeOut(String sessionID) {
        if (sessionID != null) {
            if (_editingContextsPerSession != null) {
                if (log().isDebugEnabled()) {
                    NSArray a=(NSArray)_editingContextsPerSession.objectForKey(sessionID);
                    log().debug("Session "+sessionID+" is timing out ");
                    log().debug("Found "+ ((a == null) ? 0 : a.count()) + " editing context(s)");
                }
                NSArray ecs = (NSArray)_editingContextsPerSession.removeObjectForKey(sessionID);
                // Just helping things along.
                if (ecs != null && ecs.count() > 0) {
                    for (Enumeration ecEnumerator = ecs.objectEnumerator(); ecEnumerator.hasMoreElements();) {
                        ((EOEditingContext)ecEnumerator.nextElement()).dispose();
                    }
                }                
            }
        }
    }

    /**
     * Retains an editing context for the current session. This is only needed or
     * used for versions of WO pre-5.2. If you use ERXEC to create your editing
     * contexts then you never need to call this method yourself.
     * @param ec to be retained.
     */
    public static void retainEditingContextForCurrentSession(EOEditingContext ec) {
         WOSession s=session();
         if (s != null) {
             if (_editingContextsPerSession == null) {
                 _editingContextsPerSession = new NSMutableDictionary();
             }
             NSMutableArray a = (NSMutableArray)_editingContextsPerSession.objectForKey(s.sessionID());
             if (a == null) {
                 a = new NSMutableArray();
                 _editingContextsPerSession.setObjectForKey(a, s.sessionID());
                 if (log().isDebugEnabled())
                     log().debug("Creating array for "+s.sessionID());
             }
             a.addObject(ec);
             if (log().isDebugEnabled())
                 log().debug("Added new ec to array for "+s.sessionID());
         } else if (log().isDebugEnabled()) {
             log().debug("Editing Context created with null session.");
         }
    }

    /**
     * deprecated see {@link ERXEC}
     */
    public static void setDefaultDelegate(EOEditingContext ec) {
        ERXEC.factory().setDefaultDelegateOnEditingContext(ec);
    }

    /**
     * deprecated see {@link ERXEC}
     */
    public static void setDefaultDelegate(EOEditingContext ec, boolean validation) {
        ERXEC.factory().setDefaultDelegateOnEditingContext(ec, validation);
    }

    /**
     * deprecated see {@link ERXEOControlUtilities#dataSourceForArray(NSArray)}
     */
    public static EOArrayDataSource dataSourceForArray(NSArray array) {
        return ERXEOControlUtilities.dataSourceForArray(array);
    }

    /**
     * deprecated see {@link ERXEOControlUtilities#arrayFromDataSource(NSArray)}
     */
    public static NSArray arrayFromDataSource(EODataSource dataSource) {
        return ERXEOControlUtilities.arrayFromDataSource(dataSource);
    }

    /**
     * deprecated see {@link ERXEOControlUtilities#arrayFromDataSource(NSArray)}
     */
    public static EODetailDataSource dataSourceForObjectAndKey(EOEnterpriseObject eo, String key) {
        return ERXEOControlUtilities.dataSourceForObjectAndKey(eo, key);
    }

    /**
     * @deprecated use {@link ERXArrayUtilities#friendlyDisplayForKeyPath(NSArray, String, String, String, String)
     */
    public static String friendlyEOArrayDisplayForKey(NSArray list, String attribute, String nullArrayDisplay) {
        return ERXArrayUtilities.friendlyDisplayForKeyPath(list, attribute, nullArrayDisplay, ", ", " and ");
    }

    /**
     * @deprecated use {@link ERXStringUtilities#replaceStringByStringInString(String, String, String)
     */
    public static String replaceStringByStringInString(String old, String newString, String s) {
        return ERXStringUtilities.replaceStringByStringInString(old,newString,s);
    }

    /**
     * @deprecated use {@link ERXStringUtilities#emptyStringForNull(String)
     */
    public static String emptyStringForNull(String s) {
        return ERXStringUtilities.emptyStringForNull(s);
    }

    /**
     * @deprecated use {@link ERXStringUtilities#nullForEmptyString(String)
     */
    public static String nullForEmptyString(String s) {
        return ERXStringUtilities.nullForEmptyString(s);
    }

    /**
     * @deprecated use {@link ERXStringUtilities#stringIsNullOrEmpty(String)
     */
    public static boolean stringIsNullOrEmpty(String s) {
        return ERXStringUtilities.stringIsNullOrEmpty(s);
    }

    /**
     * @deprecated use {@link ERXStringUtilities#numberOfOccurrencesOfCharInString(char,String)
     */
    public static int numberOfOccurrencesOfCharInString(char c, String s) {
        return ERXStringUtilities.numberOfOccurrencesOfCharInString(c, s);
    }

    /**
     * @deprecated use {@link ERXStringUtilities#numberOfOccurrencesOfCharInString(int,String)
     */
    public static String stringWithNtimesString(int n, String s) {
        return ERXStringUtilities.stringWithNtimesString(n, s);
    }

    /**
     * Removes all of the HTML tags from a given string.
     * Note: that this is a very simplistic implementation
     * and will most likely not work with complex HTML.
     * Note: for actual conversion of HTML tags into regular
     * strings have a look at {@link ERXSimpleHTMLFormatter}
     * @param s html string
     * @return string with all of it's html tags removed
     */
    // FIXME: this is so simplistic it will break if you sneeze
    // MOVEME: ERXStringUtilities 
    public static String removeHTMLTagsFromString(String s) {
        StringBuffer result=new StringBuffer();
        if (s.length()>0) {
            int position=0;
            while (position<s.length()) {
                int indexOfOpeningTag=s.indexOf("<",position);
                if (indexOfOpeningTag!=-1) {
                    if (indexOfOpeningTag!=position)
                        result.append(s.substring(position, indexOfOpeningTag));
                    position=indexOfOpeningTag+1;
                } else {
                    result.append(s.substring(position, s.length()));
                    position=s.length();
                }
                int indexOfClosingTag=s.indexOf(">",position);
                if (indexOfClosingTag!=-1) {
                    position= indexOfClosingTag +1;
                } else {
                    result.append(s.substring(position, s.length()));
                    position=s.length();
                }
            }
        }
        return replaceStringByStringInString("&nbsp;"," ",result.toString());
    }

    /**
     * Forces the garbage collector to run. The
     * max loop parameter determines the maximum
     * number of times to run the garbage collector
     * if the memory footprint is still going down.
     * In normal cases you would just need to call
     * this method with the parameter 1. If called
     * with the parameter 0 the garbage collector
     * will continue to run until no more free memory
     * is available to collect. <br/>
     * <br/>
     * Note: This can be a very costly operation and
     * should only be used in extreme circumstances.
     * @param maxLoop maximum times to run the garbage
     *		collector. Passing in 0 will cause the
     *		collector to run until all free objects
     *		have been collected.
     */
    public static void forceGC(int maxLoop) {
        if (log().isDebugEnabled()) log().debug("Forcing full Garbage Collection");
        Runtime runtime = Runtime.getRuntime();
        long isFree = runtime.freeMemory();
        long wasFree;
        int i=0;
        do {
            wasFree = isFree;
            runtime.gc();
            isFree = runtime.freeMemory();
            i++;
        } while (isFree > wasFree && (maxLoop<=0 || i<maxLoop) );
        runtime.runFinalization();
    }

    /**
     * Tests if an enterprise object is a new object by
     * looking to see if it is in the list of inserted
     * objects for the editing context or if the editing
     * context is null.<br/>
     * <br/>
     * Note: An object that has been deleted will have it's
     * editing context set to null which means this method
     * would report true for an object that has been deleted
     * from the database.
     * @param eo enterprise object to check
     * @return true or false depending on if the object is a
     *		new object.
     */
    // MOVEME: ERXEOFUtilities (when we have them)
    public static boolean isNewObject(EOEnterpriseObject eo) {
        if (eo.editingContext() == null) return true;
        
        EOGlobalID gid = eo.editingContext().globalIDForObject(eo);
        return gid.isTemporary();
    }

    /**
     * @deprecated use {@link ERXEOControlUtilities.primaryKeyStringForObject(EOEnterpriseObject)}
     */
    public static String primaryKeyForObject(EOEnterpriseObject eo) {
        return ERXEOControlUtilities.primaryKeyStringForObject(eo);
    }

    /**
     * @deprecated use just about anything else, like Random.nextInt() for example
     */
    public static Object rawPrimaryKeyFromPrimaryKeyAndEO(NSDictionary primaryKey, EOEnterpriseObject eo) {
        NSArray result = primaryKeyArrayForObject(eo);

        if(result!=null && result.count() == 1) {
            return result.lastObject();
        } else if (result!=null && result.count() > 1) {
            log().warn("Attempting to get a raw primary key from an object with a compound key: " + eo.eoShallowDescription());
        }
        return result;
    }

    /**
     * Gives the primary key array for a given enterprise
     * object. This has the advantage of not firing the
     * fault of the object, unlike the method in 
     * {@link com.webobjects.eoaccess.EOUtilities EOUtilities}.
     * @param obj enterprise object to get the primary key array from.
     * @return array of all the primary key values for the object.
     */
    // MOVEME: ERXEOFUtilities
    public static NSArray primaryKeyArrayForObject(EOEnterpriseObject obj) {
        EOEditingContext ec = obj.editingContext();
        if (ec == null) {
            //you don't have an EC! Bad EO. We can do nothing.
            return null;
        }
        EOGlobalID gid = ec.globalIDForObject(obj);
        if (gid.isTemporary()) {
            //no pk yet assigned
            return null;
        }
        EOKeyGlobalID kGid = (EOKeyGlobalID) gid;
        return kGid.keyValuesArray();
    }

    /**
     * Returns the raw primary key of the object. Possible
     * objects returned could be Integer, BigDecimal or NSData.
     * Note: the object passed in should only have one primary
     * key.
     * @param eo enterprise object to get the primary key from
     * @param primary key of the object in it's raw form
     */
    public static Object rawPrimaryKeyForObject(EOEnterpriseObject eo) {
        Object result = null;
        if (eo!=null)  {
            // NSDictionary d=EOUtilities.primaryKeyForObject(eo.editingContext(),eo);
            // result = rawPrimaryKeyFromPrimaryKeyAndEO(d, eo);
            result = rawPrimaryKeyFromPrimaryKeyAndEO(null, eo);
        }
        return result;
    }

    /**
     * Capitalizes the given string.
     * @param s string to capitalize
     * @return capitalized sting if the first char is a
     *		lowercase character.
     */
    // MOVEME: ERXStringUtilities
    public static String capitalize(String s) {
        String result=s;
        if (s!=null && s.length()>0) {
            char c=s.charAt(0);
            if (Character.isLowerCase(c))
                result=Character.toUpperCase(c)+s.substring(1);
        }
        return result;
    }

    /**
     * Plurifies a given string for a given language.
     * See {@link ERXLocalizer} for more information.
     * @param s string to plurify
     * @param howMany number of its
     * @param language target language
     * @return plurified string
     */
    // MOVEME: ERXStringUtilities
    public static String plurify(String s, int howMany, String language) {
        return ERXLocalizer.localizerForLanguage(language).plurifiedString(s, howMany);
    }

    /**
     * A safe comparison method that first checks to see
     * if either of the objects are null before comparing
     * them with the <code>equals</code> method.<br/>
     * <br/>
     * Note that if both objects are null then they will
     * be considered equal.
     * @param v1 first object
     * @param v2 second object
     * @return true if they are equal, false if not
     */
    public static boolean safeEquals(Object v1, Object v2) {
        return v1==v2 || (v1!=null && v2!=null && v1.equals(v2));
    }

    /**
     * A safe different comparison method that first checks to see
     * if either of the objects are null before comparing
     * them with the <code>equals</code> method.<br/>
     * <br/>
     * Note that if both objects are null then they will
     * be considered equal.
     * @param v1 first object
     * @param v2 second object
     * @return treu if they are not equal, false if they are
     */
    public static boolean safeDifferent(Object v1, Object v2) {
        return
        v1==v2 ? false :
        v1==null && v2!=null ||
        v1!=null && v2==null ||
        !v1.equals(v2);
    }

    /**
     * Tests if a given string object can be parsed into
     * an integer.
     * @param s string to be parsed
     * @return if the string can be parsed into an int
     */
    // FIXME: Should return false if the object is null.
    // MOVEME: ERXStringUtilities
    public static boolean stringIsParseableInteger(String s) {
        try {
            int x = Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns an integer from a parsable string. If the
     * string can not be parsed then 0 is returned.
     * @param s string to be parsed.
     * @return int from the string or 0 if un-parsable.
     */
    // MOVEME: ERXStringUtilities
    public static int intFromParseableIntegerString(String s) {
        try {
            int x = Integer.parseInt(s);
            return x;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // DELETEME: Already have this method in this class: replaceStringByStringInString
    //		 plus this is the wrong implementation.
    public static String substituteStringByStringInString(String s1, String s2, String s) {
        NSArray a=NSArray.componentsSeparatedByString(s,s1);
        return a!=null ? a.componentsJoinedByString(s2) : s;
    }

    // DELETEME:  Depricated use singleton method accessor
    public static ERXSimpleHTMLFormatter htmlFormatter() { return ERXSimpleHTMLFormatter.formatter(); }

    /**
     * This method handles 3 different cases
     *
     * 1. keyPath is a single key and represents a relationship
     *		--> addObjectToBothSidesOfRelationshipWithKey
     * 2. keyPath is a single key and does NOT represents a relationship
     * 3. keyPath is a real key path: break it up, navigate to the last atom
     *		--> back to 1. or 2.
     * @param to enterprise object that is having objects added to it
     * @param from enterprise object that is providing the objects
     * @param keyPath that specifies the relationship on the to object
     *		to add the objects to.
     */
    // MOVEME: ERXEOFUtilities
    // FIXME: Should ensure local instances of objects
    public static void addObjectToBothSidesOfPotentialRelationshipFromObjectWithKeyPath(EOEnterpriseObject to,
                                                                                        EOEnterpriseObject from,
                                                                                        String keyPath) {
        if (from!=null) {
            if (keyPath.indexOf('.')!=-1) { // we have a key path
                String partialKeyPath=ERXStringUtilities.keyPathWithoutLastProperty(keyPath);
                from=(EOEnterpriseObject)from.valueForKeyPath(partialKeyPath);
                keyPath=ERXStringUtilities.lastPropertyKeyInKeyPath(keyPath);
            }
            //if the key is not a keyPath we can check if the key is actually a relationship
            EOEntity e=ERXEOAccessUtilities.entityNamed(from.editingContext(), from.entityName());
            EORelationship r=e.relationshipNamed(keyPath);
            if (r!=null) //if the key correspond to a relationship
                from.addObjectToBothSidesOfRelationshipWithKey(to, keyPath);
            else
                from.takeValueForKeyPath(to,keyPath);
        }
    }

    /**
     * Recursively flattens an array of arrays into a single
     * array of elements.<br/>
     * @deprecated use {@link ERXArrayUtilities#flatten(NSArray) 
     * ERXArrayUtilities.flatten}
     */
    public static NSArray flatten(NSArray array) {
        return ERXArrayUtilities.flatten(array);
    }

    /**
     * Groups an array of objects by a given key path.
     * @deprecated use {@link ERXArrayUtilities#arrayGroupedByKeyPath(NSArray, String)
     * ERXArrayUtilities.arrayGroupedByKeyPath}
     */
    public static NSDictionary eosInArrayGroupedByKeyPath(NSArray eos, String keyPath) {
        return eosInArrayGroupedByKeyPath(eos,keyPath,true,null);
    }

    /**
     * Groups an array of objects by a given key path.
     * @deprecated use {@link ERXArrayUtilities#arrayGroupedByKeyPath(NSArray, String, boolean, String)
     * ERXArrayUtilities.arrayGroupedByKeyPath} 
     */
    public static NSDictionary eosInArrayGroupedByKeyPath(NSArray eos,
                                                          String keyPath,
                                                          boolean includeNulls,
                                                          String extraKeyPathForValues) {
        return ERXArrayUtilities.arrayGroupedByKeyPath(eos,keyPath,includeNulls,extraKeyPathForValues);
    }

    /**
     * Simple comparision method to see if two array
     * objects are identical sets.
     * @deprecated use {@link ERXArrayUtilities#arraysAreIdenticalSets(NSArray, NSArray)}
     */
    // MOVEME: ERXArrayUtilities
    public static boolean arraysAreIdenticalSets(NSArray a1, NSArray a2) {
        return ERXArrayUtilities.arraysAreIdenticalSets(a1,a2);
    }

    /**
     * Filters an array using the {@link com.webobjects.eocontrol.EOQualifierEvaluation EOQualifierEvaluation} interface.
     * @deprecated use {@link ERXArrayUtilities#filteredArrayWithQualifierEvaluation(NSArray, EOQualifierEvaluation)
     * ERXArrayUtilities.filteredArrayWithQualifierEvaluation}
     */
    public static NSArray filteredArrayWithQualifierEvaluation(NSArray a, EOQualifierEvaluation q) {
        return ERXArrayUtilities.filteredArrayWithQualifierEvaluation(a,q);
    }

    /** holds the array of hex values */
    private static final char hex[] = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    /**
     * Converts an array of bytes to a hex string
     * @param data array of bytes
     * @return hex representation of the byte array
     */
    // MOVEME: ERXStringUtilities
    public static String byteArrayToHexString (final byte data[]) {
        int len = data.length;
        char hexchars[] = new char[2 * len];

        int ix = 0;
        for (int i = 0; i < len; i++) {
            hexchars[ix++] = hex[(data[i] >> 4) & 0xf];
            hexchars[ix++] = hex[data[i] & 0xf];
        }
        return new String(hexchars);
    }


    /**
     * Converts a hex string into an array of bytes
     * @param s string
     * @return byte array
     */
    // MOVEME: ERXStringUtilities
    public static byte[] hexStringToByteArray(String s) {
        byte[] result=null;
        if (s!=null) {
            int l=s.length();
            if (l % 2 !=0) throw new RuntimeException("hexStringToByteArray: expected an even length string");
            s=s.toLowerCase();
            result=new byte[l/2];
            int i=0;
            for (int j=0; j<l;) {
                char c1=s.charAt(j++); int b1=c1<'a' ? c1-'0' : c1-'a'+10;
                char c2=s.charAt(j++); int b2=c2<'a' ? c2-'0' : c2-'a'+10;
                result[i++]=(byte)((b1<<4)+b2);
            }
        }
        return result;
    }
    
    /**
     * Returns the byte array for a given file.
     * @param f file to get the bytes from
     * @throws IOException if things go wrong
     * @return byte array of the file.
     */
    public static byte[] bytesFromFile(File f) throws IOException {
        return ERXFileUtilities.bytesFromFile(f);
    }

    /**
     * Returns a string from the file using the default
     * encoding.
     * @param f file to read
     * @return string representation of that file.
     */
    // MOVEME: ERXFileUtilities
    public static String stringFromFile(File f) throws IOException {
        return ERXFileUtilities.stringFromFile(f);
    }
    /**
     * Returns a string from the file using the specified
     * encoding.
     * @param f file to read
     * @param encoding to be used, null will use the default
     * @return string representation of the file.
     */
    // MOVEME: ERXFileUtilities    
    public static String stringFromFile(File f, String encoding) throws IOException {
        return ERXFileUtilities.stringFromFile(f, encoding);
    }

    /**
     * Determines the last modification date for a given file
     * in a framework. Note that this method will only test for
     * the global resource not the localized resources.
     * @param fileName name of the file
     * @param frameworkName name of the framework, null or "app"
     *		for the application bundle
     * @return the <code>lastModified</code> method off of the
     *		file object
     */
    // MOVEME: ERXFileUtilities
    // ENHANCEME: Should be able to specify the language to check
    public static long lastModifiedDateForFileInFramework(String fileName, String frameworkName) {
        return ERXFileUtilities.lastModifiedDateForFileInFramework(fileName, frameworkName);
    }

    /**
     * Reads a file in from the file system and then parses
     * it as if it were a property list.
     * @param fileName name of the file
     * @param aFrameWorkName name of the framework, null or
     *		'app' for the application bundle.
     * @return de-serialized object from the plist formatted file
     *		specified.
     */
    // FIXME: Capitalize inFramework
    // MOVEME: ERXFileUtilities
    public static Object readPropertyListFromFileinFramework(String fileName, String aFrameWorkName) {
        return ERXFileUtilities.readPropertyListFromFileInFramework(fileName, aFrameWorkName);
    }

    /**
     * Reads a file in from the file system for the given set
     * of languages and then parses the file as if it were a
     * property list.
     * @param fileName name of the file
     * @param aFrameWorkName name of the framework, null or
     *		'app' for the application bundle.
     * @param languageList language list search order
     * @return de-serialized object from the plist formatted file
     *		specified.
     */
    // FIXME: Not the best way of handling encoding
    // MOVEME: ERXFileUtilities
    public static Object readPropertyListFromFileInFramework(String fileName,
                                                             String aFrameWorkName,
                                                             NSArray languageList) {
        return ERXFileUtilities.readPropertyListFromFileInFramework(fileName,
                                                             aFrameWorkName,
                                                             languageList);
    }

    /**
     * For a given enterprise object and key path, will return what
     * the key 'unit' returns from the userInfo dictionary of the
     * last property of the key path's EOAttribute or EORelationship.
     * The userInfo dictionary can be edited via EOModeler, it is that
     * open book looking icon when looking at either an attribute or
     * relationship.<br/>
     * <br/>
     * For example if the userInfo dictionary or the attribute 'speed' on the
     * entity Car contained the key-value pair unit=mph, then this method
     * would be able to resolve that unit given either of these keypaths:<br/>
     * <code>userInfoUnit(aCar, "speed");<br/>
     * userInfoUnit(aDrive, "toCar.speed");</code></br>
     * Units can be very useful for adding meta information to particular
     * attributes and relationships in models. The ERDirectToWeb framework
     * adds support for displaying units.
     * @param object to resolve the key-path from
     * @param key path off of the object
     * @return unit information stored in the userInfo dictionary
     */
    // ENHANCEME: Should be more generic for resolving any key off of the userInfo
    //		dictionary.
    // ENHANCEME: Should also support defaulting to the same attribute in the parent entity
    //		if it isn't found or possibly defaulting to the entity's userInfo itself
    // MOVEME: ERXEOFUtilities
    public static String userInfoUnit(EOEnterpriseObject object, String key) {
        // return the unit stored in the userInfo dictionary of the appropriate EOAttribute
        EOEntity entity=null;
        String lastKey=null;
        String result=null;
        if (key.indexOf(".")==-1) {
            String entityName=object.entityName();
            entity=ERXEOAccessUtilities.entityNamed(object.editingContext(), entityName);
            lastKey=key;
        } else {
            String partialKeyPath=ERXStringUtilities.keyPathWithoutLastProperty(key);
            EOEnterpriseObject objectForPropertyDisplayed=(EOEnterpriseObject)object.valueForKeyPath(partialKeyPath);
            if (objectForPropertyDisplayed!=null) {
                entity=ERXEOAccessUtilities.entityNamed(object.editingContext(), objectForPropertyDisplayed.entityName());
                lastKey=ERXStringUtilities.lastPropertyKeyInKeyPath(key);
            }
        }
        if (entity!=null && lastKey!=null) {
            EOAttribute a=entity.attributeNamed(lastKey);
            NSDictionary userInfo=null;
            if (a!=null) userInfo=a.userInfo();
            else {
                EORelationship r=entity.relationshipNamed(lastKey);
                if (r!=null) userInfo=r.userInfo();
            }
            result= (String)(userInfo!=null ? userInfo.valueForKey("unit") : null);
        }
        return result;
    }

    /**
     * Resolves a given user info unit string for a given object.
     * User info values are stored in an EOAttibute or EORelationship's
     * userInfo dictionary. See the method <code>userInfoUnit</code> for
     * a better description of getting values out of the userInfo
     * dictionary. This method deals with resolving dynamic userInfo
     * keys. These keys need to start with the '@@' symbol. For instance
     * if you have the user info value '@unit' off of an attribute for the
     * entity Movie, then you can either pass in a Movie object or a
     * different object with a prefix key path to a movie object.<br/>
     * @param userInfoUnitString string to be resolved, needs to start with
     *		'@@' this keypath will be evaluated against either the object
     *		if no prefixKeyPath is specified or the object returned by
     *		the prefixKeyPath being evaluated against the object passed in.
     * @param object to resolve either the user info unit or the prefixKeyPath.
     * @param prefixKeyPath used as a prefix for the unit resolution.
     * @return the resolved unit from the object.
     */
    public static String resolveUnit(String userInfoUnitString,
                                     EOEnterpriseObject object,
                                     String prefixKeyPath) {
        // some of our units (stored in the user info take the form of @project.unit
        // this method resolves the @keyPath..
        if(userInfoUnitString!=null && userInfoUnitString.indexOf("@")>-1){
            String keyPath = userInfoUnitString.substring(1);
            String PropertyKeyWithoutLastProperty = ERXStringUtilities.keyPathWithoutLastProperty(prefixKeyPath);
            EOEnterpriseObject objectForPropertyDisplayed = null;
            if(PropertyKeyWithoutLastProperty!=null){
                objectForPropertyDisplayed = object!=null ? (EOEnterpriseObject)object.valueForKeyPath(PropertyKeyWithoutLastProperty) : null;
            }else{
                objectForPropertyDisplayed = object;
            }
            userInfoUnitString = objectForPropertyDisplayed!=null ? (String)objectForPropertyDisplayed.valueForKeyPath(keyPath) : null;
        }
        return userInfoUnitString;
    }

    /**
     * Filters out all of the duplicate objects in
     * a given array.<br/>
     * @deprecated use {@link ERXArrayUtilities#arrayWithoutDuplicates(NSArray)
     * ERXArrayUtilities.arrayWithoutDuplicates}
     */
    public static NSArray arrayWithoutDuplicates(NSArray anArray) {
        return ERXArrayUtilities.arrayWithoutDuplicates(anArray);
    }

    /**
     * Filters out duplicates of an array of enterprise objects
     * based on the value of the given key off of those objects.
     * @deprecated use {@link ERXArrayUtilities#arrayWithoutDuplicateKeyValue(NSArray, String)
     * ERXArrayUtilities.arrayWithoutDuplicateKeyValue}
     */
    public static NSArray arrayWithoutDuplicateKeyValue(NSArray eos, String key){
        return ERXArrayUtilities.arrayWithoutDuplicateKeyValue(eos,key);
    }

    /**
     * Subtracts the contents of one array from another.
     * @deprecated use {@link ERXArrayUtilities#arrayMinusArray(NSArray, NSArray)
     * ERXArrayUtilities.arrayMinusArray}
     */
    public static NSArray arrayMinusArray(NSArray main, NSArray minus){
        return ERXArrayUtilities.arrayMinusArray(main,minus);
    }

    /**
     * Creates an array preserving order by adding all of the
     * non-duplicate values from the second array to the first.
     * @deprecated use {@link ERXArrayUtilities#arrayByAddingObjectsFromArrayWithoutDuplicates(NSArray, NSArray)
     * ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates}
     */
    public static NSArray arrayByAddingObjectsFromArrayWithoutDuplicates(NSArray a1, NSArray a2) {
        return ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(a1,a2);
    }

    /**
     * Adds all of the non-duplicate elements from the second
     * array to the mutable array.
     * @deprecated use {@link ERXArrayUtilities#addObjectsFromArrayWithoutDuplicates(NSMutableArray, NSArray)
     * ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates}
     */
    public static void addObjectsFromArrayWithoutDuplicates(NSMutableArray a1, NSArray a2) {
        ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(a1,a2);
    }

    /**
    * @deprecated use {@link ERXArrayUtilities#friendlyDisplayForKeyPath(NSArray, String, String, String, String)
    * ERXArrayUtilities.friendlyDisplayForKeyPath} 
    */
    // DELETEME: duplicate method friendlyEOArrayDisplayForKey
    public static String userPresentableEOArray(NSArray array, String attribute) {
        return ERXArrayUtilities.friendlyDisplayForKeyPath(array, attribute, "None", ", ", " and ");
    }

    /**
     * Refreshes all of the objects for an array of entity names.
     * @param names array of shared entity names
     */
    // FIXME: Uses default model group.
    // MOVEME: ERXEOFUtilities
    public static void refreshSharedObjectsWithNames(NSArray names) {
        for (Enumeration e = names.objectEnumerator(); e.hasMoreElements();)
            refreshSharedObjectsWithName((String)e.nextElement());
    }

    private static NSSelector _sharedEntityDataWasRefreshedSelector = new NSSelector("sharedEntityDataWasRefreshed");

    /**
     * Refreshes all of the shared enterprise objects for a given shared entity,
     * then notifies the entity's class by calling the static method
     * sharedEntityDataWasRefreshed() if it implements it.
     *
     * @param entityName name of the shared entity
     */
    // FIXME: Uses default model group, and default shared editing context.
    // MOVEME: ERXEOFUtilities
    public static void refreshSharedObjectsWithName(String entityName) {
        if (entityName == null) {
            throw new IllegalStateException("Entity name argument is null for method: refreshSharedObjectsWithName");
        }
        EOSharedEditingContext sharedEC = EOSharedEditingContext.defaultSharedEditingContext();
        sharedEC.lock();
        try {
            EOEntity entity = ERXEOAccessUtilities.entityNamed(sharedEC, entityName);
            NSArray fetchSpecNames = entity.sharedObjectFetchSpecificationNames();
            int count =  (fetchSpecNames != null) ? fetchSpecNames.count() : 0;

            if ( count > 0 ) { //same check as ERXEOAccessUtilities.entityWithNamedIsShared(), but avoids duplicate work
                for (int index = 0 ; index < count ; ++index) {
                    String oneFetchSpecName = (String)fetchSpecNames.objectAtIndex(index);
                    EOFetchSpecification fs = entity.fetchSpecificationNamed(oneFetchSpecName);
                    if (fs != null) {
                        fs.setRefreshesRefetchedObjects(true);
                        sharedEC.bindObjectsWithFetchSpecification(fs, oneFetchSpecName);
                    }
                }

                //notify the entity class, if it wants to know
                String className = entity.className();
                Class entityClass = Class.forName(className);

                if (_sharedEntityDataWasRefreshedSelector.implementedByClass(entityClass)) {
                    _sharedEntityDataWasRefreshedSelector.invoke(entityClass);
                }
            } else {
                log().warn("Attempting to refresh a non-shared EO: " + entityName);
            }
        } catch (Exception e) {
            throw new NSForwardException(e, "Exception while refreshing shared objects for entity named " + entityName);
        } finally {
            sharedEC.unlock();
        }
    }

    /** Holds a reference to the random object */
    // FIXME: Not a thread safe object, access should be synchronized.
    private static Random _random=new Random();

    /**
     * This method can be used with Direct Action URLs to make sure
     * that the browser will reload the page. This is done by
     * adding the parameter [? | &]r=random_number to the end of the
     * url.
     * @param daURL a url to add the randomization to.
     * @return url with the addition of the randomization key
     */
    // FIXME: Should check to make sure that the key 'r' isn't already present in the url.
    public static String randomizeDirectActionURL(String daURL) {
	synchronized(_random) {
	    int r=_random.nextInt();
	    char c=daURL.indexOf('?')==-1 ? '?' : '&';
	    return  daURL+c+"r="+r;
	}
    }
    /**
     * This method can be used with Direct Action URLs to make sure
     * that the browser will reload the page. This is done by
     * adding the parameter [? | &]r=random_number to the end of the
     * url.
     * @param daURL a url to add the randomization to.
     */
    // FIXME: Should check to make sure that the key 'r' isn't already present in the url.
    public static void addRandomizeDirectActionURL(StringBuffer daURL) {
	synchronized(_random) {
	    int r=_random.nextInt();
	    char c='?';
	    for (int i=0; i<daURL.length(); i++) {
		if (daURL.charAt(i)=='?') {
		    c='&'; break;
		}
	    }
	    daURL.append(c);
	    daURL.append("r=");
	    daURL.append(r);
	}
    }
    /**
     * Adds the wosid for a given session to a given url. 
     * @param url to add wosid form value to.
     * @return url with the addition of wosid form value
     */
    // FIXME: Should check to see if the wosid form value has already been set.
    public static String addWosidFormValue(String url, WOSession s) {
        String result= url;
        if (result!=null && s!=null) {
            result += ( result.indexOf("?") == -1 ? "?" : "&" ) + "wosid=" + s.sessionID();
        } else {
            log().warn("not adding sid: url="+url+" session="+s);
        }
        return result;
    }

    /**
     * Removes any occurances of any strings in the array passed
     * in from the string passed in. Used in conjunction with
     * fuzzy matching.
     * @param newString string to have other strings removed from it
     * @param toBeCleaneds array of strings to check to see if the other
     *		string contains
     * @return cleaned string.
     */
    // MOVEME: Either ERXStringUtilities or fuzzy matching stuff
    // FIXME: Should use a StringBuffer instead of creating strings all over the place.
    public static String cleanString(String newString, NSArray toBeCleaneds) {
        String result=newString;
        if (newString!=null) {
            for(Enumeration e = toBeCleaneds.objectEnumerator(); e.hasMoreElements();){
                String toBeCleaned = (String)e.nextElement();
                if(newString.toUpperCase().indexOf(toBeCleaned)>-1){
                    result=newString.substring(0, newString.toUpperCase().indexOf(toBeCleaned));
                }
            }
        }
        return result;
    }

    /**
     * Uses the <code>setObjectForKey</code> off of the {@link WOSession}
     * class to push a Boolean object onto the session for a given key.
     * Note this is not using key value coding, meaning you don't need
     * to have a boolean instance variable corresponding to the given
     * key on your session object. This flag can be retrieved using
     * the method <code>booleanFlagOnSessionForKeyWithDefault</code>.
     * @param s session object to have the boolean flag set on
     * @param key to be used in the session's dictionary
     * @param newValue boolean value to be set on the session
     */
    public static void setBooleanFlagOnSessionForKey(WOSession s,
                                                     String key,
                                                     boolean newValue) {
        s.setObjectForKey(newValue ? Boolean.TRUE : Boolean.FALSE, key);
    }

    /**
     * Retrieves a value from the session's dictionary and evaulates
     * that object using the method <code>booleanValue</code> off of
     * {@link ERXUtilities}. If there is not an object corresponding
     * to the key passed in, then the default value is returned. The
     * usual way in which boolean values are set on the session object
     * is by using the method <code>setBooleanFlagOnSessionForKey</code>
     * in this class.
     * @param s session object to retrieve the boolean flag from
     * @param key that the boolean is stored under
     * @param defaultValue value to be returned if the object in the
     *		dictionary is null
     * @return boolean value of the object stored in the session's dictionary
     *		for the given key.
     */
    public static boolean booleanFlagOnSessionForKeyWithDefault(WOSession s,
                                                                String key,
                                                                boolean defaultValue) {
        return s.objectForKey(key) != null ? ERXValueUtilities.booleanValue(s.objectForKey(key)) : defaultValue;
    }

    /**
     * Sets the current session for this thread. This is called
     * from {@link ERXSession}'s awake and sleep methods.
     * @param session that is currently active for this thread.
     */
    public synchronized static void setSession(ERXSession session) {
        ERXThreadStorage.takeValueForKey(session, "session");
    }

    /**
     * Returns the current session object for this thread.
     * @return current session object for this thread
     */
    public synchronized static ERXSession session() {
        return (ERXSession)ERXThreadStorage.valueForKey("session");
    }

    /**
     * method used by the preferences mechanism from ERDirectToWeb
     * needs to be in here because shared by ERDirectToWeb and ERCoreBusinessLogic
     * The basic idea of this method is to construct a unique key based on
     * a context.
     * @param key preference key
     * @param context most likely a d2wContext object
     * @return a unique preference key for storing and retriving preferences
     */
    // FIXME: Needs to find a better home.
    public static String userPreferencesKeyFromContext(String key, NSKeyValueCoding context) {
        StringBuffer result=new StringBuffer(key);
        result.append('.');
        String pc=(String)context.valueForKey("pageConfiguration");
        if (pc==null || pc.length()==0) {
            String en="_All_";
            EOEntity e=(EOEntity)context.valueForKey("entity");
            if (e!=null) en=e.name();
            result.append("__");
            result.append(context.valueForKey("task"));
            result.append("_");
            result.append(en);
        } else {
            result.append(pc);
        }
        return result.toString();
    }

    /**
     * Frees all of a resources associated with a given
     * process and then destroys it.
     * @param p process to destroy
     */
    public static void freeProcessResources(Process p) {
        if (p!=null) {
            try {
                if (p.getInputStream()!=null) p.getInputStream().close();
                if (p.getOutputStream()!=null) p.getOutputStream().close();
                if (p.getErrorStream()!=null) p.getErrorStream().close();
                p.destroy();
            } catch (IOException e) {}
        }
    }
    
    /**
     * Determines if a given object implements a method given
     * the name and the array of input parameters.
     * @param object to determine if it implements a method
     * @param methodName name of the method
     * @param parameters array of parameters
     * @return if the object implements a method with the given name
     * 		and class parameters
     */
    public static boolean objectImplementsMethod(Object object, String methodName, Class[] parameters) {
        boolean implementsMethod = false;
        for (Enumeration e = (new NSArray(object.getClass().getMethods())).objectEnumerator(); e.hasMoreElements();) {
            Method m = (Method)e.nextElement();
            if (m.getName().equals(methodName) && m.getParameterTypes().equals(parameters)) {
                implementsMethod = true; break;
            }
        }
        return implementsMethod;
    }

}
