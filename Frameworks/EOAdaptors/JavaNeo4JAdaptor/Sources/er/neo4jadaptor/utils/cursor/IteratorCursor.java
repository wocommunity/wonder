package er.neo4jadaptor.utils.cursor;

import java.util.Iterator;

public class IteratorCursor <T> implements Cursor<T> {
	private final Iterator<T> it;
	
	public IteratorCursor(Iterator<T> it) {
		this.it = it;
	}
	
	public boolean hasNext() {
		return it.hasNext();
	}

	public T next() {
		return it.next();
	}

	public void remove() {
		it.remove();
	}
	
	public void close() {
		// do nothing
	}
}