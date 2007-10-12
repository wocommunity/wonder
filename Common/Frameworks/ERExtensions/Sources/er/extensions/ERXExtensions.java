/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOSession;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eoaccess.EOQualifierSQLGeneration.Support;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.jdbcadaptor.JDBCAdaptorException;

import er.extensions.remoteSynchronizer.ERXRemoteSynchronizer;

/**
 * Principal class of the ERExtensions framework. This class
 * will be loaded at runtime when the ERExtensions bundle is
 * loaded (even before the Application constructor is called)
 * This class has a boat-load of stuff in it that will hopefully
 * be finding better homes in the future. This class serves as
 * the initilization point of this framework, look in the static
 * initializer to see all the stuff that is initially setup when
 * this class is loaded. This class also has a boat load of
 * string, array and eof utilities as well as the factory methods
 * for creating editing contexts with the default delegates set.
 */
public class ERXExtensions extends ERXFrameworkPrincipal {
    
    /** Notification name, posted before object will change in an editing context */
    public final static String objectsWillChangeInEditingContext= "ObjectsWillChangeInEditingContext";
    
    /** logging support */
    private static Logger _log;
    
    private static boolean _initialized;

    public ERXExtensions() {
    }

    /** holds the default model group */
    protected ERXModelGroup defaultModelGroup;

    /**
     * Delegate method for the {@link EOModelGroup} class delegate.
     * @return a fixed ERXModelGroup
     */
    public EOModelGroup defaultModelGroup() {
        if(defaultModelGroup == null) {
            defaultModelGroup = new ERXModelGroup();
            defaultModelGroup.loadModelsFromLoadedBundles();
        }
        return defaultModelGroup;
    }
   
