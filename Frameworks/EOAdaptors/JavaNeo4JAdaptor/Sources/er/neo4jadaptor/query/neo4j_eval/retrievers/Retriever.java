package er.neo4jadaptor.query.neo4j_eval.retrievers;

import java.util.Iterator;

import er.neo4jadaptor.query.neo4j_eval.HasCost;



/**
 * For an object retrieves a value being a set of objects of some type. In other words it's a function (in mathematical terms) f:Arg -&gt; {x:Value} (result is a set!).
 * It is allowed to return an empty set.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <Arg> argument type
 * @param <Value> type of values in returned set
 */
public interface Retriever<Arg, Value> extends HasCost {
	/**
	 * @param arg
	 * @return set of values retrieved from the given argument
	 */
	public Iterator<Value> retrieve(Arg arg);

}
