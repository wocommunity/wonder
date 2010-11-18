package er.extensions.foundation;

import java.util.Iterator;

import com.webobjects.foundation.NSArray;

/**
 * A simple thread-safe class that returns the next item in a list in round robin fashion.
 * If only one item in the collection, the juggling is bypassed and that same item is returned every time.
 * 
 * @author kieran
 * @param <E> 
 *
 */
public class ERXRoundRobinCollection<E> {
	private final NSArray<E> collection;
	private volatile Iterator<E> iterator;
	private E singleItem;
	
	public ERXRoundRobinCollection(@SuppressWarnings("hiding") final NSArray<E> collection) {
		this.collection = collection.immutableClone();
		if (collection.count() == 1) {
			this.singleItem = collection.objectAtIndex(0);
		} else {
			this.iterator = collection.iterator();
		} //~ if (collection.count() == 1)
		
	}
	
	/**
	 * @return the array of objects in the pool
	 */
	public NSArray<E> array() {
		return collection;
	}
	
	/**
	 * @return the next item in the pool infinitely. When end of the list is reached, the first item in the list is returned.
	 */
	public synchronized E next() {
		if (iterator == null) {
			return singleItem;
		}
		if (!iterator.hasNext()) {
			iterator = collection.iterator();
		}
		return iterator.next();
	}

}
