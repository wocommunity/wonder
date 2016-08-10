package er.neo4jadaptor.query.expression;

import static er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator.EQUAL;
import static er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator.GREATER_OR_EQUAL;
import static er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator.GREATER_THAN;
import static er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator.ILIKE;
import static er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator.LESS_OR_EQUAL;
import static er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator.LESS_THAN;
import static er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator.LIKE;
import static er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator.MATCHES;
import static er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator.NOT_EQUAL;

import java.util.ArrayList;
import java.util.Collection;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSSelector;

import er.neo4jadaptor.ersatz.webobjects.NSTranslator;
import er.neo4jadaptor.query.expression.sentence.BinaryJoined;
import er.neo4jadaptor.query.expression.sentence.Comparison;
import er.neo4jadaptor.query.expression.sentence.Negation;
import er.neo4jadaptor.query.expression.sentence.Sentence;
import er.neo4jadaptor.query.expression.sentence.operators.BinaryOperator;
import er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator;

public class Converter {
	private final EOEntity entity;
	
	public Converter(EOEntity entity) {
		this.entity = entity;
	}
	
	public Sentence convert(EOQualifier q) {
		if (q instanceof EOAndQualifier) {
			Collection<Sentence> components = convertCollection(((EOAndQualifier) q).qualifiers());
			
			return new BinaryJoined(BinaryOperator.AND, components);
		} else if (q instanceof EOOrQualifier) {
			Collection<Sentence> components = convertCollection(((EOOrQualifier) q).qualifiers());
			
			return new BinaryJoined(BinaryOperator.OR, components);
		} else if (q instanceof EONotQualifier) {
			Sentence component = convert(((EONotQualifier) q).qualifier());
			
			return new Negation(component);
		} else if (q instanceof EOKeyValueQualifier) {
			return convertComparison((EOKeyValueQualifier) q);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	private Collection<Sentence> convertCollection(Collection<EOQualifier> qualifiers) {
		Collection<Sentence> components = new ArrayList<Sentence>();
		
		for (EOQualifier q : qualifiers) {
			components.add(convert(q));
		}
		return components;
	}
	
	private ComparisonOperator operator(NSSelector<?> operator) {
		if (operator.equals(EOKeyValueQualifier.QualifierOperatorEqual)) {
			return EQUAL;
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorNotEqual)) {
			return NOT_EQUAL;
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorLessThanOrEqualTo)) {
			return LESS_OR_EQUAL;
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorLessThan)) {
			return LESS_THAN;
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorGreaterThanOrEqualTo)) {
			return GREATER_OR_EQUAL;
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorGreaterThan)) {
			return GREATER_THAN;
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorLike)) {
			return LIKE;
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorCaseInsensitiveLike)) {
			return ILIKE;
		} else if (ComparisonOperator.MATCHES.asString.equals(operator.name())) {
			return MATCHES;
		} else {
			// case not covered
			throw new UnsupportedOperationException(operator.toString());
		}
	}
	
	private Comparison convertComparison(EOKeyValueQualifier qual) {
		ComparisonOperator operator = operator(qual.selector());
		String key = qual.key();
		Object value = NSTranslator.instance.toNeutralValue(qual.value(), entity.attributeNamed(key));
		EOEntity currentEntity = entity;
		String [] splits = key.split("\\.");
		Collection<EORelationship> rels = new ArrayList<EORelationship>();
		
		for (int i=0; i<splits.length - 1; i++) {
			EORelationship rel = currentEntity.relationshipNamed(splits[i]);
			
			rels.add(rel);
			currentEntity = rel.destinationEntity();
		}
		
		// now process the last bit
		String last = splits[splits.length-1];
		
		if (entity.attributeNamed(last) != null) {
			// it's a plain attribute value comparison 
			return new Comparison(rels, entity.attributeNamed(last), operator, value);
		} else {
			// it's a relationship value comparison, let's represent it as attribute comparison
			EORelationship lastRel = entity.relationshipNamed(last);
			EOAttribute srcAtt = asOne(lastRel.sourceAttributes());
			EOAttribute dstAtt = asOne(lastRel.destinationAttributes());
			
			rels.add(lastRel);
			if (value != null) {
				value = ((NSKeyValueCoding) value).valueForKey(dstAtt.name());
			}
			return new Comparison(rels, srcAtt, operator, value);
		}
	}
	
	private <T> T asOne(Collection<T> col) {
		if (col.size() != 1) {
			throw new IllegalArgumentException();
		} else {
			return col.iterator().next();
		}
	}
}
