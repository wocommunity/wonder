/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.testrunner;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.runner.BaseTestRunner;
import junit.runner.StandardTestSuiteLoader;
import junit.runner.TestSuiteLoader;

import org.apache.log4j.Logger;

import er.extensions.ERXPatcher;

/**
 * runs tests with ERTestListeners.<br />
 * 
 */

public class ERXTestRunner extends BaseTestRunner {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXTestRunner.class);

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
            return new TestSuite(ERXPatcher.classForName(testClass));
        }

        /* (non-Javadoc)
         * @see junit.runner.BaseTestRunner#testStarted(java.lang.String)
         */
        public void testStarted(String arg0) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see junit.runner.BaseTestRunner#testEnded(java.lang.String)
         */
        public void testEnded(String arg0) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see junit.runner.BaseTestRunner#testFailed(int, junit.framework.Test, java.lang.Throwable)
         */
        public void testFailed(int arg0, Test arg1, Throwable arg2) {
            // TODO Auto-generated method stub
            
        }
}