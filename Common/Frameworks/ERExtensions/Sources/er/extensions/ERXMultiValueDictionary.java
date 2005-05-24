package er.extensions;

import com.webobjects.foundation.*;

import er.extensions.*;


/**
 * @author david teran at cluster9.com
 *
 * Supports adding multiple values for one key which are automatically
 * stored in a ERXMutableArray
 *
 */
public class ERXMultiValueDictionary extends ERXMutableDictionary {

    /* (non-Javadoc)
     * @see com.webobjects.foundation.NSMutableDictionary#NSMutableDictionary(com.webobjects.foundation.NSMutableDictionary)
     */
    public ERXMultiValueDictionary(NSDictionary d) {
        super(d);
    }

    /* (non-Javadoc)
     * @see com.webobjects.foundation.NSMutableDictionary#NSMutableDictionary()
     */
    public ERXMultiValueDictionary() {
        super();
    }

	/**
	* overriden to check if there is already a value for the specified key
	* if yes, then the object is added to the array of values for the key
	* if not, then the object is added to a new array and then the array
	* is added to the dictionary.
	*
	* @param object the object to store in the dictionary
	* @param key the key to use for the object
	*
	*/
    public void setObjectForKey(Object object, Object key) {
        Object existingValue = objectForKey(key);
        if (existingValue != null) {
            ERXMutableArray existingValueA;
            if (existingValue instanceof ERXMutableArray) {
                existingValueA = (ERXMutableArray)existingValue;
            } else {
                existingValueA = new ERXMutableArray();
                existingValueA.addObject(existingValue);
                super.setObjectForKey(existingValueA, key);
            }
            existingValueA.addObject(object);
        	} else {
        	    super.setObjectForKey(object, key);
        	}
    }
    
    /* (non-Javadoc)
     * @see com.webobjects.foundation.NSMutableDictionary#takeValueForKey(java.lang.Object, java.lang.String)
     */
    public void takeValueForKey(Object value, String key) {
        setObjectForKey(value, key);
    }

	/**
	* returns true if the value for key is an instanceof ERXMutableArray
	*
	*/
    public boolean isMultiValue(String key) {
        return objectForKey(key) instanceof ERXMutableArray;
    }
}
