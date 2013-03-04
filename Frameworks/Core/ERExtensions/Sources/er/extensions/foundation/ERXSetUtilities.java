package er.extensions.foundation;

import java.util.Enumeration;

import com.webobjects.eocontrol.EOQualifier;
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

}
