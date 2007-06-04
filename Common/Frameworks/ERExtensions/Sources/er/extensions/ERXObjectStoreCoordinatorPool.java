package er.extensions;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Observer;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOSession;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

/**
 * This class implements EOF stack pooling including EOF stack synchronizing. 
 * It provides a special ERXEC.Factory in order to work without any changes in existing 
 * applications. The number of EOObjectStoreCoordinators can be set with the 
 * system Property <code>er.extensions.ERXObjectStoreCoordinatorPool.maxCoordinators</code>. 
 * Each Session will become one EOObjectStoreCoordinator and the method 
 * <code>newEditingContext</code> will always return an <code>EOEditingContext</code> 
 * with the same <code>EOObjectStoreCoordinator</code> for the same <code>WOSession</code>. 
 * This first release uses round-robin pooling, future versions might use better algorithms 
 * to decide which <code>EOObjectStoreCoordinator</code> will be used for the next new 
 * <code>WOSession</code>.<br>The code is tested in a heavy  multithreaded application 
 * and afawk no deadlock occures, neither in EOF nor directly in Java.
 * 
 * @author David Teran, Frank Caputo @ cluster9
 */
public class ERXObjectStoreCoordinatorPool {
    private static Logger log = Logger.getLogger(ERXObjectStoreCoordinatorPool.class);
    
    private static final String THREAD_OSC_KEY = "er.extensions.ERXObjectStoreCoordinatorPool.threadOSC";
    private Hashtable _oscForSession;
    private int _maxOS;
    private int _currentObjectStoreIndex;
    private List _objectStores;
    private List _sharedEditingContexts;
    private Observer _observer;
    private Object _lock = new Object();
    protected static ERXObjectStoreCoordinatorPool _pool;

    public static ERXObjectStoreCoordinatorPool _pool() {
    	return _pool;
    }
    
    /**
     * Calls initialize() if the required system properties exist.
     *
     */
    public static void initializeIfNecessary() {
    	if (ERXProperties.stringForKey("er.extensions.ERXObjectStoreCoordinatorPool.maxCoordinators") != null) {
    		ERXObjectStoreCoordinatorPool.initialize();
    	}
    }
    
    /**
     * Creates the singleton and registers the multi factory.
     */
    public static void initialize() {
        if(_pool == null) {
            ERXObjectStoreCoordinatorSynchronizer.initialize();
            _pool = new ERXObjectStoreCoordinatorPool();
            log.info("setting ERXEC.factory to MultiOSCFactory");
            ERXEC.setFactory(new MultiOSCFactory());
        }
    }
    
    /**
     * Creates a new ERXObjectStoreCoordinatorPool. This object is a singleton. 
     * This object is responsible to provide EOObjectStoreCoordinators 
     * based on the current Threads' session. 
     * It is used by MultiOSCFactory to get a rootObjectStore if the 
     * MultiOSCFactory is asked for a new EOEditingContext.
     */
    private ERXObjectStoreCoordinatorPool() {
        _maxOS = ERXProperties.intForKey("er.extensions.ERXObjectStoreCoordinatorPool.maxCoordinators");
        if (_maxOS == 0) {
            //this should work like the default implementation
            log.warn("Registering the pool with only one coordinator doesn't make a lot of sense.");
            _maxOS = 1;
        }
        _oscForSession = new Hashtable();
        
        NSSelector sel = new NSSelector("sessionDidCreate", ERXConstant.NotificationClassArray);
        NSNotificationCenter.defaultCenter().addObserver(this, sel, WOSession.SessionDidCreateNotification, null);
        sel = new NSSelector("sessionDidTimeout", ERXConstant.NotificationClassArray);
        NSNotificationCenter.defaultCenter().addObserver(this, sel, WOSession.SessionDidTimeOutNotification, null);
    }
    
    /** 
     * checks if the new Session has already  a EOObjectStoreCoordinator assigned, 
     * if not it assigns a EOObjectStoreCoordinator to the session.
     * @param n {@link WOSession#SessionDidCreateNotification}
     */
    public void sessionDidCreate(NSNotification n) {
        WOSession s = (WOSession) n.object();
        if (_oscForSession.get(s.sessionID()) == null) {
            _oscForSession.put(s.sessionID(), nextObjectStore());
        }
    }
    
