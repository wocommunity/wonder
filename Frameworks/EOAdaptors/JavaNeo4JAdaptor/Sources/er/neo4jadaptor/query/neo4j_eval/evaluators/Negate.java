package er.neo4jadaptor.query.neo4j_eval.evaluators;

import org.neo4j.graphdb.PropertyContainer;

import er.neo4jadaptor.query.neo4j_eval.Cost;


/**
 * Positively evaluates only candidates that do NOT match some other evaluator, making it logical negation.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public final class Negate<T extends PropertyContainer> implements Evaluator<T> {
	private final Evaluator<T> wrapped;
	
	public Negate(Evaluator<T> wrapped) {
		this.wrapped = wrapped;
	}
	
	public boolean evaluate(T candidate) {
		return ! wrapped.evaluate(candidate);
	}

	public Cost getCost() {
		return wrapped.getCost();
	}
}
