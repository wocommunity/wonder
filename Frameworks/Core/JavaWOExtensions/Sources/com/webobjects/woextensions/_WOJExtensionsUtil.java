/*
 * _WOJExtensionsUtil.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSMutableArray;

/**
 * <span class="ja">
 *  woextensions の private クラス
 * </span>
 */
public class _WOJExtensionsUtil {

    public static boolean booleanValue(Object associationValue) {
        boolean associationEvaluation = true;
        if (associationValue!=null) {
            // is this a number. If it is, evaluate it.
            if (associationValue instanceof Number) {
                if (((Number) associationValue).intValue()==0) {
                    associationEvaluation = false;
                }
            } else if (associationValue instanceof String) {
                String associationValueString = (String)associationValue;
                // is this no or false ?
                if (associationValueString.equalsIgnoreCase("no") || associationValueString.equalsIgnoreCase("false")) {
                    associationEvaluation = false;
                } else {
                    // is this a string representing a number ? Try to evaluate it.
                    try {
                        if (Integer.parseInt(associationValueString)==0) {
                            associationEvaluation = false;
                        }
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("error parsing boolean from value "+ associationValueString);
                    }
                }
            } else if (associationValue instanceof Boolean) {
                associationEvaluation = ((Boolean) associationValue).booleanValue();
            } else {
                // do nothing, it's non-null, so it's true !
            }
        } else {
            associationEvaluation = false;
        }

        return associationEvaluation;
    }

    protected static void _sortEOsUsingSingleKey(NSMutableArray<? extends EOEnterpriseObject> array, String aKey) throws NSComparator.ComparisonException {
        NSArray<EOSortOrdering> orderings = new NSArray<EOSortOrdering>(EOSortOrdering.sortOrderingWithKey(aKey, EOSortOrdering.CompareAscending));
        EOSortOrdering.sortArrayUsingKeyOrderArray(array, orderings);
    }

    protected static Object valueForBindingOrNull(String binding,WOComponent component) {
        // wod bindings of the type binding = null are converted to False Boolean
        // associations, which isn't always what we want. This utility method
        // assumes that a Boolean value means the binding value was intended to
        // be null
        if (binding == null) {
            return null;
        }
        Object result = component.valueForBinding(binding);
        if (result instanceof Boolean) {
            result = null;
        }
        return result;
    }
}  

