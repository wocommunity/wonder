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
import com.webobjects.eoaccess.*;
import java.util.Enumeration;

public class ERXRepeatingTable extends WOComponent {

    public ERXRepeatingTable(WOContext aContext) {
        super(aContext);
    }

    public boolean isStateless() { return true; }

    public void reset() { _repeatingList = null; }
    
    private NSMutableArray _repeatingList;
    public NSArray repeatingList() {
        if (_repeatingList == null) {
            _repeatingList = new NSMutableArray();
            NSArray list = (NSArray)valueForBinding("list");
            Integer numberOfRepetetions = (Integer)valueForBinding("repetetions");
            for (int i = 0; i < numberOfRepetetions.intValue(); i++) {
                    _repeatingList.addObjectsFromArray(list);
            }
        }
        return _repeatingList;
    }

}
