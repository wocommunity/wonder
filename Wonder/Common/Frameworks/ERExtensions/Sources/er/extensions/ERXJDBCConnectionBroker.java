package er.extensions;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * @author david@cluster9.com <br/>
 * 
 * supports JDBC connection pooling for separated usage, e.g. these
 * java.sql.Connections are not used by EOF. Useful for SQL statements without
 * using / blocking EOF. <br/><br/>For each
 * @see com.webobjects.eoaccess.EOModel one pool is generated in order to
 *      support multiple databases / connections. The pools are lazy initialized
 *      although the
 * @see er.extensions.ERXConfigurationManager adds the following entries to each
 *      EOModel's connectionDictionary: <br/><br/><code><br/>
 * <b> see ERXExtensions's Properties file</b><br/>
 * minConnections, based on System properties: eoModel.name() + ".DBMinConnecions" or dbMinConnectionsGLOBAL<br/>
 * maxConnections dto.<br/>
 * logPath dto.<br/>
 * connectionRecycle dto.<br/>
 * maxCheckout dto.<br/>
 * debugLevel dto.<br/>
 * <br/>
 * username, based on System properties: eoModel.name() + ".DBUser" or dbConnectUserGLOBAL<br/>
 * password dto.<br/>
 * URL dto.<br/>
 * driver dto.<br/>
 * </code>
 *      <br/>usage: <br/>check out a connection: <br/><code>
 * <pre>
 * java.sql.Connection con = ERXJDBCConnectionBroker.connectionBrokerForModel(myModel).getConnection();
 * try {
 *     java.sql.Statement s = con.createStatement();
 *     //now do something with the Statement
 * } finally {
 *     ERXJDBCConnectionBroker.connectionBrokerForModel(myModel).freeConnection(con);
 * }
 * </pre></code>
 */

public class ERXJDBCConnectionBroker {

    public static final ERXLogger log     = ERXLogger.getERXLogger(ERXJDBCConnectionBroker.class);

    private static Hashtable      brokers = new Hashtable();

    private static DbConnectionBroker connectionBrokerForConnectionDictionary(NSDictionary d) {
        String hashKey = "" + d.objectForKey("URL") + d.objectForKey("username");
        DbConnectionBroker dbBroker = (DbConnectionBroker) brokers.get(hashKey);
        if (dbBroker == null) {
            synchronized (brokers) {
                dbBroker = newConnectionBrokerWithConnectionDictionary(d);
                brokers.put(hashKey, dbBroker);
            }
        }
        return dbBroker;
    }

    /**
     * @param modelName
     * @return
     */
    public static DbConnectionBroker connectionBrokerForModelWithName(String modelName) {
        return connectionBrokerForModel(EOModelGroup.defaultGroup().modelNamed(modelName));
    }

    /**
     * @param model
     * @return
     */
    public static DbConnectionBroker connectionBrokerForModel(EOModel model) {
        return connectionBrokerForConnectionDictionary(model.connectionDictionary());
    }

    /**
     * @param model
     * @return
     */
    public static DbConnectionBroker connectionBrokerForAdaptor(EOAdaptor adaptor) {
        return connectionBrokerForConnectionDictionary(adaptor.connectionDictionary());
    }

    /**
     * @param ename
     * @return
     */
    public static DbConnectionBroker connectionBrokerForEntityNamed(String ename) {
        return connectionBrokerForModel(EOModelGroup.defaultGroup().entityNamed(ename).model());
    }

    /**
     * @param dbBroker
     * @param d
     * @return
     */
    private static DbConnectionBroker newConnectionBrokerWithConnectionDictionary(NSDictionary d) {
        DbConnectionBroker dbBroker = null;
        String url = (String) d.objectForKey("URL");
        String driver = (String) d.objectForKey("driver");
        String username = (String) d.objectForKey("username");
        String password = (String) d.objectForKey("password");
        int minConnections = Integer.parseInt((String) d.objectForKey("minConnections"));
        int maxConnections = Integer.parseInt((String) d.objectForKey("maxConnections"));
        double connectionRecycle = Double.parseDouble((String) d.objectForKey("connectionRecycle"));
        int maxCheckout = Integer.parseInt((String) d.objectForKey("maxCheckout"));

        if (log.isDebugEnabled()) {
            log.debug("driver=" + driver);
            log.debug("url=" + url);
            log.debug("username=" + username);
            log.debug("password=" + password);
            log.debug("minConnections=" + minConnections);
            log.debug("maxConnections=" + maxConnections);
            log.debug("connectionRecycle=" + connectionRecycle);
            log.debug("maxCheckout=" + maxCheckout);
        }

        try {
            dbBroker = new DbConnectionBroker(driver, url, username, password, minConnections, maxConnections, connectionRecycle,
                    maxCheckout);
        } catch (IOException e) {
            log.error("could not initiate a DbConnectionBroker");
            log.error("driver=" + driver);
            log.error("url=" + url);
            log.error("username=" + username);
            log.error("password=" + password);
            log.error("minConnections=" + minConnections);
            log.error("maxConnections=" + maxConnections);
            log.error("connectionRecycle=" + connectionRecycle);
            log.error("maxCheckout=" + maxCheckout);
            log.error(e);
            throw new NSForwardException(e);
        } finally {
        }
        return dbBroker;
    }

