package er.neo4jadaptor.query.neo4j_eval.evaluators;

import java.util.Iterator;
import java.util.regex.Pattern;

import org.neo4j.graphdb.PropertyContainer;

import er.neo4jadaptor.query.neo4j_eval.Cost;
import er.neo4jadaptor.query.neo4j_eval.retrievers.Retriever;

/**
 * Tells if a value retrieved from a candidate using {@link Retriever} matches some regular expression.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public class RegexMatch <T extends PropertyContainer> implements Evaluator<T> {
	private final Retriever<T, String> valueRetriever;
	private final Pattern pattern;
	
	public static <T extends PropertyContainer> RegexMatch<T> wildcardMatch(Retriever<T, String> valueRetriever, String expression) {
		Pattern p = Pattern.compile(wildcardExpressionToRegex(expression));
		
		return new RegexMatch<T>(valueRetriever, p);
	}
	
	private RegexMatch(Retriever<T, String> valueRetriever, Pattern pattern) {
		this.valueRetriever = valueRetriever;
		this.pattern = pattern;
	}

	private static String wildcardExpressionToRegex(String wildcardExpression) {
		// TODO: has to secure against  user's input containing special regex characters
		return wildcardExpression.replaceAll("\\*", "\\.*");
	}

	public boolean evaluate(T candidate) {
		Iterator<String> it = valueRetriever.retrieve(candidate);
		
		while (it.hasNext()) {
			String s = it.next();
			
			if (pattern.matcher(s).matches()) {
				return true;
			}
		}
		return false;
	}
	
	public Cost getCost() {
		return valueRetriever.getCost();
	}
}
