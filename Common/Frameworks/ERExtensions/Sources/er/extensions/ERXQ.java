package er.extensions;

import java.util.Enumeration;

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;

/**
 * <p>
 * ERXQ provides lots of much shorter methods of constructing EOQualifiers than
 * the very verbose style that you normally have to use. For instance ...
 * </p>
 * 
 * <code>
 * EOQualifier qualifier = new EOAndQualifier(new NSArray(new Object[] { new EOKeyValueQualifier("name", EOQualifier.QualifierOperatorsEquals, "Mike"), new EOKeyValueQualifier("admin", EOQualifier.QualifierOperatorsEquals, Boolean.TRUE) }));
 * </code>
 * 
 * <p>
 * ... becomes ...
 * </p>
 * 
 * <code>
 * EOQualifier qualifier = ERXQ.and(ERXQ.equals("name", "Mike"), ERXQ.isTrue("admin"));
 * </code>
 * 
 * @author mschrag
 */
public class ERXQ {
	/**
	 * Equivalent to EOQualifier.QualifierOperatorEqual
	 */
	public static final NSSelector EQ = EOQualifier.QualifierOperatorEqual;

	/**
	 * Equivalent to EOQualifier.QualifierOperatorNotEqual
	 */
	public static final NSSelector NE = EOQualifier.QualifierOperatorNotEqual;
	
	/**
	 * Equivalent to EOQualifier.QualifierOperatorLessThan
	 */
	public static final NSSelector LT = EOQualifier.QualifierOperatorLessThan;
	
	/**
	 * Equivalent to EOQualifier.QualifierOperatorGreaterThan
	 */
	public static final NSSelector GT = EOQualifier.QualifierOperatorGreaterThan;
	
	/**
	 * Equivalent to EOQualifier.QualifierOperatorLessThanOrEqualTo
	 */
	public static final NSSelector LTEQ = EOQualifier.QualifierOperatorLessThanOrEqualTo;
	
	/**
	 * Equivalent to EOQualifier.QualifierOperatorGreaterThanOrEqualTo
	 */
	public static final NSSelector GTEQ = EOQualifier.QualifierOperatorGreaterThanOrEqualTo;
	
	/**
	 * Equivalent to EOQualifier.QualifierOperatorContains
	 */
	public static final NSSelector CONTAINS = EOQualifier.QualifierOperatorContains;
	
	/**
	 * Equivalent to EOQualifier.QualifierOperatorLike
	 */
	public static final NSSelector LIKE = EOQualifier.QualifierOperatorLike;
	
	/**
	 * Equivalent to EOQualifier.QualifierOperatorCaseInsensitiveLike
	 */
	public static final NSSelector ILIKE = EOQualifier.QualifierOperatorCaseInsensitiveLike;

	/**
	 * Equivalent to EOQualifier.filteredArrayWithQualifier(NSArray, EOQualifier)
	 * 
	 * @param <T> the type of the array
	 * @param array the array to filter
	 * @param qualifier the qualifier to filter with
	 * @return the filtered array
	 */
	@SuppressWarnings( { "cast", "unchecked" })
	public static <T> NSArray<T> filtered(NSArray<T> array, EOQualifier qualifier) {
		return (NSArray<T>) EOQualifier.filteredArrayWithQualifier(array, qualifier);
	}

	/**
	 * Equivalent to EOQualifier.filterArrayWithQualifier(NSMutableArray, EOQualfier)
	 * 
	 * @param array the array to filter (in place)
	 * @param qualifier the qualifier to filter with
	 */
	public static void filter(NSMutableArray<?> array, EOQualifier qualifier) {
		EOQualifier.filterArrayWithQualifier(array, qualifier);
	}

	/**
	 * Equivalent to new EOOrQualifier(new NSArray(qualifiersArray).  Nulls are skipped.
	 * 
	 * @param qualifiersArray the array of qualifiers to or
	 * @return and EOOrQualifier
	 */
	public static EOOrQualifier or(EOQualifier... qualifiersArray) {
		NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
		for (EOQualifier qualifier : qualifiersArray) {
			if (qualifier != null) {
				qualifiers.addObject(qualifier);
			}
		}
		return new EOOrQualifier(qualifiers);
	}

