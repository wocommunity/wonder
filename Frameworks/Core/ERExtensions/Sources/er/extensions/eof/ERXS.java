package er.extensions.eof;

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

	public static ERXSortOrdering sortOrder(String key, NSSelector comparison) {
		return ERXSortOrdering.sortOrderingWithKey(key, comparison);
	}

	public static ERXSortOrdering desc(String key) {
		return ERXSortOrdering.sortOrderingWithKey(key, ERXS.DESC);
	}

	public static ERXSortOrdering.ERXSortOrderings descs(String... keys) {
		return sortOrders(DESC, keys);
	}

	public static ERXSortOrdering asc(String key) {
		return ERXSortOrdering.sortOrderingWithKey(key, ERXS.ASC);
	}

	public static ERXSortOrdering.ERXSortOrderings ascs(String... keys) {
		return sortOrders(ASC, keys);
	}

	public static ERXSortOrdering descInsensitive(String key) {
		return ERXSortOrdering.sortOrderingWithKey(key, ERXS.INS_DESC);
	}

	public static ERXSortOrdering.ERXSortOrderings descInsensitives(String... keys) {
		return sortOrders(INS_DESC, keys);
	}

	public static ERXSortOrdering ascInsensitive(String key) {
		return ERXSortOrdering.sortOrderingWithKey(key, ERXS.INS_ASC);
	}

	public static ERXSortOrdering.ERXSortOrderings ascInsensitives(String... keys) {
		return sortOrders(INS_ASC, keys);
	}

	public static ERXSortOrdering.ERXSortOrderings sortOrders(String key, NSSelector comparison) {
		return new ERXSortOrdering(key, comparison).array();
	}

	public static ERXSortOrdering.ERXSortOrderings sortOrders(String key1, NSSelector comparison1, String key2, NSSelector comparison2) {
		return new ERXSortOrdering.ERXSortOrderings(new EOSortOrdering[] { ERXSortOrdering.sortOrderingWithKey(key1, comparison1), ERXSortOrdering.sortOrderingWithKey(key2, comparison2) });
	}


	public static ERXSortOrdering.ERXSortOrderings sortOrders(NSSelector sel, String...keys) {
	  ERXSortOrdering.ERXSortOrderings result = new ERXSortOrdering.ERXSortOrderings();
		for (String key : keys) {
			result.addObject(ERXSortOrdering.sortOrderingWithKey(key, sel));
		}
		return result;
	}

	public static <T> void sort(NSMutableArray<T> array, EOSortOrdering... orderings) {
		EOSortOrdering.sortArrayUsingKeyOrderArray(array, new NSArray<>(orderings));
	}

	public static <T> void sort(NSMutableArray<T> array, NSArray<EOSortOrdering> orderings) {
		EOSortOrdering.sortArrayUsingKeyOrderArray(array, orderings);
	}

	public static <T> NSArray<T> sorted(NSArray<T> array, EOSortOrdering... orderings) {
		return ERXS.sorted(array, new NSArray<>(orderings));
	}

	@SuppressWarnings("unchecked")
	public static <T> NSArray<T> sorted(NSArray<T> array, NSArray<EOSortOrdering> orderings) {
		return EOSortOrdering.sortedArrayUsingKeyOrderArray(array, orderings);
	}

	public static ERXSortOrdering.ERXSortOrderings chain(EOSortOrdering... sortOrderings) {
		return new ERXSortOrdering.ERXSortOrderings(sortOrderings);
	}

	public static ERXSortOrdering.ERXSortOrderings chain(NSArray<EOSortOrdering>... arr) {
	  ERXSortOrdering.ERXSortOrderings result = new ERXSortOrdering.ERXSortOrderings();
		for (NSArray<EOSortOrdering> value : arr) {
			result.addObjectsFromArray(value);
		}
		return result;
	}

}
