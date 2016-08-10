package er.neo4jadaptor.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSSelector;

import er.extensions.eof.qualifiers.ERXInQualifier;
import er.neo4jadaptor.ersatz.webobjects.NSTranslator;
import er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator;

/**
 * Converts {@link EOQualifier}s to custom type of queries.
 * 
 * @author Jedrzej Sobanski
 * 
 * @param <ClauseType> query clause type
 */
public abstract class QueryConverter <ClauseType> {

	
	/**
	 * @param entity
	 * @param qualifier
	 * @return search criteria equivalent to the given qualifier
	 */
	public abstract ClauseType fullQuery(EOEntity entity, EOQualifier qualifier);
	
	protected ClauseType convert(EOEntity entity, EOQualifier qualifier) {
		if (qualifier == null) {
			return matchAll();
		} else if (qualifier instanceof EOKeyValueQualifier) {
			return convertKeyValueQualifier(entity, (EOKeyValueQualifier) qualifier);
		} else if (qualifier instanceof EONotQualifier) {
			ClauseType query = convert(entity, ((EONotQualifier) qualifier).qualifier());
			
			return negate(query);
		} else if (qualifier instanceof EOAndQualifier || qualifier instanceof EOOrQualifier) {
			List<ClauseType> list = new ArrayList<ClauseType>();
			Collection<EOQualifier> qualifiers = qualifier instanceof EOAndQualifier 
				? ((EOAndQualifier) qualifier).qualifiers()
				: ((EOOrQualifier) qualifier).qualifiers();
		
			for (EOQualifier q : qualifiers) {
				ClauseType  clause = convert(entity, q);
				
				if (clause != null) {
					list.add(clause);
				}
			}
			if (list.isEmpty()) {
				return null;
			} else if (qualifier instanceof EOAndQualifier) {
				return joinWithAndOperator(list);
			} else {
				return joinWithOrOperator(list);
			}
		} else {
			throw new UnsupportedOperationException(qualifier.toString());
		}
	}

	protected ClauseType convertKeyValueQualifier(EOEntity entity, EOKeyValueQualifier qual) {
		NSSelector<?> operator = qual.selector();
		String key = qual.key();
		Object value = NSTranslator.instance.toNeutralValue(qual.value(), entity.attributeNamed(key));
		
		if (operator.equals(EOKeyValueQualifier.QualifierOperatorEqual)) {
			if (qual instanceof ERXInQualifier) {
				ERXInQualifier inQualifier = (ERXInQualifier) qual;
				List<ClauseType> clauses = new ArrayList<ClauseType>();
				
				for (Object o : inQualifier.values()) {
					clauses.add(comparison(entity, key, ComparisonOperator.EQUAL, o));
				}
				return joinWithOrOperator(clauses);
			} else {
				return comparison(entity, key, ComparisonOperator.EQUAL, value);
			}
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorNotEqual)) {
			ClauseType equal = comparison(entity, key, ComparisonOperator.EQUAL, value);
			
			if (equal == null) {
				return null;
			} else {
				return negate(equal);
			}
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorLessThanOrEqualTo)) {
			return comparison(entity, key, ComparisonOperator.LESS_OR_EQUAL, value);
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorLessThan)) {
			return comparison(entity, key, ComparisonOperator.LESS_THAN, value);
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorGreaterThanOrEqualTo)) {
			return comparison(entity, key, ComparisonOperator.GREATER_OR_EQUAL, value);
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorGreaterThan)) {
			return comparison(entity, key, ComparisonOperator.GREATER_THAN, value);
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorLike)) {
			return comparison(entity, key, ComparisonOperator.LIKE, value);
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorCaseInsensitiveLike)) {
			return comparison(entity, key, ComparisonOperator.ILIKE, value);
		} else if (ComparisonOperator.MATCHES.asString.equals(operator.name())) {
			return comparison(entity, key, ComparisonOperator.MATCHES, value);
		} else if (operator.equals(EOKeyValueQualifier.QualifierOperatorContains)) {
			// unsupported
			
			throw new UnsupportedOperationException(qual.toString());
		} else {
			// case not covered
			throw new UnsupportedOperationException(qual.toString());
		}
	}

	/**
	 * Creates clause that matches records where the given entity key matches given value using comparison operator.
	 * @param entity
	 * @param key
	 * @param operator
	 * @param value
	 * @return a ClauseType object
	 */
	protected abstract ClauseType comparison(EOEntity entity, String key, ComparisonOperator operator, Object value);
	
	/**
	 * Creates negation of the given clause.
	 * @param query clause to negate
	 * @return negated clause
	 */
	protected abstract ClauseType negate(ClauseType query);
	
	/**
	 * @param queries clauses to join with logical conjunction
	 * @return clause that would evaluate to <code>true</code> if all of queries evaluate to <code>true</code>
	 */
	protected abstract ClauseType joinWithAndOperator(Collection<ClauseType> queries);
	
	/**
	 * @param queries clauses to join with logical alternative
	 * @return clause that would evaluate to <code>true</code> if at least one of queries evaluates to <code>true</code>
	 */
	protected abstract ClauseType joinWithOrOperator(Collection<ClauseType> queries);
	
	/**
	 * @return clause that would match everyting
	 */
	protected abstract ClauseType matchAll();
	
}
