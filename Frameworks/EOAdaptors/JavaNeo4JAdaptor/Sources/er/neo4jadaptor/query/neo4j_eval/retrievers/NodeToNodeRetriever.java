/**
 * 
 */
package er.neo4jadaptor.query.neo4j_eval.retrievers;

import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.webobjects.eoaccess.EORelationship;

import er.neo4jadaptor.ersatz.neo4j.Neo4JUtils;

/**
 * For a node retrieves nodes being relationship value. It can handle both to-one and to-many types of {@link EORelationship}s.
 * 
 * @author Jedrzej Sobanski
 */
public class NodeToNodeRetriever extends RelationshipRetriever<Node, Node> {
	private final Direction direction;

	private final RelationshipType relationshipType;
	
	public NodeToNodeRetriever(EORelationship rel) {
		if (rel.isToMany()) {
			relationshipType = Neo4JUtils.getRelationshipType(rel.inverseRelationship());
			direction = Direction.INCOMING;
		} else {
			relationshipType = Neo4JUtils.getRelationshipType(rel);
			direction = Direction.OUTGOING;
		}
	}
	
	public Iterator<Node> retrieve(final Node node) {
		final Iterator<Relationship> it = node.getRelationships(direction, relationshipType).iterator();
		
		return new Iterator<Node>() {
			public boolean hasNext() {
				return it.hasNext();
			}

			public Node next() {
				Relationship r = it.next();
				
				if (direction.equals(Direction.INCOMING)) {
					return r.getStartNode();
				} else {
					return r.getEndNode();
				}
			}

			public void remove() {
				it.remove();
			}
		};
	}

	@Override
	public String toString() {
		return "node-to-node through " + relationshipType.name();
	}
}