package er.neo4jadaptor.database.pool;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Provides database instances.
 * 
 * @author Jedrzej Sobanski
 */
public interface Neo4JDatabaseFactory {
	/**
	 * Get an instance at the given path with the specified configuration
	 * 
	 * @param path database location
	 * @param config Neo4J configuration
	 * @return database
	 */
	public GraphDatabaseService get(String path, Map<String, String> config);
}
