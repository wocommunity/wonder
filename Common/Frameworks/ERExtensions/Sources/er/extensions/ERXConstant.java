/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERXConstants.java created by max on Fri 27-Apr-2001 */
package er.extensions;

import com.webobjects.foundation.*;
import java.math.BigDecimal;

// Nice holder for base constaints.
public class ERXConstant {

    public static final int MAX_INT=2500;
    protected static Integer[] INTEGERS=new Integer[MAX_INT];
    static {
        for (int i=0; i<MAX_INT; i++) INTEGERS[i]=new Integer(i);
    }

    public static final Object EmptyObject = new Object();
    public static final NSArray EmptyArray = new NSArray();
    public static final NSDictionary EmptyDictionary = new NSDictionary();
    public static final Integer OneInteger = integerForInt(1);
    public static final Integer ZeroInteger = integerForInt (0);
    public static final Integer TwoInteger = integerForInt (2);
    public static final Integer ThreeInteger = integerForInt (3);
    public static final BigDecimal ZeroBigDecimal = new BigDecimal(0.00);
    public static final BigDecimal OneBigDecimal = new BigDecimal(1.00); 
    public static final Class[] EmptyClassArray = new Class[0];
    public static final Class[] NotificationClassArray = { com.webobjects.foundation.NSNotification.class };
    public static final Class[] ObjectClassArray = { Object.class };
    public static final Class[] StringClassArray = new Class[] { String.class };
    public static final Object[] EmptyObjectArray = new Object[] {};
    
    public static Integer integerForInt(int i) {
        return (i>=0 && i<MAX_INT) ? INTEGERS[i] : new Integer(i);
    }

    public static Integer integerForString(String s) throws NumberFormatException {
        return integerForInt(Integer.parseInt(s));
    }
    
}
