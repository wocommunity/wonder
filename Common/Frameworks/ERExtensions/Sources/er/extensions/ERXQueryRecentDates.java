/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.directtoweb.*;
import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import org.apache.log4j.Category;

public class ERXQueryRecentDates extends WOComponent {

    public ERXQueryRecentDates(WOContext aContext) {
        super(aContext);
    }
    
    ////////////////////////////////////  log4j category  ////////////////////////////////////////
    public final static Category cat = Category.getInstance(ERXQueryRecentDates.class);
    
    protected WODisplayGroup displayGroup;
    protected String key;

    static final int[] daysAgoArray={ 0,1,3,7,30,90,180 };
    static final String[] daysAgoString={
        "-",
        "day",
        "3 days",
        "week",
        "month",
        "3 months",
        "6 months"
    };
    private final static Integer[] indices={ERXConstant.ZeroInteger, ERXConstant.OneInteger, ERXConstant.TwoInteger, new Integer(3), new Integer(4), new Integer(5), new Integer(6) };
    final NSArray indexes=new NSArray(indices);
    Integer dateItem;

    public String displayString() {
        return daysAgoString[dateItem.intValue()];
    }

    Object date() {
        int found=0;
        NSTimestamp dateFromQueryMin=(NSTimestamp)displayGroup.queryMin().valueForKey(key);
        if (dateFromQueryMin!=null) {
            NSTimestamp now=new NSTimestamp();
            NSTimestamp.IntRef days=new NSTimestamp.IntRef();
            now.gregorianUnitsSinceTimestamp( null,
                                              null,
                                              days,
                                              null,
                                              null,
                                              null, dateFromQueryMin);
            int d=days.value;
            if (d>0) {
                for (int i=0;i<daysAgoArray.length-1;i++) {
                    if (d>=daysAgoArray[i] && d<= daysAgoArray[i+1]) {
                        found=i+1;
                        break;
                    }
                }
            }
        }
        return indexes.objectAtIndex(found);
    }

    void setDate(Integer dateIndex) {
        NSTimestamp now=new NSTimestamp();
        int howManyDaysAgo=dateIndex!=null ? daysAgoArray[dateIndex.intValue()] : 0;
        if(howManyDaysAgo==0)
            displayGroup.queryMin().removeObjectForKey(key);
        else
            displayGroup.queryMin().takeValueForKey(now.timestampByAddingGregorianUnits(0,0,-howManyDaysAgo,0,0,0), key);
    }
}