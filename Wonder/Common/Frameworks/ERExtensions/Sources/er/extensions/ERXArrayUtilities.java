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
 * Collection of {@link NSArray} utilities.<br/>
 * <br/>
 * Note that when this class is loaded it will register two new NSArray operators 
 * <b>sort</b> and <b>fetchSpec</b>.
 */
public class ERXArrayUtilities extends Object {
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
        return sortedArraySortedWithKey(array, key).mutableClone();
    }
    
    /**
     * Sorts a given array with a key in ascending fashion.
     * @param array array to be sorted.
     * @param key sort key.
     * @return sorted array.
     */
    public static NSArray sortedArraySortedWithKey(NSArray array, String key) {
        NSArray order=new NSArray(new Object[] {EOSortOrdering.sortOrderingWithKey(key, EOSortOrdering.CompareAscending)});
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
        /** public empty constructor */
	public SortOperator() {}

        /**
         * Sorts the given array by the keypath.
         * @param array array to be sorted.
         * @param keypath sort key.
         * @return immutable sorted array.
         */
	public Object compute(NSArray array, String keypath) {
            if (array.count() < 2)
                return array;
	    return sortedArraySortedWithKey(array, keypath);
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

    /** Registers the operators <b>sort</b> and <b>fetchSpec</b> during class initialization. */
    static {
        NSArray.setOperatorForKey("sort", new SortOperator());
        NSArray.setOperatorForKey("fetchSpec", new FetchSpecOperator());
    }

    /**
     * Filters a given array with a named fetch specification and bindings.
     * @param array array to be filtered.
     * @param fetchSpec name of the {@link EOFetchSpecification}.
     * @param entity name of the {@link EOEntity} to which the fetch specification is associated.
     * @param bindings bindings dictionary for qualifier variable substitution.
     * @return array filtered and sorted by the named fetch specification.
     */
    public static NSArray filteredArrayWithFetchSpecificationNamedEntityNamedBindings(NSArray array, String fetchSpec, String entity, NSDictionary bindings) {
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
     * Filters a given array with a named fetch specification.
     * @param array array to be filtered.
     * @param fetchSpec name of the {@link EOFetchSpecification}.
     * @param entity name of the {@link EOEntity} to which the fetch specification is associated.
     * @return array filtered and sorted by the named fetch specification.
     */
    public static NSArray filteredArrayWithFetchSpecificationNamedEntityNamed(NSArray array, String fetchSpec, String entity) {
        return ERXArrayUtilities.filteredArrayWithFetchSpecificationNamedEntityNamedBindings(array, fetchSpec, entity, null);
    }
}
