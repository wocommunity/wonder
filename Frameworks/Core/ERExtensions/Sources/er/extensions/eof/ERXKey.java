package er.extensions.eof;

import java.math.BigDecimal;

import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.qualifiers.ERXAndQualifier;
import er.extensions.qualifiers.ERXKeyComparisonQualifier;
import er.extensions.qualifiers.ERXKeyValueQualifier;
import er.extensions.qualifiers.ERXOrQualifier;
import er.extensions.qualifiers.ERXPrefixQualifierTraversal;

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
	/* Constants for known NSArray keypath operators */
	private static final ERXKey<BigDecimal> AVG = new ERXKey<BigDecimal>("@avg");
	private static final ERXKey<BigDecimal> SUM = new ERXKey<BigDecimal>("@sum");
	private static final ERXKey<?> MIN = new ERXKey<Object>("@min");
	private static final ERXKey<?> MAX = new ERXKey<Object>("@max");
	private static final ERXKey<Integer> COUNT = new ERXKey<Integer>("@count");
		
	/* Constants for Wonder keypath operators */
	private static final ERXKey<BigDecimal> AVG_NON_NULL = new ERXKey<BigDecimal>("@avgNonNull");
	private static final ERXKey<?> FETCH_SPEC = new ERXKey<Object>("@fetchSpec");
	private static final ERXKey<?> FLATTEN = new ERXKey<Object>("@flatten");
	private static final ERXKey<Boolean> IS_EMPTY = new ERXKey<Boolean>("@isEmpty");
	private static final ERXKey<?> LIMIT = new ERXKey<Object>("@limit");
	private static final ERXKey<BigDecimal> MEDIAN = new ERXKey<BigDecimal>("@median");
	private static final ERXKey<?> OBJECT_AT_INDEX = new ERXKey<Object>("@objectAtIndex");
	private static final ERXKey<BigDecimal> POP_STD_DEV = new ERXKey<BigDecimal>("@popStdDev");
	private static final ERXKey<?> REMOVE_NULL_VALUES = new ERXKey<Object>("@removeNullValues");
	private static final ERXKey<?> REVERSE = new ERXKey<Object>("@reverse");
	private static final ERXKey<?> SORT = new ERXKey<Object>("@sort");
	private static final ERXKey<?> SORT_ASC = new ERXKey<Object>("@sortAsc");
	private static final ERXKey<?> SORT_DESC = new ERXKey<Object>("@sortDesc");
	private static final ERXKey<?> SORT_INSENSITIVE_ASC = new ERXKey<Object>("@sortInsensitiveAsc");
	private static final ERXKey<?> SORT_INSENSITIVE_DESC = new ERXKey<Object>("@sortInsensitiveDesc");
	private static final ERXKey<BigDecimal> STD_DEV = new ERXKey<BigDecimal>("@stdDev");
	private static final ERXKey<?> SUBARRAY_WITH_RANGE = new ERXKey<Object>("@subarrayWithRange");
	private static final ERXKey<?> UNIQUE = new ERXKey<Object>("@unique");

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' avgNonNull aggregate operator @avgNonNull. For
	 * instance, if the key is "price" this will return a new ERXKey "@avgNonNull.price".
	 * 
	 * @param key
	 *            the key to use for this aggregate keypath
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.AvgNonNullOperator AvgNonNullOperator
	 */
	public static ERXKey<BigDecimal> avgNonNull(ERXKey<?> key) {
		return (ERXKey<BigDecimal>) avgNonNull().append(key);
	}
	
	/**
	 * Return ERXArrayUtilities' avgNonNull aggregate operator @avgNonNull.
	 * 
	 * @return the avgNonNull key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.AvgNonNullOperator AvgNonNullOperator
	 */
	public static ERXKey<BigDecimal> avgNonNull() {
		return AVG_NON_NULL;
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' avgNonNull aggregate operator @avgNonNull. For
	 * instance, if the key is "price" this will return a new ERXKey "@avgNonNull.price".
	 * 
	 * @param key
	 *            the key to use for this aggregate keypath
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.AvgNonNullOperator AvgNonNullOperator
	 */
	public ERXKey<BigDecimal> atAvgNonNull(ERXKey<?> key) {
		return append(ERXKey.avgNonNull(key));
	}

	/**
	 * Return a new ERXKey that appends ERXArrayUtilities' avgNonNull aggregate operator @avgNonNull.
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.AvgNonNullOperator AvgNonNullOperator
	 */
	public ERXKey<BigDecimal> atAvgNonNull() {
		return append(ERXKey.avgNonNull());
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * fetchSpec operator @fetchSpec. For instance, if the key is "price" and 
	 * fetchSpecName is "newHomes" this will return a new ERXKey "@fetchSpec.newHomes.price".
	 * @param fetchSpecName the fetchSpec name
	 * @param <U> the type of the next key
	 * 
	 * @param key
	 *            the key to use for this keypath
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.FetchSpecOperator FetchSpecOperator
	 */
	public static <U> ERXKey<NSArray<U>> fetchSpec(String fetchSpecName, ERXKey<U> key) {
		return FETCH_SPEC.append(fetchSpecName).appendAsArray(key);
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * fetchSpec operator @fetchSpec. For instance, if the key is "price" and 
	 * fetchSpecName is "newHomes" this will return a new ERXKey "@fetchSpec.newHomes.price".
	 * @param fetchSpecName the fetchSpec name
	 * @param <U> the type of the next key
	 * 
	 * @param key
	 *            the key to use for this keypath
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.FetchSpecOperator FetchSpecOperator
	 */
	public <U> ERXKey<NSArray<U>> atFetchSpec(String fetchSpecName, ERXKey<U> key) {
		return append(ERXKey.fetchSpec(fetchSpecName, key));
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * fetchSpec operator @fetchSpec. For instance, if the 
	 * fetchSpecName is "newHomes" this will return a new ERXKey "@fetchSpec.newHomes".
	 * @param fetchSpecName the fetchSpec name
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.FetchSpecOperator FetchSpecOperator
	 */
	public static <U> ERXKey<U> fetchSpec(String fetchSpecName) {
		return FETCH_SPEC.append(fetchSpecName);
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * fetchSpec operator @fetchSpec. For instance, if the 
	 * fetchSpecName is "newHomes" this will return a new ERXKey "@fetchSpec.newHomes".
	 * @param fetchSpecName the fetchSpec name
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.FetchSpecOperator FetchSpecOperator
	 */
	public <U> ERXKey<U> atFetchSpec(String fetchSpecName) {
		return (ERXKey<U>) append(ERXKey.fetchSpec(fetchSpecName));
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * flatten operator @flatten. For instance, if the key is "price"
	 *  this will return a new ERXKey "@flatten.price".
	 * @param key the key to use for this keypath
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.FlattenOperator FlattenOperator
	 */
	public static <U> ERXKey<NSArray<U>> flatten(ERXKey<U> key) {
		return FLATTEN.appendAsArray(key);
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * flatten operator @flatten. For instance, if the key is "price"
	 *  this will return a new ERXKey "@flatten.price".
	 * @param key the key to use for this keypath
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.FlattenOperator FlattenOperator
	 */
	public <U> ERXKey<NSArray<U>> atFlatten(ERXKey<U> key) {
		return append(ERXKey.flatten(key));
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * flatten operator @flatten.
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.FlattenOperator FlattenOperator
	 */
	public static <U> ERXKey<U> flatten() {
		return (ERXKey<U>) FLATTEN;
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * flatten operator @flatten.
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.FlattenOperator FlattenOperator
	 */
	public <U> ERXKey<U> atFlatten() {
		return (ERXKey<U>) append(ERXKey.flatten());
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * isEmpty operator @isEmpty. Since any keypath beyond @isEmpty is ignored, 
	 * only a no arg method is available.
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.IsEmptyOperator IsEmptyOperator
	 */
	public static ERXKey<Boolean> isEmpty() {
		return IS_EMPTY;
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * isEmpty operator @isEmpty. Since any keypath beyond @isEmpty is ignored, 
	 * only a no arg method is available.
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.IsEmptyOperator IsEmptyOperator
	 */
	public ERXKey<Boolean> atIsEmpty() {
		return append(ERXKey.isEmpty());
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * limit operator @limit. For instance, if the key is "price" and 
	 * limit is 3 this will return a new ERXKey "@limit.3.price".
	 * @param limit the maximum number of objects allowed by the limit
	 * @param <U> the type of the next key
	 * 
	 * @param key
	 *            the key to use for this keypath
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.LimitOperator LimitOperator
	 */
	public static <U> ERXKey<NSArray<U>> limit(Integer limit, ERXKey<U> key) {
		return LIMIT.append(limit.toString()).appendAsArray(key);
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * limit operator @limit. For instance, if the key is "price" and 
	 * limit is 3 this will return a new ERXKey "@limit.3.price".
	 * @param limit the maximum number of objects allowed by the limit
	 * @param <U> the type of the next key
	 * 
	 * @param key
	 *            the key to use for this keypath
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.LimitOperator LimitOperator
	 */
	public <U> ERXKey<NSArray<U>> atLimit(Integer limit, ERXKey<U> key) {
		return append(ERXKey.limit(limit , key));
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * limit operator @limit. For instance, if the limit is 3 this will return 
	 * a new ERXKey "@limit.3".
	 * 
	 * @param limit the maximum number of objects allowed by the limit
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.LimitOperator LimitOperator
	 */
	public static <U> ERXKey<U> limit(Integer limit) {
		return LIMIT.append(limit.toString());
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * limit operator @limit. For instance, if the limit is 3 this will return 
	 * a new ERXKey "@limit.3".
	 * 
	 * @param limit the maximum number of objects allowed by the limit
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.LimitOperator LimitOperator
	 */
	public <U> ERXKey<U> atLimit(Integer limit) {
		return (ERXKey<U>) append(ERXKey.limit(limit));
	}
	
	/**
	 * Return ERXArrayUtilities' median aggregate operator @median.
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.MedianOperator MedianOperator
	 */
	public static ERXKey<BigDecimal> median() {
		return MEDIAN;
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' median aggregate operator @median. For
	 * instance, if the key is "price" this will return a new ERXKey "@median.price".
	 * 
	 * @param key
	 *            the key to use for this aggregate keypath
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.MedianOperator MedianOperator
	 */
	public static ERXKey<BigDecimal> median(ERXKey<?> key) {
		return (ERXKey<BigDecimal>) MEDIAN.append(key);
	}
	
	/**
	 * Return a new ERXKey that appends the given key with ERXArrayUtilities' median aggregate operator @median.
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.MedianOperator MedianOperator
	 */
	public ERXKey<BigDecimal> atMedian() {
		return append(ERXKey.median());
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' median aggregate operator @median. For
	 * instance, if the key is "price" this will return a new ERXKey "@median.price".
	 * 
	 * @param key
	 *            the key to use for this aggregate keypath
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.MedianOperator MedianOperator
	 */
	public ERXKey<BigDecimal> atMedian(ERXKey<?> key) {
		return append(ERXKey.median(key));
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * objectAtIndex operator @objectAtIndex. For instance, if the index is 3 
	 * and the key is "price" then this will return a new ERXKey "@objectAtIndex.3.price".
	 * 
	 * @param <U> the type of the next key
	 * @param index The index of the object to return from the array
	 * @param key the key following the operator
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.ObjectAtIndexOperator ObjectAtIndexOperator
	 */
	public static <U> ERXKey<U> objectAtIndex(Integer index, ERXKey<U> key) {
		return OBJECT_AT_INDEX.append(index.toString()).append(key);
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * objectAtIndex operator @objectAtIndex. For instance, if the index is 3 
	 * and the key is "price" then this will return a new ERXKey "@objectAtIndex.3.price".
	 * 
	 * @param <U> the type of the next key
	 * @param index The index of the object to return from the array
	 * @param key the key following the operator
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.ObjectAtIndexOperator ObjectAtIndexOperator
	 */
	public <U> ERXKey<U> atObjectAtIndex(Integer index, ERXKey<U> key) {
		return append(ERXKey.objectAtIndex(index , key));
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * objectAtIndex operator @objectAtIndex. For instance, if the index is 3 
	 * then this will return a new ERXKey "@objectAtIndex.3".
	 * 
	 * @param <U> the type of the next key
	 * @param index The index of the object to return from the array
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.ObjectAtIndexOperator ObjectAtIndexOperator
	 */
	public static <U> ERXKey<U> objectAtIndex(Integer index) {
		return OBJECT_AT_INDEX.append(index.toString());
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * objectAtIndex operator @objectAtIndex. For instance, if the index is 3 
	 * then this will return a new ERXKey "@objectAtIndex.3".
	 * 
	 * @param <U> the type of the next key
	 * @param index The index of the object to return from the array
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.ObjectAtIndexOperator ObjectAtIndexOperator
	 */
	public <U> ERXKey<U> atObjectAtIndex(Integer index) {
		return (ERXKey<U>) append(ERXKey.objectAtIndex(index));
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * RemoveNullValues operator @removeNullValues. For instance, if the key is "price"
	 *  this will return a new ERXKey "@removeNullValues.price".
	 * @param key the key to use for this keypath
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.RemoveNullValuesOperator RemoveNullValuesOperator
	 */
	public static <U> ERXKey<U> removeNullValues(ERXKey<U> key) {
		return REMOVE_NULL_VALUES.append(key);
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * RemoveNullValues operator @removeNullValues. For instance, if the key is "price"
	 *  this will return a new ERXKey "@removeNullValues.price".
	 * @param key the key to use for this keypath
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.RemoveNullValuesOperator RemoveNullValuesOperator
	 */
	public <U> ERXKey<U> atRemoveNullValues(ERXKey<U> key) {
		return append(ERXKey.removeNullValues(key));
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * RemoveNullValues operator @removeNullValues.
	 * 
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.RemoveNullValuesOperator RemoveNullValuesOperator
	 */
	public static <U> ERXKey<U> removeNullValues() {
		return (ERXKey<U>) REMOVE_NULL_VALUES;
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * RemoveNullValues operator @removeNullValues.
	 * 
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.RemoveNullValuesOperator RemoveNullValuesOperator
	 */
	public <U> ERXKey<U> atRemoveNullValues() {
		return (ERXKey<U>) append(ERXKey.removeNullValues());
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * Reverse operator @reverse. For instance, if the key is "price"
	 *  this will return a new ERXKey "@reverse.price".
	 * @param key the key to use for this keypath
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.ReverseOperator ReverseOperator
	 */
	public static <U> ERXKey<NSArray<U>> reverse(ERXKey<U> key) {
		return REVERSE.appendAsArray(key);
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * Reverse operator @reverse. For instance, if the key is "price"
	 *  this will return a new ERXKey "@reverse.price".
	 * @param key the key to use for this keypath
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.ReverseOperator ReverseOperator
	 */
	public <U> ERXKey<NSArray<U>> atReverse(ERXKey<U> key) {
		return append(ERXKey.reverse(key));
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * Reverse operator @reverse.
	 * 
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.ReverseOperator ReverseOperator
	 */
	public static <U> ERXKey<U> reverse() {
		return (ERXKey<U>) REVERSE;
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * Reverse operator @reverse.
	 * 
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.ReverseOperator ReverseOperator
	 */
	public <U> ERXKey<U> atReverse() {
		return (ERXKey<U>) append(ERXKey.reverse());
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * Sort operator @sort.  For instance,
	 * <code>sort(Employee.FIRST_NAME, Employee.LAST_NAME)</code> 
	 * would return a key like @sort.firstName,lastname
	 * 
	 * @param sortKeys the ERXKeys to append for sorting
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.SortOperator SortOperator
	 */
	public static <U> ERXKey<U> sort(ERXKey<?> ... sortKeys) {
		NSArray<String> keyArray = (NSArray<String>) new NSArray<ERXKey<?>>(sortKeys).valueForKey("key");
		return SORT.append(keyArray.componentsJoinedByString(","));
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * Sort operator @sort.  For instance,
	 * <code>atSort(Employee.FIRST_NAME, Employee.LAST_NAME)</code> 
	 * would return a key like @sort.firstName,lastname
	 * 
	 * @param sortKeys the ERXKeys to append for sorting
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.SortOperator SortOperator
	 */
	public <U> ERXKey<U> atSort(ERXKey<?> ... sortKeys) {
		return (ERXKey<U>) append(ERXKey.sort(sortKeys));
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * SortAscending operator @sortAsc.  For instance,
	 * <code>sortAsc(Employee.FIRST_NAME, Employee.LAST_NAME)</code> 
	 * would return a key like @sortAsc.firstName,lastname
	 * 
	 * @param sortKeys the ERXKeys to append for sorting
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.SortOperator SortOperator
	 */
	public static <U> ERXKey<U> sortAsc(ERXKey<?> ... sortKeys) {
		NSArray<String> keyArray = (NSArray<String>) new NSArray<ERXKey<?>>(sortKeys).valueForKey("key");
		return SORT_ASC.append(keyArray.componentsJoinedByString(","));
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * SortAsc operator @sortAsc.  For instance,
	 * <code>atSortAsc(Employee.FIRST_NAME, Employee.LAST_NAME)</code> 
	 * would return a key like @sortAsc.firstName,lastname
	 * 
	 * @param sortKeys the ERXKeys to append for sorting
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.SortOperator SortOperator
	 */
	public <U> ERXKey<U> atSortAsc(ERXKey<?> ... sortKeys) {
		return (ERXKey<U>) append(ERXKey.sortAsc(sortKeys));
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * SortDescending operator @sortDesc.  For instance,
	 * <code>sortDesc(Employee.FIRST_NAME, Employee.LAST_NAME)</code> 
	 * would return a key like @sortDesc.firstName,lastname
	 * 
	 * @param sortKeys the ERXKeys to append for sorting
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.SortOperator SortOperator
	 */
	public static <U> ERXKey<U> sortDesc(ERXKey<?> ... sortKeys) {
		NSArray<String> keyArray = (NSArray<String>) new NSArray<ERXKey<?>>(sortKeys).valueForKey("key");
		return SORT_DESC.append(keyArray.componentsJoinedByString(","));
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * SortDescending operator @sortDesc.  For instance,
	 * <code>atSortDesc(Employee.FIRST_NAME, Employee.LAST_NAME)</code> 
	 * would return a key like @sortDesc.firstName,lastname
	 * 
	 * @param sortKeys the ERXKeys to append for sorting
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.SortOperator SortOperator
	 */
	public <U> ERXKey<U> atSortDesc(ERXKey<?> ... sortKeys) {
		return (ERXKey<U>) append(ERXKey.sortDesc(sortKeys));
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * SortInsensitiveAscending operator @sortInsensitiveAsc.  For instance,
	 * <code>sortInsensitiveAsc(Employee.FIRST_NAME, Employee.LAST_NAME)</code> 
	 * would return a key like @sortInsensitiveAsc.firstName,lastname
	 * 
	 * @param sortKeys the ERXKeys to append for sorting
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.SortOperator SortOperator
	 */
	public static <U> ERXKey<U> sortInsensitiveAsc(ERXKey<?> ... sortKeys) {
		NSArray<String> keyArray = (NSArray<String>) new NSArray<ERXKey<?>>(sortKeys).valueForKey("key");
		return SORT_INSENSITIVE_ASC.append(keyArray.componentsJoinedByString(","));
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * SortInsensitiveAscending operator @sortInsensitiveAsc.  For instance,
	 * <code>atSortInsensitiveAsc(Employee.FIRST_NAME, Employee.LAST_NAME)</code> 
	 * would return a key like @sortInsensitiveAsc.firstName,lastname
	 * 
	 * @param sortKeys the ERXKeys to append for sorting
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.SortOperator SortOperator
	 */
	public <U> ERXKey<U> atSortInsensitiveAsc(ERXKey<?> ... sortKeys) {
		return (ERXKey<U>) append(ERXKey.sortInsensitiveAsc(sortKeys));
	}

	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * SortInsensitiveDescending operator @sortInsensitiveDesc.  For instance,
	 * <code>sortInsensitiveDesc(Employee.FIRST_NAME, Employee.LAST_NAME)</code> 
	 * would return a key like @sortInsensitiveDesc.firstName,lastname
	 * 
	 * @param sortKeys the ERXKeys to append for sorting
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.SortOperator SortOperator
	 */
	public static <U> ERXKey<U> sortInsensitiveDesc(ERXKey<?> ... sortKeys) {
		NSArray<String> keyArray = (NSArray<String>) new NSArray<ERXKey<?>>(sortKeys).valueForKey("key");
		return SORT_INSENSITIVE_DESC.append(keyArray.componentsJoinedByString(","));
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * SortInsensitiveDescending operator @sortInsensitiveDesc.  For instance,
	 * <code>atSortInsensitiveDesc(Employee.FIRST_NAME, Employee.LAST_NAME)</code> 
	 * would return a key like @sortInsensitiveDesc.firstName,lastname
	 * 
	 * @param sortKeys the ERXKeys to append for sorting
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.SortOperator SortOperator
	 */
	public <U> ERXKey<U> atSortInsensitiveDesc(ERXKey<?> ... sortKeys) {
		return (ERXKey<U>) append(ERXKey.sortInsensitiveDesc(sortKeys));
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * SubarrayWithRange operator @subarrayWithRange. For instance, if the key is "price"
	 * and the range is <code>new NSRange(4,2)</code> this will return a new ERXKey "@subarrayWithRange.4-2.price".
	 * @param key the key to use for this keypath
	 * @param range the range for the operator
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.SubarrayWithRangeOperator SubarrayWithRangeOperator
	 */
	public static <U> ERXKey<NSArray<U>> subarrayWithRange(NSRange range, ERXKey<U> key) {
		return SUBARRAY_WITH_RANGE.append(range.location() + "-" + range.length()).appendAsArray(key);
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * SubarrayWithRange operator @subarrayWithRange. For instance, if the key is "price"
	 * and the range is <code>new NSRange(4,2)</code> this will return a new ERXKey "@subarrayWithRange.4-2.price".
	 * @param key the key to use for this keypath
	 * @param range the range for the operator
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.SubarrayWithRangeOperator SubarrayWithRangeOperator
	 */
	public <U> ERXKey<NSArray<U>> atSubarrayWithRange(NSRange range, ERXKey<U> key) {
		return append(ERXKey.subarrayWithRange(range , key));
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * SubarrayWithRange operator @subarrayWithRange. For instance, if the range 
	 * is <code>new NSRange(4,2)</code> this will return a new 
	 * ERXKey "@subarrayWithRange.4-2.price".
	 * 
	 * @param range the range for the operator
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.SubarrayWithRangeOperator SubarrayWithRangeOperator
	 */
	public static <U> ERXKey<U> subarrayWithRange(NSRange range) {
		return SUBARRAY_WITH_RANGE.append(range.location() + "-" + range.length());
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * SubarrayWithRange operator @subarrayWithRange. For instance, if the range 
	 * is <code>new NSRange(4,2)</code> this will return a new 
	 * ERXKey "@subarrayWithRange.4-2.price".
	 * 
	 * @param range the range for the operator
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.SubarrayWithRangeOperator SubarrayWithRangeOperator
	 */
	public <U> ERXKey<U> atSubarrayWithRange(NSRange range) {
		return (ERXKey<U>) append(ERXKey.subarrayWithRange(range));
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * Unique operator @unique. For instance, if the key is "price"
	 * this will return a new ERXKey "@unique.price".
	 * @param key the key to use for this keypath
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.UniqueOperator UniqueOperator
	 */
	public static <U> ERXKey<NSArray<U>> unique(ERXKey<U> key) {
		return UNIQUE.appendAsArray(key);
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * Unique operator @unique. For instance, if the key is "price"
	 * this will return a new ERXKey "@unique.price".
	 * @param key the key to use for this keypath
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.UniqueOperator UniqueOperator
	 */
	public <U> ERXKey<NSArray<U>> atUnique(ERXKey<U> key) {
		return append(ERXKey.unique(key));
	}

	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * Unique operator @unique.
	 * 
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.UniqueOperator UniqueOperator
	 */
	public static <U> ERXKey<U> unique() {
		return (ERXKey<U>) UNIQUE;
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with ERXArrayUtilities' 
	 * Unique operator @unique.
	 *  
	 * @param <U> the type of the next key
	 * 
	 * @return the new appended key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.UniqueOperator UniqueOperator
	 */
	public <U> ERXKey<U> atUnique() {
		return (ERXKey<U>) append(ERXKey.unique());
	}

	/**
	 * Return a new ERXKey that prepends the given key with NSArray's SUM aggregate operator @sum. For
	 * instance, if the key is "price" this will return a new ERXKey "@sum.price".
	 * 
	 * @param key
	 *            the key to use for this aggregate keypath
	 * @return the new appended key
	 */
	public static ERXKey<BigDecimal> sum(ERXKey<?> key) {
		return (ERXKey<BigDecimal>) SUM.append(key);
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with NSArray's SUM aggregate operator @sum. For
	 * instance, if the key is "price" this will return a new ERXKey "@sum.price".
	 * 
	 * @param key
	 *            the key to use for this aggregate keypath
	 * @return the new appended key
	 */
	public ERXKey<BigDecimal> atSum(ERXKey<?> key) {
		return append(ERXKey.sum(key));
	}
	
	/**
	 * Return a new ERXKey that uses NSArray's SUM aggregate operator @sum.
	 * 
	 * @return the new key
	 */
	public ERXKey<BigDecimal> atSum() {
		return append(ERXKey.sum());
	}
	
	/**
	 * Return a new ERXKey that uses NSArray's SUM aggregate operator @sum.
	 * 
	 * @return the new key
	 */
	public static ERXKey<BigDecimal> sum() {
		return SUM;
	}
	
	/**
	 * Return a new ERXKey that uses Wonder's standard deviation operator @popStdDev
	 * 
	 * @return the new key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.StandardDeviationOperator StandardDeviationOperator
	 */
	public static ERXKey<BigDecimal> popStdDev() {
		return POP_STD_DEV;
	}
	
	/**
	 * Return a new ERXKey that uses Wonder's standard deviation operator @popStdDev
	 * 
	 * @return the new key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.StandardDeviationOperator StandardDeviationOperator
	 */
	public ERXKey<BigDecimal> atPopStdDev() {
		return append(ERXKey.popStdDev());
	}
	
	/**
	 * Return a new ERXKey that uses Wonder's standard deviation operator @popStdDev
	 * @param key the key to append
	 * 
	 * @return the new key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.StandardDeviationOperator StandardDeviationOperator
	 */
	public static ERXKey<BigDecimal> popStdDev(ERXKey<?> key) {
		return (ERXKey<BigDecimal>)popStdDev().append(key);
	}
	
	/**
	 * Return a new ERXKey that uses Wonder's standard deviation operator @popStdDev
	 * @param key the key to append
	 * 
	 * @return the new key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.StandardDeviationOperator StandardDeviationOperator
	 */
	public ERXKey<BigDecimal> atPopStdDev(ERXKey<?> key) {
		return append(ERXKey.popStdDev(key));
	}
	
	/**
	 * Return a new ERXKey that uses Wonder's standard deviation operator @stdDev
	 * 
	 * @return the new key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.StandardDeviationOperator StandardDeviationOperator
	 */
	public static ERXKey<BigDecimal> stdDev() {
		return STD_DEV;
	}
	
	/**
	 * Return a new ERXKey that uses Wonder's standard deviation operator @stdDev
	 * 
	 * @return the new key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.StandardDeviationOperator StandardDeviationOperator
	 */
	public ERXKey<BigDecimal> atStdDev() {
		return append(ERXKey.stdDev());
	}
	
	/**
	 * Return a new ERXKey that uses Wonder's standard deviation operator @stdDev
	 * @param key the key to append
	 * 
	 * @return the new key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.StandardDeviationOperator StandardDeviationOperator
	 */
	public static ERXKey<BigDecimal> stdDev(ERXKey<?> key) {
		return (ERXKey<BigDecimal>)stdDev().append(key);
	}
	
	/**
	 * Return a new ERXKey that uses Wonder's standard deviation operator @stdDev
	 * @param key the key to append
	 * 
	 * @return the new key
	 * 
	 * @see er.extensions.foundation.ERXArrayUtilities.StandardDeviationOperator StandardDeviationOperator
	 */
	public ERXKey<BigDecimal> atStdDev(ERXKey<?> key) {
		return append(ERXKey.stdDev(key));
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with NSArray's AVERAGE aggregate operator @avg. For
	 * instance, if the key is "price" this will return a new ERXKey "@avg.price".
	 * 
	 * @param key
	 *            the key to use for this aggregate keypath
	 * @return the new appended key
	 */
	public static ERXKey<BigDecimal> avg(ERXKey<?> key) {
		return (ERXKey<BigDecimal>) AVG.append(key);
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with NSArray's AVERAGE aggregate operator @avg. For
	 * instance, if the key is "price" this will return a new ERXKey "@avg.price".
	 * 
	 * @param key
	 *            the key to use for this aggregate keypath
	 * @return the new appended key
	 */
	public ERXKey<BigDecimal> atAvg(ERXKey<?> key) {
		return append(ERXKey.avg(key));
	}
	
	/**
	 * Return a new ERXKey that uses NSArray's AVERAGE aggregate operator @avg.
	 * 
	 * @return the new key
	 */
	public static ERXKey<BigDecimal> avg() {
		return AVG;
	}
	
	/**
	 * Return a new ERXKey that uses NSArray's AVERAGE aggregate operator @avg.
	 * 
	 * @return the new key
	 */
	public ERXKey<BigDecimal> atAvg() {
		return append(ERXKey.avg());
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with NSArray's MIN aggregate operator @min. For
	 * instance, if the key is "price" this will return a new ERXKey "@min.price".
	 * 
	 * @param <U> the type of the next key
	 * 
	 * @param key
	 *            the key to use for this aggregate keypath
	 * @return the new appended key
	 */
	public static <U> ERXKey<U> min(ERXKey<U> key) {
		return MIN.append(key);
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with NSArray's MIN aggregate operator @min. For
	 * instance, if the key is "price" this will return a new ERXKey "@min.price".
	 * 
	 * @param <U> the type of the next key
	 * 
	 * @param key
	 *            the key to use for this aggregate keypath
	 * @return the new appended key
	 */
	public <U> ERXKey<U> atMin(ERXKey<U> key) {
		return append(ERXKey.min(key));
	}
	
	/**
	 * Return a new ERXKey that uses NSArray's MIN aggregate operator @min.
	 * 
	 * @param <U> the type of the next key
	 *
	 * @return the new key
	 */
	public static <U> ERXKey<U> min() {
		return (ERXKey<U>) MIN;
	}
	
	/**
	 * Return a new ERXKey that uses NSArray's MIN aggregate operator @min.
	 * 
	 * @param <U> the type of the next key
	 *
	 * @return the new key
	 */
	public <U> ERXKey<U> atMin() {
		return (ERXKey<U>) append(ERXKey.min());
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with NSArray's MAX aggregate operator @max. For
	 * instance, if the key is "price" this will return a new ERXKey "@max.price".
	 * 
	 * @param <U> the type of the next key
	 * 
	 * @param key
	 *            the key to use for this aggregate keypath
	 * @return the new appended key
	 */
	public static <U> ERXKey<U> max(ERXKey<U> key) {
		return MAX.append(key);
	}
	
	/**
	 * Return a new ERXKey that prepends the given key with NSArray's MAX aggregate operator @max. For
	 * instance, if the key is "price" this will return a new ERXKey "@max.price".
	 * 
	 * @param <U> the type of the next key
	 * 
	 * @param key
	 *            the key to use for this aggregate keypath
	 * @return the new appended key
	 */
	public <U> ERXKey<U> atMax(ERXKey<U> key) {
		return append(ERXKey.max(key));
	}
	
	/**
	 * Return a new ERXKey that uses NSArray's MAX aggregate operator @max.
	 * 
	 * @param <U> the type of the next key
	 *
	 * @return the new key
	 */
	public static <U> ERXKey<U> max() {
		return (ERXKey<U>) MAX;
	}

	/**
	 * Return a new ERXKey that uses NSArray's MAX aggregate operator @max.
	 * 
	 * @param <U> the type of the next key
	 *
	 * @return the new key
	 */
	public <U> ERXKey<U> atMax() {
		return (ERXKey<U>) append(ERXKey.max());
	}

	/**
	 * Return a new ERXKey that uses NSArray's COUNT operator @count. Since any
	 * keypath beyond @count is ignored, only a no arg method is available.
	 * 
	 * @return the new key
	 */
	public static ERXKey<Integer> count() {
		return COUNT;
	}
	
	/**
	 * Return a new ERXKey that uses NSArray's COUNT operator @count. Since any
	 * keypath beyond @count is ignored, only a no arg method is available.
	 * 
	 * @return the new key
	 */
	public ERXKey<Integer> atCount() {
		return append(ERXKey.count());
	}
	
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
	 * Return the keypath that this ERXKey represents.
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
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorEqual, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public ERXKeyComparisonQualifier is(ERXKey<T> value) {
		return ERXQ.equals(this, value);
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
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorEqual, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public ERXKeyComparisonQualifier eq(ERXKey<T> value) {
		return ERXQ.equals(this, value);
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
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorNotEqual, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public ERXKeyComparisonQualifier isNot(ERXKey<T> value) {
		return ERXQ.notEquals(this, value);
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
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorNotEqual, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public ERXKeyComparisonQualifier ne(ERXKey<T> value) {
		return ERXQ.notEquals(this, value);
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
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorGreaterThan, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public ERXKeyComparisonQualifier greaterThan(ERXKey<T> value) {
		return ERXQ.greaterThan(this, value);
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
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorGreaterThan, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public ERXKeyComparisonQualifier gt(ERXKey<T> value) {
		return ERXQ.greaterThan(this, value);
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
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorLessThan, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public ERXKeyComparisonQualifier lessThan(ERXKey<T> value) {
		return ERXQ.lessThan(this, value);
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
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorLessThan, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public ERXKeyComparisonQualifier lt(ERXKey<T> value) {
		return ERXQ.lessThan(this, value);
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
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorGreaterThanOrEqualTo, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public ERXKeyComparisonQualifier greaterThanOrEqualTo(ERXKey<T> value) {
		return ERXQ.greaterThanOrEqualTo(this, value);
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
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorGreaterThanOrEqualTo, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public ERXKeyComparisonQualifier gte(ERXKey<T> value) {
		return ERXQ.greaterThanOrEqualTo(this, value);
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
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorLessThanOrEqualTo, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public ERXKeyComparisonQualifier lessThanOrEqualTo(ERXKey<T> value) {
		return ERXQ.lessThanOrEqualTo(this, value);
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
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorLessThanOrEqualTo, value);
	 * 
	 * @param value
	 *            the value
	 * @return an ERXKeyComparisonQualifier
	 */
	public ERXKeyComparisonQualifier lte(ERXKey<T> value) {
		return ERXQ.lessThanOrEqualTo(this, value);
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
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorLessThan, value);
	 * 
	 * @param when
	 *            the date to compare with
	 * @return an ERXKeyComparisonQualifier
	 */
	@SuppressWarnings("unchecked")
	public ERXKeyComparisonQualifier before(ERXKey<? extends NSTimestamp> when) {
		return ERXQ.lessThan((ERXKey)this, when);
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
	 * Equivalent to new ERXKeyComparisonQualifier(key,
	 * EOQualifier.QualifierOperatorGreaterThan, value);
	 * 
	 * @param when
	 *            the date to compare with
	 * @return an ERXKeyComparisonQualifier
	 */
	@SuppressWarnings("unchecked")
	public ERXKeyComparisonQualifier after(ERXKey<? extends NSTimestamp> when) {
		return ERXQ.greaterThan((ERXKey)this, when);
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
	 * EOQualifier.OperatorLike, value + "*").
	 * 
	 * @param value
	 *            the substring value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier startsWith(String value) {
		return ERXQ.startsWith(_key, value);
	}
	
	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.OperatorCaseInsensitiveLike, value + "*").
	 * 
	 * @param value
	 *            the substring value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier startsWithInsensitive(String value) {
		return ERXQ.startsWithInsensitive(_key, value);
	}

	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.OperatorLike, "*" + value).
	 * 
	 * @param value
	 *            the substring value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier endsWith(String value) {
		return ERXQ.endsWith(_key, value);
	}
	
	/**
	 * Equivalent to new ERXKeyValueQualifier(key,
	 * EOQualifier.OperatorCaseInsensitiveLike, "*" + value).
	 * 
	 * @param value
	 *            the substring value
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier endsWithInsensitive(String value) {
		return ERXQ.endsWithInsensitive(_key, value);
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
	 * Return a qualifier that evalutes to true when the value of the given key
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
	 * Return a qualifier that evalutes to true when the value of the given key
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
	 * Return a qualifier that evalutes to true when the value of the given key
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
	 * Return a qualifier that evalutes to true when the value of the given key
	 * contains all of the given tokens (insensitively).
	 * 
	 * @param tokens
	 *            the list of tokens to search for
	 * @return an ERXAndQualifier
	 */
	public ERXAndQualifier containsAll(String[] tokens) {
		return ERXQ.containsAll(_key, tokens);
	}
	
	/**
	 * Return a qualifier that evaluates to true when the given to many key
	 * contains the given object.
	 * <p>
	 * Equivalent to new ERXKeyValueQualifier(key, EOQualifier.OperatorContains,
	 * value).
	 * 
	 * @param obj
	 *            the object
	 * @return an EOKeyValueQualifier
	 */
	public ERXKeyValueQualifier containsObject(Object obj) {
		return ERXQ.containsObject(_key, obj);
	}
	
	/**
	 * Equivalent to new ERXInQualifier(key, values);
	 * 
	 * @param values
	 *            the values
	 * @return an ERXKeyValueQualifier
	 */
	public ERXKeyValueQualifier hasValues(NSArray<T> values) {
		return ERXQ.hasValues(_key, values);
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
	 * Return a new ERXKey that appends the given key to this keypath. For
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
		return new ERXKey<U>((_key != null && _key.length() != 0) ? _key + NSKeyValueCodingAdditions.KeyPathSeparator + key : key);
	}

	/**
	 * Call append(key)
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
	 * Return a new ERXKey that appends the given key to this keypath. For
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
	 * Call append(key)
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
	 * Return a new ERXKey that appends the given key to this keypath. For
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
	 * Call appendAsArray(key).
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
	 * Return the value of this keypath on the given object.
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
	 * Return the value of this keypath on the given object.
	 * 
	 * @param obj
	 *            the target object to apply this keypath on
	 * @return the value of the keypath on the target object
	 */
	public Object rawValueInObject(Object obj) {
		return NSKeyValueCodingAdditions.Utility.valueForKeyPath(obj, _key);
	}

	/**
	 * Return the value of this keypath on the given object cast as an NSArray.
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
	 * Set the value of this keypath on the given object.
	 * 
	 * @param value
	 *            the value to set
	 * @param obj
	 *            the object to set the value on
	 */
	public void takeValueInObject(T value, Object obj) {
		NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(obj, value, _key);
	}
	
	/**
	 * Prefix the keys in the given qualifier with this key. For example, if you have a qualifier on Company of name = 'mDT' 
	 * and you want to find Person eo's whose companies match that qualifier, you need to prefix all the keys in the qualifier 
	 * to be "company.whatever" (to go through the company relationship on Person) -- so in the example you would need 
	 * company.name = 'mDT'. Prefix provides a mechanism to do that. 
	 * 
	 * Person.COMPANY.prefix(ERXQ.is("name", "mDT")) is equivalent to ERXQ.is("company.name", "mDT")
	 * 
	 * @param qualifier the qualifier to prefix
	 * @return a qualifier with all of its keys prefixed with this key
	 */
	public EOQualifier prefix(EOQualifier qualifier) {
		return ERXPrefixQualifierTraversal.prefixQualifierWithKey(qualifier, this);
	}

	@Override
	public String toString() {
		return _key;
	}
}
