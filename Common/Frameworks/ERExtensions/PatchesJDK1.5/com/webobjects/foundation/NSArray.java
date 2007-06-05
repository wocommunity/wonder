package com.webobjects.foundation;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * NSArray reimplementation to support JDK 1.5 templates.
 * @author ak
 *
 * @param <E>
 */

public class NSArray<E> implements Cloneable, Serializable, NSCoding, NSKeyValueCoding, NSKeyValueCodingAdditions, _NSFoundationCollection, List<E> {
	public static class _AvgNumberOperator extends _Operator implements Operator {

		public Object compute(NSArray values, String keyPath) {
			int count = values.count();
			if (count != 0) {
				BigDecimal sum = _sum(values, keyPath);
				return sum.divide(BigDecimal.valueOf(count), sum.scale() + 4, 6);
			}
			else {
				return null;
			}
		}
	}

	public static class _SumNumberOperator extends _Operator implements Operator {

		public Object compute(NSArray values, String keyPath) {
			return _sum(values, keyPath);
		}
	}

	public static class _MinOperator extends _Operator implements Operator {

		public Object compute(NSArray values, String keyPath) {
			Object min = null;
			Object objects[] = values.objectsNoCopy();
			for (int i = 0; i < objects.length; i++) {
				min = _minOrMaxValue(min, _operationValue(objects[i], keyPath), false);
			}

			return min;
		}
	}

	public static class _MaxOperator extends _Operator implements Operator {

