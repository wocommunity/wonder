package er.jdbcadaptor;
/*
 * Created on Feb 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import com.webobjects.eoaccess.EOAdaptorContext;

import er.extensions.logging.ERXLogger;


/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ERAdaptorContextDelegate implements EOAdaptorContext.Delegate {
    public static ERAdaptorContextDelegate _defaultDelegate = new ERAdaptorContextDelegate();
    public static final ERXLogger log = ERXLogger.getERXLogger(ERAdaptorContextDelegate.class);
    
    /* (non-Javadoc)
     * @see com.webobjects.eoaccess.EOAdaptorContext.Delegate#adaptorContextShouldConnect(com.webobjects.eoaccess.EOAdaptorContext)
     */
    public boolean adaptorContextShouldConnect(EOAdaptorContext context) {
        if (context instanceof ERJDBCContext) {
            ERJDBCContext ctx = (ERJDBCContext)context;
            ctx.checkoutConnection();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.webobjects.eoaccess.EOAdaptorContext.Delegate#adaptorContextShouldBegin(com.webobjects.eoaccess.EOAdaptorContext)
     */
    public boolean adaptorContextShouldBegin(EOAdaptorContext context) {
        if (context instanceof ERJDBCContext) {
            ERJDBCContext ctx = (ERJDBCContext)context;
            ctx.checkoutConnection();
        }
        return true;
    }


    /* (non-Javadoc)
     * @see com.webobjects.eoaccess.EOAdaptorContext.Delegate#adaptorContextDidCommit(com.webobjects.eoaccess.EOAdaptorContext)
     */
    public void adaptorContextDidCommit(EOAdaptorContext context) {
        if (context instanceof ERJDBCContext) {
            ERJDBCContext ctx = (ERJDBCContext)context;
            ctx.freeConnection();
        }
    }

    /* (non-Javadoc)
     * @see com.webobjects.eoaccess.EOAdaptorContext.Delegate#adaptorContextDidRollback(com.webobjects.eoaccess.EOAdaptorContext)
     */
    public void adaptorContextDidRollback(EOAdaptorContext context) {
        if (context instanceof ERJDBCContext) {
            ERJDBCContext ctx = (ERJDBCContext)context;
            ctx.freeConnection();
        }
    }

    /* (non-Javadoc)
     * @see com.webobjects.eoaccess.EOAdaptorContext.Delegate#adaptorContextDidBegin(com.webobjects.eoaccess.EOAdaptorContext)
     */
    public void adaptorContextDidBegin(EOAdaptorContext arg0) {
    }

    /* (non-Javadoc)
     * @see com.webobjects.eoaccess.EOAdaptorContext.Delegate#adaptorContextShouldCommit(com.webobjects.eoaccess.EOAdaptorContext)
     */
    public boolean adaptorContextShouldCommit(EOAdaptorContext arg0) {
        return true;
    }

    /* (non-Javadoc)
     * @see com.webobjects.eoaccess.EOAdaptorContext.Delegate#adaptorContextShouldRollback(com.webobjects.eoaccess.EOAdaptorContext)
     */
    public boolean adaptorContextShouldRollback(EOAdaptorContext arg0) {
        return true;
    }

    /** Returns the singleton of the adaptor context delegate */
    public static ERAdaptorContextDelegate defaultDelegate() {
        return _defaultDelegate;
    }

}