    /**
     * DbConnectionBroker a broker for sql connections. Original version from
     * Marc A. Mnich, based on version 1.0.13 3/12/02 Creates and manages a pool
     * of database connections.
     * 
     * @author David Teran, david@cluster9.com
     */
    public static class DbConnectionBroker implements Runnable {

        private Thread           runner;
        private Thread           pinger;

        private Connection[]     connPool;
        private int[]            connStatus;

        private long[]           connLockTime, connCreateDate;
        private String[]         connID;
        private String           dbDriver, dbServer, dbLogin, dbPassword;
        private int              currConnections, connLast, minConns, maxConns, maxConnMSec, maxCheckoutSeconds;

        //available: set to false on destroy, checked by getConnection()
        private boolean          available                 = true;

        private Boolean          supportsTransactions      = null;

        private SQLWarning       currSQLWarning;
        private String           pid;

        private final int        DEFAULTMAXCHECKOUTSECONDS = 60;
        private final int        DEFAULTDEBUGLEVEL         = 2;

        private static final int FREE                      = 0;
        private static final int IN_USE                    = 1;
        private static final int OFFLINE                   = 2;

        /**
         * Creates a new Connection Broker <br>
         * dbDriver: JDBC driver. e.g. 'oracle.jdbc.driver.OracleDriver' <br>
         * dbServer: JDBC connect string. e.g.
         * 'jdbc:oracle:thin:@203.92.21.109:1526:orcl' <br>
         * dbLogin: Database login name. e.g. 'Scott' <br>
         * dbPassword: Database password. e.g. 'Tiger' <br>
         * minConns: Minimum number of connections to start with. <br>
         * maxConns: Maximum number of connections in dynamic pool. <br>
         * logFileString: Absolute path name for log file. e.g.
         * 'c:/temp/mylog.log' <br>
         * maxConnTime: Time in days between connection resets. (Reset does a
         * basic cleanup) <br>
         * logAppend: Append to logfile (optional) <br>
         * maxCheckoutSeconds: Max time a connection can be checked out before
         * being recycled. Zero value turns option off, default is 60 seconds.
         * debugLevel: Level of debug messages output to the log file. 0 -> no
         * messages, 1 -> Errors, 2 -> Warnings, 3 -> Information
         */
        public DbConnectionBroker(String dbDriver, String dbServer, String dbLogin, String dbPassword, int minConns, int maxConns,
                double maxConnTime) throws IOException {

            setupBroker(dbDriver, dbServer, dbLogin, dbPassword, minConns, maxConns, maxConnTime, DEFAULTMAXCHECKOUTSECONDS);
        }

        /*
         * Special constructor to handle connection checkout expiration
         */
        public DbConnectionBroker(String dbDriver, String dbServer, String dbLogin, String dbPassword, int minConns, int maxConns,
                double maxConnTime, int maxCheckoutSeconds) throws IOException {

            setupBroker(dbDriver, dbServer, dbLogin, dbPassword, minConns, maxConns, maxConnTime, maxCheckoutSeconds);
        }

