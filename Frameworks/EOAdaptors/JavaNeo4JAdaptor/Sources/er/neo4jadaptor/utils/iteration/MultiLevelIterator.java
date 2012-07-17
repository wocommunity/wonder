package er.neo4jadaptor.utils.iteration;

import java.util.Iterator;


/**
 * Having tree of the fixed depth, where each node is an iterator and this iterator values are child nodes, this class provides 
 * uniform way to iterate over such tree's leaves.
 * 
 * @author jsobanski
 *
 * @param <N> node type
 * @param <L> leaf type
 */
public class MultiLevelIterator <N, L> implements Iterator<L> {
	private final Iterator<?> [] iterators;
	protected int idx = 0;
	
	public MultiLevelIterator(Iterator<N> rootIterator, int depth) {
		this.iterators = new Iterator[depth];
		
		iterators[0] = rootIterator;
	}
	
	private int depth() {
		return iterators.length;
	}
	
	@SuppressWarnings("unchecked")
	public boolean hasNext() {
		while (lastIterator() == null || ! lastIterator().hasNext()) {
			if (idx == 0 && ! iterators[idx].hasNext()) {
				return false;
			}
			// proceed backwards when necessary 
			while (idx > 0 && ! iterators[idx].hasNext()) {
				iterators[idx] = null;
				idx--;
				
			}
			// proceed forward when possible
			while (idx != depth()-1 && iterators[idx].hasNext()) {
				int nextLevel = idx+1;
				N node = (N) iterators[idx].next();
				
				iterators[nextLevel] = createThisLevelIterator(node, nextLevel);
				idx = nextLevel;
			}
			
		}
		return lastIterator() != null && lastIterator().hasNext();
	}
	
	/**
	 * Creates the next level iterator for the current iterator value being <code>currVal</code>.
	 * 
	 * @param currVal
	 * @param resultLevelIndex
	 * @return current level iterator
	 */
	protected Iterator<?> createThisLevelIterator(N currVal, int resultLevelIndex) {
		return ((Iterable<?>) currVal).iterator();
	}

	@SuppressWarnings("unchecked")
	private Iterator<L> lastIterator() {
		return (Iterator<L>) iterators[iterators.length-1];
	}
	
	public L next() {
		return lastIterator().next();
	}

	public void remove() {
		lastIterator().remove();
	}
}
