package er.neo4jadaptor.query.neo4j_by_pk;

import java.util.Collection;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOQualifier;

import er.neo4jadaptor.query.Filter;
import er.neo4jadaptor.query.Results;
import er.neo4jadaptor.query.neo4j_by_pk.results.NodesRelatedTo;
import er.neo4jadaptor.query.neo4j_by_pk.results.NodesWithIds;
import er.neo4jadaptor.query.neo4j_eval.EvaluatingFilter;
import er.neo4jadaptor.storage.neo4j.RelationshipStore;
import er.neo4jadaptor.utils.EOUtilities;

/**
 * Filters nodes being closely related to some set of nodes with known IDs. If it's known that all possible result candidates
 * have relationship to one known node then it is faster to get all of that nodes and evaluate them using even reasonably
 * slow technique rather than performing Lucene search which might be fast but taking around constant time therefore
 * making it not the fastest for small result sets.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public class ByPrimaryKeyFilter<T extends PropertyContainer> extends Filter<T> {
	public ByPrimaryKeyFilter() {
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Results<T> doFilter(GraphDatabaseService db, EOEntity entity, EOQualifier qualifier) {
		// check if there are any node ids specified, if yes then go to those nodes directly
		ValueMap map = new ValueMap(entity, qualifier);
		
		if (map.getAttributes().size() == 1) {
			EOAttribute att = map.getMostFrequentAttribute();
			EORelationship rel = EOUtilities.getRelationshipForSourceAttribute(entity, att);
			Collection<?> values = map.getValuesForAttribute(att);
			boolean containsNulls = values.contains(null);
			
			if (! containsNulls) {
				if (entity.primaryKeyAttributes().size() == 1 && att.equals(EOUtilities.primaryKeyAttribute(entity))) {
					// it's primary key
					Results<T> filter = (Results<T>) primaryKeyReference(db, (Collection<? extends Number>) values);
					
					return new EvaluatingFilter<>(filter, entity, qualifier);
				}
				if (rel != null 
						&& ! RelationshipStore.shouldBeStoredAsRelationship(rel.entity()) 
						&& ! RelationshipStore.shouldBeStoredAsRelationship(rel.destinationEntity()) ) {
					// it's using foreign key
					Results<T> filter = (Results<T>) foreignKeyReference(db, rel, (Collection<? extends Number>) values);
					
					return new EvaluatingFilter<>(filter, entity, qualifier);
				}
			}
		}
		// responsibility chain delegation
		return successor.doFilter(db, entity, qualifier);
	}

	private Results<Node> primaryKeyReference(GraphDatabaseService db, Collection<? extends Number> values) {
		return new NodesWithIds(db, values);
	}

	private Results<Node> foreignKeyReference(GraphDatabaseService db, EORelationship rel, Collection<? extends Number> nodeIds) {
		Results<Node> referencedNodesProvider = new NodesWithIds(db, nodeIds);
		
		return new NodesRelatedTo(referencedNodesProvider, rel);
	}
}
