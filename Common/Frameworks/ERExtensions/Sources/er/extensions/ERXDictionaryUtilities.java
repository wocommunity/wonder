//
// NSDictionaryUtilities.java
// Project vwdBussinessLogicJava
//
// Created by ak on Wed Jun 06 2001
//
package er.extensions;

import java.util.Enumeration;

import com.webobjects.eocontrol.EOKeyValueCoding;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

/**
 * Collection of {@link com.webobjects.foundation.NSDictionary NSDictionary} utilities.
 */
public class ERXDictionaryUtilities extends Object {

    /**
     * Creates an immutable dictionary containing all of the keys and
     * objects from two dictionaries.
     * @param dict1 the first dictionary
     * @param dict2 the second dictionary
     * @return immutbale dictionary containing the union of the two dictionaries.
     */
    public static NSDictionary dictionaryWithDictionaryAndDictionary(NSDictionary dict1, NSDictionary dict2) {
        if(dict1 == null || dict1.allKeys().count() == 0)
            return dict2;
        if(dict2 == null || dict2.allKeys().count() == 0)
            return dict1;

        NSMutableDictionary result = new NSMutableDictionary(dict2);
        result.addEntriesFromDictionary(dict1);
        return new NSDictionary(result);
    }

    /**
     * Creates an NSDictionary from a resource associated with a given bundle
     * that is in property list format.<br/>
     * @param name name of the file or resource.
     * @param bundle NSBundle to which the resource belongs.
     * @return NSDictionary de-serialized from the property list.
     */
    public static NSDictionary dictionaryFromPropertyList(String name, NSBundle bundle) {
        String string = ERXStringUtilities.stringFromResource(name, "plist", bundle);
        return (NSDictionary)NSPropertyListSerialization.propertyListFromString(string);
    }

    /**
     * Creates a dictionary from a list of alternating objects and keys
     * starting with an object.
     * @param objectsAndKeys alternating list of objects and keys
     * @return NSDictionary containing all of the object-key pairs.
     */
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

    /**
     * Removes an array of keys from a dictionary and
     * returns the result.
     * @param d dictionary to be pruned
     * @param a array of keys to be pruned
     * @return pruned dictionary
     */
    public static NSDictionary dictionaryByRemovingFromDictionaryKeysInArray(NSDictionary d, NSArray a) {
        NSMutableDictionary result=new NSMutableDictionary();
        if (d != null && a != null) {
            for (Enumeration e = d.allKeys().objectEnumerator(); e.hasMoreElements();) {
                Object key = e.nextElement();
                if (!a.containsObject(key))
                    result.setObjectForKey(d.objectForKey(key), key);
            }
        }
        return result.immutableClone();
    }

    /**
     * @param firstRow
     * @return
     */
    public static NSDictionary removeNullValues(NSDictionary dict) {
        NSMutableDictionary d = new NSMutableDictionary();
        for (Enumeration e = dict.keyEnumerator(); e.hasMoreElements();) {
            Object key = e.nextElement();
            Object o = dict.objectForKey(key);
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
    public static NSDictionary dictionaryFromObjectWithKeys(Object object, NSArray keys) {
        NSMutableDictionary result = new NSMutableDictionary();
        if(object != null && keys != null) {
            for (Enumeration e = keys.objectEnumerator(); e.hasMoreElements();) {
                String key = (String)e.nextElement();
                Object value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, key);
                if(value != null) {
                    result.setObjectForKey(value, key);
                }
            }
        }
        return result.immutableClone();
    }

    // if you're keys are not all strings, this method will throw.
    public static NSArray stringKeysSortedAscending(final NSDictionary d) {
        NSArray result = null;

        if ( d != null && d.count() > 0 ) {
            final NSArray keys = d.allKeys();

            result = ERXArrayUtilities.sortedArrayUsingComparator(keys, NSComparator.AscendingStringComparator);
        }

        return result != null ? result : NSArray.EmptyArray;
    }

    /**
     * @param d dictionary to sort keys from
     * @return keys from d sorted by ascending value they are mapped to
     */
    public static NSArray keysSortedByValueAscending(final NSDictionary d) {
        NSArray result = null;

        if ( d != null && d.count() > 0 ) {
            final NSArray keys = d.allKeys();

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
     public static void removeMatchingEntries(NSMutableDictionary dict1, NSMutableDictionary dict2) {
         ERXDictionaryUtilities._removeMatchingEntries(dict1, dict2, true);
     }

     public static void _removeMatchingEntries(NSMutableDictionary snapshot1, NSMutableDictionary snapshot2, boolean removeInverse) {
         Enumeration keys1Enum = snapshot1.allKeys().immutableClone().objectEnumerator();
         while (keys1Enum.hasMoreElements()) {
             String key = (String)keys1Enum.nextElement();
             Object value1 = snapshot1.objectForKey(key);
             Object value2 = snapshot2.objectForKey(key);
             boolean value1IsNull = (value1 == null || value1 == EOKeyValueCoding.NullValue || value1 == NSKeyValueCoding.NullValue);
             boolean value2IsNull = (value2 == null || value2 == EOKeyValueCoding.NullValue || value2 == NSKeyValueCoding.NullValue);
             if (value1IsNull && value2IsNull) {
                 snapshot1.removeObjectForKey(key);
                 snapshot2.removeObjectForKey(key);
             }
             else if (value1 != null && value1.equals(value2)) {
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
    public static void setObjectForKeys(final NSMutableDictionary dictionary, final Object object, final NSArray keys) {
        // n.b.: we explicitly don't check for a null object to be consistent with the rest of
        //       NSMutableDictionary's API
        if ( dictionary != null && keys != null && keys.count() > 0 ) {
            if ( keys.count() == 1 ) {
                dictionary.setObjectForKey(object, keys.objectAtIndex(0));
            }
            else {
                final Enumeration e = keys.objectEnumerator();

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
         private NSDictionary dictionary;

         public NSDictionaryKeyValueComparator(NSDictionary aDictionary) {
             super();
             dictionary = aDictionary;
         }

        public int compare(Object key1, Object key2) throws ComparisonException {
            Object value1 = dictionary.objectForKey(key1);
            Object value2 = dictionary.objectForKey(key2);
            if ( ! (value1 instanceof Comparable && value2 instanceof Comparable)) {
                throw new ComparisonException("dictionary values are not comparable");
            }

            return ((Comparable)value1).compareTo(value2);
        }
     }

}
