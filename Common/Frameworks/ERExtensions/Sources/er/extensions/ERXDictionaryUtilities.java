//
// NSDictionaryUtilities.java
// Project vwdBussinessLogicJava
//
// Created by ak on Wed Jun 06 2001
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;
import java.lang.*;

public class ERXDictionaryUtilities extends Object {
    public static NSDictionary dictionaryWithDictionaryAndDictionary(NSDictionary dict1, NSDictionary dict2) {
        if(dict1 == null || dict1.allKeys().count() == 0) 
            return dict2;
        if(dict2 == null || dict2.allKeys().count() == 0) 
            return dict1;
            
        NSMutableDictionary result = new NSMutableDictionary(dict2);
        result.addEntriesFromDictionary(dict1);
        return new NSDictionary(result);
    }

    public static NSDictionary dictionaryFromPropertyList(String name, NSBundle bundle) {
        return (NSDictionary)NSPropertyListSerialization.propertyListFromString(ERXStringUtilities.stringFromResource(name, "plist", bundle));
    }
    
    public static NSDictionary dictionaryWithObjectsAndKeys(Object[] objectsAndKeys) {
        NSMutableDictionary result = new NSMutableDictionary();
        Object object;
        String key;
        int length = objectsAndKeys.length;
        for(int i = 0; i < length; i+=2) {
            object = objectsAndKeys[i];
            if(object == null) {
                break;
            }
            key = (String)objectsAndKeys[i+1];
            result.setObjectForKey(object,key);
        }
        return new NSDictionary(result);
    }
}
