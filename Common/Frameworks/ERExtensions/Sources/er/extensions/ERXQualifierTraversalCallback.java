/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.lang.*;
import java.util.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;

/**
 * Basic utility method used when traversing graphs
 * of qualifiers. See the class {@link ERXQualifierTraversal}.
 */
public class ERXQualifierTraversalCallback {

    /**
     * Should traverse not qualifier?
     * @return should traverse not qualifier
     */
    public boolean traverseNotQualifier(EONotQualifier q) { return true; }
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

