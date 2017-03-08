/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXTimestampUtilities;

/**
 * Nice for adjusting the query specs for dates on a display group.
 * 
 * @binding displayGroup
 * @binding key
 */
public class ERXQueryRecentDates extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXQueryRecentDates(WOContext aContext) {
        super(aContext);
    }
    
    public WODisplayGroup displayGroup;
    public String key;

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
    private static NSArray<Integer> indexes = new NSArray<>(
        ERXConstant.ZeroInteger,
        ERXConstant.OneInteger,
        ERXConstant.TwoInteger,
        ERXConstant.integerForInt(3),
        ERXConstant.integerForInt(4),
        ERXConstant.integerForInt(5),
        ERXConstant.integerForInt(6)
    );

    public NSArray<Integer> indexes() { return indexes; }
    public Integer dateItem;

    public String displayString() {
        return daysAgoString[dateItem.intValue()];
    }

    // FIXME the return type should be Integer
    public Object date() {
        int found=0;
        NSTimestamp dateFromQueryMin=(NSTimestamp)displayGroup.queryMatch().valueForKey(key);
        if (dateFromQueryMin!=null) {
            NSTimestamp now=new NSTimestamp();
            int d = (int)ERXTimestampUtilities.differenceByDay(dateFromQueryMin, now);
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

    public void setDate(Integer dateIndex) {
        NSTimestamp now=new NSTimestamp();
        int howManyDaysAgo=dateIndex!=null ? daysAgoArray[dateIndex.intValue()] : 0;
        if(howManyDaysAgo==0) {
            displayGroup.queryMatch().removeObjectForKey(key);
            displayGroup.queryOperator().removeObjectForKey(key);
        } else {
            displayGroup.queryMatch().takeValueForKey(now.timestampByAddingGregorianUnits(0,0,-howManyDaysAgo,0,0,0), key);
            displayGroup.queryOperator().takeValueForKey(">", key);
        }
    }
}
