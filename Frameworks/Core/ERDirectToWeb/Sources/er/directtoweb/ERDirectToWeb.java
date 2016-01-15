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
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.directtoweb.KeyValuePath;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSortOrdering;
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

import er.directtoweb.pages.ERD2WPage;
import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;
import er.extensions.appserver.ERXSession;
import er.extensions.appserver.ERXWOContext;
import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXConfigurationManager;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXKeyValuePair;
import er.extensions.foundation.ERXPatcher;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * Principle class of the ERDirectToWeb framework.
 * This class is loaded when the NSBundle of this 
 * framework is loaded. When loaded this class configures
 * the directtoweb runtime to use the {@link er.directtoweb.ERD2WModel} and
 * {@link er.directtoweb.ERD2WFactory} subclasses instead of the default
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
    public final static String D2WDISPLAY_PAGE_METRICS_KEY = "ERDirectToWeb_displayPageMetrics";
    public final static String D2WDISPLAY_DETAILED_PAGE_METRICS_KEY = "ERDirectToWeb_displayDetailedPageMetrics";
    public final static Logger debugLog = Logger.getLogger("er.directtoweb.ERD2WDebugEnabled");
    public final static Logger componentNameLog = Logger.getLogger("er.directtoweb.ERD2WDebugEnabled.componentName");
    public final static Logger propertyKeyLog = Logger.getLogger("er.directtoweb.ERD2WDebugEnabled.propertyKey");
    public final static NSSelector D2WCONTEXT_SELECTOR = new NSSelector("d2wContext");

    static {
    	setUpFrameworkPrincipalClass (ERDirectToWeb.class);
    }

    @Override
    public void finishInitialization() {
        fixClasses();
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

    
    private void fixClasses(String oldName, String newName) {
        NSArray<String> names =  NSBundle.bundleForClass(getClass()).bundleClassNames();
        for (String name : names) {
            if(name.startsWith(newName)) {
                Class clazz = ERXPatcher.classForName(name);
                name = name.replaceFirst(newName + "(\\.[a-z]+)?", oldName);
                ERXPatcher.setClassForName(clazz, name);
            }
        }
    } 


    private void fixClasses() {
        fixClasses("er.directtoweb", "er.directtoweb.assignments");
        fixClasses("er.directtoweb", "er.directtoweb.assignments.delayed");
        fixClasses("er.directtoweb", "er.directtoweb.assignments.defaults");
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

    public static boolean pageMetricsEnabled() {
        return ERXExtensions.booleanFlagOnSessionForKeyWithDefault(ERXSession.session(), D2WDISPLAY_PAGE_METRICS_KEY, false);
    }

    public static void setPageMetricsEnabled(boolean value) {
        ERXExtensions.setBooleanFlagOnSessionForKey(ERXSession.session(), D2WDISPLAY_PAGE_METRICS_KEY, value);
    }

    public static boolean detailedPageMetricsEnabled() {
        return ERXExtensions.booleanFlagOnSessionForKeyWithDefault(ERXSession.session(), D2WDISPLAY_DETAILED_PAGE_METRICS_KEY, false);
    }

    public static void setDetailedPageMetricsEnabled(boolean value) {
        ERXExtensions.setBooleanFlagOnSessionForKey(ERXSession.session(), D2WDISPLAY_DETAILED_PAGE_METRICS_KEY, value);
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
     *
     * @param s the String to convert
     * @param start the start char
     * @param end the end char to check for
     *
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
     * @param startChar the start char
     * @param endChar the end char to check for
     *
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

    /**
     * Returns a valid sort ordering based on the <code>defaultSortOrdering</code> key.
     * @param d2wContext
     */
	public static NSArray<EOSortOrdering> sortOrderings(D2WContext d2wContext) {
		NSMutableArray<EOSortOrdering> validatedSortOrderings = new NSMutableArray<EOSortOrdering>();
		NSArray<String> sortOrderingDefinition = (NSArray<String>) d2wContext.valueForKey("defaultSortOrdering");
		if (sortOrderingDefinition != null) {
			for (int i = 0; i < sortOrderingDefinition.count();) {
				String sortKey = sortOrderingDefinition.objectAtIndex(i++);
				String sortSelectorKey = sortOrderingDefinition.objectAtIndex(i++);
				EOSortOrdering sortOrdering = new EOSortOrdering(sortKey, ERXArrayUtilities.sortSelectorWithKey(sortSelectorKey));
				validatedSortOrderings.addObject(sortOrdering);
			}
			if (log.isDebugEnabled()) {
				log.debug("Found sort Orderings in rules " + validatedSortOrderings);
			}
		}
		return validatedSortOrderings;
	}

    // This defaults to true.
    public static boolean booleanForKey(D2WContext context, String key) {
    	return ERXValueUtilities.booleanValue(context.valueForKey(key));
    }

    /**
     * @deprecated This is duplicated from {link: er.extensions.ERXExtensions#userInfoUnit(EOEnterpriseObject, String)}
     */
    public static String userInfoUnit(EOEnterpriseObject object, String key) {
    	String result = er.extensions.ERXExtensions.userInfoUnit(object, key);
        return result;
    }

    /**
     * Subclass of NSForwardException that can hold a d2wContext. Useful when the exception
     * occurs while evaluating embedded components. The page's d2wContext will not show you the error.
     * 
     * @author ak
     */
    public static class D2WException extends NSForwardException {
    	/**
    	 * Do I need to update serialVersionUID?
    	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
    	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
    	 */
    	private static final long serialVersionUID = 1L;

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
     * This is useful when your app fails very deep inside of a repetition of switch components
     * and you need to find out just what the state of the D2WContext is.
     * @param ex
     * @param d2wContext
     * @d2wKey componentName
     * @d2wKey customComponentName
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
     * Gathers D2W-related information from the current context.  This is mainly useful for debugging.
     * @param context the current context
     * @return a dictionary of D2W-related keys to describe the D2W state of the context.
     */
    public static synchronized NSMutableDictionary informationForContext(WOContext context) {
        NSMutableDictionary info = new NSMutableDictionary();
        D2WContext d2wContext = null;
        NSArray componentStack = ERXWOContext._componentPath(context);
        // Try to get the information for the D2WPage closest to the end of the component stack, i.e., more specific
        // info., that is especially helpful for finding problems in embedded page configurations.
        WOComponent component = null;
        for (Enumeration componentsEnum = componentStack.reverseObjectEnumerator(); componentsEnum.hasMoreElements();) {
            WOComponent c = (WOComponent)componentsEnum.nextElement();
            if (c instanceof D2WPage) {
                component = c;
                break;
            }
        }
        if (null == component) { // Fall back to the highest level page.
            component = context.page();
        }

        try {
            d2wContext = (D2WContext)component.valueForKey("d2wContext");
        } catch (NSKeyValueCoding.UnknownKeyException uke) {
            if (log.isInfoEnabled()) {
                log.info("Could not retrieve D2WContext from component context; it is probably not a D2W component.");
            }
        }

        if (d2wContext != null) {
            NSMutableDictionary d2wInfo = informationForD2WContext(d2wContext);
            if (component instanceof ERD2WPage) {
                ERD2WPage currentPage = (ERD2WPage)component;
                String subTask = (String)d2wContext.valueForKey("subTask");
                if ("tab".equals(subTask) || "wizard".equals("subTask")) {
                    NSArray sections = currentPage.sectionsForCurrentTab();
                    d2wInfo.setObjectForKey(sections != null ? sections : "null", "D2W-SectionsContentsForCurrentTab");
                    d2wInfo.removeObjectForKey("D2W-TabSectionsContents");
                }
            }
            info.addEntriesFromDictionary(d2wInfo);
        }

        return info;
    }

    /**
     * Gathers D2W-related information from the current context.  This is mainly useful for debugging.
     * @param d2wContext the D2W context from which to derive the debugging information
     * @return a dictionary of D2W-related keys to describe the state of the provided D2W context.
     */
    public static synchronized NSMutableDictionary informationForD2WContext(D2WContext d2wContext) {
        NSMutableDictionary info = new NSMutableDictionary();
        if (d2wContext != null) {
            String pageConfiguration = (String)d2wContext.valueForKeyPath("pageConfiguration");
            info.setObjectForKey(pageConfiguration != null ? pageConfiguration : "null", "D2W-PageConfiguration");

            String propertyKey = d2wContext.propertyKey();
            info.setObjectForKey(propertyKey != null ? propertyKey : "null", "D2W-PropertyKey");

            String entityName = (String)d2wContext.valueForKeyPath("entity.name");
            info.setObjectForKey(entityName != null ? entityName : "null", "D2W-EntityName");

            String task = (String)d2wContext.valueForKey("task");
            info.setObjectForKey(task != null ? task : "null", "D2W-SubTask");

            String subTask = (String)d2wContext.valueForKey("subTask");
            info.setObjectForKey(subTask != null ? subTask : "null", "D2W-SubTask");

            if ("tab".equals(subTask) || "wizard".equals("subTask")) {
                String tabKey = (String)d2wContext.valueForKey("tabKey");
                info.setObjectForKey(tabKey != null ? tabKey : "null", "D2W-TabKey");

                NSArray tabSections = (NSArray)d2wContext.valueForKey("tabSectionsContents");
                info.setObjectForKey(tabSections != null ? tabSections : "null", "D2W-TabSectionsContents");
            } else {
                NSArray displayPropertyKeys = (NSArray)d2wContext.valueForKey("displayPropertyKeys");
                info.setObjectForKey(displayPropertyKeys != null ? displayPropertyKeys : "null", "D2W-DisplayPropertyKeys");
            }

            String componentName = (String)d2wContext.valueForKey("componentName");
            info.setObjectForKey(componentName != null ? componentName : "null", "D2W-ComponentName");

            if (componentName != null && componentName.indexOf("CustomComponent") > 0) {
                String customComponentName = (String)d2wContext.valueForKey("customComponentName");
                info.setObjectForKey(customComponentName != null ? customComponentName : "null", "D2W-ComponentName");
            }

        }
        return info;
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
    
    // This is the actual method that turns trace rule firing on and off.
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
    	if(array == null) {
    		return null;
    	}
    	NSMutableArray result = new NSMutableArray();
    	for(Enumeration e = array.objectEnumerator(); e.hasMoreElements(); ){
    		String key = (String)e.nextElement();
    		result.addObject(new ERXKeyValuePair(key, ERDirectToWeb.displayNameForPropertyKey(key, entityForReportName)));
    	}
    	return result;
    }
}
