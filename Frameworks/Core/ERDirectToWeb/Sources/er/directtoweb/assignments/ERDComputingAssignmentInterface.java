/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments;

import com.webobjects.foundation.NSArray;

// 	Implemented by custom assignments that need to tell the rule system that they depend on other
//	keys.
/**
 * In the new rule caching system the significant keys are built on the fly.  For custom assignments that explicitly depend on other keys this interface needs to be implemented so the caching system can take them in to account.
 */

public interface ERDComputingAssignmentInterface {

    // returns the list of (significant) that yield values in the D2WContext this assignment uses
    // to compute its values
    public NSArray dependentKeys(String keyPath);

}
