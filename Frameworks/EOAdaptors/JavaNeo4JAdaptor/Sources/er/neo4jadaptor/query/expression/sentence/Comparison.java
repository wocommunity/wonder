package er.neo4jadaptor.query.expression.sentence;

import java.util.Collection;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EORelationship;

import er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator;



public class Comparison extends Sentence {
	private final Collection<EORelationship> relationships;
	private final EOAttribute attribute;
	private final ComparisonOperator operator;
	private final Object value;
	
	public Comparison(Collection<EORelationship> rels, EOAttribute att, ComparisonOperator op, Object val) {
		relationships = rels;
		attribute = att;
		operator = op;
		value = val;
	}
}
