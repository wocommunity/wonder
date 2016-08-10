package er.neo4jadaptor.query.neo4j_eval.retrievers;

import java.util.Iterator;

import org.neo4j.graphdb.PropertyContainer;

import er.neo4jadaptor.query.neo4j_eval.Cost;

/**
 * For a {@link Retriever} that returns {@link String} values this one returns the same values
 * but in lowercase. This is intended to be used for case insensitive comparisons.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public class LowercaseRetriever <T extends PropertyContainer> implements Retriever<T, String> {
	private final Retriever<T, String> wrapped;
	
	public LowercaseRetriever(Retriever<T, String> wrapped) {
		this.wrapped = wrapped;
	}
	
	public Iterator<String> retrieve(T node) {
		final Iterator<String> it = wrapped.retrieve(node);
		
		return new Iterator<String>() {

			public boolean hasNext() {
				return it.hasNext();
			}

			public String next() {
				return it.next().toLowerCase();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	public Cost getCost() {
		return wrapped.getCost();
	}
}
