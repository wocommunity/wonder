package er.jdbcadaptor;

import com.webobjects.jdbcadaptor.JDBCAdaptorException;
import com.webobjects.jdbcadaptor.JDBCContext;

import er.extensions.foundation.ERXProperties;
import er.extensions.jdbc.ERXJDBCAdaptor;

/**
 * @author david
 */
public class ERJDBCContext extends JDBCContext {

    public ERJDBCContext(ERJDBCAdaptor adaptor) {
        super(adaptor);
        _connectionSupportTransaction = adaptor.supportsTransactions();
    }

	/**
	 * In servlet context, when not using JNDI to obtain the database channel, you will get annoying error messages like
	 * <em>javax.naming.NameNotFoundException: Name "comp/env/jdbc" not found in context</em>.
	 * 
	 * Set the property <code>er.extensions.ERXJDBCAdaptor.ignoreJNDIConfiguration</code> to true  in order to suppress 
	 * this messages.
	 * 
	 * @throws JDBCAdaptorException
	 */
	@Override
	public void setupJndiConfiguration() throws JDBCAdaptorException {
		if(!ERXProperties.booleanForKeyWithDefault(ERXJDBCAdaptor.Context.IGNORE_JNDI_CONFIGURATION_KEY, false)) {
			super.setupJndiConfiguration();
		}
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