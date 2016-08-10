package er.jdbcadaptor;

import java.sql.Connection;

import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.jdbcadaptor.JDBCAdaptor;

import er.extensions.jdbc.ERXJDBCConnectionBroker;


/**
 * @author david
 */
public class ERJDBCAdaptor extends JDBCAdaptor {
    
    public ERJDBCAdaptor(String arg0) {
        super(arg0);
    }

    @Override
    public EOAdaptorContext createAdaptorContext() {
        return new ERJDBCContext(this);
    }

    public Connection checkoutConnection() {
        Connection c = ERXJDBCConnectionBroker.connectionBrokerForAdaptor(this).getConnection();

        return c;
    }

    public void freeConnection(Connection connection) {
        ERXJDBCConnectionBroker.connectionBrokerForAdaptor(this).freeConnection(connection);
    }

    public boolean supportsTransactions() {
        return ERXJDBCConnectionBroker.connectionBrokerForAdaptor(this).supportsTransaction();
    }
}
