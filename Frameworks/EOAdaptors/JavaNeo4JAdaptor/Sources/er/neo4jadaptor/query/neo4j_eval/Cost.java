package er.neo4jadaptor.query.neo4j_eval;


/**
 * Possible evaluation cost values. Lowest cost needs to be first, while the highest must be last.
 * 
 * @author Jedrzej Sobamski
 */
public enum Cost implements Comparable<Cost> {
	/**
	 * Lowest possible
	 */
	NONE,
	
	/**
	 * Doesn't access any properties or relationships
	 */
	PRIMARY_KEY,
	
	/**
	 * Accessing properties, but not accessing relationships
	 */
	PROPERTIES,
	
	/**
	 * Traversing relationships
	 */
	RELATIONSHIPS;
	
	private static final Cost LOWEST = Cost.values()[0];
	private static final Cost HIGHEST = Cost.values()[Cost.values().length - 1];
	
	public static Cost highest() {
		return HIGHEST;
	}
	
	public static Cost lowest() {
		return LOWEST;
	}

	public static Cost getHighest(Iterable<? extends HasCost> costs) {
		Cost max = Cost.lowest();
		
		for (HasCost h : costs) {
			Cost c = h.getCost();
			
			if (c.ordinal() > max.ordinal()) {
				max = c;
			}
			// if it's maximal value then interrupt
			if (c.equals(Cost.highest())) {
				break;
			}
		}
		return max;
	}
	
}
