package er.extensions;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.javaexchange.dbConnectionBroker.*;
import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;

/**
 * @author david@cluster9.com
 * 
 * supports JDBC connection pooling for separated usage, e.g. these java.sql.Connections are not
 * used by EOF. Useful for SQL statements without using / blocking EOF.
 * 
 * For each @see com.webobjects.eoaccess.EOModel one pool is generated in order
 * to support multiple databases / connections. The pools are lazy initialized 
 * although the @see er.extensions.ERXConfigurationManager adds the following 
 * entries to each EOModel's connectionDictionary:
 * 
 * <code>
 * <b> see ERXExtensions's Properties file
 * minConnections, based on System properties: eoModel.name() + ".DBMinConnecions" or dbMinConnectionsGLOBAL
 * maxConnections dto.
 * logPath dto.
 * connectionRecycle dto.
 * maxCheckout dto.
 * debugLevel dto.
 * 
 * username, based on System properties: eoModel.name() + ".DBUser" or dbConnectUserGLOBAL
 * password dto.
 * URL dto.
 * driver dto.
 * </code>
 * 
 * usage:
 * check out a connection:
 * <code>
 * java.sql.Connection con = DbConnectionBroker.connectionBrokerForModel(myModel).getConnection();
 * try {
 *     java.sql.Statement s = con.createStatement();
 *         //now do something with the Statement
 * } finally {
 *     DbConnectionBroker.connectionBrokerForModel(myModel).freeConnection(con);
 * }
 * 
 */

public class ERXJDBCConnectionBroker {

    public static final ERXLogger log     = ERXLogger.getERXLogger(ERXJDBCConnectionBroker.class);

    private static Hashtable      brokers = new Hashtable();

    public static DbConnectionBroker connectionBrokerForModelWithName(String modelName) {
        return connectionBrokerForModel(EOModelGroup.defaultGroup().modelNamed(modelName));
    }

    /**
     * @param model
     * @return
     */
    public static DbConnectionBroker connectionBrokerForModel(EOModel model) {
        DbConnectionBroker dbBroker = (DbConnectionBroker) brokers.get(model);
        if (dbBroker == null) {
            synchronized (brokers) {
                NSDictionary d = model.connectionDictionary();
                String url = (String) d.objectForKey("URL");
                String driver = (String) d.objectForKey("driver");
                String username = (String) d.objectForKey("username");
                String password = (String) d.objectForKey("password");
                int minConnections = Integer.parseInt((String) d.objectForKey("minConnections"));
                int maxConnections = Integer.parseInt((String) d.objectForKey("maxConnections"));
                String logPath = (String) d.objectForKey("logPath");
                double connectionRecycle = Double.parseDouble((String) d.objectForKey("connectionRecycle"));
                int maxCheckout = Integer.parseInt((String) d.objectForKey("maxCheckout"));
                int debugLevel = Integer.parseInt((String) d.objectForKey("debugLevel"));

                if (log.isDebugEnabled()) {
                    log.debug("driver=" + driver);
                    log.debug("url=" + url);
                    log.debug("username=" + username);
                    log.debug("password=" + password);
                    log.debug("minConnections=" + minConnections);
                    log.debug("maxConnections=" + maxConnections);
                    log.debug("logPath=" + logPath);
                    log.debug("connectionRecycle=" + connectionRecycle);
                    log.debug("maxCheckout=" + maxCheckout);
                    log.debug("debugLevel=" + debugLevel);
                }
                new File(logPath).getParentFile().mkdirs();

                try {
                    dbBroker = new DbConnectionBroker(driver, url, username, password, minConnections, maxConnections,
                            logPath, connectionRecycle, true, maxCheckout, debugLevel);
                    brokers.put(model, dbBroker);
                } catch (IOException e) {
                    log.error("could not initiate a DbConnectionBroker");
                    log.error("driver=" + driver);
                    log.error("url=" + url);
                    log.error("username=" + username);
                    log.error("password=" + password);
                    log.error("minConnections=" + minConnections);
                    log.error("maxConnections=" + maxConnections);
                    log.error("logPath=" + logPath);
                    log.error("connectionRecycle=" + connectionRecycle);
                    log.error("maxCheckout=" + maxCheckout);
                    log.error("debugLevel=" + debugLevel);
                    log.error(e);
                } finally {
                }
            }
        }
        return dbBroker;
    }

    /**
     * @param ename
     * @return
     */
    public static DbConnectionBroker connectionBrokerForEntityNamed(String ename) {
        return connectionBrokerForModel(EOModelGroup.defaultGroup().entityNamed(ename).model());
    }
}