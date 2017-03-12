package er.neo4jadaptor.query.lucene;

import org.apache.lucene.search.Query;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOQualifier;

import er.neo4jadaptor.query.Filter;
import er.neo4jadaptor.query.Results;
import er.neo4jadaptor.query.lucene.results.LuceneIndexHits;
import er.neo4jadaptor.query.neo4j_eval.EvaluatingFilter;
import er.neo4jadaptor.storage.IndexProvider;

/**
 * Gets result records from the Lucene index, but in case if it's not able to perform Lucene query
 * matching exactly EO qualifier criterias (if Lucene search is more liberal than {@link EOQualifier}
 * then it performs additionally {@link er.neo4jadaptor.query.neo4j_eval.EvaluatingFilter} on the results.
 * This filter will return records exactly matching search criteria.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <Type>
 */
public class LuceneFilter <Type extends PropertyContainer> extends Filter<Type> {
	private static final Logger log = LoggerFactory.getLogger(LuceneFilter.class);

	public LuceneFilter() {
	}
	
	@Override
	public Results<Type> doFilter(GraphDatabaseService db, EOEntity entity, EOQualifier qualifier) {
		LuceneQueryConverter luceneConverter = new LuceneQueryConverter();
		Query q = luceneConverter.fullQuery(entity, qualifier);
		final Results<Type> objects;
		Index<Type> index = (Index<Type>) IndexProvider.instance.getIndexForEntity(db, entity);
		IndexHits<Type> hits = index.query(q);
		
		log.debug("Querying lucene with {}.", q);
		
		// optimization
		if (LuceneOptimizer.canBeOptimized(hits, qualifier)) {
			LuceneOptimizer<Type> optimizer = new LuceneOptimizer<>(index);
			
			objects = optimizer.optimize(q, entity, qualifier); 			
		} else {
			objects = new LuceneIndexHits<>(hits);
		}
		
		if (luceneConverter.isQueryFullyCovered()) {
			return objects;
		} else {
			return new EvaluatingFilter<>(objects, entity, qualifier);
		}
	}
}