		public Object compute(NSArray values, String keyPath) {
			Object max = null;
			Object objects[] = values.objectsNoCopy();
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
					return (BigDecimal) (BigDecimal) _NSUtilities.convertNumberOrBooleanIntoCompatibleValue(object, _NSUtilities._BigDecimalClass);
				}
				if (object instanceof String) {
					return new BigDecimal((String) object);
				}
				else {
					throw new IllegalStateException("Can't convert " + object + " (class " + object.getClass().getName() + ") into number");
				}
			}
			else {
				return null;
			}
		}

		BigDecimal _sum(NSArray values, String keyPath) {
			BigDecimal sum = BigDecimal.valueOf(0L);
			Object objects[] = values.objectsNoCopy();
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
				comparison = ((Comparable) referenceValue).compareTo(compareValue);
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

		public Object compute(NSArray values, String keyPath) {
			return _NSUtilities.IntegerForInt(values.count());
		}
	}

	public static interface Operator {

		public abstract Object compute(NSArray nsarray, String s);
	}

	public static final Class _CLASS;
	public static final int NotFound = -1;
	public static final NSArray EmptyArray = new NSArray();
	private static final char _OperatorIndicatorChar = '@';
	public static final String CountOperatorName = "count";
	public static final String MaximumOperatorName = "max";
	public static final String MinimumOperatorName = "min";
	public static final String SumOperatorName = "sum";
	public static final String AverageOperatorName = "avg";
	static final long serialVersionUID = -819034676L;
	private static final String SerializationValuesFieldKey = "objects";
	private static NSMutableDictionary _operators = new NSMutableDictionary(8);
	protected static int _NSArrayClassHashCode;
	protected transient int _capacity;
	protected transient int _count;
	protected Object _objects[];
	protected transient Object _objectsCache[];
	protected transient int _hashCache;
	private transient boolean _recomputeHashCode;
	private static final ObjectStreamField serialPersistentFields[] = { new ObjectStreamField("objects", ((Object) (new Object[0])).getClass()) };

	public static NSArray<String> operatorNames() {
		NSArray operatorNames;
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
			arrayOperator = (Operator) (Operator) _operators.objectForKey(operatorName);
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
		_capacity = capacity;
		_count = 0;
		_objects = capacity <= 0 ? null : new Object[capacity];
		_objectsCache = null;
		_setMustRecomputeHash(true);
	}

	protected void _ensureCapacity(int capacity) {
		if (capacity > _capacity) {
			if (capacity == 0) {
				_objects = null;
			}
			else {
				if (capacity < 4) {
					capacity = 4;
				}
				else {
					int testCapacity = 2 * _capacity;
					if (testCapacity > capacity) {
						capacity = testCapacity;
					}
				}
				_objects = _objects != null ? _NSCollectionPrimitives.copyArray(_objects, capacity) : new Object[capacity];
			}
			_capacity = capacity;
		}
	}

	public NSArray() {
		this(null, 0, 0, false);
	}

	public NSArray(E object) {
		_recomputeHashCode = true;
		if (object == null) {
			throw new IllegalArgumentException("Attempt to insert null into an NSArray");
		}
		else {
			_initializeWithCapacity(1);
			_objects[0] = object;
			_count = 1;
			return;
		}
	}

	private void initFromObjects(Object objects[], int rangeLocation, int rangeLength, boolean checkForNull) {
		if (checkForNull) {
			int maxRange = rangeLocation + rangeLength;
			for (int i = rangeLocation; i < maxRange; i++) {
				if (objects[i] == null) {
					throw new IllegalArgumentException("Attempt to insert null into an " + getClass().getName() + ".");
				}
			}

		}
		_initializeWithCapacity(rangeLength);
		if (rangeLength > 0) {
			System.arraycopy(((Object) (objects)), rangeLocation, ((Object) (_objects)), 0, rangeLength);
		}
		_count = rangeLength;
	}

	private NSArray(E objects[], int rangeLocation, int rangeLength, boolean checkForNull) {
		_recomputeHashCode = true;
		initFromObjects(objects, rangeLocation, rangeLength, checkForNull);
	}

	public NSArray(E objects[]) {
		this(objects, 0, objects == null ? 0 : objects.length, true);
	}

	public NSArray(E objects[], NSRange range) {
		this(objects, range == null ? 0 : range.location(), range == null ? 0 : range.length(), true);
	}

	public NSArray(NSArray<E> otherArray) {
		this(otherArray == null ? null : otherArray.objectsNoCopy(), 0, otherArray == null ? 0 : otherArray.count(), false);
	}

	public NSArray(List<E> list, boolean checkForNull) {
		_recomputeHashCode = true;
		if (list == null) {
			throw new IllegalArgumentException("List cannot be null");
		}
		else {
			Object aList[] = list.toArray();
			initFromObjects(aList, 0, aList.length, checkForNull);
			return;
		}
	}

	public NSArray(Vector<E> vector, NSRange range, boolean ignoreNull) {
		_recomputeHashCode = true;
		if (range != null) {
			if (vector == null) {
				throw new IllegalArgumentException("Vector cannot be null");
			}
			int count = vector.size();
			int rangeLocation = range.location();
			int rangeLength = range.length();
			_initializeWithCapacity(count);
			for (int i = 0; i < rangeLength; i++) {
				Object object = vector.elementAt(i + rangeLocation);
				if (object != null) {
					_objects[_count++] = object;
					continue;
				}
				if (!ignoreNull) {
					throw new IllegalArgumentException("Attempt to insert null into an  " + getClass().getName() + ".");
				}
			}

		}
	}

	protected E[] objectsNoCopy() {
		if (_objectsCache == null) {
			if (_count == 0) {
				_objectsCache = _NSCollectionPrimitives.EmptyArray;
			}
			else if (_count == _capacity) {
				_objectsCache = _objects;
			}
			else {
				_objectsCache = _NSCollectionPrimitives.copyArray(_objects, _count);
			}
		}
		return (E[])_objectsCache;
	}

	public int count() {
		return _count;
	}

	public E objectAtIndex(int index) {
		if (index >= 0 && index < _count) {
			return (E)_objects[index];
		}
		if (_count == 0) {
			throw new IllegalArgumentException("Array is empty");
		}
		else {
			throw new IllegalArgumentException("Index (" + index + ") out of bounds [0, " + (_count - 1) + "]");
		}
	}

	public NSArray<E> arrayByAddingObject(E object) {
		if (object == null) {
			throw new IllegalArgumentException("Attempt to insert null into an  " + getClass().getName() + ".");
		}
		else {
			int count = count();
			Object objects[] = new Object[count + 1];
			System.arraycopy(((Object) (objectsNoCopy())), 0, ((Object) (objects)), 0, count);
			objects[count] = object;
			return new NSArray(objects, 0, count + 1, false);
		}
	}

	public NSArray<E> arrayByAddingObjectsFromArray(NSArray<E> otherArray) {
		if (otherArray != null) {
			int count = count();
			int otherCount = otherArray.count();
			if (count == 0) {
				return new NSArray<E>(otherArray);
			}
			if (otherCount == 0) {
				return (NSArray<E>) clone();
			}
			else {
				E objects[] = (E[]) new Object[count + otherCount];
				System.arraycopy(((Object) (objectsNoCopy())), 0, ((Object) (objects)), 0, count);
				System.arraycopy(((Object) (otherArray.objectsNoCopy())), 0, ((Object) (objects)), count, otherCount);
				return new NSArray(objects, 0, count + otherCount, false);
			}
		}
		else {
			return new NSArray(this);
		}
	}

	public E[] objects() {
		int count = count();
		E objects[] = (E[]) new Object[count];
		if (count > 0) {
			System.arraycopy(((Object) (objectsNoCopy())), 0, ((Object) (objects)), 0, count);
		}
		return objects;
	}

	public E[] objects(NSRange range) {
		if (range == null) {
			return (E[]) _NSCollectionPrimitives.EmptyArray;
		}
		else {
			int count = count();
			int rangeLength = range.length();
			E objects[] = (E[]) new Object[rangeLength];
			System.arraycopy(((Object) (objectsNoCopy())), range.location(), ((Object) (objects)), 0, rangeLength);
			return objects;
		}
	}

	public Vector<E> vector() {
		Vector vector = new Vector();
		Object objects[] = objectsNoCopy();
		for (int i = 0; i < objects.length; i++) {
			vector.addElement(objects[i]);
		}

		return vector;
	}

	public ArrayList<E> arrayList() {
		Object objects[] = objectsNoCopy();
		ArrayList list = new ArrayList(objects.length);
		for (int i = 0; i < objects.length; i++) {
			list.add(objects[i]);
		}

		return list;
	}

	public boolean containsObject(Object object) {
		if (object == null) {
			return false;
		}
		else {
			return _findObjectInArray(0, count(), object, false) != -1;
		}
	}

	public Object firstObjectCommonWithArray(NSArray<E> otherArray) {
		if (otherArray == null) {
			return null;
		}
		int otherCount = otherArray.count();
		if (otherCount > 0) {
			E objects[] = objectsNoCopy();
			for (int i = 0; i < objects.length; i++) {
				if (otherArray.containsObject(objects[i])) {
					return objects[i];
				}
			}

		}
		return null;
	}

	/**
	 * @deprecated Method getObjects is deprecated
	 */

	public void getObjects(Object objects[]) {
		if (objects == null) {
			throw new IllegalArgumentException("Object buffer cannot be null");
		}
		else {
			System.arraycopy(((Object) (objectsNoCopy())), 0, ((Object) (objects)), 0, count());
			return;
		}
	}

	/**
	 * @deprecated Method getObjects is deprecated
	 */

	public void getObjects(Object objects[], NSRange range) {
		if (objects == null) {
			throw new IllegalArgumentException("Object buffer cannot be null");
		}
		if (range == null) {
			throw new IllegalArgumentException("Range cannot be null");
		}
		else {
			System.arraycopy(((Object) (objectsNoCopy())), range.location(), ((Object) (objects)), 0, range.length());
			return;
		}
	}

	private final int _findObjectInArray(int index, int length, Object object, boolean identical) {
		if (count() > 0) {
			Object objects[] = objectsNoCopy();
			int maxIndex = (index + length) - 1;
			for (int i = index; i <= maxIndex; i++) {
				if (objects[i] == object) {
					return i;
				}
			}

			if (!identical) {
				for (int i = index; i <= maxIndex; i++) {
					if (object.equals(objects[i])) {
						return i;
					}
				}

			}
		}
		return -1;
	}

	public int indexOfObject(Object object) {
		if (object == null) {
			return -1;
		}
		else {
			return _findObjectInArray(0, count(), object, false);
		}
	}

	public int indexOfObject(Object object, NSRange range) {
		if (object == null || range == null) {
			return -1;
		}
		int count = count();
		int rangeLocation = range.location();
		int rangeLength = range.length();
		if (rangeLocation + rangeLength > count || rangeLocation >= count) {
			throw new IllegalArgumentException("Range [" + rangeLocation + "; " + rangeLength + "] out of bounds [0, " + (_count - 1) + "]");
		}
		else {
			return _findObjectInArray(rangeLocation, rangeLength, object, false);
		}
	}

	public int indexOfIdenticalObject(Object object) {
		if (object == null) {
			return -1;
		}
		else {
			return _findObjectInArray(0, count(), object, true);
		}
	}

	public int indexOfIdenticalObject(Object object, NSRange range) {
		if (object == null || range == null) {
			return -1;
		}
		int count = count();
		int rangeLocation = range.location();
		int rangeLength = range.length();
		if (rangeLocation + rangeLength > count || rangeLocation >= count) {
			throw new IllegalArgumentException("Range [" + rangeLocation + "; " + rangeLength + "] out of bounds [0, " + (_count - 1) + "]");
		}
		else {
			return _findObjectInArray(rangeLocation, rangeLength, object, true);
		}
	}

	public NSArray subarrayWithRange(NSRange range) {
		if (range == null) {
			return EmptyArray;
		}
		else {
			return new NSArray(objectsNoCopy(), range.location(), range.length(), false);
		}
	}

	public E lastObject() {
		int count = count();
		return count != 0 ? objectAtIndex(count - 1) : null;
	}

	private boolean _equalsArray(NSArray otherArray) {
		int count = count();
		if (count != otherArray.count()) {
			return false;
		}
		if (!_mustRecomputeHash() && !otherArray._mustRecomputeHash() && hashCode() != otherArray.hashCode()) {
			return false;
		}
		Object objects[] = objectsNoCopy();
		Object otherObjects[] = otherArray.objectsNoCopy();
		for (int i = 0; i < count; i++) {
			if (!objects[i].equals(otherObjects[i])) {
				return false;
			}
		}

		return true;
	}

	public boolean isEqualToArray(NSArray otherArray) {
		if (otherArray == this) {
			return true;
		}
		if (otherArray == null) {
			return false;
		}
		else {
			return _equalsArray(otherArray);
		}
	}

	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (object instanceof NSArray) {
			return _equalsArray((NSArray) object);
		}
		else {
			return false;
		}
	}

	public Enumeration<E> objectEnumerator() {
		return new _NSJavaArrayEnumerator(_objects, _count, false);
	}

	public Enumeration<E> reverseObjectEnumerator() {
		return new _NSJavaArrayEnumerator(_objects, _count, true);
	}

	/**
	 * @deprecated Method sortedArrayUsingSelector is deprecated
	 */

	public NSArray sortedArrayUsingSelector(NSSelector selector) throws NSComparator.ComparisonException {
		NSMutableArray array = new NSMutableArray(this);
		NSComparator comparator = new NSComparator._NSSelectorComparator(selector);
		array.sortUsingComparator(comparator);
		return array;
	}

	public NSArray<E> sortedArrayUsingComparator(NSComparator comparator) throws NSComparator.ComparisonException {
		NSMutableArray array = new NSMutableArray(this);
		array.sortUsingComparator(comparator);
		return array;
	}

	public String componentsJoinedByString(String separator) {
		Object objects[] = objectsNoCopy();
		StringBuffer buffer = new StringBuffer(objects.length * 32);
		for (int i = 0; i < objects.length; i++) {
			if (i > 0 && separator != null) {
				buffer.append(separator);
			}
			buffer.append(objects[i].toString());
		}

		return new String(buffer);
	}

	public static NSArray componentsSeparatedByString(String string, String separator) {
		NSMutableArray objects;
		label0: {
			if (string == null) {
				return EmptyArray;
			}
			int stringLength = string.length();
			int separatorLength = separator == null ? 0 : separator.length();
			if (stringLength == 0) {
				return EmptyArray;
			}
			if (separatorLength == 0) {
				return new NSArray(string);
			}
			int start = 0;
			int index = 0;
			int count = 0;
			if (separatorLength == 1 && stringLength < 256) {
				char parseData[] = string.toCharArray();
				char charSeparator = separator.charAt(0);
				for (int i = 0; i < stringLength; i++) {
					if (parseData[i] == charSeparator) {
						count++;
					}
				}

				if (count == 0) {
					return new NSMutableArray(string);
				}
				objects = new NSMutableArray(count + 1);
				int end = stringLength - 1;
				for (index = 0; index <= end; index++) {
					if (parseData[index] != charSeparator) {
						continue;
					}
					if (start == index) {
						objects.addObject("");
					}
					else {
						objects.addObject(string.substring(start, index));
					}
					start = index + 1;
				}

				if (parseData[end] == charSeparator) {
					if (start < end) {
						objects.addObject(string.substring(start, end));
					}
					objects.addObject("");
				}
				else {
					objects.addObject(string.substring(start, stringLength));
				}
				break label0;
			}
			objects = new NSMutableArray(4);
			int end = stringLength - separatorLength;
			do {
				if (start >= stringLength) {
					break label0;
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
			while (true);
			if (start < index) {
				objects.addObject(string.substring(start, index));
			}
			objects.addObject("");
		}
		return objects;
	}

	public static NSMutableArray _mutableComponentsSeparatedByString(String string, String separator) {
		return (NSMutableArray) componentsSeparatedByString(string, separator);
	}

	private Object _valueForKeyPathWithOperator(String keyPath) {
		int index = keyPath.indexOf('.');
		String operatorName;
		String operatorPath;
		if (index < 0) {
			operatorName = keyPath.substring(1);
			operatorPath = "";
		}
		else {
			operatorName = keyPath.substring(1, index);
			operatorPath = index >= keyPath.length() - 1 ? "" : keyPath.substring(index + 1);
		}
		Operator arrayOperator = operatorForKey(operatorName);
		if (arrayOperator != null) {
			return arrayOperator.compute(this, operatorPath);
		}
		else {
			throw new IllegalArgumentException("No key operator available to compute aggregate " + keyPath);
		}
	}

	public Object valueForKey(String key) {
		if (key != null) {
			if (key.charAt(0) == '@') {
				return _valueForKeyPathWithOperator(key);
			}
			if (key.equals("count")) {
				return _NSUtilities.IntegerForInt(count());
			}
		}
		Object objects[] = objectsNoCopy();
		NSMutableArray values = new NSMutableArray(objects.length);
		for (int i = 0; i < objects.length; i++) {
			Object value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(objects[i], key);
			values.addObject(value == null ? ((Object) (NSKeyValueCoding.NullValue)) : value);
		}

		return values;
	}

	public void takeValueForKey(Object value, String key) {
		Object objects[] = objectsNoCopy();
		for (int i = 0; i < objects.length; i++) {
			NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(objects[i], value, key);
		}

	}

	public Object valueForKeyPath(String keyPath) {
		if (keyPath != null && keyPath.charAt(0) == '@') {
			return _valueForKeyPathWithOperator(keyPath);
		}
		else {
			return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, keyPath);
		}
	}

	public void takeValueForKeyPath(Object value, String keyPath) {
		NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(this, value, keyPath);
	}

	public Class classForCoder() {
		return _CLASS;
	}

	public static Object decodeObject(NSCoder coder) {
		return new NSArray(coder.decodeObjects());
	}

	public void encodeWithCoder(NSCoder coder) {
		coder.encodeObjects(objectsNoCopy());
	}

	public void makeObjectsPerformSelector(NSSelector selector, Object parameters[]) {
		if (selector == null) {
			throw new IllegalArgumentException("Selector cannot be null");
		}
		Object objects[] = objectsNoCopy();
		for (int i = 0; i < objects.length; i++) {
			NSSelector._safeInvokeSelector(selector, objects[i], parameters);
		}

	}

	public int _shallowHashCode() {
		return _NSArrayClassHashCode;
	}

	public int hashCode() {
		if (_mustRecomputeHash()) {
			int hash = 0;
			int max = count() <= 16 ? count() : 16;
			for (int i = 0; i < max; i++) {
				Object element = _objects[i];
				if (element instanceof _NSFoundationCollection) {
					hash ^= ((_NSFoundationCollection) element)._shallowHashCode();
				}
				else {
					hash ^= element.hashCode();
				}
			}

			_hashCache = hash;
			_setMustRecomputeHash(false);
		}
		return _hashCache;
	}

	public Object clone() {
		return this;
	}

	public NSArray<E> immutableClone() {
		return this;
	}

	public NSMutableArray<E> mutableClone() {
		return new NSMutableArray(this);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(128);
		buffer.append("(");
		Object objects[] = objectsNoCopy();
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
			}
			else {
				buffer.append(object.toString());
			}
		}

		buffer.append(")");
		return new String(buffer);
	}

	protected boolean _mustRecomputeHash() {
		return _recomputeHashCode;
	}

	protected void _setMustRecomputeHash(boolean change) {
		_recomputeHashCode = change;
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		java.io.ObjectOutputStream.PutField fields = s.putFields();
		fields.put("objects", ((Object) (objects())));
		s.writeFields();
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		java.io.ObjectInputStream.GetField fields = null;
		fields = s.readFields();
		Object values[] = (Object[]) (Object[]) fields.get("objects", ((Object) (_NSUtilities._NoObjectArray)));
		values = values != null ? values : _NSUtilities._NoObjectArray;
		initFromObjects(values, 0, values.length, true);
	}

	private Object readResolve() throws ObjectStreamException {
		if (getClass() == _CLASS && count() == 0) {
			return EmptyArray;
		}
		else {
			return this;
		}
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
			throw new NullPointerException("com.webobjects.foundation.NSArray does not support null values");
		}
		else {
			return containsObject(element);
		}
	}

	public Iterator<E> iterator() {
		return new _NSJavaArrayListIterator(_objects, _count);
	}

	public Object[] toArray() {
		return objects();
	}

	public <T> T[] toArray(T objects[]) {
		NSArray array = arrayByAddingObjectsFromArray(new NSArray(objects));
		return (T[]) array.objects();
	}

	public boolean containsAll(Collection<?> c) {
		Object objects[] = c.toArray();
		if (objects.length > 0) {
			for (int i = 0; i < objects.length; i++) {
				if (objects[i] == null) {
					return false;
				}
				if (_findObjectInArray(0, count(), objects[i], false) == -1) {
					return false;
				}
			}

		}
		return true;
	}

	public ListIterator listIterator() {
		return new _NSJavaArrayListIterator(_objects, _count);
	}

	public ListIterator listIterator(int index) {
		return new _NSJavaArrayListIterator(_objects, _count, index);
	}

	public E get(int index) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException("Index " + index + " is out of bounds");
		}
		else {
			return objectAtIndex(index);
		}
	}

	public E set(int index, E element) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException("Index " + index + " is out of bounds");
		}
		else {
			throw new UnsupportedOperationException("Set is not a support operation in com.webobjects.foundation.NSArray");
		}
	}

	public int indexOf(Object element) {
		if (element == null) {
			throw new NullPointerException("com.webobjects.foundation.NSArray does not support null values");
		}
		else {
			return indexOfObject(element);
		}
	}

	public int lastIndexOf(Object element) {
		int lastIndex = -1;
		if (element == null) {
			throw new NullPointerException("com.webobjects.foundation.NSArray does not support null values");
		}
		for (int i = 0; i < _objects.length; i++) {
			if (_objects[i].equals(element)) {
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

	public boolean retainAll(Collection collection) {
		throw new UnsupportedOperationException("RetainAll is not a supported operation in com.webobjects.foundation.NSArray");
	}

	public boolean removeAll(Collection<?> collection) {
		throw new UnsupportedOperationException("RemoveAll is not a supported operation in com.webobjects.foundation.NSArray");
	}

	public List subList(int fromIndex, int toIndex) {
		if (fromIndex < 0 || toIndex > count() || fromIndex > toIndex) {
			throw new IndexOutOfBoundsException("Illegal index value (fromIndex < 0 || toIndex > size || fromIndex > toIndex)");
		}
		else {
			return subarrayWithRange(new NSRange(fromIndex, (toIndex - fromIndex) + 1));
		}
	}

	static {
		_CLASS = _NSUtilitiesExtra._classWithFullySpecifiedNamePrime("com.webobjects.foundation.NSArray");
		try {
			setOperatorForKey("count", new _CountOperator());
			setOperatorForKey("max", new _MaxOperator());
			setOperatorForKey("min", new _MinOperator());
			setOperatorForKey("sum", new _SumNumberOperator());
			setOperatorForKey("avg", new _AvgNumberOperator());
			_NSArrayClassHashCode = _CLASS.hashCode();
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

/*
 * DECOMPILATION REPORT
 * 
 * Decompiled from:
 * /System/Library/Frameworks/JavaFoundation.framework/Resources/Java/javafoundation.jar
 * Total time: 164 ms Jad reported messages/errors: Overlapped try statements
 * detected. Not all exception handlers will be resolved in the method
 * operatorNames Overlapped try statements detected. Not all exception handlers
 * will be resolved in the method setOperatorForKey Overlapped try statements
 * detected. Not all exception handlers will be resolved in the method
 * operatorForKey Overlapped try statements detected. Not all exception handlers
 * will be resolved in the method removeOperatorForKey Exit status: 0 Caught
 * exceptions:
 */