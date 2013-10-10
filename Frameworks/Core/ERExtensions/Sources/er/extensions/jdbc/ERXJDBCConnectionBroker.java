package er.extensions.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Date;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOCooperatingObjectStore;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.jdbcadaptor.JDBCAdaptor;
import com.webobjects.jdbcadaptor.JDBCPlugIn;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Creates and manages a pool of JDBC connections. Useful for SQL statements without
 * using / blocking EOF. Maintains one broker per each distinct connection dictionary. 
 * Connections are created on demand but not freed.  You can change the behaviour of the 
 * broker by setting some parameters either via the system properties as 
 * <code>dbSomePropertyGLOBAL</code>, <code>ModelName.DBSomeProperty</code> or as 
 * <code>someProperty</code> in the connection dictionary. 
 * <dl>
 * <dt>minConnections</dt>
 * <dd>Minimum number of connections, default 1</dd>
 * <dt>maxConnections</dt>
 * <dd>Maximum number of connections, default 1</dd>
 * <dt>maxCheckout</dt>
 * <dd>Maximum number of seconds a connection should stay checked out, default 600</dd>
 * <dt>connectionRecycle</dt>
 * <dd>Number of days a connection should stay active, default 1.0</dd>
 * </dl>
 * The {@link er.extensions.foundation.ERXConfigurationManager} adds these entries to each
 * EOModels connectionDictionary.<br/>
 * Usage: check out a connection: <br/><code>
 * <pre>
 * java.sql.Connection con = ERXJDBCConnectionBroker.connectionBrokerForModel(myModel).getConnection();
 * try {
 *     java.sql.Statement s = con.createStatement();
 *     //now do something with the Statement
 * } finally {
 *     ERXJDBCConnectionBroker.connectionBrokerForModel(myModel).freeConnection(con);
 * }
 * </pre></code>
 * @author Marc A. Mnich, based on version 1.0.13 3/12/02
 * @author david@cluster9.com original Wonder version
 * @author ak Major refactoring
 */

// CHECKME ak: How should the maxCheckout stuff work? We can't really close a
// connection while it still active??
public class ERXJDBCConnectionBroker implements ERXJDBCAdaptor.ConnectionBroker {

    public static final Logger log = Logger.getLogger(ERXJDBCConnectionBroker.class);

    private static Hashtable brokers = new Hashtable();

    private Thread reaper;

    private Thread pinger;

    private ConnectionWrapper[] wrappers;

    private String dbDriver, dbServer, dbLogin, dbPassword;

    private int activeConnections, lastRoundRobinIndex, minimumConnections, maximumConnections, maxCheckoutMillis;

    long maxConnectionMillis;

    private boolean active = true;

    private boolean supportsTransactions = false;

    private static final int DEFAULTMAXCHECKOUTSECONDS = 600;

    public static ERXJDBCConnectionBroker connectionBrokerForModelWithName(String modelName) {
        return connectionBrokerForModel(EOModelGroup.defaultGroup().modelNamed(modelName));
    }

    public static ERXJDBCConnectionBroker connectionBrokerForModel(EOModel model) {
        return connectionBrokerForConnectionDictionary(model.connectionDictionary());
    }

