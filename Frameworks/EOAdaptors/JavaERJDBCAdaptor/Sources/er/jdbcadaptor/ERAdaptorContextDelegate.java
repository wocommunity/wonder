package er.jdbcadaptor;

import com.webobjects.eoaccess.EOAdaptorContext;


/**
 * @author david
 */
public class ERAdaptorContextDelegate implements EOAdaptorContext.Delegate {
    public static ERAdaptorContextDelegate _defaultDelegate = new ERAdaptorContextDelegate();
    
    public boolean adaptorContextShouldConnect(EOAdaptorContext context) {
        if (context instanceof ERJDBCContext) {
            ERJDBCContext ctx = (ERJDBCContext)context;
            ctx.checkoutConnection();
        }
        return false;
    }

    public boolean adaptorContextShouldBegin(EOAdaptorContext context) {
        if (context instanceof ERJDBCContext) {
            ERJDBCContext ctx = (ERJDBCContext)context;
            ctx.checkoutConnection();
        }
        return true;
    }


    public void adaptorContextDidCommit(EOAdaptorContext context) {
        if (context instanceof ERJDBCContext) {
            ERJDBCContext ctx = (ERJDBCContext)context;
            ctx.freeConnection();
        }
    }

    public void adaptorContextDidRollback(EOAdaptorContext context) {
        if (context instanceof ERJDBCContext) {
            ERJDBCContext ctx = (ERJDBCContext)context;
            ctx.freeConnection();
        }
    }

    public void adaptorContextDidBegin(EOAdaptorContext arg0) {
    }

    public boolean adaptorContextShouldCommit(EOAdaptorContext arg0) {
        return true;
    }

    public boolean adaptorContextShouldRollback(EOAdaptorContext arg0) {
        return true;
    }

    /** Returns the singleton of the adaptor context delegate */
    public static ERAdaptorContextDelegate defaultDelegate() {
        return _defaultDelegate;
    }
}
