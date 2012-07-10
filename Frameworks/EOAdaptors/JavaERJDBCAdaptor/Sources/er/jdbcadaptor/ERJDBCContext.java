package er.jdbcadaptor;

import com.webobjects.jdbcadaptor.JDBCContext;

/**
 * @author david
 */
public class ERJDBCContext extends JDBCContext {

    public ERJDBCContext(ERJDBCAdaptor adaptor) {
        super(adaptor);
        _connectionSupportTransaction = adaptor.supportsTransactions();
    }

    public void checkoutConnection() {
        if (_jdbcConnection == null) {
            ERJDBCAdaptor adaptor = (ERJDBCAdaptor) adaptor();
            _jdbcConnection = adaptor.checkoutConnection();
        }
    }

    public void freeConnection() {
        ERJDBCAdaptor adaptor = (ERJDBCAdaptor) adaptor();
        adaptor.freeConnection(_jdbcConnection);
        _jdbcConnection = null;
    }
}