    public static ERXJDBCConnectionBroker connectionBrokerForEoInEditingContext(EOEnterpriseObject eo) {
        EOObjectStore os = eo.editingContext().rootObjectStore();
        if (os instanceof EOObjectStoreCoordinator) {
            EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator)os;
            EOCooperatingObjectStore cos = osc.objectStoreForObject(eo);
            if (cos instanceof EODatabaseContext) {
                EODatabaseContext dbctx = (EODatabaseContext)cos;
                EOAdaptor adaptor = dbctx.database().adaptor();
                return ERXJDBCConnectionBroker.connectionBrokerForAdaptor(adaptor);
            }
        }
        throw new IllegalStateException("No connection broker found for EC");
    }

    public static ERXJDBCConnectionBroker connectionBrokerForEntityNamed(String ename) {
        return connectionBrokerForModel(EOModelGroup.defaultGroup().entityNamed(ename).model());
    }

    public static ERXJDBCConnectionBroker connectionBrokerForAdaptor(EOAdaptor adaptor) {
        return connectionBrokerForConnectionDictionary(adaptor.connectionDictionary());
    }

    private static synchronized ERXJDBCConnectionBroker connectionBrokerForConnectionDictionary(NSDictionary d) {
        String key = "";
        String keys [] = new String[] {"URL", "username", "password", "driver", "plugin"};
        for (int i = 0; i < keys.length; i++) {
			key += d.objectForKey(keys[i]) + "\0";
		}
        ERXJDBCConnectionBroker broker = (ERXJDBCConnectionBroker) brokers.get(key);
        if (broker == null) {
            broker = newConnectionBrokerWithConnectionDictionary(d);
            brokers.put(key, broker);
        }
        return broker;
    }

    private static ERXJDBCConnectionBroker newConnectionBrokerWithConnectionDictionary(NSDictionary dict) {
        ERXJDBCConnectionBroker broker = null;
        try {
            broker = new ERXJDBCConnectionBroker(dict);
        } catch(Exception ex) {
            log.error("Error while creating broker: " + broker, ex);
            throw new NSForwardException(ex, "Error while creating broker: " + broker);
        }
        return broker;
    }

    private ERXJDBCConnectionBroker(NSDictionary dict) {
        setup(dict, DEFAULTMAXCHECKOUTSECONDS);
    }

    @Override
    public String toString() {
        return "<" +getClass().getName() +
        ": dbDriver = " + dbDriver +
        ", dbServer = " + dbServer +
        ", dbLogin = " + dbLogin +
        ", activeConnections = " + activeConnections +
        ", maximumConnections = " + maximumConnections +
        ", maxCheckoutMillis = " + maxCheckoutMillis +
        ", maxConnectionMillis = " + maxConnectionMillis;
    }

    private void setup(NSDictionary dict, int maxCheckoutSecond) {
        dbDriver = (String) dict.objectForKey("driver");
        dbServer = (String) dict.objectForKey("URL");
        dbLogin = (String) dict.objectForKey("username");
        dbPassword = (String) dict.objectForKey("password");
        
        if (dbDriver == null || dbDriver.length() == 0) {
        	JDBCAdaptor jdbcAdaptor = new JDBCAdaptor("JDBC");
        	jdbcAdaptor.setConnectionDictionary(dict);
        	JDBCPlugIn plugIn = jdbcAdaptor.plugIn();
        	dbDriver = plugIn.defaultDriverName();
        }

        minimumConnections = ERXValueUtilities.intValueWithDefault(dict.objectForKey("minConnections"), ERXProperties.intForKeyWithDefault("er.extensions.ERXJDBCConnectionBroker.minConnections", 1));
		maximumConnections = ERXValueUtilities.intValueWithDefault(dict.objectForKey("maxConnections"), ERXProperties.intForKeyWithDefault("er.extensions.ERXJDBCConnectionBroker.maxConnections", 1));
		maxCheckoutMillis = ERXValueUtilities.intValueWithDefault(dict.objectForKey("maxCheckout"), ERXProperties.intForKeyWithDefault("er.extensions.ERXJDBCConnectionBroker.maxCheckout", maxCheckoutSecond)) * 1000;
		maxConnectionMillis = ERXValueUtilities.bigDecimalValueWithDefault(dict.objectForKey("connectionRecycle"), BigDecimal.valueOf(1)).longValue() * 86400000;
        
        if (maxConnectionMillis < 30000) { // Recycle no less than 30 seconds.
            maxConnectionMillis = 30000;
        }

        // Initialize the pool of connections with the mininum connections:
        // Problems creating connections may be caused during reboot when
        // the servlet is started before the database is ready. Handle this
        // by waiting and trying again. The loop allows 5 minutes for
        // db reboot.
        boolean success = false;
        int maxTries = 20;

        wrappers = new ConnectionWrapper[maximumConnections];

        for (int tries = 1; tries < 20 && !success; tries++) {
            try {
                for (int j = 0; j < minimumConnections; j++) {
                    createWrapper();
                }
                success = true;
            } catch (SQLException e) {
                log.error("Can't create connection " + tries + " of " + maxTries + ", will retry in 15 seconds: " + e, e);
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e1) {
                }
            }
        }
        if (!success) { 
            throw new IllegalStateException("All attempts at connecting to Database exhausted: " + this);
        }

        Connection con = getConnection();
        try {
            supportsTransactions = (con.getTransactionIsolation() != 0);

            if(supportsTransactions) {
                con.setAutoCommit(false);
            }
        } catch (SQLException ex) {
            log.error(ex, ex);
        } finally {
            freeConnection(con);
        }

       reaper = new Thread() {
            /**
             * Housekeeping thread. Runs in the background with low CPU overhead.
             * Connections are checked for warnings and closure and are periodically
             * restarted. This thread is a catchall for corrupted connections and
             * prevents the buildup of open cursors. (Open cursors result when the
             * application fails to close a Statement). This method acts as fault
             * tolerance for bad connection/statement programming.
             */
            @Override
            public void run() {
                while (true) {
                    synchronized (wrappers) {
                        for (int i = 0; i < activeConnections; i++) {
                            ConnectionWrapper connection = wrappers[i];
                            connection.clearWarnings();
                        }
                    }
                    synchronized (wrappers) {
                        for (int i = 0; i < activeConnections; i++) { 
                            ConnectionWrapper connection = wrappers[i];
                            try {
                                connection.reap(maxCheckoutMillis, maxConnectionMillis);
                            } catch (SQLException e) {
                                log.error("Error while reaping: " + connection);
                            }
                        }
                    }
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }        

            /**
             * Less safe shutdown. Uses default timeout value. This method simply
             * calls the <code>destroy()</code> method with a <code>millis</code>
             * value of 10000 (10 seconds) and ignores <code>SQLException</code>
             * thrown by that method.
             * 
             * @see #destroy(int)
             */
            @Override
            public void destroy() {
                try {
                    ERXJDBCConnectionBroker.this.destroy(10000);
                } catch (SQLException e) {
                }
            }
        };

        pinger = new Thread(new Runnable() {
            boolean b = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXJDBCConnectionBroker.connectionPingEnabled", false);
            int wait = ERXProperties.intForKeyWithDefault("er.extensions.ERXJDBCConnectionBroker.connectionPingInterval", 60 * 5);

            public void run() {
                log.debug("Starting up ConnectionPing");
                while (b) {
                    synchronized (wrappers) {
                        for (int i = 0; i < wrappers.length; i++) {
                            ConnectionWrapper connection = wrappers[i];
                            connection.ping();
                        }
                    }
                    try {
                        Thread.sleep(wait * 1000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }

        });
        reaper.setName("ERXJDBCReaper");
        reaper.setDaemon(true);
        reaper.start();
        pinger.setName("ERXJDBCPinger");
        pinger.setDaemon(true);
        pinger.start();
        
        log.info("Started Broker : " + this);
    }


    /**
     * This method hands out the connections in round-robin order. This
     * prevents a faulty connection from locking up an application entirely.
     * A browser 'refresh' will get the next connection while the faulty
     * connection is cleaned up by the housekeeping thread.
     * 
     * If the min number of threads are ever exhausted, new threads are
     * added up the the max thread count. Finally, if all threads are in
     * use, this method waits 2 seconds and tries again, up to ten times.
     * After that, it returns a null.
     */
    public Connection getConnection() {

        Connection result = null;

        if (!active) {
            throw new IllegalStateException("Unsuccessful getConnection() request during destroy()");
        }
        boolean gotOne = false;

        for(int tries = 0; tries <= 10; tries++) {

            int loop = 0;
            int roundRobin = lastRoundRobinIndex + 1;

            do {
                if (roundRobin >= activeConnections) {
                    roundRobin = 0;
                }
                synchronized (wrappers) {
                    ConnectionWrapper connection = wrappers[roundRobin];
                    if (connection.isFree()) {
                        lastRoundRobinIndex = roundRobin+1;
                        connection.lock();
                        result = connection.getConnection();
                        return result;
                    }
                    loop++;
                    roundRobin++;
                }
            } while ((!gotOne) && (loop <= activeConnections));


            if (!gotOne) {
                synchronized (this) { 
                    // Add a new connections to the pool
                    if (activeConnections < maximumConnections) {
                        try {
                            createWrapper();
                        } catch (SQLException e) {
                            throw new NSForwardException(e, "Error: Unable to create new connection");
                        }
                    }
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                log.warn("Connections Exhausted! Will wait and try again in loop " + tries);
            }
        }
        throw new IllegalStateException("No new connections found");
    }

    /**
     * Returns the local JDBC ID for a connection.
     */
    private ConnectionWrapper wrapperForConnection(Connection conn) {
        synchronized (wrappers) {
            for (int i = 0; i < activeConnections; i++) {
                ConnectionWrapper connection = wrappers[i];
                if (connection.getConnection() == conn) {
                    return connection;
                }
            }
            return null;
        }
    }

    /**
     * Frees a connection. Replaces connection back into the main pool for
     * reuse.
     */
    public void freeConnection(Connection conn) {
        ConnectionWrapper wrapper = wrapperForConnection(conn);
        if (wrapper != null) {
            wrapper.unlock();
        } else {
            log.error("Could not free connection: " + conn);
        }
    }

    private Connection createConnection() throws SQLException {
        try {
            Class.forName(dbDriver);
            Connection conn = DriverManager.getConnection(dbServer, dbLogin, dbPassword);
            return conn;
        } catch (ClassNotFoundException e2) {
            throw NSForwardException._runtimeExceptionForThrowable(e2);
        }
    }

    private synchronized void createWrapper() throws SQLException {
        if(wrappers[activeConnections] == null) {
            if(activeConnections < maximumConnections) {
                wrappers[activeConnections] = new ConnectionWrapper(this);
                activeConnections++;
            } else {
                throw new IllegalStateException("Trying to get more wrappers than available: " + activeConnections + " vs " + maximumConnections);
            }
        }
    }

    private void destroy(int millis) throws SQLException {

        active = false;

        pinger.interrupt();

        // Wait until the housekeeping thread has died.
        try {
            pinger.join(millis);
        } catch (InterruptedException e) {
        }
       
        // Shut down the background housekeeping thread
        reaper.interrupt();

        // Wait until the housekeeping thread has died.
        try {
            reaper.join(millis);
        } catch (InterruptedException e) {
        }

        // The housekeeping thread could still be running
        // (e.g. if millis is too small). This case is ignored.
        // At worst, this method will throw an exception with the
        // clear indication that the timeout was too short.

        long startTime = System.currentTimeMillis();

        // Wait for freeConnection() to return any connections
        // that are still used at this time.
        int openChannelCount;
        long elapsed = System.currentTimeMillis() - startTime;
        while ((openChannelCount = getOpenChannelCount()) > 0 
                && elapsed <= millis) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            elapsed = System.currentTimeMillis() - startTime;
        }

        // Close all connections, whether safe or not
        for (int i = 0; i < activeConnections; i++) {
            wrappers[i].close();
        }

        if (openChannelCount > 0) {
            //bt-test successful
            String msg = "Unsafe shutdown: Had to close " + openChannelCount + " active DB connections after " + millis + "ms";
            // Throwing following Exception is essential because servlet
            // authors are likely to have their own error logging requirements.
            throw new SQLException(msg);
        }
    }

    private int getOpenChannelCount() {
        int useCount = 0;
        synchronized (wrappers) {
            for (int i = 0; i < activeConnections; i++) {
                if (!wrappers[i].isFree()) { 
                    useCount++;
                }
            }
        }
        return useCount;
    }

    public boolean supportsTransaction() {
        return supportsTransactions;
    }

    private static class ConnectionWrapper {
        private String pingStatement = ERXProperties.stringForKeyWithDefault("er.extensions.ERXJDBCConnectionBroker.connectionPingSQL", "SELECT 1+1;");
    
        private static final int FREE = 0;
    
        private static final int BUSY = 1;
    
        private static final int OFFLINE = 2;
    
        private Connection connection;
    
        private ERXJDBCConnectionBroker broker;
    
        private int status;
    
        private long lockTime;
    
        private long creationDate;
    
        public ConnectionWrapper(ERXJDBCConnectionBroker broker) throws SQLException {
            this.broker = broker;
            connection = broker.createConnection();
            status = FREE;
            lockTime = 0;
        }
    
        @Override
        public String toString() {
            return getClass().getName()  +
            ": connection = " + connection +
            ": status = " + status +
            ": lockTime = " + lockTime +
            ": creationDate = " + creationDate;
        }

        public Connection getConnection() {
            return connection;
        }

        public void setConnection(Connection connection) {
            creationDate = (new Date()).getTime();
            this.connection = connection;
        }
    
        public long getCreationDate() {
            return creationDate;
        }
    
        public void setCreationDate(long creationDate) {
            this.creationDate = creationDate;
        }
    
        public long getLockTime() {
            return lockTime;
        }
    
        public void setLockTime(long lockTime) {
            this.lockTime = lockTime;
        }
    
        public int getStatus() {
            return status;
        }
    
        public void setStatus(int status) {
            this.status = status;
        }
    
        public boolean isFree() {
            if(getStatus() == FREE) {
                return true;
            }
            return false;
        }

        private void close() {
            try {
                if(getConnection() != null) {
                    getConnection().close();
                    setConnection(null);
                    setStatus(OFFLINE);
                }
            } catch (SQLException ex) {
                log.warn("Cannot close connection: " + this,  ex);
            }
        }
    
        public void unlock() {
            if(getStatus() != BUSY) {
                throw new IllegalStateException("Attempt to unlock non-busy channel: " + this);
            }
            setStatus(FREE);
            setLockTime(0L);
            try {
                if (getConnection().isReadOnly()) {
                    getConnection().setReadOnly(true);
                }
                // AK: PG MUST and other probably should have set the autocommit to true on putting back in pool
                if(!getConnection().getAutoCommit()) {
                	getConnection().setAutoCommit(true);
                }
			}
			catch (SQLException e) {
				log.error(e, e);
			}
       }
    
        public void lock() {
            if(!isFree()) {
                throw new IllegalStateException("Attempt to lock busy channel: " + this);
            }
            setStatus(OFFLINE);
            setLockTime(System.currentTimeMillis());
            try {
                if (getConnection().isReadOnly()) {
                    getConnection().setReadOnly(false);
                }
                if(getConnection().getAutoCommit()) {
                	getConnection().setAutoCommit(false);
                }
            } catch (SQLException e) {
                throw new NSForwardException(e, "Could not set read only to false for connection: "+ this);
            } finally {
                setStatus(BUSY);
            }
        }
    
        private void clearWarnings() {
            try {
                SQLWarning warning = getConnection().getWarnings();
                if (warning != null) {
                    log.warn("Warnings on connection " + this + ": " + warning);
                    getConnection().clearWarnings();
                }
            } catch (SQLException e) {
                log.warn("Cannot access Warnings: " + e);
            }
        }
    
        private void ping() {
            if (isFree()) {
                setStatus(OFFLINE);
                Connection c = getConnection();
                log.debug("Pinging connection " + connection);
                try {
                    c.isClosed();
                    c.setReadOnly(false);
                    c.createStatement().executeQuery(pingStatement);
                } catch (SQLException e) {
                    log.error("Could not ping connection " + c + ", reason: " + e.getMessage(), e);
                } finally {
                    try {
                        c.rollback();
                    } catch (SQLException e1) {
                        throw new NSForwardException(e1, "could not rollback connection!");
                    }
                }
                setStatus(FREE);
            }
        }

        private void reap(long maxCheckoutMillis, long maxConnectionAgeMillis) throws SQLException {
            boolean restart = false;
            Connection reapingConnection = getConnection();
            try {
                if(!isFree()) { 
                    // Check the time it's been checked out and recycle
                    long checkoutMillis = System.currentTimeMillis() - getLockTime();
                    log.debug("Connection is in use for " + checkoutMillis + " ms: " + this);
                    if (maxCheckoutMillis != 0) {
                        if (checkoutMillis > maxCheckoutMillis) {
                            restart = true;
                            log.info("Connection " + this + " failed to be returned in time, recycling");
                        }
                    }
                    if(!restart) {
                    	// In normal use and not too old, catch it next time!
                    	return;
                    }
                }
                // Take offline (2 indicates housekeeping lock)
                setStatus(OFFLINE); 
                if(!restart) {
                    long connectionAgeMillis = System.currentTimeMillis() - getCreationDate();
                    if (connectionAgeMillis > maxConnectionAgeMillis) { 
                        // Force a reset at the max conn time
                        restart = true;
                    }
                }
                
                if(!restart) {
                    Statement stmt = null;
                    try {
                        stmt = reapingConnection.createStatement();
                    } catch (SQLException e) {
                        restart = true;
                    } finally {
                        try {
                            if (stmt != null) {
                                stmt.close();
                            }
                        } catch (SQLException e1) {
                            restart = true;
                        }
                    }
                }

                if(!restart) {
                    // Some DBs return an object even if DB is shut down
                    if (reapingConnection.isClosed()) { 
                        restart = true;
                    }
                }

                // Connection has a problem, restart it
            } catch (SQLException e) {
                restart = true;
            }
            if(restart) {
                log.debug("Recycling connection: " + this);

                try {
                    getConnection().close();
                } catch (SQLException e0) {
                    log.error("Can't close connection, might have been closed already.  Trying to recycle anyway");
                }

                setConnection(broker.createConnection());
            }
            setStatus(FREE);
        }
    }
}
