package er.neo4jadaptor.query.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

import er.neo4jadaptor.ersatz.lucene.LuceneTranslator;
import er.neo4jadaptor.ersatz.neo4j.Neo4JTranslator;
import er.neo4jadaptor.query.Results;
import er.neo4jadaptor.query.lucene.results.LuceneIndexHits;


/**
 * Consider EOQualifier:
 * <pre>
 * (((service.dvbOriginalNetworkId = 1536) and (service.dvbTransportStreamId = 2069) and (service.dvbServiceId = 19322)) and (endDateTime &gt;= (com.webobjects.foundation.NSTimestamp)'2012-06-07 04:00:00 Etc/GMT'))
 * </pre>
 * 
 * LuceneQueryConverter doesn't support relationships so it would convert it to:
 * <pre>
 * +(+(+#_type:WEPGEvent +#_type:WEPGEvent +#_type:WEPGEvent) +endDateTime:[2012060706:00:00:000 TO ZZZZ]) +#_type:WEPGEvent
 * </pre>
 * 
 * which would return more results than actually matching the original query. We could optimize lucene query by first searching for services
 * matching
 * <pre> 
 * dvbOriginalNetworkId = 1536 and dvbTransportStreamId = 2069 and dvbServiceId = 19322
 * </pre>
 * and then replace
 * ((service.dvbOriginalNetworkId = 1536) and (service.dvbTransportStreamId = 2069) and (service.dvbServiceId = 19322))
 * part with matching service IDs, so we could get in result something like:
 * +(+(+(+#_type:WEPGEvent +#_type:WEPGEvent +#_type:WEPGEvent) +endDateTime:[2012060706:00:00:000 TO ZZZZ]) +#_type:WEPGEvent) +(serviceId:00000000000000002026)
 * instead.
 * <p>
 * Due to the fact that optimization process makes another Lucene query which has a around-constant overhead, we perform optimization
 * attempt only if the initial number of results exceeds threshold of {@value #OPTIMIZATION_TRESHOLD}.
 * 
 * TODO: refactor
 * 
 * @author Jedrzej Sobanski
 *
 * @param <Type>
 */
public class LuceneOptimizer <Type extends PropertyContainer> {
	private static final Logger log = LoggerFactory.getLogger(LuceneOptimizer.class);
	
	/**
	 * Perform optimization attempt only if there are more then this many results.
	 */
	public static final int OPTIMIZATION_TRESHOLD = 1000;
	
	private final Index<Type> index;
	
	public LuceneOptimizer(Index<Type> index) {
		this.index = index;
	}
	
	public static boolean canBeOptimized(IndexHits<? extends PropertyContainer> hits, EOQualifier qualifier) {
		return hits.size() > OPTIMIZATION_TRESHOLD && false == containsAlternatives(qualifier);
	}
	
	private static <Type extends PropertyContainer> long getObjectId(Type t) {
		if (t instanceof Node) {
			return ((Node) t).getId();
		} else if (t instanceof Relationship) {
			return ((Relationship) t).getId();
		} else {
			throw new IllegalArgumentException("Doesn't know type of object " + t);
		}
	}
	
	public Results<Type> optimize(Query q, EOEntity entity, EOQualifier qualifier) {
		Map<EORelationship, List<Type>> relToNodes = relationshipsToNodes(entity, qualifier);
		BooleanQuery boolQuery = new BooleanQuery();
		IndexHits<Type> hits;
		
		// TODO: we make implicit assumption here that q is only conjunction
		boolQuery.add(q, Occur.MUST);
		
		for (EORelationship r : relToNodes.keySet()) {
			NSArray<EOAttribute> srcAtts = r.sourceAttributes();
			BooleanQuery relationshipQuery = new BooleanQuery();
			EOAttribute att;
			
			if (srcAtts.count() != 1) {
				throw new IllegalArgumentException();
			} else {
				att = srcAtts.get(0);
			}

			for (Type n : relToNodes.get(r)) {
				Object ultimateId = Neo4JTranslator.instance.toNeutralValue(getObjectId(n), att);
				String luceneValue = LuceneTranslator.instance.fromNeutralValue(ultimateId, att);
				Term term = new Term(att.name(), luceneValue);
				TermQuery termQuery = new TermQuery(term);
				
				if (relationshipQuery.clauses().size() >= BooleanQuery.getMaxClauseCount()) {
					BooleanQuery.setMaxClauseCount(BooleanQuery.getMaxClauseCount() + 10);
				}
				relationshipQuery.add(termQuery, Occur.SHOULD);
			}
			boolQuery.add(relationshipQuery, Occur.MUST);
		}
		hits = index.query(boolQuery);

		log.debug("Querying lucene with {}.", q);
		
		return new LuceneIndexHits<>(hits);
	} 
	
	private Map<EORelationship, List<Type>> relationshipsToNodes(EOEntity entity, EOQualifier qualifier) {
		Map<EORelationship, List<EOKeyValueQualifier>> relToQualifiers = new HashMap<EORelationship, List<EOKeyValueQualifier>>();
		Map<EORelationship, List<Type>> relToNodes = new HashMap<EORelationship, List<Type>>();
		LuceneQueryConverter luceneConverter = new LuceneQueryConverter();
		
		collectUsedRelationships(relToQualifiers, entity, qualifier);
		
		for (EORelationship r : relToQualifiers.keySet()) {
			NSArray<EOQualifier> qualifiers = new NSArray<>(relToQualifiers.get(r));
			Query luceneQuery;
			List<Type> nodes = new ArrayList<>();
			
			luceneQuery = luceneConverter.fullQuery(r.destinationEntity(), new EOAndQualifier(qualifiers));
			for (Type node : index.query(luceneQuery)) {
				nodes.add(node);
			}
			relToNodes.put(r, nodes);
		}
		return relToNodes;
	}

	private static boolean containsAlternatives(EOQualifier q) {
		if (q instanceof EOOrQualifier) {
			return true;
		}
		if (q instanceof EOAndQualifier) {
			for (EOQualifier q1 : ((EOAndQualifier) q).qualifiers()) {
				if (containsAlternatives(q1)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Qualifier MUST NOT contain any alternatives
	 * @param q
	 * @return
	 * 
	 * TODO: it doesn't look good - supports only AND and key-value qualifiers
	 */
	private void collectUsedRelationships(Map<EORelationship, List<EOKeyValueQualifier>> result, EOEntity e, EOQualifier q) {
		if (q instanceof EOKeyValueQualifier) {
			EOKeyValueQualifier kvq = (EOKeyValueQualifier) q;
			String key = kvq.key();
			String [] segments = key.split("\\.");
			
			if (segments.length == 2) {
				EORelationship r = e.relationshipNamed(segments[0]);
				
				if (r != null && false == r.isToMany() && false == r.isFlattened()) {
					List<EOKeyValueQualifier> list = result.get(r);
					
					if (list == null) {
						list = new ArrayList<>();
						result.put(r, list);
					}
					list.add(new EOKeyValueQualifier(segments[1], kvq.selector(), kvq.value()));
				}
			} else {
				// ignore, too complex
			}
		} else if (q instanceof EOAndQualifier) {
			for (EOQualifier q1 : ((EOAndQualifier) q).qualifiers()) {
				collectUsedRelationships(result, e, q1);
			}
		} else {
			throw new IllegalArgumentException("Qualifiers different than " + EOAndQualifier.class.getSimpleName() + " or " + EOKeyValueQualifier.class.getSimpleName() + " are not supported");
		}
	}
}
