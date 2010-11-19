package ns.foundation;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Vector;

public class NSArray<E> extends AbstractList<E> implements Cloneable, Serializable, NSKeyValueCoding, NSKeyValueCodingAdditions, _NSFoundationCollection,
    List<E> {
  
  public static class _AvgNumberOperator extends _Operator implements Operator {
    @Override
    public Object compute(NSArray<?> values, String keyPath) {
      int count = values.count();
      if (count != 0) {
        BigDecimal sum = _sum(values, keyPath);
        return sum.divide(new BigDecimal(count), sum.scale() + 4, 6);
      }
      return null;
    }
  }

  public static class _SumNumberOperator extends _Operator implements Operator {
    @Override
    public Object compute(NSArray<?> values, String keyPath) {
      return _sum(values, keyPath);
    }
  }

  public static class _MinOperator extends _Operator implements Operator {
    @Override
    public Object compute(NSArray<?> values, String keyPath) {
      Object min = null;
      for (Object obj : values) {
        min = _minOrMaxValue(min, _operationValue(obj, keyPath), false);
      }
      return min;
    }
  }

  public static class _MaxOperator extends _Operator implements Operator {
    @Override
    public Object compute(NSArray<?> values, String keyPath) {
      Object max = null;
      for (Object obj : values) {
        max = _minOrMaxValue(max, _operationValue(obj, keyPath), true);
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
        if (object instanceof Number)
          return new BigDecimal(object.toString());
        if (object instanceof Boolean)
          return new BigDecimal((Boolean) object ? 1 : 0);
        if (object instanceof String)
          return new BigDecimal((String) object);
        throw new IllegalStateException("Can't convert " + object + " (class " + object.getClass().getName() + ") into number");
      }
      return null;
    }

    BigDecimal _sum(NSArray<?> values, String keyPath) {
      BigDecimal sum = _BigDecimalZero;
      for (Object obj : values) {
        BigDecimal value = _bigDecimalForValue(_operationValue(obj, keyPath));
        if (value != null)
          sum = sum.add(value);
      }
      return sum;
    }

    @SuppressWarnings("unchecked")
    Object _minOrMaxValue(Object referenceValue, Object compareValue, boolean trueForMaxAndFalseForMin) {
      if (referenceValue == null)
        return compareValue;
      if (compareValue == null)
        return referenceValue;

      int comparison;
      if (referenceValue instanceof Number || referenceValue instanceof Boolean) {
        Comparable<?> refValue = (Comparable<?>) referenceValue;
        if (referenceValue instanceof Boolean)
          refValue = ((Boolean) referenceValue) ? 1 : 0;
        Comparable<?> compValue = (Comparable<?>) compareValue;
        if (compareValue instanceof Boolean)
          compValue = ((Boolean) compareValue) ? 1 : 0;
        comparison = ((Comparable<Object>) refValue).compareTo(compValue);
      } else if (referenceValue instanceof NSTimestamp) {
        comparison = ((NSTimestamp) referenceValue).compare((NSTimestamp) compareValue);
      } else if (referenceValue instanceof Comparable) {
        comparison = ((Comparable<Object>) referenceValue).compareTo(compareValue);
      } else {
        throw new IllegalStateException("Cannot compare values " + referenceValue + " and " + compareValue + " (they are not instance of Comparable");
      }
      if (trueForMaxAndFalseForMin) {
        if (comparison >= 0) {
          return referenceValue;
        }
      } else if (comparison <= 0) {
        return referenceValue;
      }
      return compareValue;
    }
  }

  public static class _CountOperator implements Operator {
    @Override
    public Object compute(NSArray<?> values, String keyPath) {
      return values.count();
    }
  }

  public static interface Operator {
    public Object compute(NSArray<?> values, String keyPath);
  }

  private static final char _OperatorIndicatorChar = '@';
  public static final String AverageOperatorName = "avg";
  public static final String CountOperatorName = "count";
  public static final String MaximumOperatorName = "max";
  public static final String MinimumOperatorName = "min";
  public static final String SumOperatorName = "sum";
  public static final int NotFound = -1;

  protected static final BigDecimal _BigDecimalZero = new BigDecimal(0);
  static final long serialVersionUID = -3789592578296478260L;

  @SuppressWarnings("rawtypes")
  public static final NSArray EmptyArray = new NSArray();
  public static final boolean CheckForNull = true;
  public static final boolean IgnoreNull = true;
  public static final boolean NoCopy = true;

  protected static final String NULL_NOT_ALLOWED = "Attempt to insert null into an NSArray.";
  protected static final String NULL_NOT_SUPPORTED = "NSArray does not support null values";
  private static NSMutableDictionary<String, Operator> _operators = new NSMutableDictionary<String, Operator>(8);

  private List<E> _backingStore;

  static {
    try {
      setOperatorForKey(CountOperatorName, new _CountOperator());
      setOperatorForKey(MaximumOperatorName, new _MaxOperator());
      setOperatorForKey(MinimumOperatorName, new _MinOperator());
      setOperatorForKey(SumOperatorName, new _SumNumberOperator());
      setOperatorForKey(AverageOperatorName, new _AvgNumberOperator());
    } catch (Throwable e) {
      NSLog.err.appendln("Exception occurred in initializer");
      if (NSLog.debugLoggingAllowedForLevel(1)) {
        NSLog.debug.appendln(e);
      }
      throw NSForwardException._runtimeExceptionForThrowable(e);
    }
  }

  public NSArray() {
    _initializeListWithCapacity(0);
  }

  protected NSArray(int capacity) {
    _initializeListWithCapacity(capacity);
  }

  public NSArray(E object) {
    if (object == null)
      throw new IllegalArgumentException(NULL_NOT_ALLOWED);
    _initializeListWithCapacity(1).add(object);
  }

  public NSArray(E... objects) {
    _initializeWithObjects(objects, new NSRange(0, objects != null ? objects.length : 0), NullHandling.CheckAndSkip);
  }

  public NSArray(E[] objects, NSRange range) {
    _initializeWithObjects(objects, range != null ? range : NSRange.ZeroRange, NullHandling.CheckAndFail);
  }

  public NSArray(NSArray<? extends E> otherArray) {
    _initializeWithList(otherArray, otherArray == null ? null : new NSRange(0, otherArray.size()), NullHandling.NoCheck, !NoCopy);
  }

  public NSArray(Collection<? extends E> collection, boolean checkForNull) {
    if (collection == null)
      throw new NullPointerException("collection may not be null");

    _initializeWithCollection(collection, checkForNull ? NullHandling.CheckAndFail : NullHandling.NoCheck);
  }

  public NSArray(Collection<? extends E> collection) {
    this(collection, true);
  }

  public NSArray(List<? extends E> list, boolean checkForNull) {
    if (list == null)
      throw new NullPointerException("list may not be null");

    _initializeWithList(list, new NSRange(0, list.size()), checkForNull ? NullHandling.CheckAndFail : NullHandling.NoCheck, !NoCopy);
  }

  public NSArray(List<? extends E> list, NSRange range, boolean ignoreNull) {
    if (list == null)
      throw new IllegalArgumentException("list may not be null");

    _initializeWithList(list, range != null ? range : NSRange.ZeroRange, ignoreNull ? NullHandling.CheckAndSkip : NullHandling.CheckAndFail, !NoCopy);
  }

  protected List<E> _initializeListWithCapacity(int capacity) {
    List<E> list = new ArrayList<E>(capacity);
    _setList(Collections.unmodifiableList(list));
    return list;
  }
  
  protected void _initializeWithCapacity(int capacity) {
    _initializeListWithCapacity(capacity);
  }


  protected void _initializeWithObjects(E[] objects, NSRange range, NullHandling nullHandling) {
    if (objects == null) {
      if (range == null || range.length() == 0) {
        _initializeListWithCapacity(0);
        return;
      }
      throw new NullPointerException("objects cannot be null");
    }

    _initializeWithList(Arrays.asList(objects), range, nullHandling, !NoCopy);
  }

  @SuppressWarnings("unchecked")
  protected void _initializeWithCollection(Collection<? extends E> collection, NullHandling nullHandling) {
    if (collection == null) {
      _initializeListWithCapacity(0);
      return;
    }
    if (collection instanceof List) {
      _initializeWithList((List<E>) collection, new NSRange(0, collection.size()), nullHandling, !NoCopy);
    } else {
      List<E> store = _initializeListWithCapacity(collection.size());
      if (nullHandling == NullHandling.NoCheck || collection instanceof _NSFoundationCollection) {
        store.addAll(collection);
        return;
      }
      for (E element : collection) {
        if (element == null) {
          if (nullHandling == NullHandling.CheckAndFail)
            throw new IllegalArgumentException(NULL_NOT_ALLOWED);
          continue;
        }
        store.add(element);
      }
    }
  }

  protected void _initializeWithList(List<? extends E> list, NSRange range, NullHandling nullHandling, boolean noCopy) {
    if (list == null && (range == null || range.length() == 0)) {
      _initializeListWithCapacity(0);
      return;
    }
    if (list == null)
      throw new NullPointerException("list cannot be null");
    
    if (list instanceof _NSFoundationCollection || nullHandling == NullHandling.NoCheck) {
      List<? extends E> subList = list;
      if (range.location() != 0 || range.length() != list.size()) {
        /* GWT has no java.util.List.subList so use ours */
        subList = asNSArray(list, NullHandling.NoCheck).subList(range.location(), range.maxRange());
      }
      if (noCopy) {
        _setList(subList); 
      } else {
        _initializeListWithCapacity(subList.size()).addAll(subList);
      }
    } else {
      List<E> store = _initializeListWithCapacity(list.size());
      for (int i = range.location(); i < range.maxRange(); i++) {
        E element = list.get(i);
        if (element == null) {
          if (nullHandling == NullHandling.CheckAndFail || nullHandling != NullHandling.CheckAndSkip)
            throw new IllegalArgumentException(NULL_NOT_ALLOWED);
          continue;
        }
        store.add(element);
      }
    }
  }

  protected List<E> listNoCopy() {
    return _backingStore;
  }

  @SuppressWarnings("unchecked")
  protected List<E> _setList(List<? extends E> list) {
    return _backingStore = (List<E>) list;
  }
  
  @SuppressWarnings("serial")
  static class RandomAccessNSArray<E> extends NSArray<E> implements RandomAccess { }

  public static <E> NSArray<E> asNSArray(E... objects) {
    return asNSArray(objects, NullHandling.CheckAndFail);
  }

  public static <E> NSArray<E> asNSArray(List<E> list) {
    return asNSArray(list, NullHandling.CheckAndFail);
  }

  public static <E> NSArray<E> asNSArray(E[] array, NullHandling nullHandling) {
    return asNSArray(Arrays.asList(array), nullHandling);
  }

  public static <E> NSArray<E> asNSArray(List<E> list, NullHandling nullHandling) {
    return asNSArray(list, list == null ? null : new NSRange(0, list.size()), nullHandling);
  }
  
  public static <E> NSArray<E> asNSArray(List<E> list, NSRange range, NullHandling nullHandling) {
    if (list == null || list.size() == 0)
      return emptyArray();
    if (list.getClass() == NSArray.class && range != null) {
      /* GWT has no java.util.List.subList so use ours */
      return (NSArray<E>) ((NSArray<E>)list).subList(range.location(), range.maxRange());
    }
    NSArray<E> array = list instanceof RandomAccess ? new RandomAccessNSArray<E>() : new NSArray<E>();
    array._initializeWithList(Collections.unmodifiableList(list), range, nullHandling, NoCopy);
    return array;
  }

  public NSArray<E> arrayByAddingObject(E object) {
    if (object == null)
      throw new IllegalArgumentException("object may not be null");
    NSMutableArray<E> result = this.mutableClone();
    result.addObject(object);
    return result;
  }

  public NSArray<E> arrayByAddingObjectsFromArray(NSArray<E> otherArray) {
    if (otherArray == null || otherArray.count() == 0)
      return new NSArray<E>(this);
    NSMutableArray<E> result = this.mutableClone();
    result.addObjectsFromArray(otherArray);
    return result;
  }

  public ArrayList<E> arrayList() {
    return new ArrayList<E>(this);
  }

  @Override
  public NSArray<E> clone() {
    return this;
  }

  public String componentsJoinedByString(String separator) {
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < size(); i++) {
      result.append(objectAtIndex(i).toString());
      if (i < size() - 1 && separator != null)
        result.append(separator);
    }
    return result.toString();
  }

  public static NSArray<String> componentsSeparatedByString(String string, String separator) {
    NSMutableArray<String> objects;
    if ((string == null) || (string.length() == 0)) {
      return NSArray.emptyArray();
    }
    int stringLength = string.length();

    if ((separator == null) || (separator.length() == 0)) {
      return new NSArray<String>(string);
    }
    int separatorLength = separator.length();

    int start = 0;
    int index = 0;
    int count = 0;

    if ((separatorLength == 1) && (stringLength < 256)) {
      char[] parseData = string.toCharArray();
      char charSeparator = separator.charAt(0);

      for (int i = 0; i < stringLength; i++) {
        if (parseData[i] == charSeparator) {
          count++;
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

  public boolean containsObject(Object object) {
    if (object == null)
      return false;
    return listNoCopy().contains(object);
}

  public int count() {
    return listNoCopy().size();
  }

  @SuppressWarnings("unchecked")
  public static <T> NSArray<T> emptyArray() {
    return EmptyArray;
  }

  public E firstObjectCommonWithArray(NSArray<? extends E> otherArray) {
    if (otherArray == null || otherArray.isEmpty())
      return null;

    NSSet<E> set = new NSSet<E>(otherArray);
    for (E e : this) {
      if (set.containsObject(e))
        return e;
    }

    return null;
  }

  @Override
  public int _shallowHashCode() {
    return NSArray.class.hashCode();
  }

  @Override
  public int hashCode() {
    int hash = 1;
    int index = 0;
    Iterator<E> i = iterator();
    while (i.hasNext() && index <= 16) {
      E element = i.next();
      index++;
      if (element instanceof _NSFoundationCollection) {
        hash ^= ((_NSFoundationCollection) element)._shallowHashCode();
      } else {
        hash ^= element.hashCode();
      }
    }

    return hash;
  }

  public NSArray<E> immutableClone() {
    return new NSArray<E>(this);
  }

  public int indexOfIdenticalObject(Object object) {
    if (object == null)
      return NotFound;
    for (int i = 0; i < size(); i++) {
      if (objectAtIndex(i) == object)
        return i;
    }
    return NotFound;
  }

  public int indexOfIdenticalObject(Object object, NSRange range) {
    if (object == null || range == null)
      return NotFound;

    if (range.maxRange() > count()) {
      throw new IllegalArgumentException("Range [" + range.location() + "; " + range.length() + "] out of bounds [0, " + (count() - 1) + "]");
    }

    NSArray<E> subArray = subarrayWithRange(range);
    return subArray.indexOfIdenticalObject(object) + range.location();
  }

  public int indexOfObject(Object object) {
    if (object == null) {
      return NotFound;
    }
    return listNoCopy().indexOf(object);
  }

  public int indexOfObject(Object object, NSRange range) {
    if (object == null || range == null)
      return NotFound;
    if (range.maxRange() > count())
      throw new IllegalArgumentException("range exceeds array dimensions");
    NSArray<E> subArray = subarrayWithRange(range);
    return subArray.indexOfObject(object) + range.location();
  }

  public boolean isEqualToArray(NSArray<?> otherArray) {
    return equals(otherArray);
  }

  public E lastObject() {
    int count = count();
    return count != 0 ? objectAtIndex(count - 1) : null;
  }

  public void makeObjectsPerformSelector(NSSelector<?> selector, Object... parameters) {
    if (selector == null) {
      throw new IllegalArgumentException("Selector cannot be null");
    }
    for (E element : this) {
      NSSelector._safeInvokeSelector(selector, element, parameters);
    }
  }

  public NSMutableArray<E> mutableClone() {
    return new NSMutableArray<E>(this);
  }

  public E objectAtIndex(int index) {
    if (index >= count())
      throw new IndexOutOfBoundsException("Index " + index + " is out of bounds");
    return listNoCopy().get(index);
  }

  public Enumeration<E> objectEnumerator() {
    return Collections.enumeration(this);
  }

  public Object[] objects() {
    return toArray();
  }

  public Object[] objects(NSRange range) {
    if (range == null || range.length() == 0)
      return new Object[0];
    return subarrayWithRange(range).toArray();
  }
  
  protected Object[] objectsNoCopy() {
    Object[] objs = listNoCopy().toArray();
    return objs != null ? objs : _NSCollectionPrimitives.EmptyArray;
  }

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

  public Enumeration<E> reverseObjectEnumerator() {
    return new ListReverseEnumeration();
  }

  public NSArray<E> sortedArrayUsingComparator(NSComparator<? super E> comparator) throws NSComparator.ComparisonException {
    if (comparator == null)
      throw new IllegalArgumentException("Comparator not specified");
    NSArray<E> result = mutableClone();
    Collections.sort(result, comparator);
    return result.immutableClone();
  }

  public NSArray<E> subarrayWithRange(NSRange range) {
    if (range == null || range.length() == 0)
      return NSArray.emptyArray();
    return asNSArray(subList(range.location(), range.maxRange()), NullHandling.NoCheck);
  }

  @Override
  public Object valueForKey(String key) {
    if (key != null) {
      if (key.charAt(0) == _OperatorIndicatorChar) {
        return _valueForKeyPathWithOperator(key);
      }
      if (key.equals(CountOperatorName)) {
        return count();
      }
    }
    NSMutableArray<Object> values = new NSMutableArray<Object>(size());
    for (E element : this) {
      Object value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(element, key);
      values.addObject(value == null ? ((Object) (NSKeyValueCoding.NullValue)) : value);
    }

    return values;
  }

  @Override
  public void takeValueForKey(Object value, String key) {
    for (E element : this)
      NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(element, value, key);
  }

  @Override
  public Object valueForKeyPath(String keyPath) {
    if (keyPath != null && keyPath.charAt(0) == _OperatorIndicatorChar)
      return _valueForKeyPathWithOperator(keyPath);

    return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, keyPath);
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

  @Override
  public void takeValueForKeyPath(Object value, String keyPath) {
    if (keyPath == null)
      return;
    for (E element : this) {
      if (element instanceof NSKeyValueCodingAdditions) {
        ((NSKeyValueCodingAdditions) element).takeValueForKeyPath(value, keyPath);
      }
    }
  }

  public Vector<E> vector() {
    return new Vector<E>(this);
  }

  /* Java Collection methods */

  @Override
  public boolean contains(Object element) {
    if (element == null) {
      throw new NullPointerException(NULL_NOT_SUPPORTED);
    }
    return containsObject(element);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    if (c == null)
      throw new NullPointerException(NULL_NOT_SUPPORTED);
    List<?> list = listNoCopy();
    if (c == this || c == list)
      return true;
    if (c instanceof NSArray<?> && ((NSArray<?>) c).listNoCopy() == list)
      return true;
    return list.containsAll(c);
  }

  @Override
  public E get(int index) {
    return objectAtIndex(index);
  }

  @Override
  public int indexOf(Object o) {
    if (o == null) {
      throw new NullPointerException(NULL_NOT_SUPPORTED);
    }
    return listNoCopy().indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    if (o == null) {
      throw new NullPointerException(NULL_NOT_SUPPORTED);
    }
    return listNoCopy().lastIndexOf(o);
  }

  @Override
  public int size() {
    return count();
  }

  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
      throw new IndexOutOfBoundsException("Illegal index value (fromIndex < 0 || toIndex > size || fromIndex > toIndex)");
    }
    if (fromIndex == 0 && toIndex == size()) {
      return this;
    }
    /* GWT doesn't implement java.util.List.subList so we can't delegate to super */
    return (listNoCopy() instanceof RandomAccess ? new RandomAccessSubList<E>(this, fromIndex, toIndex) : new SubList<E>(this, fromIndex, toIndex));
  }

  /* Because we implement our own SubList we need to redeclare removeRange() and modCount */

  @Override
  protected void removeRange(int fromIndex, int toIndex) {
    super.removeRange(fromIndex, toIndex);
  }

  protected transient int modCount;

  private class ListReverseEnumeration implements Enumeration<E> {
    private int currentIndex = size();

    public ListReverseEnumeration() {
      super();
    }

    @Override
    public boolean hasMoreElements() {
      return currentIndex > 0;
    }

    @Override
    public E nextElement() {
      if (hasMoreElements()) {
        currentIndex--;
        return get(currentIndex);
      }
      throw new NoSuchElementException();
    }
  }
}

/* Copied from java.util.AbstractList because GWT doesn't implement it */
class SubList<E> extends NSArray<E> {
  final NSArray<E> array;
  int offset;
  int size;
  int expectedModCount;

  SubList(NSArray<E> list, int fromIndex, int toIndex) {
    if (fromIndex < 0)
      throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
    if (toIndex > list.size())
      throw new IndexOutOfBoundsException("toIndex = " + toIndex);
    if (fromIndex > toIndex)
      throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
    array = list;
    offset = fromIndex;
    size = toIndex - fromIndex;
    expectedModCount = list.modCount;
  }

  @Override
  public boolean add(E o) {
    add(size, o);
    return true;
  };

  @Override
  public void add(int index, E element) {
    if (index < 0 || index > size)
      throw new IndexOutOfBoundsException();
    checkForComodification();
    array.add(index + offset, element);
    expectedModCount = array.modCount;
    size++;
    modCount++;
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    return addAll(size, c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    if (index < 0 || index > size)
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    int cSize = c.size();
    if (cSize == 0)
      return false;

    checkForComodification();
    array.addAll(offset + index, c);
    expectedModCount = array.modCount;
    size += cSize;
    modCount++;
    return true;
  }

  @Override
  public void clear() {
    removeRange(0, size());
  }

  @Override
  public boolean contains(Object o) {
    for (E e : this) {
      if (e == o || (e.hashCode() == o.hashCode() && e.equals(0))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    Iterator<?> e = c.iterator();
    while (e.hasNext()) {
      Object o = e.next();
      if (o == null || !contains(o))
        return false;
    }
    return true;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this || obj == listNoCopy())
      return true;
    if (obj instanceof NSArray<?> && listNoCopy() == ((NSArray<?>) obj).listNoCopy())
      return true;
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public E get(int index) {
    rangeCheck(index);
    checkForComodification();
    return array.get(index + offset);
  }

  @Override
  public int indexOf(Object o) {
    ListIterator<E> e = listIterator();
    if (o == null) {
      return NotFound;
    }
    while (e.hasNext())
      if (o.equals(e.next()))
        return e.previousIndex();

    return NotFound;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public int lastIndexOf(Object o) {
    ListIterator<E> e = listIterator(size());
    if (o == null)
      return NotFound;

    while (e.hasPrevious())
      if (o.equals(e.previous()))
        return e.nextIndex();

    return NotFound;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    boolean modified = false;
    Iterator<?> e = iterator();
    while (e.hasNext()) {
      if (c.contains(e.next())) {
        e.remove();
        modified = true;
      }
    }
    return modified;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    boolean modified = false;
    Iterator<E> e = iterator();
    while (e.hasNext()) {
      if (!c.contains(e.next())) {
        e.remove();
        modified = true;
      }
    }
    return modified;
  }

  @Override
  public E remove(int index) {
    rangeCheck(index);
    checkForComodification();
    E result = array.remove(index + offset);
    expectedModCount = array.modCount;
    size--;
    modCount++;
    return result;
  }

  @Override
  public boolean remove(Object o) {
    if (o == null)
      throw new NullPointerException("com.webobjects.foundation.NSArray does not support null values");
    Iterator<?> e = iterator();
    while (e.hasNext()) {
      if (o.equals(e.next())) {
        e.remove();
        return true;
      }
    }
    return false;
  }

  @Override
  public E set(int index, E element) {
    rangeCheck(index);
    checkForComodification();
    return array.set(index + offset, element);
  }

  @Override
  public int size() {
    checkForComodification();
    return size;
  }

  @Override
  protected void removeRange(int fromIndex, int toIndex) {
    checkForComodification();
    array.removeRange(fromIndex + offset, toIndex + offset);
    expectedModCount = array.modCount;
    size -= (toIndex - fromIndex);
    modCount++;
  }

  @Override
  public Iterator<E> iterator() {
    return listIterator();
  }

  @Override
  public ListIterator<E> listIterator(final int index) {
    checkForComodification();
    if (index < 0 || index > size)
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);

    return new ListIterator<E>() {
      private ListIterator<E> i = array.listIterator(index + offset);

      @Override
      public boolean hasNext() {
        return nextIndex() < size;
      }

      @Override
      public E next() {
        if (hasNext()) {
          return i.next();
        }
        throw new NoSuchElementException();
      }

      @Override
      public boolean hasPrevious() {
        return previousIndex() >= 0;
      }

      @Override
      public E previous() {
        if (hasPrevious()) {
          return i.previous();
        }
        throw new NoSuchElementException();
      }

      @Override
      public int nextIndex() {
        return i.nextIndex() - offset;
      }

      @Override
      public int previousIndex() {
        return i.previousIndex() - offset;
      }

      @Override
      public void remove() {
        i.remove();
        expectedModCount = array.modCount;
        size--;
        modCount++;
      }

      @Override
      public void set(E o) {
        i.set(o);
      }

      @Override
      public void add(E o) {
        i.add(o);
        expectedModCount = array.modCount;
        size++;
        modCount++;
      }
    };
  }

  @Override
  public NSArray<E> subList(int fromIndex, int toIndex) {
    return new SubList<E>(this, fromIndex, toIndex);
  }

  private void rangeCheck(int index) {
    if (index < 0 || index >= size)
      throw new IndexOutOfBoundsException("Index: " + index + ",Size: " + size);
  }

  private void checkForComodification() {
    if (array.modCount != expectedModCount)
      throw new ConcurrentModificationException();
  }
}

class RandomAccessSubList<E> extends SubList<E> implements RandomAccess {
  RandomAccessSubList(NSArray<E> list, int fromIndex, int toIndex) {
    super(list, fromIndex, toIndex);
  }

  @Override
  public NSArray<E> subList(int fromIndex, int toIndex) {
    return new RandomAccessSubList<E>(this, fromIndex, toIndex);
  }
}
