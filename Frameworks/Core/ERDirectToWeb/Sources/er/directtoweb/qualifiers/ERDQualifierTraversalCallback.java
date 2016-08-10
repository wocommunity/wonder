//
// ERDQualifierTraversalCallback.java
// Project ERDirectToWeb
//
// Created by max on Sat Jul 13 2002
//
package er.directtoweb.qualifiers;

import com.webobjects.directtoweb.BooleanQualifier;
import com.webobjects.directtoweb.NonNullQualifier;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;

/**
 * Basic utility method used when traversing graphs
 * of qualifiers. This callback adds qualifiers from
 * D2W. See the class {@link er.extensions.qualifiers.ERXQualifierTraversal}.
 */
public class ERDQualifierTraversalCallback {

    /**
    * Should traverse boolean qualifier?
     * @return should traverse boolean qualifier
     */
    public boolean traverseBooleanQualifier(BooleanQualifier q) { return true; }
    /**
    * Should traverse non null qualifier?
     * @return should traverse non null qualifier
     */
    public boolean traverseNonNullQualifier(NonNullQualifier q) { return true; }

    /**
    * Should traverse not qualifier?
     * @return should traverse not qualifier
     */
    public boolean traverseNotQualifier(EONotQualifier q) { return true; }
    /**
    * Should traverse or qualifier?
     * @return should traverse or qualifier
     */
    public boolean traverseOrQualifier(EOOrQualifier q) { return true; }
    /**
    * Should traverse and qualifier?
     * @return should traverse and qualifier
     */
    public boolean traverseAndQualifier(EOAndQualifier q) { return true; }
    /**
    * Should traverse a key value qualifier?
     * @return should traverse key value qualifier
     */
    public boolean traverseKeyValueQualifier(EOKeyValueQualifier q) { return true; }
    /**
    * Should traverse key comparison qualifier?
     * @return should traverse key comparison qualifier
     */
    public boolean traverseKeyComparisonQualifier(EOKeyComparisonQualifier q) { return true; }
        
}
