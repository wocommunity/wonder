package er.neo4jadaptor.query.expression.sentence;

import java.util.Collection;

import er.neo4jadaptor.query.expression.sentence.operators.BinaryOperator;

public class BinaryJoined extends Sentence {
	private final Collection<Sentence> components;
	private final BinaryOperator operator;
	
	public BinaryJoined(BinaryOperator op, Collection<Sentence> components) {
		operator = op;
		this.components = components;
	}
}
