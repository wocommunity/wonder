/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.util.*;

import com.webobjects.directtoweb.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import er.extensions.*;

/**
 * Converts given entries of a D2WContext with a specified page configuration to a dictionary and to rules again.<br />
 * Very useful for debugging and testing. You can effectively dump a context for a given page configuration into a <code>.plist</code> file once you are content with your page, then make tons of changes to the rules and all the while test the changed value against all your stored dictionaries, which should make you more confident to make changes like <code>*true* => componentName = "D2WString" [100]</code><br />
 * Also, given a dictionary, you can re-create the rules for creating these entries with any given level.<br />
 * Reads in your <code>d2wClientConfiguration.plists</code> files from every bundle and also reads in the values  given in the <code>editors</code> and <code>supports</code> fields.<br />
 * So be sure to keep the entries to those files up to date :) <br />
<code><pre>
 NSArray pageKeys = new NSArray(new Object [] {"pageWrapperName", "pageName", "headComponentName", "displayPropertyKeys"});
 NSArray componentKeys = new NSArray(new Object [] {"componentName", "customComponentName"});
 ERD2WContextDictionary dict = new ERD2WContextDictionary("CreateModuleGroup", pageKeys, componentKeys);
 String value = NSPropertyListSerialization.stringFromPropertyList(dict.dictionary());
 </pre></code>
 * RENAMEME: to something more sensible??
 * @author ak
 */

public class ERD2WContextDictionary {
    private static final ERXLogger log = ERXLogger.getERXLogger(ERD2WContextDictionary.class);

    protected D2WContext context;
    protected String pageConfiguration;
    protected NSMutableArray pageLevelKeys;
    protected NSMutableArray componentLevelKeys;
    protected NSMutableDictionary dict;
    protected NSMutableDictionary allKeys;

    public ERD2WContextDictionary(String pageConfigurationName, NSArray pageKeys, NSArray componentKeys) {
        this.pageConfiguration = pageConfigurationName;

        context = new D2WContext();
        context.setDynamicPage(pageConfiguration);
        if(pageKeys == null) {
            pageLevelKeys = new NSMutableArray(new Object[] {"pageWrapperName","displayPropertyKeys"});
        } else {
            pageLevelKeys = pageKeys.mutableClone();
        }

        if(componentKeys == null) {
            componentLevelKeys = new NSMutableArray(new Object[] {"componentName", "customComponentName", "displayNameForProperty"});
        } else {
            componentLevelKeys = componentKeys.mutableClone();
        }

        if("edit".equals(context.task())) {
            componentLevelKeys.addObject("isMandatory");
        }
        if("list".equals(context.task())) {
            componentLevelKeys.addObject("propertyIsSortable");
            componentLevelKeys.addObject("sortKeyForList");
        }
        allKeys = new NSMutableDictionary();
        NSMutableDictionary components = new NSMutableDictionary();
        NSMutableDictionary editors = new NSMutableDictionary();
        allKeys.setObjectForKey(components, "components");
        allKeys.setObjectForKey(editors, "editors");
        for(Enumeration e = NSBundle.frameworkBundles().objectEnumerator(); e.hasMoreElements(); ) {
            NSBundle bundle = (NSBundle)e.nextElement();
            NSDictionary dict;
            dict = ERXDictionaryUtilities.dictionaryFromPropertyList("d2wClientConfiguration", bundle);
            if(dict != null) {
                if(dict.objectForKey("components") != null) {
                    components.addEntriesFromDictionary((NSDictionary)dict.objectForKey("components"));
                }
                if(dict.objectForKey("editors") != null) {
                    editors.addEntriesFromDictionary((NSDictionary)dict.objectForKey("editors"));
                }
            }
            if(dict != null) {
                dict = ERXDictionaryUtilities.dictionaryFromPropertyList("d2wclientConfiguration", bundle);
                if(dict.objectForKey("components") != null) {
                    components.addEntriesFromDictionary((NSDictionary)dict.objectForKey("components"));
                }
                if(dict.objectForKey("editors") != null) {
                    editors.addEntriesFromDictionary((NSDictionary)dict.objectForKey("editors"));
                }
            }
        }
    }
    
    public ERD2WContextDictionary(String pageConfiguration, NSDictionary dictionary) {
        this.pageConfiguration = pageConfiguration;
        this.dict = dictionary.mutableClone();
    }

