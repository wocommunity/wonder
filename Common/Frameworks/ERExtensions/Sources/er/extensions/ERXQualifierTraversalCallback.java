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

public class ERXQualifierTraversalCallback {
    
    public boolean traverseNotQualifier(EONotQualifier q) { return true; }
    public boolean traverseBooleanQualifier(BooleanQualifier q) { return true; }
    public boolean traverseNonNullQualifier(NonNullQualifier q) { return true; }
    public boolean traverseOrQualifier(EOOrQualifier q) { return true; }
    public boolean traverseAndQualifier(EOAndQualifier q) { return true; }
    public boolean traverseKeyValueQualifier(EOKeyValueQualifier q) { return true; }
    public boolean traverseKeyComparisonQualifier(EOKeyComparisonQualifier q) { return true; }
}

