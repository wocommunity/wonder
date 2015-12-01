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

import org.apache.log4j.Logger;

import er.extensions.foundation.ERXPatcher;

/**
 * runs tests with ERTestListeners.
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

	@Override
	public synchronized void addError(Test test, Throwable t) {
            externalListener.addError(test, t);
	}
	
	@Override
	public synchronized void addFailure(Test test, AssertionFailedError t) {
            externalListener.addFailure(test, t);
	}
	
	@Override
	public synchronized void startTest(Test test) {
            externalListener.startTest(test);
	}

	@Override
	public void endTest(Test test) {
            externalListener.endTest(test);
	}
			
	@Override
	protected void runFailed(String message) {
            externalListener.runFailed(message);
	}
        @Override
        protected void clearStatus() {
            externalListener.clearStatus();
        }

        /** Get the freshest loaded class. Uses the CompilerProxy to get it. */
        @Override
        public Test getTest(String testClass) {
            return new TestSuite(ERXPatcher.classForName(testClass));
        }

        /* (non-Javadoc)
         * @see junit.runner.BaseTestRunner#testStarted(java.lang.String)
         */
        @Override
        public void testStarted(String arg0) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see junit.runner.BaseTestRunner#testEnded(java.lang.String)
         */
        @Override
        public void testEnded(String arg0) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see junit.runner.BaseTestRunner#testFailed(int, junit.framework.Test, java.lang.Throwable)
         */
        @Override
        public void testFailed(int arg0, Test arg1, Throwable arg2) {
            // TODO Auto-generated method stub
            
        }
}