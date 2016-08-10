/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.directtoweb.Assignment;
import com.webobjects.directtoweb.BooleanAssignment;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.directtoweb.Rule;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.foundation.ERXDictionaryUtilities;

/**
 * Converts given entries of a D2WContext with a specified page configuration to a dictionary and to rules again.<br>
 * Very useful for debugging and testing. You can effectively dump a context for a given page configuration 
 * into a <code>.plist</code> file once you are content with your page, then make tons of changes to the rules 
 * and all the while test the changed value against all your stored dictionaries, which should make you 
 * more confident to make changes like <code>*true* =&gt; componentName = "D2WString" [100]</code><br>
 * Also, given a dictionary, you can re-create the rules for creating these entries with any given level.<br>
 * Reads in your <code>d2wclientConfiguration.plists</code> files from every bundle and also reads in the values  
 * given in the <code>editors</code> and <code>supports</code> fields.<br>
 * So be sure to keep the entries to those files up to date :)
<pre><code>
 NSArray pageKeys = new NSArray(new Object [] {"pageWrapperName", "pageName", "headComponentName", "displayPropertyKeys"});
 NSArray componentKeys = new NSArray(new Object [] {"componentName", "customComponentName"});
 ERD2WContextDictionary dict = new ERD2WContextDictionary("CreateModuleGroup", pageKeys, componentKeys);
 String value = NSPropertyListSerialization.stringFromPropertyList(dict.dictionary());
 </code></pre>
 * RENAMEME: to something more sensible??
 * @author ak
 */

public class ERD2WContextDictionary {
    private static final Logger log = Logger.getLogger(ERD2WContextDictionary.class);

    public static class Configuration {
   		
    	private NSMutableDictionary components = new NSMutableDictionary();
   		
    	private NSMutableDictionary editors = new NSMutableDictionary();

    	public Configuration() {
    		NSMutableArray bundles = NSBundle.frameworkBundles().mutableClone();
    		bundles.addObject(NSBundle.mainBundle());
    		for(Enumeration e = bundles.objectEnumerator(); e.hasMoreElements(); ) {
    			NSBundle bundle = (NSBundle)e.nextElement();
    			NSDictionary dict;
    			String path = bundle.resourcePathForLocalizedResourceNamed("d2wclientConfiguration.plist", null);
    			if(path != null) {
    				dict = ERXDictionaryUtilities.dictionaryFromPropertyList("d2wclientConfiguration", bundle);
    				if(dict != null) {
    					if(dict.objectForKey("components") != null) {
    						components.addEntriesFromDictionary((NSDictionary)dict.objectForKey("components"));
    					}
    					if(dict.objectForKey("editors") != null) {
    						editors.addEntriesFromDictionary((NSDictionary)dict.objectForKey("editors"));
    					}
    				}
    			}
    		}
    	}
    	
    	public NSMutableDictionary components() {
    		return components;
    	}
    	
    	public NSMutableDictionary editors() {
    		return editors;
    	}
    }
    
    protected D2WContext _context;
    protected String _pageConfiguration;
    protected NSMutableArray _pageLevelKeys;
    protected NSMutableArray _componentLevelKeys;
    protected NSMutableDictionary _dictionary;
    protected NSMutableDictionary _allKeys;

    public ERD2WContextDictionary(String pageConfigurationName, NSArray pageKeys, NSArray componentKeys) {
        _pageConfiguration = pageConfigurationName;

        _context = ERD2WContext.newContext();
        _context.setDynamicPage(_pageConfiguration);
        _context.setTask(_context.task());
        _context.setEntity(_context.entity());
        if(pageKeys == null) {
            _pageLevelKeys = new NSMutableArray(new Object[] {"pageWrapperName", "displayPropertyKeys", "pageName"});
        } else {
            _pageLevelKeys = pageKeys.mutableClone();
        }
        NSArray keys = new NSMutableArray(new Object[] {"componentName", "customComponentName", 
                "displayNameForProperty", "propertyKey"});
        _componentLevelKeys = new NSMutableArray();
        if(componentKeys != null) {
            _componentLevelKeys = componentKeys.mutableClone();
        }
        for(Enumeration e = keys.objectEnumerator(); e.hasMoreElements(); ) {
        	String key = (String)e.nextElement();
        	if(!_componentLevelKeys.containsObject(key)) {
        		_componentLevelKeys.addObject(key);
        	}
        }

        if("edit".equals(_context.task())) {
            _componentLevelKeys.addObject("isMandatory");
        }
        if("list".equals(_context.task())) {
            _componentLevelKeys.addObject("propertyIsSortable");
            _componentLevelKeys.addObject("sortKeyForList");
        }
        _allKeys = new NSMutableDictionary();
        Configuration config = new Configuration();
        _allKeys.setObjectForKey(config.components(), "components");
        _allKeys.setObjectForKey(config.editors(), "editors");
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
        _context.setPropertyKey(null);
        return dictionary;
    }

