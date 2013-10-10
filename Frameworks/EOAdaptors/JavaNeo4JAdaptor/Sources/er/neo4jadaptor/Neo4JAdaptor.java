package er.neo4jadaptor;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.synchronization.EOSchemaSynchronizationFactory;
import com.webobjects.foundation.NSDictionary;

import er.neo4jadaptor.database.pool.DatabasePool;

@SuppressWarnings("deprecation")
public class Neo4JAdaptor extends EOAdaptor {
	private static final String CONFIG_PATH_KEY = "URL";
	
	private GraphDatabaseService db;
	
	static {
//		Neo4JDelegate.init();
	}
	
	public static void init() {
		// trigger static initializer
	}
	
	public Neo4JAdaptor(String name) {
		super(name);
	}

	@Override
	public void assertConnectionDictionaryIsValid() {
	}
	
	@Override
	public void setConnectionDictionary(NSDictionary<String, Object> dictionary) {
		super.setConnectionDictionary(dictionary);
		
		String databasePath = (String) dictionary.objectForKey(CONFIG_PATH_KEY);
		
		if (databasePath == null) {
			throw new IllegalArgumentException("No \"" + CONFIG_PATH_KEY + "\" value given");
		}
		
		db = DatabasePool.instance.get(databasePath);
	}

	@Override
	public EOSQLExpressionFactory expressionFactory() {
		return null;
	}

	@Override
	public boolean isValidQualifierType(String typeName, EOModel model) {
		return true;
	}

	@Override
	public EOSchemaSynchronizationFactory schemaSynchronizationFactory() {
		return null;
	}
	
	@Override
	public Class<?> defaultExpressionClass() {
		throw new UnsupportedOperationException();
	}

	
	
	@Override
	public EOAdaptorContext createAdaptorContext() {
		return new Neo4JContext(this);
	}

	@Override
	public EOSchemaGeneration synchronizationFactory() {
		throw new UnsupportedOperationException();
	}

	public GraphDatabaseService getDatabase() {
		return db;
	}

	public Number nextValFromSequenceNamed(String sequenceName) {
		Node node = db.getReferenceNode();
		Transaction tx = db.beginTx();

		try {
			int val = (Integer) node.getProperty(sequenceName, 0);
			
			node.setProperty(sequenceName, val+1);
			tx.success();
			
			return val;
		} finally {
			tx.finish();
		}
	}
}
