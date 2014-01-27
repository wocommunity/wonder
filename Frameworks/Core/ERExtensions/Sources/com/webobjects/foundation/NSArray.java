package com.webobjects.foundation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

/**
 * <span class="en">
 * NSArray re-implementation to support JDK 1.5 templates. Use with
 * </span>
 * 
 * <span class="ja">
 * JDK 1.5 テンプレートをサポートする為の再実装。使用は
 * </span>
 * 
 * <pre>
 * NSArray<Bug> bugs = ds.fetchObjects();
 * 
 * for(Bug : bugs) {
 * 	  ...
 * }</pre>
 *
 * @param <E> - type of array contents
 */
public class NSArray<E> implements Cloneable, Serializable, NSCoding, NSKeyValueCoding, NSKeyValueCodingAdditions, _NSFoundationCollection, List<E> {
  
  static final long serialVersionUID = -3789592578296478260L;

	public static class _AvgNumberOperator extends _Operator implements Operator {

		public Object compute(NSArray<?> values, String keyPath) {
			int count = values.count();
			if (count != 0) {
				BigDecimal sum = _sum(values, keyPath);
				return sum.divide(BigDecimal.valueOf(count), sum.scale() + 4, 6);
			}
			return null;
		}
	}

	public static class _SumNumberOperator extends _Operator implements Operator {

		public Object compute(NSArray<?> values, String keyPath) {
			return _sum(values, keyPath);
		}
	}

	public static class _MinOperator extends _Operator implements Operator {

		public Object compute(NSArray<?> values, String keyPath) {
			Object min = null;
			Object[] objects = values.objectsNoCopy();
			for (int i = 0; i < objects.length; i++) {
				min = _minOrMaxValue(min, _operationValue(objects[i], keyPath), false);
			}

			return min;
		}
	}

	public static class _MaxOperator extends _Operator implements Operator {

		public Object compute(NSArray<?> values, String keyPath) {
			Object max = null;
			Object[] objects = values.objectsNoCopy();
			for (int i = 0; i < objects.length; i++) {
				max = _minOrMaxValue(max, _operationValue(objects[i], keyPath), true);
			}

			return max;
		}
	}

	public static class _Operator {

		protected Object _operationValue(Object object, String keyPath) {
			return keyPath == null || keyPath.length() <= 0 ? object : NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, keyPath);
		}

		private BigDecimal _bigDecimalForValue(Object object) {
			if (object != null) {
				if (_NSUtilities._isClassANumberOrABoolean(object.getClass())) {
					return (BigDecimal) _NSUtilities.convertNumberOrBooleanIntoCompatibleValue(object, _NSUtilities._BigDecimalClass);
				}
				if (object instanceof String) {
					return new BigDecimal((String) object);
				}
				throw new IllegalStateException("Can't convert " + object + " (class " + object.getClass().getName() + ") into number");
			}
			return null;
		}

		BigDecimal _sum(NSArray<?> values, String keyPath) {
			BigDecimal sum = BigDecimal.valueOf(0L);
			Object[] objects = values.objectsNoCopy();
			for (int i = 0; i < objects.length; i++) {
				BigDecimal value = _bigDecimalForValue(_operationValue(objects[i], keyPath));
				if (value != null) {
					sum = sum.add(value);
				}
			}

			return sum;
		}

