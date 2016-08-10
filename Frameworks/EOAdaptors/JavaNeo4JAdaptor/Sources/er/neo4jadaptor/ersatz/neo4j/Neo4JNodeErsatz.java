package er.neo4jadaptor.ersatz.neo4j;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;

import er.neo4jadaptor.utils.EOUtilities;

/**
 * Ersatz stored on Neo4J node.
 * 
 * @author Jedrzej Sobanski
 */
public class Neo4JNodeErsatz extends Neo4JErsatz {
	public Neo4JNodeErsatz(EOEntity entity, Node pc) {
		super(entity, pc);
	}

	protected Node getNode() {
		return (Node) getPropertyContainer();
	}
	
	private boolean isPrimaryKey(EOAttribute att) {
		EOAttribute pk = EOUtilities.primaryKeyAttribute(entity);
		
		return att.name().equals(pk.name());
	}
	
	private long getPrimaryKey() {
		return getNode().getId();
	}
	
	@Override
	public Object get(EOAttribute att) {
		if (isPrimaryKey(att)) {
			long pk = getPrimaryKey();
			
			return Neo4JTranslator.instance.toNeutralValue(pk, att);
		} else {
			return super.get(att);
		}
	}
	
	@Override
	public void put(EOAttribute att, Object value) {
		if (isPrimaryKey(att)) {
			long val = ((Number) value).longValue();
			
			if (val != getPrimaryKey()) {
				throw new UnsupportedOperationException("Unable to change node's primary key");
			} else {
				// do nothing
			}
		} else {
			super.put(att, value);
		}
	}
	
	@Override
	protected Number getForeignKeyValue(EORelationship rel) {
		RelationshipType relType = Neo4JUtils.getRelationshipType(rel);
		Relationship r = getNode().getSingleRelationship(relType, Direction.OUTGOING);
		
		if (r == null) {
			return null;
		} else {
			long id = r.getEndNode().getId();
			EOAttribute dstAtt = EOUtilities.primaryKeyAttribute(rel.destinationEntity());
			
			return EOUtilities.convertToAttributeType(dstAtt, id);
		}
	}

	@Override
	protected void setForeignKeyValue(EORelationship rel, Number referencedId) {
		RelationshipType relType = Neo4JUtils.getRelationshipType(rel);
		Relationship r = getNode().getSingleRelationship(relType, Direction.OUTGOING);
		
		if (referencedId == null) {
			if (r != null) {
				r.delete();
			}
		} else {
			Node dstNode = getNode().getGraphDatabase().getNodeById(referencedId.longValue());
			
			if (r != null && r.getEndNode().equals(dstNode)) {
				return;
			}
			if (r != null) {
				// it exists, but it's invalid
				r.delete();
			}
			getNode().createRelationshipTo(dstNode, relType);
		}
	}

	/**
	 * Deletes corresponding node and all of its to-one relationships. 
	 */
	@Override
	public void delete() {
		// remove all relationships that this node owns (in relational database these would be simply foreign key values
		// therefore removing a row would remove it's owned foreign keys. We could simply traverse through all of its
		// surrounding outgoing relationships and delete them, but it would delete join entity relationships too.
		for (EORelationship rel : entity.relationships()) {
			if (! rel.isCompound() && ! rel.isToMany()) {
				RelationshipType relType = Neo4JUtils.getRelationshipType(rel);
				Relationship r = getNode().getSingleRelationship(relType, Direction.OUTGOING);
				
				if (r != null) {
					r.delete();
				}
			}
		}
		
		// remove the node itself
		getNode().delete();
	}
}
