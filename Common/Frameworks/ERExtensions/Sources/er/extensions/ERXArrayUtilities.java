//
// NSArrayUtilities.java
// Project vwdBussinessLogicJava
//
// Created by ak on Wed Jun 06 2001
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;

/**
 * Collection of {@link com.webobjects.foundation.NSArray NSArray} utilities.
 */
public class ERXArrayUtilities extends Object {
    /**
    * Holds the null grouping key for use when grouping objects
     * based on a key that might return null and nulls are allowed
     */
    public static final String NULL_GROUPING_KEY="**** NULL GROUPING KEY ****";

    /**
    * Groups an array of objects by a given key path. The dictionary
     * that is returned contains keys that correspond to the grouped
     * keys values. This means that the object pointed to by the key
     * path must be a cloneable object. For instance using the key path
     * 'company' would not work because enterprise objects are not
     * cloneable. Instead you might choose to use the key path 'company.name'
     * or 'company.primaryKey', if your enterprise objects support this
     * see {@link ERXGenericRecord} if interested.
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
     * keys values. This means that the object pointed to by the key
     * path must be a cloneable object. For instance using the key path
     * 'company' would not work because enterprise objects are not
     * cloneable. Instead you might choose to use the key path 'company.name'
     * of 'company.primaryKey', if your enterprise objects support this
     * see {@link ERXGenericRecord} if interested.
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
    * Simple comparision method to see if two array
     * objects are identical sets.
     * @param a1 first array
     * @param a2 second array
     * @return result of comparison
     */
    public static boolean arraysAreIdenticalSets(NSArray a1, NSArray a2) {
        boolean result=false;
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
    * Filters an array using the {@link EOQualifierEvaluation} interface.
     * @param a array to be filtered
     * @param q qualifier to do the filtering
     * @return array of filtered results.
     */
    // CHECKME: Is this a value add? EOQualifier has filteredArrayWithQualifier
    public static NSArray filteredArrayWithQualifierEvaluation(NSArray a, EOQualifierEvaluation q) {
        NSMutableArray result=null;
        if (a!=null) {
            result=new NSMutableArray();
            for (Enumeration e=a.objectEnumerator(); e.hasMoreElements();) {
                Object o=e.nextElement();
                if (q.evaluateWithObject(o)) result.addObject(o);
            }
        }
        return result;
    }


    /**
    * Filters out duplicates of an array of enterprise objects
     * based on the value of the given key off of those objects.
     * Note: Current implementation depends on the key returning a
     * cloneable object. Also the order is not preseved from the
     * original array.
     * @param eos array of enterprise objects
     * @param key key path to be evaluated off of every enterprise
     *		object
     * @return filter array of objects based on the value of a key-path.
     */
    // FIXME: Broken implementation, relies on the value returned by the key to be Cloneable
    //		also doesn't handle the case of the key returning null or an actual keyPath
    //		and has the last object in the array winning the duplicate tie.
    // FIXME: Does not preserve order.
    public static NSArray arrayWithoutDuplicateKeyValue(NSArray eos, String key){
        NSMutableDictionary dico = new NSMutableDictionary();
        for(Enumeration e = eos.objectEnumerator(); e.hasMoreElements(); ){
            NSKeyValueCoding eo = (NSKeyValueCoding)e.nextElement();
            Object value = eo.valueForKey(key);
            if(value != null){
                dico.setObjectForKey(eo, value);
            }
        }
        return dico.allValues();
    }

    /**
    * Subtracts the contents of one array from another.
     * Note: Current implementation does not preserve order.
     * @param main array to have values removed from it.
     * @param minus array of values to remove from the main array
     * @param result array after performing subtraction.
     */
    // FIXME: This has the side effect of removing any duplicate elements from
    //		the main array as well as not preserving the order of the array
    public static NSArray arrayMinusArray(NSArray main, NSArray minus){
        NSSet result = ERXUtilities.setFromArray(main);
        return result.setBySubtractingSet(ERXUtilities.setFromArray(minus)).allObjects();
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
    * Recursively flattens an array of arrays into a single
     * array of elements.<br/>
     * <br/>
     * For example:<br/>
     * <code>NSArray foos;</code> //Assume exists<br/>
     * <code>NSArray bars = (NSArray)foos.valueForKey("toBars");</code>
     * In this case if <code>foos</code> contained five elements
     * then the array <code>bars</code> will contain five arrays
     * each corresponding to what <code>aFoo.toBars</code> would
     * return. To have the entire collection of <code>bars</code>
     * in one single arra you would call:
     * <code>NSArray allBars = flatten(bars)</code>
     * @param array to be flattened
     * @return an array containing all of the elements from
     *		all of the arrays contained within the array
     *		passed in.
     */
    // ENHANCEME: Should add option to filter duplicates
    public static NSArray flatten(NSArray array) {
        NSMutableArray newArray=null;
        for (int i=0; i<array.count(); i++) {
            Object element=array.objectAtIndex(i);
            if (element instanceof NSArray) {
                if (newArray==null) {
                    newArray=new NSMutableArray();
                    for (int j=0; j<i; j++) {
                        if(array.objectAtIndex(j)!=null){
                            newArray.addObject(array.objectAtIndex(j));
                        }
                    }
                }
                NSArray a=flatten((NSArray)element);
                for (int j=0; j<a.count();j++) {
                    if(a.objectAtIndex(j)!=null){
                        newArray.addObject(a.objectAtIndex(j));
                    }
                }
            }
        }
        return (newArray !=null) ? newArray : array;
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
    public static NSArray valuesForKeyPaths(NSArray array, NSArray paths) {
        NSMutableArray result = new NSMutableArray();

        Enumeration e = paths.objectEnumerator();
        while(e.hasMoreElements()) {
            result.addObject((NSArray)array.valueForKeyPath((String)e.nextElement()));
        }
        return result;
    }

    /**
     * Sorts a given array with a key in ascending fashion and returns a mutable clone of the result.
     * @param array array to be sorted.
     * @param key sort key.
     * @return mutable clone of sorted array.
     */
    public static NSMutableArray sortedMutableArraySortedWithKey(NSArray array, String key) {
        return sortedArraySortedWithKey(array, key, null).mutableClone();
    }
    
    /**
     * Sorts a given array with a key in ascending fashion.
     * @param array array to be sorted.
     * @param key sort key.
     * @param selector sort order selector to use, if null, then sort will be ascending.
     * @return sorted array.
     */
    public static NSArray sortedArraySortedWithKey(NSArray array, String key, NSSelector selector) {
        NSArray order=new NSArray(new Object[] {EOSortOrdering.sortOrderingWithKey(key, selector == null ? EOSortOrdering.CompareAscending : selector)});
        return EOSortOrdering.sortedArrayUsingKeyOrderArray(array, order);
    }

    /**
     * Define an {@link NSArray$Operator} for the key <b>sort</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@sort.firstName");</code><br/>
     * <br/>
     * Which in this case would return myArray sorted ascending by first name.
     */
    static class SortOperator implements NSArray.Operator
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
	    return sortedArraySortedWithKey(array, keypath, selector);
	}
    }

    /**
     * Define an {@link NSArray$Operator} for the key <b>fetchSpec</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@fetchSpec.fetchUsers");</code><br/>
     * <br/>
     * Which in this case would return myArray filtered and sorted by the
     * EOFetchSpecification named fetchUsers.
     */
    static class FetchSpecOperator implements NSArray.Operator
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
            return filteredArrayWithFetchSpecificationNamedEntityNamed(array, keypath, eo.entityName());
        }
    }

    /**
    * Define an {@link NSArray$Operator} for the key <b>flatten</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@flatten");</code><br/>
     * <br/>
     * Which in this case would return myArray flattened.
     */
    static class FlattenOperator implements NSArray.Operator {
        /** public empty constructor */
        public FlattenOperator() {}

        /**
        * Flattens the given array.
         * @param array array to be filtered.
         * @param keypath name of fetch specification.
         * @return immutable filtered array.
         */
        public Object compute(NSArray array, String keypath) {
            return flatten(array);
        }
    }

    /**
     * Define an {@link com.webobjects.foundation.NSArray$Operator} for the key <b>flatten</b>.<br/>
     * <br/>
     * This allows for key value paths like:<br/>
     * <br/>
     * <code>myArray.valueForKey("@flatten");</code><br/>
     * <br/>
     * Which in this case would return myArray flattened.
     */
    static class UniqueOperator implements NSArray.Operator {
        /** public empty constructor */
        public UniqueOperator() {}

        /**
        * Flattens the given array.
         * @param array array to be filtered.
         * @param keypath name of fetch specification.
         * @return immutable filtered array.
         */
        public Object compute(NSArray array, String keypath) {
            return arrayWithoutDuplicates(array);
        }
    }

    /** Will register new NSArray operators
    * <b>sort</b>, <b>sortAsc</b>, <b>sortDesc</b>, <b>sortInsensitiveAsc</b>,
    * <b>sortInsensitiveDesc</b>, <b>unique</b>, <b>flatten</b> and <b>fetchSpec</b> */
    public static void initialize() {
        NSArray.setOperatorForKey("sort", new SortOperator(EOSortOrdering.CompareAscending));
        NSArray.setOperatorForKey("sortAsc", new SortOperator(EOSortOrdering.CompareAscending));
        NSArray.setOperatorForKey("sortDesc", new SortOperator(EOSortOrdering.CompareDescending));
        NSArray.setOperatorForKey("sortInsensitiveAsc", new SortOperator(EOSortOrdering.CompareCaseInsensitiveAscending));
        NSArray.setOperatorForKey("sortInsensitiveDesc", new SortOperator(EOSortOrdering.CompareCaseInsensitiveDescending));
        NSArray.setOperatorForKey("flatten", new FlattenOperator());
        NSArray.setOperatorForKey("fetchSpec", new FetchSpecOperator());
        NSArray.setOperatorForKey("unique", new UniqueOperator());
    }

    
    /**
    * Filters out all of the duplicate objects in
     * a given array.<br/>
     * Note: The current implementation does not preserve
     * 		the order of elements in the array.
     * @param anArray to be filtered
     * @return filtered array.
     */
    // FIXME: Does not preserve array order
    public static NSArray arrayWithoutDuplicates(NSArray anArray) {
        NSMutableSet aSet = new NSMutableSet();
        aSet.addObjectsFromArray(anArray);
        return aSet.allObjects();
    }

    /**
     * Filters a given array with a named fetch specification and bindings.
     *
     * @param array array to be filtered.
     * @param fetchSpec name of the {@link EOFetchSpecification}.
     * @param entity name of the {@link com.webobjects.eoaccess.EOEntity EOEntity} 
     * to which the fetch specification is associated.
     * @param bindings bindings dictionary for qualifier variable substitution.
     * @return array filtered and sorted by the named fetch specification.
     */    
    public static NSArray filteredArrayWithEntityFetchSpecification(NSArray array, String entity, String fetchSpec, NSDictionary bindings) {
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
     * @depreceated
     */
    public static NSArray filteredArrayWithFetchSpecificationNamedEntityNamedBindings(NSArray array, String fetchSpec, String entity, NSDictionary bindings) {
        return filteredArrayWithEntityFetchSpecification( array, entity, fetchSpec, bindings);
    }

    /**
     * Filters a given array with a named fetch specification.
     *
     * @param array array to be filtered.
     * @param fetchSpec name of the {@link EOFetchSpecification}.
     * @param entity name of the {@link com.webobjects.eoaccess.EOEntity EOEntity} 
     * to which the fetch specification is associated.
     * @return array filtered and sorted by the named fetch specification.
     */
    public static NSArray filteredArrayWithEntityFetchSpecification(NSArray array, String fetchSpec, String entity) {
        return ERXArrayUtilities.filteredArrayWithEntityFetchSpecification(array, entity, fetchSpec, null);
    }

    /**
    * @depreceated
     */
    public static NSArray filteredArrayWithFetchSpecificationNamedEntityNamed(NSArray array, String fetchSpec, String entity) {
        return ERXArrayUtilities.filteredArrayWithEntityFetchSpecification(array, entity, fetchSpec, null);
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
                    arrayContainsAnyObject = true; break;
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
        }
        return arrayContainsAllObjects;        
    }
}
