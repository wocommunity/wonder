package er.neo4jadaptor.database.pool;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.foundation.ERXProperties;

/**
 * Provides database instances with all its settings, except for the filesystem location, being read from
 * system properties. Additionally it can perform database warm up, which can be useful especially in
 * cases where the entire database is cached in memory.
 * <p>
 * Configuration system properties:
 * <ul>
 * <li><code>{@value #DBTYPE_PROPERTY_KEY}</code> - database type label. Possible values are described in {@link DatabaseFactoryTypeLabels}.</li>
 * <li><code>{@value #CONFIG_PROPERTY_KEY}</code> - Neo4J database configuration dictionary</li>
 * <li><code>{@value #WARMUP_PROPERTY_KEY}</code> - boolean flag denoting whether to perform initial database warm up (iterates over all properties for
 * all nodes), defaults to {@value #WARMUP_DEFAULT_VALUE}</li>
 * </ul>
 * 
 * 
 * @author Jedrzej Sobanski
 */
public class DatabasePool {
	private static final Logger log = LoggerFactory.getLogger(DatabasePool.class);
	
	/**
	 * Singleton instance.
	 */
	public static final DatabasePool instance = new DatabasePool();
	
	/**
	 * System property name for database {@link DatabaseFactoryType} label.
	 * 
	 * @see DatabaseFactoryTypeLabels
	 */
	public static final String DBTYPE_PROPERTY_KEY = "neo4j.pool.database.type";
	
	/**
	 * System property name for Neo4J configuration dictionary.
	 */
	public static final String CONFIG_PROPERTY_KEY = "neo4j.pool.database.config";
	
	/**
	 * System property name for db warmup.
	 */
	public static final String WARMUP_PROPERTY_KEY = "neo4j.pool.doWarmUp";
	
	/**
	 * Default value for db warmup setting.
	 */
	public static final boolean WARMUP_DEFAULT_VALUE = false;
	
	private final Map<String, GraphDatabaseService> map = new HashMap<>();
	private final boolean WARMUP_DATABASE = ERXProperties.booleanForKeyWithDefault(WARMUP_PROPERTY_KEY, WARMUP_DEFAULT_VALUE);
	private final String databaseType = ERXProperties.stringForKey(DBTYPE_PROPERTY_KEY);
	
	private DatabasePool() {
		
	}
	
	/**
	 * Gets database instance configured as in system properties operating at the given URL.
	 * 
	 * @param url database url
	 * @return database
	 */
	public GraphDatabaseService get(String url) {
		if (! url.startsWith("file://")) {
			throw new IllegalArgumentException("Only URLs for file protocol (starting with 'file://') are supported");
		}
		String path = url.substring("file://".length());
		
		GraphDatabaseService db = map.get(url);
		
		if (db == null) {
			Map<String, String> config = getConfig();
			DatabaseFactoryType factoryType = DatabaseFactoryType.getForLabel(databaseType);
			
			try {				
				db = factoryType.getFactory().get(path, config);
			} catch (RuntimeException e) {
				log.error("Failed to create database", e);
				throw e;
			}
			
			if (WARMUP_DATABASE) {
				warmUp(db);
			}
			
			registerShutdownHook(db);
			map.put(url, db);
		}
		return db;
	}
	
	@SuppressWarnings("deprecation")
	private void warmUp(GraphDatabaseService db) {
		for (Node n : db.getAllNodes()) {
			for (String s : n.getPropertyKeys()) {
				n.getProperty(s);
			}
			
			for (Relationship r : n.getRelationships()) {
				for (String s : r.getPropertyKeys()) {
					r.getProperty(s);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, String> getConfig() {
		return ERXProperties.dictionaryForKey(CONFIG_PROPERTY_KEY);
	}

	private void registerShutdownHook(final GraphDatabaseService graphDb) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}
}
