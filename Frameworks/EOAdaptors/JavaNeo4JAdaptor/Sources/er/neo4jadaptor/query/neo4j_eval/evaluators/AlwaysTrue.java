package er.neo4jadaptor.query.neo4j_eval.evaluators;

import org.neo4j.graphdb.PropertyContainer;

import er.neo4jadaptor.query.neo4j_eval.Cost;


/**
 * Evaluates all objects as matching its criteria, without even checking.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public final class AlwaysTrue<T extends PropertyContainer> implements Evaluator<T> {
	private static AlwaysTrue instance = new AlwaysTrue();
	
	public static <T extends PropertyContainer> AlwaysTrue<T> instance() {
		return instance;
	}
	
	private AlwaysTrue() {
		
	}
	
	public boolean evaluate(PropertyContainer candidate) {
		return true;
	}

	public Cost getCost() {
		return Cost.NONE;
	}
}
