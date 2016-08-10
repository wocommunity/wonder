/**
 * 
 */
package er.neo4jadaptor.query.neo4j_eval.retrievers;

import java.util.Iterator;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.webobjects.eoaccess.EORelationship;

/**
 * For a node retrieves relationships which are representing some join entity records.
 * 
 * @author Jedrzej Sobanski
 *
 */
public class NodeToJoinRelationshipRetriever extends RelationshipRetriever<Node, Relationship> {

	private final RelationshipType relationshipType;
	
	public NodeToJoinRelationshipRetriever(EORelationship rel) {
		relationshipType = DynamicRelationshipType.withName(rel.destinationEntity().name());
	}
	
	public Iterator<Relationship> retrieve(Node node) {
		return node.getRelationships(relationshipType).iterator();
	}

	@Override
	public String toString() {
		return "node-to-relationship through " + relationshipType.name();
	}
}