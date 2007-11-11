package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOCooperatingObjectStore;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;

/**
 * @author david
 * 
 * Adds functionality to automatically close all related JDBC Connections.
 */
public class ERXObjectStoreCoordinator extends EOObjectStoreCoordinator {
	public static Logger log = Logger.getLogger(ERXObjectStoreCoordinator.class);

	public boolean _didClose = false;
	public boolean _shouldClose = false;

	/**
	 * @see com.webobjects.eocontrol.EOObjectStoreCoordinator
	 * 
	 */
	public ERXObjectStoreCoordinator() {
		super();
	}

	public ERXObjectStoreCoordinator(boolean shouldClose) {
		this();
		_shouldClose = shouldClose;
	}

	@Override
	public void addCooperatingObjectStore(EOCooperatingObjectStore objectStore) {
		if (cooperatingObjectStores().indexOfIdenticalObject(objectStore) < 0) {
			if (objectStore.coordinator() != null) {
				throw new IllegalStateException("Cannot add " + objectStore + " to this EOObjectStoreCoordinator because it already has another.");
			}
			super.addCooperatingObjectStore(objectStore);
		}
	}

	@Override
	public void dispose() {
		if (_shouldClose) {
			_didClose = ERXEOAccessUtilities.closeDatabaseConnections(this);
			if (!_didClose && _shouldClose) {
				log.error("shouldClose was true but could not close all Connections!");
			}
		}
		super.dispose();
	}
}
