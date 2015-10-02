/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.testrunner;

import junit.framework.TestListener;

/**
 * template listner interface.
 */

public interface ERXTestListener extends TestListener {

    // template methods called by BaseTestRunner.  ERTestRunner will forward these to its external listener
    public void runFailed(String message);
    public void clearStatus();
}
