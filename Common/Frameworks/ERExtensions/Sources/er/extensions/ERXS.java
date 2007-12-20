package er.extensions;

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;

/**
 * <p>
 * ERXS provides lots of much shorter methods of constructing and using
 * EOSortOrdering than the very verbose style that you normally have to use.
 * </p>
 */
public class ERXS {
	public static final NSSelector ASC = EOSortOrdering.CompareAscending;
	public static final NSSelector DESC = EOSortOrdering.CompareDescending;
	public static final NSSelector INS_ASC = EOSortOrdering.CompareCaseInsensitiveAscending;
	public static final NSSelector INS_DESC = EOSortOrdering.CompareCaseInsensitiveDescending;

	public static EOSortOrdering sortOrder(String key, NSSelector comparison) {
		return EOSortOrdering.sortOrderingWithKey(key, comparison);
	}

	public static EOSortOrdering desc(String key) {
		return EOSortOrdering.sortOrderingWithKey(key, ERXS.DESC);
	}

	public static NSArray<EOSortOrdering> descs(String key) {
		return new NSArray<EOSortOrdering>(EOSortOrdering.sortOrderingWithKey(key, ERXS.DESC));
	}

	public static EOSortOrdering asc(String key) {
		return EOSortOrdering.sortOrderingWithKey(key, ERXS.ASC);
	}

	public static NSArray<EOSortOrdering> ascs(String key) {
		return new NSArray<EOSortOrdering>(EOSortOrdering.sortOrderingWithKey(key, ERXS.ASC));
	}

	public static EOSortOrdering descInsensitive(String key) {
		return EOSortOrdering.sortOrderingWithKey(key, ERXS.INS_DESC);
	}

	public static NSArray<EOSortOrdering> descInsensitives(String key) {
		return new NSArray<EOSortOrdering>(EOSortOrdering.sortOrderingWithKey(key, ERXS.INS_DESC));
	}

	public static EOSortOrdering ascInsensitive(String key) {
		return EOSortOrdering.sortOrderingWithKey(key, ERXS.INS_ASC);
	}

	public static NSArray<EOSortOrdering> ascInsensitives(String key) {
		return new NSArray<EOSortOrdering>(EOSortOrdering.sortOrderingWithKey(key, ERXS.INS_ASC));
	}

	public static NSArray<EOSortOrdering> sortOrders(String key, NSSelector comparison) {
		return new NSArray<EOSortOrdering>(EOSortOrdering.sortOrderingWithKey(key, comparison));
	}

	public static NSArray<EOSortOrdering> sortOrders(String key1, NSSelector comparison1, String key2, NSSelector comparison2) {
		return new NSArray<EOSortOrdering>(new EOSortOrdering[] { EOSortOrdering.sortOrderingWithKey(key1, comparison1), EOSortOrdering.sortOrderingWithKey(key2, comparison2) });
	}

	public static <T> void sort(NSMutableArray<T> array, EOSortOrdering... orderings) {
		EOSortOrdering.sortArrayUsingKeyOrderArray(array, new NSArray<EOSortOrdering>(orderings));
	}

	public static <T> void sort(NSMutableArray<T> array, NSArray<EOSortOrdering> orderings) {
		EOSortOrdering.sortArrayUsingKeyOrderArray(array, orderings);
	}

	public static <T> NSArray<T> sorted(NSArray<T> array, EOSortOrdering... orderings) {
		return ERXS.sorted(array, new NSArray<EOSortOrdering>(orderings));
	}

	@SuppressWarnings("unchecked")
	public static <T> NSArray<T> sorted(NSArray<T> array, NSArray<EOSortOrdering> orderings) {
		return (NSArray<T>) EOSortOrdering.sortedArrayUsingKeyOrderArray(array, orderings);
	}
}
