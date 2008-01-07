package er.extensions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

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

/**
 * Collection of {@link com.webobjects.foundation.NSArray NSArray} utilities.
 */
public class ERXArrayUtilities extends Object {
	
	   private static Logger log = Logger.getLogger(ERXArrayUtilities.class);

	   /**
	    * Holds the null grouping key for use when grouping objects
	    * based on a key that might return null and nulls are allowed
	    */
    public static final String NULL_GROUPING_KEY="**** NULL GROUPING KEY ****";

    /** caches if array utilities have been initialized */
    private static boolean initialized = false;

    /** Caches sort orderings for given keys */
    private final static NSDictionary _selectorsByKey=new NSDictionary(new NSSelector [] {
        EOSortOrdering.CompareAscending,
        EOSortOrdering.CompareCaseInsensitiveAscending,
        EOSortOrdering.CompareCaseInsensitiveDescending,
        EOSortOrdering.CompareDescending,
    }, new String [] {
        "compareAscending",
        "compareCaseInsensitiveAscending",
        "compareCaseInsensitiveDescending",
        "compareDescending",
    });

    /**
     * Simply utility method to create a concreate
     * set object from an array
     * @param array of elements
     * @return concreate set.
     */
    // CHECKME: Is this a value add?
    public static NSSet setFromArray(NSArray array) {
        if (array == null || array.count() == 0)
            return NSSet.EmptySet;
        else {
            Object [] objs = new Object[array.count()];
            objs = array.objects();
            return new NSSet(objs);
        }
    }

    /**
        * The qualifiers EOSortOrdering.CompareAscending.. and friends are
     * actually 'special' and processed in a different/faster way when
     * sorting than a selector that would be created by
     * new NSSelector("compareAscending", ObjectClassArray). This method
     * eases the pain on creating those selectors from a string.
     * @param key sort key
     */
    public static NSSelector sortSelectorWithKey(String key) {
        NSSelector result=null;
        if (key!=null) {
            result=(NSSelector)_selectorsByKey.objectForKey(key);
            if (result==null) result=new NSSelector(key, ERXConstant.ObjectClassArray);
        }
        return result;
    }

    /**
     * Groups an array of objects by a given key path. The dictionary
     * that is returned contains keys that correspond to the grouped
     * keys values. 
     * 
     * @param objects array of objects to be grouped
     * @param keyPath path used to group the objects.
     * @return a dictionary where the keys are the grouped values and the
     * 		objects are arrays of the objects that have the grouped
     *		characteristic. Note that if the key path returns null
     *		then one of the keys will be the static ivar NULL_GROUPING_KEY
     */
    public static NSDictionary arrayGroupedByKeyPath(NSArray objects, String keyPath) {
        return arrayGroupedByKeyPath(objects,keyPath,true,null);
    }

