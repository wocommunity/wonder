/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.testrunner;

import java.lang.reflect.*;
import java.text.NumberFormat;
import java.util.*;
import java.io.PrintStream;
import junit.framework.*;
import junit.runner.*;
import er.extensions.*;

public class ERXTestRunner extends BaseTestRunner {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXTestRunner.class);

        public ERXTestListener externalListener = null;

	/**
	 * Constructs a TestRunner.
	 */
        public ERXTestRunner(ERXTestListener extListener) {
            super();
            externalListener = extListener;
	}
	
	/**
	 * Always use the StandardTestSuiteLoader. Overridden from
	 * BaseTestRunner.
	 */
	public TestSuiteLoader getLoader() {
		return new StandardTestSuiteLoader();
	}

	public synchronized void addError(Test test, Throwable t) {
            externalListener.addError(test, t);
	}
	
	public synchronized void addFailure(Test test, AssertionFailedError t) {
            externalListener.addFailure(test, t);
	}
	
	public synchronized void startTest(Test test) {
            externalListener.startTest(test);
	}

	public void endTest(Test test) {
            externalListener.endTest(test);
	}
			
	protected void runFailed(String message) {
            externalListener.runFailed(message);
	}
        protected void clearStatus() {
            externalListener.clearStatus();
        }

        /** Get the freshest loaded class. Uses the CompilerProxy to get it. */
        public Test getTest(String testClass) {
            return new TestSuite(ERXCompilerProxy.defaultProxy().classForName(testClass));
        }
}