	/**
	 * Equivalent to new EOAndQualifier(new NSArray(qualifiersArray).  Nulls are skipped.
	 * 
	 * @param qualifiersArray the array of qualifiers to and
	 * @return and EOAndQualifier
	 */
	public static EOAndQualifier and(EOQualifier... qualifiersArray) {
		NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
		for (EOQualifier qualifier : qualifiersArray) {
			if (qualifier != null) {
				qualifiers.addObject(qualifier);
			}
		}
		return new EOAndQualifier(qualifiers);
	}

	/**
	 * Equivalent to new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorLike, value);
	 * 
	 * @param key the key
	 * @param value the value
	 * @return an EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier like(String key, Object value) {
		return new EOKeyValueQualifier(key, ERXQ.LIKE, value);
	}

	/**
	 * Equivalent to new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorCaseInsensitiveLike, value);
	 * 
	 * @param key the key
	 * @param value the value
	 * @return an EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier likeInsensitive(String key, Object value) {
		return new EOKeyValueQualifier(key, ERXQ.ILIKE, value);
	}

	/**
	 * Equivalent to new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorEqual, value);
	 * 
	 * @param key the key
	 * @param value the value
	 * @return an EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier equals(String key, Object value) {
		return new EOKeyValueQualifier(key, ERXQ.EQ, value);
	}

	/**
	 * Returns isNull or isNotNull depending on the value of yesOrNo.
	 * 
	 * @param key the key
	 * @param yesOrNo if true, returns isNull, if false, returns isNotNull
	 * @return the EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier isNull(String key, boolean yesOrNo) {
		return (yesOrNo) ? ERXQ.isNull(key) : ERXQ.isNotNull(key);
	}

	/**
	 * Equivalent to new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorEqual, null);
	 * 
	 * @param key the key
	 * @return an EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier isNull(String key) {
		return new EOKeyValueQualifier(key, ERXQ.EQ, null);
	}

	/**
	 * Equivalent to new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorNotEqual, null);
	 * 
	 * @param key the key
	 * @return an EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier isNotNull(String key) {
		return new EOKeyValueQualifier(key, ERXQ.NE, null);
	}

	/**
	 * Equivalent to new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorEqual, Boolean.TRUE);
	 * 
	 * @param key the key
	 * @return an EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier isTrue(String key) {
		return new EOKeyValueQualifier(key, ERXQ.EQ, Boolean.TRUE);
	}

	/**
	 * Equivalent to new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorEqual, Boolean.FALSE);
	 * 
	 * @param key the key
	 * @return an EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier isFalse(String key) {
		return new EOKeyValueQualifier(key, ERXQ.EQ, Boolean.FALSE);
	}

	/**
	 * Equivalent to new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorNotEqual, value);
	 * 
	 * @param key the key
	 * @param value the value
	 * @return an EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier notEquals(String key, Object value) {
		return new EOKeyValueQualifier(key, ERXQ.NE, value);
	}

	/**
	 * Equivalent to new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorLessThan, value);
	 * 
	 * @param key the key
	 * @param value the value
	 * @return an EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier lessThan(String key, Object value) {
		return new EOKeyValueQualifier(key, ERXQ.LT, value);
	}

	/**
	 * Equivalent to new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorGreaterThan, value);
	 * 
	 * @param key the key
	 * @param value the value
	 * @return an EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier greaterThan(String key, Object value) {
		return new EOKeyValueQualifier(key, ERXQ.GT, value);
	}

	/**
	 * Equivalent to new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorLessThanOrEqualTo, value);
	 * 
	 * @param key the key
	 * @param value the value
	 * @return an EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier lessThanOrEqualTo(String key, Object value) {
		return new EOKeyValueQualifier(key, ERXQ.LTEQ, value);
	}

	/**
	 * Equivalent to new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorGreaterThanOrEqualTo, value);
	 * 
	 * @param key the key
	 * @param value the value
	 * @return an EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier greaterThanOrEqualTo(String key, Object value) {
		return new EOKeyValueQualifier(key, ERXQ.GTEQ, value);
	}

	/**
	 * Equivalent to new EONotQualifier(qualifier);
	 * 
	 * @param qualifier the qualifier to not
	 * @return an EONotQualifier
	 */
	public static EONotQualifier not(EOQualifier qualifier) {
		return new EONotQualifier(qualifier);
	}

