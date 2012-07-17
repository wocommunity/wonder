package er.neo4jadaptor.storage;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.helpers.collection.MapUtil;

import com.webobjects.eoaccess.EOEntity;

import er.neo4jadaptor.storage.neo4j.RelationshipStore;

/**
 * Provides lucene indexes for various entities.
 * 
 * @author Jedrzej Sobanski
 *
 */
public class IndexProvider {

	private static final Map<String, String> INDEX_CONFIG = MapUtil.stringMap(
			IndexManager.PROVIDER, "lucene",
			"type", "exact"
			);
	
	public static final IndexProvider instance = new IndexProvider();
	
	private IndexProvider() {
		
	}
	
	public Index<? extends PropertyContainer> getIndexForEntity(GraphDatabaseService db, EOEntity entity) {
		if (RelationshipStore.shouldBeStoredAsRelationship(entity)) {
			Index<Relationship> index = db.index().forRelationships("relationships", INDEX_CONFIG);
			
			return index;
		} else {
			Index<Node> index = db.index().forNodes("nodes", INDEX_CONFIG);
			
			return index;
		}
	}
}
