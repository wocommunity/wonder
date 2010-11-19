package ns.foundation;



import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class NSSet<E> extends AbstractSet<E> implements Set<E>, _NSFoundationCollection, Cloneable, Serializable {

  private static final long serialVersionUID = -2249197480070093501L;

  public static final boolean CheckForNull = true;
  @SuppressWarnings("rawtypes")
  public static final NSSet EmptySet = new NSSet();
	public static final boolean IgnoreNull = true;
	
	protected static final String NULL_NOT_ALLOWED = "Attempt to insert null into an NSSet.";
	
	private Set<E> _backingStore;
	
	public NSSet() {
	  _initializeWithCapacity(0);
	}
	
	protected NSSet(int capacity) {
	  _initializeWithCapacity(capacity);
	}
	
  public NSSet(Collection<? extends E> collection) {
	  if (collection == null)
	    throw new IllegalArgumentException("objects may not be null");

	  _initializeWithCollection(collection, NullHandling.CheckAndFail);
	}
	
	public NSSet(E object) {
    if (object == null)
      throw new IllegalArgumentException("object may not be null");
	  _initializeWithCapacity(1).add(object);
	}
	
	public NSSet(E... objects) {
    if (objects == null)
      throw new IllegalArgumentException("objects may not be null");
    
	  _initializeWithObjects(objects, NullHandling.CheckAndFail);
	}
	
	public NSSet(NSArray<? extends E> objects) {
    if (objects == null)
      throw new IllegalArgumentException("objects may not be null");

    _initializeWithCollection(objects, NullHandling.CheckAndFail);

	}
	
	public NSSet(NSSet<? extends E> otherSet) {
	  this(otherSet, false);
	}
	
  public NSSet(Set<? extends E> set, boolean ignoreNull) {
    if (set == null)
      throw new IllegalArgumentException("set may not be null");

    _initializeWithCollection(set, ignoreNull ? NullHandling.CheckAndSkip : NullHandling.CheckAndFail);
	}
	
  protected Set<E> setNoCopy() {
    return _backingStore;
  }
  
  protected Set<E> _setSet(Set<E> set) {
    return _backingStore = set;
  }
  
  protected Set<E> _initializeWithCapacity(int capacity) {
    Set<E> set = new HashSet<E>(capacity);
    _setSet(Collections.unmodifiableSet(set));
    return set;
  }
	
  protected void _initializeWithObjects(E[] objects, NullHandling nullHandling) {
    Set<E> store = _initializeWithCapacity(objects.length);
    if (nullHandling == NullHandling.NoCheck) {
      store.addAll(Arrays.asList(objects));
      return;
    }
    for (E e : objects) {
      if (e == null) {
        if (nullHandling == NullHandling.CheckAndFail)
          throw new IllegalArgumentException(NULL_NOT_ALLOWED);
      } else {
        store.add(e);
      }
    }
  }

  protected void _initializeWithCollection(Collection<? extends E> collection, NullHandling nullHandling) {
    if (collection == null) {
      _initializeWithCapacity(0);
      return;
    }
    Set<E> store = _initializeWithCapacity(collection.size());
    if (nullHandling == NullHandling.NoCheck || collection instanceof _NSFoundationCollection) {
      store.addAll(collection);
      return;
    }
    for (E e : collection) {
      if (e == null) {
        if (nullHandling == NullHandling.CheckAndFail)
          throw new IllegalArgumentException(NULL_NOT_ALLOWED);
        continue;
      }
      store.add(e);
    }
  }

  public static <E> NSSet<E> asSet(Set<E> set) {
    return asSet(set, NullHandling.CheckAndFail);
  }
  
  public static <E> NSSet<E> asSet(Set<E> set, NullHandling nullHandling) {
    if (set == null || set.size() == 0)
      return emptySet();
    if (set.getClass() == NSSet.class)
      return (NSSet<E>)set;
    return _initializeNSSetWithSet(new NSSet<E>(), Collections.unmodifiableSet(set), nullHandling);
  }

  public static <E> NSMutableSet<E> asMutableSet(Set<E> set) {
    return asMutableSet(set, NullHandling.CheckAndFail);
  }
  
  public static <E> NSMutableSet<E> asMutableSet(Set<E> set, NullHandling nullHandling) {
    if (set == null)
      return new NSMutableSet<E>();
    if (set.getClass() == NSMutableSet.class)
      return (NSMutableSet<E>)set;
    return _initializeNSSetWithSet(new NSMutableSet<E>(), set, nullHandling);
  }
  

  protected static <E, T extends NSSet<E>> T _initializeNSSetWithSet(T nsset, Set<E> set, NullHandling nullHandling) {
    if (set == null)
      throw new IllegalArgumentException("set may not be null");
  
    if (nullHandling != NullHandling.NoCheck && set.size() > 0) {
      try {
        if (set.contains(null)) {
          if (nullHandling == NullHandling.CheckAndFail)
            throw new IllegalArgumentException(NULL_NOT_ALLOWED);
          nsset._initializeWithCollection(set, nullHandling);
          return nsset;
        }
      } catch (NullPointerException e) {
        // Must not support nulls either 
      }
    }
    nsset._setSet((Set<E>)set);
    
    return nsset;
  }
  
	public NSArray<E> allObjects() {
		return new NSArray<E>(this);
	}
	
	public E anyObject() {
		if (isEmpty())
			return null;
		
		int index = 0;
		return (E) allObjects().get(index);
	}
 
  public int count() {
    return size();
  }

  public boolean containsObject(Object object) {
    return contains(object);
  }
	
	@Override
  public NSSet<E> clone() {
		return this;
	}
	
	@SuppressWarnings("unchecked")
  public static <T> NSSet<T> emptySet() {
		return EmptySet;
	}
	
	@Override
  public int _shallowHashCode() {
	  return NSSet.class.hashCode();
	}
	
	@Override
  public int hashCode() {
	  return _shallowHashCode() ^ count();
	}
	 
  @Override
  public boolean equals(Object obj) {
	  if (obj == this || obj == setNoCopy())
	    return true;
    if (obj instanceof NSSet<?> && setNoCopy() == ((NSSet<?>)obj).setNoCopy())
      return true;
	  return super.equals(obj);
	}
	
	public HashSet<E> hashSet() {
		return new HashSet<E>(this);
	}
	
	public NSSet<E> immutableClone() {
		return this;
	}
	
	public boolean intersectsSet(NSSet<?> otherSet) {		
		for (Object o : otherSet) {
			if (containsObject(o)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isEqualToSet(NSSet<?> otherSet) {
		return equals(otherSet);
	}
	
	public boolean isSubsetOfSet(NSSet<?> otherSet) {		
		for (Object o : this) {
			if (!otherSet.containsObject(o)) {
				return false;
			}
		}
		return true;
	}
	
	public Object member(Object object) {
	  if (!contains(object)) 
	    return null;

	  for(Object element : this) {
	    if (element.equals(object))
	      return element;
	  }
	  return null;
	}
	
	public NSMutableSet<E> mutableClone() {
		return new NSMutableSet<E>(this);
	}
	
	@Override
  public Iterator<E> iterator() {
    return setNoCopy().iterator();
  }
	
	@Override
  public int size() {
    return setNoCopy().size();
  }

  public NSSet<E> setByIntersectingSet(NSSet<?> otherSet) {
		NSMutableSet<E> result = new NSMutableSet<E>();
		
		for (E e : this) {
			if (otherSet.contains(e)) {
				result.add(e);
			}
		}
		
		return result;
	}
	
	public NSSet<E> setBySubtractingSet(NSSet<?> otherSet) {
		NSSet<E> result = mutableClone();
		result.removeAll(otherSet);
		return result;
	}
	
	public NSSet<E> setByUnioningSet(NSSet<? extends E> otherSet) {
		NSSet<E> result = mutableClone();
		result.addAll(otherSet);
		return result;
	}
		
}
