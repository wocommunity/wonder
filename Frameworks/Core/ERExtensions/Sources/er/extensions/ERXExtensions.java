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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import er.extensions.logging.ERXLoggingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOSession;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOQualifierSQLGeneration.Support;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eoaccess.ERXEntityDependencyOrderingDelegate;
import com.webobjects.eoaccess.ERXModel;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierVariable;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation._NSStringUtilities;
import com.webobjects.jdbcadaptor.JDBCAdaptorException;

import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXAdaptorChannelDelegate;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXDatabase;
import er.extensions.eof.ERXDatabaseContext;
import er.extensions.eof.ERXDatabaseContextDelegate;
import er.extensions.eof.ERXDatabaseContextMulticastingDelegate;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEnterpriseObjectCache;
import er.extensions.eof.ERXEntityClassDescription;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.eof.ERXModelGroup;
import er.extensions.eof.ERXObjectStoreCoordinatorPool;
import er.extensions.eof.ERXSharedEOLoader;
import er.extensions.eof.qualifiers.ERXFullTextQualifier;
import er.extensions.eof.qualifiers.ERXFullTextQualifierSupport;
import er.extensions.eof.qualifiers.ERXPrimaryKeyListQualifier;
import er.extensions.eof.qualifiers.ERXRegExQualifier;
import er.extensions.eof.qualifiers.ERXToManyQualifier;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXConfigurationManager;
import er.extensions.foundation.ERXMutableURL;
import er.extensions.foundation.ERXPatcher;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXRuntimeUtilities;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXSystem;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.jdbc.ERXJDBCAdaptor;
import er.extensions.localization.ERXLocalizer;
import er.extensions.logging.ERXLogger;
import er.extensions.partials.ERXPartialInitializer;
import er.extensions.qualifiers.ERXFalseQualifier;
import er.extensions.qualifiers.ERXFalseQualifierSupport;
import er.extensions.qualifiers.ERXTrueQualifier;
import er.extensions.qualifiers.ERXTrueQualifierSupport;
import er.extensions.remoteSynchronizer.ERXRemoteSynchronizer;
import er.extensions.validation.ERXValidationFactory;

/**
 * Principal class of the ERExtensions framework. This class
 * will be loaded at runtime when the ERExtensions bundle is
 * loaded (even before the Application constructor is called)
 * This class has a boat-load of stuff in it that will hopefully
 * be finding better homes in the future. This class serves as
 * the initialization point of this framework, look in the static
 * initializer to see all the stuff that is initially setup when
 * this class is loaded. This class also has a boat load of
 * string, array and EOF utilities as well as the factory methods
 * for creating editing contexts with the default delegates set.
 */
public class ERXExtensions extends ERXFrameworkPrincipal {
    
    /** Notification name, posted before object will change in an editing context */
    public final static String objectsWillChangeInEditingContext= "ObjectsWillChangeInEditingContext";

    /** Notification name, posted before EOAdaptor debug logging will change its setting. */
    public final static String eoAdaptorLoggingWillChangeNotification = "EOAdaptorLoggingWillChange";
    
    /** logging support */
    private static Logger _log;
    
    private static boolean _initialized;

    public ERXExtensions() {
    }

    /** holds the default model group */
    protected volatile ERXModelGroup defaultModelGroup;

