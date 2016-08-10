package er.neo4jadaptor.storage.neo4j;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.webobjects.eoaccess.EOEntity;

import er.extensions.foundation.ERXProperties;

/**
 * Makes imaginary division of nodes between temporary and permanent. Temporary nodes can be periodically removed if not used.
 * Temporary nodes are marked by special relationship to the root node.
 * 
 * @author Jedrzej Sobanski
 */
public class NodeSpaceManager {
	private final GraphDatabaseService db;

	public NodeSpaceManager(GraphDatabaseService db) {
		this.db = db;
	}
	
	/**
	 * Marks node as temporary. Node must be neither temporary nor permanent (state not set)
	 * @param node
	 */
	public void setIsTemporary(Node node) {
		Node root = db.getReferenceNode();
		
		root.createRelationshipTo(node, Space.TEMPORARY);
	}
	
	public void setIsPermanent(Node node, EOEntity entity) {
		Relationship tempRelationship = node.getSingleRelationship(Space.TEMPORARY, Direction.INCOMING);
		
		if (tempRelationship != null) {
			tempRelationship.delete();
		}
		if (shouldLinkToRoot(entity)) {
			Node root = db.getReferenceNode();
			
			root.createRelationshipTo(node, Space.PERMANENT);
		}
	}

	private boolean shouldLinkToRoot(EOEntity entity) {
		String property = "n4j.entity." + entity.name() + ".linkPermanentToRoot";
		boolean linkToRoot = ERXProperties.booleanForKeyWithDefault(property, false);

		return linkToRoot;
	}
	
	public boolean isTemporary(Node node) { 
		return node.hasRelationship(Space.TEMPORARY);
	}
	
	public boolean isPermanent(Node node) {
		return ! isTemporary(node);
	}
	
	public static enum Space implements RelationshipType {
		/** space for nodes created temporary, that should eventually end up in PERMANENT space. If they won't, then should be removed */
		TEMPORARY,
		PERMANENT
		;
	}
}
