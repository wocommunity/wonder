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
import org.apache.log4j.Category;

public class ERXQualifierTraversal {

    ///////////////////////////  log4j category  ///////////////////////////
    public static final Category cat = Category.getInstance("er.extensions.eo.ERQualifierTraversal");
    
    // a simple class that traverses a network of qualifiers; this would be better achieved with categories
    static public boolean traverseQualifier(EOQualifierEvaluation q, ERXQualifierTraversalCallback cb) {
        Boolean result=null;
        if (q==null)
            result=Boolean.TRUE;
        else {
            if (q instanceof EOOrQualifier) {
                EOOrQualifier aq=(EOOrQualifier)q;
                cb.traverseOrQualifier(aq);
                result=Boolean.TRUE;
                for (Enumeration e=aq.qualifiers().objectEnumerator(); e.hasMoreElements(); ) {
                    if (!traverseQualifier((EOQualifierEvaluation)e.nextElement(),cb)) {
                        result=Boolean.FALSE;
                        break;
                    }
                }
            } else if (q instanceof EOAndQualifier) {
                EOAndQualifier aq=(EOAndQualifier)q;
                cb.traverseAndQualifier(aq);
                result=Boolean.TRUE;
                for (Enumeration e=aq.qualifiers().objectEnumerator(); e.hasMoreElements(); ) {
                    if (!traverseQualifier((EOQualifierEvaluation)e.nextElement(),cb)) {
                        result=Boolean.FALSE;
                        break;
                    }
                }
            } else if (q instanceof EOKeyValueQualifier) {
                result=cb.traverseKeyValueQualifier((EOKeyValueQualifier)q) ? Boolean.TRUE : Boolean.FALSE;
            } else if (q instanceof EOKeyComparisonQualifier) {
                result=cb.traverseKeyComparisonQualifier((EOKeyComparisonQualifier)q) ? Boolean.TRUE : Boolean.FALSE;
            } else if (q instanceof EONotQualifier) {
                result=cb.traverseNotQualifier((EONotQualifier)q) ? Boolean.TRUE : Boolean.FALSE;
            } else if (q instanceof BooleanQualifier) {
                result=cb.traverseBooleanQualifier((BooleanQualifier)q) ? Boolean.TRUE : Boolean.FALSE;
            } else if (q instanceof NonNullQualifier) {
                result=cb.traverseNonNullQualifier((NonNullQualifier)q) ? Boolean.TRUE : Boolean.FALSE;
            }
        }
        if (result==null) {
            cat.error("Found unknown qualifier type:"+q.getClass().getName());
            throw new RuntimeException("Found unknown qualifier type:"+q.getClass().getName());            
        }
        return result.booleanValue();
    }
}
