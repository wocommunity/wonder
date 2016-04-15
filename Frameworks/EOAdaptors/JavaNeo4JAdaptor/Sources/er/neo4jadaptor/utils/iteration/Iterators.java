package er.neo4jadaptor.utils.iteration;

import java.util.Iterator;

public class Iterators {
	public static <T> Iterator<T> singleton(T t) {
		return new SingleIterator<T>(t);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Iterator<T> empty() {
		return (Iterator<T>) EmptyIterator.instance;	// this cast is safe
	}
	
	static final class SingleIterator <T> implements Iterator<T> {
		private T t;
		
		public SingleIterator(T t) {
			this.t = t;
		}
		
		public boolean hasNext() {
			return t != null;
		}
		
		public T next() {
			T ret = t;
			
			t = null;
			
			return ret;
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
