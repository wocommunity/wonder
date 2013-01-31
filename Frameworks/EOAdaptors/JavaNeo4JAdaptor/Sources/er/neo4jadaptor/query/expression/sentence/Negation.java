package er.neo4jadaptor.query.expression.sentence;

public class Negation extends Sentence {
	private final Sentence wrapped;
	
	public Negation(Sentence toNegate) {
		wrapped = toNegate;
	}
}
