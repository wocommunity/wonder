package er.neo4jadaptor.query.neo4j_eval.retrievers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.PropertyContainer;

import er.neo4jadaptor.query.neo4j_eval.Cost;
import er.neo4jadaptor.utils.iteration.MultiLevelIterator;

/**
 * Chains retrievers <code>r_0, r_1, ..., r_K</code> to simulate getting value by key-path.
 * <code>r_0, r_1, ..., r_{K-1}</code> are expected to on retrieval to return {@link PropertyContainer}s
 * while <code>r_K</code> can return objects of any type.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <N>
 * @param <V>
 */
public class ChainedRetriever <N extends PropertyContainer, V> implements Retriever<N, V> {
	private final List<Retriever<N, ?>> chain = new ArrayList<Retriever<N, ?>>();
	
	@SuppressWarnings("unchecked")
	public ChainedRetriever(
			List<Retriever<? extends PropertyContainer, ? extends PropertyContainer>> retrievers, 
			Retriever<? extends PropertyContainer, V> lastRetriever) {
//		chain.addAll((Collection<? extends Retriever<N, ?>>) retrievers);
		for (Retriever<? extends PropertyContainer, ? extends PropertyContainer> r : retrievers) {
			chain.add((Retriever<N, ?>) r);
		}
		chain.add((Retriever<N, ?>) lastRetriever);
	}
	
	public Iterator<V> retrieve(N node) {
		Retriever<N, N>  rootRetriever = getRootRetriever();
		
		return new MultiLevelIterator<N, V>(rootRetriever.retrieve(node), chain.size()) {
			@Override
			protected Iterator<?> createThisLevelIterator(N node, int resultLevelIndex) {
				return chain.get(resultLevelIndex).retrieve(node);
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	private Retriever<N, N> getRootRetriever() {
		// we assume that the chain contains at least 2 values therefore its first element is a retriever that 
		// ..transforms PropertyContainer (Node/Relationship) into another Node/Relationship
		return (Retriever<N, N>) chain.get(0);
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		
		for (Retriever<?, ?> r : chain) {
			if (b.length() > 0) {
				b.append(", ");
			}
			b.append(r.toString());
		}
		
		return b.toString();
	}
	
	public Cost getCost() {
		return Cost.getHighest(chain);
	}
}
