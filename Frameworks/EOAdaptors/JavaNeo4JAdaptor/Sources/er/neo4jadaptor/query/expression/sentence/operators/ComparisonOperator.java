package er.neo4jadaptor.query.expression.sentence.operators;


public enum ComparisonOperator {
	LESS_THAN("<"),
	LESS_OR_EQUAL("<="),
	GREATER_THAN(">"),
	GREATER_OR_EQUAL(">="), 
	EQUAL("="),
	NOT_EQUAL("!="),
	
	/**
	 * Case sensitive match using wildcards
	 */
	LIKE("like"),
	/**
	 * Case insensitive match using wildcards
	 */
	ILIKE("ilike"),
	/**
	 * Regular expression match
	 */
	MATCHES("matches");
	
	public final String asString;
	
	private ComparisonOperator(String asString) {
		this.asString = asString;
	}
	
	@Override
	public String toString() {
		return asString;
	}
	
}
