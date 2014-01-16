//
// NSDictionaryUtilities.java
// Project vwdBussinessLogicJava
//
// Created by ak on Wed Jun 06 2001
//
package er.extensions.foundation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;

import com.webobjects.appserver.WOMessage;
import com.webobjects.eocontrol.EOKeyValueCoding;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;


/**
 * Collection of {@link com.webobjects.foundation.NSDictionary NSDictionary} utilities.
 */
public class ERXDictionaryUtilities {

    /**
     * Creates an immutable dictionary containing all of the keys and
     * objects from two dictionaries.
     * @param dict1 the first dictionary
     * @param dict2 the second dictionary
     * @return immutbale dictionary containing the union of the two dictionaries.
     */
    public static <K, V> NSDictionary<K, V> dictionaryWithDictionaryAndDictionary(NSDictionary<? extends K, ? extends V> dict1, NSDictionary<? extends K, ? extends V> dict2) {
        if(dict1 == null || dict1.allKeys().count() == 0)
            return (NSDictionary<K, V>) dict2;
        if(dict2 == null || dict2.allKeys().count() == 0)
            return (NSDictionary<K, V>) dict1;

        NSMutableDictionary<K, V> result = new NSMutableDictionary<K, V>(dict2);
        result.addEntriesFromDictionary(dict1);
        return new NSDictionary<K, V>(result);
    }

    /**
     * Creates an NSDictionary from a resource associated with a given bundle
     * that is in property list format.<br/>
     * @param name name of the file or resource.
     * @param bundle NSBundle to which the resource belongs.
     * @return NSDictionary de-serialized from the property list.
     */
    @SuppressWarnings("unchecked")
	public static NSDictionary dictionaryFromPropertyList(String name, NSBundle bundle) {
        String string = ERXStringUtilities.stringFromResource(name, "plist", bundle);
        return (NSDictionary<?,?>)NSPropertyListSerialization.propertyListFromString(string);
    }

    /**
     * Creates a dictionary from a list of alternating objects and keys
     * starting with an object.
     * @param objectsAndKeys alternating list of objects and keys
     * @return NSDictionary containing all of the object-key pairs.
     */
    @SuppressWarnings("unchecked")
	public static NSDictionary dictionaryWithObjectsAndKeys(Object[] objectsAndKeys) {
        NSMutableDictionary<Object, Object> result = new NSMutableDictionary<Object, Object>();
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
        return new NSDictionary<Object, Object>(result);
    }

    /**
     * Removes an array of keys from a dictionary and
     * returns the result.
     * @param d dictionary to be pruned
     * @param a array of keys to be pruned
     * @return pruned dictionary
     */
    public static <K, V> NSDictionary<K, V> dictionaryByRemovingFromDictionaryKeysInArray(NSDictionary<K, V> d, NSArray<K> a) {
        NSMutableDictionary<K, V> result=new NSMutableDictionary<K, V>();
        if (d != null && a != null) {
            for (Enumeration<K> e = d.allKeys().objectEnumerator(); e.hasMoreElements();) {
                K key = e.nextElement();
                if (!a.containsObject(key)) {
                    result.setObjectForKey(d.objectForKey(key), key);
                }
            }
        }
        return result.immutableClone();
    }
    
    /**
     * Creates a new dictionary with only the keys and objects in the array.  The result is the objects for the
     * intersection of keys in the dictionary and the array.  This is the opposite of dictionaryByRemovingFromDictionaryKeysInArray.
     * 
     * @param d dictionary to be pruned
     * @param a array of keys to be included
     * @return pruned dictionary
     */
    public static <K, V> NSDictionary<K, V> dictionaryByRemovingKeysNotInArray(NSDictionary<K, V> d, NSArray<K> a) {
        NSMutableDictionary<K, V> result=new NSMutableDictionary<K, V>();
        if (d != null && a != null) {
            for (Enumeration<K> e = a.objectEnumerator(); e.hasMoreElements();) {
                K key = e.nextElement();
                V value = d.objectForKey(key);
                if (value != null) {
                    result.setObjectForKey(value, key);
                }
            }
        }
        return result.immutableClone();
    }