        private void setupBroker(String dbDriver, String dbServer, String dbLogin, String dbPassword, int minConns, int maxConns,
                double maxConnTime, int maxCheckoutSeconds) throws IOException {

            connPool = new Connection[maxConns];
            connStatus = new int[maxConns];
            connLockTime = new long[maxConns];
            connCreateDate = new long[maxConns];
            connID = new String[maxConns];
            currConnections = minConns;
            this.maxConns = maxConns;
            this.dbDriver = dbDriver;
            this.dbServer = dbServer;
            this.dbLogin = dbLogin;
            this.dbPassword = dbPassword;
            this.maxCheckoutSeconds = maxCheckoutSeconds;
            maxConnMSec = (int) (maxConnTime * 86400000.0); //86400 sec/day
            if (maxConnMSec < 30000) { // Recycle no less than 30 seconds.
                maxConnMSec = 30000;
            }

            ERXJDBCConnectionBroker.log.info("-----------------------------------------");
            ERXJDBCConnectionBroker.log.info("-----------------------------------------");
            ERXJDBCConnectionBroker.log.info("Starting DbConnectionBroker");
            ERXJDBCConnectionBroker.log.info("dbDriver = " + dbDriver);
            ERXJDBCConnectionBroker.log.info("dbServer = " + dbServer);
            ERXJDBCConnectionBroker.log.info("dbLogin = " + dbLogin);
            ERXJDBCConnectionBroker.log.info("minconnections = " + minConns);
            ERXJDBCConnectionBroker.log.info("maxconnections = " + maxConns);
            ERXJDBCConnectionBroker.log.info("Total refresh interval = " + maxConnTime + " days");
            ERXJDBCConnectionBroker.log.info("maxCheckoutSeconds = " + maxCheckoutSeconds);
            ERXJDBCConnectionBroker.log.info("-----------------------------------------");

            // Initialize the pool of connections with the mininum connections:
            // Problems creating connections may be caused during reboot when
            // the
            //    servlet is started before the database is ready. Handle this
            //    by waiting and trying again. The loop allows 5 minutes for
            //    db reboot.
            boolean connectionsSucceeded = false;
            int dbLoop = 20;

            try {
                for (int i = 1; i < dbLoop; i++) {
                    try {
                        for (int j = 0; j < currConnections; j++) {
                            createConn(j);
                        }
                        connectionsSucceeded = true;
                        break;
                    } catch (SQLException e) {
                        ERXJDBCConnectionBroker.log.error("--->Attempt (" + String.valueOf(i) + " of " + String.valueOf(dbLoop)
                                + ") failed to create new connections set at startup: ");
                        ERXJDBCConnectionBroker.log.error("    " + e);
                        ERXJDBCConnectionBroker.log.error("    Will try again in 15 seconds...");
                        try {
                            Thread.sleep(15000);
                        } catch (InterruptedException e1) {
                        }
                    }
                }
                if (!connectionsSucceeded) { // All attempts at connecting to db
                    // exhausted
                    ERXJDBCConnectionBroker.log.error("\r\nAll attempts at connecting to Database exhausted");
                    throw new IOException();
                }
            } catch (Exception e) {
                throw new IOException();
            }

            // Fire up the background housekeeping thread

            runner = new Thread(this);
            runner.start();

            // Fire up the ping process

            pinger = new Thread(new ConnectionPing());
            pinger.start();

        }//End DbConnectionBroker()

