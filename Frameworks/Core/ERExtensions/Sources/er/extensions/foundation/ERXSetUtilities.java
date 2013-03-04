package er.extensions.foundation;

import java.util.Enumeration;

import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;

/**
 * Collection of {@link com.webobjects.foundation.NSSet NSSet} utilities.
 */
public class ERXSetUtilities {
	/**
	 * Like EOQualifier.filteredArrayWithQualifier but for an NSSet.
	 * 
	 * @param <T>
	 *            type of set contents
	 * @param set
	 *            the set to filter
	 * @param qualifier
	 *            the qualifier to apply
	 * @return the filtered set
	 */
	public static <T> NSSet<T> filteredSetWithQualifier(NSSet<T> set, EOQualifier qualifier) {
		if (set == null) {
			return NSSet.EmptySet;
		}
		if (qualifier == null || qualifier._isEmpty()) {
			return set;
		}
		int count = set.count();
		NSMutableSet<T> filteredSet = new NSMutableSet<T>(count);
		Enumeration setEnum = set.objectEnumerator();
		while (setEnum.hasMoreElements()) {
			Object object = setEnum.nextElement();
			if (qualifier.evaluateWithObject(object)) {
				filteredSet.addObject((T) object);
			}
		}
		return filteredSet;
	}

	/**
	 * Like EOQualifier.filterArrayWithQualifier but for an NSMutableSet.
	 * 
	 * @param <T>
	 *            type of set contents
	 * @param set
	 *            the set to filter in-place
	 * @param qualifier
	 *            the qualifier to apply
	 */
	public static <T> void filterSetWithQualifier(NSMutableSet<T> set, EOQualifier qualifier) {
		set.setSet(ERXSetUtilities.filteredSetWithQualifier(set, qualifier));
	}

	/**
	 * Takes an unordered set and creates a sorted array from its elements.
	 * 
	 * @param <T>
	 *            type of set contents
	 * @param set
	 *            the set containing the elements to sort
	 * @param orderings
	 *            the sort orderings
	 * @return an array with sorted elements
	 */
	public static <T> NSArray<T> sortedArrayFromSet(NSSet<T> set, NSArray<EOSortOrdering> orderings) {
		return EOSortOrdering.sortedArrayUsingKeyOrderArray(set.allObjects(), orderings);
	}

	/**
	 * Takes an unordered set and creates a sorted array from its elements.
	 * 
	 * @param <T>
	 *            type of set contents
	 * @param set
	 *            the set containing the elements to sort
	 * @param orderings
	 *            list of sort orderings
	 * @return an array with sorted elements
	 */
	public static <T> NSArray<T> sortedArrayFromSet(NSSet<T> set, EOSortOrdering... orderings) {
		return sortedArrayFromSet(set, new NSArray<EOSortOrdering>(orderings));
	}

	/**
	 * Takes an unordered set and creates a sorted array from its elements.
	 * 
	 * @param <T>
	 *            type of set contents
	 * @param set
	 *            the set containing the elements to sort
	 * @param comparator
	 *            a comparator
	 * @return an array with sorted elements
	 * @throws NSComparator.ComparisonException if comparator cannot sort these elements
	 * @throws IllegalArgumentException if comparator is <code>null</code>
	 */
	public static <T> NSArray<T> sortedArrayFromSet(NSSet<T> set, NSComparator comparator) throws NSComparator.ComparisonException {
		NSMutableArray<T> array = new NSMutableArray<T>((T[]) set.toArray());
		array.sortUsingComparator(comparator);
		return array;
	}
}
