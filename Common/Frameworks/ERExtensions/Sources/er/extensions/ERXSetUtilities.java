package er.extensions;

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
	 * @param set
	 *            the set to filter
	 * @param qualifier
	 *            the qualifier to apply
	 * @return the filtered set
	 */
	public static NSSet filteredSetWithQualifier(NSSet set, EOQualifier qualifier) {
		if (set == null) {
			return NSSet.EmptySet;
		}
		if (qualifier == null || qualifier._isEmpty()) {
			return set;
		}
		int count = set.count();
		NSMutableSet filteredSet = new NSMutableSet(count);
		Enumeration setEnum = set.objectEnumerator();
		while (setEnum.hasMoreElements()) {
			Object object = setEnum.nextElement();
			if (qualifier.evaluateWithObject(object)) {
				filteredSet.addObject(object);
			}
		}
		return filteredSet;
	}

	/**
	 * Like EOQualifier.filterArrayWithQualifier but for an NSMutableSet.
	 * 
	 * @param set
	 *            the set to filter in-place
	 * @param qualifier
	 *            the qualifier to apply
	 */
	public static void filterSetWithQualifier(NSMutableSet set, EOQualifier qualifier) {
		set.setSet(ERXSetUtilities.filteredSetWithQualifier(set, qualifier));
	}

}
