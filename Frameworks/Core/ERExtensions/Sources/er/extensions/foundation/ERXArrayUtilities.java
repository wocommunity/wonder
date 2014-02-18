package er.extensions.foundation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierEvaluation;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSSet;

import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.eof.ERXKey;

/**
 * Collection of {@link com.webobjects.foundation.NSArray NSArray} utilities.
 */
public class ERXArrayUtilities {
	
	   private static Logger log = Logger.getLogger(ERXArrayUtilities.class);

	   /**
	    * Holds the null grouping key for use when grouping objects
	    * based on a key that might return null and nulls are allowed
	    */
    public static final String NULL_GROUPING_KEY="**** NULL GROUPING KEY ****";

    /** caches if array utilities have been initialized */
    private static boolean initialized = false;

    /** Caches sort orderings for given keys */
    private final static NSDictionary<String, NSSelector> _selectorsByKey = new NSDictionary<String, NSSelector>(new NSSelector[] {
        EOSortOrdering.CompareAscending,
        EOSortOrdering.CompareCaseInsensitiveAscending,
        EOSortOrdering.CompareCaseInsensitiveDescending,
        EOSortOrdering.CompareDescending,
    }, new String[] {
        "compareAscending",
        "compareCaseInsensitiveAscending",
        "compareCaseInsensitiveDescending",
        "compareDescending",
    });

    /**
     * Simply utility method to create a concrete set object from an array.
     *
     * @param array of elements
     * @return set created from given array
     */
    // CHECKME: Is this a value add?
    public static <T> NSSet<T> setFromArray(NSArray<T> array) {
        if (array == null || array.count() == 0) {
            return NSSet.EmptySet;
        }
        return new NSSet<T>(array);
    }

    /**
     * The qualifiers EOSortOrdering.CompareAscending and friends are
     * actually 'special' and processed in a different/faster way when
     * sorting than a selector that would be created by:
     * <code>new NSSelector("compareAscending", ObjectClassArray)</code>
     * This method eases the pain on creating those selectors from a string.
     *
     * @param key sort key
     * @return selector for the given sort ordering key or <code>null</code>
     */
    public static NSSelector sortSelectorWithKey(String key) {
        NSSelector result = null;
        if (key != null && !key.equals("")) {
            result = _selectorsByKey.objectForKey(key);
            if (result == null) {
            	result = new NSSelector(key, ERXConstant.ObjectClassArray);
            }
        }
        return result;
    }
    
    /**
     * Starting with an array of KeyValueCoding-compliant objects and a keyPath,
     * this method calls valueForKey on each object in the array and groups the
     * contents of the array, using the result of the valueForKey call as a key
     * in a dictionary. If passed a null array, null is returned. If passed a null
     * keyPath, an empty dictionary is returned. This is a typesafe variant of
     * arrayGroupedByKeyPath(NSArray<V> objects, String keyPath).
     *
     * <p>See arrayGroupedByKeyPath(NSArray<V> objects, String keyPath) for examples.</p>
     *
     * <p>This method calls
     * arrayGroupedByKeyPath(NSArray objects, String keyPath, Object nullGroupingKey, String valueKeyPath)
     * with includeNulls set to true and valueKeyPath set to null.</p>
     *
     * @param objects array of objects to be grouped
     * @param keyPath path into objects used to group the objects
     * @return a dictionary where the keys are the grouped values and the
     *          objects are arrays of the objects that have that value.
     *          Objects for which the key path returns null will be grouped
     *          with the key {@link #NULL_GROUPING_KEY}
     */
    public static <K, V> NSDictionary<K, NSArray<V>> arrayGroupedByKeyPath(NSArray<V> objects, ERXKey<K> keyPath) {
        return arrayGroupedByKeyPath(objects, (keyPath == null) ? null : keyPath.key());
    }

    /**
     * Starting with an array of KeyValueCoding-compliant objects and a keyPath,
     * this method calls valueForKey on each object in the array and groups the 
     * contents of the array, using the result of the valueForKey call as a key
     * in a dictionary. If passed a null array, null is returned. If passed a null
     * keyPath, an empty dictionary is returned.
     *
     * <p>If one starts with:
<pre>( { lastName = "Barker"; firstName = "Bob"; favoriteColor = "blue"; },
{ firstName = "Bob"; favoriteColor = "red"; },
{ lastName = "Further"; firstName = "Frank"; favoriteColor = "green"; } )</pre>
     * and one calls arrayGroupedByKeyPath(objects, "firstName"), one gets:
<pre>{ "Bob" = ( { lastName = "Barker"; firstName = "Bob"; favoriteColor = "blue"; }, { firstName = "Bob"; favoriteColor = "red"; } );
"Frank" = ( { lastName = "Further"; firstName = "Frank"; favoriteColor = "green"; } ); }</pre><br/>
     * If one calls arrayGroupedByKeyPath(objects, "lastName"), one gets:
<pre>{ "Bob" = ( { lastName = "Barker"; firstName = "Bob"; favoriteColor = "blue"; } );
"Frank" = ( { lastName = "Further"; firstName = "Frank"; favoriteColor = "green"; } );
"**** NULL GROUPING KEY ****" = ( { firstName = "Bob"; favoriteColor = "red"; } ); }</pre></p>
     *
     * <p>This method calls arrayGroupedByKeyPath(objects, keyPath, includeNulls, valueKeyPath) with
     * includeNulls set to true and valueKeyPath set to null.</p>
     * 
     * @param objects array of objects to be grouped
     * @param keyPath path into objects used to group the objects
     * @return a dictionary where the keys are the grouped values and the
     * 		objects are arrays of the objects that have that value.
     *		Objects for which the key path returns null will be grouped 
     *          with the key {@link #NULL_GROUPING_KEY}
     */
    public static <K, V> NSDictionary<K, NSArray<V>> arrayGroupedByKeyPath(NSArray<V> objects, String keyPath) {
        return arrayGroupedByKeyPath(objects,keyPath,true,null);
    }

    /**
     * Starting with an array of KeyValueCoding-compliant objects and a keyPath,
     * this method calls valueForKey on each object in the array and groups the
     * contents of the array, using the result of the valueForKey call as a key
     * in a dictionary. If passed a null array, null is returned. If passed a null
     * keyPath, an empty dictionary is returned. If valueKeyPath is not null, then
     * the grouped arrays each have valueForKey called with valueKeyPath and the
     * grouped arrays are replaced with the results of those calls. This is a
     * typesafe variant of
     * arrayGroupedByKeyPath(NSArray<T> objects, String keyPath, boolean includeNulls, String valueKeyPath).
     *
     * <p>See arrayGroupedByKeyPath(NSArray<T> objects, String keyPath, boolean includeNulls, String valueKeyPath)
     * for examples.</p>
     *
     * @param objects array of objects to be grouped
     * @param keyPath path into objects used to group the objects
     * @param includeNulls determines if keyPaths that resolve to null
     *        are included in the resulting dictionary
     * @param valueKeyPath used to call valueForKey on the arrays in
     *        the results dictionary, with the results of those calls each
     *        replacing the corresponding array in the results dictionary.
     * @return a dictionary where the keys are the grouped values and the
     *          objects are arrays of the objects that have that value.
     *          Objects for which the key path returns null will be grouped
     *          with the key {@link #NULL_GROUPING_KEY}
     */
    public static <T, K, V> NSDictionary<K, NSArray<V>> arrayGroupedByKeyPath(NSArray<T> objects, ERXKey<K> keyPath, boolean includeNulls, ERXKey<V> valueKeyPath) {
        return arrayGroupedByKeyPath(objects, (keyPath == null) ? null : keyPath.key(), includeNulls, (valueKeyPath == null) ? null : valueKeyPath.key());
    }

    /**
     * Starting with an array of KeyValueCoding-compliant objects and a keyPath,
     * this method calls valueForKey on each object in the array and groups the
     * contents of the array, using the result of the valueForKey call as a key
     * in a dictionary. If passed a null array, null is returned. If passed a null
     * keyPath, an empty dictionary is returned. If valueKeyPath is not null, then
     * the grouped arrays each have valueForKey called with valueKeyPath and the
     * grouped arrays are replaced with the results of those calls.
     * 
     * <p>If one starts with:
<pre>( { lastName = "Barker"; firstName = "Bob"; favoriteColor = "blue"; },
{ firstName = "Bob"; favoriteColor = "red"; },
{ lastName = "Further"; firstName = "Frank"; favoriteColor = "green"; } )</pre>
     * and one calls arrayGroupedByKeyPath(objects, "firstName", true, "favoriteColor"), one gets:
<pre>{Frank = ("green"); Bob = ("blue", "red");</pre>
     * If one calls arrayGroupedByKeyPath(objects, "lastName", false, "favoriteColor"), one gets:
<pre>{Further = ("green"); Barker = ("blue"); }</pre></p>
     * If one calls arrayGroupedByKeyPath(objects, "lastName", true, "favoriteColor"), one gets:
<pre>{Further = ("green"); Barker = ("blue"); "**** NULL GROUPING KEY ****" = ("red"); }</pre></p>
     *
     * @param objects array of objects to be grouped
     * @param keyPath path into objects used to group the objects
     * @param includeNulls determines if keyPaths that resolve to null
     *        are included in the resulting dictionary
     * @param valueKeyPath used to call valueForKey on the arrays in
     *        the results dictionary, with the results of those calls each
     *        replacing the corresponding array in the results dictionary.
     * @return a dictionary where the keys are the grouped values and the
     *          objects are arrays of the objects that have that value.
     *          Objects for which the key path returns null will be grouped
     *          with the key {@link #NULL_GROUPING_KEY}
     */
    public static <T, K, V> NSDictionary<K, NSArray<V>> arrayGroupedByKeyPath(NSArray<T> objects, String keyPath, boolean includeNulls, String valueKeyPath) {
        return arrayGroupedByKeyPath(objects, keyPath, (includeNulls) ? (K)NULL_GROUPING_KEY : null, valueKeyPath);
    }

    /**
     * Starting with an array of KeyValueCoding-compliant objects and a keyPath,
     * this method calls valueForKey on each object in the array and groups the
     * contents of the array, using the result of the valueForKey call as a key
     * in a dictionary. If passed a null array, null is returned. If passed a null
     * keyPath, an empty dictionary is returned. If valueKeyPath is not null, then
     * the grouped arrays each have valueForKey called with valueKeyPath and the
     * grouped arrays are replaced with the results of those calls. This is a
     * typesafe variant of
     * arrayGroupedByKeyPath(NSArray objects, String keyPath, Object nullGroupingKey, String valueKeyPath).
     *
     * <p>See arrayGroupedByKeyPath(NSArray objects, String keyPath, Object nullGroupingKey, String valueKeyPath)
     * for examples.</p>
     *
     * @param objects array of objects to be grouped
     * @param keyPath path into objects used to group the objects
     * @param nullGroupingKey used as the key in the results dictionary
     *        for the array of objects for which the valueForKey with keyPath
     *        result is null.
     * @param valueKeyPath used to call valueForKey on the arrays in
     *        the results dictionary, with the results of those calls each
     *        replacing the corresponding array in the results dictionary.
     * @return a dictionary where the keys are the grouped values and the
     *          objects are arrays of the objects that have that value.
     *          Objects for which the key path returns null will be grouped
     *          with the key {@link #NULL_GROUPING_KEY}
     */
    public static <T, K, V> NSDictionary<K, NSArray<V>> arrayGroupedByKeyPath(NSArray<T> objects, ERXKey<K> keyPath, K nullGroupingKey, ERXKey<V> valueKeyPath) {
        return arrayGroupedByKeyPath(objects, (keyPath == null) ? null : keyPath.key(), nullGroupingKey, (valueKeyPath == null) ? null : valueKeyPath.key());
    }
    
