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
 * Very useful for debugging and testing. You can effectively dump a context for a given page configuration 
 * into a <code>.plist</code> file once you are content with your page, then make tons of changes to the rules 
 * and all the while test the changed value against all your stored dictionaries, which should make you 
 * more confident to make changes like <code>*true* => componentName = "D2WString" [100]</code><br />
 * Also, given a dictionary, you can re-create the rules for creating these entries with any given level.<br />
 * Reads in your <code>d2wClientConfiguration.plists</code> files from every bundle and also reads in the values  
 * given in the <code>editors</code> and <code>supports</code> fields.<br />
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

    protected D2WContext _context;
    protected String _pageConfiguration;
    protected NSMutableArray _pageLevelKeys;
    protected NSMutableArray _componentLevelKeys;
    protected NSMutableDictionary _dictionary;
    protected NSMutableDictionary _allKeys;

    public ERD2WContextDictionary(String pageConfigurationName, NSArray pageKeys, NSArray componentKeys) {
        _pageConfiguration = pageConfigurationName;

        _context = new D2WContext();
        _context.setDynamicPage(_pageConfiguration);
        if(pageKeys == null) {
            _pageLevelKeys = new NSMutableArray(new Object[] {"pageWrapperName", "displayPropertyKeys"});
        } else {
            _pageLevelKeys = pageKeys.mutableClone();
        }

        if(componentKeys == null) {
            _componentLevelKeys = new NSMutableArray(new Object[] {"componentName", "customComponentName", 
                    "displayNameForProperty", "propertyKey"});
        } else {
            _componentLevelKeys = componentKeys.mutableClone();
        }

        if("edit".equals(_context.task())) {
            _componentLevelKeys.addObject("isMandatory");
        }
        if("list".equals(_context.task())) {
            _componentLevelKeys.addObject("propertyIsSortable");
            _componentLevelKeys.addObject("sortKeyForList");
        }
        _allKeys = new NSMutableDictionary();
        
        NSMutableDictionary components = new NSMutableDictionary();
        NSMutableDictionary editors = new NSMutableDictionary();
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
        }
        _allKeys.setObjectForKey(components, "components");
        _allKeys.setObjectForKey(editors, "editors");
    }
    
    public ERD2WContextDictionary(String pageConfiguration, NSDictionary dictionary) {
        _pageConfiguration = pageConfiguration;
        _dictionary = dictionary.mutableClone();
    }

    protected void addPageLevelValues() {
        for(Enumeration e = _pageLevelKeys.objectEnumerator(); e.hasMoreElements(); ) {
            String key = (String)e.nextElement();
            Object o = _context.valueForKey(key);
            if(o != null)
                _dictionary.takeValueForKey(o, key);
        }
        String path = "components." + _context.valueForKey("pageName") + ".editors";
        NSArray keys = (NSArray)_allKeys.valueForKeyPath(path);
        if(keys != null) {
            for(Enumeration e = keys.objectEnumerator(); e.hasMoreElements(); ) {
                String key = (String)e.nextElement();
                Object o = _context.valueForKey(key);
                if(o != null)
                    _dictionary.takeValueForKeyPath(o, key);
            }
        }
        path = "components." + _context.valueForKey("pageName") + ".supports";
        keys = (NSArray)_allKeys.valueForKeyPath(path);
        if(keys != null) {
            for(Enumeration e = keys.objectEnumerator(); e.hasMoreElements(); ) {
                String key = (String)e.nextElement();
                Object o = _context.valueForKey(key);
                if(o != null)
                    _dictionary.takeValueForKeyPath(o, key);
            }
        }
    }

    /**
     * Returns the keys for the given property key. To find which keys are
     * requiered, the componentName key is used to get the editors and supports.
     * @param propertyKey
     */
    protected NSDictionary componentLevelValuesForKey(String propertyKey) {
        _context.setPropertyKey(propertyKey);
        NSMutableDictionary dictionary = new NSMutableDictionary();
        for(Enumeration e = _componentLevelKeys.objectEnumerator(); e.hasMoreElements(); ) {
            String key = (String)e.nextElement();
            Object o = _context.valueForKey(key);
            if(o != null)
                dictionary.setObjectForKey(o, key);
        }
        String path = "components." + dictionary.objectForKey("componentName") + ".editors";
        NSArray keys = (NSArray)_allKeys.valueForKeyPath(path);
        if(keys != null) {
            for(Enumeration e = keys.objectEnumerator(); e.hasMoreElements(); ) {
                String key = (String)e.nextElement();
                Object o = _context.valueForKey(key);
                if(o != null)
                    dictionary.setObjectForKey(o, key);
            }
        }
        path = "components." + dictionary.objectForKey("componentName") + ".supports";
        keys = (NSArray)_allKeys.valueForKeyPath(path);
        if(keys != null) {
            for(Enumeration e = keys.objectEnumerator(); e.hasMoreElements(); ) {
                String key = (String)e.nextElement();
                Object o = _context.valueForKey(key);
                if(o != null)
                    dictionary.setObjectForKey(o, key);
            }
        }
        return dictionary;
    }

    public NSDictionary dictionary() {
        if(_dictionary == null) {
            _dictionary = new NSMutableDictionary();
            addPageLevelValues();
            NSArray displayPropertyKeys = (NSArray)_context.valueForKey("displayPropertyKeys");
            if(displayPropertyKeys != null && displayPropertyKeys.count() > 0) {
                NSMutableDictionary componentLevelKeys = new NSMutableDictionary();
                for(Enumeration e = displayPropertyKeys.objectEnumerator(); e.hasMoreElements(); ) {
                    String key = (String)e.nextElement();
                    componentLevelKeys.setObjectForKey(componentLevelValuesForKey(key), key);
                }
                _dictionary.setObjectForKey( componentLevelKeys, "componentLevelKeys");
            }
        }
        return _dictionary;
    }

    public NSArray rulesForLevel(int level) {
        NSMutableArray arr = new NSMutableArray();
        for(Enumeration e = dictionary().keyEnumerator(); e.hasMoreElements(); ) {
            String key = (String)e.nextElement();
            if(!"componentLevelKeys".equals(key)) {
                Object value = dictionary().valueForKey(key);
                EOQualifier q = EOQualifier.qualifierWithQualifierFormat( "pageConfiguration = '" + _pageConfiguration + "'" , null);
                Assignment a;
                if("true".equals(value) || "false".equals(value)) {
                    a = new BooleanAssignment(key, value);
                } else {
                    a = new Assignment(key, value);
                }
                arr.addObject(new Rule(level, q, a));
            }
        }
        NSArray keys = (NSArray)_dictionary.valueForKey("displayPropertyKeys");
        if(keys != null && keys.count() > 0) {
            for(Enumeration e = keys.objectEnumerator(); e.hasMoreElements(); ) {
                String key = (String)e.nextElement();
                Object value = dictionary().valueForKeyPath("componentLevelKeys." + key);
                EOQualifier q = EOQualifier.qualifierWithQualifierFormat( "pageConfiguration = '" + _pageConfiguration + "' and propertyKey = '" + key + "'" , null);
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
        return _context;
    }
}