/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.*;

// A simle utility for providing deprecated functionality for NSTimestamps
public class ERXTimestampUtility {

    public static GregorianCalendar calendarForTimestamp(NSTimestamp t) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(t);
        return c;
    }
    
    public static int hourOfDay(NSTimestamp t) {
        return calendarForTimestamp(t).get(GregorianCalendar.HOUR);
    }

    public static int minuteOfHour(NSTimestamp t) {
        return calendarForTimestamp(t).get(GregorianCalendar.MINUTE);        
    }
}
