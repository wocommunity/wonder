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

public class ERXArrayUtilities extends Object {
    public static NSArray arrayFromPropertyList(String name, NSBundle bundle) {
        return (NSArray)NSPropertyListSerialization.propertyListFromString(ERXStringUtilities.stringFromResource(name, "plist", bundle));
    }
    
    public static NSMutableArray sortedMutableArraySortedWithKey(NSArray arr, String key) {
        return sortedArraySortedWithKey(arr, key).mutableClone();
    }

    public static NSArray sortedArraySortedWithKey(NSArray arr, String key) {
        NSArray order=new NSArray(new Object[] {EOSortOrdering.sortOrderingWithKey(key, EOSortOrdering.CompareAscending)});
        return EOSortOrdering.sortedArrayUsingKeyOrderArray(arr, order);
    }

    static class SortOperator implements NSArray.Operator
    {
	public SortOperator() {
	    /* empty */
	}
	
	public Object compute(NSArray array, String keypath) {
            if(array.count() == 0)
                return array;
	    return sortedArraySortedWithKey(array, keypath);
	}
    }

    static class FetchSpecOperator implements NSArray.Operator
    {
	public FetchSpecOperator() {
	    /* empty */
	}
	
	public Object compute(NSArray array, String keypath) {
            if(array.count() == 0) {
                return array;
            }
            EOEnterpriseObject eo = (EOEnterpriseObject)array.objectAtIndex(0);
	    return filteredArrayWithFetchSpecificationNamedEntityNamed(array, keypath, eo.entityName());
	}
    }
    
    
    static {
        NSArray.setOperatorForKey("sort", new SortOperator());
        NSArray.setOperatorForKey("fetchSpec", new FetchSpecOperator());
    }

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
    
    public static NSArray filteredArrayWithFetchSpecificationNamedEntityNamed(NSArray array, String fetchSpec, String entity) {
        return ERXArrayUtilities.filteredArrayWithFetchSpecificationNamedEntityNamedBindings(array, fetchSpec, entity, null);
    }

}