   /**
     * Configures the framework. All the bits and pieces that need
     * to be configured are configured, those that need to happen
     * later are delayed by registering an observer for notifications
     * that are posted when the application is finished launching.
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
   
    protected void initialize() {
    	NSNotificationCenter.defaultCenter().addObserver(this,
    			new NSSelector("bundleDidLoad", ERXConstant.NotificationClassArray),
    			ERXApplication.AllBundlesLoadedNotification,
    			null);
    }

    public void bundleDidLoad(NSNotification n) {
    	if(_initialized) return;
    	_initialized = true;
    	
    	try {
    		// This will load any optional configuration files, 
    		ERXConfigurationManager.defaultManager().initialize();
    		// ensures that WOOutputPath's was processed with this @@
    		// variable substitution. WOApplication uses WOOutputPath in
    		// its constructor so we need to modify it before calling
    		// the constructor.
        	EOModelGroup.setClassDelegate(this);
        	ERXSystem.updateProperties();

    		// AK: enable this when we're ready
        	// WOEncodingDetector.sharedInstance().setFallbackEncoding("UTF-8");
        	
        	// GN: configure logging with optional custom subclass of ERXLogger
        	String className = ERXProperties.stringForKey("er.extensions.erxloggerclass"); 
        	if (className != null) {
	        	Class loggerClass = Class.forName(className);
	        	Method method = loggerClass.getDeclaredMethod(ERXLogger.CONFIGURE_LOGGING_WITH_SYSTEM_PROPERTIES, (Class[]) null);
	        	method.invoke(loggerClass, (Object[]) null);
        	}
        	else {
        		// default behaviour:
        		ERXLogger.configureLoggingWithSystemProperties();
        	}
        	
            ERXArrayUtilities.initialize();
            
    		// False by default
    		if (ERXValueUtilities.booleanValue(System.getProperty(ERXSharedEOLoader.PatchSharedEOLoadingPropertyKey))) {
    			ERXSharedEOLoader.patchSharedEOLoading();
    		}            
    		ERXExtensions.configureAdaptorContextRapidTurnAround(this);
    		ERXJDBCAdaptor.registerJDBCAdaptor();
    		EODatabaseContext.setDefaultDelegate(ERXDatabaseContextDelegate.defaultDelegate());
    		ERXAdaptorChannelDelegate.setupDelegate();
    		ERXEC.factory().setDefaultDelegateOnEditingContext(EOSharedEditingContext.defaultSharedEditingContext(), true);

    		ERXEntityClassDescription.registerDescription();
    		if (!ERXProperties.webObjectsVersionIs52OrHigher()) {
    			NSNotificationCenter.defaultCenter().addObserver(this,
    					new NSSelector("sessionDidTimeOut", ERXConstant.NotificationClassArray),
    					WOSession.SessionDidTimeOutNotification,
    					null);
    			NSNotificationCenter.defaultCenter().addObserver(this,
    					new NSSelector("editingContextDidCreate",
    							ERXConstant.NotificationClassArray),
    							ERXEC.EditingContextDidCreateNotification,
    							null);                    
    		}
    	} catch (Exception e) {
    		throw NSForwardException._runtimeExceptionForThrowable(e);
    	}
    }

    /**
     * This method is called when the application has finished
     * launching. Here is where log4j is configured for rapid
     * turn around, the compiler proxy is initialized and the
     * validation template system is configured.
     */
    public void finishInitialization() {
    	ERXJDBCAdaptor.registerJDBCAdaptor();
        ERXConfigurationManager.defaultManager().loadOptionalConfigurationFiles();
        ERXProperties.populateSystemProperties();
        
        ERXConfigurationManager.defaultManager().configureRapidTurnAround();
        ERXLocalizer.initialize();
        ERXValidationFactory.defaultFactory().configureFactory();
        // update configuration with system properties that might depend
        // on others like 
        // log4j.appender.SQL.File=@@loggingBasePath@@/@@port@@.sql
        // loggingBasePath=/var/log/@@name@@
        // name and port are resolved via WOApplication.application()
        // ERXLogger.configureLoggingWithSystemProperties();
        
        _log = Logger.getLogger(ERXExtensions.class);

        registerSQLSupportForSelector(new NSSelector(ERXPrimaryKeyListQualifier.IsContainedInArraySelectorName), 
                EOQualifierSQLGeneration.Support.supportForClass(ERXPrimaryKeyListQualifier.class));
        registerSQLSupportForSelector(new NSSelector(ERXToManyQualifier.MatchesAllInArraySelectorName), 
                EOQualifierSQLGeneration.Support.supportForClass(ERXToManyQualifier.class));
        registerSQLSupportForSelector(new NSSelector(ERXRegExQualifier.MatchesSelectorName), 
                EOQualifierSQLGeneration.Support.supportForClass(ERXRegExQualifier.class));
		if (!ERXApplication.isWO54()) {
	        //AK: in 5.4 disable 
			EOQualifierSQLGeneration.Support.setSupportForClass(new ERXInOrQualifierSupport(), EOOrQualifier._CLASS);
		}
		
		// ERXObjectStoreCoordinatorPool has a static initializer, so just load the class if
		// the configuration setting exists
        if (ERXRemoteSynchronizer.remoteSynchronizerEnabled() || ERXProperties.booleanForKey("er.extensions.ERXDatabaseContext.activate")) {
        	String className = ERXProperties.stringForKeyWithDefault("er.extensions.ERXDatabaseContext.className", ERXDatabaseContext.class.getName());
        	Class c = ERXPatcher.classForName(className);
        	if(c == null) {
        		throw new IllegalStateException("er.extensions.ERXDatabaseContext.className not found: " + className);
        	}
        	EODatabaseContext.setContextClassToRegister(c);
        }
		ERXObjectStoreCoordinatorPool.initializeIfNecessary();
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

    /** logging support for the adaptor channel */
    public static Logger adaptorLogger;

    /** logging support for shared object loading */
    public static Logger sharedEOadaptorLogger;

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
    //		between logger names and NSLog groups, for example
    //		com.webobjects.logging.DebugGroupSQLGeneration we should
    //		be able to get the last part of the logger name and look up that log group and turn 
    public static void configureAdaptorContextRapidTurnAround(Object anObserver) {
        if (!_isConfigureAdaptorContextRapidTurnAround) {
            // This allows enabling from the log4j system.
            adaptorLogger = Logger.getLogger("er.transaction.adaptor.EOAdaptorDebugEnabled");
            
            sharedEOadaptorLogger = Logger.getLogger("er.transaction.adaptor.EOSharedEOAdaptorDebugEnabled");
            if ((adaptorLogger.isDebugEnabled() 
            		&& !NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess))
            		|| ERXProperties.booleanForKey("EOAdaptorDebugEnabled")) {
                NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess);
                NSLog.setAllowedDebugLevel(NSLog.DebugLevelInformational);
            }
            adaptorEnabled = NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess) ? Boolean.TRUE : Boolean.FALSE;
                                          // Allows rapid turn-around of adaptor debugging.
            NSNotificationCenter.defaultCenter().addObserver(anObserver,
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
        	setAdaptorLogging(targetState.booleanValue());
        }
    }

    /**
     * Returns the current state of EOAdaptor logging.
     * @return
     */
	public static boolean adaptorLogging() {
		return NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess);
	}

    /**
     * Turn EOAdaptor logging on and off.
     * @param onOff
     */
    public static void setAdaptorLogging(boolean onOff) {
    	Boolean targetState = onOff ? Boolean.TRUE : Boolean.FALSE;
    	if (NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess) != targetState.booleanValue()) {
    		if (targetState.booleanValue()) {
    			NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess);
    		} else {
    			NSLog.refuseDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess);
    		}
    	}
    	if (targetState.booleanValue()) {
    		adaptorLogger.info("Adaptor debug on");
    	} else {
    		adaptorLogger.info("Adaptor debug off");
    	}
    	adaptorEnabled = targetState;
   }

    /**
     * Retaining the editing contexts explicitly until the session that was active
     * when they were created goes away
     * this hopefully will go some way towards avoiding the 'attempted to send'
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
                if (_log.isDebugEnabled()) {
                    NSArray a=(NSArray)_editingContextsPerSession.objectForKey(sessionID);
                    _log.debug("Session "+sessionID+" is timing out ");
                    _log.debug("Found "+ ((a == null) ? 0 : a.count()) + " editing context(s)");
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
                 if (_log.isDebugEnabled())
                	 _log.debug("Creating array for "+s.sessionID());
             }
             a.addObject(ec);
             if (_log.isDebugEnabled())
            	 _log.debug("Added new ec to array for "+s.sessionID());
         } else if (_log.isDebugEnabled()) {
        	 _log.debug("Editing Context created with null session.");
         }
    }

    /**
     * Removes all of the HTML tags from a given string.
     * Note: this is a very simplistic implementation
     * and will most likely not work with complex HTML.
     * Note: for actual conversion of HTML tags into regular
     * strings have a look at {@link ERXSimpleHTMLFormatter}
     * @param s html string
     * @return string with all of its html tags removed
     */
    // FIXME: this is so simplistic it will break if you sneeze
    // MOVEME: ERXStringUtilities 
    public static String removeHTMLTagsFromString(String s) {
        StringBuffer result=new StringBuffer();
        if (s != null && s.length()>0) {
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
        return ERXStringUtilities.replaceStringByStringInString("&nbsp;"," ",result.toString());
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
        if (_log.isDebugEnabled()) _log.debug("Forcing full Garbage Collection");
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
        runtime.runFinalization(); //TODO: should this be inside the loop?
    }

    /**
     * Capitalizes the given string.
     * @param s string to capitalize
     * @return capitalized string if the first char is a
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
     * Pluralizes a given string for a given language.
     * See {@link ERXLocalizer} for more information.
     * @param s string to pluralize
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
        return v1 != v2 && (v1 == null || v2 == null || !v1.equals(v2));
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
            Integer.parseInt(s);
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
     * @return the <code>lastModified</code> method of the file object
     */
    // MOVEME: ERXFileUtilities
    // ENHANCEME: Should be able to specify the language to check
    public static long lastModifiedDateForFileInFramework(String fileName, String frameworkName) {
        return ERXFileUtilities.lastModifiedDateForFileInFramework(fileName, frameworkName);
    }

    /**
     * Reads a file in from the file system and parses it as if it were a property list.
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
     * of languages and parses the file as if it were a property list.
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
     * userInfo dictionary.
     *  See the method {@link #userInfoUnit(EOEnterpriseObject, String) userInfoUnit} for
     * a better description of getting values out of the userInfo
     * dictionary. This method deals with resolving dynamic userInfo
     * keys. These keys need to start with the '@@' symbol. For instance
     * if you have the user info value '@unit' off of an attribute for the
     * entity Movie, then you can either pass in a Movie object or a
     * different object with a prefix key path to a movie object.<br/>
     * @param userInfoUnitString string to be resolved, needs to start with
     *		'@@'. This keypath will be evaluated against either the object
     *		if no prefixKeyPath is specified or the object returned by
     *		the prefixKeyPath being evaluated against the object passed in.
     * @param object to resolve either the user info unit or the prefixKeyPath.
     * @param prefixKeyPath used as a prefix for the unit resolution.
     * @return the resolved unit from the object.
     */
    public static String resolveUnit(String userInfoUnitString,
                                     EOEnterpriseObject object,
                                     String prefixKeyPath) {
        // some of our units (stored in the user info) take the form of @project.unit
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
     * sharedEntityDataWasRefreshed() if the shared entity implements it.
     *
     * @param entityName name of the shared entity
     */
    // FIXME: Uses default model group, and default shared editing context.
    // MOVEME: ERXEOFUtilities
    public static void refreshSharedObjectsWithName(String entityName) {
        if (entityName == null) {
            throw new IllegalArgumentException("Entity name argument is null for method: refreshSharedObjectsWithName");
        }
        EOSharedEditingContext sharedEC = EOSharedEditingContext.defaultSharedEditingContext();
        sharedEC.lock();
        try {
            EOEntity entity = ERXEOAccessUtilities.entityNamed(sharedEC, entityName);

            //if entity caches objects, clear out the cache
            if( entity.cachesObjects() ) {
                EODatabaseContext databaseContext = EOUtilities.databaseContextForModelNamed(sharedEC, entity.model().name());
                EODatabase database = databaseContext.database();
                database.invalidateResultCacheForEntityNamed(entityName);
            }

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
                _log.warn("Attempting to refresh a non-shared EO: " + entityName);
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
     * Adds the session ID (wosid) for a given session to a given url. 
     * @param url to add wosid form value to.
     * @return url with the addition of wosid form value
     */
    // FIXME: Should check to see if the wosid form value has already been set.
    public static String addWosidFormValue(String url, WOSession s) {
        String result= url;
        if (result!=null && s!=null) {
            result += ( result.indexOf("?") == -1 ? "?" : "&" ) + "wosid=" + s.sessionID();
        } else {
        	_log.warn("not adding sid: url="+url+" session="+s);
        }
        return result;
    }

    /**
     * Given an initial string and an array of substrings, 
     * Removes any occurances of any of the substrings
     * from the initial string. Used in conjunction with
     * fuzzy matching.
     * @param newString initial string from which to remove other strings
     * @param toBeCleaneds array of substrings to be removed from the initial string.
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
     * Uses the <code>setObjectForKey</code> method of the {@link WOSession}
     * class to push a Boolean object onto the session for a given key.
     * Note this is not using key value coding, meaning you don't need
     * to have a boolean instance variable corresponding to the given
     * key on your session object. This flag can be retrieved using
     * the method <code>booleanFlagOnSessionForKeyWithDefault</code>.
     * @param s session object on which to set the boolean flag 
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
     * that object using the <code>booleanValue</code> method of
     * {@link ERXUtilities}. If there is no object corresponding
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
     * @deprecated use  ERXSession.setSession(session) instead
     */
    public synchronized static void setSession(ERXSession session) {
    	 ERXSession.setSession(session);
    }

    /**
     * Returns the current session object for this thread.
     * @return current session object for this thread
     * @deprecated use  ERXSession.session() instead
     */
    public synchronized static ERXSession session() {
        return  ERXSession.session();
    }

    /**
     * Constructs a unique key based on a context.
     * A method used by the preferences mechanism from ERDirectToWeb which
     * needs to be here because it is shared by ERDirectToWeb and ERCoreBusinessLogic.
     * 
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
     * Frees all of the resources associated with a given
     * process and then destroys the process.
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
     * Note that this doesn't quite check the method signature
     * since the method return type is not checked.
     *
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
