package er.neo4jadaptor.utils.iteration;

import java.util.Iterator;

public final class EmptyIterator <T> implements Iterator<T> {
	public static final EmptyIterator<?> instance = new EmptyIterator<Object>();
	
	private EmptyIterator() {
		
	}
	
	public boolean hasNext() {
		return false;
	}

	public T next() {
		throw new UnsupportedOperationException();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}
