/*
 * _WOJExtensionsUtil.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (“Apple”) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under Apple’s copyrights in this original Apple 
 * software (the “Apple Software”), to use, reproduce, modify and 
 * redistribute the Apple Software, with or without modifications, in 
 * source and/or binary forms; provided that if you redistribute the  
 * Apple Software in its entirety and without modifications, you must 
 * retain this notice and the following text and disclaimers in all such
 * redistributions of the Apple Software.  Neither the name, trademarks,
 * service marks or logos of Apple Computer, Inc. may be used to endorse
 * or promote products derived from the Apple Software without specific
 * prior written permission from Apple.  Except as expressly stated in
 * this notice, no other rights or licenses, express or implied, are
 * granted by Apple herein, including but not limited to any patent
 * rights that may be infringed by your derivative works or by other
 * works in which the Apple Software may be incorporated.
 *
 * The Apple Software is provided by Apple on an "AS IS" basis.  APPLE 
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS
 * USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 
 *
 * IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT,
 * INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE,
 * REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE,
 * HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING
 * NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

public class _WOJExtensionsUtil extends Object {

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

    protected static void _sortEOsUsingSingleKey(NSMutableArray array, String aKey) throws NSComparator.ComparisonException {
        
        NSArray orderings = new NSArray(EOSortOrdering.sortOrderingWithKey(aKey, EOSortOrdering.CompareAscending));

        EOSortOrdering.sortArrayUsingKeyOrderArray(array, orderings);
    }

    protected static Object valueForBindingOrNull(String binding,WOComponent component) {
        // wod bindings of the type binding = null are converted to False Boolean
        // associations, which isn't always what we want. This utility method
        // assumes that a Boolean value means the binding value was intented to
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

