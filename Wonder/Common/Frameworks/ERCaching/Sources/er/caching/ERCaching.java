package er.caching;

import com.danga.memcached.SockIOPool;

import er.extensions.ERXFrameworkPrincipal;
import er.extensions.ERXProperties;
import er.extensions.ERXSystem;

public class ERCaching extends ERXFrameworkPrincipal {

    static {
        setUpFrameworkPrincipalClass(ERCaching.class);
    }

    public void finishInitialization() {
        // finally setup the server
        /*ServiceRegistry registry = new SimpleServiceRegistry();
        InetSocketAddress addr = new InetSocketAddress("localhost", 1624);
        try {
            registry.bind(new Service("Memcached", TransportType.SOCKET, addr), new ServerSessionHandler(1000, "0.1", true, 0, 32 * 1024000));
        } catch (IOException e) {
           throw NSForwardException._runtimeExceptionForThrowable(e);
        }
*/
        String servers = ERXProperties.stringForKey("er.caching.servers");
        if(servers.length() == 0) {
            log.error("No Servers found");
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
