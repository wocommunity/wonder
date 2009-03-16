package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.jdbcadaptor.OraclePlugIn.OracleSynchronizationFactory;

/** Overrides OracleSynchronizationFactory. This class does not add any
 * additional implementation, its just there to be consistent with the
 * other EOF PlugIns
 * 
 * @author David Teran
 *
 */
    public class EROracleSynchronizationFactory extends OracleSynchronizationFactory {

    public EROracleSynchronizationFactory(EOAdaptor eoadaptor) {
        super(eoadaptor);
    }
}
