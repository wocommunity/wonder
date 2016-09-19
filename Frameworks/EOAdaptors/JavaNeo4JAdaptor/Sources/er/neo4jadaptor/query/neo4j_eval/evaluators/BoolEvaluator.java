package er.neo4jadaptor.query.neo4j_eval.evaluators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.PropertyContainer;

import er.neo4jadaptor.query.neo4j_eval.Cost;
import er.neo4jadaptor.query.neo4j_eval.HasCost;

/**
 * Evaluates candidates to match some or all of some other {@link Evaluator}s building something
 * equivalent to logical sentences.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public final class BoolEvaluator <T extends PropertyContainer> implements Evaluator<T> {
	private final List<Evaluator<T>> evals = new ArrayList<Evaluator<T>>();
	
	private final boolean interruptOn;
	
	public static <T extends PropertyContainer> Evaluator<T> and(Iterable<Evaluator<T>> evaluators) {
		return new BoolEvaluator<>(evaluators, false);
	}

	public static <T extends PropertyContainer> Evaluator<T> or(Iterable<Evaluator<T>> evaluators) {
		return new BoolEvaluator<>(evaluators, true);
	}
	
	public Comparator<HasCost> COST_COMPARATOR = new Comparator<HasCost>() {
		public int compare(HasCost h1, HasCost h2) {
			return h1.getCost().compareTo(h2.getCost());
		}
	};
	
	@SuppressWarnings("unchecked")
	private boolean canBeFlattened(Evaluator<T> e) {
		return e instanceof BoolEvaluator && ((BoolEvaluator<T>) e).interruptOn == interruptOn;
	}
	
	private BoolEvaluator(Iterable<Evaluator<T>> evaluators, boolean interruptOn) {
		for (Evaluator<T> e : evaluators) {
			if (canBeFlattened(e)) {
				// if one of components is the same evaluator then add just its subcomponents (flatten)
				BoolEvaluator<T> be = (BoolEvaluator<T>) e;
				
				this.evals.addAll(be.evals);
			} else {
				this.evals.add(e);
			}
		}
		
		// start with the lowest cost operations
		Collections.sort(this.evals, COST_COMPARATOR);
		
		this.interruptOn = interruptOn;
	}
	
	public boolean evaluate(T candidate) {
		for (Evaluator<T> e : evals) {
			if (e.evaluate(candidate) == interruptOn) {
				return interruptOn;
			}
		}
		return ! interruptOn;
	}
	
	public Cost getCost() {
		return Cost.getHighest(evals);
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		Iterator<Evaluator<T>> it = evals.iterator();

		if (it.hasNext()) {
			b.append(it.next());
		}
		while (it.hasNext()) {
			if (interruptOn == false) {
				b.append(" and ");
			} else {
				b.append(" or ");
			}
			b.append(it.next());
		}
		return b.toString();
	}
}