        /**
         * Housekeeping thread. Runs in the background with low CPU overhead.
         * Connections are checked for warnings and closure and are periodically
         * restarted. This thread is a catchall for corrupted connections and
         * prevents the buildup of open cursors. (Open cursors result when the
         * application fails to close a Statement). This method acts as fault
         * tolerance for bad connection/statement programming.
         */
        public void run() {
            boolean forever = true;
            Statement stmt = null;
            String currCatalog = null;
            long maxCheckoutMillis = maxCheckoutSeconds * 1000;

            while (forever) {

                // Get any Warnings on connections and print to event file
                for (int i = 0; i < currConnections; i++) {
                    try {
                        currSQLWarning = connPool[i].getWarnings();
                        if (currSQLWarning != null) {
                            ERXJDBCConnectionBroker.log.warn("Warnings on connection " + String.valueOf(i) + " " + currSQLWarning);
                            connPool[i].clearWarnings();
                        }
                    } catch (SQLException e) {
                        ERXJDBCConnectionBroker.log.warn("Cannot access Warnings: " + e);
                    }

                }

                for (int i = 0; i < currConnections; i++) { // Do for each
                    // connection
                    long age = System.currentTimeMillis() - connCreateDate[i];

                    try { // Test the connection with createStatement call
                        synchronized (connStatus) {
                            if (connStatus[i] > 0) { // In use, catch it next
                                // time!

                                // Check the time it's been checked out and
                                // recycle
                                long timeInUse = System.currentTimeMillis() - connLockTime[i];
                                ERXJDBCConnectionBroker.log.debug("Warning.  Connection " + i + " in use for " + timeInUse + " ms");
                                if (maxCheckoutMillis != 0) {
                                    if (timeInUse > maxCheckoutMillis) {
                                        ERXJDBCConnectionBroker.log.debug("Warning. Connection " + i + " failed to be returned in time.  Recycling...");
                                        throw new SQLException();
                                    }
                                }

                                continue;
                            }
                            connStatus[i] = OFFLINE; // Take offline (2
                            // indicates
                            // housekeeping lock)
                        }

                        if (age > maxConnMSec) { // Force a reset at the max
                            // conn time
                            throw new SQLException();
                        }

                        stmt = connPool[i].createStatement();
                        connStatus[i] = FREE; // Connection is O.K.
                        //log.println("Connection confirmed for conn = " +
                        //             String.valueOf(i));

                        // Some DBs return an object even if DB is shut down
                        if (connPool[i].isClosed()) { throw new SQLException(); }

                        // Connection has a problem, restart it
                    } catch (SQLException e) {

                        ERXJDBCConnectionBroker.log.debug(new Date().toString() + " ***** Recycling connection " + String.valueOf(i) + ":");

                        try {
                            connPool[i].close();
                        } catch (SQLException e0) {
                            ERXJDBCConnectionBroker.log.error("Error!  Can't close connection!  Might have been closed already.  Trying to recycle anyway... ("
                                                + e0 + ")");
                        }

                        try {
                            createConn(i);
                        } catch (SQLException e1) {
                            ERXJDBCConnectionBroker.log.error("Failed to create connection: " + e1);
                            connStatus[i] = FREE; // Can't open, try again next
                            // time
                        }
                    } finally {
                        try {
                            if (stmt != null) {
                                stmt.close();
                            }
                        } catch (SQLException e1) {
                        }
                        ;
                    }

                }

                try {
                    Thread.sleep(20000);
                } // Wait 20 seconds for next cycle

                catch (InterruptedException e) {
                    // Returning from the run method sets the internal
                    // flag referenced by Thread.isAlive() to false.
                    // This is required because we don't use stop() to
                    // shutdown this thread.
                    return;
                }

            }

        } // End run

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

            Connection conn = null;

