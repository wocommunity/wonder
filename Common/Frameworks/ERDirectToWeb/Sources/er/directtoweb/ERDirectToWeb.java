/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.net.URL;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.directtoweb.KeyValuePath;
import com.webobjects.directtoweb.QueryPageInterface;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.ERXConfigurationManager;
import er.extensions.ERXConstant;
import er.extensions.ERXExtensions;
import er.extensions.ERXFileUtilities;
import er.extensions.ERXFrameworkPrincipal;
import er.extensions.ERXKeyValuePair;
import er.extensions.ERXLocalizer;
import er.extensions.ERXProperties;
import er.extensions.ERXValueUtilities;

/**
 * Principle class of the ERDirectToWeb framework.
 * This class is loaded when the NSBundle of this 
 * framework is loaded. When loaded this class configures
 * the directtoweb runtime to use the {@link ERD2WModel} and 
 * {@link ERD2WFactory} subclasses instead of the default
 * implementations. See each class for a description of the 
 * additions/improvements made to the base implementation.
 * This class also has a bunch of utility methods that are
 * used throughout this framework.
 */
public class ERDirectToWeb extends ERXFrameworkPrincipal {

    public final static Class REQUIRES[] = new Class[] {ERXExtensions.class};
    
    /** logging support */
    public final static Logger log = Logger.getLogger("er.directtoweb.ERDirectToWeb");
    public final static String D2WDEBUGGING_ENABLED_KEY = "ERDirectToWeb_d2wDebuggingEnabled";
    public final static String D2WDISPLAY_COMPONENTNAMES_KEY = "ERDirectToWeb_displayComponentNames";
    public final static String D2WDISPLAY_PROPERTYKEYS_KEY = "ERDirectToWeb_displayPropertyKeys";
    public final static Logger debugLog = Logger.getLogger("er.directtoweb.ERD2WDebugEnabled");
    public final static Logger componentNameLog = Logger.getLogger("er.directtoweb.ERD2WDebugEnabled.componentName");
    public final static Logger propertyKeyLog = Logger.getLogger("er.directtoweb.ERD2WDebugEnabled.propertyKey");

    static {
    	setUpFrameworkPrincipalClass (ERDirectToWeb.class);
    }

    public void finishInitialization() {
        ERD2WModel model=ERD2WModel.erDefaultModel();        // force initialization
    	// NOTE: doing Class.ERD2WModel doesn't seem enough
    	// to guarantee fire of ERD2WModel's static initializer
//  	Configures the system for trace rule firing.
        if(!(D2W.factory() instanceof ERD2WFactory)) {
        	D2W.setFactory(new ERD2WFactory());
        }
    	configureTraceRuleFiringRapidTurnAround();
    	ERDirectToWeb.warmUpRuleCache();
        model.checkRules();
    	NSNotificationCenter.defaultCenter().addObserver(this,
    	        new NSSelector("resetModel",
    	                ERXConstant.NotificationClassArray),
    	                ERXLocalizer.LocalizationDidResetNotification,
    					null);
    	NSNotificationCenter.defaultCenter().addObserver(this,
    			new NSSelector("sortRules",
    					ERXConstant.NotificationClassArray),
    					ERD2WModel.WillSortRules,
    					null);
    }

    public void resetModel(NSNotification n) {
    	ERD2WModel.erDefaultModel().resetModel();
    }
    
    public void sortRules(NSNotification n) {
    	ERD2WModel model = (ERD2WModel)n.object();
    	if(ERD2WModel.erDefaultModel() == model) {
    		URL url = ERXFileUtilities.pathURLForResourceNamed("d2wClient.d2wmodel", "ERDirectToWeb", null);
    		model.mergePathURL(url);
    	}
    }

    public static void setD2wDebuggingEnabled(WOSession s, boolean enabled) {
        ERXExtensions.setBooleanFlagOnSessionForKey(s, D2WDEBUGGING_ENABLED_KEY, enabled);
        if (!enabled) {
            setD2wComponentNameDebuggingEnabled(s, false);
            setD2wPropertyKeyDebuggingEnabled(s, false);
        }
    }
    public static boolean d2wDebuggingEnabled(WOSession s) {
        return ERXExtensions.booleanFlagOnSessionForKeyWithDefault(s,
                                                                  D2WDEBUGGING_ENABLED_KEY,
                                                                  debugLog.isDebugEnabled());
    }