		Object _minOrMaxValue(Object referenceValue, Object compareValue, boolean trueForMaxAndFalseForMin) {
			if (referenceValue == null) {
				return compareValue;
			}
			if (compareValue == null) {
				return referenceValue;
			}
			int comparison;
			if (_NSUtilities._isClassANumberOrABoolean(referenceValue.getClass())) {
				comparison = _NSUtilities.compareNumbersOrBooleans(referenceValue, compareValue);
			}
			else if (referenceValue instanceof NSTimestamp) {
				comparison = ((NSTimestamp) referenceValue).compare((NSTimestamp) compareValue);
			}
			else if (referenceValue instanceof Comparable) {
				comparison = ((Comparable<Object>) referenceValue).compareTo(compareValue);
			}
			else {
				throw new IllegalStateException("Cannot compare values " + referenceValue + " and " + compareValue + " (they are not instance of Comparable");
			}
			if (trueForMaxAndFalseForMin) {
				if (comparison >= 0) {
					return referenceValue;
				}
			}
			else if (comparison <= 0) {
				return referenceValue;
			}
			return compareValue;
		}
	}

	public static class _CountOperator implements Operator {

		public Object compute(NSArray<?> values, String keyPath) {
			return _NSUtilities.IntegerForInt(values.count());
		}
	}

	public static abstract interface Operator {

		public abstract Object compute(NSArray<?> nsarray, String s);
	}

	public static final Class _CLASS = _NSUtilitiesExtra._classWithFullySpecifiedNamePrime("com.webobjects.foundation.NSArray");

	public static final int NotFound = -1;
	public static final NSArray EmptyArray = new NSArray<Object>();
	private static final char _OperatorIndicatorChar = '@';
	public static final String CountOperatorName = "count";
	public static final String MaximumOperatorName = "max";
	public static final String MinimumOperatorName = "min";
	public static final String SumOperatorName = "sum";
	public static final String AverageOperatorName = "avg";
	private static final String SerializationValuesFieldKey = "objects";
	private static NSMutableDictionary<String, Operator> _operators = new NSMutableDictionary<String, Operator>(8);
	protected static final int _NSArrayClassHashCode = _CLASS.hashCode();
	protected Object[] _objects;
	protected transient int _hashCache;
	private transient boolean _recomputeHashCode = true;
	public static final boolean CheckForNull = true;
	public static final boolean IgnoreNull = true;
	private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField(SerializationValuesFieldKey, ((Object) (new Object[0])).getClass()) };

	public static NSArray<String> operatorNames() {
		NSArray<String> operatorNames;
		synchronized (_operators) {
			operatorNames = _operators.allKeys();
		}
		return operatorNames;
	}

	public static void setOperatorForKey(String operatorName, Operator arrayOperator) {
		if (operatorName == null) {
			throw new IllegalArgumentException("Operator key cannot be null");
		}
		if (arrayOperator == null) {
			throw new IllegalArgumentException("Operator cannot be null for " + operatorName);
		}
		synchronized (_operators) {
			_operators.setObjectForKey(arrayOperator, operatorName);
		}
	}

	public static Operator operatorForKey(String operatorName) {
		Operator arrayOperator;
		synchronized (_operators) {
			arrayOperator = _operators.objectForKey(operatorName);
		}
		return arrayOperator;
	}

	public static void removeOperatorForKey(String operatorName) {
		if (operatorName != null) {
			synchronized (_operators) {
				_operators.removeObjectForKey(operatorName);
			}
		}
	}
	
	protected void _initializeWithCapacity(int capacity) {
		_setObjects(capacity <= 0 ? null : new Object[capacity]);
		_setCount(0);
		_setMustRecomputeHash(true);
	}

	public NSArray() {
		this(null, 0, 0, false, false);
	}

	public NSArray(E object) {
		if (object == null) {
			throw new IllegalArgumentException("Attempt to insert null into an NSArray");
		}
		_initializeWithCapacity(1);
		_objects()[0] = object;
		_setCount(1);

	}

	private void initFromObjects(Object[] objects, int rangeLocation, int rangeLength, boolean checkForNull, boolean ignoreNull) {
		initFromObjects(objects, rangeLocation, rangeLength, 0, checkForNull, ignoreNull);
	}
	
	private void initFromObjects(Object[] objects, int rangeLocation, int rangeLength, int offset, boolean checkForNull, boolean ignoreNull) {
		if (checkForNull) {
			int maxRange = rangeLocation + rangeLength;
			int validCount = 0;
			Object[] validObjects = new Object[maxRange];
			for (int i = rangeLocation; i < maxRange; i++) {
				Object o = objects[i];
				if (o != null) {
					validObjects[validCount++] = o;
					continue;
				}
				if (!ignoreNull)
					throw new IllegalArgumentException("Attempt to insert null into an " + getClass().getName() + ".");
			}
			_initializeWithCapacity(validCount + offset);

			if (validCount > 0) {
				System.arraycopy(validObjects, 0, _objects(), offset, validCount);				
			}
			_setCount(validCount + offset);
		} else {
			_initializeWithCapacity(rangeLength + offset);
			if (rangeLength > 0) {
				System.arraycopy(objects, rangeLocation, _objects(), offset, rangeLength);
			}
			_setCount(rangeLength + offset);
		}
	}
	
	private void initFromList(List<? extends E> list, int rangeLocation, int rangeLength, int offset, boolean checkForNull, boolean ignoreNull) {
		int maxRange = rangeLocation + rangeLength;
		if (checkForNull) {
			int validCount = 0;
			Object[] validObjects = new Object[maxRange];
			for (int i = rangeLocation; i < maxRange; i++) {
				Object o = list.get(i);
				if (o != null) {
					validObjects[validCount++] = o;
					continue;
				}
				if (!ignoreNull)
					throw new IllegalArgumentException("Attempt to insert null into an " + getClass().getName() + ".");
			}

			_initializeWithCapacity(validCount + offset);

			if (validCount > 0) {
				System.arraycopy(validObjects, 0, _objects(), offset, validCount);				
			}
			_setCount(validCount + offset);
		} else {
			_initializeWithCapacity(rangeLength + offset);
			System.arraycopy(list.toArray(), rangeLocation, _objects(), offset, rangeLength);
			_setCount(rangeLength + offset);
		}
	}

	protected NSArray(Object[] objects, int rangeLocation, int rangeLength, boolean checkForNull, boolean ignoreNull) {
		initFromObjects(objects, rangeLocation, rangeLength, checkForNull, ignoreNull);
	}

	public NSArray(E[] objects) {
		this(objects, 0, objects == null ? 0 : objects.length, true, true);
	}
	
	public NSArray(E object, E... objects) {
		if (object == null) {
			initFromObjects(objects, 0, objects == null ? 0 : objects.length, 0, true, true);
		} else {
			initFromObjects(objects, 0, objects == null ? 0 : objects.length, 1, true, true);
			_objects()[0] = object;
		}
	}

	public NSArray(E[] objects, NSRange range) {
		this(objects, range == null ? 0 : range.location(), range == null ? 0 : range.length(), true, true);
	}

	public NSArray(NSArray<? extends E> otherArray) {
		this(otherArray == null ? null : (E[])otherArray.objectsNoCopy(), 0, otherArray == null ? 0 : otherArray.count(), false, false);
	}

	public NSArray(List<? extends E> list, boolean checkForNull) {
		if (list == null) {
			throw new NullPointerException("List cannot be null");
		}
		initFromList(list, 0, list.size(), 0, checkForNull, false);
	}

	public NSArray(Collection<? extends E> collection, boolean checkForNull) {
		if (collection == null) {
			throw new NullPointerException("Collection cannot be null");
		}
		
		Object[] anArray = collection.toArray();
		initFromObjects(anArray, 0, anArray.length, checkForNull, false);
	}

	public NSArray(Collection<? extends E> collection) {
		this(collection, true);
	}

    public NSArray(List<? extends E> list, NSRange range, boolean ignoreNull) {
    	if (list == null) {
    		throw new IllegalArgumentException("List cannot be null");
    	}
    	initFromList(list, range != null ? range.location() : 0, range != null ? range.length() : 0, 0, true, ignoreNull);
    }

    public NSArray(Vector<? extends E> vector, NSRange range, boolean ignoreNull) {
    	this((List<E>)vector, range, ignoreNull);
    }

    protected void _setCount(int count) {
//    	if(count != count() && count != 0) {
//    		throw new IllegalStateException();
//    	}
    }
     
	protected Object[] _objects() {
		return _objects;
	}

	protected void _setObjects(Object[] objects) {
		_objects = objects;
	}

	protected Object[] objectsNoCopy() {
		Object[] objs = _objects();
		return objs != null ? objs : _NSCollectionPrimitives.EmptyArray;
	}

	public int count() {
		return _objects != null ? _objects.length : 0;
	}

	public E objectAtIndex(int index) {
		int count = count();
		if (index >= 0 && index < count) {
			return (E)_objects()[index];
		}
		if (count == 0) {
			throw new IllegalArgumentException("Array is empty");
		}
		throw new IllegalArgumentException("Index (" + index + ") out of bounds [0, " + (count() - 1) + "]");
	}

	public NSArray<E> arrayByAddingObject(E object) {
		if (object == null) {
			throw new IllegalArgumentException("Attempt to insert null into an  " + getClass().getName() + ".");
		}
		int count = count();
		Object[] objects = new Object[count + 1];
		System.arraycopy(objectsNoCopy(), 0, objects, 0, count);
		objects[count] = object;
		return new NSArray<E>(objects, 0, count + 1, false, false);
	}

	public NSArray<E> arrayByAddingObjectsFromArray(NSArray<? extends E> otherArray) {
		if (otherArray != null) {
			int count = count();
			int otherCount = otherArray.count();
			if (count == 0) {
				return (NSArray<E>) otherArray.immutableClone();
			}
			if (otherCount == 0) {
				return immutableClone();
			}
			Object[] objects = new Object[count + otherCount];
			System.arraycopy(objectsNoCopy(), 0, objects, 0, count);
			System.arraycopy(otherArray.objectsNoCopy(), 0, objects, count, otherCount);
			return new NSArray<E>(objects, 0, count + otherCount, false, false);
		}
		return immutableClone();
	}

	public Object[] objects() {
		int count = count();
		Object[] objects = new Object[count];
		if (count > 0) {
			System.arraycopy(objectsNoCopy(), 0, objects, 0, count);
		}
		return objects;
	}

	public Object[] objects(NSRange range) {
		if (range == null) {
			return _NSCollectionPrimitives.EmptyArray;
		}
		int rangeLength = range.length();
		Object[] objects = new Object[rangeLength];
		System.arraycopy(objectsNoCopy(), range.location(), objects, 0, rangeLength);
		return objects;
	}

	public Vector<E> vector() {
		Vector<E> vector = new Vector<E>();
		E[] objects = (E[])objectsNoCopy();
		for (int i = 0; i < objects.length; i++) {
			vector.addElement(objects[i]);
		}

		return vector;
	}

	public ArrayList<E> arrayList() {
		E[] objects = (E[])objectsNoCopy();
		ArrayList<E> list = new ArrayList<E>(objects.length);
		for (int i = 0; i < objects.length; i++) {
			list.add(objects[i]);
		}

		return list;
	}

	public boolean containsObject(Object object) {
		if (object == null) {
			return false;
		}
		return _findObjectInArray(0, count(), object, false) != NotFound;
	}

	public E firstObjectCommonWithArray(NSArray<?> otherArray) {
		if (otherArray == null) {
			return null;
		}
		int otherCount = otherArray.count();
		if (otherCount > 0) {
			Object[] objects = objectsNoCopy();
			for (int i = 0; i < objects.length; i++) {
				if (otherArray.containsObject(objects[i])) {
					return (E)objects[i];
				}
			}

		}
		return null;
	}

	/**
	 * @deprecated use {@link #objects()} or {@link #objectsNoCopy()}
	 */
	@Deprecated
	public void getObjects(Object[] objects) {
		if (objects == null) {
			throw new IllegalArgumentException("Object buffer cannot be null");
		}
		System.arraycopy(objectsNoCopy(), 0, objects, 0, count());
	}

	/**
	 * @deprecated use {@link #objects(NSRange)}
	 */
	@Deprecated
	public void getObjects(Object[] objects, NSRange range) {
		if (objects == null) {
			throw new IllegalArgumentException("Object buffer cannot be null");
		}
		if (range == null) {
			throw new IllegalArgumentException("Range cannot be null");
		}
		System.arraycopy(objectsNoCopy(), range.location(), objects, 0, range.length());
	}

	private final int _findObjectInArray(int index, int length, Object object, boolean identical) {
		if (count() > 0) {
			Object[] objects = objectsNoCopy();
			int maxIndex = (index + length) - 1;
			for (int i = index; i <= maxIndex; i++) {
				if (objects[i] == object) {
					return i;
				} 
				if (!identical && object.equals(objects[i])) {
					return i;
				}

			}

		}
		return NotFound;
	}
	
	public int indexOfObject(Object object) {
		if (object == null) {
			return NotFound;
		}
		return _findObjectInArray(0, count(), object, false);
	}

	public int indexOfObject(Object object, NSRange range) {
		if (object == null || range == null) {
			return NotFound;
		}
		int count = count();
		int rangeLocation = range.location();
		int rangeLength = range.length();
		if (rangeLocation + rangeLength > count || rangeLocation >= count) {
			throw new IllegalArgumentException("Range [" + rangeLocation + "; " + rangeLength + "] out of bounds [0, " + (count() - 1) + "]");
		}
		return _findObjectInArray(rangeLocation, rangeLength, object, false);
	}

	public int indexOfIdenticalObject(Object object) {
		if (object == null) {
			return NotFound;
		}
		return _findObjectInArray(0, count(), object, true);
	}

	public int indexOfIdenticalObject(Object object, NSRange range) {
		if (object == null || range == null) {
			return NotFound;
		}
		int count = count();
		int rangeLocation = range.location();
		int rangeLength = range.length();
		if (rangeLocation + rangeLength > count || rangeLocation >= count) {
			throw new IllegalArgumentException("Range [" + rangeLocation + "; " + rangeLength + "] out of bounds [0, " + (count() - 1) + "]");
		}
		return _findObjectInArray(rangeLocation, rangeLength, object, true);
	}

	public NSArray<E> subarrayWithRange(NSRange range) {
		if (range == null) {
			return EmptyArray;
		}
		return new NSArray<E>(objectsNoCopy(), range.location(), range.length(), false, false);
	}

	public E lastObject() {
		int count = count();
		return count != 0 ? objectAtIndex(count - 1) : null;
	}

	private boolean _equalsArray(NSArray<?> otherArray) {
		int count = count();
		if (count != otherArray.count()) {
			return false;
		}
		if (!_mustRecomputeHash() && !otherArray._mustRecomputeHash() && hashCode() != otherArray.hashCode()) {
			return false;
		}
		Object[] objects = objectsNoCopy();
		Object[] otherObjects = otherArray.objectsNoCopy();
		for (int i = 0; i < count; i++) {
			if (!objects[i].equals(otherObjects[i])) {
				return false;
			}
		}

		return true;
	}

	public boolean isEqualToArray(NSArray<?> otherArray) {
		if (otherArray == this) {
			return true;
		}
		if (otherArray == null) {
			return false;
		}
		return _equalsArray(otherArray);
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (object instanceof NSArray) {
			return _equalsArray((NSArray<?>) object);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public Enumeration<E> objectEnumerator() {
		return new _NSJavaArrayEnumerator(objectsNoCopy(), count(), false);
	}

	@SuppressWarnings("unchecked")
	public Enumeration<E> reverseObjectEnumerator() {
		return new _NSJavaArrayEnumerator(objectsNoCopy(), count(), true);
	}

	/**
	 * @deprecated Method sortedArrayUsingSelector is deprecated
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public NSArray sortedArrayUsingSelector(NSSelector selector) throws NSComparator.ComparisonException {
		NSMutableArray array = new NSMutableArray(this);
		NSComparator comparator = new NSComparator._NSSelectorComparator(selector);
		array.sortUsingComparator(comparator);
		return array;
	}

	public NSArray<E> sortedArrayUsingComparator(NSComparator comparator) throws NSComparator.ComparisonException {
		NSMutableArray<E> array = new NSMutableArray<E>(this);
		array.sortUsingComparator(comparator);
		return array;
	}

	public String componentsJoinedByString(String separator) {
		Object[] objects = objectsNoCopy();
		StringBuilder buffer = new StringBuilder(objects.length * 32);
		for (int i = 0; i < objects.length; i++) {
			if (i > 0 && separator != null) {
				buffer.append(separator);
			}
			buffer.append(objects[i].toString());
		}

		return buffer.toString();
	}

	public static NSArray<String> componentsSeparatedByString(String string, String separator) {
		NSMutableArray<String> objects;
		if ((string == null) || (string.length() == 0)) {
			return emptyArray();
		}
		int stringLength = string.length();

		if ((separator == null) || (separator.length() == 0)) {
			return new NSArray<String>(string);
		}
		int separatorLength = separator.length();

		int start = 0; int index = 0; int count = 0;

		if ((separatorLength == 1) && (stringLength < 256)) {
			char[] parseData = string.toCharArray();
			char charSeparator = separator.charAt(0);

			for (int i = 0; i < stringLength; ++i) {
				if (parseData[i] == charSeparator) {
					++count;
				}
			}

			if (count == 0) {
				return new NSArray<String>(string);
			}

			objects = new NSMutableArray<String>(count + 1);
			int end = stringLength - 1;
			for (index = 0; index <= end; ++index) {
				if (parseData[index] == charSeparator) {
					if (start == index) {
						objects.addObject("");
					} else {
						objects.addObject(string.substring(start, index));
					}
					start = index + 1;
				}
			}
			if (parseData[end] == charSeparator) {
				objects.addObject("");
			} else {
				objects.addObject(string.substring(start, stringLength));
			}
		} else {
			objects = new NSMutableArray<String>(4);
			int end = stringLength - separatorLength;
			while (true) { 
				if (start >= stringLength) {
					return objects;
				}
				index = string.indexOf(separator, start);

				if (index < 0) {
					index = stringLength;
				}

				if (index == end) {
					break;
				}
				objects.addObject(string.substring(start, index));
				start = index + separatorLength;
			}
			if (start <= index) {
				objects.addObject(string.substring(start, index));
			}

			objects.addObject("");
		}

		return objects;
	}

	public static NSMutableArray<String> _mutableComponentsSeparatedByString(String string, String separator) {
		return componentsSeparatedByString(string, separator).mutableClone();
	}

	private Object _valueForKeyPathWithOperator(String keyPath) {
		int index = keyPath.indexOf('.');
		String operatorName;
		String operatorPath;
		if (index < 0) {
			operatorName = keyPath.substring(1);
			operatorPath = "";
		} else {
			operatorName = keyPath.substring(1, index);
			operatorPath = index >= keyPath.length() - 1 ? "" : keyPath.substring(index + 1);
		}
		Operator arrayOperator = operatorForKey(operatorName);
		if (arrayOperator != null) {
			return arrayOperator.compute(this, operatorPath);
		}
		throw new IllegalArgumentException("No key operator available to compute aggregate " + keyPath);
	}

	public Object valueForKey(String key) {
		if (key != null) {
			if (key.charAt(0) == _OperatorIndicatorChar) {
				return _valueForKeyPathWithOperator(key);
			}
			if (key.equals(CountOperatorName)) {
				return _NSUtilities.IntegerForInt(count());
			}
		}
		Object[] objects = objectsNoCopy();
		NSMutableArray<Object> values = new NSMutableArray<Object>(objects.length);
		for (int i = 0; i < objects.length; i++) {
			Object value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(objects[i], key);
			values.addObject(value == null ? ((Object) (NSKeyValueCoding.NullValue)) : value);
		}

		return values;
	}

	public void takeValueForKey(Object value, String key) {
		Object[] objects = objectsNoCopy();
		for (int i = 0; i < objects.length; i++) {
			NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(objects[i], value, key);
		}

	}

	public Object valueForKeyPath(String keyPath) {
		if (keyPath != null && keyPath.charAt(0) == _OperatorIndicatorChar) {
			return _valueForKeyPathWithOperator(keyPath);
		}
		return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, keyPath);
	}

	public void takeValueForKeyPath(Object value, String keyPath) {
		NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(this, value, keyPath);
	}

	@SuppressWarnings("unchecked")
	public Class classForCoder() {
		return _CLASS;
	}

	public static Object decodeObject(NSCoder coder) {
		return new NSArray<Object>(coder.decodeObjects());
	}

	public void encodeWithCoder(NSCoder coder) {
		coder.encodeObjects(objectsNoCopy());
	}

	public void makeObjectsPerformSelector(NSSelector selector, Object... parameters) {
		if (selector == null) {
			throw new IllegalArgumentException("Selector cannot be null");
		}
		Object[] objects = objectsNoCopy();
		for (int i = 0; i < objects.length; i++) {
			NSSelector._safeInvokeSelector(selector, objects[i], parameters);
		}

	}

	public int _shallowHashCode() {
		return _NSArrayClassHashCode;
	}

	@Override
	public int hashCode() {
		if (_mustRecomputeHash()) {
			int hash = 0;
			int max = count() <= 16 ? count() : 16;
			for (int i = 0; i < max; i++) {
				Object element = objectAtIndex(i);
				if (element instanceof _NSFoundationCollection) {
					hash ^= ((_NSFoundationCollection) element)._shallowHashCode();
				} else {
					hash ^= element.hashCode();
				}
			}

			_hashCache = hash;
			_setMustRecomputeHash(false);
		}
		return _hashCache;
	}

	@Override
	public Object clone() {
		return this;
	}

	public NSArray<E> immutableClone() {
		return this;
	}

	public NSMutableArray<E> mutableClone() {
		return new NSMutableArray<E>(this);
	}

	@Override
	public String toString() {
		if(count() == 0) {
			return "()";
		}
		StringBuilder buffer = new StringBuilder(128);
		buffer.append('(');
		Object[] objects = objectsNoCopy();
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (i > 0) {
				buffer.append(", ");
			}
			if (object instanceof String) {
				buffer.append('"');
				buffer.append((String) object);
				buffer.append('"');
				continue;
			}
			if (object instanceof Boolean) {
				buffer.append(((Boolean) object).booleanValue() ? "true" : "false");
			} else {
				buffer.append(object == this ? "THIS" : object.toString());
			}
		}

		buffer.append(')');
		return buffer.toString();
	}

	protected boolean _mustRecomputeHash() {
		return _recomputeHashCode;
	}

	protected void _setMustRecomputeHash(boolean change) {
		_recomputeHashCode = change;
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		java.io.ObjectOutputStream.PutField fields = s.putFields();
		fields.put(SerializationValuesFieldKey, objects());
		s.writeFields();
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		java.io.ObjectInputStream.GetField fields = null;
		fields = s.readFields();
		Object[] values = (Object[]) fields.get(SerializationValuesFieldKey, _NSUtilities._NoObjectArray);
		values = values != null ? values : _NSUtilities._NoObjectArray;
		initFromObjects(values, 0, values.length, true, false);
	}

	private Object readResolve() throws ObjectStreamException {
		if (getClass() == _CLASS && count() == 0) {
			return EmptyArray;
		}
		return this;
	}

	public void add(int index, E element) {
		throw new UnsupportedOperationException("add is not a supported operation in com.webobjects.foundation.NSArray");
	}

	public boolean add(E element) {
		throw new UnsupportedOperationException("add is not a supported operation in com.webobjects.foundation.NSArray");
	}

	public boolean addAll(Collection<? extends E> collection) {
		throw new UnsupportedOperationException("addAll is not a supported operation in com.webobjects.foundation.NSArray");
	}

	public boolean addAll(int index, Collection<? extends E> collection) {
		throw new UnsupportedOperationException("addAll is not a supported operation in com.webobjects.foundation.NSArray");
	}

	public boolean contains(Object element) {
		if (element == null) {
			throw new NullPointerException("NSArray does not support null values");
		}
		return containsObject(element);
	}

	@SuppressWarnings("unchecked")
	public Iterator<E> iterator() {
		return new _NSJavaArrayListIterator(objectsNoCopy(), count());
	}

	public Object[] toArray() {
		return objects();
	}

	public <T> T[] toArray(T[] objects) {
		if (objects == null) {
		  throw new NullPointerException("List.toArray() cannot have a null parameter");
		}
		
		int count = count();
		if (count <= 0) {
			return objects;
		}
		
		Object[] objs = objectsNoCopy();
        if (objects.length < objs.length) {
            objects = (T[])java.lang.reflect.Array.newInstance(objects.getClass().getComponentType(), objs.length);
        }
	    System.arraycopy(objs, 0, objects, 0, objs.length);
        return objects;
	}

	public boolean containsAll(Collection<?> c) {
		Object[] objects = c.toArray();
		if (objects.length > 0) {
			for (int i = 0; i < objects.length; i++) {
				if (objects[i] == null) {
					return false;
				}
				if (_findObjectInArray(0, count(), objects[i], false) == NotFound) {
					return false;
				}
			}

		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public ListIterator<E> listIterator() {
		Object[] objs = objectsNoCopy();
		return new _NSJavaArrayListIterator(objs, count());
	}

	@SuppressWarnings("unchecked")
	public ListIterator<E> listIterator(int index) {
		Object[] objs = objectsNoCopy();
		return new _NSJavaArrayListIterator(objs, count(), index);
	}

	public E get(int index) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException("Index " + index + " is out of bounds");
		}
		return objectAtIndex(index);
	}

	public E set(int index, E element) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException("Index " + index + " is out of bounds");
		}
		throw new UnsupportedOperationException("Set is not a support operation in com.webobjects.foundation.NSArray");
	}

	public int indexOf(Object element) {
		if (element == null) {
			throw new NullPointerException("com.webobjects.foundation.NSArray does not support null values");
		}
		return indexOfObject(element);
	}

	public int lastIndexOf(Object element) {
		int lastIndex = NotFound;
		if (element == null) {
			throw new NullPointerException("com.webobjects.foundation.NSArray does not support null values");
		}
		for (int i = 0; i < count(); i++) {
			if (objectAtIndex(i).equals(element)) {
				lastIndex = i;
			}
		}

		return lastIndex;
	}

	public boolean isEmpty() {
		return count() == 0;
	}

	public int size() {
		return count();
	}

	public E remove(int index) {
		throw new UnsupportedOperationException("Remove is not a support operation in com.webobjects.foundation.NSArray");
	}

	public boolean remove(Object object) {
		throw new UnsupportedOperationException("Remove is not a support operation in com.webobjects.foundation.NSArray");
	}

	public void clear() {
		throw new UnsupportedOperationException("Clear is not a supported operation in com.webobjects.foundation.NSArray");
	}

	public boolean retainAll(Collection<?> collection) {
		throw new UnsupportedOperationException("RetainAll is not a supported operation in com.webobjects.foundation.NSArray");
	}

	public boolean removeAll(Collection<?> collection) {
		throw new UnsupportedOperationException("RemoveAll is not a supported operation in com.webobjects.foundation.NSArray");
	}

	public List<E> subList(int fromIndex, int toIndex) {
		if (fromIndex < 0 || toIndex > count() || fromIndex > toIndex) {
			throw new IndexOutOfBoundsException("Illegal index value (fromIndex < 0 || toIndex > size || fromIndex > toIndex)");
		}
		return subarrayWithRange(new NSRange(fromIndex, (toIndex - fromIndex)));
	}

	public static final <T> NSArray<T> emptyArray() {
		return EmptyArray;
	}
	
	static {
		try {
			setOperatorForKey(CountOperatorName, new _CountOperator());
			setOperatorForKey(MaximumOperatorName, new _MaxOperator());
			setOperatorForKey(MinimumOperatorName, new _MinOperator());
			setOperatorForKey(SumOperatorName, new _SumNumberOperator());
			setOperatorForKey(AverageOperatorName, new _AvgNumberOperator());
		}
		catch (Throwable e) {
			NSLog.err.appendln("Exception occurred in initializer");
			if (NSLog.debugLoggingAllowedForLevel(1)) {
				NSLog.debug.appendln(e);
			}
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}
}