    public NSDictionary dictionary() {
    	if(_dictionary == null) {
    		_dictionary = new NSMutableDictionary();
    		addPageLevelValues();
    		NSArray displayPropertyKeys = (NSArray)_context.valueForKey("displayPropertyKeys");
    		if(displayPropertyKeys != null && displayPropertyKeys.count() > 0) {
    			NSMutableDictionary componentLevelKeys = new NSMutableDictionary();
    			addPropertyKeys(componentLevelKeys, displayPropertyKeys);
    			_dictionary.setObjectForKey( componentLevelKeys, "componentLevelKeys");
    		}
    	}
    	return _dictionary;
    }

    protected void addPropertyKeys(NSMutableDictionary componentLevelKeys, NSArray array) {
        for(Enumeration e = array.objectEnumerator(); e.hasMoreElements(); ) {
            Object o = e.nextElement();
            if(o instanceof NSArray) {
                addPropertyKeys(componentLevelKeys, (NSArray)o);
            } else {
                String key = (String)o;
                componentLevelKeys.setObjectForKey(componentLevelValuesForKey(key), key);
            }
        }
    }

    public NSArray rulesForLevel(int level) {
        NSMutableArray arr = new NSMutableArray();
        for(Enumeration e = dictionary().keyEnumerator(); e.hasMoreElements(); ) {
            String key = (String)e.nextElement();
            if(!"componentLevelKeys".equals(key)) {
                Object value = dictionary().valueForKey(key);
                EOQualifier q = EOQualifier.qualifierWithQualifierFormat( "pageConfiguration = '" + _pageConfiguration + "'" , null);
                Assignment a = createAssigment(key, value);
                arr.addObject(new Rule(level, q, a));
            }
        }
        NSArray keys = (NSArray)_dictionary.valueForKey("displayPropertyKeys");
        if(keys != null && keys.count() > 0) {
            addRulesForPropertyKeys(level, arr, keys);
        }
        return arr;
    }

	private Assignment createAssigment(String key, Object value) {
		Assignment a;
		if("true".equals(value) || "false".equals(value)) {
		    a = new BooleanAssignment(key, value);
		} else {
		    a = new Assignment(key, value);
		}
		return a;
	}
    
    protected void addRulesForPropertyKeys(int level, NSMutableArray rules, NSArray keys) {
        for(Enumeration e = keys.objectEnumerator(); e.hasMoreElements(); ) {
            Object o = e.nextElement();
            if(o instanceof NSArray) {
                addRulesForPropertyKeys(level, rules, keys);
            } else {
                String propertyKey = (String)o;
                NSDictionary values = (NSDictionary)dictionary().valueForKeyPath("componentLevelKeys." + propertyKey);
                EOQualifier q = EOQualifier.qualifierWithQualifierFormat( "pageConfiguration = '" + _pageConfiguration + "' and propertyKey = '" + propertyKey + "'" , null);
                for (Enumeration e1 = values.keyEnumerator(); e1.hasMoreElements();) {
                    String key = (String)e1.nextElement();
                    Object value = values.objectForKey(key);
                    Assignment a = createAssigment(key, value);
                    rules.addObject(new Rule(level, q, a));
                }
            }
        }
    }

    public D2WContext context() {
        return _context;
    }
    
    public String dictionaryString() {
    	return NSPropertyListSerialization.stringFromPropertyList(dictionary());
    }
    
    @Override
    public String toString() {
    	return context() + ": " + dictionaryString();
    }
}