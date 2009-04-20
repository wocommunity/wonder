package er.extensions.eof;

import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.qualifiers.ERXAndQualifier;
import er.extensions.qualifiers.ERXKeyValueQualifier;
import er.extensions.qualifiers.ERXOrQualifier;

/**
 * <p>
 * ERXKey provides a rich wrapper around a keypath. When combined with chainable
 * qualifiers, ERXKey provides a starting point for the qualifier chain. As an
 * example:
 * </p>
 * 
 * <pre>
 * &lt;code&gt;
 * public class Person extends ERXGenericRecord {
 *   ...
 *   public static final ERXKey&lt;Country&gt; country = new ERXKey&lt;Country&gt;(Person.COUNTRY_KEY);
 *   public static final ERXKey&lt;NSTimestamp&gt; birthDate = new ERXKey&lt;NSTimestamp&gt;(Person.BIRTH_DATE_KEY);
 *   ...
 * }
 * 
 *   Country germany = ...;
 *   NSTimestamp someRandomDate = ...;
 *   EOQualifier qualifier = Person.country.is(germany).and(Person.birthDate.after(someRandomDate));
 * &lt;/code&gt;
 * </pre>
 * 
 * @param <T> the type of the value of this key
 *  
 * @author mschrag
 */
public class ERXKey<T> {
	/**
	 * Enums to desribe the type of key this represents.
	 * 
	 * @author mschrag
	 */
	public static enum Type {
		Attribute, ToOneRelationship, ToManyRelationship
	}
	
	public interface ValueCoding {
		public <T> T valueForKey(ERXKey<T> key);

		public <T> void takeValueForKey(Object value, ERXKey<T> key);
	}

	private String _key;

	/**
	 * Constructs an ERXKey.
	 * 
	 * @param key
	 *            the underlying keypath
	 */
	public ERXKey(String key) {
		_key = key;
	}

	/**
	 * Equivalent to ERXS.asc(key())
	 * 
	 * @return asc sort ordering for key
	 */
	public ERXSortOrdering asc() {
		return ERXS.asc(key());
	}

	/**
	 * Equivalent to ERXS.ascs(key())
	 * 
	 * @return asc sort ordering for key
	 */
	public ERXSortOrdering.ERXSortOrderings ascs() {
		return ERXS.ascs(key());
	}

	/**
	 * Equivalent to ERXS.desc(key())
	 * 
	 * @return desc sort ordering for key
	 */
	public ERXSortOrdering desc() {
		return ERXS.desc(key());
	}

	/**
	 * Equivalent to ERXS.descs(key())
	 * 
	 * @return desc sort ordering for key
	 */
	public ERXSortOrdering.ERXSortOrderings descs() {
		return ERXS.descs(key());
	}

	/**
	 * Equivalent to ERXS.ascInsensitive(key())
	 * 
	 * @return ascInsensitive sort ordering for key
	 */
	public ERXSortOrdering ascInsensitive() {
		return ERXS.ascInsensitive(key());
	}

	/**
	 * Equivalent to ERXS.ascInsensitives(key())
	 * 
	 * @return ascInsensitive sort ordering for key
	 */
	public ERXSortOrdering.ERXSortOrderings ascInsensitives() {
		return ERXS.ascInsensitives(key());
	}

	/**
	 * Equivalent to ERXS.descInsensitive(key())
	 * 
	 * @return descInsensitive sort ordering for key
	 */
	public ERXSortOrdering descInsensitive() {
		return ERXS.descInsensitive(key());
	}

	/**
	 * Equivalent to ERXS.descInsensitives(key())
	 * 
	 * @return descInsensitive sort ordering for key
	 */
	public ERXSortOrdering.ERXSortOrderings descInsensitives() {
		return ERXS.descInsensitives(key());
	}

