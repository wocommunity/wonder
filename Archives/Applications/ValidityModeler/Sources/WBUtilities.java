import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;


/**
 * WBUtilities only has static methods that are used to sort, filter, and manipulate EOEnterpriseObjects.<BR>
 * This class also has a convience toHTML(String str) method.
 */
public final class WBUtilities  {
    
    private static final WBComboFormatter htmlFormatter = new WBComboFormatter(false, false, true);

    private WBUtilities(){
        //empty
    }

    /**
     * Sorts an array of EOEnterpriseObjects with a key.  The array will be ascending.
     *
     * @param anArray		Array of EOEnterpriseObjects
     * @param key		Key to sort by
     * @see #reverseSortArrayWithKey
     */
    public static void sortArrayWithKey(NSMutableArray anArray, String key){
        NSMutableArray sortOrderings = new NSMutableArray();
        sortOrderings.addObject(new EOSortOrdering(key, EOSortOrdering.CompareAscending));
        EOSortOrdering.sortArrayUsingKeyOrderArray(anArray, sortOrderings);
    }

    /**
     * Sorts an array of EOEnterpriseObjects with a key.  The array will be descending.
     *
     * @param anArray		Array of EOEnterpriseObjects
     * @param key		Key to sort by
     * @see #sortArrayWithKey
     */
    public static void reverseSortArrayWithKey(NSMutableArray anArray, String key){
        NSMutableArray sortOrderings = new NSMutableArray();
        sortOrderings.addObject(new EOSortOrdering(key, EOSortOrdering.CompareDescending));
        EOSortOrdering.sortArrayUsingKeyOrderArray(anArray, sortOrderings);
    }

    /**
     * Sorts an array of EOEnterpriseObjects with two keys.  The array will be ascending.
     *
     * @param anArray		Array of EOEnterpriseObjects
     * @param key1		Key to sort by first
     * @param key3		Key to sort by second
     * @see #reverseSortArrayWithKey1And2
     */
    public static void sortArrayWithKey1And2(NSMutableArray anArray, String key1, String key2){
        NSMutableArray sortOrderings = new NSMutableArray();
        sortOrderings.addObject(new EOSortOrdering(key1, EOSortOrdering.CompareAscending));
        sortOrderings.addObject(new EOSortOrdering(key2, EOSortOrdering.CompareAscending));
        EOSortOrdering.sortArrayUsingKeyOrderArray(anArray, sortOrderings);
    }

    /**
     * Sorts an array of EOEnterpriseObjects with two keys.  The array will be descending.
     *
     * @param anArray		Array of EOEnterpriseObjects
     * @param key1		Key to sort by first
     * @param key3		Key to sort by second
     * @see #sortArrayWithKey1And2
     */
    public static void reverseSortArrayWithKey1And2(NSMutableArray anArray, String key1, String key2){
        NSMutableArray sortOrderings = new NSMutableArray();
        sortOrderings.addObject(new EOSortOrdering(key1, EOSortOrdering.CompareDescending));
        sortOrderings.addObject(new EOSortOrdering(key2, EOSortOrdering.CompareDescending));
        EOSortOrdering.sortArrayUsingKeyOrderArray(anArray, sortOrderings);
    }

    /**
     * Sorts an array of EOEnterpriseObjects with a key.  The array will be ascending.
     *
     * @return			NSArray of sorted EOEnterpriseObjects
     * @param anArray		Array of EOEnterpriseObjects
     * @param key		Key to sort by
     * @see #reverseSortedArrayWithKey
     */
    public static NSArray sortedArrayWithKey(NSMutableArray anArray, String key){
        NSMutableArray sortOrderings = new NSMutableArray();
        sortOrderings.addObject(new EOSortOrdering(key, EOSortOrdering.CompareAscending));
        return EOSortOrdering.sortedArrayUsingKeyOrderArray(anArray, sortOrderings);
    }

    /**
     * Sorts an array of EOEnterpriseObjects with a key.  The array will be descending.
     *
     * @return			NSArray of sorted EOEnterpriseObjects
     * @param anArray		Array of EOEnterpriseObjects
     * @param key		Key to sort by
     * @see #sortedArrayWithKey
     */
    public static NSArray reverseSortedArrayWithKey(NSMutableArray anArray, String key){
        NSMutableArray sortOrderings = new NSMutableArray();
        sortOrderings.addObject(new EOSortOrdering(key, EOSortOrdering.CompareDescending));
        return EOSortOrdering.sortedArrayUsingKeyOrderArray(anArray, sortOrderings);
    }

    /**
     * Filters an array of EOEnterpriseObjects with a key and value.
     *
     * @return			NSArray of filted EOEnterpriseObjects
     * @param anArray		Array of EOEnterpriseObjects
     * @param key		Key to filter by
     * @param value		Value for key
     * @see #sortedArrayWithKey
     */
    public static NSArray filteredArrayWithKeyAndValue(NSArray anArray, String key, String value){
        NSMutableArray args = new NSMutableArray();
        args.addObject(key);
        args.addObject(value);
        EOQualifier qualifier = EOQualifier.qualifierWithQualifierFormat("%@ = %@", args);
        return EOQualifier.filteredArrayWithQualifier(anArray, qualifier);
    }

    /**
     * Takes a String and converts the carriage returns to breaks and tabs to five non-break spaces.
     *
     * @return			HTML formatted string
     * @param str		String to format
     */
    public static String toHTML(String str){
        if(str != null){
            return htmlFormatter.format(str);
        }
        return null;
    }

    /**
     * Short hand method for EOUtilities.localInstanceOfObject(ec, anObject);.
     *
     * @return			EOEnterpriseObject
     * @param ec		EOEditingContext
     * @param anObject		EOEnterpriseObject
     */
    public static EOEnterpriseObject localInstance(EOEditingContext ec, EOEnterpriseObject anObject){
        return (EOEnterpriseObject)EOUtilities.localInstanceOfObject(ec, anObject);
    }

    /**
     * Invalidate the Object
     *
     * @param object		EOEnterpriseObject to invalidate
     */
    public static void invalidateObject(EOEnterpriseObject object){
        if(object != null){
            EOEditingContext ec = object.editingContext();
            EOGlobalID globalID = ec.globalIDForObject(object);
            //invalidate the object
            if(globalID != null){
                NSArray globalIDs = new NSArray(globalID);
                ec.invalidateObjectsWithGlobalIDs(globalIDs);
            }
        }
    }

    /**
     * Returns the Primary Key for an EOEnterpriseObject.
     *
     * @return			Returns Primary Key for object if object and key are not null, 0 otherwise.
     * @param object		EOEnterpriseObject to get the Primary Key
     * @param key		Key for Primary Key
     */
    public static Number primaryKeyForObjectWithKey(EOEnterpriseObject object, String key){
        if(object !=null && key != null){
            NSDictionary pDict = (NSDictionary)EOUtilities.primaryKeyForObject(object.editingContext(), object);
            return (Number)pDict.objectForKey(key);
        }
        return Integer.valueOf(0);
    }

}
