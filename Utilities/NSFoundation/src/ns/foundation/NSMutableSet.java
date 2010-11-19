package ns.foundation;



import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class NSMutableSet<E> extends NSSet<E> {

  private static final long serialVersionUID = 5500240200143968272L;

  public NSMutableSet() {
		super();
	}
	
	public NSMutableSet(Collection<? extends E> collection) {
		super(collection);
	}
	
	public NSMutableSet(E object) {
		super(object);
	}
	
	public NSMutableSet(E... objects) {
		super(objects);
	}
	
	public NSMutableSet(int capacity) {
		super(capacity);
	}
	
	public NSMutableSet(NSArray<? extends E> objects) {
		super(objects);
	}
	
	public NSMutableSet(NSSet<? extends E> otherSet) {
		super(otherSet);
	}
	
	public NSMutableSet(Set<? extends E> set, boolean ignoreNull) {
		super(set, ignoreNull);
	}
	
	@Override
  protected Set<E> _initializeWithCapacity(int capacity) {
    return _setSet(new HashSet<E>(capacity));
  }
	
	@Override
	public boolean add(E o) {
	  return setNoCopy().add(o);
	};
  
	@Override
	public boolean addAll(Collection<? extends E> c) {
	  return setNoCopy().addAll(c);
	}
	
	public void addObject(E object) {
		add(object);
	}
	
	public void addObjectsFromArray(NSArray<? extends E> array) {
		addAll(array);
	}
	
	@Override
	public void clear() {
	  setNoCopy().clear();
	}
	
	@Override
	public NSMutableSet<E> clone() {
	  return mutableClone();
	}
	
	@Override
	public NSMutableSet<E> mutableClone() {
	  return new NSMutableSet<E>(this);
	}
	
	@Override
	public NSSet<E> immutableClone() {
	  return new NSSet<E>(this);
	}
	
	@Override
	public int _shallowHashCode() {
	  return NSMutableSet.class.hashCode();
	}
	
	@Override
	public boolean remove(Object o) {
	  return setNoCopy().remove(o);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
	  return setNoCopy().removeAll(c);
	}
	
	public void removeAllObjects() {
		clear();
	}
	
	@SuppressWarnings("unchecked")
  public E removeObject(Object object) {
		if (remove(object))
			return (E) object;
		return null;
	}
	
  public void setSet(NSSet<? extends E> otherSet) {
		clear();
		addAll(otherSet);
	}
	
	public void subtractSet(NSSet<? extends E> otherSet) {
		removeAll(otherSet);
	}
	
	public void unionSet(NSSet<? extends E> otherSet) {
		addAll(otherSet);
	}
}
