/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import com.webobjects.directtoweb.ERD2WUtilities;
import com.webobjects.appserver.*;
import er.extensions.*;
import org.apache.log4j.Category;
import java.util.*;

// Primary Package Class
public class ERDirectToWeb {

    //////////////////////////////////////////////  log4j category  ////////////////////////////////////////////
    public final static Category cat = Category.getInstance("er.directtoweb.ERDirectToWeb");
    public final static String D2WDEBUGGING_ENABLED_KEY = "ERDirectToWeb_d2wDebuggingEnabled";
    public final static String D2WDISPLAY_COMPONENTNAMES_KEY = "ERDirectToWeb_displayComponentNames";
    public final static String D2WDISPLAY_PROPERTYKEYS_KEY = "ERDirectToWeb_displayPropertyKeys";
    public final static Category debugCat = Category.getInstance("er.directtoweb.ERD2WDebugEnabled");
    public final static Category componentNameCat = Category.getInstance("er.directtoweb.ERD2WDebugEnabled.componentName");
    public final static Category propertyKeyCat = Category.getInstance("er.directtoweb.ERD2WDebugEnabled.propertyKey");
    // Notification Observer
    public static class Observer {
        public void didFinishedLaunchingApp(NSNotification n) {
            ERDirectToWeb.warmUpRuleCache();
            NSNotificationCenter.defaultCenter().addObserver(this,
                                                             new NSSelector("resetModel",
                                                                            ERXConstant.NotificationClassArray),
                                                             ERXCompilerProxy.CompilerProxyDidCompileClassesNotification,
                                                             null);
            NSNotificationCenter.defaultCenter().addObserver(this,
                                                             new NSSelector("resetModel",
                                                                            ERXConstant.NotificationClassArray),
                                                             ERXLocalizer.LocalizationDidResetNotification,
                                                             null);
        }
        public void resetModel(NSNotification n) {
            ERD2WModel.erDefaultModel().resetModel();
        }
    }
    
