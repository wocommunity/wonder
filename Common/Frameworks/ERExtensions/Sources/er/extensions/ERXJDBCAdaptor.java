/**
 * 
 */
package er.extensions;

import java.sql.*;

import com.webobjects.eoaccess.*;
import com.webobjects.jdbcadaptor.*;

/**
 * Subclass of the JDBC adaptor that supports connection pooling. Will get patched into
 * the runtime via the usual class name magic if the property 
 * <code>er.extensions.ERXJDBCAdaptor.className</code> is set.
 * @author ak
 *
 */
public class ERXJDBCAdaptor extends JDBCAdaptor {
    
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXJDBCAdaptor.class);

    public class Context extends JDBCContext {

        public Context(EOAdaptor eoadaptor) {
            super(eoadaptor);
        }

        private void freeConnection() {
             if(_jdbcConnection != null) {
                ERXJDBCAdaptor.this.freeConnection(_jdbcConnection);
                _jdbcConnection = null;
            }
        }

        private void checkoutConnection() {
            if(_jdbcConnection == null) {
                _jdbcConnection = ERXJDBCAdaptor.this.checkoutConnection();
            }
        }

        public boolean connect() throws JDBCAdaptorException {
            checkoutConnection();
            return _jdbcConnection != null;
        }

        public EOAdaptorChannel createAdaptorChannel() {
            return super.createAdaptorChannel();
        }

        public void disconnect() throws JDBCAdaptorException {
            freeConnection();
            super.disconnect();
        }

        public void beginTransaction() {
            checkoutConnection();
            super.beginTransaction();
        }

        public void transactionDidCommit() {
            super.transactionDidCommit();
            freeConnection();
        }

        public void transactionDidRollback() {
            super.transactionDidRollback();
            freeConnection();
       }
    }
    
    public ERXJDBCAdaptor(String s) {
        super(s);
    }

    public EOAdaptorContext createAdaptorContext() {
        return new Context(this);
    }

    private Connection checkoutConnection() {
        Connection c = connectionBroker().getConnection();
        return c;
    }

    private ERXJDBCConnectionBroker connectionBroker() {
        return ERXJDBCConnectionBroker.connectionBrokerForAdaptor(this);
    }

    private void freeConnection(Connection connection) {
        connectionBroker().freeConnection(connection);
    }

    private boolean supportsTransactions() {
        return connectionBroker().supportsTransaction();
    }

    public static void registerJDBCAdaptor() {
        String className = ERXProperties.stringForKey("er.extensions.ERXJDBCAdaptor.className");
        if(className != null) {
            Class c = ERXPatcher.classForName(className);
            if(c == null) {
                throw new IllegalStateException("Can't find class: " + className);
            }
            ERXPatcher.setClassForName(c, JDBCAdaptor.class.getName());
        }
    }
}