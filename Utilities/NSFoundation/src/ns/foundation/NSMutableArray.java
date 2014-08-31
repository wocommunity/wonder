package ns.foundation;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

public class NSMutableArray<E> extends NSArray<E> {
  
  private static final long serialVersionUID = 1107878723451378786L;

  public NSMutableArray() {
    super();
  }

  public NSMutableArray(int capacity) {
    super(capacity);
  }

  public NSMutableArray(Collection<? extends E> collection) {
    super(collection);
  }

  public NSMutableArray(E object) {
    super(object);
  }

  public NSMutableArray(E... objects) {
    super(objects);
  }

  public NSMutableArray(E[] objects, NSRange range) {
    super(objects, range);
  }

  public NSMutableArray(List<? extends E> list, NSRange range, boolean ignoreNull) {
    super(list, range, ignoreNull);
  }

  public NSMutableArray(NSArray<? extends E> otherArray) {
    super(otherArray);
  }
  
  @Override
  protected List<E> _initializeListWithCapacity(int capacity) {
    return _setList(new ArrayList<E>(capacity));
  }
  
  @Override
  protected void _initializeWithCapacity(int capacity) {
    _initializeListWithCapacity(capacity);
  }
  
  @SuppressWarnings("serial")
  static class RandomAccessNSMutableArray<E> extends NSMutableArray<E> implements RandomAccess { }


  public static <E> NSMutableArray<E> asNSMutableArray(E... objects) {
    return asNSMutableArray(objects, NullHandling.CheckAndFail);
  }

  public static <E> NSMutableArray<E> asNSMutableArray(E[] array, NullHandling nullHandling) {
    return asNSMutableArray(Arrays.asList(array), nullHandling);
  }

  public static <E> NSMutableArray<E> asNSMutableArray(List<E> list, NullHandling nullHandling) {
    return asNSMutableArray(list, list == null ? null : new NSRange(0, list.size()), nullHandling);
  }

  public static <E> NSMutableArray<E> asNSMutableArray(List<E> list, NSRange range, NullHandling nullHandling) {
    if (list == null)
      return new NSMutableArray<E>();
    NSMutableArray<E> array = list instanceof RandomAccess ? new RandomAccessNSMutableArray<E>() : new NSMutableArray<E>();
    array._initializeWithList(list, new NSRange(0, list.size()), nullHandling, NoCopy);
    return array;
  }

  public void addObject(E object) {
    if (object == null)
      throw new IllegalArgumentException(NULL_NOT_ALLOWED);
    if (listNoCopy().add(object)) {
      modCount++;
    }
  }

  public void addObjects(E... objects) {
    if (objects == null)
      return;
    for (E obj : objects) {
      addObject(obj);
    }
  }
  
  public void addObjectsFromCollection(Collection<? extends E> collection) {
    if (collection.size() == 0)
      return;
    for (E obj : collection) {
      addObject(obj);
    }
  }

  public void addObjectsFromArray(NSArray<? extends E> otherArray) {
    if (otherArray.count() == 0)
      return;
    if (listNoCopy().addAll(otherArray)) {
      modCount++;
    }
  }

  @Override
  public int _shallowHashCode() {
    return NSMutableArray.class.hashCode();
  }

  public void insertObjectAtIndex(E object, int index) {
    if (object == null)
      throw new IllegalArgumentException(NULL_NOT_ALLOWED);
    listNoCopy().add(index, object);
    modCount++;  
  }

  public void _moveObjectAtIndexToIndex(int sourceIndex, int destIndex) {
    if (sourceIndex == destIndex)
      return;
    List<E> listNoCopy = listNoCopy();
    listNoCopy.add(destIndex, listNoCopy.remove(sourceIndex));
  }


  public void removeAllObjects() {
    if (count() > 0)
      modCount++;
    listNoCopy().clear();  
  }

  public boolean removeIdenticalObject(Object object) {
    return removeIdenticalObject(object, new NSRange(0, count()));
  }

  public boolean removeIdenticalObject(Object object, NSRange range) {
    boolean modified = false;
    for (int i = range.location(); i < range.maxRange(); i++) {
      if (get(i) == object) {
        removeObjectAtIndex(i);
        modified = true;
      }
    }
    return modified;
  }

