/*
 * Created on Feb 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package er.jdbcadaptor;
import java.sql.*;

import com.webobjects.eoaccess.*;
import com.webobjects.jdbcadaptor.*;

import er.extensions.*;


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
     * @return
     */
    public Connection checkoutConnection() {
        Connection c = ERXJDBCConnectionBroker.connectionBrokerForAdaptor(this).getConnection();
        try {
            log.info("autoCommit = "+c.getAutoCommit());
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return c;
    }


    /**
     * @param connection
     */
    public void freeConnection(Connection connection) {
        ERXJDBCConnectionBroker.connectionBrokerForAdaptor(this).freeConnection(connection);
    }

    /**
     * @return
     */
    public boolean supportsTransactions() {
        return ERXJDBCConnectionBroker.connectionBrokerForAdaptor(this).supportsTransaction();
    }
}
