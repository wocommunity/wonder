package er.neo4jadaptor.ersatz.neo4j;

import org.neo4j.graphdb.Relationship;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;

/**
 * Ersatz stored on Neo4J {@link Relationship}.
 * 
 * @author Jedrzej Sobanski
 */
public class Neo4JRelationshipErsatz extends Neo4JErsatz {

	public Neo4JRelationshipErsatz(EOEntity entity, Relationship pc) {
		super(entity, pc);
	}

	protected Relationship getRelationship() {
		return (Relationship) getPropertyContainer();
	}
	
	@Override
	protected Number getForeignKeyValue(EORelationship rel) {
		return (Number) getAttribute(getRelationship(), rel.sourceAttributes().get(0));
	}

	@Override
	protected void setForeignKeyValue(EORelationship rel, Number val) {
		// it can't modify foreign key value as once created relationship can't chane its destinations
		// we will only validate that no one attempts to modify existing source and destinations
		
		if (val == null) {
			throw new UnsupportedOperationException("Can't unlink join relationship from any of its nodes");
		}
		long valId = val.longValue();
		
		if (valId != getRelationship().getStartNode().getId() && valId != getRelationship().getEndNode().getId()) {
			throw new UnsupportedOperationException("Can't modify join relationship start/end node");
		}
		
		setAttribute(getRelationship(), rel.sourceAttributes().get(0), val);
	}
	
	/**
	 * Delete relationship storing data for the record that this ersatz represents.
	 */
	@Override
	public void delete() {
		getRelationship().delete();
	}
}