	/**
	 * Returns the keypath that this ERXKey represents.
	 * 
	 * @return the keypath that this ERXKey represents
	 */
	public String key() {
		return _key;
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorEqual, Boolean.TRUE);
	 * 
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier isTrue() {
		return ERXQ.isTrue(_key);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorEqual, Boolean.FALSE);
	 * 
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier isFalse() {
		return ERXQ.isFalse(_key);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorEqual, value) only if the value is not null.
	 * If the value is null, this will return null, allowing you to
	 * conditionally chain an equals only if the value is non-null.
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier or null
	 */
	public ERXKeyValueQualifier isUnlessNull(T value) {
		return (value == null) ? null : is(value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorEqual, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier is(T value) {
		return ERXQ.equals(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorEqual, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier eq(T value) {
		return ERXQ.equals(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorNotEqual, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier isNot(T value) {
		return ERXQ.notEquals(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorNotEqual, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier ne(T value) {
		return ERXQ.notEquals(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorGreaterThan, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier greaterThan(T value) {
		return ERXQ.greaterThan(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorGreaterThan, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier gt(T value) {
		return ERXQ.greaterThan(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorLessThan, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier lessThan(T value) {
		return ERXQ.lessThan(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorLessThan, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier lt(T value) {
		return ERXQ.lessThan(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorGreaterThanOrEqualTo, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier greaterThanOrEqualTo(T value) {
		return ERXQ.greaterThanOrEqualTo(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorGreaterThanOrEqualTo, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier gte(T value) {
		return ERXQ.greaterThanOrEqualTo(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorLessThanOrEqualTo, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier lessThanOrEqualTo(T value) {
		return ERXQ.lessThanOrEqualTo(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorLessThanOrEqualTo, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier lte(T value) {
		return ERXQ.lessThanOrEqualTo(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorLike, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier like(String value) {
		return ERXQ.like(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorCaseInsensitiveLike, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier likeInsensitive(String value) {
		return ERXQ.likeInsensitive(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorCaseInsensitiveLike, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier ilike(String value) {
		return ERXQ.likeInsensitive(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorEqual, null);
	 * 
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier isNull() {
		return ERXQ.isNull(_key);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorNotEqual, null);
	 * 
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier isNotNull() {
		return ERXQ.isNotNull(_key);
	}

	/**
	 * Equivalent to a new ERXOrQualifier of EOKeyValueQualifier with key equals
	 * value for each value.
	 * 
	 * @param values
	 *            the values
	 * @return an ERXOrQualifier
	 */
	public ERXOrQualifier inObjects(T... values) {
		return ERXQ.inObjects(_key, values);
	}

	/**
	 * Equivalent to a new ERXOrQualifier of EOKeyValueQualifier with key equals
	 * value for each value.
	 * 
	 * @param values
	 *            the values
	 * @return an ERXOrQualifier
	 */
	public ERXOrQualifier in(NSArray<T> values) {
		return ERXQ.in(_key, values);
	}

	/**
	 * Equivalent to a new ERXAndQualifier of
	 * EONotQualifier(EOKeyValueQualifier) with key equals value for each value.
	 * 
	 * @param values
	 *            the values
	 * @return an ERXAndQualifier
	 */
	public ERXAndQualifier notIn(NSArray<T> values) {
		return ERXQ.notIn(_key, values);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorLessThan, value);
	 * 
	 * @param when
	 *            the date to compare with
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier before(NSTimestamp when) {
		return ERXQ.lessThan(_key, when);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.QualifierOperatorGreaterThan, value);
	 * 
	 * @param when
	 *            the date to compare with
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier after(NSTimestamp when) {
		return ERXQ.greaterThan(_key, when);
	}

	/**
	 * Equivalent to key > lowerBound and key < upperBound (exclusive). Note
	 * that this does not return an ERXBetweenQualifier.
	 * 
	 * @param lowerBound
	 *            the lower bound value
	 * @param upperBound
	 *            the upper bound value
	 * @return the qualifier
	 */
	public EOQualifier between(T lowerBound, T upperBound) {
		return ERXQ.between(_key, lowerBound, upperBound);
	}

	/**
	 * Equivalent to key >= lowerBound and key <= upperBound (inclusive). Note
	 * that this does not return an ERXBetweenQualifier.
	 * 
	 * @param lowerBound
	 *            the lower bound value
	 * @param upperBound
	 *            the upper bound value
	 * @param inclusive
	 *            whether or not the between includes the endpoints
	 * @return the qualifier
	 */
	public EOQualifier between(T lowerBound, T upperBound, boolean inclusive) {
		return ERXQ.between(_key, lowerBound, upperBound, inclusive);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.OperatorCaseInsensitiveLike, "*" + value + "*").
	 * 
	 * @param value
	 *            the substring value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier contains(String value) {
		return ERXQ.contains(_key, value);
	}

	/**
	 * Returns a qualifier that evalutes to true when the value of the given key
	 * contains any of the given tokens (insensitively) in the search string.
	 * The search string will be tokenized by splitting on space characters.
	 * 
	 * @param tokens
	 *            a whitespace separated list of tokens to search for
	 * @return an ERXOrQualifier
	 */
	public ERXOrQualifier containsAny(String tokens) {
		return ERXQ.containsAny(_key, tokens);
	}

	/**
	 * Returns a qualifier that evalutes to true when the value of the given key
	 * contains any of the given tokens (insensitively).
	 * 
	 * @param tokens
	 *            the list of tokens to search for
	 * @return an ERXOrQualifier
	 */
	public ERXOrQualifier containsAny(String[] tokens) {
		return ERXQ.containsAny(_key, tokens);
	}

	/**
	 * Returns a qualifier that evalutes to true when the value of the given key
	 * contains all of the given tokens (insensitively) in the search string.
	 * The search string will be tokenized by splitting on space characters.
	 * 
	 * @param tokens
	 *            a whitespace separated list of tokens to search for
	 * @return an ERXAndQualifier
	 */
	public ERXAndQualifier containsAll(String tokens) {
		return ERXQ.containsAll(_key, tokens);
	}

	/**
	 * Returns a qualifier that evalutes to true when the value of the given key
	 * contains all of the given tokens (insensitively).
	 * 
	 * @param tokens
	 *            the list of tokens to search for
	 * @return an ERXAndQualifier
	 */
	public ERXAndQualifier containsAll(String[] tokens) {
		return ERXQ.containsAll(_key, tokens);
	}

	@Override
	public int hashCode() {
		return _key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ERXKey && ((ERXKey) obj)._key.equals(_key);
	}

	/**
	 * Returns a new ERXKey that appends the given key to this keypath. For
	 * instance, if this key is "person" and you add "firstName" to it, this
	 * will return a new ERXKey "person.firstName".
	 * 
	 * Note: ERXKey has a limitation that it will not return the proper generic
	 * type if you attempt to build a keypath extension of an NSArray. For
	 * instance,
	 * ERXKey&lt;NSArray&lt;Person&gt;&gt;.append(ERXKey&lt;String&gt;) will
	 * return ERXKey&lt;String&gt; when, in fact, it should be
	 * ERXKey&lt;NSArray&lt;String&gt;&gt;. This is a limitation due to type
	 * erasure with generics that we cannot currently resolve this problem.
	 * 
	 * @param <U> the type of the next key 
	 * 
	 * @param key
	 *            the key to append to this keypath
	 * @return the new appended key
	 */
	public <U> ERXKey<U> append(String key) {
		return new ERXKey<U>(_key + "." + key);
	}

	/**
	 * Calls append(key)
	 * 
	 * @param <U>
	 *            the key type
	 * @param key
	 *            the key to append to this keypath
	 * @return the new appended key
	 */
	public <U> ERXKey<U> dot(String key) {
		return append(key);
	}

	/**
	 * Returns a new ERXKey that appends the given key to this keypath. For
	 * instance, if this key is "person" and you add "firstName" to it, this
	 * will return a new ERXKey "person.firstName".
	 * 
	 * Note: ERXKey has a limitation that it will not return the proper generic
	 * type if you attempt to build a keypath extension of an NSArray. For
	 * instance,
	 * ERXKey&lt;NSArray&lt;Person&gt;&gt;.append(ERXKey&lt;String&gt;) will
	 * return ERXKey&lt;String&gt; when, in fact, it should be
	 * ERXKey&lt;NSArray&lt;String&gt;&gt;. This is a limitation due to type
	 * erasure with generics that we cannot currently resolve this problem.
	 * 
	 * @param <U> the type of the next key
	 * 
	 * @param key
	 *            the key to append to this keypath
	 * @return the new appended key
	 */
	@SuppressWarnings("unchecked")
	public <U> ERXKey<U> append(ERXKey<U> key) {
		return append(key.key());
	}

	/**
	 * Calls append(key)
	 * 
	 * @param <U>
	 *            the key type
	 * @param key
	 *            the key to append to this keypath
	 * @return the new appended key
	 */
	public <U> ERXKey<U> dot(ERXKey<U> key) {
		return append(key);
	}

	/**
	 * Returns a new ERXKey that appends the given key to this keypath. For
	 * instance, if this key is "person" and you add "firstName" to it, this
	 * will return a new ERXKey "person.firstName".
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * 		ERXKey&lt;String&gt; k = new ERXKey&lt;String&gt;(&quot;foo&quot;);
	 * 		ERXKey&lt;NSArray&lt;String&gt;&gt; a = new ERXKey&lt;NSArray&lt;String&gt;&gt;(&quot;foos&quot;);
	 * 		k = k.append(k);
	 * 		a = a.append(k);
	 * 		a = k.appendAsArray(k);
	 * 		k = k.appendAsArray(k);
	 * 		a = k.appendAsArray(a);
	 * 		a = a.appendAsArray(k);
	 * 		a = a.appendAsArray(a);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param <U> the type of the next key in the array 
	 * 
	 * @param key
	 *            the key to append to this keypath
	 * @return the new appended key
	 */
	@SuppressWarnings("unchecked")
	public <U> ERXKey<NSArray<U>> appendAsArray(ERXKey<U> key) {
		return append(key.key());
	}

	/**
	 * Calls appendAsArray(key).
	 * 
	 * @param <U>
	 *            the type
	 * @param key
	 *            the key to append to this keypath
	 * @return the new append to this keypath
	 */
	public <U> ERXKey<NSArray<U>> dotArray(ERXKey<U> key) {
		return append(key.key());
	}

	/**
	 * Returns the value of this keypath on the given object.
	 * 
	 * Note: If you ERXKey representation a keypath through an NSArray, this
	 * method will result in a ClassCastException. See the 'Note' on .append(..)
	 * for further explanation.
	 * 
	 * @param obj
	 *            the target object to apply this keypath on
	 * @return the value of the keypath on the target object
	 */
	@SuppressWarnings("unchecked")
	public T valueInObject(Object obj) {
		return (T) rawValueInObject(obj);
	}

	/**
	 * Returns the value of this keypath on the given object.
	 * 
	 * @param obj
	 *            the target object to apply this keypath on
	 * @return the value of the keypath on the target object
	 */
	public Object rawValueInObject(Object obj) {
		return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(obj, _key);
	}

	/**
	 * Returns the value of this keypath on the given object cast as an NSArray.
	 * 
	 * @param obj
	 *            the target object to apply this keypath on
	 * @return the value of the keypath on the target object
	 */
	@SuppressWarnings("unchecked")
	public NSArray<T> arrayValueInObject(Object obj) {
		return (NSArray<T>) rawValueInObject(obj);
	}

	/**
	 * Sets the value of this keypath on the given object.
	 * 
	 * @param value
	 *            the value to set
	 * @param obj
	 *            the object to set the value on
	 */
	public void takeValueInObject(T value, Object obj) {
		NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(obj, value, _key);
	}

	@Override
	public String toString() {
		return _key;
	}
}
