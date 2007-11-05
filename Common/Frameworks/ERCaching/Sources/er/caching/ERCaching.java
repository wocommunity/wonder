package er.caching;

import com.danga.memcached.SockIOPool;

import er.extensions.ERXFrameworkPrincipal;

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
        String[] serverlist = { "localhost:1624" };

        // initialize the pool for memcache servers
        SockIOPool pool = SockIOPool.getInstance();
        pool.setServers(serverlist);

        pool.setInitConn(5);
        pool.setMinConn(5);
        pool.setMaxConn(50);
        pool.setMaintSleep(30);

        pool.setNagle(false);
        pool.initialize();

    }

}
