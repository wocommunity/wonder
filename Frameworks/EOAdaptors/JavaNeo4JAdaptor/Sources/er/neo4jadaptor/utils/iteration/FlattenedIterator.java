package er.neo4jadaptor.utils.iteration;

import java.util.Iterator;

public class FlattenedIterator <V> implements Iterator<V> {
	@SuppressWarnings("unused")
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FlattenedIterator.class);
	
	private final Iterator<? extends Iterator<V>> it;
	
	private Iterator<V> internal;
	
	public FlattenedIterator(Iterator<? extends Iterator<V>> it) {
		this.it = it;
	}
	
	protected Iterator<? extends Iterator<V>> external() {
		return it;
	}
	
	protected Iterator<V> internal() {
		return internal;
	}
	
	private boolean internalHasNext() {
		return internal != null && internal.hasNext();
	}
	
	public boolean hasNext() {
		while (! internalHasNext() && it.hasNext()) {
			internal = it.next();
		}
		return internal != null && internal.hasNext();
	}
	
	public V next() {
		return internal.next();
	}
	
	public void remove() {
		internal.remove();
	}
}
