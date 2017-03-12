package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSMutableArray;

/**
 * ERXQ
 * 
 * @author mschrag
 */
public class ERXChainedQualifierUtils {
	/**
	 * Returns a new qualifier that represents the original qualifier and'd to
	 * the given list of qualifiers.
	 * 
	 * @param originalQualifier
	 *            the qualifier to and
	 * @param qualifiers
	 *            the qualifiers to and with this qualifier
	 * @return an ERXAndQualifier
	 */
	public static ERXAndQualifier and(EOQualifier originalQualifier, EOQualifier... qualifiers) {
		NSMutableArray<EOQualifier> newQualifiers = new NSMutableArray<>();
		if (originalQualifier != null) {
			newQualifiers.addObject(originalQualifier);
		}
		for (EOQualifier qualifier : qualifiers) {
			if (qualifier != null) {
				newQualifiers.addObject(qualifier);
			}
		}
		return new ERXAndQualifier(newQualifiers);
	}

	/**
	 * Returns a new qualifier that represents the original qualifier or'd to
	 * the given list of qualifiers.
	 * 
	 * @param originalQualifier
	 *            the qualifier to or
	 * @param qualifiers
	 *            the qualifiers to or with this qualifier
	 * @return an ERXOrQualifier
	 */
	public static ERXOrQualifier or(EOQualifier originalQualifier, EOQualifier... qualifiers) {
		NSMutableArray<EOQualifier> newQualifiers = new NSMutableArray<>();
		if (originalQualifier != null) {
			newQualifiers.addObject(originalQualifier);
		}
		for (EOQualifier qualifier : qualifiers) {
			if (qualifier != null) {
				newQualifiers.addObject(qualifier);
			}
		}
		return new ERXOrQualifier(newQualifiers);
	}
}
