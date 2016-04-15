package er.neo4jadaptor;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSDictionary;

import er.neo4jadaptor.ersatz.Ersatz;
import er.neo4jadaptor.ersatz.webobjects.NSDictionaryErsatz;
import er.neo4jadaptor.storage.Store;
import er.neo4jadaptor.storage.StoreFactory;

public class Neo4JContext <T extends Ersatz> extends EOAdaptorContext {
	private static final Logger log = LoggerFactory.getLogger(Neo4JContext.class);

	private Transaction tx;
	private final StoreFactory storeFactory;
	
	public Neo4JContext(Neo4JAdaptor adaptor) {
		super(adaptor);
		
		storeFactory = StoreFactory.create(adaptor.getDatabase());
	}
	
	@Override
	public Neo4JAdaptor adaptor() {
		return (Neo4JAdaptor) super.adaptor();
	}
	
	public Store<Ersatz, T> entityStoreForEntity(EOEntity entity) {
		return (Store<Ersatz, T>) storeFactory.storeForEntity(entity);
	}
	
	@Override
	public NSDictionary<String, Object> _newPrimaryKey(EOEnterpriseObject object, EOEntity entity) {
		Store<?, ?> store = entityStoreForEntity(entity);
		Ersatz pk = store.newPrimaryKey();
		
		return NSDictionaryErsatz.toSnapshot(pk);
	}
	
	private GraphDatabaseService getDatabase() {
		return adaptor().getDatabase();
	}
	
	@Override
	public void beginTransaction() {
		if (! hasOpenTransaction()) {
			tx = getDatabase().beginTx();
			transactionDidBegin();
		}
	}

	@Override
	public void commitTransaction() {
		try {
			storeFactory.getTemporaryNodePool().cleanup();

			tx.success();
		} finally {
			try {
				tx.finish();
				transactionDidCommit();
			} finally {
				tx = null;
			}
		}
	}
	
	@Override
	public EOAdaptorChannel createAdaptorChannel() {
		return new Neo4JChannel<T>(this);
	}

	@Override
	public void handleDroppedConnection() {
		log.warn("Dropped connection");
	}

	@Override
	public void rollbackTransaction() {
		if (tx != null) {
			try {
				tx.failure();
			} finally {
				tx.finish();
			}
		}
		transactionDidRollback();
	}
}
