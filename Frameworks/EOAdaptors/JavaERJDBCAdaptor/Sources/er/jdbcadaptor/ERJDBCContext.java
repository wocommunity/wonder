/*
 * Created on Feb 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package er.jdbcadaptor;

import com.webobjects.jdbcadaptor.JDBCContext;

import er.extensions.logging.ERXLogger;

/**
 * @author david
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ERJDBCContext extends
        JDBCContext {

    public static ERXLogger log = ERXLogger.getERXLogger(ERJDBCContext.class);

    /**
     * @param adaptor
     */
    public ERJDBCContext(ERJDBCAdaptor adaptor) {
        super(adaptor);
        _connectionSupportTransaction = adaptor.supportsTransactions();
    }
    /**
     *  
     */
    public void checkoutConnection() {
        if (_jdbcConnection == null) {
            ERJDBCAdaptor adaptor = (ERJDBCAdaptor) adaptor();
            _jdbcConnection = adaptor.checkoutConnection();
        }
    }

    /**
     *  
     */
    public void freeConnection() {
        ERJDBCAdaptor adaptor = (ERJDBCAdaptor) adaptor();
        adaptor.freeConnection(_jdbcConnection);
        _jdbcConnection = null;
    }

}