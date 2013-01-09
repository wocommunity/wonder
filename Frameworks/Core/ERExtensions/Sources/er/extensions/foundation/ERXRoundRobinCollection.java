package er.extensions.foundation;

import java.util.Iterator;

import com.webobjects.foundation.NSArray;

/**
 * A simple thread-safe class that returns the next item in a list in round robin fashion.
 * If only one item in the collection, the juggling is bypassed and that same item is returned every time.
 * 
 * @author kieran
 * @param <E> class of the collection's items
 */
public class ERXRoundRobinCollection<E> {
	private final NSArray<E> _collection;
	private volatile Iterator<E> _iterator;
	private E _singleItem;
	
	public ERXRoundRobinCollection(final NSArray<E> collection) {
		_collection = collection.immutableClone();
		if (collection.count() == 1) {
			_singleItem = collection.objectAtIndex(0);
		} else {
			_iterator = collection.iterator();
		} //~ if (collection.count() == 1)
		
	}
	
	/**
	 * @return the array of objects in the pool
	 */
	public NSArray<E> array() {
		return _collection;
	}
	
	/**
	 * @return the next item in the pool infinitely. When end of the list is reached, the first item in the list is returned.
	 */
	public synchronized E next() {
		if (_iterator == null) {
			return _singleItem;
		}
		if (!_iterator.hasNext()) {
			_iterator = _collection.iterator();
		}
		return _iterator.next();
	}

}