    /**
     * Starting with an array of KeyValueCoding-compliant objects and a keyPath,
     * this method calls valueForKey on each object in the array and groups the
     * contents of the array, using the result of the valueForKey call as a key
     * in a dictionary. If passed a null array, null is returned. If passed a null
     * keyPath, an empty dictionary is returned. If valueKeyPath is not null, then
     * the grouped arrays each have valueForKey called with valueKeyPath and the
     * grouped arrays are replaced with the results of that call.
     *
     * <p>If one starts with:
<pre>( { lastName = "Barker"; firstName = "Bob"; favoriteColor = "blue"; },
{ firstName = "Bob"; favoriteColor = "red"; },
{ lastName = "Further"; firstName = "Frank"; favoriteColor = "green"; } )</pre>
     * and one calls arrayGroupedByKeyPath(objects, "firstName", null, "favoriteColor"), one gets:
<pre>{Frank = ("green"); Bob = ("blue", "red");</pre>
     * If one calls arrayGroupedByKeyPath(objects, "lastName", "extra", "favoriteColor"), one gets:
<pre>{Further = ("green"); Barker = ("blue"); "extra" = ("red"); }</pre>
     * If one calls arrayGroupedByKeyPath(objects, "lastName", null, "favoriteColor"), one gets:
<pre>{Further = ("green"); Barker = ("blue"); "**** NULL GROUPING KEY ****" = ("red"); }</pre></p>
     *
     * @param objects array of objects to be grouped
     * @param keyPath path into objects used to group the objects
     * @param nullGroupingKey used as the key in the results dictionary
     *        for the array of objects for which the valueForKey with keyPath
     *        result is null.
     * @param valueKeyPath used to call valueForKey on the arrays in
     *        the results dictionary, with the results of those calls each
     *        replacing the corresponding array in the results dictionary.
     * @return a dictionary where the keys are the grouped values and the
     *          objects are arrays of the objects that have that value.
     *          Objects for which the key path returns null will be grouped
     *          with the key {@link #NULL_GROUPING_KEY}
     */
    @SuppressWarnings("unchecked")
    public static NSDictionary arrayGroupedByKeyPath(NSArray objects, String keyPath, Object nullGroupingKey, String valueKeyPath) {
        if (objects == null) return null;
        NSMutableDictionary result=new NSMutableDictionary();
        for (Enumeration e=objects.objectEnumerator(); e.hasMoreElements();) {
            Object eo = e.nextElement();
            Object key = NSKeyValueCodingAdditions.Utility.valueForKeyPath(eo,keyPath);
            boolean isNullKey = key==null || key instanceof NSKeyValueCoding.Null;
            if (!isNullKey || nullGroupingKey != null) {
                if (isNullKey) key=nullGroupingKey;
                NSMutableArray existingGroup=(NSMutableArray)result.objectForKey(key);
                if (existingGroup==null) {
                    existingGroup=new NSMutableArray();
                    result.setObjectForKey(existingGroup,key);
                }
                if (valueKeyPath!=null) {
                    Object value=NSKeyValueCodingAdditions.Utility.valueForKeyPath(eo,valueKeyPath);
                    if (value!=null) existingGroup.addObject(value);
                } else {
                    existingGroup.addObject(eo);
                }
            }
        }
        return result;
    }
    
    /**
     * Typesafe variant of arrayGroupedByToManyKeyPath.
     * 
     * @param objects the objects to be grouped
     * @param keyPath the key to group by
     * @param includeNulls determins if the keypaths that resolve to null should be allowed in the group
     * @return the resulting dictionary
     */
	public static <K, V> NSDictionary<K, NSArray<V>> arrayGroupedByToManyKeyPath(NSArray<V> objects, ERXKey<K> keyPath, boolean includeNulls) {
    	return arrayGroupedByToManyKeyPath(objects, keyPath.key(), includeNulls);
    }
    
    /**
     * Groups an array of objects by a given to-many key path, where every
     * single item in the to-many will put the object in the corresponding group. 
     * A typical example is an array of users with a roles relationship. The result to
     * calling <code>arrayGroupedByToManyKeyPath(users, "roles.name")</code> would be 
     * <code>"admin" = (user1, user2); "editor" = (user3);...</code>.
     * The dictionary that is returned contains keys that correspond to the grouped
     * keys values. This means that the object pointed to by the key
     * path must be a cloneable object. For instance using the key path
     * 'users' would not work because enterprise objects are not
     * cloneable. Instead you might choose to use the key path 'users.name'
     * of 'users.primaryKey', if your enterprise objects support this
     * see {@link ERXGenericRecord} if interested.
     * @param objects array of objects to be grouped
     * @param keyPath path used to group the objects.
     * @param includeNulls determines if keyPaths that resolve to null
     *      should be allowed into the group.
     * @return a dictionary where the keys are the grouped values and the
     *      objects are arrays of the objects that have the grouped
     *      characteristic. Note that if the key path returns null
     *      then one of the keys will be the static ivar NULL_GROUPING_KEY
     */
    public static <K, V> NSDictionary<K, NSArray<V>> arrayGroupedByToManyKeyPath(NSArray<V> objects,
            String keyPath,
            boolean includeNulls) {
    	return arrayGroupedByToManyKeyPath(objects, keyPath,  (includeNulls) ? (K) NULL_GROUPING_KEY : null);
    }
    
    /**
     * Typesafe variant of arrayGroupedByToManyKeyPath.
     * 
     * @param objects array of objects to be grouped
     * @param keyPath path used to group the objects.
     * @param nullGroupingKey if not-null, determines if keyPaths that resolve to null
     *      should be allowed into the group; if so, this key is used for them
     * @return a dictionary where the keys are the grouped values and the
     *      objects are arrays of the objects that have the grouped
     *      characteristic. Note that if the key path returns null
     *      then one of the keys will be the static ivar NULL_GROUPING_KEY
     */
	public static <K, V> NSDictionary<K, NSArray<V>> arrayGroupedByToManyKeyPath(NSArray<V> objects, ERXKey<K> keyPath, K nullGroupingKey) {
    	return arrayGroupedByToManyKeyPath(objects, keyPath.key(), nullGroupingKey);
    }

    /**
     * Groups an array of objects by a given to-many key path, where every
     * single item in the to-many will put the object in the corresponding group. 
     * The dictionary that is returned contains keys that correspond to the grouped
     * keys values. This means that the object pointed to by the key
     * path must be a cloneable object. For instance using the key path
     * 'users' would not work because enterprise objects are not
     * cloneable. Instead you might choose to use the key path 'users.name'
     * of 'users.primaryKey', if your enterprise objects support this
     * see {@link ERXGenericRecord} if interested.
     * @param objects array of objects to be grouped
     * @param keyPath path used to group the objects.
     * @param nullGroupingKey if not-null, determines if keyPaths that resolve to null
     *      should be allowed into the group; if so, this key is used for them
     * @return a dictionary where the keys are the grouped values and the
     *      objects are arrays of the objects that have the grouped
     *      characteristic. Note that if the key path returns null
     *      then one of the keys will be the static ivar NULL_GROUPING_KEY
     */
	public static <K, V> NSDictionary<K, NSArray<V>> arrayGroupedByToManyKeyPath(NSArray<V> objects,
            String keyPath,
            K nullGroupingKey) {
    	return arrayGroupedByToManyKeyPath(objects, keyPath, nullGroupingKey, null);
    }
    
    /**
     * Typesafe variant of arrayGroupedByToManyKeyPath.
     * 
     * @param objects array of objects to be grouped
     * @param keyPath path used to group the objects.
     * @param nullGroupingKey if not-null, determines if keyPaths that resolve to null
     *      should be allowed into the group; if so, this key is used for them
     * @param valueKeyPath allows the grouped objects in the result to be
     *        derived from objects (by evaluating valueKeyPath), instead
     *        of being members of the objects collection.  Objects that 
     *        evaluate valueKeyPath to null have no value included in the
     *        result
     * @return a dictionary where the keys are the grouped values and the
     *      objects are arrays of the objects that have the grouped
     *      characteristic. Note that if the key path returns null
     *      then one of the keys will be the static ivar NULL_GROUPING_KEY
     */
	public static <T, K, V> NSDictionary<K, NSArray<V>> arrayGroupedByToManyKeyPath(NSArray<T> objects, ERXKey<K> keyPath, K nullGroupingKey, ERXKey<V> valueKeyPath) {
    	return arrayGroupedByToManyKeyPath(objects, keyPath.key(), nullGroupingKey, (valueKeyPath == null) ? null : valueKeyPath.key());
    }

