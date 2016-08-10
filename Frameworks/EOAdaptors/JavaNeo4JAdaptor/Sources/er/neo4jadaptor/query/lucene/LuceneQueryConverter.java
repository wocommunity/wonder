package er.neo4jadaptor.query.lucene;

import java.util.Collection;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.regex.RegexQuery;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;

import er.neo4jadaptor.ersatz.lucene.LuceneErsatz;
import er.neo4jadaptor.ersatz.lucene.LuceneTranslator;
import er.neo4jadaptor.query.QueryConverter;
import er.neo4jadaptor.query.expression.sentence.operators.ComparisonOperator;
import er.neo4jadaptor.storage.lucene.LuceneStore;

/**
 * Builds Lucene {@link Query} equal to, or more liberal than the given {@link EOQualifier}. Search results
 * found using returned qualifier might therefore need additional filtering.
 * 
 * @author Jedrzej Sobanski
 *
 */
public class LuceneQueryConverter extends QueryConverter<Query> {
//	public static LuceneQueryConverter instance = new LuceneQueryConverter();
	private boolean isQueryFullyCovered = true;
	
	public LuceneQueryConverter() {
		
	}
	
	@Override
	protected Query negate(Query query) {
		BooleanQuery boolQuery = new BooleanQuery();
		
		// When using MUST_NOT clause it can not appear on its own, it must be accompanied by another query in BooleanQuery
		// For this reason this artificial match-all query is added
		// @see Boolean.Clause.Occur.MUST_NOT 
		boolQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
		
		boolQuery.add(query, BooleanClause.Occur.MUST_NOT);
		
		return boolQuery;
	}
	
	private Query joinWithOccur(Collection<Query> queries, Occur occur) {
		if (BooleanQuery.getMaxClauseCount() < queries.size()) {
			BooleanQuery.setMaxClauseCount(queries.size());
		}
		
		BooleanQuery boolQuery = new BooleanQuery();
		
		for (Query q : queries) {
			boolQuery.add(q, occur);
		}
		return boolQuery;
	}
	
	@Override
	protected Query joinWithAndOperator(Collection<Query> queries) {
		return joinWithOccur(queries, Occur.MUST);
	}
	
	@Override
	protected Query joinWithOrOperator(Collection<Query> queries) {
		return joinWithOccur(queries, Occur.SHOULD);
	}
	
	@Override
	protected Query matchAll() {
		return new MatchAllDocsQuery();
	}

	public static Query matchAllOfEntity(EOEntity entity) {
		Term term = new Term(LuceneStore.TYPE_PROPERTY_NAME, entity.name());
		
		return new TermQuery(term);
	}
	
	@Override
	protected Query convertKeyValueQualifier(EOEntity entity, EOKeyValueQualifier qual) {
		String key = qual.key();
		
		if (entity.attributeNamed(key) == null) {
			isQueryFullyCovered = false;
			
			return matchAllOfEntity(entity);
		} else {
			return super.convertKeyValueQualifier(entity, qual);
		}
	}
	
	private Query rangeQuery(String key, EOAttribute att, ComparisonOperator operator, Object value) {
		String min = null;
		String max = null;
		boolean minInclusive = false;
		boolean maxInclusive = false;
		
		switch (operator) {
		case LESS_THAN:
		case LESS_OR_EQUAL:
			min = null;
			max = LuceneTranslator.instance.fromNeutralValue(value, att);
			break;
		case GREATER_THAN:
		case GREATER_OR_EQUAL:
			min = LuceneTranslator.instance.fromNeutralValue(value, att);
			max = null;
			break;
		}
		
		if (ComparisonOperator.LESS_OR_EQUAL.equals(operator)) {
			maxInclusive = true;
		}
		if (ComparisonOperator.GREATER_OR_EQUAL.equals(operator)) {
			minInclusive = true;
		}
		
		return new TermRangeQuery(key, min, max, minInclusive, maxInclusive);
	}
	
	@Override
	protected Query comparison(EOEntity entity, String key, ComparisonOperator operator, Object value) {
		EOAttribute att = entity.attributeNamed(key);
		String luceneValue = LuceneTranslator.instance.fromNeutralValue(value, att);
		
		switch (operator) {
		case EQUAL:
			return new TermQuery(new Term(key, luceneValue));
		case LESS_THAN:
		case LESS_OR_EQUAL:
		case GREATER_THAN:
		case GREATER_OR_EQUAL:
			return rangeQuery(key, att, operator, value);
		case LIKE:
			return new WildcardQuery(new Term(key, luceneValue));
		case ILIKE:
			String lowercaseKey = LuceneErsatz.lowercasePropertyName(key);
			
			if (value == null) {
				return new WildcardQuery(new Term(lowercaseKey, luceneValue));
			} else {
				return new WildcardQuery(new Term(lowercaseKey, luceneValue.toLowerCase()));
			}
		case MATCHES:
			return new RegexQuery(new Term(key, luceneValue));
		}
		throw new UnsupportedOperationException("Operator " + operator + " not covered in switch-case");
	}
	
	@Override
	public Query fullQuery(EOEntity entity, EOQualifier qualifier) {
		Query q = convert(entity, qualifier);
		BooleanQuery boolQuery = new BooleanQuery();
		
		boolQuery.add(q, Occur.MUST);
		boolQuery.add(matchAllOfEntity(entity), Occur.MUST);
		
		return boolQuery;
	}
	
	public boolean isQueryFullyCovered() {
		return isQueryFullyCovered;
	}
}