  public Object removeLastObject() {
    if (!isEmpty()) {
      return removeObjectAtIndex(size() - 1);
    }
    return null;
  }

  public boolean removeObject(Object e) {
    boolean modified = false;
    while (remove(e)) {
      modified = true;
    }
    return modified;
  }

  public boolean removeObject(Object object, NSRange range) {
    boolean modified = false;
    for (int i = range.location(); i < range.maxRange(); i++) {
      if (get(i).equals(object)) {
        removeObjectAtIndex(i);
        modified = true;
      }
    }
    return modified;
  }

  public E removeObjectAtIndex(int index) {
    E e = listNoCopy().remove(index);
    modCount++;
    return e;
  }

  public void removeObjects(Object... objects) {
    removeAll(new NSArray<Object>(objects));
  }

  public void removeObjectsInArray(NSArray<?> otherArray) {
    removeAll(otherArray);
  }

  public void removeObjectsInRange(NSRange range) {
    NSMutableArray<E> objectsToRemove = new NSMutableArray<E>();
    for (int i = range.location(); i < range.maxRange(); i++) {
      objectsToRemove.add(get(i));
    }
    removeObjectsInArray(objectsToRemove);
  }

  public E replaceObjectAtIndex(E object, int index) {
    if (object == null)
      throw new IllegalArgumentException(NULL_NOT_ALLOWED);
    E e = listNoCopy().set(index, object);
    modCount++;
    return e;
  }

  public void replaceObjectsInRange(NSRange range, NSArray<E> otherArray, NSRange otherRange) {
    if (range == null || otherRange == null)
      throw new IllegalArgumentException("Both ranges cannot be null");
    if (otherArray == null)
      throw new IllegalArgumentException("Other array cannot be null");
    
    int rangeLength = range.length();
    int rangeLocation = range.location();
    int otherRangeLength = otherRange.length();
    int otherRangeLocation = otherRange.location();
    
    while (rangeLength > 0 && otherRangeLength > 0) {
      replaceObjectAtIndex(otherArray.objectAtIndex(otherRangeLocation++), rangeLocation++);
      rangeLength--;
      otherRangeLength--;
    }

    while (otherRangeLength > 0) {
      insertObjectAtIndex(otherArray.objectAtIndex(otherRangeLocation++), rangeLocation++);
      otherRangeLength--;
    }

    while (rangeLength > 0) {
      removeObjectAtIndex(rangeLocation);
      rangeLength--;
    }
  }

  public void setArray(NSArray<? extends E> otherArray) {
    removeAllObjects();
    addObjectsFromArray(otherArray);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void sortUsingComparator(NSComparator comparator) {
    Collections.sort(this, comparator);
    modCount++;
  }

  /*
   * Methods from Java Collections
   *
   * We implement all methods because the backing store might have optimizations for 
   * some of these.
   */
  
  @Override
  public boolean add(E element) {
    addObject(element);
    return true;
  }
  
  @Override
  public void add(int index, E element) {
    insertObjectAtIndex(element, index);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    if (c.size() == 0)
      return false;
    addObjectsFromCollection(c);
    return true;
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    if (c.size() == 0)
      return false;
    if (!(c instanceof _NSFoundationCollection)) {
      try {
        if (c.contains(null))
          throw new IllegalArgumentException(NULL_NOT_ALLOWED);
      } catch (NullPointerException e) {
        // Must not allow nulls either
      }
    }
    if (listNoCopy().addAll(index, c)) {
      modCount++;
      return true;
    }
    return false;
  }

  @Override
  public void clear() {
    removeAllObjects();
  }

  @Override
  public NSMutableArray<E> clone() {
    return mutableClone();
  }

  @Override
  public E remove(int index) {
    return removeObjectAtIndex(index);
  }

  @Override
  public boolean remove(Object o) {
    if (listNoCopy().remove(o)) {
      modCount++;
      return true;
    }
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    if (listNoCopy().removeAll(c)) {
      modCount++;
      return true;
    }
    return false; 
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    if (listNoCopy().retainAll(c)) {
      modCount++;
      return true;
    }
    return false;
  }

  @Override
  public E set(int index, E element) {
    return replaceObjectAtIndex(element, index);
  }
}