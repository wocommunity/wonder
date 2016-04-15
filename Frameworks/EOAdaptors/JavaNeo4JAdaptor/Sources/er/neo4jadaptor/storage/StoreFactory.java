package er.neo4jadaptor.storage;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;

import com.webobjects.eoaccess.EOEntity;

import er.neo4jadaptor.ersatz.Ersatz;
import er.neo4jadaptor.ersatz.neo4j.Neo4JErsatz;
import er.neo4jadaptor.storage.lucene.LuceneStore;
import er.neo4jadaptor.storage.neo4j.NodeSpaceManager;
import er.neo4jadaptor.storage.neo4j.NodeStore;
import er.neo4jadaptor.storage.neo4j.RelationshipStore;
import er.neo4jadaptor.storage.neo4j.TemporaryNodePool;

/**
 * Provides Neo4J+Lucene hybrid store for each entity in Neo4J graph database. 
 * 
 * @author Jedrzej Sobanski
 */
public class StoreFactory {
	private final Map<String, Store<Ersatz, Neo4JErsatz>> map = new HashMap<String, Store<Ersatz, Neo4JErsatz>>();
	private final GraphDatabaseService db;
	private final NodeSpaceManager spaceManager;
	private final TemporaryNodePool tempNodePool;
	
	/**
	 * Create store factory for the given database.
	 * 
	 * @param db graph database
	 * @return store factory
	 */
	public static StoreFactory create(GraphDatabaseService db) {
		return new StoreFactory(db, new NodeSpaceManager(db));
	}
	
	private StoreFactory(GraphDatabaseService db, NodeSpaceManager spaceManager) {
		this.db = db;
		this.spaceManager = spaceManager;
		tempNodePool = new TemporaryNodePool(db, spaceManager);
	}

	/**
	 * Lazy gets store for the given entity. If such a store already exists then it will be returned
	 * instead of recreating.
	 * 
	 * @param <Type>
	 * @param entity
	 * @return store that's capable of handling requested entity
	 */
	public <Type extends PropertyContainer> Store<Ersatz, Neo4JErsatz> storeForEntity(EOEntity entity) {
		String label = toLabel(entity);
		Store<Ersatz, Neo4JErsatz> store = map.get(label);
		
		if (store == null) {
			int countPrimaryKeys = entity.primaryKeyAttributes().count();
			Store<Ersatz, Neo4JErsatz> neoStore;
			LuceneStore<Type> luceneStore = new LuceneStore<Type>(db, entity);
			
			if (countPrimaryKeys == 1) {
				neoStore = new NodeStore(db, entity, spaceManager, tempNodePool);
			} else if (countPrimaryKeys == 2) {
				neoStore = new RelationshipStore(db, entity);
			} else {
				throw new IllegalArgumentException();
			}
			store = new CompositeStore<Type>(neoStore, luceneStore);
			
			map.put(label, store);
		}
		return store;
	}
	
	private String toLabel(EOEntity entity) {
		return entity.name();
	}

	public TemporaryNodePool getTemporaryNodePool() {
		return tempNodePool;
	}
	
}
