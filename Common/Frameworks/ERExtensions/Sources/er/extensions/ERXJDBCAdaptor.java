package er.extensions;

import java.sql.*;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import com.webobjects.jdbcadaptor.*;

/**
 * Subclass of the JDBC adaptor and accompanying classes that supports 
 * connection pooling and posting of adaptor operation notifications. 
 * Will get patched into the runtime via the usual class name magic if the property 
 * <code>er.extensions.ERXJDBCAdaptor.className</code> is set to this class's name or 
 * another subclass of JDBCAdaptor. The connection pooling will be enabled if the 
 * system property <code>er.extensions.ERXJDBCAdaptor.useConnectionBroker</code> is set.
 * @author ak
 *
 */
public class ERXJDBCAdaptor extends JDBCAdaptor {
    
    public static final Logger log = Logger.getLogger(ERXJDBCAdaptor.class);
    
    public static final String USE_CONNECTION_BROKER_KEY = "er.extensions.ERXJDBCAdaptor.useConnectionBroker";
    
    public static final String CLASS_NAME_KEY = "er.extensions.ERXJDBCAdaptor.className";
    
    private static Boolean switchReadWrite = null;
    
    private static boolean switchReadWrite() {
        if (switchReadWrite == null) {
            switchReadWrite = "false".equals(ERXSystem.getProperty("er.extensions.ERXJDBCAdaptor.switchReadWrite", "false")) ? Boolean.FALSE : Boolean.TRUE;
        }
        return switchReadWrite.booleanValue();
    }

    public static void registerJDBCAdaptor() {
        String className = ERXProperties.stringForKey(CLASS_NAME_KEY);
        if(className != null) {
            Class c = ERXPatcher.classForName(className);
            if(c == null) {
                throw new IllegalStateException("Can't find class: " + className);
            }
            ERXPatcher.setClassForName(c, JDBCAdaptor.class.getName());
        }
    }

    /**
     * Channel subclass to support notification posting.
     * @author ak
     *
     */
    public static class Channel extends JDBCChannel {

        public Channel(JDBCContext jdbccontext) {
            super(jdbccontext);
        }

        private boolean setReadOnly(boolean mode) {
            boolean old = false;
            if(switchReadWrite()) {
                try {
                    Connection connection = ((JDBCContext)adaptorContext()).connection();
                    if (connection != null) {
                        old = connection.isReadOnly();
                        connection.setReadOnly(mode);
                    } else {
                        throw new EOGeneralAdaptorException("Can't switch connection mode to " + mode + ", the connection is null");
                    }
                } catch (java.sql.SQLException e) {
                    throw new EOGeneralAdaptorException("Can't switch connection mode to " + mode, new NSDictionary(e, "originalException"));
                } 
            }
            return old;
        }

        /**
         * Overridden to switch the connection to read-only while selecting.
         */
        public void selectAttributes(NSArray array, EOFetchSpecification fetchspecification, boolean lock, EOEntity entity) {
            boolean mode = setReadOnly(!lock);
            super.selectAttributes(array, fetchspecification, lock, entity);
            setReadOnly(mode);
        }

        /**
         * Overridden to post a notification when the operations were performed.
         */
        public void performAdaptorOperations(NSArray ops) {
            super.performAdaptorOperations(ops);
            ERXAdaptorOperationWrapper.adaptorOperationsDidPerform(ops);
        }

    }

    /**
     * Context subclass that uses connection pooling.
     * 
     * @author ak
     */
    public static class Context extends JDBCContext {

        private boolean _useConnectionBroker;
        
        public Context(EOAdaptor eoadaptor) {
            super(eoadaptor);
            _useConnectionBroker = ERXProperties.booleanForKeyWithDefault(USE_CONNECTION_BROKER_KEY, false);
        }
        
        private boolean useConnectionBroker() {
            return _useConnectionBroker;
        }

        private void freeConnection() {
            if(useConnectionBroker()) {
                if(_jdbcConnection != null) {
                    ((ERXJDBCAdaptor)adaptor()).freeConnection(_jdbcConnection);
                    _jdbcConnection = null;
                }
            }
        }

        private void checkoutConnection() {
            if(useConnectionBroker()) {
                if(_jdbcConnection == null) {
                    _jdbcConnection = ((ERXJDBCAdaptor)adaptor()).checkoutConnection();
                }
            }
        }

        public boolean connect() throws JDBCAdaptorException {
            boolean connected = false;
            if(useConnectionBroker()) {
                checkoutConnection();
                connected = _jdbcConnection != null;
            } else {
                connected = super.connect();
            }
            return connected;
        }

        protected JDBCChannel createJDBCChannel() {
            return new Channel(this);
        }
        
        protected JDBCChannel _cachedAdaptorChannel() {
            if (_cachedChannel == null) {
                _cachedChannel = createJDBCChannel();
            }
            return _cachedChannel;
        }

        public EOAdaptorChannel createAdaptorChannel() {
            if (_cachedChannel != null) {
                JDBCChannel jdbcchannel = _cachedChannel;
                _cachedChannel = null;
                return jdbcchannel;
            } else {
                return createJDBCChannel();
            }
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

    public Context createJDBCContext() {
        return new Context(this);
    }

    public EOAdaptorContext createAdaptorContext() {
        return createJDBCContext();
    }

    protected Connection checkoutConnection() {
        Connection c = connectionBroker().getConnection();
        return c;
    }

    private ERXJDBCConnectionBroker connectionBroker() {
        return ERXJDBCConnectionBroker.connectionBrokerForAdaptor(this);
    }

    protected void freeConnection(Connection connection) {
        connectionBroker().freeConnection(connection);
    }

    private boolean supportsTransactions() {
        return connectionBroker().supportsTransaction();
    }
}