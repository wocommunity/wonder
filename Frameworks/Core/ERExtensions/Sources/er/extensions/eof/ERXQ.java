package er.extensions.eof;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;

import er.extensions.eof.qualifiers.ERXInQualifier;
import er.extensions.eof.qualifiers.ERXRegExQualifier;
import er.extensions.eof.qualifiers.ERXToManyQualifier;
import er.extensions.qualifiers.ERXAndQualifier;
import er.extensions.qualifiers.ERXFalseQualifier;
import er.extensions.qualifiers.ERXKeyComparisonQualifier;
import er.extensions.qualifiers.ERXKeyValueQualifier;
import er.extensions.qualifiers.ERXNotQualifier;
import er.extensions.qualifiers.ERXOrQualifier;
import er.extensions.qualifiers.ERXTrueQualifier;

/**
 * ERXQ provides lots of much shorter methods of constructing EOQualifiers than
 * the very verbose style that you normally have to use. For instance ...
 *
 * <blockquote><pre>
   EOQualifier qualifier = new ERXAndQualifier(
            new NSArray(new Object[] {
                   new ERXKeyValueQualifier("name", EOQualifier.QualifierOperatorsEquals, "Mike"),
                   new ERXKeyValueQualifier("admin", EOQualifier.QualifierOperatorsEquals,
                   Boolean.TRUE) }));
   </pre></blockquote>
 *
 * <p>
 * ... becomes ...
 * </p>
 *
 * <blockquote><code>
   EOQualifier qualifier = ERXQ.and(ERXQ.equals("name", "Mike"), ERXQ.isTrue("admin"));
 * </code></blockquote>
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
	 * Equivalent to EOQualifier.filteredArrayWithQualifier(NSArray,
	 * EOQualifier)
	 *
	 * @param <T>
	 *            the type of the array
	 * @param array
	 *            the array to filter
	 * @param qualifier
	 *            the qualifier to filter with
	 * @return the filtered array
	 */
	@SuppressWarnings( { "cast", "unchecked" })
	public static <T> NSArray<T> filtered(NSArray<T> array, EOQualifier qualifier) {
		return (NSArray<T>) EOQualifier.filteredArrayWithQualifier(array, qualifier);
	}

	/**
	 * Equivalent to EOQualifier.filterArrayWithQualifier(NSMutableArray,
	 * EOQualifier)
	 *
	 * @param array
	 *            the array to filter (in place)
	 * @param qualifier
	 *            the qualifier to filter with
	 */
	public static void filter(NSMutableArray<?> array, EOQualifier qualifier) {
		EOQualifier.filterArrayWithQualifier(array, qualifier);
	}

	/**
	 * Returns the one object that matches the qualifier in the given array (or
	 * null if there is no match).
	 *
	 * @param <T>
	 *            the type of the objects
	 * @param array
	 *            the array to filter
	 * @param qualifier
	 *            the qualifier to filter on
	 * @return one matching object or null
	 * @throws IllegalStateException if more than one object matched
	 */
	public static <T> T one(NSArray<T> array, EOQualifier qualifier) {
		T object;
		if (array == null) {
			object = null;
		}
		else {
			NSArray<T> objects = ERXQ.filtered(array, qualifier);
			int count = objects.count();
			if (count == 0) {
				object = null;
			}
			else if (count == 1) {
				object = objects.lastObject();
			}
			else {
				throw new IllegalStateException("There was more than one object that matched the qualifier '" + qualifier + "'.");
			}
		}
		return object;
	}

	/**
	 * Returns the first object that matches the qualifier in the given array
	 * (or null if there is no match).
	 *
	 * @param <T>
	 *            the type of the objects
	 * @param array
	 *            the array to filter
	 * @param qualifier
	 *            the qualifier to filter on
	 * @return one matching object or null
	 * @throws IllegalStateException if more than one object matched
	 */
	public static <T> T first(NSArray<T> array, EOQualifier qualifier) {
		T object;
		if (array == null) {
			object = null;
		}
		else {
			NSArray<T> objects = ERXQ.filtered(array, qualifier);
			int count = objects.count();
			if (count == 0) {
				object = null;
			}
			else {
				object = objects.objectAtIndex(0);
			}
		}
		return object;
	}

	/**
	 * Returns the one object that matches the qualifier in the given array (or
	 * throws if there is no match).
	 *
	 * @param <T>
	 *            the type of the objects
	 * @param array
	 *            the array to filter
	 * @param qualifier
	 *            the qualifier to filter on
	 * @return one matching object
	 * @throws IllegalStateException if more than one object matched
	 * @throws NoSuchElementException if no objects matched
	 */
	public static <T> T requiredOne(NSArray<T> array, EOQualifier qualifier) {
		T object = ERXQ.one(array, qualifier);
		if (object == null) {
			throw new NoSuchElementException("There was no object that matched the qualifier '" + qualifier + "'.");
		}
		return object;
	}

	/**
	 * Equivalent to new ERXOrQualifier(new NSArray(qualifiersArray). Nulls are
	 * skipped.
	 *
	 * @param qualifiersArray
	 *            the array of qualifiers to or
	 * @return and EOOrQualifier
	 */
	public static ERXOrQualifier or(EOQualifier... qualifiersArray) {
		NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
		for (EOQualifier qualifier : qualifiersArray) {
			if (qualifier != null) {
				qualifiers.addObject(qualifier);
			}
		}
		return new ERXOrQualifier(qualifiers);
	}

	/**
	 * Equivalent to new ERXOrQualifier(new NSArray(qualifiersArray).
	 *
	 * @param qualifiers
	 *            the NSArray of qualifiers to or
	 * @return an ERXOrQualifier
	 */
	public static ERXOrQualifier or(NSArray<? extends EOQualifier> qualifiers) {
		return new ERXOrQualifier(qualifiers);
	}
	
	/**
	 * Equivalent to new ERXAndQualifier(new NSArray(qualifiersArray). Nulls are
	 * skipped.
	 *
	 * @param qualifiersArray
	 *            the array of qualifiers to and
	 * @return and EOAndQualifier
	 */
	public static ERXAndQualifier and(EOQualifier... qualifiersArray) {
		NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
		for (EOQualifier qualifier : qualifiersArray) {
			if (qualifier != null) {
				qualifiers.addObject(qualifier);
			}
		}
		return new ERXAndQualifier(qualifiers);
	}
	
	/**
	 * Equivalent to new ERXAndQualifier(new NSArray(qualifiersArray).
	 *
	 * @param qualifiers
	 *            the NSArray of qualifiers to and
	 * @return an ERXAndQualifier
	 */
	public static ERXAndQualifier and(NSArray<? extends EOQualifier> qualifiers) {
		return new ERXAndQualifier(qualifiers);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorLike, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier like(String key, Object value) {
		return new ERXKeyValueQualifier(key, ERXQ.LIKE, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorCaseInsensitiveLike, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier likeInsensitive(String key, Object value) {
		return new ERXKeyValueQualifier(key, ERXQ.ILIKE, value);
	}

	/**
	 * Equivalent to new ERXRegExQualifier(key, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier matches(String key, String value) {
		return new ERXRegExQualifier(key, value);
	}

	/**
	 * Equivalent to new ERXInQualifier(key, values);
	 *
	 * @param key
	 *            the key
	 * @param values
	 *            the values
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier hasValues(String key, NSArray values) {
		return new ERXInQualifier(key, values);
	}

	/**
	 * Equivalent to new ERXToManyQualifier(key, values);
	 *
	 * @param key
	 *            the key
	 * @param values
	 *            the values
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier has(String key, NSArray values) {
		return hasAtLeast(key, values, 0);
	}

	/**
	 * Equivalent to new ERXToManyQualifier(key, values);
	 *
	 * @param key
	 *            the key
	 * @param values
	 *            the values
	 * @param min
	 *            the minimum number of objects from values to match
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier hasAtLeast(String key, NSArray values, int min) {
		return new ERXToManyQualifier(key, values, min);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorEqual, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier equals(String key, Object value) {
		return new ERXKeyValueQualifier(key, ERXQ.EQ, value);
	}

	/**
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorEqual, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public static <T> ERXKeyComparisonQualifier equals(ERXKey<T> key, ERXKey<T> value) {
		return new ERXKeyComparisonQualifier(key.key(), ERXQ.EQ, value.key());
	}
	
	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorEqual, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier is(String key, Object value) {
		return ERXQ.equals(key, value);
	}

	/**
	 * Returns isNull or isNotNull depending on the value of yesOrNo.
	 *
	 * @param key
	 *            the key
	 * @param yesOrNo
	 *            if true, returns isNull, if false, returns isNotNull
	 * @return the EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier isNull(String key, boolean yesOrNo) {
		return (yesOrNo) ? ERXQ.isNull(key) : ERXQ.isNotNull(key);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorEqual, null);
	 *
	 * @param key
	 *            the key
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier isNull(String key) {
		return new ERXKeyValueQualifier(key, ERXQ.EQ, null);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorNotEqual, null);
	 *
	 * @param key
	 *            the key
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier isNotNull(String key) {
		return new ERXKeyValueQualifier(key, ERXQ.NE, null);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorEqual, Boolean.TRUE);
	 *
	 * @param key
	 *            the key
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier isTrue(String key) {
		return new ERXKeyValueQualifier(key, ERXQ.EQ, Boolean.TRUE);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorEqual, Boolean.FALSE);
	 *
	 * @param key
	 *            the key
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier isFalse(String key) {
		return new ERXKeyValueQualifier(key, ERXQ.EQ, Boolean.FALSE);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorNotEqual, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier notEquals(String key, Object value) {
		return new ERXKeyValueQualifier(key, ERXQ.NE, value);
	}

	/**
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorNotEqual, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public static <T >ERXKeyComparisonQualifier notEquals(ERXKey<T> key, ERXKey<T> value) {
		return new ERXKeyComparisonQualifier(key.key(), ERXQ.NE, value.key());
	}
	
	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorLessThan, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier lessThan(String key, Object value) {
		return new ERXKeyValueQualifier(key, ERXQ.LT, value);
	}

	/**
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorLessThan, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */	
	public static <T> ERXKeyComparisonQualifier lessThan(ERXKey<T> key, ERXKey<T> value) {
		return new ERXKeyComparisonQualifier(key.key(), ERXQ.LT, value.key());
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorGreaterThan, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier greaterThan(String key, Object value) {
		return new ERXKeyValueQualifier(key, ERXQ.GT, value);
	}

	/**
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorGreaterThan, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public static <T> ERXKeyComparisonQualifier greaterThan(ERXKey<T> key, ERXKey<T> value) {
		return new ERXKeyComparisonQualifier(key.key(), ERXQ.GT, value.key());
	}
	
	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorLessThanOrEqualTo, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier lessThanOrEqualTo(String key, Object value) {
		return new ERXKeyValueQualifier(key, ERXQ.LTEQ, value);
	}

	/**
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorLessThanOrEqualTo, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public static <T> ERXKeyComparisonQualifier lessThanOrEqualTo(ERXKey<T> key, ERXKey<T> value) {
		return new ERXKeyComparisonQualifier(key.key(), ERXQ.LTEQ, value.key());
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorGreaterThanOrEqualTo, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier greaterThanOrEqualTo(String key, Object value) {
		return new ERXKeyValueQualifier(key, ERXQ.GTEQ, value);
	}

	/**
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorGreaterThanOrEqualTo, value);
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public static <T> ERXKeyComparisonQualifier greaterThanOrEqualTo(ERXKey<T> key, ERXKey<T> value) {
		return new ERXKeyComparisonQualifier(key.key(), ERXQ.GTEQ, value.key());
	}

	/**
	 * Equivalent to new ERXNotQualifier(qualifier);
	 *
	 * @param qualifier
	 *            the qualifier to not
	 * @return an EONotQualifier
	 */
	public static ERXNotQualifier not(EOQualifier qualifier) {
		return new ERXNotQualifier(qualifier);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key, operator, value);
	 *
	 * @param key
	 *            the key
	 * @param operator
	 *            ERXQ.EQ, NE, GT, LT, etc
	 * @param value
	 *            the value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier compare(String key, NSSelector operator, Object value) {
		return new ERXKeyValueQualifier(key, operator, value);
	}

	/**
	 * Equivalent to a new ERXOrQualifier of EOKeyValueQualifier with key equals
	 * value for each value.
	 *
	 * @param key
	 *            the key
	 * @param values
	 *            the values
	 * @return an EOQualifier
	 */
	public static ERXOrQualifier inObjects(String key, Object... values) {
		NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
		for (Object value : values) {
			qualifiers.addObject(ERXQ.equals(key, value));
		}
		return new ERXOrQualifier(qualifiers);
	}

	/**
	 * Equivalent to a new ERXAndQualifier of
	 * EONotQualifier(EOKeyValueQualifier) with key equals value for each value.
	 *
	 * @param key
	 *            the key
	 * @param values
	 *            the values
	 * @return an EOQualifier
	 */
	public static ERXAndQualifier notInObjects(String key, Object... values) {
		NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
		for (Object value : values) {
			qualifiers.addObject(ERXQ.notEquals(key, value));
		}
		return new ERXAndQualifier(qualifiers);
	}

	/**
	 * Equivalent to a new ERXOrQualifier of EOKeyValueQualifier with key equals
	 * value for each value.
	 *
	 * @param key
	 *            the key
	 * @param values
	 *            the values
	 * @return an EOQualifier
	 */
	public static ERXOrQualifier in(String key, NSArray<?> values) {
		if(values.count() == 0) {
			return new ERXOrQualifier(new NSArray<EOQualifier>(new ERXFalseQualifier()));
		}
		NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
		Enumeration valuesEnum = values.objectEnumerator();
		while (valuesEnum.hasMoreElements()) {
			Object value = valuesEnum.nextElement();
			qualifiers.addObject(ERXQ.equals(key, value));
		}
		return new ERXOrQualifier(qualifiers);
	}

	/**
	 * Equivalent to a new ERXAndQualifier of
	 * EONotQualifier(EOKeyValueQualifier) with key equals value for each value.
	 *
	 * @param key
	 *            the key
	 * @param values
	 *            the values
	 * @return an EOQualifier
	 */
	public static ERXAndQualifier notIn(String key, NSArray values) {
		NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
		Enumeration valuesEnum = values.objectEnumerator();
		while (valuesEnum.hasMoreElements()) {
			Object value = valuesEnum.nextElement();
			qualifiers.addObject(ERXQ.notEquals(key, value));
		}
		return new ERXAndQualifier(qualifiers);
	}

	/**
	 * Equivalent to key > lowerBound and key < upperBound (exclusive). Not that
	 * this does not return an ERXBetweenQualifier.
	 *
	 * @param key
	 *            the key
	 * @param lowerBound
	 *            the lower bound value
	 * @param upperBound
	 *            the upper bound value
	 * @return the qualifier
	 */
	public static EOQualifier between(String key, Object lowerBound, Object upperBound) {
		return ERXQ.between(key, lowerBound, upperBound, false);
	}

	/**
	 * Equivalent to key >= lowerBound and key <= upperBound (inclusive). Not
	 * that this does not return an ERXBetweenQualifier.
	 *
	 * @param key
	 *            the key
	 * @param lowerBound
	 *            the lower bound value
	 * @param upperBound
	 *            the upper bound value
	 * @param inclusive
	 *            if the lowerBound and upperBound should be inclusive
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
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.OperatorLike, value + "*").
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the substring value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier startsWith(String key, String value) {
		value = value + "*";
		return ERXQ.like(key, value);
	}
	
	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.OperatorCaseInsensitiveLike, value + "*").
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the substring value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier startsWithInsensitive(String key, String value) {
		value = value + "*";
		return ERXQ.likeInsensitive(key, value);
	}
	
	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.OperatorLike, "*" + value).
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the substring value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier endsWith(String key, String value) {
		value = "*" + value;
		return ERXQ.like(key, value);
	}
	
	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.OperatorCaseInsensitiveLike, "*" + value).
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the substring value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier endsWithInsensitive(String key, String value) {
		value = "*" + value;
		return ERXQ.likeInsensitive(key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key, EOQualifier.OperatorContains,
	 * value).
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier containsObject(String key, Object value) {
		return new ERXKeyValueQualifier(key, ERXQ.CONTAINS, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.OperatorCaseInsensitiveLike, "*" + value + "*").
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the substring value
	 * @return an EOKeyValueQualifier
	 */
	public static ERXKeyValueQualifier contains(String key, String value) {
		value = "*" + value + "*";
		return ERXQ.likeInsensitive(key, value);
	}

	/**
	 * Returns a qualifier that evalutes to true when the value of any of the
	 * given keys contains any of the given tokens (insensitively) in the search
	 * string. The search string will be tokenized by splitting on space
	 * characters.
	 *
	 * @param keys
	 *            the keys
	 * @param tokensWithWhitespace
	 *            a whitespace separated list of tokens to search for
	 * @return an ERXOrQualifier
	 */
	public static ERXOrQualifier containsAny(NSArray<String> keys, String tokensWithWhitespace) {
		NSMutableArray<ERXOrQualifier> qualifiers = new NSMutableArray<ERXOrQualifier>();
		for (String key : keys) {
			qualifiers.addObject(ERXQ.containsAny(key, tokensWithWhitespace));
		}
		return new ERXOrQualifier(qualifiers);
	}

	/**
	 * Returns a qualifier that evalutes to true when the value of the given key
	 * contains any of the given tokens (insensitively) in the search string.
	 * The search string will be tokenized by splitting on space characters.
	 *
	 * @param key
	 *            the key
	 * @param tokensWithWhitespace
	 *            a whitespace separated list of tokens to search for
	 * @return an ERXOrQualifier
	 */
	public static ERXOrQualifier containsAny(String key, String tokensWithWhitespace) {
		String[] searchStrings;
		if (tokensWithWhitespace == null) {
			searchStrings = new String[0];
		}
		else {
			searchStrings = tokensWithWhitespace.split("\\s+");
		}
		return ERXQ.containsAny(key, searchStrings);
	}

	/**
	 * Returns a qualifier that evalutes to true when the value of the given key
	 * contains any of the given tokens (insensitively).
	 *
	 * @param key
	 *            the key
	 * @param tokens
	 *            the list of tokens to search for
	 * @return an ERXOrQualifier
	 */
	public static ERXOrQualifier containsAny(String key, String[] tokens) {
		ERXOrQualifier qualifier;
		if (tokens.length == 0) {
			qualifier = null;
		}
		else {
			NSMutableArray<EOQualifier> searchQualifiers = new NSMutableArray<EOQualifier>();
			for (String token : tokens) {
				searchQualifiers.addObject(ERXQ.contains(key, token));
			}
			qualifier = new ERXOrQualifier(searchQualifiers);
		}
		return qualifier;
	}

	/**
	 * Returns a qualifier that evalutes to true when the value of any of the
	 * given keys contains all of the given tokens (insensitively) in the search
	 * string. The search string will be tokenized by splitting on space
	 * characters.
	 *
	 * @param keys
	 *            the keys
	 * @param tokensWithWhitespace
	 *            a whitespace separated list of tokens to search for
	 * @return an ERXOrQualifier
	 */
	public static ERXOrQualifier containsAll(NSArray<String> keys, String tokensWithWhitespace) {
		NSMutableArray<ERXAndQualifier> qualifiers = new NSMutableArray<ERXAndQualifier>();
		for (String key : keys) {
			qualifiers.addObject(ERXQ.containsAll(key, tokensWithWhitespace));
		}
		return new ERXOrQualifier(qualifiers);
	}

	/**
	 * Returns a qualifier that evalutes to true when the value of the given key
	 * contains all of the given tokens (insensitively) in the search string.
	 * The search string will be tokenized by splitting on space characters.
	 *
	 * @param key
	 *            the key
	 * @param tokensWithWhitespace
	 *            a whitespace separated list of tokens to search for
	 * @return an ERXAndQualifier
	 */
	public static ERXAndQualifier containsAll(String key, String tokensWithWhitespace) {
		String[] searchStrings;
		if (tokensWithWhitespace == null) {
			searchStrings = new String[0];
		}
		else {
			searchStrings = tokensWithWhitespace.split("\\s+");
		}
		return ERXQ.containsAll(key, searchStrings);
	}

	/**
	 * Returns a qualifier that evalutes to true when the value of the given key
	 * contains all of the given tokens (insensitively).
	 *
	 * @param key
	 *            the key
	 * @param tokens
	 *            the list of tokens to search for
	 * @return an ERXAndQualifier
	 */
	public static ERXAndQualifier containsAll(String key, String[] tokens) {
		ERXAndQualifier qualifier;
		if (tokens.length == 0) {
			qualifier = null;
		}
		else {
			NSMutableArray<EOQualifier> searchQualifiers = new NSMutableArray<EOQualifier>();
			for (String token : tokens) {
				searchQualifiers.addObject(ERXQ.contains(key, token));
			}
			qualifier = new ERXAndQualifier(searchQualifiers);
		}
		return qualifier;
	}

	/**
	 * Returns a qualifier that evaluates to true when all values in the given
	 * tokens are found when searching across any of the keypaths.  As an example, you
	 * could search for "Mike Schrag" across the keys (firstName, lastName) and it would
	 * find (firstName=Mike or lastName=Mike) and (firstName=Schrag or lastName=Schrag).
	 * Be careful of this one as it permutes quickly.
	 *
	 * @param keys
	 *            keypaths to perform search in
	 * @param tokensWithWhitespace
	 *            tokens to search for
	 * @return an ERXAndQualifier
	 */
	public static ERXAndQualifier containsAllInAny(String[] keys, String tokensWithWhitespace) {
		String[] searchStrings;
		if (tokensWithWhitespace == null) {
			searchStrings = new String[0];
		}
		else {
			searchStrings = tokensWithWhitespace.split("\\s+");
		}
		return ERXQ.containsAllInAny(keys, searchStrings);
	}

	/**
	 * Returns a qualifier that evaluates to true when all values in the given
	 * tokens are found when searching across any of the keypaths.  As an example, you
	 * could search for "Mike Schrag" across the keys (firstName, lastName) and it would
	 * find (firstName=Mike or lastName=Mike) and (firstName=Schrag or lastName=Schrag).
	 * Be careful of this one as it permutes quickly.
	 *
	 * @param keys
	 *            keypaths to perform search in
	 * @param tokens
	 *            tokens to search for
	 * @return an ERXAndQualifier
	 */
	public static ERXAndQualifier containsAllInAny(String[] keys, String[] tokens) {
		ERXAndQualifier qualifier;
		if (tokens.length == 0) {
			qualifier = null;
		}
		else {
			NSMutableArray<EOQualifier> searchQualifiers = new NSMutableArray<EOQualifier>();
			for (String token : tokens) {
				NSMutableArray<EOQualifier> tokenQualifiers = new NSMutableArray<EOQualifier>();
				for (String key : keys) {
					tokenQualifiers.addObject(ERXQ.contains(key, token));
				}
				searchQualifiers.addObject(new ERXOrQualifier(tokenQualifiers));
			}
			qualifier = new ERXAndQualifier(searchQualifiers);
		}
		return qualifier;
	}

	/**
	 * Generates a key path from a var args list of strings. Reduces this mess:
	 *
	 * <pre>
	 * qualifiers.addObject(ERXQ.equals(Distribution.PUBLICATION + &quot;.&quot; + Publication.AD + &quot;.&quot; + Ad.STATE, DisplayAdStateMachine.ReadyForPrinting));
	 * </pre>
	 *
	 * to:
	 *
	 * <pre>
	 * qualifiers.addObject(ERXQ.equals(ERXQ.keyPath(Distribution.PUBLICATION, Publication.AD, Ad.STATE, DisplayAdStateMachine.ReadyForPrinting));
	 * </pre>
	 *
	 * @param elements
	 *            one or more string to concatenate into a keyPath
	 * @return elements with "." between them to form a keypath
	 */
	public static String keyPath(String... elements) {
		return new NSArray<String>(elements).componentsJoinedByString(".");
	}
	
	/**
	 * Analyzes the given qualifier and returns all found {@link EOKeyValueQualifier} objects
	 * contained within.
	 *
	 * @param qualifier
	 *            qualifier to analyze
	 * @return array of found key value qualifiers
	 */
	public static NSArray<EOKeyValueQualifier> extractKeyValueQualifiers(EOQualifier qualifier) {
		if (qualifier == null) {
			return NSArray.EmptyArray;
		}
  		NSMutableArray<EOKeyValueQualifier> array = new NSMutableArray<EOKeyValueQualifier>();
  		_extractKeyValueQualifiers(array, qualifier);
  		return array;
  	}
	
	private static void _extractKeyValueQualifiers(NSMutableArray<EOKeyValueQualifier> array, EOQualifier qualifier) {
		if (qualifier instanceof EOKeyValueQualifier) {
			array.add((EOKeyValueQualifier) qualifier);
  		} else if (qualifier instanceof EOAndQualifier || qualifier instanceof EOOrQualifier) {
  			NSArray<EOQualifier> qualifiers;
  			if (qualifier instanceof EOAndQualifier) {
  				qualifiers = ((EOAndQualifier)qualifier).qualifiers();
  			} else {
  				qualifiers = ((EOOrQualifier)qualifier).qualifiers();
  			}
  			for (EOQualifier item : qualifiers) {
  				_extractKeyValueQualifiers(array, item);
  			}
  		} else if (qualifier instanceof EONotQualifier) {
  			_extractKeyValueQualifiers(array, ((EONotQualifier)qualifier).qualifier());
  		}
	}
	
	/**
	 * Takes a qualifier and searches for the given qualifier within to replace it with another one.
	 *
	 * @param qualifier
	 *            the qualifier to modify
	 * @param searchFor
	 *            the qualifier to replace
	 * @param replaceWith
	 *            the qualifier that replaces the searched one
	 * @return modified qualifier
	 */
	public static EOQualifier replaceQualifierWithQualifier(EOQualifier qualifier, EOQualifier searchFor, EOQualifier replaceWith) {
		if (qualifier == null || searchFor == null) {
			throw new IllegalStateException("The params qualifier and searchFor must not be null!");
		}
		if (replaceWith == null) {
			replaceWith = new ERXTrueQualifier();
		}
  		EOQualifier result = qualifier;
  		if (qualifier.equals(searchFor)) {
  			result = replaceWith;
  		} else if (qualifier instanceof EOAndQualifier) {
  			NSMutableArray<EOQualifier> array = new NSMutableArray<EOQualifier>();
  			for (EOQualifier item : ((EOAndQualifier)qualifier).qualifiers()) {
  				array.add(replaceQualifierWithQualifier(item, searchFor, replaceWith));
  			}
  			result = new EOAndQualifier(array);
  		} else if (qualifier instanceof EOOrQualifier) {
  			NSMutableArray<EOQualifier> array = new NSMutableArray<EOQualifier>();
  			for (EOQualifier item : ((EOOrQualifier)qualifier).qualifiers()) {
  				array.add(replaceQualifierWithQualifier(item, searchFor, replaceWith));
  			}
  			result = new EOOrQualifier(array);
  		} else if (qualifier instanceof EONotQualifier) {
  			EOQualifier replacedQualifier = replaceQualifierWithQualifier(((EONotQualifier)qualifier).qualifier(), searchFor, replaceWith);
  			result = new EONotQualifier(replacedQualifier);
  		}
		return result;
	}

	/**
	 * Inspired by EOUtilities.objectsWithValues method, creates EOKeyValueQualifiers for a variable-length list of parameters,
	 * combines these qualifier into an EOAndQualifier and returns that qualifier. Values can be a dictionary, in which case the
	 * values are added to the EOQualifier list as EOKeyValueQualifier objects. Values can be an EOQualifier, which just gets included
	 * in the list of EOQualifiers. When String-Object pairs or ERXKey-Object pairs appear in the list, they are turned into an
	 * EOKeyValueQualifier and added to the list.
         * <p>
	 * An IllegalArgumentException is thrown if objects are not of the right type or if a bad sequence is used, such as a sequence of
	 * String-Object-Object or String-String-String.
	 * <p>
	 * See er.extensions.eof.ERXEOControlUtilitiesTest in the ERXTest project for details.
	 * <p>
	 * @param values a list of objects that can be used to create an EOQualifier. An NSDictionary or EOQualifier can stand alone in the
	 *               list. A String or ERXKey must be followed by an Object.
	 */
	public static EOQualifier matchingValues(Object... values) {

		NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();

		int idx = 0;
		while (idx < values.length) {
			if (values[idx] instanceof NSDictionary) {
				NSDictionary<String,Object> theseValues = (NSDictionary<String,Object>)values[idx];
				for (String aKey : theseValues.allKeys()) {
					qualifiers.add(new EOKeyValueQualifier(aKey, EOQualifier.QualifierOperatorEqual, theseValues.objectForKey(aKey)));
				}
				idx++;
				continue;
			}
			if (values[idx] instanceof EOQualifier) {
				qualifiers.add((EOQualifier)values[idx]);
				idx++;
				continue;
			}
			if (values[idx] instanceof String || values[idx] instanceof ERXKey) {
				if ((idx+1) < values.length) {
					if (values[idx] instanceof String)
						qualifiers.add(new EOKeyValueQualifier((String)values[idx], EOQualifier.QualifierOperatorEqual, values[idx+1]));
					else
						qualifiers.add(((ERXKey)values[idx]).is(values[idx+1]));
				} else
					throw new IllegalArgumentException("Parameters to matchingValues did not match allowed sequence of objects. List of values is length "+values.length+".");
				idx += 2;
				continue;
			}
			throw new IllegalArgumentException("Parameters to matchingValues did not match allowed sequence of objects. Incorrect class for parameter # "+idx+".");
		}
		return (qualifiers.size() == 0) ? null : ((qualifiers.size() == 1) ? qualifiers.get(0) : new EOAndQualifier(qualifiers));
	}
}

