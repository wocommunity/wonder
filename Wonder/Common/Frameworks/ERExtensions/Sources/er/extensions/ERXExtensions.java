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
import com.webobjects.appserver.*;
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
    private static ERXEditingContextDelegate _defaultEditingContextDelegate;
    /** holds references to default editing context delegate without validation */
    private static ERXECNoValidationDelegate _defaultECNoValidationDelegate;

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
            ERXConfigurationManager.defaultManager().configureRapidTurnAround();
            //ERXLog4j.configureLogging(); // Call this again to update configuration from ERConfigurationPath
            //ERXLog4j.configureRapidTurnAround(); // Will only be enabled if WOCaching is off.
            // initialize compiler proxy
            ERXCompilerProxy.defaultProxy().initialize();
            ERXLocalizer.initialize();
            ERXValidationFactory.defaultFactory().configureFactory();
            ERXArrayUtilities.initialize();
        }

        /**
         * This method is called every time a session times
         * out. It allows us to release references to all the
         * editing contexts that were created when that particular
         * session was active.
         * @param n notification that contains the session ID.
         */
        public void sessionDidTimeOut(NSNotification n) {
            String sessionID=(String)n.object();
            ERXExtensions.sessionDidTimeOut(sessionID);
        }
    }
    
    /** We only ever want the framework to be init's once. */
    // CHECKME: This was needed back in the 4.5 days because initialize() might be called multiple
    //          times on a given object (very rare, but boy when it happened it caused problems).
    //		Now with statics I don't think that this is needed.
    private static boolean _isInitialized=false;

    /**
     * Configures the framework. All the bits and pieces that need
     * to be configured are configured, those that need to happen
     * later are delayed by registering an observer for notifications
     * that are posted when the application is finished launching.
     */
    static {
        if (!_isInitialized) {
            try {
                //ERXLog4j.configureLogging();
                ERXLogger.configureLogging(System.getProperties());
                ERXConfigurationManager.defaultManager().initialize();
                log().info("Initializing framework: ERXExtensions");
                // Initing defaultEditingContext delegates
                _defaultEditingContextDelegate = new ERXDefaultEditingContextDelegate();
                _defaultECNoValidationDelegate = new ERXECNoValidationDelegate();
                // CHECKME: This shouldn't be needed now with WO 5
                ERXRetainer.retain(_defaultEditingContextDelegate);
                ERXRetainer.retain(_defaultECNoValidationDelegate);
            
            } catch (Exception e) {
                System.out.println("Caught exception: " + e.getMessage() + " stack: ");
                e.printStackTrace();
            }

            Observer observer = new Observer();
            ERXRetainer.retain(observer); // has to be retained

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
                // CHECKME: I think this bug is fixed
                // This patches shared eo loading so cross model relationships to shared eos work.
                //ERXSharedEOLoader.patchSharedEOLoading();
                NSNotificationCenter.defaultCenter().addObserver(observer,
                                                                 new NSSelector("didSave", ERXConstant.NotificationClassArray),
                                                                 EOEditingContext.EditingContextDidSaveChangesNotification,
                                                                 null);
                NSNotificationCenter.defaultCenter().addObserver(observer,
                                                                 new NSSelector("finishedLaunchingApp", ERXConstant.NotificationClassArray),
                                                                 WOApplication.ApplicationDidFinishLaunchingNotification,
                                                                 null);
                NSNotificationCenter.defaultCenter().addObserver(observer,
                                                                 new NSSelector("sessionDidTimeOut", ERXConstant.NotificationClassArray),
                                                                 WOSession.SessionDidTimeOutNotification,
                                                                 null);
                _isInitialized=true;
            } catch (Exception e) {
                System.out.println("Caught exception: " + e.getMessage() + " stack: ");
                e.printStackTrace();
            }
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
            if (adaptorLogger.isDebugEnabled() && !NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration)) {
                NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration);
                NSLog.setAllowedDebugLevel(NSLog.DebugLevelInformational);
            }
            adaptorEnabled = NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration) ? Boolean.TRUE : Boolean.FALSE;
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
            if (NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration) != targetState.booleanValue())
                if (targetState.booleanValue()) {
                    NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration);
                    NSLog.setAllowedDebugLevel(NSLog.DebugLevelInformational);
                } else {
                    NSLog.refuseDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration);
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

    // MOVEME: All of these editing context creation methods and delegates should be moved to
    //		ERXECFactory
    /**
     * Returns the default editing context delegate.
     * This delegate is used by default for all editing
     * contexts that are created.
     * @return the default editing context delegate
     */
    // MOVEME: ERXECFactory
    public static ERXEditingContextDelegate defaultEditingContextDelegate() { return _defaultEditingContextDelegate; }

    /**
     * Sets the default editing context delegate to be
     * used for editing context creation.
     * @param delegate to be set on every created editing
     *		context by default.
     */
     // ENHANCEME: Shouldn't require the editing context delegate to be a subclass of ours.
    public static void setDefaultEditingContextDelegate(ERXEditingContextDelegate delegate) {
        _defaultEditingContextDelegate = delegate;
    }

    /**
     * Default delegate that does not perform validation.
     * Not performing validation can be a good thing when
     * using nested editing contexts as sometimes you only
     * want to validation one object, not all the objects.
     * @returns default delegate that doesn't perform validation
     */
    // MOVEME: ERXECFactory
    public static ERXECNoValidationDelegate defaultECNoValidationDelegate() { return _defaultECNoValidationDelegate; }

    /**
     * Sets the default editing context delegate to be
     * used for editing context creation that does not
     * allow validation.
     * @param delegate to be set on every created editing
     *		context that doesn't allow validation.
     */
     // ENHANCEME: Shouldn't require the editing context delegate to be a subclass of ours.
    public static void setDefaultECNoValidationDelegate(ERXECNoValidationDelegate delegate) {
        _defaultECNoValidationDelegate = delegate;
    }

    public final static String objectsWillChangeInEditingContext= "ObjectsWillChangeInEditingContext";

    /**
     * Factory method to create a new editing context. Sets
     * the current default delegate on the newly created
     * editing context.
     * @return a newly created editing context with the
     *		default delegate set.
     */
    // MOVEME: ERXECFactory
    public static EOEditingContext newEditingContext() {
        return ERXExtensions.newEditingContext(EOEditingContext.defaultParentObjectStore(), true);
    }

    /**
     * Factory method to create a new editing context with
     * validation disabled. Sets the default no validation
     * delegate on the editing context. Becareful an
     * editing context that does not perform validation
     * means that none of the usual validation methods are
     * called on the enterprise objects before they are saved
     * to the database.
     * @param validation flag that determines if validation
     *		should or should not be enabled.
     * @return a newly created editing context with a delegate
     *		set that has disabled validation.
     */
    // MOVEME: ERXECFactory
    public static EOEditingContext newEditingContext(boolean validation) {
        return ERXExtensions.newEditingContext(EOEditingContext.defaultParentObjectStore(), validation);
    }

    /**
     * Creates a new editing context with the specified object
     * store as the parent object store. This method is useful
     * when creating nested editing contexts. After creating
     * the editing context the default delegate is set on the
     * editing context.<br/>
     * <br/>
     * Note: an {@link EOEditingContext} is a subclass of EOObjectStore
     * so passing in another editing context to this method is
     * completely kosher.
     * @param objectStore parent object store for the newly created
     *		editing context.
     * @return new editing context with the given parent object store
     */
    // MOVEME: ERXECFactory
    public static EOEditingContext newEditingContext(EOObjectStore objectStore) {
        return ERXExtensions.newEditingContext(objectStore, true);
    }

    /**
     * Utility method used to execute arbitrary SQL. This
     * has the advantage over the 
     * {@link com.webobjects.eoaccess.EOUtilities EOUtilities}
     * <code>rawRowsForSQL</code> in that it can be used with
     * other statements besides just SELECT without throwing
     * exceptions.
     * @param entityName name of an entity in the model connected
     *		to the database you wish to execute SQL against
     * @param exp SQL expression
     * @param ec editing context that determines which model group
     *		and database context to use.
     */
    // FIXME: To conform to naming conventions, should have the ec as the first parameter.
    //		also should be passing in the model name and not an entityName
    // ENHANCEME: Should support the use of bindings
    // MOVEME: ERXEOFUtilities
    public static void evaluateSQLWithEntityNamed(String entityName, String exp, EOEditingContext ec) {
        // FIXME: Should be getting the model group from the ec
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        EODatabaseContext dbContext = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), ec);
        EOAdaptorChannel adaptorChannel = dbContext.availableChannel().adaptorChannel();
        if (!adaptorChannel.isOpen())
            adaptorChannel.openChannel();
        EOSQLExpressionFactory factory=adaptorChannel.adaptorContext().adaptor().expressionFactory();
        adaptorChannel.evaluateExpression(factory.expressionForString(exp));
    }

    /**
     * Retaining the editing contexts explicitly until the session that was active
     * when they were created goes away
     * this hopefully will go some way towards avoiding the 'attempted to send
     * message to EO whose EditingContext is gone. 
     */
    // FIXME: This feature/bug fix should be able to be disabled
    private static NSMutableDictionary _editingContextsPerSession=new NSMutableDictionary();

    /**
     * Creates a new editing context with the specified object
     * store as the parent object store and with validation turned
     * on or off depending on the flag passed in. This method is useful
     * when creating nested editing contexts. After creating
     * the editing context the default delegate is set on the
     * editing context if validation is enabled or the default no
     * validation delegate is set if validation is disabled.<br/>
     * <br/>
     * Note: an {@link EOEditingContext} is a subclass of EOObjectStore
     * so passing in another editing context to this method is
     * completely kosher.
     * @param objectStore parent object store for the newly created
     *		editing context.
     * @param validation determines if the editing context should perform
     *		validation
     * @return new editing context with the given parent object store
     *		and the delegate corresponding to the validation flag
     */
    // MOVEME: ERXECFactory
    public static EOEditingContext newEditingContext(EOObjectStore objectStore, boolean validation) {
        EOEditingContext ec = new EOEditingContext(objectStore);
        ERXExtensions.setDefaultDelegate(ec, validation);
        WOSession s=session();
        if (s!=null) {
            NSMutableArray a=(NSMutableArray)_editingContextsPerSession.objectForKey(s.sessionID());
            if (a==null) {
                a=new NSMutableArray();
                _editingContextsPerSession.setObjectForKey(a,s.sessionID());
                if (log().isDebugEnabled()) log().debug("Creating array for "+s.sessionID());
            }
            a.addObject(ec);
            if (log().isDebugEnabled()) log().debug("Added new ec to array for "+s.sessionID());
        } else if (log().isDebugEnabled()) {
            log().debug("Editing Context created with null session.");
        }
        return ec;
    }

    /**
     * This method is called when a session times out.
     * Calling this method will release references to
     * all editing contexts that were created when this
     * session was active.
     * @param sessionID of the session that timed out
     */
    public static void sessionDidTimeOut(String sessionID) {
        if (sessionID!=null) {
            if (log().isDebugEnabled()) {
                NSArray a=(NSArray)_editingContextsPerSession.objectForKey(sessionID);
                log().debug("Session "+sessionID+" is timing out ");
                log().debug("Found "+ ((a == null) ? 0 : a.count()) + " editing context(s)");
            }
            // FIXME: Should help things along by calling dispose on all the ecs being held here.
            _editingContextsPerSession.removeObjectForKey(sessionID);
        }
    }

    /**
     * Sets the default editing context delegate on
     * the given editing context.
     * @param ec editing context to have it's delegate set.
     */
    // FIXME: Misleading name, sounds like we are setting the actual delegate and not setting the delegate on
    //		an editing context
    // MOVEME: ERXECFactory
    public static void setDefaultDelegate(EOEditingContext ec) { ERXExtensions.setDefaultDelegate(ec, true); }

    /**
     * Sets either the default editing context delegate
     * that does or does not allow validation based on
     * the validation flag passed in on the given editing context.
     * @param ec editing context to have it's delegate set.
     * @param validation flag that determines if the editing context
     * 		should perform validation on objects being saved.
     */
    // FIXME: Misleading name, sounds like we are setting the actual delegate and not setting the delegate on
    //		an editing context
    // MOVEME: ERXECFactory
    public static void setDefaultDelegate(EOEditingContext ec, boolean validation) {
        log().debug("Setting default delegate...");
        if (ec != null) {
            if (validation)
                ec.setDelegate(ERXExtensions.defaultEditingContextDelegate());
            else
                ec.setDelegate(ERXExtensions.defaultECNoValidationDelegate());
        }
    }

    // DELETEME: This is duplicated in ERXUtilities
    public static EOArrayDataSource dataSourceForArray(NSArray array) {
        EOArrayDataSource dataSource = null;
        if (array != null && array.count() > 0) {
            EOEnterpriseObject eo = (EOEnterpriseObject)array.objectAtIndex(0);
            dataSource = new EOArrayDataSource(eo.classDescription(), eo.editingContext());
            dataSource.setArray(array);
        }
        return dataSource;
    }

    // DELETEME: This is duplicated in ERXUtilities
    public static NSArray arrayFromDataSource(EODataSource dataSource) {
        WODisplayGroup dg = new WODisplayGroup();
        dg.setDataSource(dataSource);
        dg.fetch(); // Have to fetch in the array, go figure.
        return dg.allObjects();
    }

    /**
     * Creates a detail data source for a given enterprise
     * object and a relationship key. These types of datasources
     * can be very handy when you are displaying a list of objects
     * a la D2W style and then some objects are added or removed
     * from the relationship. If an array datasource were used
     * then the list would not reflect the changes made, however
     * the detail data source will reflect changes made to the
     * relationship.<br/>
     * Note: the relationship key does not have to be an eo
     * relationship, instead it just has to return an array of
     * enterprise objects.
     * @param object that has the relationship
     * @param key relationship key
     * @return detail data source for the given object-key pair.
     */
    public static EODetailDataSource dataSourceForObjectAndKey(EOEnterpriseObject object, String key) {
        EODetailDataSource eodetaildatasource = new EODetailDataSource(EOClassDescription.classDescriptionForEntityName(object.entityName()), key);
        eodetaildatasource.qualifyWithRelationshipKey(key, object);
        return eodetaildatasource;
    }

    /**
     * Displays a list of attributes off of enterprise
     * objects in a 'friendly' manner. <br/>
     * <br/>
     * For example, given an array containing three user
     * objects and the attribute key "firstName", the
     * result of calling this method would be the string:
     * "Max, Anjo and Patrice".
     * @param list of objects to be displayed in a friendly
     *		manner
     * @param attribute key to be called on each object in
     *		the list
     * @param nullArrayDisplay string to be returned if the
     *		list is null or empty
     * @param friendly display string
     */
    // FIXME: Localization, also doesn't have to be eos. if attribute isn't specified should default to toString
    public static String friendlyEOArrayDisplayForKey(NSArray list, String attribute, String nullArrayDisplay) {
        String result=null;
        int count = list!=null ? list.count() : 0;
        if (count==0) {
            result=nullArrayDisplay;
        } else if (count == 1) {
            result= (String) (attribute!= null ? ((EOEnterpriseObject)list.objectAtIndex(0)).valueForKeyPath(attribute) :
                              ((EOEnterpriseObject)list.objectAtIndex(0)).toString());
        } else if (count > 1) {
            StringBuffer buffer = new StringBuffer();
            for(int i = 0; i < count; i++) {
                String attributeValue =  (attribute!= null ? (String) ((EOEnterpriseObject)list.objectAtIndex(i)).valueForKeyPath(attribute) :
                                          ((EOEnterpriseObject)list.objectAtIndex(i)).toString());
                if (i == 0) {
                    buffer.append(attributeValue);
                } else if  (i == (count - 1)) {
                    buffer.append(" and " + attributeValue);
                } else {
                    buffer.append(", " + attributeValue);
                }
            }
            result=buffer.toString();
        }
        return result;
    }

    /**
     * Replaces a given string by another string in a string.
     * This method is just a cover method for calling the
     * same method in {@link ERXSimpleHTMLFormatter}.
     * @param old string to be replaced
     * @param newString to be inserted
     * @param s string to have the replacement done on it
     * @return string after having all of the replacement done.
     */
    // MOVEME: ERXStringUtilities
    public static String replaceStringByStringInString(String old, String newString, String s) {
        return ERXStringUtilities.replaceStringByStringInString(old,newString,s);
    }

    /**
     * Simple utility method that will return the
     * string "" if the string passed in is null
     * otherwise it will return the passed in
     * string.
     * @param s string to test
     * @return "" if the string is null else the string
     */
    // MOVEME: ERXStringUtilities
    public static String emptyStringForNull(String s) {
        return s==null ? "" : s;
    }
    
    /**
     * Simple utility method that will return the
     * string "" if the object passed in is null
     * otherwise it will return the passed in
     * object with toString called on it.
     * @param o object to test
     * @return "" if the object is null else <code>toString</code>
     *		on the object.
     */
    // MOVEME: ERXStringUtilities
    public static String emptyStringForNull(Object o) {
        return o==null ? ""  : emptyStringForNull(o.toString());
    }

    /**
     * Simple utility method that will return null
     * if the string passed in is equal to ""
     * otherwise it will return the passed in
     * string.
     * @param s string to test
     * @return null if the string is "" else the string.
     */
    // MOVEME: ERXStringUtilities
    public static String nullForEmptyString(String s) {
        return s==null ? null : (s.length()==0 ? null : s);
    }

    /**
     * Simple utility method that will return null
     * if the <code>toString</code> method off of
     * the object is equal to "" otherwise it will
     * return the passed in object with toString
     * called on it.
     * @param o object to test
     * @return null if the object's <code>toString</code>
     *		is equal to "" or the value of
     *		<code>toString</code>on the object.
     */
    // MOVEME: ERXStringUtilities
    public static String nullForEmptyString(Object o) {
        return o==null ? null : nullForEmptyString(o.toString());
    }

    /**
     * Simple test if the string is either null or
     * equal to "".
     * @param s string to test
     * @return result of the above test
     */
    // MOVEME: ERXStringUtilities
    public static boolean stringIsNullOrEmpty(String s) {
        return ((s == null) || (s.length() == 0));
    }

    /**
     * Counts the number fo occurrences of a particular
     * <code>char</code> in a given string.
     * @param c char to count in string
     * @param s string to look for specified char in.
     * @return number of occurences of a char in the string
     */
    // MOVEME: ERXStringUtilities
    public static int numberOfOccurrencesOfCharInString(char c, String s) {
        int result=0;
        if (s!=null) {
            for (int i=0; i<s.length();)
                if (s.charAt(i++)==c) result++;
        }
        return result;
    }

    /**
     * String multiplication.
     * @param n the number of times to concatinate a given string
     * @param s string to be multipled
     * @return multiplied string
     */
    // MOVEME: ERXStringUtilities
    public static String stringWithNtimesString(int n, String s) {
        StringBuffer sb=new StringBuffer();
        for (int i=0; i<n; i++) sb.append(s);
        return sb.toString();
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
    // ENHANCEME: Should be able to differentiate between a deleted eo and a new eo by looking
    //		  at the EOGlobalID
    // MOVEME: ERXEOFUtilities (when we have them)
    public static boolean isNewObject(EOEnterpriseObject eo) {
        return eo.editingContext()==null || eo.editingContext().insertedObjects().containsObject(eo);
    }

    /**
     * Returns the string representation of the primary key for
     * a given object. Note that the object should only have
     * one primary key.
     * @param eo object to get the primary key for.
     * @return string representation of the primary key of the
     *		object.
     */
    // MOVEME: ERXEOFUtilities
    public static String primaryKeyForObject(EOEnterpriseObject eo) {
        Object pk=rawPrimaryKeyForObject(eo);
        return pk!=null ? pk.toString() : null;
    }

    // DELETEME: This one is very confusing, plus the primaryKey dictionary isn't even used?!?!
    public static Object rawPrimaryKeyFromPrimaryKeyAndEO(NSDictionary primaryKey, EOEnterpriseObject eo) {
        NSArray result = primaryKeyArrayForObject(eo);

        if(result!=null && result.count() == 1) {
            return result.lastObject();
        } else if (result!=null && result.count() > 1) {
            log().warn("Attempting to get a raw primary key from an object with a compound key: " + eo);
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
    // FIXME: Bad model group, should ensure local instances of objects
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
            EOEntity e=EOModelGroup.defaultGroup().entityNamed(from.entityName());
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
     * ERXArrayUtilities.flatten} instead.
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
     * Filters an array using the {@link EOQualifierEvaluation} interface.
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
    // MOVEME: ERXFileUtilities
    public static byte[] bytesFromFile(File f) throws IOException {
        if (f == null)
            throw new IOException("null file");
        int size = (int) f.length();
        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[size];
        int bytesRead = 0;
        while (bytesRead < size)
            bytesRead += fis.read(data, bytesRead, size - bytesRead);
        fis.close();
        return data;
    }

    /**
     * Returns a string from the file using the default
     * encoding.
     * @param f file to read
     * @return string representation of that file.
     */
    // MOVEME: ERXFileUtilities
    public static String stringFromFile(File f) throws IOException {
        return new String(bytesFromFile(f));
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
        return new String(bytesFromFile(f), encoding);
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
        long lastModified = 0;
        String filePath = WOApplication.application().resourceManager().pathForResourceNamed(fileName,
                                                                                             frameworkName,
                                                                                             null);
        if (filePath!=null) {
            lastModified = new File(filePath).lastModified();
        }
        return lastModified;
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
        return readPropertyListFromFileInFramework(fileName, aFrameWorkName, null);
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
    public static Object readPropertyListFromFileInFramework(String fileName, String aFrameWorkName, NSArray languageList) {
        String filePath = WOApplication.application().resourceManager().pathForResourceNamed(fileName,
                                                                                             aFrameWorkName,
                                                                                             languageList);
        Object result=null;
        if (filePath!=null) {
            File file = new File(filePath);
            try {
                try {
                    result=NSPropertyListSerialization.propertyListFromString(stringFromFile(file));
                } catch (IllegalArgumentException iae) {
                    result=NSPropertyListSerialization.propertyListFromString(stringFromFile(file, "UTF-16"));
                }
            } catch (IOException ioe) {
                log().error("ConfigurationManager: Error reading "+filePath);
            }
        }
        return result;
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
    // FIXME: Shouldn't be using the defaultModel group, should be getting it
    //		from the rootObjectStoreCoordintator
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
            entity=EOModelGroup.defaultGroup().entityNamed(entityName);
            lastKey=key;
        } else {
            String partialKeyPath=ERXStringUtilities.keyPathWithoutLastProperty(key);
            EOEnterpriseObject objectForPropertyDisplayed=(EOEnterpriseObject)object.valueForKeyPath(partialKeyPath);
            if (objectForPropertyDisplayed!=null) {
                entity=EOModelGroup.defaultGroup().entityNamed(objectForPropertyDisplayed.entityName());
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

    // DELETEME: duplicate method friendlyEOArrayDisplayForKey
    public static String userPresentableEOArray(NSArray array, String attribute) {
        String userPresentable = "";
        if (array != null && array.count() > 0) {
            if (attribute == null)
                attribute = "description";
            if (array.count() > 1) {
                NSArray subArray = array.subarrayWithRange(new NSRange(0, array.count() - 1));
                userPresentable = ((NSArray)subArray.valueForKey(attribute)).componentsJoinedByString(", ") + " and ";
            }
            userPresentable += ((NSKeyValueCoding)array.lastObject()).valueForKey(attribute);
        }
        return userPresentable;
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

    /**
     * Useful method to refresh all of the shared enterprise objects
     * for a given shared entity. The current implementation depends
     * on the shared entity to have a fetch specification named 'FetchAll'
     * which will be created for you if you check the box that says
     * 'share all objects'.
     * @param entityName name of the shared entity
     */
    // FIXME: Only works with shared objects that share all of their objects, i.e. has a FetchAll fetch spec
    //		also only works with the global model group.
    // CHECKME: Should check that this still works under WO 5
    // MOVEME: ERXEOFUtilities
    public static void refreshSharedObjectsWithName(String entityName) {
        EOEditingContext peer = ERXExtensions.newEditingContext();
        peer.setSharedEditingContext(null);
        EOFetchSpecification fetchAll = EOModelGroup.defaultGroup().entityNamed(entityName).fetchSpecificationNamed("FetchAll");
        if (fetchAll != null) {
            // Need to refault all the shared EOs first.
            for (Enumeration e = EOUtilities.objectsForEntityNamed(peer, entityName).objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                peer.rootObjectStore().refaultObject(eo, peer.globalIDForObject(eo), peer);
            }
            fetchAll.setRefreshesRefetchedObjects(true);
            peer.objectsWithFetchSpecification(fetchAll);
        } else {
            log().warn("Attempting to refresh a non-shared EO: " + entityName);
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
        int r=_random.nextInt();
        char c=daURL.indexOf('?')==-1 ? '?' : '&';
        return  daURL+c+"r="+r;
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
        return s.objectForKey(key) != null ? ERXUtilities.booleanValue(s.objectForKey(key)) : defaultValue;
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