	/**
	 * Equivalent to new EOKeyValueQualifier(key, operator, value);
	 * 
	 * @param key the key
	 * @param operator ERXQ.EQ, NE, GT, LT, etc
	 * @param value the value
	 * @return an EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier compare(String key, NSSelector operator, Object value) {
		return new EOKeyValueQualifier(key, operator, value);
	}

	/**
	 * Equivalent to a new EOOrQualifier of EOKeyValueQualifier with key equals value for each value.
	 * 
	 * @param key the key
	 * @param values the values
	 * @return an EOQualifier
	 */
	public static EOQualifier in(String key, Object... values) {
		NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
		for (Object value : values) {
			qualifiers.addObject(ERXQ.equals(key, value));
		}
		return new EOOrQualifier(qualifiers);
	}

	/**
	 * Equivalent to a new EOOrQualifier of EOKeyValueQualifier with key equals value for each value.
	 * 
	 * @param key the key
	 * @param values the values
	 * @return an EOQualifier
	 */
	public static EOQualifier in(String key, NSArray<?> values) {
		NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
		Enumeration valuesEnum = values.objectEnumerator();
		while (valuesEnum.hasMoreElements()) {
			Object value = valuesEnum.nextElement();
			qualifiers.addObject(ERXQ.equals(key, value));
		}
		return new EOOrQualifier(qualifiers);
	}

	/**
	 * Equivalent to key > lowerBound and key < upperBound (exclusive).  Not that this
	 * does not return an ERXBetweenQualifier.
	 * 
	 * @param key the key
	 * @param lowerBound the lower bound value
	 * @param upperBound the upper bound value
	 * @return the qualifier
	 */
	public static EOQualifier between(String key, Object lowerBound, Object upperBound) {
		return ERXQ.between(key, lowerBound, upperBound, false);
	}

	/**
	 * Equivalent to key >= lowerBound and key <­ upperBound (inclusive).  Not that this
	 * does not return an ERXBetweenQualifier.
	 * 
	 * @param key the key
	 * @param lowerBound the lower bound value
	 * @param upperBound the upper bound value
	 * @return the qualifier
	 */
	public static EOQualifier between(String key, Object lowerBound, Object upperBound, boolean inclusive) {
		EOKeyValueQualifier lowerQ = null;
		EOKeyValueQualifier upperQ = null;

		if (inclusive) {
			if (lowerBound != null) {
				lowerQ = ERXQ.greaterThanOrEqualTo(key, lowerBound);
			}
			if (upperBound != null) {
				upperQ = ERXQ.lessThanOrEqualTo(key, upperBound);
			}
		}
		else {
			if (lowerBound != null) {
				lowerQ = ERXQ.greaterThan(key, lowerBound);
			}
			if (upperBound != null) {
				upperQ = ERXQ.lessThan(key, upperBound);
			}
		}

		if (lowerQ == null && upperQ == null) {
			return null;
		}
		if (lowerQ == null) {
			return upperQ;
		}
		if (upperQ == null) {
			return lowerQ;
		}
		return ERXQ.and(lowerQ, upperQ);
	}

	/**
	 * Equivalent to new EOKeyValueQualifier(key, EOQualifier.OperatorCaseInsensitiveLike, "*" + value + "*").
	 * 
	 * @param key the key
	 * @param value the substring value
	 * @return an EOKeyValueQualifier
	 */
	public static EOKeyValueQualifier contains(String key, String value) {
		value = "*" + value + "*";
		return ERXQ.likeInsensitive(key, value);
	}
}
