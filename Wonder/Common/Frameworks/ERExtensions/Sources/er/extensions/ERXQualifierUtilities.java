/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import org.apache.log4j.Category;

/**
 * Useful collection of qualifier utilities for comparing
 * qualifires. This is needed because EOQualifier <code>equals</code>
 * is not implemented. 
 */
public class ERXQualifierUtilities {

    /** logging support */
    public final static Category cat = Category.getInstance(ERXQualifierUtilities.class);

    /**
     * Tests if two qualifiers are equal.
     * 
     * @param q1 first qualifier
     * @param q2 second qualifier
     * @retrun if the qualifiers are <code>equal<code>.
     */
    public static boolean qualifiersAreEqual(EOQualifierEvaluation q1, EOQualifierEvaluation q2) {
        boolean areEqual = q1 == q2;
        if (!areEqual && q1.getClass().equals(q2.getClass())) {
            if (q1 instanceof EOKeyValueQualifier) {
                areEqual = keyValueQualifiersAreEqual((EOKeyValueQualifier)q1, (EOKeyValueQualifier)q2);
            } else if (q1 instanceof EOKeyComparisonQualifier) {
                areEqual = keyComparisonQualifiersAreEqual((EOKeyComparisonQualifier)q1, (EOKeyComparisonQualifier)q2);    
            } else if (q1 instanceof EONotQualifier) {
                areEqual = notQualifiersAreEqual((EONotQualifier)q1, (EONotQualifier)q2);
            } else if (q1 instanceof EOOrQualifier) {
                areEqual = qualifierArraysAreEqual(((EOOrQualifier)q1).qualifiers(), ((EOOrQualifier)q2).qualifiers());
            } else if (q1 instanceof EOAndQualifier) {
                areEqual = qualifierArraysAreEqual(((EOAndQualifier)q1).qualifiers(), ((EOAndQualifier)q2).qualifiers());
            } else {
                cat.warn("Unknown qualifier type: " + q1);
            }
        }
        return areEqual;
    }

    /**
     * Tests if arrays of qualifiers are equal.
     * 
     * @param q1s array of qualifiers
     * @param q2s array of qualifiers
     * @return if the array of qualfiers are equal
     */
    public static boolean qualifierArraysAreEqual(NSArray q1s, NSArray q2s) {
        boolean areEqual = q1s.count() == q2s.count();
        if (areEqual) {
            NSMutableArray mutableQualifiers = new NSMutableArray();
            mutableQualifiers.addObjectsFromArray(q2s);
            for (int c = 0; c < q1s.count(); c++) {
                boolean cQualifierEqual = false;
                EOQualifierEvaluation q1 = (EOQualifierEvaluation)q1s.objectAtIndex(c);
                for (int d = 0; d < mutableQualifiers.count(); d++) {
                    EOQualifierEvaluation q2 = (EOQualifierEvaluation)mutableQualifiers.objectAtIndex(d);
                    if (qualifiersAreEqual(q1, q2)) {
                        cQualifierEqual = true;
                        mutableQualifiers.removeObjectAtIndex(d); break;
                    }
                }
                if (!cQualifierEqual) {
                    areEqual = false; break;
                }
            }
        }
        return areEqual;
    }

    /**
     * Tests if two EOKeyValueQualifiers are equal.
     *
     * @param q1 first qualifier
     * @param q2 second qualifier
     * @return if the two qualifiers are equal
     */
    public static boolean keyValueQualifiersAreEqual(EOKeyValueQualifier q1, EOKeyValueQualifier q2) {
        return q1.key().equals(q2.key()) && q1.selector().equals(q2.selector()) && ((q1.value() == null && q2.value() == null) ||
                                                                                    (q1.value().equals(q2.value())));
    }

    /**
     * Tests if two EOKeyComparisonQualifier are equal
     *
     * @param q1 first qualifier
     * @param q2 second qualifier
     * @return if the two qualifiers are equal
     */
    public static boolean keyComparisonQualifiersAreEqual(EOKeyComparisonQualifier q1, EOKeyComparisonQualifier q2) {
        return q1.leftKey().equals(q2.leftKey()) && q1.selector().equals(q2.selector()) && q1.rightKey().equals(q2.rightKey());
    }
    
    /**
     * Tests if two EONotQualifier are equal
     *
     * @param q1 first qualifier
     * @param q2 second qualifier
     * @return if the two qualifiers are equal
     */
    public static boolean notQualifiersAreEqual(EONotQualifier q1, EONotQualifier q2) {
        return qualifiersAreEqual((EOQualifierEvaluation)q1.qualifier(), (EOQualifierEvaluation)q2.qualifier());
    }
}
