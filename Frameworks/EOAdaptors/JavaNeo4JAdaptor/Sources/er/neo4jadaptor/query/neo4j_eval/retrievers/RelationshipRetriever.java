package er.neo4jadaptor.query.neo4j_eval.retrievers;


import org.neo4j.graphdb.PropertyContainer;

import com.webobjects.eoaccess.EORelationship;

import er.neo4jadaptor.query.neo4j_eval.Cost;
import er.neo4jadaptor.storage.neo4j.RelationshipStore;

/**
 * Retrieves EO relationship destination.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <Arg>
 * @param <T>
 */
public abstract class RelationshipRetriever <Arg, T extends PropertyContainer> implements Retriever<Arg, T> {
	public static RelationshipRetriever<? extends PropertyContainer, ? extends PropertyContainer> create(EORelationship rel) {
		boolean isSourceStoredAsNode = ! RelationshipStore.shouldBeStoredAsRelationship(rel.entity());
		boolean isDestinationStoredAsNode = ! RelationshipStore.shouldBeStoredAsRelationship(rel.destinationEntity());
		
		if (rel.isFlattened()) {
			throw new IllegalArgumentException("Flattened relationships are not supported by this method");
		}
		if (isSourceStoredAsNode) {
			if (isDestinationStoredAsNode) {
				return new NodeToNodeRetriever(rel);
			} else {
				return new NodeToJoinRelationshipRetriever(rel);
			}			
		} else {
			if (isDestinationStoredAsNode) {
				return new RelationshipToNode(rel);
			} else {
				throw new UnsupportedOperationException();
			}
		}
	}
	
	public Cost getCost() {
		return Cost.RELATIONSHIPS;
	}
}
