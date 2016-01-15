package er.neo4jadaptor.query.neo4j_eval;

import org.neo4j.graphdb.PropertyContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOQualifier;

import er.neo4jadaptor.query.Results;
import er.neo4jadaptor.query.neo4j_eval.evaluators.Evaluator;
import er.neo4jadaptor.utils.cursor.Cursor;

/**
 * Filter that using Neo4J java api examines node/relationship properties and their relationships
 * to filter those that exactly match search criteria.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public class EvaluatingFilter<T extends PropertyContainer> implements Results<T> {
	private static final Logger log = LoggerFactory.getLogger(EvaluatingFilter.class);

	@SuppressWarnings("unchecked")
	private static final EvaluationQueryConverter<?> generator = new EvaluationQueryConverter();

	private T next;
	private Cursor<T> it;
	private boolean hasFinished = false;
	
	private final Results<T> wrapped;
	private final Evaluator<T> eval;
	
	private int countHits = 0;
	private int countMisses = 0;
	
	@SuppressWarnings("unchecked")
	public EvaluatingFilter(Results<T> objects, EOEntity entity, EOQualifier q) {
		this(objects, (Evaluator<T>) generator.fullQuery(entity, q));	
	}
	
	private EvaluatingFilter(Results<T> wrapped, Evaluator<T> eval) {
		this.wrapped = wrapped;
		this.eval = eval;
		this.it = wrapped;
	}
	
	public boolean hasNext() {
		if (hasFinished) {
			return false;
		} else if (next == null) {
			next = calculateNext();
			
			if (next == null) {
				hasFinished = true;
			}
			
		}
		return next != null;
	}

	public void close() {
		it.close();
	}
	
	public T next() {
		T result = next;
		
		next = null;
		
		return result;
	}
	
	private T calculateNext() {
		while (it.hasNext()) {
			T candidate = it.next();
			
			if (eval.evaluate(candidate)) {
				countHits++;
				log.debug("Evaluating {} with {}, result: match", candidate, eval);
				return candidate;
			} else {
				countMisses++;
				log.debug("Evaluating {} with {}, result: miss", candidate, eval);
			}
		}
		if (log.isDebugEnabled() && countMisses > 0) {
			log.debug("Had {} hits vs. {} misses for {}", countHits, countMisses, wrapped);
		}
		return null;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