    /**
     * Groups an array of objects by a given to-many key path, where every
     * single item in the to-many will put the object in the corresponding group. 
     * The dictionary that is returned contains keys that correspond to the grouped
     * keys values. This means that the object pointed to by the key
     * path must be a cloneable object. For instance using the key path
     * 'users' would not work because enterprise objects are not
     * cloneable. Instead you might choose to use the key path 'users.name'
     * of 'users.primaryKey', if your enterprise objects support this
     * see {@link ERXGenericRecord} if interested.
     * @param objects array of objects to be grouped
     * @param keyPath path used to group the objects.
     * @param nullGroupingKey if not-null, determines if keyPaths that resolve to null
     *      should be allowed into the group; if so, this key is used for them
     * @param valueKeyPath allows the grouped objects in the result to be
     *        derived from objects (by evaluating valueKeyPath), instead
     *        of being members of the objects collection.  Objects that 
     *        evaluate valueKeyPath to null have no value included in the
     *        result
     * @return a dictionary where the keys are the grouped values and the
     *      objects are arrays of the objects that have the grouped
     *      characteristic. Note that if the key path returns null
     *      then one of the keys will be the static ivar NULL_GROUPING_KEY
     */
    @SuppressWarnings("unchecked")
	public static NSDictionary arrayGroupedByToManyKeyPath(NSArray objects,
            String keyPath,
            Object nullGroupingKey,
            String valueKeyPath) {
        NSMutableDictionary result=new NSMutableDictionary();
        for (Enumeration e=objects.objectEnumerator(); e.hasMoreElements();) {
            Object object = e.nextElement();
            Object key = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object,keyPath);
            boolean isNullKey = key==null || key instanceof NSKeyValueCoding.Null;
            if (!isNullKey || nullGroupingKey != null) {
                if (isNullKey) key=nullGroupingKey;
                NSArray array = (NSArray)key;
                for(@SuppressWarnings("null") Enumeration keys = array.objectEnumerator(); keys.hasMoreElements(); ) {
                    key = keys.nextElement();
                    NSMutableArray existingGroup=(NSMutableArray)result.objectForKey(key);
                    if (existingGroup==null) {
                        existingGroup=new NSMutableArray();
                        result.setObjectForKey(existingGroup,key);
                    }
                    if (valueKeyPath!=null) {
                        Object value=NSKeyValueCodingAdditions.Utility.valueForKeyPath(object,valueKeyPath);
                        if (value!=null) existingGroup.addObject(value);
                    } else {
                        existingGroup.addObject(object);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Simple comparison method to see if two array
     * objects are identical sets.
     * @param a1 first array
     * @param a2 second array
     * @return result of comparison
     */
    public static <T> boolean arraysAreIdenticalSets(NSArray<? super T> a1, NSArray<? super T> a2) {
    	if (a1 == null || a2 == null) {
    		return a1 == a2;
    	}
        boolean result=true;
        for (Enumeration<? super T> e=a1.objectEnumerator();e.hasMoreElements();) {
            Object i=e.nextElement();
            if (!a2.containsObject(i)) {
                result=false; break;
            }
        }
        if (result) {
            for (Enumeration<? super T> e=a2.objectEnumerator();e.hasMoreElements();) {
                Object i=e.nextElement();
                if (!a1.containsObject(i)) {
                    result=false; break;
                }
            }
        }
        return result;
    }

    /**
     * Filters an array using the {@link com.webobjects.eocontrol.EOQualifierEvaluation EOQualifierEvaluation} interface.
     * 
     * @param array to be filtered
     * @param qualifier to do the filtering
     * @return array of filtered results.
     */
    public static <T> NSArray<T> filteredArrayWithQualifierEvaluation(NSArray<T> array, EOQualifierEvaluation qualifier) {
        if (array == null) {
            return NSArray.EmptyArray;
        }
        return filteredArrayWithQualifierEvaluation(array.objectEnumerator(), qualifier);
    }

    /**
     * Filters any kinds of collections that implements {@link Enumeration} 
     * interface such as {@link com.webobjects.foundation.NSArray NSArray}, {@link com.webobjects.foundation.NSSet NSSet}, {@link Vector} 
     * and {@link Hashtable} using the {@link com.webobjects.eocontrol.EOQualifierEvaluation EOQualifierEvaluation} interface. 
     *
     * @param enumeration to be filtered; to obtain an enumeration, 
     *             use objectEnumerator() for the collections in 
     *             com.webobjects.foundation package 
     *             and use elements() for the Vector and Hashtable
     * @param qualifier to do the filtering
     * @return array of filtered results.
     */
    public static <T> NSArray<T> filteredArrayWithQualifierEvaluation(Enumeration<T> enumeration, EOQualifierEvaluation qualifier) {
        NSMutableArray<T> result = new NSMutableArray<T>();
        while (enumeration.hasMoreElements()) {
            T object = enumeration.nextElement();
            if (qualifier.evaluateWithObject(object)) 
                result.addObject(object);
        }
        return result;
    }

    /**
     * Filters any kinds of collections that implements {@link Enumeration} 
     * interface such as {@link com.webobjects.foundation.NSArray NSArray}, {@link com.webobjects.foundation.NSSet NSSet}, {@link Vector} 
     * and {@link Hashtable} using the {@link com.webobjects.eocontrol.EOQualifierEvaluation EOQualifierEvaluation} interface. 
     *
     * @param enumeration to be filtered; to obtain an enumeration, 
     *             use objectEnumerator() for the collections in 
     *             com.webobjects.foundation package 
     *             and use elements() for the Vector and Hashtable
     * @param qualifier to do the filtering
     * @return true if there is at least one match
     */
    public static boolean enumerationHasMatchWithQualifierEvaluation(Enumeration<?> enumeration, EOQualifierEvaluation qualifier) {
        while (enumeration.hasMoreElements()) {
            Object object = enumeration.nextElement();
            if (qualifier.evaluateWithObject(object)) 
                return true;
        }
        return false;
    }

    /**
     * Filters any kinds of collections that implements {@link Iterator} 
     * interface such as {@link com.webobjects.foundation.NSArray NSArray}, {@link com.webobjects.foundation.NSSet NSSet}, {@link Vector} 
     * and {@link Hashtable} using the {@link com.webobjects.eocontrol.EOQualifierEvaluation EOQualifierEvaluation} interface. 
     *
     * @param iterator to be filtered; to obtain an iterator, 
     *             use iterator() for the java collections
     * @param qualifier to do the filtering
     * @return true if there is at least one match
     */
    public static boolean iteratorHasMatchWithQualifierEvaluation(Iterator<?> iterator, EOQualifierEvaluation qualifier) {
        while (iterator.hasNext()) {
            Object object = iterator.next();
            if (qualifier.evaluateWithObject(object)) 
                return true;
        }
        return false;
    }

    /**
     * Filters any kind of collections that implements {@link Iterator} 
     * interface such as {@link ArrayList}, {@link HashMap}, {@link SortedSet} 
     * and {@link TreeSet} using the {@link com.webobjects.eocontrol.EOQualifierEvaluation EOQualifierEvaluation} interface. 
     *
     * @param iterator to be filtered; use iterator() to obtain 
     *             an iterator from the collections
     * @param qualifier to do the filtering
     * @return array of filtered results.
     */
    public static <T> NSArray<T> filteredArrayWithQualifierEvaluation(Iterator<T> iterator, EOQualifierEvaluation qualifier) {
        NSMutableArray<T> result = new NSMutableArray<T>();
        while (iterator.hasNext()) {
            T object = iterator.next();
            if (qualifier.evaluateWithObject(object)) 
                result.addObject(object);
        }
        return result;
    }

    /**
     * Filters out duplicates of an array of objects
     * based on the value of the given key path off of those objects.
     * Objects with a null value will be skipped, too.
     * @param objects array of objects
     * @param key keypath to be evaluated off of every object
     * @return filter array of objects based on the value of a keypath.
     */
    public static <T> NSArray<T> arrayWithoutDuplicateKeyValue(NSArray<T> objects, String key){
        NSMutableSet<T> present = new NSMutableSet<T>();
        NSMutableArray<T> result = new NSMutableArray<T>();
        for(Enumeration<T> e = objects.objectEnumerator(); e.hasMoreElements(); ){
            T o = e.nextElement();
            T value = (T)NSKeyValueCodingAdditions.Utility.valueForKeyPath(o, key);
            if(value != null && !present.containsObject(value)) {
                present.addObject(value);
                result.addObject(o);
            }
        }
        return result;
    }

    /**
     * Subtracts the contents of one array from another.
     * The order of the array should be preseved.
     * 
     * @param main array to have values removed from it.
     * @param minus array of values to remove from the main array
     * @return array after performing subtraction.
     */
    public static <T> NSArray<T> arrayMinusArray(NSArray<T> main, NSArray<?> minus) {
		NSSet<Object> minusSet = new NSSet<Object>(minus);
		NSMutableArray<T> mutableResult = new NSMutableArray<T>(main.count()); 
		Enumeration<T> e = main.objectEnumerator();
		while (e.hasMoreElements()) {
			T obj = e.nextElement();
			if (! minusSet.containsObject(obj)) 
				mutableResult.addObject(obj);
		}
		return mutableResult.immutableClone();
    }
	
    /**
     * Subtracts a single object from an array.
     * @param main array to have value removed from it.
     * @param object to be removed
     * @return array after performing subtraction.
     */
    public static <T> NSArray<T> arrayMinusObject(NSArray<T> main, Object object) {
        NSMutableArray<T> mutable = new NSMutableArray<T>(main);
        mutable.removeObject(object);
        return mutable.immutableClone();
    }
        
    /**
     * Creates an array preserving order by adding all of the
     * non-duplicate values from the second array to the first.
     * @param a1 first array
     * @param a2 second array
     * @return array containing all of the elements of the first
     *		array and all of the non-duplicate elements of
     *		the second array.
     */
    public static <T> NSArray<T> arrayByAddingObjectsFromArrayWithoutDuplicates(NSArray<? extends T> a1, NSArray<? extends T> a2) {
        if (a2.count()==0)
            return (NSArray<T>) a1;
        
        NSMutableArray<T> result=new NSMutableArray<T>(a1);
        addObjectsFromArrayWithoutDuplicates(result, a2);
        return result;
    }
    
    /**
     * Creates an array that has all of the objects of the parameter array
     * without the first object.
     * @param array the array to use to create the result
     * @return an array containing all objects but the first of the
     *         parameter array.  if null is passed, null is returned.
     *         if the parameter array is empty, an empty array is returned.
     */
    public static <T> NSArray<T> arrayByRemovingFirstObject(NSArray<T> array) {
        NSArray<T> result = null;
        
        if ( array != null ) {
            final int count = array.count();
            
            result = count > 1 ? array.subarrayWithRange(new NSRange(1, count-1)) : NSArray.EmptyArray;
        }
        
        return result;
    }
    
    /**
     * Adds the object to the mutable array if the object is not null.
     * @param array mutable array where non-null object will be added
     * @param object to be added to array
     */
    public static <T> void safeAddObject(NSMutableArray<T> array, T object) {
        if (array != null && object != null) {
            array.addObject(object);
        }
    }
 
    /**
     * Adds all of the non-duplicate elements from the second
     * array to the mutable array.
     * @param a1 mutable array where non-duplicate objects are
     *		added
     * @param a2 array to be added to a1
     */
    public static <T> void addObjectsFromArrayWithoutDuplicates(NSMutableArray<T> a1, NSArray<? extends T> a2) {
        NSMutableSet<T> resultSet=new NSMutableSet<T>(a1);
        for (Enumeration<? extends T> e=a2.objectEnumerator(); e.hasMoreElements();) {
        	T elt=e.nextElement();
        	if (!resultSet.containsObject(elt)) { 
            	a1.addObject(elt);
            	resultSet.addObject(elt);
            }
        }
    }

    /** 
     * Recursively flattens an array of arrays and individual
     * objects into a single array of elements.<br/>
     * <br/>
     * For example:<br/>
     * <code>NSArray foos;</code> //Assume exists<br/>
     * <code>NSArray bars = (NSArray)foos.valueForKey("toBars");</code>
     * In this case if <code>foos</code> contained five elements 
     * then the array <code>bars</code> will contain five arrays
     * each corresponding to what <code>aFoo.toBars</code> would
     * return. To have the entire collection of <code>bars</code>
     * in one single array you would call:
     * <code>NSArray allBars = flatten(bars)</code>
     * @param originalArray array to be flattened
     * @param filterDuplicates determines if the duplicate values
     *      should be filtered
     * @return an array containing all of the elements from
     *      all of the arrays contained within the array
     *      passed in. (Optionally, with duplicate elements filtered out)
     */
    @SuppressWarnings("unchecked")
	public static NSArray flatten(NSArray<?> originalArray, boolean filterDuplicates) {
        NSArray<?> flattenedArray = flatten(originalArray);
        
        if (filterDuplicates) {
            return arrayWithoutDuplicates(flattenedArray);
        } 
        return flattenedArray;
    }
    

    /** 
     * Recursively flattens an array of arrays and individual
     * objects into a single array of elements.<br/>
     * <br/>
     * For example:<br/>
     * <code>NSArray foos;</code> //Assume exists<br/>
     * <code>NSArray bars = (NSArray)foos.valueForKey("toBars");</code>
     * In this case if <code>foos</code> contained five elements 
     * then the array <code>bars</code> will contain five arrays
     * each corresponding to what <code>aFoo.toBars</code> would
     * return. To have the entire collection of <code>bars</code>
     * in one single array you would call:
     * <code>NSArray allBars = flatten(bars)</code>
     * @param originalArray array to be flattened
     * @return an array containing all of the elements from
     *      all of the arrays contained within the array
     *      passed in.
     */
    @SuppressWarnings("unchecked")
	public static NSArray flatten(NSArray<?> originalArray) {
        if (originalArray == null || originalArray.count() < 1) {
            return originalArray;
        }
        
        NSMutableArray<Object> newArray = null;  // Not gonna create a new array if we don't actually need to flatten
        for (int i = 0; i < originalArray.count(); i++) {
            Object element = originalArray.objectAtIndex(i);
            if (element instanceof NSArray) {
                if (newArray == null) {
                    // Turns out we actually need to flatten
                    newArray = new NSMutableArray<Object>();
                    for (int backfillIndex = 0; backfillIndex < i; backfillIndex++) {
                        // backfill any singles we put off copying
                        newArray.addObject(originalArray.objectAtIndex(backfillIndex));
                    }
                }
                
                NSArray<?> flattenedChildArray = flatten((NSArray<?>)element);
                newArray.addObjectsFromArray(flattenedChildArray);
            } else if (newArray != null) {
                newArray.addObject(element);
            }  // Otherwise let's put off copying the elment, the backfill section above will take care of it.
        }
        
        // CLEANUP: Arguably safer to return the immutable array we are declared as returning
        return (newArray != null ? newArray : originalArray);
    }

    /**
     * Creates an NSArray from a resource associated with a given bundle
     * that is in property list format.<br/>
     * @param name name of the file or resource.
     * @param bundle NSBundle to which the resource belongs.
     * @return NSArray de-serialized from the property list.
     */
    @SuppressWarnings("unchecked")
	public static NSArray arrayFromPropertyList(String name, NSBundle bundle) {
        return (NSArray<?>)NSPropertyListSerialization.propertyListFromString(ERXStringUtilities.stringFromResource(name, "plist", bundle));
    }

    /**
     * Performs multiple key-value coding calls against an array or an object.
     * @param array collection or object to be acted upon.
     * @param paths array of keypaths.
     * @return for collections, returns an array containing an array of values for every keypath.
     * For objects, returns an array containing a value for every keypath.
     */
    @SuppressWarnings("unchecked")
	public static NSArray valuesForKeyPaths(Object array, NSArray<String> paths) {
        NSMutableArray<Object> result = new NSMutableArray<Object>();

        Enumeration<String> e = paths.objectEnumerator();
        while(e.hasMoreElements()) {
        	Object value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(array, e.nextElement());
            result.addObject(value != null ? value : NSKeyValueCoding.NullValue);
        }
        return result;
    }

    /**
     * Returns the first object of the array.  If the array is null or empty, null is returned.
     *
     * @param array the array to search.
     * @return the first object in array.  null if array is empty or if array is null.
     */
    public static <T> T firstObject(NSArray<T> array) {
        T result = null;
        
        if ( array != null && array.count() > 0 )
            result = array.objectAtIndex(0);
        
        return result;
    }
    
    /**
     * Finds the index of the first object in the array with a given value for a given keypath.
     * Assumes that all objects in the array either are NSKeyValueCoding.NullValue or have the given keypath.
     * @param array the array to search.
     * @param value the value to look for.
     * @param keyPath the keypath to use to compare to value.
     * @return index of the first object with the qualification.  -1 if none matches.
     */
    public static int indexOfFirstObjectWithValueForKeyPath(NSArray<?> array, Object value, String keyPath) {
        final int count = array.count();
        int result = -1;
        int i = 0;
        while ( i < count) {
            Object currentObject = array.objectAtIndex(i);
            if(currentObject != NSKeyValueCoding.NullValue) {
                Object currentValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(currentObject, keyPath);
                currentValue = (currentValue == NSKeyValueCoding.NullValue ? null : currentValue);
                if(currentValue == value || (value != null && value.equals(currentValue))) {
                    return i;
                }
            }
            i++;
        }
        
        return result;
    }
    
    /**
     * Finds the first object in the array with a given value for a given key path.
     * @param array the array to search.
     * @param value the value to look for.
     * @param keyPath the keypath to use to compare to value.
     * @return first object in the array with the qualification.  null if none matches.
     */
    public static <T> T firstObjectWithValueForKeyPath(NSArray<T> array, Object value, String keyPath) {
        final int index = indexOfFirstObjectWithValueForKeyPath(array, value, keyPath);
        
        return index >= 0 ? array.objectAtIndex(index) : null;
    }

    /**
     * Walks over an array and returns an array of objects from that array that have a particular
     * value for a particular key path.  Treats null and NSKeyValueCoding.NullValue equivalently.
     * Any NSKeyValueCoding.NullValue objects in the array are skipped.  If array is null or empty,
     * an empty array is returned.
     *
     * @param array array to search
     * @param valueToLookFor value to look for
     * @param keyPath key path to apply on each object on the array to compare against valueToLookFor
     * @return an array of matching objects
     */
    public static <T> NSArray<T> objectsWithValueForKeyPath(final NSArray<T> array, final Object valueToLookFor, final String keyPath) {
        final boolean valueToLookForIsNull = valueToLookFor == null || valueToLookFor == NSKeyValueCoding.NullValue;
        NSArray<T> result = null;

        if ( array != null && array.count() > 0 ) {
            final NSMutableArray<T> a = new NSMutableArray<T>();
            final Enumeration<T> arrayEnumerator = array.objectEnumerator();

            while ( arrayEnumerator.hasMoreElements() ) {
                final T theObject = arrayEnumerator.nextElement();

                if ( theObject != NSKeyValueCoding.NullValue ) {
                    final Object theValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(theObject, keyPath);
                    final boolean theValueIsNull = theValue == null || theValue == NSKeyValueCoding.NullValue;

                    if ( (theValueIsNull && valueToLookForIsNull) || ObjectUtils.equals(valueToLookFor, theValue) )
                        a.addObject(theObject);
                }
            }

            result = a.immutableClone();
        }

        return result != null ? result : NSArray.EmptyArray;
    }
    /**
     * Locates an object within an array using a custom equality check provided as an ERXEqualator.  This
     * is useful if you have an array of EOs and want to find a particular EO in it without regard to editing
     * contexts.
     * @param array the array to search.
     * @param object the object to look for.
     * @param equalator the equalator to use for performing the equality check between object and each object
     *        in the array.
     * @return index of first occuring object in the array that is defined as equal by the equalator. -1
     *         if no such object is found.
     */
    public static <T> int indexOfObjectUsingEqualator(NSArray<T> array, T object, ERXEqualator equalator) {
        final int count = array.count();
        int result = -1;
        int i = 0;
        
        while ( i < count && result == -1 ) {
            final Object currentObject = array.objectAtIndex(i);
            
            if ( equalator.objectIsEqualToObject(currentObject, object) )
                result = i;
            
            i++;
        }
        
        return result;
    }

    /**
     * Sorts a given array with a key in ascending fashion and returns a mutable clone of the result.
     * @param array array to be sorted.
     * @param key sort key.
     * @return mutable clone of sorted array.
     */
    // CHECKME ak: I probably wrote this, but do we really need it?
    public static <T> NSMutableArray<T> sortedMutableArraySortedWithKey(NSArray<T> array, String key) {
        return sortedArraySortedWithKey(array, key).mutableClone();
    }

    /**
     * Sorts a given array with a key in ascending fashion.
     * @param array array to be sorted.
     * @param key sort key.
     * @return mutable clone of sorted array.
     */
    public static <T> NSArray<T> sortedArraySortedWithKey(NSArray<T> array, String key) {
        return sortedArraySortedWithKey(array, key, null);
    }

    /**
     * Sorts a given array with a key in ascending fashion.
     * @param array array to be sorted.
     * @param key sort key.
     * @param selector sort order selector to use, if null, then sort will be case insensitive ascending.
     * @return sorted array.
     */
    public static <T> NSArray<T> sortedArraySortedWithKey(NSArray<T> array, String key, NSSelector selector) {
        ERXAssert.PRE.notNull("Attempting to sort null array of objects.", array);
        ERXAssert.PRE.notNull("Attepting to sort array of objects with null key.", key);
        NSArray<EOSortOrdering> order=new NSArray<EOSortOrdering>(new EOSortOrdering[] {EOSortOrdering.sortOrderingWithKey(key, selector == null ? EOSortOrdering.CompareCaseInsensitiveAscending : selector)});
        return EOSortOrdering.sortedArrayUsingKeyOrderArray(array, order);
    }

    /**
       * Sorts a given array with a set of keys according to the given selector.
     * @param array array to be sorted.
     * @param keys sort keys
     * @param selector sort order selector to use, if null, then sort will be case insensitive ascending.
     * @return sorted array.
     */
    public static <T> NSArray<T> sortedArraySortedWithKeys(NSArray<T> array, NSArray<String> keys, NSSelector selector) {
        ERXAssert.PRE.notNull("Attempting to sort null array of objects.", array);
        ERXAssert.PRE.notNull("Attepting to sort an array with null keys.", keys);
        if (keys.count() < 2)
            return sortedArraySortedWithKey(array, keys.lastObject(), selector == null ? EOSortOrdering.CompareCaseInsensitiveAscending : selector);

        NSMutableArray<EOSortOrdering> order = new NSMutableArray<EOSortOrdering>(keys.count());
        for (Enumeration<String> keyEnumerator = keys.objectEnumerator(); keyEnumerator.hasMoreElements();) {
            String key = keyEnumerator.nextElement();
            order.addObject(EOSortOrdering.sortOrderingWithKey(key, selector == null ? EOSortOrdering.CompareCaseInsensitiveAscending : selector));
        }
        return EOSortOrdering.sortedArrayUsingKeyOrderArray(array, order);
    }   
    
    /**
     * Sorts a given mutable array with a key in place.
     * @param array array to be sorted.
     * @param key sort key.
     */
    public static void sortArrayWithKey(NSMutableArray<?> array, String key) {
        sortArrayWithKey(array, key, null);
    }

    /**
     * Sorts a given mutable array with a key in place.
     * @param array array to be sorted.
     * @param key sort key.
     * @param selector sort order selector to use, if null, then sort will be ascending.
     */
    public static void sortArrayWithKey(NSMutableArray<?> array, String key, NSSelector selector) {
        ERXAssert.PRE.notNull("Attempting to sort null array of eos.", array);
        ERXAssert.PRE.notNull("Attempting to sort array of eos with null key.", key);
        NSArray<EOSortOrdering> order=new NSArray<EOSortOrdering>(new EOSortOrdering[] {EOSortOrdering.sortOrderingWithKey(key, selector == null ? EOSortOrdering.CompareCaseInsensitiveAscending : selector)});
        EOSortOrdering.sortArrayUsingKeyOrderArray(array, order);
    }

    /**
     * The BaseOperator is Wonder's core class of 
     * {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator}. 
     * This class adds support for chaining multiple array operators in a single 
     * keypath via its 
     * {@link er.extensions.foundation.ERXArrayUtilities.BaseOperator#contents(NSArray, String) contents} 
     * method.<br/>
     */
    static abstract class BaseOperator implements NSArray.Operator {
    	
    	/**
    	 * Rather than iterating through the array argument calling
    	 * {@link com.webobjects.foundation.NSKeyValueCodingAdditions.Utility#valueForKeyPath(Object, String) valueForKeyPath}
    	 * on each array object, this method operates by calling
    	 * {@link com.webobjects.foundation.NSArray#valueForKeyPath(String) valueForKeyPath}
    	 * on the array argument instead.  This method is used by Wonder operators to chain 
    	 * multiple array operators in a single key path.
    	 * 
    	 * @param array the array value for the operator
    	 * @param keypath the keypath to call on the array argument 
    	 * @return the object value produced by valueForKeyPath, or the array itself
    	 * if the keypath is empty
    	 */
        public Object contents(NSArray<?> array, String keypath) {
            if(array != null && array.count() > 0  && keypath != null && keypath.length() > 0) {
                return NSKeyValueCodingAdditions.Utility.valueForKeyPath(array, keypath);
            }
            return array;
        }
    }

    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} 
     * for the key <b>sort</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <ol>
     * <li><code>myArray.valueForKey("@sort.firstName");</code></li>
     * <li><code>myArray.valueForKey("@sort.lastName,firstName.length");</code></li>
     * </ol>
     * Which in the first case would return myArray sorted ascending by first name 
     * and the second case by lastName and then by the length() of the firstName. Due
     * to the way the sort key arguments are written, this key cannot occur anywhere
     * except at the very end of the keypath.
     */
    public static class SortOperator implements NSArray.Operator {
        private NSSelector selector;
        
        public SortOperator(NSSelector selector) {
            this.selector = selector;
        }

        /**
         * Sorts the given array by the keypath.
         * @param array array to be sorted.
         * @param keypath sort key.
         * @return immutable sorted array.
         */
        public Object compute(NSArray array, String keypath) {
            if (array.count() < 2)
                return array;
            if (keypath != null && keypath.indexOf(",") != -1) {
                return sortedArraySortedWithKeys(array,
                        NSArray.componentsSeparatedByString(keypath, ","),
                        selector);
            } 
            return sortedArraySortedWithKey(array, keypath, selector);

        }
    }

    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} 
     * for the key <b>fetchSpec</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@fetchSpec.fetchUsers");</code><br/>
     * <br/>
     * Which in this case would return myArray filtered and sorted by the
     * EOFetchSpecification named "fetchUsers" which must be a model-based fetchspec 
     * in the first object's entity.
     * 
     * @see BaseOperator
     */
    public static class FetchSpecOperator extends BaseOperator {
        public FetchSpecOperator() {
        }
        
        /**
         * Filters and sorts the given array by the named fetchspecification.
         * @param array array to be filtered.
         * @param keypath name of fetch specification.
         * @return immutable filtered array.
         */
        public Object compute(NSArray array, String keypath) {
            if(array.count() == 0) {
                return array;
            }
            EOEnterpriseObject eo = (EOEnterpriseObject)array.objectAtIndex(0);
            String fetchSpec = ERXStringUtilities.firstPropertyKeyInKeyPath(keypath);
            keypath = ERXStringUtilities.keyPathWithoutFirstProperty(keypath);
            if(keypath == null) {
                return filteredArrayWithEntityFetchSpecification(array, eo.entityName(), fetchSpec);
            }
			array = filteredArrayWithEntityFetchSpecification(array, eo.entityName(), fetchSpec);
			return contents(array, keypath);
        }
    }

    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} 
     * for the key <b>flatten</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@flatten.someOtherPath");</code><br/>
     * <br/>
     * Which in this case would return myArray flattened if myArray is an NSArray 
     * of NSArrays (of NSArrays etc) before continuing to process someOtherPath.
     * 
     * @see BaseOperator
     */
    public static class FlattenOperator extends BaseOperator {
        public FlattenOperator() {
        }

        /**
        * Flattens the given array.
         * @param array array to be flattened.
         * @param keypath additional keypath
         * @return value following keypath for flattened array
         */
        public Object compute(NSArray array, String keypath) {
            array = flatten(array);
            return contents(array, keypath);
        }
    }

    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} 
     * for the key <b>isEmpty</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@isEmpty");</code><br/>
     * <br/>
     * Which in this case would return {@link java.lang.Boolean#TRUE true} if the
     * myArray.count() == 0, or {@link java.lang.Boolean#FALSE false} if it is not.
     * This operator always ends computation.  Any keypath following the isEmpty
     * operator is simply ignored.
     * 
     */
    public static class IsEmptyOperator implements NSArray.Operator {
        public IsEmptyOperator() {
        }

        /**
        * returns true if the given array is empty, usefull for WOHyperlink disabled binding.
         * @param array array to be checked.
         * @param keypath the keypath. This value is ignored.
         * @return <code>Boolean.TRUE</code> if array is empty, <code>Boolean.FALSE</code> otherwise.
         */
        public Object compute(NSArray array, String keypath) {
            return array.count() == 0 ? Boolean.TRUE : Boolean.FALSE;
        }
    }


    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} 
     * for the key <b>subarrayWithRange</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKeyPath("@subarrayWithRange.20-3.someOtherPath");</code><br/>
     * <br/>
     * Which in this case would return the three objects from <code>myArray</code>, starting 
     * at the index of 20, before continuing to process <code>someOtherPath</code>.
     * <br/><br/>
     * Note that the syntax for the range argument is <b>not</b> startIndex-endIndex. The API 
     * matches that of NSRange.  You must provide a start index and an array length.
     *  
     * @see BaseOperator
     */
    public static class SubarrayWithRangeOperator extends BaseOperator {
        public SubarrayWithRangeOperator() {
        }

        /**
         * @param array array to truncate
         * @param keypath the key path to follow after truncation
         * @return the value produced by the keypath after truncating the array
         */
        public Object compute(NSArray array, String keypath) {
        	if(ERXStringUtilities.stringIsNullOrEmpty(keypath)) {
        		throw new IllegalArgumentException("subarrayWithRange must be used " +
        				"like '@subarrayWithRange.start-length'");
        	}
        	
        	String rangeString = ERXStringUtilities.firstPropertyKeyInKeyPath(keypath);
        	keypath = ERXStringUtilities.keyPathWithoutFirstProperty(keypath);
        	
        	int index = rangeString.indexOf('-');
        	if(index < 1 || index >= rangeString.length()) {
        		throw new IllegalArgumentException("subarrayWithRange must be used " +
        				"like '@subarrayWithRange.start-length' current key path: " +
        				"\"@subarrayWithRange." + rangeString + "\"");
        	}
        	
        	int start = Integer.valueOf(rangeString.substring(0, index));
        	int length = Integer.valueOf(rangeString.substring(++index));
        	array = array.subarrayWithRange(new NSRange(start, length));
        	return contents(array, keypath);
        }
    }


    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} 
     * for the key <b>limit</b>, which is similar to subarrayWithRange except it is 
     * always from 0 to the limit value.  If the limit specified is larger than the 
     * size of the array, the entire array will be returned.
     * 
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKeyPath("@limit.10.someOtherPath");</code><br/>
     * <br/>
     * Which in this case would return the first 10 objects in <code>myArray</code> 
     * before continuing to process <code>someOtherPath</code>.
     * 
     * @see BaseOperator
     */
    public static class LimitOperator extends BaseOperator {
        public LimitOperator() {
        }