    private static boolean _isInitialized=false;
    static {
        // called implicitely because ERDirectToWeb is the principal class of the framework
        if (!_isInitialized) {
            if (cat.isDebugEnabled()) cat.debug("Initializing framework: ERDirectToWeb");
            Class c=ERD2WModel.class;        // force initialization
                                             // Configures the system for trace rule firing.
            D2W.setFactory(new ERD2WFactory());
            try {
                ERDirectToWeb.configureTraceRuleFiringRapidTurnAround();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            Observer observer=new Observer();
            ERXRetainer.retain(observer); // has to be retained on the objC side!!
            NSNotificationCenter.defaultCenter().addObserver(observer,
                                                             new NSSelector("didFinishedLaunchingApp",
                                                                            ERXConstant.NotificationClassArray),
                                                             WOApplication.ApplicationDidFinishLaunchingNotification,
                                                             null);
            _isInitialized = true;
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
                                                                  debugCat.isDebugEnabled());
    }

    public static void setD2wComponentNameDebuggingEnabled(WOSession s, boolean enabled) {
        ERXExtensions.setBooleanFlagOnSessionForKey(s, D2WDISPLAY_COMPONENTNAMES_KEY, enabled);
    }
    public static boolean d2wComponentNameDebuggingEnabled(WOSession s) {
        return ERXExtensions.booleanFlagOnSessionForKeyWithDefault(s,
                                                                  D2WDISPLAY_COMPONENTNAMES_KEY,
                                                                  componentNameCat.isDebugEnabled());
    }

    public static void setD2wPropertyKeyDebuggingEnabled(WOSession s, boolean enabled) {
        ERXExtensions.setBooleanFlagOnSessionForKey(s, D2WDISPLAY_PROPERTYKEYS_KEY, enabled);
    }
    public static boolean d2wPropertyKeyDebuggingEnabled(WOSession s) {
        return ERXExtensions.booleanFlagOnSessionForKeyWithDefault(s,
                                                                  D2WDISPLAY_PROPERTYKEYS_KEY,
                                                                  propertyKeyCat.isDebugEnabled());
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

    // This defaults to true.
    public static boolean booleanForKey(D2WContext context, String key) {
        Integer i=(Integer)context.valueForKey(key);
        return i==null || i.intValue()!=0;
    }

    public static String userInfoUnit(EOEnterpriseObject object, String key) {
        // return the unit stored in the userInfo dictionary of the appropriate EOAttribute
        EOEntity entity=null;
        String lastKey=null;
        String result=null;
        if (object == null || key == null) {
            cat.warn("UserInfoUnit: Attempting to relsolve a unit for object: " + object + " key: " + key);
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

    public static WOComponent printerFriendlyPageForD2WContext(D2WContext context, WOSession session) {
        D2WContext newContext=new D2WContext(session);
        String newTask=context.task().equals("edit") ? "inspect" : context.task();
        newContext.takeValueForKey(newTask,"task");
        // not using subTask directly here because the cache mechanism relies on being able to compute wether this key
        // is 'computable' (subTask is since a rule can fire to give a default) or an external output
//        newContext.takeValueForKey("printerFriendly","subTask");
        newContext.takeValueForKey("printerFriendly","forcedSubTask");
        newContext.takeValueForKey(context.valueForKey("pageName"),"existingPageName");
        newContext.takeValueForKey(context.valueForKey("subTask"),"existingSubTask");
        newContext.takeValueForKey(context.valueForKey("pageConfiguration"),"pageConfiguration");
        newContext.takeValueForKey(context.entity(),"entity");
        WOComponent result=WOApplication.application().pageWithName((String)newContext.valueForKey("pageName"),session.context());
        ((D2WPage)result).setLocalContext(newContext);
        return result;
    }

    public static WOComponent csvExportPageForD2WContext(D2WContext context, WOSession session) {
        D2WContext newContext=new D2WContext(session);
        newContext.takeValueForKey(context.task(),"task");
        // not using subTask directly here because the cache mechanism relies on being able to compute wether this key
        // is 'computable' (subTask is since a rule can fire to give a default) or an external output
        newContext.takeValueForKey("csv","forcedSubTask");
        newContext.takeValueForKey(context.valueForKey("pageName"),"existingPageName");
        newContext.takeValueForKey(context.valueForKey("subTask"),"existingSubTask");
        newContext.takeValueForKey(context.valueForKey("pageConfiguration"),"pageConfiguration");
        newContext.takeValueForKey(context.entity(),"entity");
        WOComponent result=WOApplication.application().pageWithName((String)newContext.valueForKey("pageName"),session.context());
        ((D2WPage)result).setLocalContext(newContext);
        return result;
    }

    
    public static WOComponent pageForTaskSubTaskAndEntityNamed(String task, String subtask, String entityName, WOSession session) {
        D2WContext newContext=new D2WContext(session);
        newContext.setTask(task);
        newContext.setEntity(EOModelGroup.defaultGroup().entityNamed(entityName));
        newContext.takeValueForKey(subtask, "subTask");
        WOComponent result=WOApplication.application().pageWithName((String)newContext.valueForKey("pageName"),session.context());
        ((D2WPage)result).setLocalContext(newContext);
        return result;        
    }

    public static QueryPageInterface queryPageWithFetchSpecificationForEntityNamed(String fsName, String entityName, WOSession s) {
        WOComponent result= pageForTaskSubTaskAndEntityNamed("query", "fetchSpecification", entityName,s);
        result.takeValueForKey(fsName, "fetchSpecificationName");
        return (QueryPageInterface)result;
    }
    
    public static String displayNameForPropertyKey(String key, String entityName, String language) {
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        ERD2WUtilities.resetContextCache(d2wContext());
        d2wContext().setEntity(entity);
        d2wContext().setPropertyKey(key);
        return d2wContext().displayNameForProperty();
    }

    // Needs to be a late init because then it will hook itself up to the correct D2WModel
    private static D2WContext _context;
    public static D2WContext d2wContext() {
        if (_context == null)
            _context = new D2WContext();
        return _context;
    }

    public static Object d2wContextValueForKey(String key, String entityName) {
        return d2wContextValueForKey(key, entityName, null);
    }

    public static Object d2wContextValueForKey(String key, String entityName, NSDictionary extraValuesForContext) {
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        ERD2WUtilities.resetContextCache(d2wContext());
        d2wContext().setEntity(entity);
        if (extraValuesForContext!=null) {
            for (Enumeration e=extraValuesForContext.allKeys().objectEnumerator(); e.hasMoreElements();) {
                String k=(String)e.nextElement();
                d2wContext().takeValueForKey(extraValuesForContext.objectForKey(k),k);
            }
        }
        return d2wContext().valueForKey(key);
    }

    
    public static String createConfigurationForEntityNamed(String entityName) {
        return (String)d2wContextValueForKey("createConfigurationNameForEntity", entityName);
    }

    public static void warmUpRuleCache() {
        cat.debug("Preparing DirectToWeb Data Structures");
        ERD2WModel.erDefaultModel().prepareDataStructures();
    }

    public static Category trace;

    // This enables us to turn trace rule firing on or off at will.


    public static class _Observer {
        public void configureTraceRuleFiring(NSNotification n) {
            ERDirectToWeb.configureTraceRuleFiring();
        }
    }

    private static boolean _initializedTraceRuleFiring = true;
    public static void configureTraceRuleFiringRapidTurnAround() {
        if (!_initializedTraceRuleFiring) {
            // otherwise not properly initialized
            trace = Category.getInstance("er.directtoweb.rules.D2WTraceRuleFiringEnabled");
            // Note: If the configuration file says debug, but the command line parameter doesn't we need to turn
            //   rule tracing on.
            // BOOGIE
            if (trace.isDebugEnabled() && !NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupRules)) {
                NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupRules);
                NSLog.setAllowedDebugLevel(NSLog.DebugLevelDetailed);
                trace.info("Rule tracing on");
            }
            Object observer=new _Observer();
            ERXRetainer.retain(observer); // has to be retained on the objC side!!
            NSNotificationCenter.defaultCenter().addObserver(observer,
                                                             new NSSelector("configureTraceRuleFiring",
                                                                            ERXConstant.NotificationClassArray),
                                                             ERXLog4j.ConfigurationDidChangeNotification,
                                                             null);
            _initializedTraceRuleFiring = true;
        }
    }
    
    // This is the actual method that turns trace rule firign on and off.
    public static void configureTraceRuleFiring() {
        if (trace.isDebugEnabled() && !NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupRules)) {
            NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupRules);
            NSLog.setAllowedDebugLevel(NSLog.DebugLevelDetailed);
            trace.info("Rule tracing on");
        } else if (!trace.isDebugEnabled() && NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupRules)) {
            NSLog.refuseDebugLoggingForGroups(NSLog.DebugGroupRules);
            trace.info("Rule tracing off");
        }
    }

    public static NSArray displayableArrayForKeyPathArray(NSArray array, String entityForReportName, String language){
        NSMutableArray result = new NSMutableArray();
        for(Enumeration e = array.objectEnumerator(); e.hasMoreElements(); ){
            String key = (String)e.nextElement();
            result.addObject(new ERXKeyValuePair(key, ERDirectToWeb.displayNameForPropertyKey(key, entityForReportName, language)));
        }
        return (NSArray)result;
    }
}
