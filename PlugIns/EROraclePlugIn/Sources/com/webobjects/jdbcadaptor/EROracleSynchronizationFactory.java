package com.webobjects.jdbcadaptor;

import com.webobjects.jdbcadaptor.OraclePlugIn.*;
import com.webobjects.eoaccess.*;

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