    public static void setD2wComponentNameDebuggingEnabled(WOSession s, boolean enabled) {
        ERXExtensions.setBooleanFlagOnSessionForKey(s, D2WDISPLAY_COMPONENTNAMES_KEY, enabled);
    }
    public static boolean d2wComponentNameDebuggingEnabled(WOSession s) {
        return ERXExtensions.booleanFlagOnSessionForKeyWithDefault(s,
                                                                  D2WDISPLAY_COMPONENTNAMES_KEY,
                                                                  componentNameLog.isDebugEnabled());
    }

    public static void setD2wPropertyKeyDebuggingEnabled(WOSession s, boolean enabled) {
        ERXExtensions.setBooleanFlagOnSessionForKey(s, D2WDISPLAY_PROPERTYKEYS_KEY, enabled);
    }
    public static boolean d2wPropertyKeyDebuggingEnabled(WOSession s) {
        return ERXExtensions.booleanFlagOnSessionForKeyWithDefault(s,
                                                                  D2WDISPLAY_PROPERTYKEYS_KEY,
                                                                  propertyKeyLog.isDebugEnabled());
    }
    
    public static String resolveUnit(String userInfoUnitString,
                                     EOEnterpriseObject object,
                                     String prefixKeyPath) {
        // allows units stored in the user info of the attribute to take the form of @project.unit
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
     * Checks if a given property key is in the format (foo) or [foo] and returns the stripped string.
     * @param s the String to convert
     * @param start the start char
     * @param start the end char to check for
     * @return stripped String or null if the string does not start with <code>start</code> and ends with <code>end</code>.
     */
    public static String convertedPropertyKeyFromString(String s, char start, char end) {
        if(s.length()> 2) {
            if(s.charAt(0) == start && s.charAt(s.length()-1) == end) {
                return s.substring(1, s.length() - 1);
            }
        }
        return null;
    }

    /**
     * Converts a given array of keys to a format usable for section and tab display.
     * For example ("(foo)", bar, baz) is transformed to a list of ERD2WContainers usable for section display.
     * The format ((foo, bar, baz)) is also understood.
     * @param keyArray the NSArray to convert
     * @param start the start char
     * @param start the end char to check for
     * @return nested NSMutableArray.
     */
    public static NSMutableArray convertedPropertyKeyArray(NSArray keyArray, char startChar, char endChar) {
        NSMutableArray result = new NSMutableArray();
        Object firstValue = null;
        if(keyArray.count() > 0) {
            firstValue = keyArray.objectAtIndex(0);
        }
        if(firstValue != null) {
            boolean isKeyArrayFormat = false;
            if(firstValue instanceof String)
                isKeyArrayFormat = convertedPropertyKeyFromString((String)firstValue, startChar, endChar) != null;
            if(firstValue instanceof String && !isKeyArrayFormat) {
                ERD2WContainer c=new ERD2WContainer();
                c.name = "";
                c.keys = new NSMutableArray(keyArray);
                result.addObject(c);
            } else {
                NSMutableArray tmp = null;
                for (Enumeration e = keyArray.objectEnumerator(); e.hasMoreElements();) {
                    if(isKeyArrayFormat) {
                        String currentValue = (String)e.nextElement();
                        String currentLabel = convertedPropertyKeyFromString(currentValue, startChar, endChar);
                        if(currentLabel != null) {
                            ERD2WContainer c=new ERD2WContainer();
                            c.name = currentLabel;
                            tmp = new NSMutableArray();
                            c.keys = tmp;
                            result.addObject(c);
                        } else {
                            tmp.addObject(currentValue);
                        }
                    } else {
                        NSArray current = (NSArray)e.nextElement();
                        ERD2WContainer c=new ERD2WContainer();
                        c.name = (String)current.objectAtIndex(0);
                        c.keys = current.mutableClone();
                        c.keys.removeObjectAtIndex(0);
                        result.addObject(c);
                    }
                }
            }
        }
        return result;
    }

    // This defaults to true.
    public static boolean booleanForKey(D2WContext context, String key) {
    	return ERXValueUtilities.booleanValue(context.valueForKey(key));
    }

    // DELETEME: This is duplicated from ERExtensions
    public static String userInfoUnit(EOEnterpriseObject object, String key) {
        // return the unit stored in the userInfo dictionary of the appropriate EOAttribute
        EOEntity entity=null;
        String lastKey=null;
        String result=null;
        if (object == null || key == null) {
            log.warn("UserInfoUnit: Attempting to relsolve a unit for object: " + object + " key: " + key);
        } else if (key.indexOf(".")==-1) {
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
            Object property=entity.attributeNamed(lastKey);
            property=property==null ? entity.relationshipNamed(lastKey) : property;
            //BOOGIE
            EOAttribute a=entity.attributeNamed(lastKey);
            NSDictionary userInfo=null;
            if (a!=null) userInfo=a.userInfo();
            else {
                EORelationship r=entity.relationshipNamed(lastKey);
                if (r!=null) userInfo=r.userInfo();
            }
            //
           // NSDictionary userInfo=(NSDictionary)(property!=null ? property.valueForKey("userInfo") : null);
            result= (String)(userInfo!=null ? userInfo.valueForKey("unit") : null);
        }
        return result;
    }

    /** @deprecated use ERD2WFactory.erFactory().printerFriendlyPageForD2WContext(D2WContext context, WOSession session)*/
    public static WOComponent printerFriendlyPageForD2WContext(D2WContext context, WOSession session) {
        return ERD2WFactory.erFactory().printerFriendlyPageForD2WContext(context, session);
    }

    /** @deprecated use ERD2WFactory.erFactory().csvExportPageForD2WContext(D2WContext context, WOSession session)*/
    public static WOComponent csvExportPageForD2WContext(D2WContext context, WOSession session) {
        return ERD2WFactory.erFactory().csvExportPageForD2WContext(context, session);
    }

    /** @deprecated use ERD2WFactory.erFactory().pageForTaskSubTaskAndEntityNamed(String task, String subtask, String entityName, WOSession session)*/
    public static WOComponent pageForTaskSubTaskAndEntityNamed(String task, String subtask, String entityName, WOSession session) {
        return ERD2WFactory.erFactory().pageForTaskSubTaskAndEntityNamed(task, subtask, entityName, session);
    }

    /** @deprecated use ERD2WFactory.erFactory().queryPageWithFetchSpecificationForEntityNamed(String fsName, String entityName, WOSession session)*/
    public static QueryPageInterface queryPageWithFetchSpecificationForEntityNamed(String fsName, String entityName, WOSession session) {
        return ERD2WFactory.erFactory().queryPageWithFetchSpecificationForEntityNamed(fsName, entityName, session);
    }

    /** @deprecated use ERD2WFactory.erFactory().errorPageForException(Throwable e, WOSession session)*/
    public static WOComponent errorPageForException(Throwable e, WOSession session) {
        return ERD2WFactory.erFactory().errorPageForException(e, session);
    }

    /**
     * Subclass of NSForwardException that can hold a d2wContext. Usefull when the exception 
     * occurs while evaluating embedded components. The page's d2wContext will not show you the error.
     * 
     * @author ak
     */
    public static class D2WException extends NSForwardException {
    	
		private D2WContext _context;
		
		public D2WException(Exception ex, D2WContext context) {
			super(ex);
			_context = context;
		}
		
		public D2WContext d2wContext() {
			return _context;
		}
	}
    
    /**
     * Logs some debugging info and and throws a D2WException that wraps the original exception.
     * This is usefull when your app fails very deep inside of a repetition of switch components
     * and you need to find out just what the state of the D2WContext is.
     * @param ex
     * @param d2wContext
     */
    public static synchronized void reportException(Exception ex, D2WContext d2wContext) {
        if(d2wContext != null) {
            log.error("Exception <"+ex+">: "+
                      "pageConfiguration <" + d2wContext.valueForKeyPath("pageConfiguration") + ">, "+
                      "propertyKey <" + d2wContext.propertyKey() + ">, "+
                      "entityName <" + d2wContext.valueForKeyPath("entity.name") + ">, "+
                      "displayPropertyKeys <" +d2wContext.valueForKeyPath("displayPropertyKeys")+ ">, "+
                      "componentName <" + d2wContext().valueForKey("componentName") + ">, "+
                      "customComponent <" +  d2wContext().valueForKey("customComponentName") + ">", ex);
        } else {
            log.error("Exception <"+ex+">: with NULL d2wContext"/*, ex*/);
        }
        if(shouldRaiseException(true)) {
            if(!(ex instanceof D2WException)) {
                ex = new D2WException(ex, d2wContext);
            }
            throw (D2WException)ex;
        }
    }
    
    /**
     * Checks the system property <code>er.directtoweb.ERDirectToWeb.shouldRaiseExceptions</code>.
     * @param defaultValue
     */
    public static boolean shouldRaiseException(boolean defaultValue) {
        return ERXProperties.booleanForKeyWithDefault("er.directtoweb.ERDirectToWeb.shouldRaiseExceptions", defaultValue);
    }
    
    public static synchronized String displayNameForPropertyKey(String key, String entityName) {
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        d2wContext()._localValues().clear();
        //ERD2WUtilities.resetContextCache(d2wContext());
        d2wContext().setEntity(entity);
        d2wContext().setPropertyKey(key);
        String result = d2wContext().displayNameForProperty();
        d2wContext()._localValues().clear();
        return result;
    }

    // Needs to be a late init because then it will hook itself up to the correct D2WModel
    private static D2WContext _context;
    private static D2WContext d2wContext() {
        if (_context == null)
            _context = ERD2WContext.newContext();
        return _context;
    }

    public static Object d2wContextValueForKey(String key, String entityName) {
        return d2wContextValueForKey(key, entityName, null);
    }

    public static synchronized Object  d2wContextValueForKey(String key, String entityName, NSDictionary extraValuesForContext) {
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        d2wContext()._localValues().clear();
        // ERD2WUtilities.resetContextCache(d2wContext());
        d2wContext().setEntity(entity);
        if (extraValuesForContext!=null) {
            d2wContext().takeValuesFromDictionary(extraValuesForContext);
            /*
            for (Enumeration e=extraValuesForContext.allKeys().objectEnumerator(); e.hasMoreElements();) {
                String k=(String)e.nextElement();
                d2wContext().takeValueForKey(extraValuesForContext.objectForKey(k),k);
            }*/
        }
        Object result = d2wContext().valueForKey(key);
        d2wContext()._localValues().clear();
        return result;
    }

    
    public static String createConfigurationForEntityNamed(String entityName) {
        return (String)d2wContextValueForKey("createConfigurationName", entityName);
    }

    public static void warmUpRuleCache() {
        log.debug("Preparing DirectToWeb Data Structures");
        ERD2WModel.erDefaultModel().prepareDataStructures();
    }

    public static Logger trace;

    public void configureTraceRuleFiring(NSNotification n) {
        ERDirectToWeb.configureTraceRuleFiring();
    }

    private void configureTraceRuleFiringRapidTurnAround() {
        if (trace == null) {
            // otherwise not properly initialized
            trace = Logger.getLogger("er.directtoweb.rules.D2WTraceRuleFiringEnabled");
            // Note: If the configuration file says debug, but the command line parameter doesn't we need to turn
            //   rule tracing on.
            // BOOGIE
            configureTraceRuleFiring();
            NSNotificationCenter.defaultCenter().addObserver(ERXFrameworkPrincipal.sharedInstance(ERDirectToWeb.class),
                                                             new NSSelector("configureTraceRuleFiring",
                                                                            ERXConstant.NotificationClassArray),
                                                             ERXConfigurationManager.ConfigurationDidChangeNotification,
                                                             null);
        }
    }
    
    // This is the actual method that turns trace rule firign on and off.
    public static void configureTraceRuleFiring() {
        //AK: we can trace firing much more fine-grained than the default engine
        // and also enabling the debug level NSLog spews out a ton of ridiculous 
        // info about images and the like, so we leave the NSLog alone...
        if (trace.isDebugEnabled() && !NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupRules)) {
            //NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupRules);
            //NSLog.setAllowedDebugLevel(NSLog.DebugLevelDetailed);
            trace.info("Rule tracing on");
        } else if (!trace.isDebugEnabled() && NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupRules)) {
            //NSLog.refuseDebugLoggingForGroups(NSLog.DebugGroupRules);
            trace.info("Rule tracing off");
        }
    }

    public static NSArray displayableArrayForKeyPathArray(NSArray array, String entityForReportName){
        NSMutableArray result = new NSMutableArray();
        for(Enumeration e = array.objectEnumerator(); e.hasMoreElements(); ){
            String key = (String)e.nextElement();
            result.addObject(new ERXKeyValuePair(key, ERDirectToWeb.displayNameForPropertyKey(key, entityForReportName)));
        }
        return (NSArray)result;
    }
}
