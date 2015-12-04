/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.testrunner;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestResult;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * extensions to hold multiple errors and failures.
 */

public class ERXTestResult extends TestResult {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXTestResult.class);

    protected NSMutableArray _errors;
    protected NSMutableArray _failures;

    public ERXTestResult() {
        super();
        _errors = new NSMutableArray();
        _failures = new NSMutableArray();
    }

    @Override
    public synchronized void addError(Test test, Throwable t) {
        super.addError(test, t);
        _errors.addObject(fErrors.get(fErrors.size() -1));
    }
    
    @Override
    public synchronized void addFailure(Test test, AssertionFailedError t) {
        super.addFailure(test, t);
        _failures.addObject(fFailures.get(fFailures.size() - 1));
    }

    public NSArray errorsArray() {
        return _errors;
    }

    public NSArray failuresArray() {
        return _failures;
    }

    public boolean hasErrors() {
        return _errors.count() > 0;
    }
    
    public boolean hasFailures() {
        return _failures.count() > 0;
    }
}
