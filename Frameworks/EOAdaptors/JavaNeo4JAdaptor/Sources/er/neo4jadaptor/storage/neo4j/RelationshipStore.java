package er.neo4jadaptor.storage.neo4j;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

import er.neo4jadaptor.ersatz.Ersatz;
import er.neo4jadaptor.ersatz.neo4j.Neo4JErsatz;
import er.neo4jadaptor.ersatz.neo4j.Neo4JRelationshipErsatz;
import er.neo4jadaptor.storage.Store;
import er.neo4jadaptor.utils.cursor.Cursor;

/**
 * Stores join entity records on Neo4J relationship. Join entity is an entity that has primary
 * key being two values at the same time being foreign keys pointing to other entities. There should
 * be no other EO relationships except for the main two making the join. Entity attributes are stored 
 * as relationship properties.
 * 
 * @author Jedrzej Sobanski
 */
public class RelationshipStore implements Store<Ersatz, Neo4JErsatz> {
	private final GraphDatabaseService db;
	private final EOEntity entity;
	
	private final EOAttribute pk1;
	private final EOAttribute pk2;
	
	private final RelationshipType relationshipType;
	
	public static boolean shouldBeStoredAsRelationship(EOEntity entity) {
		return entity.primaryKeyAttributes().count() == 2;
	}
	
	public RelationshipStore(GraphDatabaseService db, EOEntity entity) {
		this.db = db;
		this.entity = entity;
		relationshipType = DynamicRelationshipType.withName(entity.name());
		
		NSArray<EOAttribute> pks = entity.primaryKeyAttributes();
		
		if (! shouldBeStoredAsRelationship(entity)) {
			throw new IllegalArgumentException("This store can't handle entity " + entity.name());
		} else {
			pk1 = pks.get(0);
			pk2 = pks.get(1);
		}
	}

	public Ersatz newPrimaryKey() {
		// we don't care about primary key, it will be known on the insertion time
		return Ersatz.EMPTY;
	}

	public Neo4JErsatz insert(Ersatz row) {
		long pk1Val = ((Number) row.get(pk1)).longValue();
		long pk2Val = ((Number) row.get(pk2)).longValue();
		Node n1 = db.getNodeById(pk1Val);
		Node n2 = db.getNodeById(pk2Val);
		Relationship r = n1.createRelationshipTo(n2, relationshipType);
		Neo4JRelationshipErsatz ret = new Neo4JRelationshipErsatz(entity, r);
		
		Ersatz.copy(row, ret);
		
		return ret;
	}
	
	public void update(Ersatz newValues, Neo4JErsatz neoErsatz) {
		Ersatz.copy(newValues, neoErsatz);
	}
	
	public void delete(Neo4JErsatz neoErsatz) {
		neoErsatz.delete();
	}
	
	public Cursor<Neo4JErsatz> query(EOQualifier q) {
		throw new UnsupportedOperationException();
	}
}
