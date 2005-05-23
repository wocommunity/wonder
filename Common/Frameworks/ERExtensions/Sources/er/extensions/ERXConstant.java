/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import java.math.BigDecimal;

/**
 * Collection of base constants. Note that upon class initialization
 * 2500 Integers will be created and cached, from 0 - 2499.
 */
public class ERXConstant {

    public static final int MAX_INT=2500;
    protected static Integer[] INTEGERS=new Integer[MAX_INT];
    static {
        for (int i=0; i<MAX_INT; i++) INTEGERS[i]=new Integer(i);
    }

    public static final Object EmptyObject = new Object();
    public static final NSArray EmptyArray = NSArray.EmptyArray;
    public static final NSArray SingleNullValueArray = new NSArray(NSKeyValueCoding.NullValue);
    public static final NSDictionary EmptyDictionary = NSDictionary.EmptyDictionary;
    public static final Integer MinusOneInteger = new Integer(-1);
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

    /**
     * Returns an Integer for a given int
     * @return potentially cache Integer for a given int
     */
    public static Integer integerForInt(int i) {
        return (i>=0 && i<MAX_INT) ? INTEGERS[i] : new Integer(i);
    }

    /**
     * Returns an Integer for a given String
     * @throws NumberFormatException forwarded from the
     *		parseInt method off of Integer
     * @return potentially cache Integer for a given String
     */
    // MOVEME: ERXStringUtilities
    public static Integer integerForString(String s) throws NumberFormatException {
        return integerForInt(Integer.parseInt(s));
    }
}
