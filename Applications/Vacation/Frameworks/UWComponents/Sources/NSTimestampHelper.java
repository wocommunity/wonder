//
// NSTimestampHelper.java
// Project UWComponents
//
// Created by amishra on Mon Nov 04 2002
//

package com.uw.shared;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class NSTimestampHelper extends Object {

    public static long minusDays(NSTimestamp first, NSTimestamp second) {
        long deltaTime = first.getTime() - second.getTime();
        return deltaTime / 1000 / 60 / 60 / 24;
    }

}
