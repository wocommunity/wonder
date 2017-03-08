package er.extensions.concurrency;

import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXObjectStoreCoordinator;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXRoundRobinCollection;

/**
 * A class that manages a pool of object store coordinators for background tasks.
 * Such a pool is useful to prevent intensive EOF activity of background tasks
 * interfering with normal use page browsing.
 * 
 * The default pool size is one. More can be set using the property
 * <code>er.extensions.concurrency.ERXTaskObjectStoreCoordinatorPool.maxCoordinators</code>
 * 
 * @author kieran
 *
 */
public class ERXTaskObjectStoreCoordinatorPool {
	private static class Pool {
		static final ERXRoundRobinCollection<EOObjectStoreCoordinator> COLLECTION = initializePool();
		
		static ERXRoundRobinCollection<EOObjectStoreCoordinator> initializePool() {
			int maxCoordinators = ERXProperties.intForKeyWithDefault("er.extensions.concurrency.ERXTaskObjectStoreCoordinatorPool.maxCoordinators", 1);
			NSMutableArray<EOObjectStoreCoordinator> coordinators = new NSMutableArray<>(maxCoordinators);
			for (int i = 0; i < maxCoordinators; i++) {
				int poolItemID = i + 1;
				ERXObjectStoreCoordinator osc = new ERXObjectStoreCoordinator(true);
				osc.setName("TaskPool-" + poolItemID + "/" + maxCoordinators);
				coordinators.add(osc);
			}
			return new ERXRoundRobinCollection<>(coordinators.immutableClone());
		}
	}
	
	/**
	 * @return a OSC from the pool in Round Robin fashion
	 */
	public static EOObjectStoreCoordinator objectStoreCoordinator() {
		return Pool.COLLECTION.next();
	}

}
