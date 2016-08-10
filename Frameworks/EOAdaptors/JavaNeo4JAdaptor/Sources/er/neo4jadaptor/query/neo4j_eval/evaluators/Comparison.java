package er.neo4jadaptor.query.neo4j_eval.evaluators;

import java.util.Iterator;

import org.neo4j.graphdb.PropertyContainer;

import er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator;
import er.neo4jadaptor.query.neo4j_eval.Cost;
import er.neo4jadaptor.query.neo4j_eval.retrievers.Retriever;

/**
 * Checks if a some of the candidate's property/relationship value is equal/greater/less than some value.
 * To retrieve this property or relationship value it utilizes {@link er.neo4jadaptor.query.neo4j_eval.retrievers.Retriever}.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public class Comparison <T extends PropertyContainer> implements Evaluator<T> {
	private final Retriever<T, ?> valueRetriever;
	private final ComparisonOperator operator;
	private final Object rValue;

	/**
	 * 
	 * @param valueRetriever retrieved to get the value from each candidate
	 * @param operator comparison operator
	 * @param value value to compare to
	 */
	public Comparison(Retriever<T, ?> valueRetriever, ComparisonOperator operator, Object value) {
		this.valueRetriever = valueRetriever;
		this.operator = operator;
		this.rValue = value;
	}
	
	@SuppressWarnings("unchecked")
	private static int compare(Comparable<?> left, Object right) {
		if (left == right) {
			return 0;
		} else if (left == null) {
			if (right != null) {
				return -1;
			} else {
				return 1;
			}
		} else if (right == null) {
			if (left != null) {
				return -1;
			} else {
				return 1;
			}
		}
		
		// both are not-null at this point
		
		
		if (left instanceof Number) {
			// for number comparison convert both either to long or double
			if (left instanceof Long || left instanceof Integer || left instanceof Byte || left instanceof Short) {
				left = ((Number) left).longValue();
				right = ((Number) right).longValue();
			}
			if (left instanceof Double || left instanceof Float) {
				left = ((Number) left).doubleValue();
				right = ((Number) right).doubleValue();
			}
		}
		
		return ((Comparable) left).compareTo(right);
	}
	
	@SuppressWarnings("cast")
	public boolean evaluate(T candidate) {
		Iterator<?> lValues = valueRetriever.retrieve(candidate);
		
		if (rValue == null && ! lValues.hasNext()) {
			// if there are no values on the right hand side then it counts as null
			
			return true;
		}
		while (lValues.hasNext()) {
			Object lValue = lValues.next();
			final boolean comparisonResult;
			
			switch (operator) {
			case EQUAL:
				comparisonResult = compare((Comparable<?>) lValue, rValue) == 0;
				break;
			case GREATER_THAN:
				comparisonResult = compare((Comparable<?>) lValue, rValue) > 0;
				break;
			case GREATER_OR_EQUAL:
				comparisonResult = compare((Comparable<?>) lValue, rValue) >= 0;
				break;
			case LESS_THAN:
				comparisonResult = compare((Comparable<?>) lValue, rValue) < 0;
				break;
			case LESS_OR_EQUAL:
				comparisonResult = compare((Comparable<?>) lValue, rValue) <= 0;
				break;
			default:
				throw new UnsupportedOperationException();
			}
			
			if (comparisonResult) {
				// terminate early
				return true;
			}
		}
		return false;
	}
	
	public Cost getCost() {
		return valueRetriever.getCost();
	}
	
	@Override
	public String toString() {
		return valueRetriever.toString() + operator + rValue; 
	}
}