    /** Removes the timed out session from the internal array.
     * session.
     * @param n {@link WOSession#SessionDidTimeOutNotification}
     */
    public void sessionDidTimeout(NSNotification n) {
        String sessionID = (String) n.object();
        _oscForSession.remove(sessionID);
    }
    
    /**
     * @return the sessionID from the session stored in ERXThreadStorage.
     */
    protected String sessionID() {
        WOSession session = ERXSession.session();
        String sessionID = null;
        if (session != null) {
            sessionID = session.sessionID();
        }
        return sessionID;
    }
    
    /** 
     * returns the session related EOObjectStoreCoordinator. 
     * If session is null then it returns the nextObjectStore.
     * This method is used to create new EOEditingContexts with the MultiOSCFactory
     * @return an EOEditingContext
     */
    public EOObjectStore currentRootObjectStore() {
        String sessionID = sessionID();
        EOObjectStore os = null;
        if (sessionID != null) {
            os = (EOObjectStore) _oscForSession.get(sessionID);
            if (os == null) {
                os = nextObjectStore();
                _oscForSession.put(sessionID, os);
            }
        } else {
        	os = (EOObjectStore) ERXThreadStorage.valueForKey(ERXObjectStoreCoordinatorPool.THREAD_OSC_KEY);
        	if (os == null) {
        		os = nextObjectStore();
        		if (os != null) {
        			ERXThreadStorage.takeValueForKey(os, ERXObjectStoreCoordinatorPool.THREAD_OSC_KEY);
        		}
        	}
        }
        return os;
    }
    
    /** 
     * Lazy initialises the objectStores and then returns the next one, 
     * this is based on round robin.
     * @return the next EOObjectStore based on round robin
     */
    public EOObjectStore nextObjectStore() {
        synchronized (_lock) {
            if (_objectStores == null) {
                _initObjectStores();
            }
            if (_currentObjectStoreIndex == _maxOS) {
                _currentObjectStoreIndex = 0;
            }
            EOObjectStore os = (EOObjectStore) _objectStores.get(_currentObjectStoreIndex++);
        	return os;
        }
    }
    
    public EOSharedEditingContext sharedEditingContextForObjectStore(EOObjectStore os) {
        int index = _objectStores.indexOf(os);
        EOSharedEditingContext ec = null;
        if(index >= 0) {
            ec = (EOSharedEditingContext)_sharedEditingContexts.get(index);
        }
        return ec;
    }
    
    /** 
     * This class uses different EOF stack when creating new EOEditingContexts.
     */
    public static class MultiOSCFactory extends ERXEC.DefaultFactory {
        public MultiOSCFactory() {
            super();
        }
        public EOEditingContext _newEditingContext() {
            return _newEditingContext(true);
        }
        
        public EOEditingContext _newEditingContext(boolean validationEnabled) {
            EOObjectStore os = _pool.currentRootObjectStore();
            EOEditingContext ec = _newEditingContext(os, validationEnabled);
            ec.lock();
            try {
            	EOSharedEditingContext sec = (useSharedEditingContext()) ? _pool.sharedEditingContextForObjectStore(os) : null;
            	ec.setSharedEditingContext(sec);
            } finally {
                ec.unlock();
            }
            return ec;
        }
    }
    
    private void _initObjectStores() {
        log.info("initializing Pool...");
        _objectStores = new ArrayList(_maxOS);
        _sharedEditingContexts = new ArrayList(_maxOS);
        for (int i = 0; i < _maxOS; i++) {
            EOObjectStore os = new EOObjectStoreCoordinator();
            _objectStores.add(os);
            _sharedEditingContexts.add(new EOSharedEditingContext(os));
        }
        if(_maxOS > 0) {
            EOObjectStoreCoordinator.setDefaultCoordinator((EOObjectStoreCoordinator)_objectStores.get(0));
        }
        log.info("initializing Pool finished");
     }
}

