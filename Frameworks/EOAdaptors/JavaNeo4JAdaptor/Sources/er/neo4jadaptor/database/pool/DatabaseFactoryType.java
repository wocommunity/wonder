package er.neo4jadaptor.database.pool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.foundation.ERXProperties;

/**
 * Responsible for providing the right database type for a given label. 
 * 
 * @see DatabaseFactoryTypeLabels
 * 
 * @author Jedrzej Sobanski
 */
public enum DatabaseFactoryType {
	/**
	 * Embedded database running in read-only mode (useful when the db is locked by another instance running in writable mode and locking database).
	 */
	EMBEDDED_READ_ONLY(DatabaseFactoryTypeLabels.LABEL_EMBEDDED_READ_ONLY, new Neo4JDatabaseFactory() {
		public GraphDatabaseService get(String path, Map<String, String> config) {
			log.info("Creating Neo4J embedded read-only database instance with config {} in {}.", config, path);
			
			return new EmbeddedReadOnlyGraphDatabase(path, config);
		}
	}),
	
	/**
	 * Embedded database running in writable mode.
	 */
	EMBEDDED_WRITABLE(DatabaseFactoryTypeLabels.LABEL_EMBEDDED_WRITABLE, new Neo4JDatabaseFactory() {
		public GraphDatabaseService get(String path, Map<String, String> config) {
			log.info("Creating Neo4J embedded writable database instance with config {} in {}.", config, path);
			
			return new EmbeddedGraphDatabase(path, config);
		}
	}),
	
	/**
	 * Highly available database. 
	 * 
	 * <div><b>Requires Neo4J enterprise edition.</b></div>
	 */
	HIGHLY_AVAILABLE(DatabaseFactoryTypeLabels.LABEL_HIGHLY_AVAILABLE, new Neo4JDatabaseFactory() {
		public GraphDatabaseService get(String path, Map<String, String> config) {
			log.info("Creating Neo4J highly-available database instance with config {} in {}.", config, path);
			
			try {
				Constructor<? extends GraphDatabaseService> constructor = haConstructor();
				String serverId = config.get(HA_SERVER_ID_KEY);
				String fullPath = path + "-" + serverId;
			
				return constructor.newInstance(fullPath, config);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		
		@SuppressWarnings("unchecked")
		private Constructor<? extends GraphDatabaseService> haConstructor() throws SecurityException, NoSuchMethodException, ClassNotFoundException {
			Class<? extends GraphDatabaseService> haClass = (Class<? extends GraphDatabaseService>) Class.forName(HA_CLASS_NAME);
				
			return haClass.getConstructor(String.class, Map.class);
		}
	});
	
	private static final Logger log = LoggerFactory.getLogger(DatabaseFactoryType.class);

	private static final String HA_SERVER_ID_KEY = ERXProperties.stringForKey("neo4j.pool.database.highly-available.server-id-config-key");
	private static final String HA_CLASS_NAME = ERXProperties.stringForKey("neo4j.pool.database.highly-available.class");
	
	private final String label;
	private final Neo4JDatabaseFactory factory;
	
	private DatabaseFactoryType(String label, Neo4JDatabaseFactory factory) {
		this.label = label;
		this.factory = factory;
	}
	
	/**
	 * Gets factory type for the given type label.
	 * 
	 * @param label
	 * @return factory type
	 */
	public static DatabaseFactoryType getForLabel(String label) {
		for (DatabaseFactoryType df : values()) {
			if (label.equals(df.label)) {
				return df;
			}
		}
		return null;
	}

	public Neo4JDatabaseFactory getFactory() {
		return factory;
	}
}
