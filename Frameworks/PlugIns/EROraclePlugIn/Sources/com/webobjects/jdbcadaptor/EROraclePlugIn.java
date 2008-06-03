package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;


/** Overrides OraclePlugIn in order to provide the modified
 * EROracleExpression class to EOF.
 * 
 * @author David Teran
 *
 */
public class EROraclePlugIn extends OraclePlugIn {

    public EROraclePlugIn(JDBCAdaptor jdbcadaptor) {
        super(jdbcadaptor);
        System.out.println("loading EROracle PlugIn");
    }

    
    /* (non-Javadoc)
     * @see com.webobjects.jdbcadaptor.JDBCPlugIn#defaultExpressionClass()
     */
    public Class defaultExpressionClass() {
        return EROracleExpression.class;
    }

    /* (non-Javadoc)
     * @see com.webobjects.jdbcadaptor.JDBCPlugIn#createSynchronizationFactory()
     */
    public EOSynchronizationFactory createSynchronizationFactory() {
        return super.createSynchronizationFactory();
//        try {
//            return new EROracleSynchronizationFactory(adaptor());
//        } catch ( Exception e ) {
//            throw new NSForwardException(e, "Couldn't create synchronization factory");
//        }
    }
}