    /**
     *
     */
    public static <K, V> NSDictionary<K, V> removeNullValues(NSDictionary<K, V> dict) {
        NSMutableDictionary<K, V> d = new NSMutableDictionary<K, V>();
        for (Enumeration<K> e = dict.keyEnumerator(); e.hasMoreElements();) {
            K key = e.nextElement();
            V o = dict.objectForKey(key);
            if (!(o instanceof NSKeyValueCoding.Null)) {
                d.setObjectForKey(o, key);
            }
        }
        return d;
    }

    /**
     * Creates a dictionary from an objects and an array of key paths
     * @param object object to pull the values from
     * @param keys array of keys
     * @return NSDictionary containing all of the object-key pairs.
     */
    public static NSDictionary<String, Object> dictionaryFromObjectWithKeys(Object object, NSArray<String> keys) {
        NSMutableDictionary<String, Object> result = new NSMutableDictionary<String, Object>();
        if(object != null && keys != null) {
            for (Enumeration<String> e = keys.objectEnumerator(); e.hasMoreElements();) {
                String key = e.nextElement();
                Object value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, key);
                if(value != null) {
                    result.setObjectForKey(value, key);
                }
            }
        }
        return result.immutableClone();
    }

    // if you're keys are not all strings, this method will throw.
	public static NSArray<String> stringKeysSortedAscending(final NSDictionary<String, ?> d) {
        NSArray<String> result = null;

        if ( d != null && d.count() > 0 ) {
            final NSArray<String> keys = d.allKeys();
            result = ERXArrayUtilities.sortedArrayUsingComparator(keys, NSComparator.AscendingStringComparator);
        }

        return result != null ? result : NSArray.EmptyArray;
    }

    /**
     * @param d dictionary to sort keys from
     * @return keys from d sorted by ascending value they are mapped to
     */
    public static <T> NSArray<T> keysSortedByValueAscending(final NSDictionary<T, ?> d) {
        NSArray<T> result = null;

        if ( d != null && d.count() > 0 ) {
            final NSArray<T> keys = d.allKeys();
            result = ERXArrayUtilities.sortedArrayUsingComparator(keys, new NSDictionaryKeyValueComparator(d));
        }

        return result != null ? result : NSArray.EmptyArray;
    }

    /**
     * Removes entries from both dictionaries that match, leaving you with two dictionaries containing
     * only values that did NOT match.  Note that this comparison considers null == EO/NSKeyValueCoding.NullValue.
     *
     * @param dict1 the first dictionary
     * @param dict2 the second dictionary
     */
    public static <K, V> void removeMatchingEntries(NSMutableDictionary<? extends K, ? extends V> dict1, NSMutableDictionary<? extends K, ? extends V> dict2) {
         _removeMatchingEntries(dict1, dict2, true);
     }

    public static <K, V> void _removeMatchingEntries(NSMutableDictionary<? extends K, ? extends V> snapshot1, NSMutableDictionary<? extends K, ? extends V> snapshot2, boolean removeInverse) {
        Enumeration<? extends K> keys1Enum = snapshot1.allKeys().immutableClone().objectEnumerator();
        while (keys1Enum.hasMoreElements()) {
            String key = (String)keys1Enum.nextElement();
            Object value1 = snapshot1.objectForKey(key);
            Object value2 = snapshot2.objectForKey(key);
            boolean value1IsNull = (value1 == null || value1 == EOKeyValueCoding.NullValue || value1 == NSKeyValueCoding.NullValue);
            boolean value2IsNull = (value2 == null || value2 == EOKeyValueCoding.NullValue || value2 == NSKeyValueCoding.NullValue);
            if (value1IsNull && value2IsNull) {
                snapshot1.removeObjectForKey(key);
                snapshot2.removeObjectForKey(key);
            } else if (value1 != null && value1.equals(value2)) {
                snapshot1.removeObjectForKey(key);
                snapshot2.removeObjectForKey(key);
            }
        }
        // flip around the comparison and remove again
        if (removeInverse) {
            _removeMatchingEntries(snapshot2, snapshot1, false);
        }
    }

    /**
     * Sets the object for each of the keys in the array on a mutable dictionary.
     *
     * @param dictionary dictionary to mutate.  a null dictionary is a no-op.
     * @param object object to set.  an exception will be thrown if object is null.
     * @param keys array of keys to invoke <code>setObjectForKey()</code> for each key.  a null
     *        or empty array is a no-op.
     */
    public static <K, V> void setObjectForKeys(final NSMutableDictionary<K, V> dictionary, final V object, final NSArray<K> keys) {
        // n.b.: we explicitly don't check for a null object to be consistent with the rest of
        //       NSMutableDictionary's API
        if ( dictionary != null && keys != null && keys.count() > 0 ) {
            if ( keys.count() == 1 ) {
                dictionary.setObjectForKey(object, keys.objectAtIndex(0));
            } else {
                final Enumeration<K> e = keys.objectEnumerator();

                while ( e.hasMoreElements() ) {
                    dictionary.setObjectForKey(object, e.nextElement());
                }
            }
        }
    }

     /**
      * Compares dictionary keys based on the value they are associated with.  Useful for getting a list
      * of keys in alphabetical order of their values.
     */
     public static class NSDictionaryKeyValueComparator extends NSComparator {
         private NSDictionary<?,?> dictionary;

         public NSDictionaryKeyValueComparator(NSDictionary<?,?> aDictionary) {
             super();
             dictionary = aDictionary;
         }

        @Override
        public int compare(Object key1, Object key2) throws ComparisonException {
            Object value1 = dictionary.objectForKey(key1);
            Object value2 = dictionary.objectForKey(key2);
            if ( ! (value1 instanceof Comparable && value2 instanceof Comparable)) {
                throw new ComparisonException("dictionary values are not comparable");
            }

            return ((Comparable<Object>)value1).compareTo(value2);
        }
     }

     /**
      * Returns a deep clone of the given dictionary.  A deep clone will attempt to 
      * clone the keys and values (deeply) of this dictionary as well as the 
      * dictionary itself.
      * 
      * @param dict the dictionary to clone
      * @param onlyCollections if true, only collections in this dictionary will be cloned, not individual values
      * @return a deep clone of dict
      */
     public static <K, V> NSDictionary<K, V> deepClone(NSDictionary<K, V> dict, boolean onlyCollections) {
    	 NSMutableDictionary<K, V> clonedDict = null;
    	 if (dict != null) {
    		 clonedDict = dict.mutableClone();
	    	 for (K key : dict.allKeys()) {
	    		 V value = dict.objectForKey(key);
	    		 K cloneKey = ERXUtilities.deepClone(key, onlyCollections);
	    		 V cloneValue = ERXUtilities.deepClone(value, onlyCollections);
	    		 if (cloneKey != key) {
	    			 clonedDict.removeObjectForKey(key);
	    			 if (cloneValue != null) {
	    				 clonedDict.setObjectForKey(cloneValue, cloneKey);
	    			 }
	    		 } else if (cloneValue != null) {
	    			 if (cloneValue != value) {
	    				 clonedDict.setObjectForKey(cloneValue, cloneKey);
	    			 }
	    		 } else {
	    			 clonedDict.removeObjectForKey(key);
	    		 }
	    	 }
    	 }
    	 return clonedDict;
     }
     
     /**
	 * Encodes a dictionary into a string that can be used in a request uri.
	 * @param dict dictionary with form values
	 * @param separator optional value separator
	 */
	public static String queryStringForDictionary(NSDictionary<?, ?> dict, String separator) {
		return queryStringForDictionary(dict, separator,  WOMessage.defaultURLEncoding());
	}
    
    /**
	 * Encodes a dictionary into a string that can be used in a request uri.
	 * @param dict dictionary with form values
	 * @param separator optional value separator
	 */
	public static String queryStringForDictionary(NSDictionary<?, ?> dict, String separator, String encoding) {
		if (separator == null) {
			separator = "&";
		}
		StringBuilder sb = new StringBuilder(100);
		if (dict != null) {
			for (Enumeration<?> e = dict.allKeys().objectEnumerator(); e.hasMoreElements();) {
				Object key = e.nextElement();
				try {
					sb.append(URLEncoder.encode(key.toString(), encoding));
					sb.append('=');
					sb.append(URLEncoder.encode(dict.objectForKey(key).toString(), encoding));
					if (e.hasMoreElements()) {
						sb.append(separator);
					}
				}
				catch (UnsupportedEncodingException ex) {
					// yeah right...like this will ever happen
					throw NSForwardException._runtimeExceptionForThrowable(ex);
				}
			}
		}
		return sb.toString();
	}
}
