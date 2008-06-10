package er.caching;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.meetup.memcached.SockIOPool;
import com.thimbleware.jmemcached.Cache;
import com.thimbleware.jmemcached.LRUCacheStorageDelegate;
import com.thimbleware.jmemcached.MemCacheDaemon;
import com.webobjects.foundation.NSForwardException;

import er.extensions.ERXFrameworkPrincipal;
import er.extensions.foundation.ERXProperties;

public class ERCaching extends ERXFrameworkPrincipal {

    static {
        setUpFrameworkPrincipalClass(ERCaching.class);
    }

    public void finishInitialization() {
        // finally setup the server
        try {
            MemCacheDaemon daemon = new MemCacheDaemon();
            LRUCacheStorageDelegate cacheStorage = new LRUCacheStorageDelegate(50000, 2 ^ 23, 1024000);
            daemon.setCache(new Cache(cacheStorage));
            daemon.setAddr(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 12345));
            daemon.setIdleTime(50);
            daemon.setPort(12345);
            daemon.setVerbose(true);
            daemon.start();
        } catch (IOException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }

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
        pool.setMaxConn(ERXProperties.intForKeyWithDefault("er.caching.initialConnections", 50));
        pool.setMaintSleep(ERXProperties.intForKeyWithDefault("er.caching.sleepTime", 30));

        pool.setNagle(ERXProperties.booleanForKeyWithDefault("er.caching.useNagle", false));
        pool.initialize();

    }

}