            if (available) {
                boolean gotOne = false;

                for (int outerloop = 1; outerloop <= 10; outerloop++) {

                    try {
                        int loop = 0;
                        int roundRobin = connLast + 1;
                        if (roundRobin >= currConnections) roundRobin = 0;

                        do {
                            synchronized (connStatus) {
                                if ((connStatus[roundRobin] == FREE) && (!connPool[roundRobin].isClosed())) {
                                    conn = connPool[roundRobin];
                                    connStatus[roundRobin] = IN_USE;
                                    connLockTime[roundRobin] = System.currentTimeMillis();
                                    connLast = roundRobin;
                                    gotOne = true;
                                    break;
                                } else {
                                    loop++;
                                    roundRobin++;
                                    if (roundRobin >= currConnections) roundRobin = 0;
                                }
                            }
                        } while ((gotOne == false) && (loop < currConnections));

                    } catch (SQLException e1) {
                        ERXJDBCConnectionBroker.log.error("Error: " + e1);
                    }

                    if (gotOne) {
                        break;
                    } else {
                        synchronized (this) { // Add new connections to the pool
                            if (currConnections < maxConns) {

                                try {
                                    createConn(currConnections);
                                    currConnections++;
                                } catch (SQLException e) {
                                    ERXJDBCConnectionBroker.log.error("Error: Unable to create new connection: " + e);
                                    
                                }
                            }
                        }

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }
                        ERXJDBCConnectionBroker.log.error("-----> Connections Exhausted!  Will wait and try again in loop " + String.valueOf(outerloop));
                        
                    }

                } // End of try 10 times loop

            } else {
                ERXJDBCConnectionBroker.log.warn("Unsuccessful getConnection() request during destroy()");
            } // End if(available)
            if (ERXJDBCConnectionBroker.log.isDebugEnabled()) {
            ERXJDBCConnectionBroker.log.debug("Handing out connection " + idOfConnection(conn) + " --> "
                        + (new SimpleDateFormat("MM/dd/yyyy  hh:mm:ss a")).format(new java.util.Date()));
            }
            try {
                if (conn.isReadOnly()) conn.setReadOnly(false);
            } catch (SQLException e) {
                log.error("could not set read only to false for connection "+conn, e);
            }
            return conn;

        }

        /**
         * Returns the local JDBC ID for a connection.
         */
        public int idOfConnection(Connection conn) {
            int match;
            String tag;

            try {
                tag = conn.toString();
            } catch (NullPointerException e1) {
                tag = "none";
            }

            match = -1;

            for (int i = 0; i < currConnections; i++) {
                if (connID[i].equals(tag)) {
                    match = i;
                    break;
                }
            }
            return match;
        }

        /**
         * Frees a connection. Replaces connection back into the main pool for
         * reuse.
         */
        public String freeConnection(Connection conn) {
            String res = "";

            int thisconn = idOfConnection(conn);
            if (thisconn >= 0) {
                connStatus[thisconn] = FREE;
                res = "freed " + conn.toString();
                //log.println("Freed connection " + String.valueOf(thisconn) +
                //            " normal exit: ");
            } else {
                ERXJDBCConnectionBroker.log.error("----> Error: Could not free connection!!!");
            }

            return res;

        }

        /**
         * Returns the age of a connection -- the time since it was handed out
         * to an application.
         */
        public long getAge(Connection conn) { // Returns the age of the
            // connection in millisec.
            int thisconn = idOfConnection(conn);
            return System.currentTimeMillis() - connLockTime[thisconn];
        }

        private void createConn(int i)

        throws SQLException {

            Date now = new Date();

            try {
                Class.forName(dbDriver);

                connPool[i] = DriverManager.getConnection(dbServer, dbLogin, dbPassword);

                connStatus[i] = FREE;
                connID[i] = connPool[i].toString();
                connLockTime[i] = 0;
                connCreateDate[i] = now.getTime();

            } catch (ClassNotFoundException e2) {
                ERXJDBCConnectionBroker.log.error("Error creating connection: " + e2);
            }

            ERXJDBCConnectionBroker.log.error(now.toString() + "  Opening connection " + String.valueOf(i) + " " + connPool[i].toString() + ":");
        }

        /**
         * Shuts down the housekeeping thread and closes all connections in the
         * pool. Call this method from the destroy() method of the servlet.
         */

        /**
         * Multi-phase shutdown. having following sequence:
         * <OL>
         * <LI><code>getConnection()</code> will refuse to return
         * connections.
         * <LI>The housekeeping thread is shut down. <br>
         * Up to the time of <code>millis</code> milliseconds after shutdown
         * of the housekeeping thread, <code>freeConnection()</code> can still
         * be called to return used connections.
         * <LI>After <code>millis</code> milliseconds after the shutdown of
         * the housekeeping thread, all connections in the pool are closed.
         * <LI>If any connections were in use while being closed then a
         * <code>SQLException</code> is thrown.
         * <LI>The log is closed.
         * </OL>
         * <br>
         * Call this method from a servlet destroy() method.
         * 
         * @param millis
         *            the time to wait in milliseconds.
         * @exception SQLException
         *                if connections were in use after <code>millis</code>.
         */
        public void destroy(int millis) throws SQLException {

            // Checking for invalid negative arguments is not necessary,
            // Thread.join() does this already in runner.join().

            // Stop issuing connections
            available = false;

            // Shut down the background housekeeping thread
            runner.interrupt();

            // Wait until the housekeeping thread has died.
            try {
                runner.join(millis);
            } catch (InterruptedException e) {
            } // ignore

            // The housekeeping thread could still be running
            // (e.g. if millis is too small). This case is ignored.
            // At worst, this method will throw an exception with the
            // clear indication that the timeout was too short.

            long startTime = System.currentTimeMillis();

            // Wait for freeConnection() to return any connections
            // that are still used at this time.
            int useCount;
            while ((useCount = getUseCount()) > 0 && System.currentTimeMillis() - startTime <= millis) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                } // ignore
            }

            // Close all connections, whether safe or not
            for (int i = 0; i < currConnections; i++) {
                try {
                    connPool[i].close();
                } catch (SQLException e1) {
                    ERXJDBCConnectionBroker.log.warn("Cannot close connections on Destroy");
                }
            }

            if (useCount > 0) {
                //bt-test successful
                String msg = "Unsafe shutdown: Had to close " + useCount + " active DB connections after " + millis + "ms";
                ERXJDBCConnectionBroker.log.info(msg);
                // Throwing following Exception is essential because servlet
                // authors
                // are likely to have their own error logging requirements.
                throw new SQLException(msg);
            }

        }//End destroy()

        /**
         * Less safe shutdown. Uses default timeout value. This method simply
         * calls the <code>destroy()</code> method with a <code>millis</code>
         * value of 10000 (10 seconds) and ignores <code>SQLException</code>
         * thrown by that method.
         * 
         * @see #destroy(int)
         */
        public void destroy() {
            try {
                destroy(10000);
            } catch (SQLException e) {
            }
        }

        /**
         * Returns the number of connections in use.
         */
        // This method could be reduced to return a counter that is
        // maintained by all methods that update connStatus.
        // However, it is more efficient to do it this way because:
        // Updating the counter would put an additional burden on the most
        // frequently used methods; in comparison, this method is
        // rarely used (although essential).
        public int getUseCount() {
            int useCount = 0;
            synchronized (connStatus) {
                for (int i = 0; i < currConnections; i++) {
                    if (connStatus[i] > FREE) { // In use
                        useCount++;
                    }
                }
            }
            return useCount;
        }//End getUseCount()

        /**
         * Returns the number of connections in the dynamic pool.
         */
        public int getSize() {
            return currConnections;
        }//End getSize()

        /**
         *  
         */
        public boolean supportsTransaction() {
            if (supportsTransactions == null) {
                try {
                    Connection con = getConnection();

                    supportsTransactions = (con.getTransactionIsolation() != 0) ? Boolean.TRUE : Boolean.FALSE;

                    if (supportsTransactions.booleanValue()) {
                        con.setAutoCommit(false);
                    }

                    freeConnection(con);

                } catch (Exception sqle) {
                    throw new RuntimeException("Failed to create connection pool", sqle);
                }
            }
            return supportsTransactions.booleanValue();
        }

        public class ConnectionPing implements Runnable {

            /*
             * (non-Javadoc)
             * 
             * @see java.lang.Runnable#run()
             */
            public void run() {
                ERXJDBCConnectionBroker.log.debug("starting up ConnectionPing");
                while (true) {
                    boolean b = ERXProperties
                            .booleanForKeyWithDefault("er.extensions.ERXJDBCConnectionBroker.connectionPingEnabled", false);
                    if (!b) {
                        try {
                            Thread.sleep(1000 * 60 * 1); // wait a minute
                        } catch (InterruptedException e) {
                        }
                    } else {
                        int wait = ERXProperties.intForKeyWithDefault("er.extensions.ERXJDBCConnectionBroker.connectionPingInterval",
                                60 * 5);
                        String sql = ERXProperties.stringForKeyWithDefault("er.extensions.ERXJDBCConnectionBroker.connectionPingSQL",
                                "SELECT 1+1;");
                        synchronized (connPool) {
                            for (int i = 0; i < connPool.length; i++) {
                                if (connStatus[i] == FREE && connPool[i] != null) {
                                    synchronized (connStatus) {
                                        Connection c = connPool[i];
                                        connStatus[i] = IN_USE;
                                        ERXJDBCConnectionBroker.log.debug("pinging connection " + connStatus[i]);
                                        try {
                                            c.setAutoCommit(false);
                                            c.setReadOnly(false);
                                            ResultSet rs = c.createStatement().executeQuery(sql);
                                        } catch (Exception e) {
                                            if (ERXJDBCConnectionBroker.log.isDebugEnabled()) {
                                                ERXJDBCConnectionBroker.log.error("could not ping connection " + c + ", reason: "
                                                        + e.getMessage(), e);
                                            } else {
                                                ERXJDBCConnectionBroker.log.error("could not ping connection " + c + ", reason: "
                                                        + e.getMessage());
                                            }
                                        } finally {
                                            try {
                                                c.rollback();
                                            } catch (SQLException e1) {
                                                throw new NSForwardException(e1, "could not rollback connection!");
                                            }
                                            connStatus[i] = FREE;
                                        }
                                    }
                                }
                            }
                        }
                        try {
                            Thread.sleep(wait * 1000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }

    }// End class

    /** returns a DbConnectionBroker which belongs to the adaptor who is responsible to save
     * the EOEnterpriseObject to the database.
     * 
     * @param eo, the EOEnterpriseObject which gives information which adaptor should be used
     * @return a DbConnectionBroker which uses the same connection url as the adaptor who is
     * responsible for the eo or null, if no adaptor can be found for the eo.
     */
    public static DbConnectionBroker connectionBrokerForEoInEditingContext(EOEnterpriseObject eo) {
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
        return null;
    }
}