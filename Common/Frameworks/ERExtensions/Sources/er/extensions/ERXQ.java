package er.extensions;

import java.util.Enumeration;

import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;

import er.extensions.qualifiers.ERXAndQualifier;
import er.extensions.qualifiers.ERXKeyValueQualifier;
import er.extensions.qualifiers.ERXNotQualifier;
import er.extensions.qualifiers.ERXOrQualifier;

/**
 * <p>
 * ERXQ provides lots of much shorter methods of constructing EOQualifiers than
 * the very verbose style that you normally have to use. For instance ...
 * </p>
 * 
 * <code>
 * EOQualifier qualifier = new ERXAndQualifier(new NSArray(new Object[] { new ERXKeyValueQualifier("name", EOQualifier.QualifierOperatorsEquals, "Mike"), new ERXKeyValueQualifier("admin", EOQualifier.QualifierOperatorsEquals, Boolean.TRUE) }));
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
	 * EOQualfier)
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
	 * Equivalent to key >= lowerBound and key <­ upperBound (inclusive). Not
	 * that this does not return an ERXBetweenQualifier.
	 * 
	 * @param key
	 *            the key
	 * @param lowerBound
	 *            the lower bound value
	 * @param upperBound
	 *            the upper bound value
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
		String[] searchStrings = tokensWithWhitespace.split("\\s+");
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
		String[] searchStrings = tokensWithWhitespace.split("\\s+");
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
}
