package er.neo4jadaptor.query.neo4j_eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.neo4j.graphdb.PropertyContainer;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

import er.neo4jadaptor.query.QueryConverter;
import er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator;
import er.neo4jadaptor.query.neo4j_eval.evaluators.AlwaysTrue;
import er.neo4jadaptor.query.neo4j_eval.evaluators.BoolEvaluator;
import er.neo4jadaptor.query.neo4j_eval.evaluators.Comparison;
import er.neo4jadaptor.query.neo4j_eval.evaluators.Evaluator;
import er.neo4jadaptor.query.neo4j_eval.evaluators.Negate;
import er.neo4jadaptor.query.neo4j_eval.evaluators.RegexMatch;
import er.neo4jadaptor.query.neo4j_eval.retrievers.AttributeRetriever;
import er.neo4jadaptor.query.neo4j_eval.retrievers.ChainedRetriever;
import er.neo4jadaptor.query.neo4j_eval.retrievers.LowercaseRetriever;
import er.neo4jadaptor.query.neo4j_eval.retrievers.PrimaryKeyRetriever;
import er.neo4jadaptor.query.neo4j_eval.retrievers.RelationshipRetriever;
import er.neo4jadaptor.query.neo4j_eval.retrievers.Retriever;
import er.neo4jadaptor.utils.EOUtilities;

/**
 * Converts {@link EOQualifier} to {@link Evaluator} having exactly the same semantics.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public class EvaluationQueryConverter <T extends PropertyContainer> extends QueryConverter<Evaluator<T>> {
	public EvaluationQueryConverter() {
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Evaluator<T> comparison(EOEntity entity, String key, ComparisonOperator operator, Object value) {
		Retriever<T, ?> retriever = buildRetriever(entity, key);
		
		if (operator.equals(ComparisonOperator.LIKE) || operator.equals(ComparisonOperator.ILIKE)) {
			Retriever<T, String> textRetriever = (Retriever<T, String>) retriever;
			
			
			if (operator.equals(ComparisonOperator.ILIKE)) {
				value = ((String) value).toLowerCase();
				textRetriever = new LowercaseRetriever<>(textRetriever);
			}
			
			return RegexMatch.wildcardMatch(textRetriever, (String) value);
		} else {
			return new Comparison<T>(retriever, operator, value);
		}
	}
	
	private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

	@SuppressWarnings("unchecked")
	private <V> Retriever<T, V> buildRetriever(EOEntity entity, String keypath) {
		keypath = EOUtilities.unflattenedKey(entity, keypath);
		
		EOEntity current = entity;
		String [] splits = DOT_PATTERN.split(keypath);
		List<Retriever<? extends PropertyContainer, ? extends PropertyContainer>> interimRetrievers = new ArrayList();
		Retriever<? extends PropertyContainer, V> lastRetriever;
		
		// iterate over splits except for the last one
		for (int i=0; i<splits.length-1; i++) {	
			String key = splits[i];
			EORelationship rel = current.relationshipNamed(key);
			RelationshipRetriever<? extends PropertyContainer, ? extends PropertyContainer> r = RelationshipRetriever.create(rel);
			
			interimRetrievers.add(r);
			
			current = rel.destinationEntity();
		}
		
		// now handle last one (which could be attribute or relationship)
		{
			String key = splits[splits.length - 1];
			EORelationship rel = current.relationshipNamed(key);
			
			if (rel != null) {
				lastRetriever = (Retriever<? extends PropertyContainer, V>) RelationshipRetriever.create(rel);				
			} else {
				EOAttribute att = current.attributeNamed(key);
				NSArray<EOAttribute> pks = current.primaryKeyAttributes();
				
				if (pks.size() == 1 && pks.get(0).equals(att)) {
					lastRetriever = (Retriever<? extends PropertyContainer, V>) PrimaryKeyRetriever.create(current);
				} else {
					// it must be an attribute
					lastRetriever = (Retriever<? extends PropertyContainer, V>) AttributeRetriever.create(current, att);
				}
			}
		}
		
		if (interimRetrievers.isEmpty()) {
			return (Retriever<T, V>) lastRetriever;
		} else {
			return new ChainedRetriever<T, V>(interimRetrievers, lastRetriever);
		}
	}
	
	@Override
	public Evaluator<T> fullQuery(EOEntity entity, EOQualifier qualifier) {
		return convert(entity, qualifier);
	}

	@Override
	protected Evaluator<T> joinWithAndOperator(Collection<Evaluator<T>> queries) {
		return BoolEvaluator.and(queries);
	}

	@Override
	protected Evaluator<T> joinWithOrOperator(Collection<Evaluator<T>> queries) {
		return BoolEvaluator.or(queries);
	}

	@Override
	protected Evaluator<T> matchAll() {
		return AlwaysTrue.instance();
	}

	@Override
	protected Evaluator<T> negate(Evaluator<T> query) {
		return new Negate<T>(query);
	}
}