    /**
     * Delegate method for the {@link EOModelGroup} class delegate.
     * @return a fixed ERXModelGroup
     */
    public EOModelGroup defaultModelGroup() {
        if(defaultModelGroup == null) {
        	synchronized (ERXModel._ERXGlobalModelLock) {
        		if (defaultModelGroup == null) {
		        	String defaultModelGroupClassName = ERXProperties.stringForKey("er.extensions.defaultModelGroupClassName");
		        	if (defaultModelGroupClassName == null) {
			            defaultModelGroup = new ERXModelGroup();
		        	}
		        	else {
		        		try {
							defaultModelGroup = Class.forName(defaultModelGroupClassName).asSubclass(ERXModelGroup.class).newInstance();
						}
						catch (Exception e) {
							throw new RuntimeException("Failed to create custom ERXModelGroup subclass '" + defaultModelGroupClassName + "'.", e);
						}
		        	}
		            defaultModelGroup.loadModelsFromLoadedBundles();
        		}
        	}
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
     * configuring the adaptor context so that SQL debugging can
     * be enabled and disabled on the fly through the log4j system.
     * Handling cleanup issues when sessions timeout, i.e. releasing
     * all references to editing contexts created for that session.
     * Handling call all of the <code>did*</code> methods on
     * {@link ERXGenericRecord} subclasses after an editing context
     * has been saved. This delegate is also responsible for configuring
     * {@link ERXValidationFactory}.
     * This delegate is configured when this framework is loaded.
     */
    @Override
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
    		// ensures that WOOutputPath's was processed with this @@
    		// variable substitution. WOApplication uses WOOutputPath in
    		// its constructor so we need to modify it before calling
    		// the constructor.
    		ERXConfigurationManager.defaultManager().initialize();
        	EOModelGroup.setClassDelegate(this);
        	ERXSystem.updateProperties();
 
    		// AK: enable this when we're ready
        	// WOEncodingDetector.sharedInstance().setFallbackEncoding(CharEncoding.UTF_8);
        	
        	// GN: configure logging with optional custom subclass of ERXLogger
        	String className = ERXProperties.stringForKey("er.extensions.erxloggerclass"); 
        	if (className != null) {
	        	Class loggerClass = Class.forName(className);
	        	Method method = loggerClass.getDeclaredMethod(ERXLoggingUtilities.CONFIGURE_LOGGING_WITH_SYSTEM_PROPERTIES, (Class[]) null);
	        	method.invoke(loggerClass, (Object[]) null);
        	}
        	else {
        		// default behaviour:
        		ERXLoggingUtilities.configureLoggingWithSystemProperties();
        	}
        	
            ERXArrayUtilities.initialize();
            
    		// False by default
    		if (ERXValueUtilities.booleanValue(System.getProperty(ERXSharedEOLoader.PatchSharedEOLoadingPropertyKey))) {
    			ERXSharedEOLoader.patchSharedEOLoading();
			}
			ERXExtensions.configureAdaptorContextRapidTurnAround(this);
			ERXJDBCAdaptor.registerJDBCAdaptor();

			if (EODatabaseContext.defaultDelegate() == null) {
				if (ERXProperties.booleanForKey(ERXEntityDependencyOrderingDelegate.ERXEntityDependencyOrderingDelegateActiveKey)) {
					ERXDatabaseContextMulticastingDelegate.addDefaultDelegate(new ERXEntityDependencyOrderingDelegate());
					ERXDatabaseContextMulticastingDelegate.addDefaultDelegate(ERXDatabaseContextDelegate.defaultDelegate());
				}
				else {
					EODatabaseContext.setDefaultDelegate(ERXDatabaseContextDelegate.defaultDelegate());
				}
			}
    		
    		ERXAdaptorChannelDelegate.setupDelegate();
    		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("sharedEditingContextWasInitialized", ERXConstant.NotificationClassArray), EOSharedEditingContext.DefaultSharedEditingContextWasInitializedNotification, null);

    		ERXEntityClassDescription.registerDescription();
    		ERXPartialInitializer.registerModelGroupListener();
    	} catch (Exception e) {
    		throw NSForwardException._runtimeExceptionForThrowable(e);
    	}
    }

    /**
     * This method is called when the application has finished
     * launching. Here is where log4j is configured for rapid
     * turn around and the validation template system is configured.
     */
    @Override
    public void finishInitialization() {
    	ERXJDBCAdaptor.registerJDBCAdaptor();
        // AK: we now setup the properties three times. At startup, in ERX.init
		// and here. Note that this sucks beyond belief, as this will produce
		// unforeseen results in several cases, but it's the only way to set up
		// all parts of the property handling. The first install only loads plain
		// and user props the second has no good way to set up the main bundle and this one
		// comes too late for static inits
    	ERXConfigurationManager.defaultManager().loadConfiguration();
    	
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
        
        _log = LoggerFactory.getLogger(ERXExtensions.class);
		ERXProperties.pathsForUserAndBundleProperties(true);

		try {
			// MS: initialize these with Class.forName(Whatever.class.getName()) because the .class class literal does not trigger static initializers to run in 1.5,
			// which means that the sql generation support classes are not registered
			registerSQLSupportForSelector(new NSSelector(ERXPrimaryKeyListQualifier.IsContainedInArraySelectorName), EOQualifierSQLGeneration.Support.supportForClass(Class.forName(ERXPrimaryKeyListQualifier.class.getName())));
			registerSQLSupportForSelector(new NSSelector(ERXToManyQualifier.MatchesAllInArraySelectorName), EOQualifierSQLGeneration.Support.supportForClass(Class.forName(ERXToManyQualifier.class.getName())));
	        registerSQLSupportForSelector(new NSSelector(ERXRegExQualifier.MatchesSelectorName), EOQualifierSQLGeneration.Support.supportForClass(Class.forName(ERXRegExQualifier.class.getName())));
	        registerSQLSupportForSelector(new NSSelector(ERXFullTextQualifier.FullTextContainsSelectorName), EOQualifierSQLGeneration.Support.supportForClass(Class.forName(ERXFullTextQualifier.class.getName())));
		}
		catch (Throwable t) {
			throw NSForwardException._runtimeExceptionForThrowable(t);
		}

		EOQualifierSQLGeneration.Support.setSupportForClass(new ERXFullTextQualifierSupport(), ERXFullTextQualifier.class);
		EOQualifierSQLGeneration.Support.setSupportForClass(new ERXFalseQualifierSupport(), ERXFalseQualifier.class);
		EOQualifierSQLGeneration.Support.setSupportForClass(new ERXTrueQualifierSupport(), ERXTrueQualifier.class);

		// ERXObjectStoreCoordinatorPool has a static initializer, so just load the class if
		// the configuration setting exists
        if (ERXRemoteSynchronizer.remoteSynchronizerEnabled() || ERXProperties.booleanForKey("er.extensions.ERXDatabaseContext.activate")) {
        	String dbCtxClassName = ERXProperties.stringForKeyWithDefault("er.extensions.ERXDatabaseContext.className", ERXDatabaseContext.class.getName());
        	Class dbCtxClass = ERXPatcher.classForName(dbCtxClassName);
        	if(dbCtxClass == null) {
        		throw new IllegalStateException("er.extensions.ERXDatabaseContext.className not found: " + dbCtxClassName);
        	}
        	EODatabaseContext.setContextClassToRegister(dbCtxClass);

        	String dbClassName = ERXProperties.stringForKeyWithDefault("er.extensions.ERXDatabase.className", ERXDatabase.class.getName());
        	Class dbClass = ERXPatcher.classForName(dbClassName);
        	if(dbClass == null) {
        		throw new IllegalStateException("er.extensions.ERXDatabase.className not found: " + dbClassName);
        	}
        	if( ERXDatabase.class.isAssignableFrom( dbClass ) ) {
        		ERXDatabaseContext.setDatabaseContextClass( dbClass );
        	} else {
        		throw new IllegalStateException("er.extensions.ERXDatabase.className is not a subclass of ERXDatabase: " + dbClassName);
        	}
        	
        	int mapCapacity = ERXProperties.intForKey( "er.extensions.ERXDatabase.snapshotCacheMapInitialCapacity" );
        	if( mapCapacity > 0 ) {
        		ERXDatabase.setSnapshotCacheMapInitialCapacity( mapCapacity );
        	}
        	float mapLoadFactor = ERXProperties.floatForKey( "er.extensions.ERXDatabase.snapshotCacheMapInitialLoadFactor" );
        	if( mapLoadFactor > 0.0f ) {
        		ERXDatabase.setSnapshotCacheMapInitialLoadFactor( mapLoadFactor );
        	}
        }
		ERXObjectStoreCoordinatorPool.initializeIfNecessary();
    }

    @Override
    public void didFinishInitialization() {
        ERXEnterpriseObjectCache.setApplicationDidFinishInitialization(true);
        super.didFinishInitialization();
    }

    private static Map<String, Support> _qualifierKeys;
    
    public static synchronized void registerSQLSupportForSelector(NSSelector selector, EOQualifierSQLGeneration.Support support) {
        if(_qualifierKeys == null) {
            _qualifierKeys = new HashMap<>();
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
     * 
     * Added SQL generation fix for EOKeyValueQualifiers in an edge case
     * where the key is a key path (with two or more keys) and the last key
     * is a derived attribute.  For example, "customer.fullName" where the
     * last key fullName is defined as: 'firstName || ' ' || lastName.
     * 
     * @author Ricardo Parada
     */
    public static class KeyValueQualifierSQLGenerationSupport extends EOQualifierSQLGeneration.Support {
        
        public static final String HANDLES_KEY_PATH_WITH_DERIVED_ATTRIBUTE_PROPERTY_NAME = "er.extensions.KeyValueQualifierSQLGenerationSupport.handlesKeyPathWithDerivedAttribute";
        
        private EOQualifierSQLGeneration.Support _old;
        
        public KeyValueQualifierSQLGenerationSupport(EOQualifierSQLGeneration.Support old) {
            _old = old;
        }

        private EOQualifierSQLGeneration.Support supportForQualifier(EOQualifier qualifier) {
            EOQualifierSQLGeneration.Support support = null;
            if(qualifier instanceof EOKeyValueQualifier) {
                synchronized (_qualifierKeys) {
                    support = _qualifierKeys.get(((EOKeyValueQualifier)qualifier).selector().name());
                }
            }
            if(support == null) {
                support = _old;
            }
            return support;
        }
        
        @Override
        public String sqlStringForSQLExpression(EOQualifier eoqualifier, EOSQLExpression e) {
        	// Check to see if checking for edge case is enabled
    		boolean handlesKeyPathWithDerivedAttribute = ERXProperties.booleanForKeyWithDefault(HANDLES_KEY_PATH_WITH_DERIVED_ATTRIBUTE_PROPERTY_NAME, true);
        	try {
        		if (handlesKeyPathWithDerivedAttribute && isKeyPathWithDerivedAttributeCase(e.entity(), eoqualifier)) {
        				return sqlStringForSQLExpressionWithKeyPathWithDerivedAttribute(eoqualifier, e);
        		}
        		
        		// Otherwise handle normally
        		return supportForQualifier(eoqualifier).sqlStringForSQLExpression(eoqualifier, e);
        	}
        	catch (JDBCAdaptorException ex) {
        		ERXExtensions._log.error("Failed to generate sql string for qualifier " + eoqualifier + " on entity " + e.entity() + ".");
        		if (handlesKeyPathWithDerivedAttribute == false && isKeyPathWithDerivedAttributeCase(e.entity(), eoqualifier)) {
        			ERXExtensions._log.error("Consider setting " + HANDLES_KEY_PATH_WITH_DERIVED_ATTRIBUTE_PROPERTY_NAME + "=true");
        		}
        		throw ex;
        	}
        }
        
        /**
         * This method handles an edge case where the key of the qualifier is a key path and
         * the last key in the key path references a derived attribute. For example, if the
         * key is "order.customerAge" and customerAge were defined as:
         * 
         * <pre><code>(TRUNC(MONTHS_BETWEEN(orderDate,customer.birthDate)/12,0))</code></pre>
         * 
         * and the key value qualifier was something like "order.customerAge > 50" then this
         * method would generate SQL like this:
         * 
         * <pre><code>(TRUNC(MONTHS_BETWEEN(T1.ORDER_DATE,T2.BIRTH_DATE)/12,0)) > 50</code></pre>
         * 
         * Without this fix EOF ends up throwing an exception.
         * 
         * @param eoqualifier An EOKeyValueQualifier with a key that is a key path and the last
         * component in the key path is a derived attribute.
         * @param e The EOSQLExpression participating in the SQL generation.
         * @return The SQL for the eoqualifier.
         */
        public String sqlStringForSQLExpressionWithKeyPathWithDerivedAttribute(EOQualifier eoqualifier, EOSQLExpression e) {
    		// Using the example where the key-value qualifier's key is the key path 
        	// 'order.customerAge' and its last key 'customerAge' is an attribute defined 
        	// as '(TRUNC(MONTHS_BETWEEN(orderDate,customer.birthDate)/12,0))'
        	// then we get the destination attribute, i.e. 'customerAge'.  Then we parse
        	// the properties referenced by its definition, i.e. 'orderDate'
        	// and 'customer.birthDate'.
        	
        	EOKeyValueQualifier keyValueQualifier = (EOKeyValueQualifier) eoqualifier;
        	String keyPath = keyValueQualifier.key();
    		EOAttribute derivedAttribute = destinationAttribute(e.entity(), keyPath);
    		NSArray<String> propertyKeys = parseDefinitionPropertyKeys(derivedAttribute);
    		
    		// Get the keys preceding the derived attribute, for example, if key
    		// is 'order.customerAge' then the key preceding 'customerAge' would be
    		// 'order' which will become the prefix.
    		
    		NSArray<String> allKeys = NSArray.componentsSeparatedByString(keyPath, ".");
    		String lastKey = allKeys.lastObject();
    		NSMutableArray<String> prefixKeys = allKeys.mutableClone();
    		prefixKeys.removeObject(lastKey);
    		String prefix = prefixKeys.componentsJoinedByString(".") + ".";
    		
    		// Now we prefix every key path referenced by the definition and then
    		// generate SQL for them.  For example the orderDate property key will
    		// become order.orderDate and customer.birthDate will become
    		// order.customer.birthDate.  We then convert it to SQL, i.e. T2.ORDER_DATE
    		// and replace it in the definition.  The end result is to have SQL that
    		// looks like this:
    		//
    		// (TRUNC(MONTHS_BETWEEN(T1.ORDER_DATE,T2.BIRTH_DATE)/12,0)) > 60.0
    		//
    		String sqlDefinition = derivedAttribute.definition();
    		for (String unPrefixedKey : propertyKeys) {
    			String prefixedKey = prefix + unPrefixedKey;
    			String sqlString = e.sqlStringForAttributeNamed(prefixedKey);
    			if (StringUtils.isNotBlank(sqlString)) {
    				sqlDefinition = sqlDefinition.replaceAll(unPrefixedKey, sqlString);
    			}
    		}
    		
    		// Up to this point we have generated the SQL for the key by replacing
    		// it for the SQL for its definition and referenced properties.  Now
    		// we need to add the SQL for the selector and value in the qualifier.
    		
    		// Make sure that we have a value before we proceed.  If the value
    		// turns out to be an EOQualifierValue which is a place holder for
    		// a value then we don't have a value and we should throw an exception.
    		
    		NSSelector qualifierSelector = keyValueQualifier.selector();
    		Object qualifierValue = keyValueQualifier.value();
    		if ((qualifierValue instanceof EOQualifierVariable)) {
    			throw new IllegalStateException(
    					"sqlStringForKeyValueQualifier: attempt to generate SQL for " 
    						+ eoqualifier.getClass().getName() 
    						+ " " + eoqualifier 
    						+ " failed because the qualifier variable '$" 
    						+ ((EOQualifierVariable)qualifierValue).key() 
    						+ "' is unbound."
    				);
    		}
    		
    		String keyString = sqlDefinition;
    		
    		boolean isLike =
    				qualifierSelector.equals(EOQualifier.QualifierOperatorLike)
    					|| qualifierSelector.equals(EOQualifier.QualifierOperatorCaseInsensitiveLike);
    		
    		Object value;
    		if (isLike) {
    			// Convert the special literal used in like expressions, i.e. * and ?
    			// into their SQL equivalent % and _
    			value = e.sqlPatternFromShellPattern((String)qualifierValue);
    		} else {
    			value = qualifierValue;
    		}

    		String qualifierSQL;
    		if (qualifierSelector.equals(EOQualifier.QualifierOperatorCaseInsensitiveLike)) {
    			String valueString = sqlStringForAttributeValue(e, derivedAttribute, value);
    			qualifierSQL = e.sqlStringForCaseInsensitiveLike(valueString, keyString);
    		} else {
    			String valueString = sqlStringForAttributeValue(e, derivedAttribute, value);
    			String operatorString = e.sqlStringForSelector(qualifierSelector, value);
    			qualifierSQL = _NSStringUtilities.concat(keyString, " ", operatorString, " ", valueString);
    		}
    		if (isLike) {
    			char escapeChar = e.sqlEscapeChar();
    			if (escapeChar != 0) {
    				qualifierSQL = _NSStringUtilities.concat(qualifierSQL, " ESCAPE '" + escapeChar + "'");
    			}
    		}
    		
    		// If debug mode print something like this:
    		//
    		// KeyValueQualifierSQLGenerationSupport handled edge case for key-value qualifier with key referencing derived attribute: Order.customerAge
    		//    Key value qualifier: (order.customerAge > 30)
    		//    Key path: order.customerAge
    		//    Attribute customerAge is defined as: TRUNC(MONTHS_BETWEEN(orderDate,customer.birthDate)/12,0)
    		//    SQL generated: TRUNC(MONTHS_BETWEEN(T1.ORDER_DATE,T2.BIRTH_DATE)/12,0) > ?
    		// 
    		if (ERXExtensions._log.isDebugEnabled()) {
        		ERXExtensions._log.debug(getClass().getSimpleName() 
        				+ " handled edge case for key-value qualifier with key path"
        				+ " referencing a derived attribute: " 
        				+ derivedAttribute.entity().name() 
        				+ "." 
        				+ derivedAttribute.name()
        			);
        		ERXExtensions._log.debug("   Key value qualifier: "+ keyValueQualifier);
        		ERXExtensions._log.debug("   Key path: " + keyValueQualifier.key());
        		ERXExtensions._log.debug("   Attribute " + derivedAttribute.name() + " is defined as: " + derivedAttribute.definition());
        		ERXExtensions._log.debug("   SQL generated: " + qualifierSQL);
    		}
    		
    		return qualifierSQL;
    	}
        
        /**
         * Uses the EOSQLExpression provided to get the SQL string for value and
         * corresponding attribute.
         * 
         * @param e The EOSQLExpression to use to generate the SQL
         * @param att The attribute corresponding to the value passed in
         * @param value The value to convert to SQL
         * @return The SQL string for the value
         */
        public String sqlStringForAttributeValue(EOSQLExpression e, EOAttribute att, Object value) {
        	if (value != NSKeyValueCoding.NullValue 
        			&& (((e.useBindVariables()) && (e.shouldUseBindVariableForAttribute(att))) || (e.mustUseBindVariableForAttribute(att)))) {
        		
        		NSMutableDictionary<String, Object> binding = e.bindVariableDictionaryForAttribute(att, value);
        		e.addBindVariableDictionary(binding);
        		return (String)binding.objectForKey("BindVariablePlaceholder");
        	}
        	return e.formatValueForAttribute(value, att);
        }
        
        public static String formatValueForAttribute(EOSQLExpression e, Object value, EOAttribute attribute) {
        	return e.formatValueForAttribute(value, attribute);
        }

        /**
         * Normally EOF can handle key value qualifiers with a key corresponding to a
         * derived attribute, i.e. fullName attribute defined as firstName || ' ' || lastName.
         * However, if the key is a key path, i.e. customer.fullName then EOF throws an
         * exception.  This method checks to see if the key in the eoqualifier is a key
         * path and the last key in the key path corresponds to a derived attribute.
         * 
         * @param entity The entity where the eoqualifier is rooted
         * @param eoqualifier A qualifier to test
         * @return true if the eoqualifier is an EOKeyValueQualifier and its key is a
         * key path (with two or more keys) and the last key references a derived
         * attribute.
         */
        public static boolean isKeyPathWithDerivedAttributeCase(EOEntity entity, EOQualifier eoqualifier) {
             // Make sure it's a EOKeyValueQualifier as we need to get a key
            if (!(eoqualifier instanceof EOKeyValueQualifier)) {
                return false;
            }
            
            EOKeyValueQualifier keyValueQualifier = (EOKeyValueQualifier) eoqualifier;
            String keyPath = keyValueQualifier.key();
            
            // If it's not a key path with at least two keys then it's not
            // the edge case that we are looking for
            if (keyPath.contains(".") == false) {
                return false;
            }
            
            // Traverse the key path to get to last attribute referenced
            EOAttribute attr = destinationAttribute(entity, keyPath);
            
            // If the key path lead to an attribute that is derived then it is
            // the special case that we checking for.
            return attr != null && attr.isDerived();
        }
        
        /**
         * Returns the last attribute referenced by key path.
         * 
         * @param rootEntity The entity where the key path begins.
         * @param keyPath The key path leading to an attribute.
         * @return The attribute referenced by the last key in the key path.
         * If the last key in the key path is not an attribute then it returns null.
         */
        public static EOAttribute destinationAttribute(EOEntity rootEntity, String keyPath) {
            // Parse the keys in key path
            String[] keys = keyPath.split("\\.");
            
            // Traverse the key path to get to last attribute referenced
            EOAttribute attr = null;
            EOEntity entity = rootEntity;
            for (String key : keys) {
                EORelationship relationship = entity.anyRelationshipNamed(key);
                if (relationship != null) {
                    entity = relationship.destinationEntity();
                    attr = null;
                } else {
                    attr = entity.anyAttributeNamed(key);
                }
            }
            return attr;
        }

        /**
         * Given the definition of a derived attribute belonging to the entity provided
         * this method parses the definition looking for key paths that represent properties.
         * For example, a customerAge attribute in a hypothetical Order entity could have a
         * definition of 'TRUNC(MONTHS_BETWEEN(orderDate,customer.birthDate)/12,0)' and you
         * can call this method to get an array containing orderDate and customer.birthDate
         * which are the propertyKeys found in the definition.
         * 
         * @param derivedAttribute An EOAttribute with a definition
         * @return An array with the key paths referenced by the definition of the derived
         * attribute.
         */
        public static NSArray<String> parseDefinitionPropertyKeys(EOAttribute derivedAttribute) {
        	EOEntity entity = derivedAttribute.entity();
        	String definition = derivedAttribute.definition();
        	Pattern p = Pattern.compile("('[^']*')|[,]*+\\b([a-z]+[a-zA-Z0-9_\\.]*)");
        	Matcher m = p.matcher(definition);
        	NSMutableArray<String> propertyKeys = new NSMutableArray<>();
        	while (m.find()) {
        		// Please note that the regular expression has two groups separated
        		// with an or, i.e. |. The first group in the regular expression
        		// matches a single-quoted literal which are to be skipped because
        		// the text within it are not to be considered properties.
        		if (m.group(1) != null) {
        			continue;
        		}
        		
        		// The second group matches key paths preceded optionally with a comma
        		// as in the customerAge definition example. So get the key path and if
        		// consider it a property if it references an attribute when applied to
        		// the entity.
        		String keyPath = m.group(2);
        		if (destinationAttribute(entity, keyPath) != null) {
        			propertyKeys.add(keyPath);
        		}
        	}
        	return propertyKeys.immutableClone();
        }

        @Override
        public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier eoqualifier, EOEntity eoentity) {
            EOQualifier result = supportForQualifier(eoqualifier).schemaBasedQualifierWithRootEntity(eoqualifier, eoentity);
            return result;
        }

        @Override
        public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier eoqualifier, EOEntity eoentity, String s) {
            return supportForQualifier(eoqualifier).qualifierMigratedFromEntityRelationshipPath(eoqualifier, eoentity, s);
        }
    }

    /**
     * This method is called every time the configuration file
     * is changed. This allows for turning SQL debugging on and
     * off at runtime.
     * @param n notification posted when the configuration file
     * 	changes.
     */
    public void configureAdaptorContext(NSNotification n) {
        ERXExtensions.configureAdaptorContext();
    }
    
    /**
     * This method is called for the following notification
     * {@link EOSharedEditingContext#DefaultSharedEditingContextWasInitializedNotification}
     * 
     * @param n the notification.
     */
    public void sharedEditingContextWasInitialized(NSNotification n) {
    	EOSharedEditingContext sec = EOSharedEditingContext.defaultSharedEditingContext();
    	ERXEC._factory().setDefaultDelegateOnEditingContext(sec, true);
    }

    /** logging support for the adaptor channel */
    public static Logger adaptorLogger;

    /** logging support for shared object loading */
    public static Logger sharedEOadaptorLogger;

    /** flag to indicate if adaptor channel logging is enabled */
    private static Boolean adaptorEnabled;

    /** 
     * flag to indicate if rapid turn around is enabled for the
     * adaptor channel logging. 
     */
    private static boolean _isConfigureAdaptorContextRapidTurnAround = false;

    /**
     * Configures the passed in observer to register a call back 
     * when the configuration file is changed. This allows one to 
     * change a logger's setting and have that changed value change
     * the NSLog setting to log the generated SQL. This method is
     * called as part of the framework initialization process.
     * @param anObserver object to register the call back with.
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
            adaptorLogger = LoggerFactory.getLogger("er.transaction.adaptor.EOAdaptorDebugEnabled");
            
            sharedEOadaptorLogger = LoggerFactory.getLogger("er.transaction.adaptor.EOSharedEOAdaptorDebugEnabled");
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
        if (adaptorLogger != null) {
	        if (adaptorLogger.isDebugEnabled() && !adaptorEnabled.booleanValue()) {
	            targetState = Boolean.TRUE;
	        } else if (!adaptorLogger.isDebugEnabled() && adaptorEnabled.booleanValue()) {
	            targetState = Boolean.FALSE;
	        }
	        if (targetState != null) {
	        	setAdaptorLogging(targetState.booleanValue());
	        }
        }
    }

    /**
     * Returns the current state of EOAdaptor logging.
     */
	public static boolean adaptorLogging() {
		return NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess);
	}

    /**
     * Turn EOAdaptor logging on and off.
     * @param onOff
     */
    public static void setAdaptorLogging(boolean onOff) {
    	Boolean targetState = Boolean.valueOf(onOff);
    	if (NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess) != targetState.booleanValue()) {
			// Post a notification to give us a hook to perform other operations necessary to get logging going, e.g. change Logger settings, etc.
			NSNotificationCenter.defaultCenter().postNotification(new NSNotification(eoAdaptorLoggingWillChangeNotification, targetState));
    		if (targetState.booleanValue()) {
    			NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess);
    		} else {
    			NSLog.refuseDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration|NSLog.DebugGroupDatabaseAccess);
    		}
    	}
    	if (adaptorLogger != null) {
	    	if (targetState.booleanValue()) {
	    		adaptorLogger.info("Adaptor debug on");
	    	} else {
	    		adaptorLogger.info("Adaptor debug off");
	    	}
    	}	
    	adaptorEnabled = targetState;
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
     * is available to collect.
     * <p>
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
     * This method handles 3 different cases
     *
     * 1. keyPath is a single key and represents a relationship
     *		--&gt; addObjectToBothSidesOfRelationshipWithKey
     * 2. keyPath is a single key and does NOT represents a relationship
     * 3. keyPath is a real key path: break it up, navigate to the last atom
     *		--&gt; back to 1. or 2.
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
     * For a given enterprise object and key path, will return what
     * the key 'unit' returns from the userInfo dictionary of the
     * last property of the key path's EOAttribute or EORelationship.
     * The userInfo dictionary can be edited via EOModeler, it is that
     * open book looking icon when looking at either an attribute or
     * relationship.
     * <p>
     * For example if the userInfo dictionary or the attribute 'speed' on the
     * entity Car contained the key-value pair unit=mph, then this method
     * would be able to resolve that unit given either of these keypaths:
     * <pre><code>userInfoUnit(aCar, "speed");
     *userInfoUnit(aDrive, "toCar.speed");</code></pre>
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
     * different object with a prefix key path to a movie object.
     * 
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
            if (entity == null) {
                _log.warn("Attempting to refresh a non-existent (or not accessible) EO: " + entityName);
                return;
            }

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

    /**
     * This method can be used with Direct Action URLs to make sure
     * that the browser will reload the page. This is done by
     * adding the parameter [? | &amp;]r=random_number to the end of the
     * url.
     * @param daURL a url to add the randomization to.
     * @return url with the addition of the randomization key
     */
    // FIXME: Should check to make sure that the key 'r' isn't already present in the url.
    public static String randomizeDirectActionURL(String daURL) {
	    int r=ThreadLocalRandom.current().nextInt();
	    char c=daURL.indexOf('?')==-1 ? '?' : '&';
	    return  daURL+c+"r="+r;
    }
    /**
     * This method can be used with Direct Action URLs to make sure
     * that the browser will reload the page. This is done by
     * adding the parameter [? | &amp;]r=random_number to the end of the
     * url.
     * @param daURL a url to add the randomization to.
     */
    // FIXME: Should check to make sure that the key 'r' isn't already present in the url.
    public static void addRandomizeDirectActionURL(StringBuffer daURL) {
	    int r=ThreadLocalRandom.current().nextInt();
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
     * Adds the session ID for a given session to a given URL.
     * 
     * @param urlString
     *            URL string to add session ID form value to
     * @param session
     *            session object
     * @return URL with the addition of session ID form value
     */
    public static String addSessionIdFormValue(String urlString, WOSession session) {
    	if (urlString == null || session == null) {
    		_log.warn("not adding session ID: url=" + (urlString != null ? urlString : "<null>") + " session=" + (session != null ? session : "<null>"));
    		return urlString;
    	}
    	String sessionIdKey = WOApplication.application().sessionIdKey();
    	try {
			ERXMutableURL url = new ERXMutableURL(urlString);
			if (!url.containsQueryParameter(sessionIdKey)) {
				url.setQueryParameter(sessionIdKey, session.sessionID());
			}
			return url.toExternalForm();
		}
		catch (MalformedURLException e) {
			_log.error("invalid URL string: " + urlString, e);
		}
    	
    	return urlString;
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
     * Retrieves a value from the session's dictionary and evaluates
     * that object using the <code>booleanValue</code> method of
     * {@link ERXValueUtilities}. If there is no object corresponding
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
     * Constructs a unique key based on a context.
     * A method used by the preferences mechanism from ERDirectToWeb which
     * needs to be here because it is shared by ERDirectToWeb and ERCoreBusinessLogic.
     * 
     * @param key preference key
     * @param context most likely a d2wContext object
     * @return a unique preference key for storing and retrieving preferences
     */
    // FIXME: Needs to find a better home.
    public static String userPreferencesKeyFromContext(String key, NSKeyValueCoding context) {
        StringBuilder result = new StringBuilder(key);
        result.append('.');
        String pc=(String)context.valueForKey("pageConfiguration");
        if (pc==null || pc.length()==0) {
            String en="_All_";
            EOEntity e=(EOEntity)context.valueForKey("entity");
            if (e!=null) en=e.name();
            result.append("__");
            result.append(context.valueForKey("task"));
            result.append('_');
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
     * @deprecated use {@link ERXRuntimeUtilities#freeProcessResources(Process)} instead
     */
    @Deprecated
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
            if (m.getName().equals(methodName) && Arrays.equals(m.getParameterTypes(), parameters)) {
                implementsMethod = true; break;
            }
        }
        return implementsMethod;
    }

    /**
     * Initializes your WOApplication programmatically (for use in test cases and main methods) with
     * the assumption that the current directory is your main bundle URL.
     * 
     * @param applicationSubclass your Application subclass
     * @param args the commandline arguments for your application
     */
    public static void initApp(Class applicationSubclass, String[] args) {
		try {
	    	File woaFolder = new File(".").getCanonicalFile();
	    	if (!woaFolder.getName().endsWith(".woa")) {
	    		if (new File(woaFolder, ".project").exists()) {
	    			File buildFolder = new File(new File(woaFolder, "build"), woaFolder.getName() + ".woa");
	    			if (buildFolder.exists()) {
	    				woaFolder = buildFolder;
	    			}
	    			else {
		    			File distFolder = new File(new File(woaFolder, "dist"), woaFolder.getName() + ".woa");
	    				if (distFolder.exists()) {
	    					woaFolder = distFolder;
	    				}
	    				else {
	    					//Bundle-less builds. Yay!
	    		    		//throw new IllegalArgumentException("You must run your application from a .woa folder to call this method.");
	    				}
	    			}
	    		}
	    	}
	    	ERXExtensions.initApp(null, woaFolder.toURI().toURL(), applicationSubclass, args);
		}
		catch (IOException e) {
			throw new NSForwardException(e);
		}
    }
    
    /**
     * Initializes your WOApplication programmatically (for use in test cases and main methods).
     * 
     * @param mainBundleName the name of your main bundle
     * @param applicationSubclass your Application subclass
     * @param args the commandline arguments for your application
     */
    public static void initApp(String mainBundleName, Class applicationSubclass, String[] args) {
    	ERXExtensions.initApp(mainBundleName, null, applicationSubclass, args);
    }
    
    private static boolean _appInitialized = false;
    /**
     * Initializes your WOApplication programmatically (for use in test cases and main methods).
     * 
     * @param mainBundleName the name of your main bundle (or null to use mainBundleURL)
     * @param mainBundleURL the URL to your main bundle (ignored if mainBundleName is set)
     * @param applicationSubclass your Application subclass
     * @param args the commandline arguments for your application
     */
    public static void initApp(String mainBundleName, URL mainBundleURL, Class applicationSubclass, String[] args) {
    	if (_appInitialized) {
    		return;
    	}
    	try {
	        ERXApplication.setup(args);
	        if (mainBundleURL != null) {
		        System.setProperty("webobjects.user.dir", new File(mainBundleURL.getFile()).getCanonicalPath());
	        }
	        // Odds are you are only using this method for test cases and development mode
	        System.setProperty("er.extensions.ERXApplication.developmentMode", "true");
	        ERXApplication.primeApplication(mainBundleName, mainBundleURL, applicationSubclass.getName());
	        //NSNotificationCenter.defaultCenter().postNotification(new NSNotification(ERXApplication.ApplicationDidCreateNotification, WOApplication.application()));
		}
		catch (IOException e) {
			throw new NSForwardException(e);
		}
    	_appInitialized = true;
    }
    
    /**
     * Initializes Wonder EOF programmatically (for use in test cases and main methods).  You do
     * not need to call this method if you already called initApp.  This is lighter-weight than 
     * initApp, and tries to just get enough configured to make EOF work properly.  This method
     * assumes you are running your app from a .woa folder.
     * 
     * <p>This is equivalent to calling <code>initEOF(new File("."), args)</code>.</p>
     * 
     * @param args the commandline arguments for your application
     * @throws IllegalArgumentException if the current dir or mainBundleFolder is not a *.woa bundle.
     */
    public static void initEOF(final String[] args) {
    	ERXExtensions.initEOF(new File("."), args);
    }
    /**
     * Initializes Wonder EOF programmatically (for use in test cases and main methods).  You do
     * not need to call this method if you already called initApp.  This is lighter-weight than 
     * initApp, and tries to just get enough configured to make EOF work properly.
     * 
     * <p>This is equivalent to calling <code>initEOF(mainBundleFolder, args, true, true, true)</code>.</p>
     * 
     * @param mainBundleFolder the folder of your main bundle
     * @param args the commandline arguments for your application
     * @throws IllegalArgumentException if the current dir or mainBundleFolder is not a *.woa bundle.
     */
    public static void initEOF(final File mainBundleFolder, final String[] args) {
    	initEOF(mainBundleFolder, args, true, true, true);
    }
    /**
     * <p>
     * Initializes Wonder EOF programmatically (for use in test cases and main methods).  You do
     * not need to call this method if you already called initApp.  This is lighter-weight than 
     * initApp, and tries to just get enough configured to make EOF work properly. This method is also,
     * unlike {@link #initEOF(String[])} or {@link #initEOF(File, String[])}, not so restrictive as to
     * require the name of the bundle be a <code>*.woa</code>, and nor does it require <code>*.framework</code>
     * as the name of the bundle.
     * </p>
     * <p>
     * It can therefore be useful for, and used with, folder, jar or war
     * bundles -- whichever bundle is referenced by <code>mainBundleURI</code> -- so long as it is
     * bundle-like in content rather than by name. For NSBundle, this usually just requires a Resources folder
     * within the bundle.
     * </p>
     * <p>
     * For example, if you're build tool compiles sources and puts Resources under <code>target/classes</code>
     * you can call this method via <code>ERXExtensions.initEOF(new File(projectDir, "target/classes"), args, true)</code>.
     * </p>
     * <p><b>Note 1:</b>
     *  this will set the system property <code>webobjects.user.dir</code> to the canonical path of the 
     * given bundle uri if, and only if, the bundle uri points to a directory and is schema is <code>file</code>.
     * </p>
     * <p><b>Note 2:</b>
     *  this will set NSBundle's mainBundle to the referenced bundle loaded via
     *  {@link er.extensions.foundation.ERXRuntimeUtilities#loadBundleIfNeeded(File)} if found.
     * </p>
     * 
     * <p>This is equivalent to calling <code>initEOF(mainBundleFolder, args, assertsBundleExists, false, true)</code>.</p>
     * 
     * @param mainBundleFile the archive file or directory of your main bundle
     * @param args the commandline arguments for your application
     * @param assertsBundleExists ensures that the bundle exists and is loaded
     * @throws NSForwardException if the given bundle doesn't satisfy the given assertions or
     *  		ERXRuntimeUtilities.loadBundleIfNeeded or ERXApplication.setup fails.
     * @see #initEOF(File, String[], boolean, boolean, boolean)
     */
    public static void initEOF(final File mainBundleFile, final String[] args, boolean assertsBundleExists) {
    	initEOF(mainBundleFile, args, assertsBundleExists, false, true);
    }
    private static boolean _eofInitialized = false;
    private static final Lock _eofInitializeLock = new ReentrantLock();
    /**
     * <p>
     * Initializes Wonder EOF programmatically (for use in test cases and main methods).  You do
     * not need to call this method if you already called initApp.  This is lighter-weight than 
     * initApp, and tries to just get enough configured to make EOF work properly. This method is also,
     * unlike {@link #initEOF(String[])} or {@link #initEOF(File, String[])}, not so restrictive as to
     * require the name of the bundle be a <code>*.woa</code>, and nor does it require <code>*.framework</code>
     * as the name of the bundle.
     * </p>
     * <p>
     * It can therefore be useful for, and used with, folder, jar or war
     * bundles -- whichever bundle is referenced by <code>mainBundleURI</code> -- so long as it is
     * bundle-like in content rather than by name. For NSBundle, this usually just requires a Resources folder
     * within the bundle.
     * </p>
     * <p>
     * For example, if you're build tool compiles sources and puts Resources under <code>target/classes</code>
     * you can call this method via <code>ERXExtensions.initEOF(new File(projectDir, "target/classes").toURI(), args)</code>.
     * </p>
     * <p><b>Note 1:</b>
     *  this will set the system property <code>webobjects.user.dir</code> to the canonical path of the 
     * given bundle uri if, and only if, the bundle uri points to a directory and is schema is <code>file</code>.
     * </p>
     * <p><b>Note 2:</b>
     *  this will set NSBundle's mainBundle to the referenced bundle loaded via
     *  {@link er.extensions.foundation.ERXRuntimeUtilities#loadBundleIfNeeded(File)} if found.
     * </p>
     * 
     * @param mainBundleFile the archive file or directory of your main bundle
     * @param args the commandline arguments for your application
     * @param assertsBundleExists ensures that the bundle exists and is loaded
     * @param assertsBundleIsWOApplicationFolder ensures that the bundle referenced by mainBundleFile, or the current dir if fallbackToUserDirAsBundle is true, is a <code>*.woa</code> bundle folder.
     * @param fallbackToUserDirAsBundle falls back to current dir if the mainBundleFile does not exist
     * @throws NSForwardException if the given bundle doesn't satisfy the given assertions or
     *  		ERXRuntimeUtilities.loadBundleIfNeeded or ERXApplication.setup fails.
     * @see er.extensions.foundation.ERXRuntimeUtilities#loadBundleIfNeeded(File)
     * @see NSBundle#_setMainBundle(NSBundle)
     * @see er.extensions.appserver.ERXApplication#setup(String[])
     * @see #bundleDidLoad(NSNotification)
     */
    public static void initEOF(final File mainBundleFile, String[] args, boolean assertsBundleExists, boolean assertsBundleIsWOApplicationFolder, boolean fallbackToUserDirAsBundle) {
    	_eofInitializeLock.lock();
    	try {
	    	if (!_eofInitialized) {
	    		try {
	    			File bundleFile = mainBundleFile;
	    			
	    			if (assertsBundleIsWOApplicationFolder) {
	    				if (!bundleFile.exists() || !bundleFile.getName().endsWith(".woa")) {
	    					bundleFile = new File(".").getCanonicalFile();
	    					if (!bundleFile.exists() || !bundleFile.getName().endsWith(".woa")) {
	    						throw new IllegalArgumentException("Assertion failure. You must run your application from the .woa folder to call this method.");
	    					}
	    				}
	    			}
	    			
	    			if (assertsBundleExists) {
	    				if (bundleFile == null || !bundleFile.exists()) {
	    					if (fallbackToUserDirAsBundle) {
	    						bundleFile = new File(".").getCanonicalFile();
	    					} else {
	    						throw new IllegalArgumentException("Assertion failure. The main bundle is required to exist to call this method.");
	    					}
	    				}
	    			}
	    			
	    			if (bundleFile != null && bundleFile.isDirectory()) {
	    				System.setProperty("webobjects.user.dir", bundleFile.getCanonicalPath());
	    			}
	    			
	    			NSBundle mainBundle = null;
	    			try {
	    				mainBundle = ERXRuntimeUtilities.loadBundleIfNeeded(bundleFile);
	    				if (mainBundle == null) {
							throw new IllegalArgumentException("The main bundle failed to load.");
						}
	    				NSBundle._setMainBundle(mainBundle);
						NSLog.debug.appendln("initEOF setting main bundle to " + mainBundle);
	    			}
	    			catch (Exception e) {
	    				if (assertsBundleExists) {
	    					throw e;
	    				}
	    			}
									
					ERXApplication.setup(args);
					ERXFrameworkPrincipal.sharedInstance(ERXExtensions.class).bundleDidLoad(null);
				}
				catch (Exception e) {
					throw new NSForwardException(e);
				}
		    	_eofInitialized = true;
	    	}
    	} finally {
    		_eofInitializeLock.unlock();
    	}
    }
}