    /**
     * Groups an array of objects by a given key path. The dictionary
     * that is returned contains keys that correspond to the grouped
     * keys values. 
     * 
     * @param eos array of objects to be grouped
     * @param keyPath path used to group the objects.
     * @param includeNulls determines if keyPaths that resolve to null
     *		should be allowed into the group.
     * @param extraKeyPathForValues allows a selected object to include
     *		more objects in the group. This is going away in the
     *		future.
     * @return a dictionary where the keys are the grouped values and the
     * 		objects are arrays of the objects that have the grouped
     *		characteristic. Note that if the key path returns null
     *		then one of the keys will be the static ivar NULL_GROUPING_KEY
     */
    // FIXME: Get rid of extraKeyPathForValues, it doesn't make sense.
    public static NSDictionary arrayGroupedByKeyPath(NSArray eos,
                                                     String keyPath,
                                                     boolean includeNulls,
                                                     String extraKeyPathForValues) {
        NSMutableDictionary result=new NSMutableDictionary();
        for (Enumeration e=eos.objectEnumerator(); e.hasMoreElements();) {
            Object eo = e.nextElement();
            Object key = NSKeyValueCodingAdditions.Utility.valueForKeyPath(eo,keyPath);
            boolean isNullKey = key==null || key instanceof NSKeyValueCoding.Null;
            if (!isNullKey || includeNulls) {
                if (isNullKey) key=NULL_GROUPING_KEY;
                NSMutableArray existingGroup=(NSMutableArray)result.objectForKey(key);
                if (existingGroup==null) {
                    existingGroup=new NSMutableArray();
                    result.setObjectForKey(existingGroup,key);
                }
                if (extraKeyPathForValues!=null) {
                    Object value=NSKeyValueCodingAdditions.Utility.valueForKeyPath(eo,extraKeyPathForValues);
                    if (value!=null) existingGroup.addObject(value);
                } else
                    existingGroup.addObject(eo);
            }
        }
        return result;
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
     * @param eos array of objects to be grouped
     * @param keyPath path used to group the objects.
     * @param includeNulls determines if keyPaths that resolve to null
     *      should be allowed into the group.
     * @return a dictionary where the keys are the grouped values and the
     *      objects are arrays of the objects that have the grouped
     *      characteristic. Note that if the key path returns null
     *      then one of the keys will be the static ivar NULL_GROUPING_KEY
     */
    public static NSDictionary arrayGroupedByToManyKeyPath(NSArray eos,
            String keyPath,
            boolean includeNulls) {
        NSMutableDictionary result=new NSMutableDictionary();
        for (Enumeration e=eos.objectEnumerator(); e.hasMoreElements();) {
            Object eo = e.nextElement();
            Object key = NSKeyValueCodingAdditions.Utility.valueForKeyPath(eo,keyPath);
            boolean isNullKey = key==null || key instanceof NSKeyValueCoding.Null;
            if (!isNullKey || includeNulls) {
                if (isNullKey) key=NULL_GROUPING_KEY;
                NSArray array = (NSArray)key;
                for(Enumeration keys = array.objectEnumerator(); keys.hasMoreElements(); ) {
                    key = keys.nextElement();
                    NSMutableArray existingGroup=(NSMutableArray)result.objectForKey(key);
                    if (existingGroup==null) {
                        existingGroup=new NSMutableArray();
                        result.setObjectForKey(existingGroup,key);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Simple comparision method to see if two array
     * objects are identical sets.
     * @param a1 first array
     * @param a2 second array
     * @return result of comparison
     */
    public static boolean arraysAreIdenticalSets(NSArray a1, NSArray a2) {
        boolean result=true;
        for (Enumeration e=a1.objectEnumerator();e.hasMoreElements();) {
            Object i=e.nextElement();
            if (!a2.containsObject(i)) {
                result=false; break;
            }
        }
        if (result) {
            for (Enumeration e=a2.objectEnumerator();e.hasMoreElements();) {
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
    public static NSArray filteredArrayWithQualifierEvaluation(NSArray array, EOQualifierEvaluation qualifier) {
        if (array == null) 
            return NSArray.EmptyArray;
        else 
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
    public static NSArray filteredArrayWithQualifierEvaluation(Enumeration enumeration, EOQualifierEvaluation qualifier) {
        NSMutableArray result = new NSMutableArray();
        while (enumeration.hasMoreElements()) {
            Object object = enumeration.nextElement();
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
    public static boolean enumerationHasMatchWithQualifierEvaluation(Enumeration enumeration, EOQualifierEvaluation qualifier) {
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
    public static boolean iteratorHasMatchWithQualifierEvaluation(Iterator iterator, EOQualifierEvaluation qualifier) {
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
    public static NSArray filteredArrayWithQualifierEvaluation(Iterator iterator, EOQualifierEvaluation qualifier) {
        NSMutableArray result = new NSMutableArray();
        while (iterator.hasNext()) {
            Object object = iterator.next();
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
    public static NSArray arrayWithoutDuplicateKeyValue(NSArray objects, String key){
        NSMutableSet present = new NSMutableSet();
        NSMutableArray result = new NSMutableArray();
        for(Enumeration e = objects.objectEnumerator(); e.hasMoreElements(); ){
            Object o = e.nextElement();
            Object value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(o, key);
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
    public static NSArray arrayMinusArray(NSArray main, NSArray minus) {
		NSSet minusSet = new NSSet(minus);
		NSMutableArray mutableResult = new NSMutableArray(main.count()); 
		Enumeration e = main.objectEnumerator();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (! minusSet.containsObject(obj)) 
				mutableResult.addObject(obj);
		}
		return mutableResult.immutableClone();
    }
	
    /**
     * Subtracts a single object from an array.
     * @param main array to have value removed from it.
     * @param minus object to be removed
     * @return array after performing subtraction.
     */
    public static NSArray arrayMinusObject(NSArray main, Object object) {
        NSMutableArray mutable = new NSMutableArray(main);
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
    public static NSArray arrayByAddingObjectsFromArrayWithoutDuplicates(NSArray a1, NSArray a2) {
        // FIXME this is n2 -- could be made n lg n
        NSArray result=null;
        if (a2.count()==0)
            result=a1;
        else {
            NSMutableArray mutableResult=new NSMutableArray(a1);
            for (Enumeration e=a2.objectEnumerator(); e.hasMoreElements();) {
                Object elt=e.nextElement();
                if (!mutableResult.containsObject(elt)) mutableResult.addObject(elt);
            }
            result=mutableResult;
        }
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
    public static NSArray arrayByRemovingFirstObject(NSArray array) {
        NSArray result = null;
        
        if ( array != null ) {
            final int count = array.count();
            
            result = count > 1 ? array.subarrayWithRange(new NSRange(1, count-1)) : NSArray.EmptyArray;
        }
        
        return result;
    }

    /**
     * Adds all of the non-duplicate elements from the second
     * array to the mutable array.
     * @param a1 mutable array where non-duplicate objects are
     *		added
     * @param a2 array to be added to a1
     */
    public static void addObjectsFromArrayWithoutDuplicates(NSMutableArray a1, NSArray a2) {
        for (Enumeration e=a2.objectEnumerator(); e.hasMoreElements();) {
            Object elt=e.nextElement();
            if (!a1.containsObject(elt)) a1.addObject(elt);
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
     * @param array to be flattened
     * @param filterDuplicates determines if the duplicate values
     *      should be filtered
     * @return an array containing all of the elements from
     *      all of the arrays contained within the array
     *      passed in. (Optionally, with duplicate elements filtered out)
     */
    public static NSArray flatten(NSArray originalArray, boolean filterDuplicates) {
        NSArray flattenedArray = flatten(originalArray);
        
        if (filterDuplicates) {
            return arrayWithoutDuplicates(flattenedArray);
        } else {
            return flattenedArray;
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
     * @param array to be flattened
     * @return an array containing all of the elements from
     *      all of the arrays contained within the array
     *      passed in.
     */
    public static NSArray flatten(NSArray originalArray) {
        if (originalArray == null || originalArray.count() < 1) {
            return originalArray;
        }
        
        NSMutableArray newArray = null;  // Not gonna create a new array if we don't actually need to flatten
        for (int i = 0; i < originalArray.count(); i++) {
            Object element = originalArray.objectAtIndex(i);
            if (element instanceof NSArray) {
                if (newArray == null) {
                    // Turns out we actually need to flatten
                    newArray = new NSMutableArray();
                    for (int backfillIndex = 0; backfillIndex < i; backfillIndex++) {
                        // backfill any singles we put off copying
                        newArray.addObject(originalArray.objectAtIndex(backfillIndex));
                    }
                }
                
                NSArray flattenedChildArray = flatten((NSArray)element);
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
    public static NSArray arrayFromPropertyList(String name, NSBundle bundle) {
        return (NSArray)NSPropertyListSerialization.propertyListFromString(ERXStringUtilities.stringFromResource(name, "plist", bundle));
    }

    /**
     * Performs multiple key-value coding calls against an array.
     * @param array object to be acted upon.
     * @param paths array of keypaths.
     * @return returns an array containing an array of values for
     *         every keypath.
     */
    public static NSArray valuesForKeyPaths(Object array, NSArray paths) {
        NSMutableArray result = new NSMutableArray();

        Enumeration e = paths.objectEnumerator();
        while(e.hasMoreElements()) {
            result.addObject(NSKeyValueCodingAdditions.Utility.valueForKeyPath(array, (String)e.nextElement()));
        }
        return result;
    }

    /**
     * Returns the first object of the array.  If the array is null or empty, null is returned.
     *
     * @param array the array to search.
     * @return the first object in array.  null if array is empty or if array is null.
     */
    public static Object firstObject(NSArray array) {
        Object result = null;
        
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
    public static int indexOfFirstObjectWithValueForKeyPath(NSArray array, Object value, String keyPath) {
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
    public static Object firstObjectWithValueForKeyPath(NSArray array, Object value, String keyPath) {
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
    public static NSArray objectsWithValueForKeyPath(final NSArray array, final Object valueToLookFor, final String keyPath) {
        final boolean valueToLookForIsNull = valueToLookFor == null || valueToLookFor == NSKeyValueCoding.NullValue;
        NSArray result = null;

        if ( array != null && array.count() > 0 ) {
            final NSMutableArray a = new NSMutableArray();
            final Enumeration arrayEnumerator = array.objectEnumerator();

            while ( arrayEnumerator.hasMoreElements() ) {
                final Object theObject = arrayEnumerator.nextElement();

                if ( theObject != NSKeyValueCoding.NullValue ) {
                    final Object theValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(theObject, keyPath);
                    final boolean theValueIsNull = theValue == null || theValue == NSKeyValueCoding.NullValue;

                    if ( (theValueIsNull && valueToLookForIsNull) || ERXExtensions.safeEquals(valueToLookFor, theValue) )
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
    public static int indexOfObjectUsingEqualator(NSArray array, Object object, ERXEqualator equalator) {
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
    public static NSMutableArray sortedMutableArraySortedWithKey(NSArray array, String key) {
        return sortedArraySortedWithKey(array, key).mutableClone();
    }

    /**
     * Sorts a given array with a key in ascending fashion.
     * @param array array to be sorted.
     * @param key sort key.
     * @return mutable clone of sorted array.
     */
    public static NSArray sortedArraySortedWithKey(NSArray array, String key) {
        return sortedArraySortedWithKey(array, key, null);
    }

    /**
     * Sorts a given array with a key in ascending fashion.
     * @param array array to be sorted.
     * @param key sort key.
     * @param selector sort order selector to use, if null, then sort will be case insensitive ascending.
     * @return sorted array.
     */
    public static NSArray sortedArraySortedWithKey(NSArray array, String key, NSSelector selector) {
        ERXAssert.PRE.notNull("Attempting to sort null array of objects.", array);
        ERXAssert.PRE.notNull("Attepting to sort array of objects with null key.", key);
        NSArray order=new NSArray(new Object[] {EOSortOrdering.sortOrderingWithKey(key, selector == null ? EOSortOrdering.CompareCaseInsensitiveAscending : selector)});
        return EOSortOrdering.sortedArrayUsingKeyOrderArray(array, order);
    }

    /**
        * Sorts a given array with a set of keys according to the given selector.
     * @param array array to be sorted.
     * @param keys sort keys
     * @param selector sort order selector to use, if null, then sort will be case insensitive ascending.
     * @return sorted array.
     */
    public static NSArray sortedArraySortedWithKeys(NSArray array, NSArray keys, NSSelector selector) {
        ERXAssert.PRE.notNull("Attempting to sort null array of objects.", array);
        ERXAssert.PRE.notNull("Attepting to sort an array with null keys.", keys);
        if (keys.count() < 2)
            return sortedArraySortedWithKey(array, (String)keys.lastObject(), selector == null ? EOSortOrdering.CompareCaseInsensitiveAscending : selector);

        NSMutableArray order = new NSMutableArray(keys.count());
        for (Enumeration keyEnumerator = keys.objectEnumerator(); keyEnumerator.hasMoreElements();) {
            String key = (String)keyEnumerator.nextElement();
            order.addObject(EOSortOrdering.sortOrderingWithKey(key, selector == null ? EOSortOrdering.CompareCaseInsensitiveAscending : selector));
        }
        return EOSortOrdering.sortedArrayUsingKeyOrderArray(array, order);
    }   
    
    /**
     * Sorts a given mutable array with a key in place.
     * @param array array to be sorted.
     * @param key sort key.
     */
    public static void sortArrayWithKey(NSMutableArray array, String key) {
        sortArrayWithKey(array, key, null);
    }

    /**
     * Sorts a given mutable array with a key in place.
     * @param array array to be sorted.
     * @param key sort key.
     * @param selector sort order selector to use, if null, then sort will be ascending.
     */
    public static void sortArrayWithKey(NSMutableArray array, String key, NSSelector selector) {
        ERXAssert.PRE.notNull("Attempting to sort null array of eos.", array);
        ERXAssert.PRE.notNull("Attempting to sort array of eos with null key.", key);
        NSArray order=new NSArray(new Object[] {EOSortOrdering.sortOrderingWithKey(key, selector == null ? EOSortOrdering.CompareCaseInsensitiveAscending : selector)});
        EOSortOrdering.sortArrayUsingKeyOrderArray(array, order);
    }

    /**
     * The core class of {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator}, which adds support for keyPaths.<br/>
     */

    static abstract class BaseOperator implements NSArray.Operator {
        public NSArray contents(NSArray array, String keypath) {
            if(array != null && array.count() > 0  && keypath != null && keypath.length() > 0) {
                array = (NSArray)NSKeyValueCodingAdditions.Utility.valueForKeyPath(array, keypath);
            }
            return array;
        }
    }

    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} for the key <b>sort</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@sort.firstName");</code><br/>
     * <code>myArray.valueForKey("@sort.lastName,firstName");</code><br/>
     * <br/>
     * Which in the first case would return myArray sorted ascending by first name and the second case
     * by lastName and then by firstName.
     */
    public static class SortOperator implements NSArray.Operator
    {
        private NSSelector selector;
        
        /** public empty constructor */
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
            } else {
                return sortedArraySortedWithKey(array, keypath, selector);
            }
        }
    }

    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} for the key <b>fetchSpec</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@fetchSpec.fetchUsers");</code><br/>
     * <br/>
     * Which in this case would return myArray filtered and sorted by the
     * EOFetchSpecification named "fetchUsers" which must be a model-based fetchspec in the
     * first object's entity.
     */
    public static class FetchSpecOperator implements NSArray.Operator
    {
        /** public empty constructor */
        public FetchSpecOperator() {}
        
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
            return filteredArrayWithEntityFetchSpecification(array, eo.entityName(), keypath);
        }
    }

    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} for the key <b>flatten</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@flatten");</code><br/>
     * <br/>
     * Which in this case would return myArray flattened if myArray is an NSArray of NSArrays (of NSArrays etc).
     */
    public static class FlattenOperator extends BaseOperator {
        /** public empty constructor */
        public FlattenOperator() {}

        /**
        * Flattens the given array.
         * @param array array to be filtered.
         * @param keypath name of fetch specification.
         * @return immutable filtered array.
         */
        public Object compute(NSArray array, String keypath) {
            array = flatten(array);
            return contents(array, keypath);
        }
    }

    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} for the key <b>isEmpty</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@isEmpty");</code><br/>
     * <br/>
     * 
     */
    public static class IsEmptyOperator implements NSArray.Operator {
        /** public empty constructor */
        public IsEmptyOperator() {}

        /**
        * returns true if the given array is empty, usefull for WOHyperlink disabled binding.
         * @param array array to be checked.
         * @param keypath name of fetch specification.
         * @return <code>Boolean.TRUE</code> if array is empty, <code>Boolean.FALSE</code> otherwise.
         */
        public Object compute(NSArray array, String keypath) {
            return array.count() == 0 ? Boolean.TRUE : Boolean.FALSE;
        }
    }


    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} for the key <b>subarrayWithRange</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKeyPath("@subarrayWithRange.3-20.name");</code><br/>
     * <br/>
     *
     */
    public static class SubarrayWithRangeOperator extends BaseOperator {
        /** public empty constructor */
        public SubarrayWithRangeOperator() {}

        /**
         * @param array array to be checked.
         * @param keypath name of fetch specification.
         * @return <code>Boolean.TRUE</code> if array is empty, <code>Boolean.FALSE</code> otherwise.
         */
        public Object compute(NSArray array, String keypath) {
            int i1 = keypath.indexOf(".");
            int i2 = keypath.indexOf("-");
            String rest = null;
            if ( i1 == -1 || i2 == -1 ) {
                throw new IllegalArgumentException("subarrayWithRange must be used like @subarrayWithRange.start-length");
            }
            String str = keypath.substring(i1, i2);
            int start = str.length() == 0 ? 0 : Integer.parseInt(str);
            str = keypath.substring(i2);
            int dot = str.indexOf(".");
            if(dot >= 0) {
            	rest = str.substring(dot);
            	str = str.substring(0, dot);
            }
            int length = str.length() == 0 ? array.count() : Integer.parseInt(str);
            NSArray objects = array.subarrayWithRange(new NSRange(start, length));
            return contents(objects, rest);
        }
    }

    

    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} for the key <b>unique</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKeyPath("@unique.someOtherPath");</code><br/>
     * <br/>
     * Which in this case would return only those objects which are unique in myArray.
     */
    public static class UniqueOperator extends BaseOperator {
        /** public empty constructor */
        public UniqueOperator() {
        }

        /**
         * Removes duplicates.
         * 
         * @param array
         *            array to be filtered.
         * @param keypath
         *            name of fetch specification.
         * @return immutable filtered array.
         */
        public Object compute(NSArray array, String keypath) {
            array = contents(array, keypath);
            if (array != null) array = arrayWithoutDuplicates(array);
            return array;
        }
    }


    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} for the key <b>removeNullValues</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKeyPath("@removeNullValues.someOtherPath");</code><br/>
     * <br/>
     * Which in this case would return myArray without the occurrences of NSKeyValueCoding.Null.
     */
    public static class RemoveNullValuesOperator extends BaseOperator {
        /** public empty constructor */
        public RemoveNullValuesOperator() {
        }

        /**
         * Removes null values from the given array.
         * 
         * @param array
         *            array to be filtered.
         * @param keypath
         *            name of fetch specification.
         * @return immutable filtered array.
         */
        public Object compute(NSArray array, String keypath) {
            if(keypath != null) {
                array = contents(array, keypath);
            }
            if (array != null) array = removeNullValues(array);
            return array;
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
        /** public empty constructor */
        public ObjectAtIndexOperator() {}

        /**
         * returns the keypath value for n-ths object.
         * @param array array to be checked.
         * @param keypath integer value of index (zero based).
         * @return <code>null</code> if array is empty or value is not in index, <code>keypath</code> value otherwise.
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
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} for the key <b>avgNonNull</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@avgNonNull.revenue");</code><br/>
     * <br/>
     * which will sum up all values and divide by the number of nun-null entries. 
     */
    public static class AvgNonNullOperator implements NSArray.Operator {
        /** public empty constructor */
        public AvgNonNullOperator() {}

        /**
         * returns the average value for over all non-null values.
         * @param array array to be checked.
         * @param keypath value of average.
         * @return computed average as double or <code>NULL</code>.
         */
        public Object compute(NSArray array, String keypath) {
            BigDecimal result = new BigDecimal(0L);
            int count = 0;
            
            for(Enumeration e = array.objectEnumerator(); e.hasMoreElements();) {
                Object value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(e.nextElement(), keypath);
                if(value != null && value != NSKeyValueCoding.NullValue) {
                    count = count+1;
                    result = result.add(ERXValueUtilities.bigDecimalValue(value));
                }
            }
            if(count == 0) {
                return null;
            }
            return result.divide(BigDecimal.valueOf((long) count), result.scale() + 4, 6);
        }
    }

    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} for the key <b>reverse</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@reverse.someMorePath");</code><br/>
     * <br/>
     * which return a reversed result as to you would normally get.
     */
    public static class ReverseOperator extends BaseOperator {
        /** public empty constructor */
        public ReverseOperator() {}

        /**
         * returns the reverse value for the values of the keypath.
         * @param array array to be checked.
         * @param keypath value of reverse.
         * @return reversed array for keypath.
         */
        public Object compute(NSArray array, String keypath) {
            array = reverse(array);
            array = contents(array, keypath);
            return array;
        }
    }
    /**
     * Define an {@link com.webobjects.foundation.NSArray.Operator NSArray.Operator} for the key <b>median</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@median.someMorePath");</code><br/>
     * <br/>
     * which return the median of the array elements at the given key path.
     * The median is the value for which half of the elements are above and half the elements are below.
     * As such, an array sort is needed and this might be very costly depending of the size of the array.
     */
    public static class MedianOperator extends BaseOperator {
        /** public empty constructor */
        public MedianOperator() {}

        /**
         * returns the median value for the values of the keypath.
         * @param array array to be checked.
         * @param keypath value of reverse.
         * @return reversed array for keypath.
         */
        public Object compute(NSArray array, String keypath) {
            return median(array, keypath);
        }
    }
    /** 
     * Will register new NSArray operators
     * <b>sort</b>, <b>sortAsc</b>, <b>sortDesc</b>, <b>sortInsensitiveAsc</b>,
     * <b>sortInsensitiveDesc</b>, <b>unique</b>, <b>flatten</b>, <b>reverse</b> and <b>fetchSpec</b> 
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
        }
    }
    
    
    /**
     * Calculates the median value of an array.
     * The median is the value for which half of the elements are above and half the elements are below.
     * As such, an array sort is needed and this might be very costly depending of the size of the array.
     * @param array array of objects
     * @param keypath key path for the median
     * @return
     */
    public static Number median(NSArray array, String keypath) {
        int count = array.count();
        Number value;
        if(count == 0) {
            value = null;
        } else if(count == 1) {
            value = (Number) array.valueForKeyPath(keypath);
        } else {
            // array = (NSArray) array.valueForKeyPath(keypath);
            // array = ERXArrayUtilities.sortedArraySortedWithKey(array, "doubleValue");
            array = ERXArrayUtilities.sortedArraySortedWithKey(array, keypath);
            int mid = count / 2;
            if(count % 2 == 0) {
                Object o = array.objectAtIndex(mid-1);
                Number a = (Number)NSKeyValueCodingAdditions.Utility.valueForKeyPath(o, keypath);
                o = array.objectAtIndex(mid);
                Number b = (Number)NSKeyValueCodingAdditions.Utility.valueForKeyPath(o, keypath);
                value = new Double((a.doubleValue()+b.doubleValue())/2);
            } else {
                value = (Number) NSKeyValueCodingAdditions.Utility.valueForKeyPath(array.objectAtIndex(mid), keypath);
            }
        }
        return value;
    }
    
    
    /**
     * Filters out all of the duplicate objects in
     * a given array.<br/> Preserves the order now.
     * @param anArray to be filtered
     * @return filtered array.
     */
    public static NSArray arrayWithoutDuplicates(NSArray anArray) {
        NSMutableArray result = new NSMutableArray();
        NSMutableSet already = new NSMutableSet();
        for(Enumeration e = anArray.objectEnumerator(); e.hasMoreElements();){
            Object object = e.nextElement();
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
     public static NSArray batchedArrayWithSize(NSArray array, int batchSize) {
        if(array == null || array.count() == 0)
            return NSArray.EmptyArray;

        NSMutableArray batchedArray = new NSMutableArray();
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
    public static NSArray filteredArrayWithEntityFetchSpecification(NSArray array, String entity, String fetchSpec, NSDictionary bindings) {
        EOEntity wrongParamEntity = EOModelGroup.defaultGroup().entityNamed(fetchSpec);
        if (wrongParamEntity != null) {
        	fetchSpec = entity;
			entity = wrongParamEntity.name();
			log.error("filteredArrayWithEntityFetchSpecification Calling conventions have changed from fetchSpec, entity to entity, fetchSpec");
		}
        EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed(fetchSpec, entity);
        NSArray sortOrderings, result;
        EOQualifier qualifier;

        if (bindings != null) {
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);
        }

        result = new NSArray(array);

        if ((qualifier = spec.qualifier()) != null) {
            result = EOQualifier.filteredArrayWithQualifier(result, qualifier);
        }

        if ((sortOrderings = spec.sortOrderings()) != null) {
            result = EOSortOrdering.sortedArrayUsingKeyOrderArray(result,sortOrderings);
        }

        return result;
    }
    
    /**
     * @deprecated
     */
    public static NSArray filteredArrayWithFetchSpecificationNamedEntityNamedBindings(NSArray array, String fetchSpec, String entity, NSDictionary bindings) {
        return filteredArrayWithEntityFetchSpecification( array, entity, fetchSpec, bindings);
    }

    /**
     * @deprecated
     */
    public static NSArray filteredArrayWithFetchSpecificationNamedEntityNamed(NSArray array, String fetchSpec, String entity) {
        return ERXArrayUtilities.filteredArrayWithEntityFetchSpecification(array, entity, fetchSpec, null);
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
    public static NSArray filteredArrayWithEntityFetchSpecification(NSArray array, String entity, String fetchSpec) {
        return ERXArrayUtilities.filteredArrayWithEntityFetchSpecification(array, entity,  fetchSpec, null);
    }
    
    /**
     * shifts a given object in an array one value to the left (index--).
     *
     * @param array array to be modified.
     * @param object the object that should be moved
     */
    public static void shiftObjectLeft(NSMutableArray array, Object object) {
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
    public static void shiftObjectRight(NSMutableArray array, Object object) {
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
    public static boolean arrayContainsAnyObjectFromArray(NSArray array, NSArray objects) {
        boolean arrayContainsAnyObject = false;
        if (array != null && objects != null && array.count() > 0 && objects.count() > 0) {
            for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();) {
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
    public static boolean arrayContainsArray(NSArray array, NSArray objects) {
        boolean arrayContainsAllObjects = true;
        if (array != null && objects != null && array.count() > 0 && objects.count() > 0) {
            for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();) {
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
    public static NSArray intersectingElements(NSArray array1, NSArray array2) {
        NSMutableArray intersectingElements = null;
        if (array1 != null && array1.count() > 0 && array2 != null && array2.count() > 0) {
            intersectingElements = new NSMutableArray();
            NSArray bigger = array1.count() > array2.count() ? array1 : array2;
            NSArray smaller = array1.count() > array2.count() ? array2 : array1;
            for (Enumeration e = smaller.objectEnumerator(); e.hasMoreElements();) {
                Object object = e.nextElement();
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
    public static NSArray reverse(NSArray array) {
        NSArray reverse = null;
        if (array != null && array.count() > 0) {
            NSMutableArray reverseTemp = new NSMutableArray(array.count());
            for (Enumeration reverseEnumerator = array.reverseObjectEnumerator(); reverseEnumerator.hasMoreElements();) {
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
    public static String friendlyDisplayForKeyPath(NSArray list, String attribute, String nullArrayDisplay, String separator, String finalSeparator) {
        Object result = null;
        int count = list!=null ? list.count() : 0;
        if (count==0) {
            result=nullArrayDisplay;
        } else if (count == 1) {
            result= (attribute!= null ? NSKeyValueCodingAdditions.Utility.valueForKeyPath(list.objectAtIndex(0), attribute) : list.objectAtIndex(0));
        } else if (count > 1) {
            StringBuffer buffer = new StringBuffer();
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
    public static NSArray arrayForKeysPath(NSArray array, NSArray keys) {
        NSMutableArray result=new NSMutableArray();
        if (array != null && keys != null) {
            for (Enumeration e = array.objectEnumerator(); e.hasMoreElements();) {
                Object o = e.nextElement();
                result.addObject(ERXDictionaryUtilities.dictionaryFromObjectWithKeys(o, keys));
            }
        }
        return result.immutableClone();
    }
    
    /** Removes all occurencies of NSKeyValueCoding.NullValue in the provided array
     * @param a the array from which the NullValue should be removed
     * @return a new NSArray with the same order than the original array but 
     * without NSKeyValueCoding.NullValue objects
     */
    public static NSArray removeNullValues(NSArray a) {
        NSMutableArray aa = new NSMutableArray();
        for (int i = 0; i < a.count(); i++) {
            Object o = a.objectAtIndex(i);
            if (!(o instanceof NSKeyValueCoding.Null)) {
                aa.addObject(o);
            }
        }
        return aa;
    }
    
        /** Converts an Object array to a String array by casting each element.
        * This is analogus to <code>String[] myStringArray = (String[])myObjectArray;</code> 
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
        return new NSArray(o).toString();
    }
    
    /** pretty prints a two dimensional Object array which is ugly when using toString
     * @param o the object which one wants to print as a String
     * @return the String which can be used in lets say 
     * <code>log.info("my array = "+ERXArrayUtilities.objectArrayToString(myArray));</code>
     */
    public static String objectArrayToString(Object[][] o) {
        NSMutableArray a = new NSMutableArray();
        for (int i = 0; i < o.length; i++) {
            a.addObject(objectArrayToString(o[i]));
        }
        return a.toString();
    }
    
    /** pretty prints a NSArray of two dimensional Object array which is ugly when using toString
     * @param o the object which one wants to print as a String
     * @return the String which can be used in lets say 
     * <code>log.info("my array = "+ERXArrayUtilities.objectArrayToString(myArray));</code>
     */
    public static String objectArraysToString(NSArray a) {
        NSMutableArray aa = new NSMutableArray();
        for (int i = 0; i < a.count(); i++) {
            aa.addObject(objectArrayToString((Object[][])a.objectAtIndex(i)));
        }
        return aa.toString();
    }

    /** removes all occurencies of NSKeyValueCoding.Null from the end of the array
     * @param array the array from which the values should be removed
     * @return a new NSArray which does not have NSKeyValueCoding.Null instances at the end
     */
    public static NSArray removeNullValuesFromEnd(NSArray array) {
        NSMutableArray a = array.mutableClone();
        while (a.lastObject() instanceof NSKeyValueCoding.Null) {
            a.removeLastObject();
        }
        return a;
    }
    
    public static String[] toStringArray(NSArray a) {
        String[] b = new String[a.count()];
        for (int i = a.count(); i-- > 0; b[i] = a.objectAtIndex(i).toString()) {
          // do nothing
        }
        return b;
    }

    /**
     * Calls dictionaryOfObjectsIndexedByKeyPathThrowOnCollision() passing false for throwOnCollision.
     */
    public static NSDictionary dictionaryOfObjectsIndexedByKeyPath(final NSArray array, final String keyPath) {
        return dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(array, keyPath, false);
    }

    /**
     * Given an array of objects, returns a dictionary mapping the value by performing valueForKeyPath on each object in
     * the array to the object in the array.  This is similar in concept to but different in semantic from
     * <code>arrayGroupedByKeyPath()</code>.  That method is focused on multiple objects returning the same value for the keypath
     * and, so, objects are grouped into arrays.  That is not particularly useful when there aren't collisions in the array or when
     * you don't care if there are collisions.  For example, with a CreditCard EO object, one could rely on the paymentType.name value
     * to be unique and thus you're more interested in being able to rapidly get to the EO.  <code>arrayGroupedByKeyPath()</code>
     * would require either flattening out the arrays or navigating them everytime.
     *
     * @param array array to index
     * @param keyPath keyPath to index.  if any object returns null of NSKeyValueCoding.NullValue for this keyPath, the
     *        object is not put into the resulting dictionary.
     * @param throwOnCollision if true and two objects in the array have the same non-null (or non-NullValue) value for keyPath,
     *        an exception is thrown.  if false, the last object in the array wins.
     * @return a dictionary indexing the given array.  if array is null, an empty dictionary is returned.
     */
    public static NSDictionary dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(final NSArray array, final String keyPath, final boolean throwOnCollision) {
        final NSMutableDictionary result = new NSMutableDictionary();
        final Enumeration e = array.objectEnumerator();

        while ( e.hasMoreElements() ) {
            final Object theObject = e.nextElement();
            final Object theKey = NSKeyValueCodingAdditions.Utility.valueForKeyPath(theObject, keyPath);

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
    public static NSArray arrayBySelectingInstancesOfClass(final NSArray array, final Class aClass) {
        NSArray result = null;

        if ( array != null && array.count() > 0 ) {
            final NSMutableArray a = new NSMutableArray();
            final Enumeration e = array.objectEnumerator();

            while ( e.hasMoreElements() ) {
                final Object theObject = e.nextElement();

                if ( aClass == null || aClass.isInstance(theObject) )
                    a.addObject(theObject);
            }

            if ( a.count() > 0 )
                result = a.immutableClone();
        }

        return result != null ? result : NSArray.EmptyArray;
    }

    /**
     * Just like the method on NSArray, except it catches the NSComparator.ComparisonException and, if thrown,
     * it wraps it in a runtime exception.  Returns null when passed null for array.
     */
    public static NSArray sortedArrayUsingComparator(final NSArray array, final NSComparator comparator) {
        NSArray result = array;

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
	 * @param array
	 *            in that the two given {@link Object}s have to be swapped
	 * @param object1
	 *            one object in the {@link NSArray} that will be swapped
	 * @param object2
	 *            the other object in the {@link NSArray} that will be swapped
	 * 
	 * @return the new {@link NSArray} with the swapped elements, if you provide
	 *         an {@link NSMutableArray} you'll get a mutable array back, a normal
	 *         NSArray otherwise
	 * 
	 * @throws {@link RuntimeException}
	 *             if one of the {@link Object}s is not in the {@link NSArray}
	 */
    public static NSArray swapObjects(NSArray array, final Object object1, final Object object2) {
    	int indexOfObject1 = array.indexOf(object1);
    	int indexOfObject2 = array.indexOf(object2);
    	
    	if (indexOfObject1 >= 0 && indexOfObject2 >= 0) {
    		return ERXArrayUtilities.swapObjectesAtIndexes(array, indexOfObject1, indexOfObject2);
    	}
    	else {
    		throw new RuntimeException("One of the given objects is not element of the array!");
    	}
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
	 * @return the new {@link NSArray} with the swapped elements, if you provide
	 *         an {@link NSMutableArray} you'll get a mutable array back, a normal
	 *         NSArray otherwise
	 * 
	 * @throws {@link RuntimeException} if one of the indexes is out of bound
	 */
	public static NSArray swapObjectesAtIndexes(NSArray array, final int indexOfObject1, final int indexOfObject2) {
		NSMutableArray tmpArray;
		if (array instanceof NSMutableArray) {
			tmpArray = (NSMutableArray) array;
		}
		else {
			tmpArray = array.mutableClone();
		}

		try {
			Object tmpObject = array.objectAtIndex(indexOfObject1);
			tmpArray.set(indexOfObject1, array.objectAtIndex(indexOfObject2));
			tmpArray.set(indexOfObject2, tmpObject);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		if (array instanceof NSMutableArray) {
			return tmpArray;
		}
		else {
			return tmpArray.immutableClone();
		}
	}

}
