//
// NSArrayUtilities.java
// Project vwdBussinessLogicJava
//
// Created by ak on Wed Jun 06 2001
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import org.apache.log4j.*;
import java.util.Enumeration;

/**
 * Collection of {@link NSArray} utilities.
 */
public class ERXArrayUtilities extends Object {
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
    * Define an {@link NSArray$Operator} for the key <b>flatten</b>.<br/>
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
     * @param array array to be filtered.
     * @param fetchSpec name of the {@link EOFetchSpecification}.
     * @param entity name of the {@link EOEntity} to which the fetch specification is associated.
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
     * @param array array to be filtered.
     * @param fetchSpec name of the {@link EOFetchSpecification}.
     * @param entity name of the {@link EOEntity} to which the fetch specification is associated.
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
}