        /**
         * Computes the subarray of the given array.
         * 
         * @param array array to be truncated.
         * @param keypath the key path to follow after truncation.
         * @return the value produced by following the keypath after truncation.
         */
        public Object compute(NSArray array, String keypath) {
            int dotIndex = keypath.indexOf(".");
            String limitStr;
            String rest;
            if (dotIndex == -1) {
            	limitStr = keypath;
            	rest = null;
            }
            else {
            	limitStr = keypath.substring(0, dotIndex);
            	rest = keypath.substring(dotIndex + 1);
            }
            int length = limitStr.length() == 0 ? 0 : Integer.parseInt(limitStr);
            length = Math.min(length, array.count());
            NSArray<?> objects = array.subarrayWithRange(new NSRange(0, length));
            return contents(objects, rest);
        }
    }


    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} 
     * for the key <b>unique</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKeyPath("@unique.someOtherPath");</code><br/>
     * <br/>
     * Which in this case would return only those objects which are unique in myArray 
     * before continuing to process someOtherPath.
     * 
     * @see BaseOperator
     */
    public static class UniqueOperator extends BaseOperator {
        public UniqueOperator() {
        }

        /**
         * Removes duplicates.
         * 
         * @param array
         *            array to be uniqued.
         * @param keypath
         *            the key path after removing duplicates from the array
         * @return the value produced by following the keypath after removing duplicates
         */
        public Object compute(NSArray array, String keypath) {
            if (array != null) array = arrayWithoutDuplicates(array);
            return contents(array, keypath);
        }
    }


    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} 
     * for the key <b>removeNullValues</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKeyPath("@removeNullValues.someOtherPath");</code><br/>
     * <br/>
     * Which in this case would remove the occurrences of NSKeyValueCoding.Null from myArray
     * before continuing to process someOtherPath.
     * 
     * @see BaseOperator
     */
    public static class RemoveNullValuesOperator extends BaseOperator {
        public RemoveNullValuesOperator() {
        }

        /**
         * Removes null values from the given array.
         * 
         * @param array
         *            array to be filtered.
         * @param keypath
         *            the key path to follow after filtering
         * @return the value produced by following keypath after filtering nulls from the array
         */
        public Object compute(NSArray array, String keypath) {
        	array = removeNullValues(array);
            return contents(array, keypath);
        }
    }

    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} for the key <b>objectAtIndex</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@objectAtIndex.3.firstName");</code><br/>
     * <br/>
     *
     */
    public static class ObjectAtIndexOperator implements NSArray.Operator {
        public ObjectAtIndexOperator() {
        }

        /**
         * returns the keypath value for n-ths object.
         * @param array array to be checked.
         * @param keypath integer value of index (zero based).
         * @return <code>null</code> if array is empty or value is not in index, <code>keypath</code> value for the object at index otherwise.
         */
        public Object compute(NSArray array, String keypath) {
            int end = keypath.indexOf(".");
            int index = Integer.parseInt(keypath.substring(0, end == -1 ? keypath.length() : end));
            Object value = null;
            if(index < array.count() )
                value = array.objectAtIndex(index);
            if(end != -1 && value != null) {
                value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(value, keypath.substring(end+1));
            }
            return value;
        }
    }

    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} 
     * for the key <b>avgNonNull</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <ul>
     * <li><code>myArray.valueForKey("@avgNonNull.payment.amount");</code></li>
     * <li><code>myArray.valueForKey("payment.@avgNonNull.amount");</code></li>
     * <li><code>myArray.valueForKey("payment.amount.@avgNonNull");</code></li>
     * </ul>
     * which will sum up all values for the key amount and divide by the number 
     * of nun-null entries. @avgNonNull applies to the array of objects to its 
     * left if it is the last key in the path.  Otherwise it applies to the end 
     * of the keypath to its right.  It should not be followed by an array or 
     * any other array operators.  This is because it does not call 
     * {@link com.webobjects.foundation.NSArray#valueForKeyPath(String) valueForKeyPath} on 
     * the array to its left, but instead loops through the values of the array 
     * to its left, calling 
     * {@link com.webobjects.foundation.NSKeyValueCodingAdditions.Utility#valueForKeyPath(Object, String) valueForKeyPath}
     * on the individual array values instead. This behavior is consistent with 
     * Apple's standard NSArray operators.
     */
    public static class AvgNonNullOperator implements NSArray.Operator {
        public AvgNonNullOperator() {
        }

        /**
         * returns the average value for over all non-null values.
         * @param array array to be checked.
         * @param keypath path to numeric values
         * @return computed average as BigDecimal or <code>NULL</code>.
         */
        public Object compute(NSArray array, String keypath) {
            BigDecimal sum = new BigDecimal(0L);
            int count = 0;
            Object obj, tmp;
            BigDecimal val;
            final boolean noKeypath = keypath == null || keypath.length() <= 0;
            
            for(Enumeration<?> e = array.objectEnumerator(); e.hasMoreElements();) {
            	tmp = e.nextElement();
            	obj = noKeypath?tmp:NSKeyValueCodingAdditions.Utility.valueForKeyPath(tmp, keypath);
                if(!ERXValueUtilities.isNull(obj)) {
                    count += 1;
                    val = ERXValueUtilities.bigDecimalValue(obj);
                    sum = sum.add(val);
                }
            }
            if(count == 0) {
                return null;
            }
            return sum.divide(BigDecimal.valueOf(count), sum.scale() + 4, BigDecimal.ROUND_HALF_EVEN);
        }
    }

    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} 
     * for the key <b>reverse</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@reverse.someOtherPath");</code><br/>
     * <br/>
     * which would reverse the order of the array myArray before continuing to
     * process someOtherPath.
     * 
     * @see BaseOperator
     */
    public static class ReverseOperator extends BaseOperator {
        public ReverseOperator() {
        }

        /**
         * returns the reverse value for the values of the keypath.
         * @param array array to be checked.
         * @param keypath additional keypath
         * @return value produced following keypath after array is reversed
         */
        public Object compute(NSArray array, String keypath) {
            array = reverse(array);
            return contents(array, keypath);
        }
    }
    
    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} 
     * for the key <b>median</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <ul>
     * <li><code>myArray.valueForKey("@median.payment.amount");</code></li>
     * <li><code>myArray.valueForKey("payment.@median.amount");</code></li>
     * <li><code>myArray.valueForKey("payment.amount.@median");</code></li>
     * </ul>
     * which return the median of the array elements at the given key path. The 
     * median is the value for which half of the elements are above and half the 
     * elements are below. As such, an array sort is needed and this might be 
     * very costly depending of the size of the array. 
     * <br/><br/>
     * The @median operator applies to the array of objects to its 
     * left if it is the last key in the path.  Otherwise it applies to the end 
     * of the keypath to its right.  It should not be followed by an array or 
     * any other array operators.  This is because it does not call 
     * {@link com.webobjects.foundation.NSArray#valueForKeyPath(String) valueForKeyPath} on 
     * the array to its left, but instead loops through the values of the array 
     * to its left, calling 
     * {@link com.webobjects.foundation.NSKeyValueCodingAdditions.Utility#valueForKeyPath(Object, String) valueForKeyPath}
     * on the individual array values instead. This behavior is consistent with 
     * Apple's standard NSArray operators.
     */
    public static class MedianOperator implements NSArray.Operator {
        public MedianOperator() {
        }

        /**
         * returns the median value for the values of the keypath.
         * @param array array to be checked.
         * @param keypath path to numeric values
         * @return median value
         */
        public Object compute(NSArray array, String keypath) {
            return median(array, keypath);
        }
    }
    
    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} 
     * for the key <b>stdDev</b> and <b>popStdDev</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <ul>
     * <li><code>myArray.valueForKey("@stdDev.payment.amount");</code></li>
     * <li><code>myArray.valueForKey("payment.@stdDev.amount");</code></li>
     * <li><code>myArray.valueForKey("payment.amount.@stdDev");</code></li>
     * </ul>
     * All three of these examples will return the same value, which in this case 
     * is the standard deviation of the amounts. The standard deviation is a 
     * measure of the dispersion of a sample of numbers. The population standard 
     * deviation is used if you have the values for an entire population.
     * <br/><br/>
     * The standard deviation operator applies to the array of objects to its 
     * left if it is the last key in the path.  Otherwise it applies to the end 
     * of the keypath to its right.  It should not be followed by an array or 
     * any other array operators.  This is because it does not call 
     * {@link com.webobjects.foundation.NSArray#valueForKeyPath(String) valueForKeyPath} on 
     * the array to its left, but instead loops through the values of the array 
     * to its left, calling 
     * {@link com.webobjects.foundation.NSKeyValueCodingAdditions.Utility#valueForKeyPath(Object, String) valueForKeyPath}
     * on the individual array values instead. This behavior is consistent with 
     * Apple's standard NSArray operators.
     */
    public static class StandardDeviationOperator implements NSArray.Operator {
    	private boolean isPop;
    	
    	public StandardDeviationOperator(boolean isPopulation) {
    		isPop = isPopulation;
    	}
    	
        /**
         * returns the standard deviation value for the values of the keypath.
         * @param array array to be checked.
         * @param keypath path to numeric values
         * @return standard deviation value
         */
    	public Object compute(NSArray array, String keypath) {
    		return stdDev(array, keypath, isPop);
    	}
    }
    
    /** 
     * Will register new NSArray operators
     * <b>sort</b>, <b>sortAsc</b>, <b>sortDesc</b>, <b>sortInsensitiveAsc</b>,
     * <b>sortInsensitiveDesc</b>, <b>unique</b>, <b>flatten</b>, <b>reverse</b>, 
     * <b>limit</b>, and <b>fetchSpec</b> 
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        if (ERXProperties.booleanForKeyWithDefault("er.extensions.ERXArrayUtilities.ShouldRegisterOperators", true)) {
            NSArray.setOperatorForKey("sort", new SortOperator(EOSortOrdering.CompareAscending));
            NSArray.setOperatorForKey("sortAsc", new SortOperator(EOSortOrdering.CompareAscending));
            NSArray.setOperatorForKey("sortDesc", new SortOperator(EOSortOrdering.CompareDescending));
            NSArray.setOperatorForKey("sortInsensitiveAsc", new SortOperator(EOSortOrdering.CompareCaseInsensitiveAscending));
            NSArray.setOperatorForKey("sortInsensitiveDesc", new SortOperator(EOSortOrdering.CompareCaseInsensitiveDescending));
            NSArray.setOperatorForKey("flatten", new FlattenOperator());
            NSArray.setOperatorForKey("fetchSpec", new FetchSpecOperator());
            NSArray.setOperatorForKey("unique", new UniqueOperator());
            NSArray.setOperatorForKey("isEmpty", new IsEmptyOperator());
            NSArray.setOperatorForKey("subarrayWithRange", new SubarrayWithRangeOperator());
            NSArray.setOperatorForKey("objectAtIndex", new ObjectAtIndexOperator());
            NSArray.setOperatorForKey("avgNonNull", new AvgNonNullOperator());
            NSArray.setOperatorForKey("reverse", new ReverseOperator());
            NSArray.setOperatorForKey("removeNullValues", new RemoveNullValuesOperator());
            NSArray.setOperatorForKey("median", new MedianOperator());
            NSArray.setOperatorForKey("limit", new LimitOperator());
            NSArray.setOperatorForKey("stdDev", new StandardDeviationOperator(false));
            NSArray.setOperatorForKey("popStdDev", new StandardDeviationOperator(true));
        }
    }
    
    
    /**
     * Calculates the median value of an array.
     * The median is the value for which half of the elements are above and half the elements are below.
     * As such, an array sort is needed and this might be very costly depending of the size of the array.
     * @param array array of objects
     * @param keypath key path for the median
     * 
     * @return the median value
     */
    public static Number median(NSArray<?> array, String keypath) {
        final int count = array.count();
        final boolean noKeypath = keypath == null || keypath.length() <= 0;
        Object obj, tmp;
        Number value;
        
        if(count == 0) {
            value = null;
        
        } else if(count == 1) {
            obj = noKeypath?array.objectAtIndex(0):array.valueForKeyPath(keypath);
            value = ERXValueUtilities.bigDecimalValue(obj);
        
        } else {
        	//Sort the array
        	NSArray sortedArray;
        	if(noKeypath) {
        		NSMutableArray sortlist = array.mutableClone();
        		Collections.sort(sortlist);
        		sortedArray = sortlist;
        	} else {
        		sortedArray = sortedArraySortedWithKey(array, keypath);
        	}
        	
        	//Find the midpoint
            int mid = count / 2;
            obj = sortedArray.objectAtIndex(mid);
            
            //If the count is even, average the two midpoints
            if(count % 2 == 0) {
            	tmp = noKeypath?obj:NSKeyValueCodingAdditions.Utility.valueForKeyPath(obj, keypath);
            	BigDecimal a = ERXValueUtilities.bigDecimalValue(tmp);
            	obj = sortedArray.objectAtIndex(mid - 1);
            	tmp = noKeypath?obj:NSKeyValueCodingAdditions.Utility.valueForKeyPath(obj, keypath);
            	BigDecimal b = ERXValueUtilities.bigDecimalValue(tmp);
            	BigDecimal sum = a.add(b);
            	value = sum.divide(BigDecimal.valueOf(2), sum.scale() + 4, BigDecimal.ROUND_HALF_EVEN);
            	
            } else {
            	tmp = noKeypath?obj:NSKeyValueCodingAdditions.Utility.valueForKeyPath(obj, keypath);
            	value = ERXValueUtilities.bigDecimalValue(tmp);
            }
        }
        return value;
    }
        
    /**
     * Finds the standard deviation of the numeric values found in the array at the
     * specified keypath. If the keypath is null or empty, then the array values are
     * used instead. If the array has fewer than two objects, null is returned. If
     * isPopulation is true, the population standard deviation is calculated. If
     * isPopulation is false, the sample standard deviation is calculated. Use a 
     * true value for isPopulation if you know the values for an entire population 
     * and false if you are dealing with a sample.
     * 
     * @param array an array of objects
     * @param keypath a key path to a numeric value on each object
     * @param isPopulation 
     * @return the standard deviation for the numeric values
     */
    public static Number stdDev(NSArray<?> array, String keypath, boolean isPopulation) {
    	final int count = array.count();
    	if(count < 2) {return null;}
    	final boolean noKeypath = keypath == null || keypath.length() <= 0;
    	Object val = noKeypath?array.valueForKey("@avg"):array.valueForKeyPath(keypath + ".@avg");
    	BigDecimal mean = ERXValueUtilities.bigDecimalValue(val);
    	BigDecimal sum = BigDecimal.valueOf(0);
    	BigDecimal divisor = BigDecimal.valueOf(isPopulation?count:count-1);
    	BigDecimal diff;
    	Object obj;
    	
    	for(Object tmp: array) {
    		obj = noKeypath?tmp:NSKeyValueCodingAdditions.Utility.valueForKeyPath(tmp, keypath);
    		diff = ERXValueUtilities.bigDecimalValue(obj).subtract(mean);
    		diff = diff.multiply(diff);
    		sum = sum.add(diff);
    	}
    	
    	sum = sum.divide(divisor, sum.scale() + 4, BigDecimal.ROUND_HALF_EVEN);
    	return BigDecimal.valueOf(Math.sqrt(sum.doubleValue()));
    }
        
    /**
     * Shorter name for arrayWithoutDuplicates, which I always forget the name of.
     * 
     * @param <T> type of the array
     * @param array the array to return distinct values from
     * @return an array of distinct elements from the input array
     */
    public static <T> NSArray<T> distinct(NSArray<T> array) {
      return arrayWithoutDuplicates(array);
    }
    
    /**
     * Filters out all of the duplicate objects in
     * a given array.<br/> Preserves the order now.
     * @param anArray to be filtered
     * @return filtered array.
     */
    public static <T> NSArray<T> arrayWithoutDuplicates(NSArray<T> anArray) {
        NSMutableArray<T> result = new NSMutableArray<T>();
        NSMutableSet<T> already = new NSMutableSet<T>();
        for(T object : anArray) {
            if(!already.containsObject(object)){
                already.addObject(object);
                result.addObject(object);
            }
        }
        return result;
    }

    /**
     * Batches an NSArray into sub-arrays of the given size.
     * @param array array to batch
     * @param batchSize number of items in each batch
     * @return NSArray of NSArrays, each with at most batchSize items
     */
    public static <T> NSArray<NSArray<T>> batchedArrayWithSize(NSArray<T> array, int batchSize) {
        if(array == null || array.count() == 0)
            return NSArray.EmptyArray;

        NSMutableArray<NSArray<T>> batchedArray = new NSMutableArray<NSArray<T>>();
        int count = array.count();

        for(int i = 0; i < count; i+=batchSize) {
            int length = batchSize;
            if(i + length > count)
                length = count - i;
            batchedArray.addObject(array.subarrayWithRange(new NSRange(i, length)));
        }
        return batchedArray;
    }

    /**
     * Filters a given array with a named fetch specification and bindings.
     *
     * @param array array to be filtered.
     * @param fetchSpec name of the {@link com.webobjects.eocontrol.EOQualifierEvaluation EOQualifierEvaluation}.
     * @param entity name of the {@link com.webobjects.eoaccess.EOEntity EOEntity} 
     * to which the fetch specification is associated.
     * @param bindings bindings dictionary for qualifier variable substitution.
     * @return array filtered and sorted by the named fetch specification.
     */    
    public static <T> NSArray<T> filteredArrayWithEntityFetchSpecification(NSArray<T> array, String entity, String fetchSpec, NSDictionary<String, ?> bindings) {
        EOEntity wrongParamEntity = EOModelGroup.defaultGroup().entityNamed(fetchSpec);
        if (wrongParamEntity != null) {
        	fetchSpec = entity;
			entity = wrongParamEntity.name();
			log.error("filteredArrayWithEntityFetchSpecification Calling conventions have changed from fetchSpec, entity to entity, fetchSpec");
		}
        EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed(fetchSpec, entity);
        NSArray<EOSortOrdering> sortOrderings;
        NSArray<T> result;
        EOQualifier qualifier;

        if (bindings != null) {
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);
        }

        result = new NSArray<T>(array);

        if ((qualifier = spec.qualifier()) != null) {
            result = EOQualifier.filteredArrayWithQualifier(result, qualifier);
        }

        if ((sortOrderings = spec.sortOrderings()) != null) {
            result = EOSortOrdering.sortedArrayUsingKeyOrderArray(result,sortOrderings);
        }

        return result;
    }
    
    /**
     * @deprecated use {@link #filteredArrayWithEntityFetchSpecification(NSArray, String, String, NSDictionary)}
     */
    @Deprecated
    public static <T> NSArray<T> filteredArrayWithFetchSpecificationNamedEntityNamedBindings(NSArray<T> array, String fetchSpec, String entity, NSDictionary<String, ?> bindings) {
        return filteredArrayWithEntityFetchSpecification( array, entity, fetchSpec, bindings);
    }

    /**
     * @deprecated use {@link #filteredArrayWithEntityFetchSpecification(NSArray, String, String, NSDictionary)}
     */
    @Deprecated
    public static <T> NSArray<T> filteredArrayWithFetchSpecificationNamedEntityNamed(NSArray<T> array, String fetchSpec, String entity) {
        return filteredArrayWithEntityFetchSpecification(array, entity, fetchSpec, null);
    }

    /**
     * Filters a given array with a named fetch specification.
     *
     * @param array array to be filtered.
     * @param fetchSpec name of the {@link com.webobjects.eocontrol.EOQualifierEvaluation EOQualifierEvaluation}.
     * @param entity name of the {@link com.webobjects.eoaccess.EOEntity EOEntity} 
     * to which the fetch specification is associated.
     * @return array filtered and sorted by the named fetch specification.
     */
    public static <T> NSArray<T> filteredArrayWithEntityFetchSpecification(NSArray<T> array, String entity, String fetchSpec) {
        return filteredArrayWithEntityFetchSpecification(array, entity,  fetchSpec, null);
    }
    
    /**
     * shifts a given object in an array one value to the left (index--).
     *
     * @param array array to be modified.
     * @param object the object that should be moved
     */
    public static <T> void shiftObjectLeft(NSMutableArray<T> array, T object) {
        int index = array.indexOfObject(object);
        if (index == -1) return;
        if (index > 0) {
            array.insertObjectAtIndex(object, index -1);
            array.removeObjectAtIndex(index + 1);
        }
    }

    /**
     * shifts a given object in an array one value to the right (index++).
     *
     * @param array array to be modified.
     * @param object the object that should be moved
     */
    public static <T> void shiftObjectRight(NSMutableArray<T> array, T object) {
        int index = array.indexOfObject(object);
        if (index == -1) return;
        if (index < array.count() - 1) {
            array.insertObjectAtIndex(object, index + 2);
            array.removeObjectAtIndex(index);
        }
    }

    /**
     * Function to determine if an array contains any of
     * the elements of another array.
     * @param array to test if it contains any of the objects
     * @param objects array of objects to test if the first array
     *		contains any of
     * @return if the first array contains any elements from the second
     *		array
     */
    public static <T> boolean arrayContainsAnyObjectFromArray(NSArray<? extends T> array, NSArray<? extends T> objects) {
        boolean arrayContainsAnyObject = false;
        if (array != null && objects != null && array.count() > 0 && objects.count() > 0) {
            for (Enumeration<? extends T> e = objects.objectEnumerator(); e.hasMoreElements();) {
                if (array.containsObject(e.nextElement())) {
                    arrayContainsAnyObject = true; 
                    break;
                }
            }
        }
        return arrayContainsAnyObject;
    }

    /**
     * Function to determine if an array contains all of
     * the elements of another array.
     * @param array to test if it contains all of the objects of another array
     * @param objects array of objects to test if the first array
     *		contains all of
     * @return if the first array contains all of the elements from the second
     *		array
     */
    public static <T> boolean arrayContainsArray(NSArray<? extends T> array, NSArray<? extends T> objects) {
        boolean arrayContainsAllObjects = true;
        if (array != null && objects != null && array.count() > 0 && objects.count() > 0) {
            for (Enumeration<? extends T> e = objects.objectEnumerator(); e.hasMoreElements();) {
                if (!array.containsObject(e.nextElement())) {
                    arrayContainsAllObjects = false; break;
                }
            }
        } else if (array == null || array.count() == 0) {
            return false;
        }
        return arrayContainsAllObjects;        
    }

    /**
     * Intersects the elements of two arrays. This has the effect of
     * stripping out duplicates.
     * @param array1 the first array
     * @param array2 the second array
     * @return the intersecting elements
     */
    public static <T> NSArray<T> intersectingElements(NSArray<? extends T> array1, NSArray<? extends T> array2) {
        NSMutableArray<T> intersectingElements = null;
        if (array1 != null && array1.count() > 0 && array2 != null && array2.count() > 0) {
            intersectingElements = new NSMutableArray<T>();
            NSArray<? extends T> bigger = array1.count() > array2.count() ? array1 : array2;
            NSArray<? extends T> smaller = array1.count() > array2.count() ? array2 : array1;
            for (Enumeration<? extends T> e = smaller.objectEnumerator(); e.hasMoreElements();) {
                T object = e.nextElement();
                if (bigger.containsObject(object) && !intersectingElements.containsObject(object))
                    intersectingElements.addObject(object);
            }
        }
        return intersectingElements != null ? intersectingElements : NSArray.EmptyArray;
    }

    /**
     * Reverses the elements of an array
     * @param array to be reversed
     * @return reverse ordered array
     */
    public static <T> NSArray<T> reverse(NSArray<T> array) {
        NSArray<T> reverse = null;
        if (array != null && array.count() > 0) {
            NSMutableArray<T> reverseTemp = new NSMutableArray<T>(array.count());
            for (Enumeration<T> reverseEnumerator = array.reverseObjectEnumerator(); reverseEnumerator.hasMoreElements();) {
                reverseTemp.addObject(reverseEnumerator.nextElement());
            }
            reverse = reverseTemp.immutableClone();
        }
        return reverse != null ? reverse : NSArray.EmptyArray;
    }

    /**
     * Displays a list of attributes off of
     * objects in a 'friendly' manner. <br/>
     * <br/>
     * For example, given an array containing three user
     * objects and the attribute key "firstName", the
     * result of calling this method would be the string:
     * "Max, Anjo and Patrice".
     * @param list of objects to be displayed in a friendly
     *		manner
     * @param attribute key to be called on each object in
     *		the list
     * @param nullArrayDisplay string to be returned if the
     *		list is null or empty
     * @param separator string to be used for the first items
     * @param finalSeparator used between the last items
     * @return friendly display string
     */
    @SuppressWarnings("null")
	public static String friendlyDisplayForKeyPath(NSArray<?> list, String attribute, String nullArrayDisplay, String separator, String finalSeparator) {
        Object result = null;
        int count = list!=null ? list.count() : 0;
        if (count==0) {
            result=nullArrayDisplay;
        } else if (count == 1) {
            result= (attribute!= null ? NSKeyValueCodingAdditions.Utility.valueForKeyPath(list.objectAtIndex(0), attribute) : list.objectAtIndex(0));
        } else if (count > 1) {
            StringBuilder buffer = new StringBuilder();
            for(int i = 0; i < count; i++) {
                Object attributeValue = (attribute!= null ? NSKeyValueCodingAdditions.Utility.valueForKeyPath(list.objectAtIndex(i), attribute) : list.objectAtIndex(i));
                if (i>0) buffer.append(i == (count - 1) ? finalSeparator : separator);
                buffer.append(attributeValue);
            }
            result=buffer.toString();
        }
        return (result == null ? null : result.toString());
    }

    /**
     * Returns an array of dictionaries containing the key/value pairs for the given paths.
     * @param array array of objects
     * @param keys array of keys
     * @return array of dictionaries containing values for the key paths
     */
    public static NSArray<?> arrayForKeysPath(NSArray<?> array, NSArray<String> keys) {
        NSMutableArray<Object> result=new NSMutableArray<Object>();
        if (array != null && keys != null) {
            for (Enumeration<?> e = array.objectEnumerator(); e.hasMoreElements();) {
                Object object = e.nextElement();
                result.addObject(ERXDictionaryUtilities.dictionaryFromObjectWithKeys(object, keys));
            }
        }
        return result.immutableClone();
    }
    
    /** Removes all occurrences of NSKeyValueCoding.NullValue in the provided array
     * @param array the array from which the NullValue should be removed
     * @return a new NSArray with the same order than the original array but 
     * without NSKeyValueCoding.NullValue objects
     */
    public static <T> NSArray<T> removeNullValues(NSArray<T> array) {
        return removeNullValues(array, array);
    }
    
    /** Removes all occurrences of NSKeyValueCoding.NullValue in the provided array
     * @param target array to remove objects from
     * @param array array of values
     * @return a new NSArray with the same order than the original array but 
     * without NSKeyValueCoding.NullValue objects
     */
    public static <T> NSArray<T> removeNullValues(NSArray<T> target, NSArray<T> array) {
        if (target == null) return null;
        if (array == null) return target;
        NSMutableArray<T> result = new NSMutableArray<T>();
        int i = 0;
        for (T object : array) {
            if (!(object instanceof NSKeyValueCoding.Null)) {
                result.addObject(target.objectAtIndex(i));
            }
            i++;
        }
        return result;
    }
    
    /** Converts an Object array to a String array by casting each element.
     * This is analogous to <code>String[] myStringArray = (String[])myObjectArray;</code> 
     * except that it creates a clone of the array.
     * @param o an Object array containing String elements
     * @return a String array containing the same elements
     */
    public static String[] objectArrayCastToStringArray(Object[] o) {
        String[] s = new String[o.length];
        for (int i = 0; i < o.length; i++) {
            s[i] = (String)o[i];
        }
        return s;
    }
    
    /** pretty prints an Object array which is ugly when using toString
     * @param o the object which one wants to print as a String
     * @return the String which can be used in lets say 
     * <code>log.info("my array = "+ERXArrayUtilities.objectArrayToString(myArray));</code>
     */
    public static String objectArrayToString(Object[] o) {
        return new NSArray<Object>(o).toString();
    }
    
    /** pretty prints a two dimensional Object array which is ugly when using toString
     * @param array the object which one wants to print as a String
     * @return the String which can be used in lets say 
     * <code>log.info("my array = "+ERXArrayUtilities.objectArrayToString(myArray));</code>
     */
    public static String objectArrayToString(Object[][] array) {
        NSMutableArray<Object> result = new NSMutableArray<Object>();
        for (Object[] oa : array) {
            result.addObject(objectArrayToString(oa));
        }
        return result.toString();
    }
    
    /** pretty prints a NSArray of two dimensional Object array which is ugly when using toString
     * @param array the object which one wants to print as a String
     * @return the String which can be used in lets say 
     * <code>log.info("my array = "+ERXArrayUtilities.objectArrayToString(myArray));</code>
     */
    public static String objectArraysToString(NSArray<Object[][]> array) {
        NSMutableArray<Object> aa = new NSMutableArray<Object>();
        for (Object[][] oa : array) {
            aa.addObject(objectArrayToString(oa));
        }
        return aa.toString();
    }

    /** removes all occurencies of NSKeyValueCoding.Null from the end of the array
     * @param array the array from which the values should be removed
     * @return a new NSArray which does not have NSKeyValueCoding.Null instances at the end
     */
    public static <T> NSArray<T> removeNullValuesFromEnd(NSArray<T> array) {
        if (array == null) return null;
        NSMutableArray<T> a = array.mutableClone();
        while (a.lastObject() instanceof NSKeyValueCoding.Null) {
            a.removeLastObject();
        }
        return a;
    }
    
    public static String[] toStringArray(NSArray<?> a) {
        String[] b = new String[a.count()];
        for (int i = a.count(); i-- > 0; b[i] = a.objectAtIndex(i).toString()) {
          // do nothing
        }
        return b;
    }

    /**
     * Calls <code>dictionaryOfObjectsIndexedByKeyPathThrowOnCollision()</code> passing <code>false</code> for throwOnCollision.
     * 
     * @param array array to index
     * @param keyPath keyPath to index. If any object returns <code>null</code> or NSKeyValueCoding.NullValue for this keyPath, the
     *        object is not put into the resulting dictionary.
     * @return a dictionary indexing the given array. If array is <code>null</code>, an empty dictionary is returned.
     * @see #dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(NSArray, String, boolean)
     */
    public static <K, T> NSDictionary<K, T> dictionaryOfObjectsIndexedByKeyPath(final NSArray<T> array, final String keyPath) {
        return dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(array, keyPath, false);
    }

    /**
     * Given an array of objects, returns a dictionary mapping the value by performing valueForKeyPath on each object in
     * the array to the object in the array. This is similar in concept to but different in semantic from
     * <code>arrayGroupedByKeyPath()</code>. That method is focused on multiple objects returning the same value for the keypath
     * and, so, objects are grouped into arrays. That is not particularly useful when there aren't collisions in the array or when
     * you don't care if there are collisions. For example, with a CreditCard EO object, one could rely on the paymentType.name value
     * to be unique and thus you're more interested in being able to rapidly get to the EO. <code>arrayGroupedByKeyPath()</code>
     * would require either flattening out the arrays or navigating to them every time.
     *
     * @param array array to index
     * @param keyPath keyPath to index. If any object returns <code>null</code> or NSKeyValueCoding.NullValue for this keyPath, the
     *        object is not put into the resulting dictionary.
     * @param throwOnCollision if <code>true</code> and two objects in the array have the same non-null (or non-NullValue) value for keyPath,
     *        an exception is thrown. If <code>false</code>, the last object in the array wins.
     * @return a dictionary indexing the given array. If array is <code>null</code>, an empty dictionary is returned.
     */
    public static <K, T> NSDictionary<K, T> dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(final NSArray<T> array, final String keyPath, final boolean throwOnCollision) {
		if (array == null)
			return NSDictionary.emptyDictionary();
    	final NSMutableDictionary<K, T> result = new NSMutableDictionary<K, T>();
        final Enumeration<T> e = array.objectEnumerator();

        while ( e.hasMoreElements() ) {
            final T theObject = e.nextElement();
            final K theKey = (K) NSKeyValueCodingAdditions.Utility.valueForKeyPath(theObject, keyPath);

            if ( theKey != null && theKey != NSKeyValueCoding.NullValue ) {
                final Object existingObject = throwOnCollision ? result.objectForKey(theKey) : null;

                if ( existingObject != null ) {
                    throw new RuntimeException("Collision with value ('" + theKey + "') for keyPath '" + keyPath + "'.  Initial object: '" +
                                               existingObject + ", subsequent object: " + theObject);
                }

                result.setObjectForKey(theObject, theKey);
            }
        }

        return result.immutableClone();
    }

    /**
     * Prunes an array for only instances of the given class.
     *
     * @param array array to process
     * @param aClass class to use.  null results in the result being a copy of the <code>array</code>.
     * @return an array which is a subset of the <code>array</code> where each object in the result is
     *         an instance of <code>aClass</code>.
     */
    public static <T> NSArray<T> arrayBySelectingInstancesOfClass(final NSArray<?> array, final Class<T> aClass) {
        NSArray<T> result = null;

        if ( array != null && array.count() > 0 ) {
            final NSMutableArray<T> a = new NSMutableArray<T>();
            final Enumeration<?> e = array.objectEnumerator();

            while ( e.hasMoreElements() ) {
                final Object theObject = e.nextElement();

                if ( aClass == null || aClass.isInstance(theObject) )
                    a.addObject((T)theObject);
            }

            if ( a.count() > 0 )
                result = a.immutableClone();
        }

        return result != null ? result : NSArray.EmptyArray;
    }

    /**
     * Just like the method {@link com.webobjects.foundation.NSArray#sortedArrayUsingComparator(NSComparator)}, 
     * except it catches the NSComparator.ComparisonException and, if thrown,
     * it wraps it in a runtime exception.  Returns null when passed null for array.
     * @param array 
     * @param comparator 
     * @param <T> 
     * @return the sorted array
     */
    public static  <T> NSArray<T> sortedArrayUsingComparator(final NSArray<T> array, final NSComparator comparator) {
        NSArray<T> result = array;

        if ( array != null ) {
            if ( array.count() < 2 ) {
                result = array;
            }
            else {
                try {
                    result = array.sortedArrayUsingComparator(comparator);
                }
                catch ( NSComparator.ComparisonException e ) {
                    throw new RuntimeException(e);
                }
            }
        }

        return result;
    }
    
    
	/**
	 * Swaps the two given {@link Object}s in the given {@link NSArray} and
	 * returns a new {@link NSArray}. If one of the {@link Object}s is not
	 * element of the {@link NSArray} a {@link RuntimeException} will be thrown.
	 * 
	 * @author edgar - Jan 7, 2008
	 * @param <T> 
	 * @param array
	 *            in that the two given {@link Object}s have to be swapped
	 * @param object1
	 *            one object in the {@link NSArray} that will be swapped
	 * @param object2
	 *            the other object in the {@link NSArray} that will be swapped
	 * 
	 * @return the new {@link NSArray} with the swapped elements
	 * 
	 * @throws {@link RuntimeException}
	 *             if one of the {@link Object}s is not in the {@link NSArray}
	 */
    public static <T> NSArray<T> arrayWithObjectsSwapped(final NSArray<T> array, final Object object1, final Object object2) {
    	int indexOfObject1 = array.indexOf(object1);
    	int indexOfObject2 = array.indexOf(object2);
    	
    	if (indexOfObject1 >= 0 && indexOfObject2 >= 0) {
    		return arrayWithObjectsAtIndexesSwapped(array, indexOfObject1, indexOfObject2);
    	}
    	throw new RuntimeException("At least one of the given objects is not element of the array!");
    }

	/**
	 * Swaps the two objects at the given indexes in the given {@link NSArray} and
	 * returns a new {@link NSArray}.
	 * 
	 * @author edgar - Jan 7, 2008
	 * @param array in that the two {@link Object}s at the given indexes have to be swapped
	 * @param indexOfObject1 index of one object in the {@link NSArray} that will be swapped
	 * @param indexOfObject2 index of the other object in the {@link NSArray} that will be swapped
	 * 
	 * @return the new {@link NSArray} with the swapped elements
	 * 
	 * @throws {@link RuntimeException} if one of the indexes is out of bound
	 */
	public static <T> NSArray<T> arrayWithObjectsAtIndexesSwapped(final NSArray<T> array, final int indexOfObject1, final int indexOfObject2) {
		if (array == null || array.count() < 2) {
			throw new RuntimeException ("Array is either null or does not have enough elements.");
		}
		NSMutableArray<T> tmpArray = array.mutableClone();
		try {
			T tmpObject = array.objectAtIndex(indexOfObject1);
			tmpArray.set(indexOfObject1, array.objectAtIndex(indexOfObject2));
			tmpArray.set(indexOfObject2, tmpObject);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return tmpArray.immutableClone();
	}

	/**
	 * Swaps two objects a and b in an array inplace 
	 * 
	 * @author cug - Jan 7, 2008
	 * 
	 * @param array the array 
	 * @param a - first object
	 * @param b - second object
	 * 
	 * @throws {@link RuntimeException} if one or both indexes are out of bounds
	 */
	public static <T> void swapObjectsInArray (NSMutableArray<T> array, T a, T b) {
		if (array == null || array.count() < 2) {
			throw new RuntimeException ("Array is either null or does not have enough elements.");
		}
		int indexOfA = array.indexOf(a);
		int indexOfB = array.indexOf(b);
		
		if (indexOfA >= 0 && indexOfB >= 0) {
			swapObjectsAtIndexesInArray(array, indexOfA, indexOfB);
		}
		else {
			throw new RuntimeException ("At least one of the objects is not element of the array!");
		}
	}

	/**
	 * Swaps two objects at the given indexes in an array inplace 
	 * 
	 * @author cug - Jan 7, 2008
	 * 
	 * @param array the array 
	 * @param indexOfA - index of the first object
	 * @param indexOfB - index of the second object
	 * 
	 * @throws {@link RuntimeException} if one or both indexes are out of bounds
	 */
	public static <T> void swapObjectsAtIndexesInArray (NSMutableArray<T> array, int indexOfA, int indexOfB) {
		try {
			T tmp = array.replaceObjectAtIndex(array.objectAtIndex(indexOfA), indexOfB);
			array.replaceObjectAtIndex(tmp, indexOfA);
		}
		catch (Exception e) {
			throw new RuntimeException();
		}
	}
	
	/**
	* Swaps the object a with the object at the given index
	*
	* @author edgar - Apr 14, 2008
	* @param array the array
	* @param a - first object
	* @param indexOfB - index of second object
	*/
	public static <T> void swapObjectWithObjectAtIndexInArray(NSMutableArray<T> array, T a, int indexOfB) {
		if (array == null || array.count() < 2) {
			throw new RuntimeException ("Array is either null or does not have enough elements.");
		}
		
		int indexOfA = array.indexOf(a);
		
		if (indexOfA >= 0 && indexOfB >= 0) {
			if (indexOfA != indexOfB) {
				swapObjectsAtIndexesInArray(array, indexOfA, indexOfB);
			}
		}
		else {
			throw new RuntimeException ("At least one of the objects is not element of the array!");
		}
	}

     /**
      * Returns a deep clone of the given array.  A deep clone will attempt 
      * to clone the values of this array as well as the array itself.
      * 
      * @param array the array to clone
      * @param onlyCollections if true, only collections in this array will be cloned, not individual values
      * @return a deep clone of array
      */
	public static <T> NSArray<T> deepClone(NSArray<T> array, boolean onlyCollections) {
		NSMutableArray<T> clonedArray = null;
		if (array != null) {
			clonedArray = array.mutableClone();
			for (int i = array.count() - 1; i >= 0; i --) {
				T value = array.objectAtIndex(i);
				T clonedValue = ERXUtilities.deepClone(value, onlyCollections);
				if (clonedValue != null) {
					if (clonedValue != value) {
						clonedArray.replaceObjectAtIndex(clonedValue, i);
					}
				}
				else {
					clonedArray.removeObjectAtIndex(i);
				}
			}
		}
		return clonedArray;
    }

     /**
      * Returns a deep clone of the given set.  A deep clone will attempt 
      * to clone the values of this set as well as the set itself.
      * 
      * @param set the set to clone
      * @param onlyCollections if true, only collections in this array will be cloned, not individual values
      * @return a deep clone of set
      */
	public static <T> NSSet<T> deepClone(NSSet<T> set, boolean onlyCollections) {
		NSMutableSet<T> clonedSet = null;
		if (set != null) {
			clonedSet = set.mutableClone();
			for (T value : set) {
				T clonedValue = ERXUtilities.deepClone(value, onlyCollections);
				if (clonedValue != null) {
					if (clonedValue != value) {
						clonedSet.removeObject(value);
						clonedSet.addObject(clonedValue);
					}
				}
				else {
					clonedSet.removeObject(value);
				}
			}
		}
		return clonedSet;
    }
	
	/**
	 * <span class="en">
	 * Check if an NSArray is null or Empty
	 * 
	 * @param aNSArray - NSArray
	 * 
	 * @return if an NSArray is null or Empty <code>true</code> returns
	 * </span>
	 * 
	 * <span class="ja">
	 * 	配列が null か空かをチェックします
	 * 
	 * 	@param aNSArray - NSArray
	 * 
	 * 	@return 配列が null か空の場合は <code>true</code> が戻ります
	 * </span>
	 */
	@SuppressWarnings("javadoc")
	public static boolean arrayIsNullOrEmpty(NSArray<?> aNSArray){
		if(aNSArray == null)
			return true;

		if(aNSArray.isEmpty())
			return true;

		return false;
	}

	/**
	 * <span class="en">
	 * 	To create oneLine Log for an NSArray&lt;String&gt;
	 * 
	 * 	@param aNSArray - NSArray
	 * 
	 * 	@return change a NSArray to String
	 * </span>
	 * 
	 * <span class="ja">
	 * 	NSArray 配列をログとして出力する時に複数行に渡らないで、一行で収まるように
	 * 
	 * 	@param aNSArray - 文字列配列
	 * 
	 * 	@return NSArray を String に変換した行
	 * </span>
	 */
	@SuppressWarnings("javadoc")
	public static String arrayToLogstring(NSArray<String> aNSArray){
		String result = "( ";

		for(String obj : aNSArray) {
			result += obj + ", ";
		}
		result = result.substring(0, result.length()-2);
		result += " )";

		return result;
	}

}
