package er.caching;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import com.meetup.memcached.SockIOPool;
import com.meetup.memcached.test.UnitTests;
import com.thimbleware.jmemcached.Cache;
import com.thimbleware.jmemcached.LRUCacheStorageDelegate;
import com.thimbleware.jmemcached.MemCacheDaemon;
import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSForwardException;

import er.extensions.ERXFrameworkPrincipal;
import er.extensions.foundation.ERXProperties;

/**
 *
 * @property er.caching.servers
 * @property er.caching.server.host
 * @property er.caching.server.port
 * @property er.caching.server.maxItems
 * @property er.caching.server.maxMemory
 * @property er.caching.initialConnections
 * @property er.caching.sleepTime
 * @property er.caching.maxConnections
 * @property er.caching.minConnections
 * @property er.caching.useNagle
 */
public class ERCaching extends ERXFrameworkPrincipal {

	private static Logger log = Logger.getLogger(ERCaching.class);
	
    static {
        setUpFrameworkPrincipalClass(ERCaching.class);
    }

    public void finishInitialization() {
    	startServer();
        startClient();
    }

	public static void startClient() {
		String servers = ERXProperties.stringForKey("er.caching.servers");
        if (servers == null || servers.length() == 0) {
            log.error("No Servers found, set er.caching.servers=server1:port1,server2:port2...");
            return;
        }
        String[] serverlist = servers.split(",\\s*");

        // initialize the pool for memcache servers
        SockIOPool pool = SockIOPool.getInstance();
        pool.setServers(serverlist);

        pool.setInitConn(ERXProperties.intForKeyWithDefault("er.caching.initialConnections", 5));
        pool.setMinConn(ERXProperties.intForKeyWithDefault("er.caching.minConnections", 5));
        pool.setMaxConn(ERXProperties.intForKeyWithDefault("er.caching.maxConnections", 50));
        pool.setMaintSleep(ERXProperties.intForKeyWithDefault("er.caching.sleepTime", 30));

        pool.setNagle(ERXProperties.booleanForKeyWithDefault("er.caching.useNagle", false));
        pool.initialize();
	}

	public static void startServer() {
		try {
    		int port = ERXProperties.intForKeyWithDefault("er.caching.server.port", 0);
    		if(port > 0) {
        		int maxItems = ERXProperties.intForKeyWithDefault("er.caching.server.maxItems", 0);
        		int maxMemory = ERXProperties.intForKeyWithDefault("er.caching.server.maxMemory", 128);
    			MemCacheDaemon daemon = new MemCacheDaemon();
    			LRUCacheStorageDelegate cacheStorage = new LRUCacheStorageDelegate(maxItems, maxMemory * 1024*1024, 0);
    			daemon.setCache(new Cache(cacheStorage));
    			
    			String host = ERXProperties.stringForKeyWithDefault("er.caching.server.host", WOApplication.application().host());
				daemon.setAddr(new InetSocketAddress(host, port));
    			daemon.setIdleTime(50);
    			daemon.setVerbose(true);
    			daemon.start();
    			log.info("Server started: " + host + ":" + port + " maxItems=" + maxItems + " maxMemory=" + maxMemory);
    		}
        } catch (IOException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
	}

    public static void runTests() {
        //MemcachedBench.main(new String[]{"1000", "0"});
        /*MemcachedTest.main(new String[]{"4", "5", "5"});
        TestMemcached.main(new String[]{"4", "5", "5"});*/
        UnitTests.main(new String[]{});
    }
    
    
}
