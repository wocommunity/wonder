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
import com.webobjects.directtoweb.*;
import java.lang.reflect.Method;
import java.lang.*;
import java.util.*;
import java.io.*;
import org.apache.log4j.Category;

/**
 * Principle class of the ERExtensions framework. This class
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
    private static Category _cat;
    /**
     * creates and caches the logging logger
     * @return logging logger
     */
    public static Category cat() {
        if (_cat == null)
            _cat = Category.getInstance(ERXExtensions.class);
        return _cat;
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
            ERXLog4j.configureRapidTurnAround(); // Will only enable if WOCaching is off.
            ERXSession.registerNotifications();
 	    // initialize compiler proxy
	    ERXCompilerProxy.defaultProxy().initialize();
            ERXLocalizer.initialize();
            ERXValidationFactory.defaultFactory().configureFactory();
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
            // This will configure the Log4j system.
            // This is OK to call multiple times as it will only be configured the first time.
            try {
                ERXLog4j.configureLogging();
                ERXConfigurationManager.initializeDefaults();
                cat().info("Initializing framework: ERXExtensions");
                // Initing defaultEditingContext delegates
                _defaultEditingContextDelegate = new ERXDefaultEditingContextDelegate();
                _defaultECNoValidationDelegate = new ERXECNoValidationDelegate();
                // CHECKME: This shouldn't be needed now with WO 5
                ERXRetainer.retain(_defaultEditingContextDelegate);
                ERXRetainer.retain(_defaultECNoValidationDelegate);

                Observer observer = new Observer();
                ERXRetainer.retain(observer); // has to be retained
                ERXExtensions.configureAdaptorContextRapidTurnAround(observer);
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
    public static Category adaptorCategory;
    /** logging support for shared object loading */
    public static Category sharedEOAdaptorCategory;
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
     * change a logger category and have that changed value change
     * the NSLog setting to log the generated SQL. This method is
     * called as part of the framework initialization process.
     * @param observer object to register the call back with.
     */
    // FIXME: This shouldn't be enabled when the application is in production.
    // FIXME: Now that all of the logging has been centralized, we should just be able
    //		to do something like this, but much more generic, i.e. have a mapping
    //		between category names and NSLog groups, for example com.webobjects.logging.DebugGroupSQLGeneration we should
    //		be able to get the last part of the logger name and look up that log group and turn 
    public static void configureAdaptorContextRapidTurnAround(Object observer) {
        if (!_isConfigureAdaptorContextRapidTurnAround) {
            // This allows enabling from the log4j system.
            adaptorCategory = Category.getInstance("er.transaction.adaptor.EOAdaptorDebugEnabled");
            sharedEOAdaptorCategory = Category.getInstance("er.transaction.adaptor.EOSharedEOAdaptorDebugEnabled");
            if (adaptorCategory.isDebugEnabled() && !NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration)) {
                NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration);
                NSLog.setAllowedDebugLevel(NSLog.DebugLevelInformational);
            }
            adaptorEnabled = NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration) ? Boolean.TRUE : Boolean.FALSE;
                                          // Allows rapid turn-around of adaptor debugging.
            NSNotificationCenter.defaultCenter().addObserver(observer,
                                                             new NSSelector("configureAdaptorContext", ERXConstant.NotificationClassArray),
                                                             ERXLog4j.ConfigurationDidChangeNotification,
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
        if (adaptorCategory.isDebugEnabled() && !adaptorEnabled.booleanValue()) {
            targetState = Boolean.TRUE;
        } else if (!adaptorCategory.isDebugEnabled() && adaptorEnabled.booleanValue()) {
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
                adaptorCategory.info("Adaptor debug on");
            } else {
                adaptorCategory.info("Adaptor debug off");
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
     * Default delegate that does not perform validation.
     * Not performing validation can be a good thing when
     * using nested editing contexts as sometimes you only
     * want to validation one object, not all the objects.
     * @returns default delegate that doesn't perform validation
     */
    // MOVEME: ERXECFactory
    public static ERXECNoValidationDelegate defaultECNoValidationDelegate() { return _defaultECNoValidationDelegate; }

    // DELETEME: Not a real notification, don't think it is being used.
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
     * has the advantage over the {@link EOUtilities}
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
                if (cat().isDebugEnabled()) cat().debug("Creating array for "+s.sessionID());
            }
            a.addObject(ec);
            if (cat().isDebugEnabled()) cat().debug("Added new ec to array for "+s.sessionID());
        } else if (cat().isDebugEnabled()) {
            cat().debug("Editing Context created with null session.");
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
            if (cat().isDebugEnabled()) {
                NSArray a=(NSArray)_editingContextsPerSession.objectForKey(sessionID);
                cat().debug("Session "+sessionID+" is timing out ");
                cat().debug("Found "+ ((a == null) ? 0 : a.count()) + " editing context(s)");
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
        return ERXSimpleHTMLFormatter.replaceStringByStringInString(old,newString,s);
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
        if (cat().isDebugEnabled()) cat().debug("Forcing full Garbage Collection");
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
    // ENHANCEME: Should be able to differentuate between a deleted eo and a new eo by looking
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
            cat().warn("Attempting to get a raw primary key from an object with a compound key: " + eo);
        }
        return result;
    }

    /**
     * Gives the primary key array for a given enterprise
     * object. This has the advantage of not firing the
     * fault of the object, unlike the method in {@link EOUtilities}.
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
                String partialKeyPath=KeyValuePath.keyPathWithoutLastProperty(keyPath);
                from=(EOEnterpriseObject)from.valueForKeyPath(partialKeyPath);
                keyPath=KeyValuePath.lastPropertyKeyInKeyPath(keyPath);
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
     * <br/>
     * For example:<br/>
     * <code>NSArray foos;</code> //Assume exists<br/>
     * <code>NSArray bars = (NSArray)foos.valueForKey("toBars");</code>
     * In this case if <code>foos</code> contained five elements
     * then the array <code>bars</code> will contain five arrays
     * each corresponding to what <code>aFoo.toBars</code> would
     * return. To have the entire collection of <code>bars</code>
     * in one single arra you would call:
     * <code>NSArray allBars = flatten(bars)</code>
     * @param array to be flattened
     * @return an array containing all of the elements from
     *		all of the arrays contained within the array
     *		passed in.
     */
    // ENHANCEME: Should add option to filter duplicates
    // MOVEME: ERXArrayUtilities
    public static NSArray flatten(NSArray array) {
        NSMutableArray newArray=null;
        for (int i=0; i<array.count(); i++) {
            Object element=array.objectAtIndex(i);
            if (element instanceof NSArray) {
                if (newArray==null) {
                    newArray=new NSMutableArray();
                    for (int j=0; j<i; j++) {
                        if(array.objectAtIndex(j)!=null){
                            newArray.addObject(array.objectAtIndex(j));
                        }
                    }
                }
                NSArray a=flatten((NSArray)element);
                for (int j=0; j<a.count();j++) {
                    if(a.objectAtIndex(j)!=null){
                        newArray.addObject(a.objectAtIndex(j));
                    }
                }
            }
        }
        return (newArray !=null) ? newArray : array;
    }

    /**
     * Holds the null grouping key for use when grouping objects
     * based on a key that might return null and nulls are allowed
     */  
    public static final String NULL_GROUPING_KEY="**** NULL GROUPING KEY ****";

    /**
     * Groups an array of objects by a given key path. The dictionary
     * that is returned contains keys that correspond to the grouped
     * keys values. This means that the object pointed to by the key
     * path must be a cloneable object. For instance using the key path
     * 'company' would not work because enterprise objects are not
     * cloneable. Instead you might choose to use the key path 'company.name'
     * of 'company.primaryKey', if your enterprise objects support this
     * see {@link ERXGenericRecord} if interested.
     * @param eos array of objects to be grouped
     * @param keyPath path used to group the objects.
     * @return a dictionary where the keys are the grouped values and the
     * 		objects are arrays of the objects that have the grouped
     *		characteristic. Note that if the key path returns null
     *		then one of the keys will be the static ivar NULL_GROUPING_KEY
     */
    // ENHANCEME: Doesn't have to be just eos ...
    // MOVEME: ERXDictionaryUtilities or ERXArrayUtilities
    public static NSDictionary eosInArrayGroupedByKeyPath(NSArray eos, String keyPath) {
        return eosInArrayGroupedByKeyPath(eos,keyPath,true,null);
    }

    /**
     * Groups an array of objects by a given key path. The dictionary
     * that is returned contains keys that correspond to the grouped
     * keys values. This means that the object pointed to by the key
     * path must be a cloneable object. For instance using the key path
     * 'company' would not work because enterprise objects are not
     * cloneable. Instead you might choose to use the key path 'company.name'
     * of 'company.primaryKey', if your enterprise objects support this
     * see {@link ERXGenericRecord} if interested.
     * @param eos array of objects to be grouped
     * @param keyPath path used to group the objects.
     * @param includeNulls determines if keyPaths that resolve to null
     *		should be allowed into the group.
     * @param extraKeyPathForValues allows a selected object to include
     *		more objects in the group. This is going away in the
     *		future.
     * @return a dictionary where the keys are the grouped values and the
     * 		objects are arrays of the objects that have the grouped
     *		characteristic. Note that if the key path returns null
     *		then one of the keys will be the static ivar NULL_GROUPING_KEY
     */
    // ENHANCEME: Doesn't have to be just eos ...
    // MOVEME: ERXDictionaryUtilities or ERXArrayUtilities
    // FIXME: Get rid of extraKeyPathForValues, it doesn't make sense.
    public static NSDictionary eosInArrayGroupedByKeyPath(NSArray eos,
                                                          String keyPath,
                                                          boolean includeNulls,
                                                          String extraKeyPathForValues) {
        NSMutableDictionary result=new NSMutableDictionary();
        for (Enumeration e=eos.objectEnumerator(); e.hasMoreElements();) {
            EOEnterpriseObject eo=(EOEnterpriseObject)e.nextElement();
            Object key=eo.valueForKeyPath(keyPath);
            boolean isNullKey = key==null || key instanceof NSKeyValueCoding.Null;
            if (!isNullKey || includeNulls) {
                if (isNullKey) key=NULL_GROUPING_KEY;
                NSMutableArray existingGroup=(NSMutableArray)result.objectForKey(key);
                if (existingGroup==null) {
                    existingGroup=new NSMutableArray();
                    result.setObjectForKey(existingGroup,key);
                }
                if (extraKeyPathForValues!=null) {
                    Object value=((EOEnterpriseObject)eo).valueForKeyPath(extraKeyPathForValues);
                    if (value!=null) existingGroup.addObject(value);
                } else
                    existingGroup.addObject(eo);
            }
        }
        return result;
    }

    /**
     * Simple comparision method to see if two array
     * objects are identical sets.
     * @param a1 first array
     * @param a2 second array
     * @return result of comparison
     */
    // MOVEME: ERXArrayUtilities
    public static boolean arraysAreIdenticalSets(NSArray a1, NSArray a2) {
        boolean result=false;
        for (Enumeration e=a1.objectEnumerator();e.hasMoreElements();) {
            Object i=e.nextElement();
            if (!a2.containsObject(i)) {
                result=false; break;
            }
        }
        if (result) {
            for (Enumeration e=a2.objectEnumerator();e.hasMoreElements();) {
                Object i=e.nextElement();
                if (!a1.containsObject(i)) {
                    result=false; break;
                }
            }
        }
        return result;
    }

    /**
     * Filters an array using the {@link EOQualifierEvaluation} interface.
     * @param a array to be filtered
     * @param q qualifier to do the filtering
     * @return array of filtered results.
     */
    // CHECKME: Is this a value add? EOQualifier has filteredArrayWithQualifier
    // MOVEME: ERXArrayUtilities
    public static NSArray filteredArrayWithQualifierEvaluation(NSArray a, EOQualifierEvaluation q) {
        NSMutableArray result=null;
        if (a!=null) {
            result=new NSMutableArray();
            for (Enumeration e=a.objectEnumerator(); e.hasMoreElements();) {
                Object o=e.nextElement();
                if (q.evaluateWithObject(o)) result.addObject(o);
            }
        }
        return result;
    }

    /**
     * Removes an array of keys from a dictionary and
     * returns the result.
     * @param d dictionary to be pruned
     * @param a array of keys to be pruned
     * @return pruned dictionary
     */
    // MOVEME: ERXDictionaryUtilities
    public static NSDictionary dictionaryByRemovingFromDictionaryKeysInArray(NSDictionary d, NSArray a) {
        NSMutableDictionary result=new NSMutableDictionary();
        if (d!=null && a!=null) {
            for (Enumeration e=d.allKeys().objectEnumerator();e.hasMoreElements();) {
                String key=(String)e.nextElement();
                if (!a.containsObject(key))
                    result.setObjectForKey(d.objectForKey(key),key);
            }
        }
        return result;
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
    // CHECKME: Anyone know the reason why this has final in the method
    //		signature?
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
                cat().error("ConfigurationManager: Error reading "+filePath);
            }
        }
        return result;
    }

    // DELETEME: Not sure exactly what to do with this one, tempted just to delete it
    //		to get rid of the D2W dependency. Could instead use the class description's
    //		basic name beautifier and have that be localized.
    public static String displayNameForPropertyKey(String key, String entityName) {
        D2WContext context = ((ERXApplication)WOApplication.application()).d2wContext();
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        context.setEntity(entity);
        // grosse but efficient hack -- force the computation of pageConfiguration
        // so that caching works correctly -- saves us from having to add entity as a significant key
        // Not needed anymore.
        //context.valueForKey("pageConfiguration");
        context.setPropertyKey(key);
        return context.displayNameForProperty();
    }

    // DELETEME: No real value add
    public static Object configurationForKey(String key) {
        return System.getProperty(key) != null ?
        System.getProperty(key) : ERXExtensions.d2wContextValueForKey(key);
    }

    // DELETEME: Shouldn't have this depenedency
    public static Object d2wContextValueForKey(String key) {
        return key != null ? ((ERXApplication)WOApplication.application()).d2wContext().valueForKey(key) : null;
    }

    // DELETEME: Shouldn't have this dependency.
    public static Object d2wContextValueForKey(String key, String entityName) {
        D2WContext context = ((ERXApplication)WOApplication.application()).d2wContext();
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        context.setEntity(entity);
        // grosse but efficient hack -- force the computation of pageConfiguration
        // so that caching works correctly -- saves us from having to add entity as a significant key
        //System.out.println("pageConfig="+context.valueForKey("pageConfiguration"));
        return context.valueForKey(key);
    }

    // DELETEME: This is silly.
    public static String createConfigurationForEntityNamed(String entityName) {
        D2WContext context = ((ERXApplication)WOApplication.application()).d2wContext();
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        context.setEntity(entity);
        // grosse but efficient hack -- force the computation of pageConfiguration
        // so that caching works correctly -- saves us from having to add entity as a significant key
        context.valueForKey("pageConfiguration");
        return (String)context.valueForKey("createConfigurationNameForEntity");
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
            String partialKeyPath=KeyValuePath.keyPathWithoutLastProperty(key);
            EOEnterpriseObject objectForPropertyDisplayed=(EOEnterpriseObject)object.valueForKeyPath(partialKeyPath);
            if (objectForPropertyDisplayed!=null) {
                entity=EOModelGroup.defaultGroup().entityNamed(objectForPropertyDisplayed.entityName());
                lastKey=KeyValuePath.lastPropertyKeyInKeyPath(key);
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
            String PropertyKeyWithoutLastProperty = KeyValuePath.keyPathWithoutLastProperty(prefixKeyPath);
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
     * Note: The current implementation does not preserve
     * 		the order of elements in the array.
     * @param anArray to be filtered
     * @return filtered array.
     */
    // FIXME: Does not preserve array order
    // MOVEME: ERXArrayUtilities
    public static NSArray arrayWithoutDuplicates(NSArray anArray) {
        NSMutableSet aSet = new NSMutableSet();
        aSet.addObjectsFromArray(anArray);
        return aSet.allObjects();
    }

    /**
     * Filters out duplicates of an array of enterprise objects
     * based on the value of the given key off of those objects.
     * Note: Current implementation depends on the key returning a
     * cloneable object. Also the order is not preseved from the
     * original array.
     * @param eos array of enterprise objects
     * @param key key path to be evaluated off of every enterprise
     *		object
     * @return filter array of objects based on the value of a key-path.
     */
    // FIXME: Broken implementation, relies on the value returned by the key to be Cloneable
    //		also doesn't handle the case of the key returning null or an actual keyPath
    //		and has the last object in the array winning the duplicate tie.
    // FIXME: Does not preserve order.
    // ENHANCEME: Doesn't need to be eo specific.
    // MOVEME: ERXArrayUtilities
    public static NSArray arrayWithoutDuplicateKeyValue(NSArray eos, String key){
        NSMutableDictionary dico = new NSMutableDictionary();
        for(Enumeration e = eos.objectEnumerator(); e.hasMoreElements(); ){
            EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
            Object value = eo.valueForKey(key);
            if(value != null){
                dico.setObjectForKey(eo, value);
            }
        }
        return dico.allValues();
    }

    /**
     * Subtracts the contents of one array from another.
     * Note: Current implementation does not preserve order.
     * @param main array to have values removed from it.
     * @param minus array of values to remove from the main array
     * @param result array after performing subtraction.
     */
    // FIXME: This has the side effect of removing any duplicate elements from
    //		the main array as well as not preserving the order of the array
    // MOVEME: ERXArrayUtilities
    public static NSArray arrayMinusArray(NSArray main, NSArray minus){
        NSSet result = ERXUtilities.setFromArray(main);
        return result.setBySubtractingSet(ERXUtilities.setFromArray(minus)).allObjects();
    }

    /**
     * Creates an array preserving order by adding all of the
     * non-duplicate values from the second array to the first.
     * @param a1 first array
     * @param a2 second array
     * @return array containing all of the elements of the first
     *		array and all of the non-duplicate elements of
     *		the second array.
     */
    // MOVEME: ERXArrayUtilities
    public static NSArray arrayByAddingObjectsFromArrayWithoutDuplicates(NSArray a1, NSArray a2) {
        // FIXME this is n2 -- could be made n lg n
        NSArray result=null;
        if (a2.count()==0)
            result=a1;
        else {
            NSMutableArray mutableResult=new NSMutableArray(a1);
            for (Enumeration e=a2.objectEnumerator(); e.hasMoreElements();) {
                Object elt=e.nextElement();
                if (!mutableResult.containsObject(elt)) mutableResult.addObject(elt);
            }
            result=mutableResult;
        }
        return result;
    }

    /**
     * Adds all of the non-duplicate elements from the second
     * array to the mutable array.
     * @param a1 mutable array where non-duplicate objects are
     *		added
     * @param a2 array to be added to a1
     */
    // MOVEME: ERXArrayUtilities
    public static void addObjectsFromArrayWithoutDuplicates(NSMutableArray a1, NSArray a2) {
        for (Enumeration e=a2.objectEnumerator(); e.hasMoreElements();) {
            Object elt=e.nextElement();
            if (!a1.containsObject(elt)) a1.addObject(elt);
        }
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
            cat().warn("Attempting to refresh a non-shared EO: " + entityName);
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
            cat().warn("not adding sid: url="+url+" session="+s);
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

    /** holds the base adjustment for fuzzy matching */
    // FIXME: Not thread safe
    // MOVEME: Needs to go with the fuzzy matching stuff
    protected static double adjustement = 0.5;

    /**
     * Sets the base adjustment used for fuzzy matching
     * @param newAdjustment factor to be used.
     */
    // FIXME: Not thread safe.
    // MOVEME: fuzzy matching stuff
    public static void setAdjustement(double newAdjustement) {
        adjustement = newAdjustement;
    }

    /**
     * Fuzzy matching is useful for catching user entered typos. For example
     * if a user is search for a company named 'Aple' within your application
     * they aren't going to find it. Thus the idea of fuzzy matching, meaning you
     * can define a threshold of 'how close can they be' type of thing.
     * 
     * @param name to be matched against
     * @param entityName name of the entity to perform the match against.
     * @param proertyKey to be matched against
     * @param synonymsKey allows objects to have additional values to be matched
     * 		against in addition to just the value of the propertyKey
     * @param ec context to fetch data in
     * @param cleaner object used to clean a string, for example the cleaner might
     *		strip out the words 'The' and 'Inc.'
     * @param comparisonString can be either 'asc' or 'desc' to tell how the results
     *		should be sorted. Bad design, this will change.
     * @return an array of objects that match in a fuzzy manner the name passed in.
     */
    // FIXME: This needs to be made more generic, i.e. right now it depends on having a field 'distance' on the
    //	      enterprise object. Also right now it fetches *all* of the attributes for *all* of the entities.
    //	      that is very costly. Should only be getting the attribute and pk.
    // FIXME: Bad api design with the comparisonString, should just pass in an EOSortOrdering
    // MOVEME: Not sure, maybe it's own class and put the interface as a static inner interface
    public static NSArray fuzzyMatch(String name,
                                     String entityName,
                                     String propertyKey,
                                     String synonymsKey,
                                     EOEditingContext ec,
                                     ERXFuzzyMatchCleaner cleaner,
                                     String comparisonString){
        NSMutableArray results = new NSMutableArray();
        NSArray rawRows = EOUtilities.rawRowsMatchingValues( ec, entityName, null);
        if(name == null)
            name = "";
        name = name.toUpperCase();
        String cleanedName = cleaner.cleanStringForFuzzyMatching(name);
        for(Enumeration e = rawRows.objectEnumerator(); e.hasMoreElements(); ){
            NSDictionary dico = (NSDictionary)e.nextElement();
            Object value = dico.valueForKey(propertyKey);
            boolean trySynonyms = true;
            //First try to match with the name of the eo
            if( value!=null && value instanceof String){
                String comparedString = ((String)value).toUpperCase();
                String cleanedComparedString = cleaner.cleanStringForFuzzyMatching(comparedString);
                if( (distance(name, comparedString) <=
                     Math.min((double)name.length(), (double)comparedString.length())*adjustement ) ||
                    (distance(cleanedName, cleanedComparedString) <=
                     Math.min((double)cleanedName.length(), (double)cleanedComparedString.length())*adjustement)){
                    ERXGenericRecord object = (ERXGenericRecord)EOUtilities.objectFromRawRow( ec, entityName, dico);
                    object.takeValueForKey(new Double(distance(name, comparedString)), "distance");
                    results.addObject(object);
                    trySynonyms = false;
                }
            }
            //Then try to match using the synonyms vector
            if(trySynonyms && synonymsKey != null){
                Object synonymsString = dico.valueForKey(synonymsKey);
                if(synonymsString != null && synonymsString instanceof String){
                    Object plist  = NSPropertyListSerialization.propertyListFromString((String)synonymsString);
                    Vector v = (Vector)plist;
                    for(int i = 0; i< v.size(); i++){
                        String comparedString = ((String)v.elementAt(i)).toUpperCase();
                        if((distance(name, comparedString) <=
                            Math.min((double)name.length(), (double)comparedString.length())*adjustement) ||
                           (distance(cleanedName, comparedString) <=
                            Math.min((double)cleanedName.length(), (double)comparedString.length())*adjustement)){
                            ERXGenericRecord object = (ERXGenericRecord)EOUtilities.objectFromRawRow( ec, entityName, dico);
                            object.takeValueForKey(new Double(distance(name, comparedString)), "distance");
                            results.addObject(object);
                            break;
                        }
                    }
                }

            }
        }
        if(comparisonString != null){
            NSArray sortOrderings = new NSArray();
            if(comparisonString.equals("asc")){
                sortOrderings = new NSArray(new Object [] { new EOSortOrdering("distance",
                                                                               EOSortOrdering.CompareAscending) });
            }else if(comparisonString.equals("desc")){
                sortOrderings = new NSArray(new Object [] { new EOSortOrdering("distance",
                                                                               EOSortOrdering.CompareDescending) });
            }
            results = (NSMutableArray)EOSortOrdering.sortedArrayUsingKeyOrderArray((NSArray)results, sortOrderings);
        }
        return results;
    }

    /**
     * Java port of the distance algorithm.
     *
     * The code below comes from the following post on http://mail.python.org
     * Fuzzy string matching
     *   Magnus L. Hetland mlh@idt.ntnu.no
     *   27 Aug 1999 15:51:03 +0200
     *
     *  Explanation of the distance algorithm...
     *
     *  The algorithm:
     *
     *  def distance(a,b):
     *   c = {}
     *  n = len(a); m = len(b)
     *
     *  for i in range(0,n+1):
     *  c[i,0] = i
     *  for j in range(0,m+1):
     *  c[0,j] = j
     *
     *  for i in range(1,n+1):
     *  for j in range(1,m+1):
     *  x = c[i-1,j]+1
     *  y = c[i,j-1]+1
     *  if a[i-1] == b[j-1]:
     *    z = c[i-1,j-1]
     *  else:
     *    z = c[i-1,j-1]+1
     *  c[i,j] = min(x,y,z)
     *  return c[n,m]
     *
     *  It calculates the following: Given two strings, a and b, and three
     *  operations, adding, subtracting and exchanging single characters, what
     *  is the minimal number of steps needed to translate a into b?
     *
     *  The method is based on the following idea:
     *
     *  We want to find the distance between a[:x] and b[:y]. To do this, we
     *  first calculate
     *
     *  1) the distance between a[:x-1] and b[:y], adding the cost of a
     *  subtract-operation, used to get from a[:x] to a[:z-1];
     *
     *  2) the distance between a[:x] and b[:y-1], adding the cost of an
     *  addition-operation, used to get from b[:y-1] to b[:y];
     *
     *  3) the distance between a[:x-1] and b[:y-1], adding the cost of a
     *  *possible* exchange of the letter b[y] (with a[x]).
     *
     *  The cost of the subtraction and addition operations are 1, while the
     *  exchange operation has a cost of 1 if a[x] and b[y] are different, and
     *  0 otherwise.
     *
     *  After calculating these costs, we choose the least one of them (since
     *                                                          we want to use the best solution.)
     *
     *  Instead of doing this recursively (i.e. calculating ourselves "back"
     *                             from the final value), we build a cost-matrix c containing the optimal
     *  costs, so we can reuse them when calculating the later values. The
     *  costs c[i,0] (from string of length n to empty string) are all i, and
     *  correspondingly all c[0,j] (from empty string to string of length j)
     *  are j.
     *
     *  Finally, the cost of translating between the full strings a and b
     *  (c[n,m]) is returned.
     *
     *  I guess that ought to cover it...
     * --------------------------
     * @param a first string
     * @param b second string
     * @return the distance between the two strings
     */
    // MOVEME: ERXStringUtilities
     public static double distance( String a, String b){
        int n = a.length();
        int m = b.length();
        int c[][] = new int[n+1][m+1];
        for(int i = 0; i<=n; i++){
            c[i][0] = i;
        }
        for(int j = 0; j<=m; j++){
            c[0][j] = j;
        }
        for(int i = 1; i<=n; i++){
            for(int j = 1; j<=m; j++){
                int x = c[i-1][j] + 1;
                int y = c[i][j-1] + 1;
                int z = 0;
                if(a.charAt(i-1) == b.charAt(j-1))
                    z = c[i-1][j-1];
                else
                    z = c[i-1][j-1] + 1;
                int temp = Math.min(x,y);
                c[i][j] = Math.min(z, temp);
            }
        }
        return c[n][m];
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
    /** Holds a session-thread name relationship. Better achived using a thread local */
    private static NSMutableDictionary _sessionsPerThread=new NSMutableDictionary();
    /**
     * Sets the current session for this thread. This is called
     * from {@link ERXSession}'s awake and sleep methods.
     * @param session that is currently active for this thread.
     */
    // FIXME: Should use weak references as well as an InheritableThreadLocal for all of this type
    //		of stuff.
    // this should be thread-safe
    // even though we don't intend to run with full MT on at this point
    public synchronized static void setSession(WOSession session) {
        Object key=Thread.currentThread().getName();
        if (session!=null)
            _sessionsPerThread.setObjectForKey(session,key);
        else
            _sessionsPerThread.removeObjectForKey(key);
    }

    /**
     * Returns the current session object for this thread.
     * @return current session object for this thread
     */
    // ENHANCEME: Better done using a WeakHashMap and a ThreadLocal
    public synchronized static WOSession session() {
        Object key=Thread.currentThread().getName();
        return (WOSession)_sessionsPerThread.objectForKey(key);
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