    protected void addPageLevelValues() {
        for(Enumeration e = pageLevelKeys.objectEnumerator(); e.hasMoreElements(); ) {
            String key = (String)e.nextElement();
            Object o = context.valueForKey(key);
            if(o != null)
                dict.takeValueForKey(o, key);
        }
        String path = "components." + context.valueForKey("pageName") + ".editors";
        NSArray keys = (NSArray)allKeys.valueForKeyPath(path);
        if(keys != null) {
            for(Enumeration e = keys.objectEnumerator(); e.hasMoreElements(); ) {
                String key = (String)e.nextElement();
                Object o = context.valueForKey(key);
                if(o != null)
                    dict.takeValueForKeyPath(o, key);
            }
        }
        path = "components." + context.valueForKey("pageName") + ".supports";
        keys = (NSArray)allKeys.valueForKeyPath(path);
        if(keys != null) {
            for(Enumeration e = keys.objectEnumerator(); e.hasMoreElements(); ) {
                String key = (String)e.nextElement();
                Object o = context.valueForKey(key);
                if(o != null)
                    dict.takeValueForKeyPath(o, key);
            }
        }
    }

    protected void addComponentLevelValuesForKey(String propertyKey) {
        context.setPropertyKey(propertyKey);
        dict.takeValueForKeyPath( new NSMutableDictionary(), "componentLevelKeys." + propertyKey);
        for(Enumeration e = componentLevelKeys.objectEnumerator(); e.hasMoreElements(); ) {
            String key = (String)e.nextElement();
            Object o = context.valueForKey(key);
            if(o != null)
                dict.takeValueForKeyPath(o, "componentLevelKeys." + propertyKey + "." + key);
        }
        String path = "components." + dict.valueForKeyPath("componentLevelKeys." + propertyKey + ".componentName") + ".editors";
        NSArray keys = (NSArray)allKeys.valueForKeyPath(path);
        if(keys != null) {
            for(Enumeration e = keys.objectEnumerator(); e.hasMoreElements(); ) {
                String key = (String)e.nextElement();
                Object o = context.valueForKey(key);
                if(o != null)
                    dict.takeValueForKeyPath(o, "componentLevelKeys." + propertyKey + "." + key);
            }
        }
        path = "components." + dict.valueForKeyPath("componentLevelKeys." + propertyKey + ".componentName") + ".supports";
        keys = (NSArray)allKeys.valueForKeyPath(path);
        if(keys != null) {
            for(Enumeration e = keys.objectEnumerator(); e.hasMoreElements(); ) {
                String key = (String)e.nextElement();
                Object o = context.valueForKey(key);
                if(o != null)
                    dict.takeValueForKeyPath(o, key);
            }
        }
    }

    public NSDictionary dictionary() {
        if(dict == null) {
            dict = new NSMutableDictionary();
            addPageLevelValues();
            NSArray displayPropertyKeys = (NSArray)context.valueForKey("displayPropertyKeys");
            if(displayPropertyKeys != null && displayPropertyKeys.count() > 0) {
                dict.setObjectForKey( new NSMutableDictionary(), "componentLevelKeys");
                for(Enumeration e = displayPropertyKeys.objectEnumerator(); e.hasMoreElements(); ) {
                    String key = (String)e.nextElement();
                    addComponentLevelValuesForKey(key);
                }
            }
        }
        return dict;
    }

    public NSArray rulesForLevel(int level) {
        NSMutableArray arr = new NSMutableArray();
        for(Enumeration e = dictionary().keyEnumerator(); e.hasMoreElements(); ) {
            String key = (String)e.nextElement();
            if(!"componentLevelKeys".equals(key)) {
                Object value = dictionary().valueForKey(key);
                EOQualifier q = EOQualifier.qualifierWithQualifierFormat( "pageConfiguration = '" + pageConfiguration + "'" , null);
                Assignment a;
                if("true".equals(value) || "false".equals(value)) {
                    a = new BooleanAssignment(key, value);
                } else {
                    a = new Assignment(key, value);
                }
                arr.addObject(new Rule(level, q, a));
            }
        }
        NSArray keys = (NSArray)dict.valueForKey("displayPropertyKeys");
        if(keys != null && keys.count() > 0) {
            for(Enumeration e = keys.objectEnumerator(); e.hasMoreElements(); ) {
                String key = (String)e.nextElement();
                Object value = dictionary().valueForKeyPath("componentLevelKeys." + key);
                EOQualifier q = EOQualifier.qualifierWithQualifierFormat( "pageConfiguration = '" + pageConfiguration + "' and propertyKey = '" + key + "'" , null);
                Assignment a;
                if("true".equals(value) || "false".equals(value)) {
                    a = new BooleanAssignment(key, value);
                } else {
                    a = new Assignment(key, value);
                }
                arr.addObject(new Rule(level, q, a));
            }
        }
        return arr;
    }
    public D2WContext context() {
        return context;
    }
}