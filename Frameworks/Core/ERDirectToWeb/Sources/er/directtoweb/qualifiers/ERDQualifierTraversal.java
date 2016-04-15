/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.qualifiers;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.directtoweb.BooleanQualifier;
import com.webobjects.directtoweb.NonNullQualifier;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifierEvaluation;

/**
 * Contains a single method for traversing
 * a network of qualifiers.
 */
public class ERDQualifierTraversal {
    private static final Logger log = LoggerFactory.getLogger(ERDQualifierTraversal.class);
    
    /**
     * Simple method to traverse a network of qualifiers
     * using a callback.
     * @param q qualifier to traverse
     * @param cb call back
     * @return if the traversal was successful
     */
    public static boolean traverseQualifier(EOQualifierEvaluation q, ERDQualifierTraversalCallback cb) {
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
            } else if (q instanceof EONotQualifier) {
                EONotQualifier nq = (EONotQualifier)q;
                cb.traverseNotQualifier((EONotQualifier)q);
                result = traverseQualifier(nq.qualifier(),cb) ? Boolean.TRUE : Boolean.FALSE;
            } else if (q instanceof EOKeyValueQualifier) {
                result=cb.traverseKeyValueQualifier((EOKeyValueQualifier)q) ? Boolean.TRUE : Boolean.FALSE;
            } else if (q instanceof EOKeyComparisonQualifier) {
                result=cb.traverseKeyComparisonQualifier((EOKeyComparisonQualifier)q) ? Boolean.TRUE : Boolean.FALSE;
            } else if (q instanceof BooleanQualifier) {
                result=cb.traverseBooleanQualifier((BooleanQualifier)q) ? Boolean.TRUE : Boolean.FALSE;
            } else if (q instanceof NonNullQualifier) {
                result=cb.traverseNonNullQualifier((NonNullQualifier)q) ? Boolean.TRUE : Boolean.FALSE;
            }
        }
        if (result==null) {
            log.error("Found unknown qualifier type: {}", q.getClass());
            throw new RuntimeException("Found unknown qualifier type:"+q.getClass().getName());            
        }
        return result.booleanValue();
    }
}
