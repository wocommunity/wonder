package er.neo4jadaptor.query.neo4j_eval.evaluators;

import org.neo4j.graphdb.PropertyContainer;

import er.neo4jadaptor.query.neo4j_eval.HasCost;


/**
 * Tells whether objects match some criteria.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public interface Evaluator<T extends PropertyContainer> extends HasCost {
	/**
	 * Perform object evaluation.
	 * 
	 * @param t object to be evaluated
	 * @return <code>true</code> if <code>t</code> matches criteria
	 */
	public boolean evaluate(T t);
}
