//
//  ERXPathDirectAction.java
//  ERExtensions
//
//  Created by Max Muller III on Fri Sep 19 2003.
//  Copyright (c) 2003 Apple. All rights reserved.
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import java.util.*;

public class ERXPathDirectAction extends WODirectAction {

    protected static final ERXLogger log = ERXLogger.getERXLogger(ERXPathDirectAction.class);

    protected NSArray pathParts;
    protected NSDictionary pathPartsByKeys;
    protected NSDictionary pathPartsByKeysCaseInsensitive;
    
    public ERXPathDirectAction(WORequest aRequest) {
        super(aRequest);
    }

    public NSArray pathParts() {
        if (pathParts == null) {
            if (request().requestHandlerPathArray().count() > 2) {
                pathParts = request().requestHandlerPathArray().subarrayWithRange(new NSRange(1, request().requestHandlerPathArray().count() - 2));
            }
            if (pathParts == null)
                pathParts = NSArray.EmptyArray;
            if (log.isDebugEnabled())
                log.debug("Generated path parts: " + pathParts + " for uri: " + request().uri());
        }
        return pathParts;
    }

    public NSDictionary pathPartsByKeys() {
        if (pathPartsByKeys == null) {
            NSMutableDictionary temp = null;
            NSMutableDictionary tempCaseInsensitive = null;
            for (Enumeration pathEnumerator = pathParts().objectEnumerator();
                 pathEnumerator.hasMoreElements();) {
                String path = (String)pathEnumerator.nextElement();
                if (path.indexOf('=') != -1) {
                    if (temp == null) {
                        temp = new NSMutableDictionary();
                        tempCaseInsensitive = new NSMutableDictionary();
                    }
                    NSArray parts = NSArray.componentsSeparatedByString(path, "=");
                    if (parts.count() == 2) {
                        temp.setObjectForKey(parts.objectAtIndex(1),
                                             parts.objectAtIndex(0));
                        tempCaseInsensitive.setObjectForKey(parts.objectAtIndex(1),
                                             ((String)parts.objectAtIndex(0)).toLowerCase());                        
                    }
                }
            }
            pathPartsByKeys = temp != null ? temp : NSDictionary.EmptyDictionary;
            pathPartsByKeysCaseInsensitive = tempCaseInsensitive != null ? tempCaseInsensitive : NSDictionary.EmptyDictionary;
        }
        return pathPartsByKeys;
    }

    public NSDictionary pathPartsByKeysCaseInsensitive() {
        if (pathPartsByKeysCaseInsensitive == null)
            pathPartsByKeys();
        return pathPartsByKeysCaseInsensitive;
    }

    public boolean hasPathPartForKey(String key) {
        return hasPathPartForKey(key, false);
    }

    public boolean hasPathPartForKey(String key, boolean caseInsensitive) {
        if (caseInsensitive) {
            key = key.toLowerCase();
        }
        return caseInsensitive ? pathPartsByKeysCaseInsensitive().objectForKey(key) != null : pathPartsByKeys().objectForKey(key) != null;
    }
    
    public String pathPartForKey(String key) {
        return pathPartForKeyWithDefault(key, null);
    }

    public String pathPartForKeyWithDefault(String key, String defaultValue) {
        return pathPartForKeyWithDefault(key, defaultValue, false);
    }

    public String pathPartForKeyWithDefault(String key,
                                            String defaultValue,
                                            boolean caseInsensitiveCompare) {
        String value = null;
        if (caseInsensitiveCompare) {
            key = key.toLowerCase();
            value = (String)pathPartsByKeysCaseInsensitive().objectForKey(key);
        } else {
            value = (String)pathPartsByKeys().objectForKey(key);            
        }
        return value != null ? value : defaultValue;
    }
        

}
