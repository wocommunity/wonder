/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERGraphUtilities.java created by angela on Thu 01-Nov-2001 */
package er.extensions.components;

import java.awt.Color;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXConstant;

/**
 * Utility methods useful when using GifPlot.
 */

public class ERXGraphUtilities {
    private static final Logger log = LoggerFactory.getLogger(ERXGraphUtilities.class);

    public static final Integer fiftyOne = ERXConstant.integerForInt(51);
    public static final Integer oneHundredTwo = ERXConstant.integerForInt(102);
    public static final Integer oneHundredFiftyThree = ERXConstant.integerForInt(153);
    public static final Integer oneHundredSixtySix = ERXConstant.integerForInt(166);
    public static final Integer twoHundredFour = ERXConstant.integerForInt(204);
    public static final Integer twoHundredFiftyFour = ERXConstant.integerForInt(254);
    public static final Integer twoHundredFiftyFive = ERXConstant.integerForInt(255);

    public static final NSArray orange = new NSArray(new Object[]{twoHundredFiftyFive,oneHundredTwo, ERXConstant.ZeroInteger});
    public static final NSArray yellow = new NSArray(new Object[]{twoHundredFiftyFive,oneHundredFiftyThree,ERXConstant.ZeroInteger });
    public static final NSArray blue = new NSArray(new Object[]{fiftyOne,oneHundredTwo,oneHundredFiftyThree});
    public static final NSArray green = new NSArray(new Object[]{oneHundredTwo, oneHundredTwo, fiftyOne});
    public static final NSArray grey = new NSArray(new Object[]{twoHundredFour,twoHundredFour,twoHundredFour});

    public static final Color awtOrange = new Color(255, 102, 0);
    public static final Color awtYellow = new Color(255, 153, 0);
    public static final Color awtBlue = new Color(51, 102, 153);
    public static final Color awtGreen = new Color(102, 102, 51);
    public static final Color awtGrey = new Color(204, 204, 204);

    public static int computeSumForKey(NSArray values, String key) {
        int sum = 0;
        for(Enumeration e = values.objectEnumerator(); e.hasMoreElements();)
            sum += Integer.parseInt(((NSDictionary)e.nextElement()).objectForKey(key).toString());
        return sum;
    }

    //X-AXIS
        private static NSArray _lastNMonthsAsStringsArray;
        public static NSArray lastNMonthsAsStringsArray(int numberDesiredMonths) {
            if (_lastNMonthsAsStringsArray == null) {
                NSMutableArray result = new NSMutableArray();
                NSTimestamp today = new NSTimestamp();
                for (int i=1;i<=numberDesiredMonths;i++)
                    result.addObject(today.timestampByAddingGregorianUnits(0, (i * -1), 0, 0, 0, 0).toString());
                _lastNMonthsAsStringsArray = result;
                log.debug("*********** result for lastNMonthsAsStringsArray = {}", result);
            }
            return _lastNMonthsAsStringsArray;
        }

        private static NSArray _lastNMonthsArray;
        public static NSArray lastNMonthsArray(int numberDesiredMonths) {
            if (_lastNMonthsArray == null) {
                NSMutableArray result = new NSMutableArray();
                NSTimestamp today = new NSTimestamp();
                for (int i=1;i<=numberDesiredMonths;i++)
                    result.addObject(today.timestampByAddingGregorianUnits(0, (i * -1), 0, 0, 0, 0));
              //  log.debug("*********** today = {}", today);
               // log.debug("*********** numberDesiredMonths = {}", numberDesiredMonths);
                _lastNMonthsArray = result;
                log.debug("*********** result for lastNMonthsArray = {}", result);
            }
            return _lastNMonthsArray;
        }

}
