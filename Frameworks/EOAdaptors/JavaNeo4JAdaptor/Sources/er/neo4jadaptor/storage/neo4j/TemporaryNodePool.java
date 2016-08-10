package er.neo4jadaptor.storage.neo4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import er.extensions.foundation.ERXProperties;
import er.neo4jadaptor.storage.neo4j.NodeSpaceManager.Space;

/**
 * <p>
 * Provides temporary node instances. This class has been created due to performance requirements, as
 * nodes can be created only within transaction, which is costy to create each new node within one transaction.
 * This class creates a set of nodes within single transaction, which then can be passed to class' client
 * and if it runs out of nodes then will create new batch again in one transaction.
 * </p>
 * 
 * <p>
 * Batches are created at various size, growing exponentially
 * </p>
 * 
 * @author Jedrzej Sobanski
 */
public class TemporaryNodePool {
	private static final int INITIAL_POOL_SIZE = ERXProperties.intForKeyWithDefault("TemporaryNodePool.initialPoolSize", 4);
	private static final int MAX_POOL_SIZE = ERXProperties.intForKeyWithDefault("TemporaryNodePool.maxPoolSize", 256);
	
	private final GraphDatabaseService db;
	private final NodeSpaceManager spaceManager;
	
	private List<Node> pool;
	Integer previousPoolSize = null;
	
	public TemporaryNodePool(GraphDatabaseService db, NodeSpaceManager spaceManager) {
		this.db = db;
		this.spaceManager = spaceManager;
	}
	
	private void createPool(int poolSize) {
		List<Node> nodes = new ArrayList<Node>();
		Transaction tx = db.beginTx();
		
		try {
			for (int i=0; i<poolSize; i++) {
				Node node = db.createNode();
				
				spaceManager.setIsTemporary(node);
				
				nodes.add(node);
			}
			tx.success();
		} finally {
			tx.finish();
		}
		pool = nodes;
	}
	
	public Node getNextTemporaryNode() {
		if (pool == null || pool.isEmpty()) {
			int poolSize = previousPoolSize != null ? 2*previousPoolSize : INITIAL_POOL_SIZE;
			
			poolSize = Math.min(poolSize, MAX_POOL_SIZE);
			poolSize = Math.min(poolSize, INITIAL_POOL_SIZE);
			
			createPool(poolSize);
			
			previousPoolSize = poolSize;
		}
		
		if (pool.isEmpty()) {
			throw new IllegalStateException();
		}
		
		Node ret = pool.get(0);
		
		pool.remove(ret);
		
		return ret;
	}

	public void cleanup() {
		Node root = db.getReferenceNode();
		Iterator<Relationship> it = root.getRelationships(Space.TEMPORARY, Direction.OUTGOING).iterator();
		
		while (it.hasNext()) {
			Relationship r = it.next();
			Node endNode = r.getEndNode();
			
			r.delete();
			endNode.delete();
		}
		pool = null;
		previousPoolSize = null;
	}
}
