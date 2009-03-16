/*
 * Created on Feb 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package er.jdbcadaptor;
import java.sql.Connection;

import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.jdbcadaptor.JDBCAdaptor;

import er.extensions.jdbc.ERXJDBCConnectionBroker;
import er.extensions.logging.ERXLogger;


/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ERJDBCAdaptor extends
        JDBCAdaptor {

    public static ERXLogger log = ERXLogger.getERXLogger(ERJDBCAdaptor.class);
    
    /**
     * @param arg0
     */
    public ERJDBCAdaptor(String arg0) {
        super(arg0);
    }

    public EOAdaptorContext createAdaptorContext() {
        return new ERJDBCContext(this);
    }

    /**
     */
    public Connection checkoutConnection() {
        Connection c = ERXJDBCConnectionBroker.connectionBrokerForAdaptor(this).getConnection();

        return c;
    }


    /**
     * @param connection
     */
    public void freeConnection(Connection connection) {
        ERXJDBCConnectionBroker.connectionBrokerForAdaptor(this).freeConnection(connection);
    }

    /**
     */
    public boolean supportsTransactions() {
        return ERXJDBCConnectionBroker.connectionBrokerForAdaptor(this).supportsTransaction();
    }
}
