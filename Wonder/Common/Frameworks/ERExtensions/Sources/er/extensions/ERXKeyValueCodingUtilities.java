/*
 * Created on 08.03.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package er.extensions;

import java.lang.reflect.*;

import com.webobjects.foundation.*;

/**
 * Utilities for use with key value coding.
 * @author ak
 */
public class ERXKeyValueCodingUtilities {
	
    /**
     * Extends key-value coding to a class. You can currently only
     * get the static fields of the class, not method results.
     * @param clazz
     * @param key
     * @return
     */
    public static Object classValueForKey(Class clazz, String key) {
    	Object result = null;
    	if(key != null) {
    		try {
    			Field f = clazz.getDeclaredField(key);
    			result = f.get(clazz);
    		} catch (Exception e) {
    			throw new NSForwardException(e);
    		}
    	}
    	return result;
    }
}
