/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.testrunner;

import java.util.Enumeration;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.runner.BaseTestRunner;

import org.apache.log4j.Logger;

/**
 * Used for running a batch of tests.
 */

public class ERXBatchTestInterface implements ERXTestListener {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXBatchTestInterface.class);

    public static final boolean SHOW_EXCEPTIONS=true;
    public static final boolean HIDE_EXCEPTIONS=false;
    
    private Logger _userLog;

    protected String _test;
    protected boolean _showExceptions;
    protected String _errorMessage;

    protected ERXTestRunner aTestRunner;
    protected ERXTestResult testResult;
    protected long runTime;
    
    public ERXBatchTestInterface(String test) {
        super();
        _test = test;
        _showExceptions = HIDE_EXCEPTIONS;
        _errorMessage = "";
        runTime = 0;
        aTestRunner = null;
        testResult = null;
    }

    private void resetInterface() {
        runTime = 0;
        _errorMessage = "";
        aTestRunner = new ERXTestRunner(this);
        testResult = new ERXTestResult();
    }

    // ACTION METHOD
    public void performTest(Logger userLogger, boolean showExceptions) {
        _showExceptions = showExceptions;
        _userLog = userLogger;
        resetInterface();
        try {
            testResult = start();
            print();
        } catch(Exception e) {
            _errorMessage = e.getMessage();
            userLog().error("[ERROR] " + e.getMessage());
        }
    }

    // Starts a test run. Analyzes the command line arguments (test) and runs the given test suite.
    protected ERXTestResult start() throws Exception {
        if (_test.equals("")) {
            throw new Exception("You need to provide the name of a class to use as the TestCase for this run.");
        }
        try {
            Test suite = aTestRunner.getTest(_test);
            return doRun(suite);
        }
        catch(Exception e) {
            throw new Exception("Could not create and run test suite: "+e);
        }
    }

    protected ERXTestResult doRun(Test suite) {
        testResult.addListener(this);
        long startTime = System.currentTimeMillis();
        suite.run(testResult);
        long endTime = System.currentTimeMillis();
        runTime = endTime-startTime;
        return testResult;
    }

    private Logger userLog() {
        if (_userLog == null) {
            return log;
        }
        return _userLog;
    }
    
    public synchronized void print() {
        printHeader();
        printFailures();
        printErrors();
    }

    /**
     * Prints the errors to the standard output
     */
    protected void printErrors() {
        if (testResult.errorCount() != 0) {
            if (testResult.errorCount() == 1)
                userLog().info("There was "+testResult.errorCount()+" error:");
            else
                userLog().info("There were "+testResult.errorCount()+" errors:");

            int i= 1;
            for (Enumeration e= testResult.errors(); e.hasMoreElements(); i++) {
                TestFailure failure= (TestFailure)e.nextElement();
                userLog().info(i + ") " + failure.failedTest());
                userLog().info(BaseTestRunner.getFilteredTrace(failure.thrownException()));
            }
        }
    }
    /**
     * Prints failures to the standard output
     */
    protected void printFailures() {
        if (testResult.failureCount() != 0) {
            if (testResult.failureCount() == 1)
                userLog().info("There was " + testResult.failureCount() + " failure:");
            else
                userLog().info("There were " + testResult.failureCount() + " failures:");
            int i = 1;
            for (Enumeration e= testResult.failures(); e.hasMoreElements(); i++) {
                TestFailure failure= (TestFailure) e.nextElement();
                userLog().info(i + ") " + failure.failedTest());
                userLog().info(BaseTestRunner.getFilteredTrace(failure.thrownException()));
            }
        }
    }
    /**
     * Prints the header of the report
     */
    protected void printHeader() {
        if (testResult.wasSuccessful()) {
            userLog().info("OK (" + testResult.runCount() + " tests)");

        } else {
            userLog().info("FAILURES!!!");
            userLog().info("Tests run: " + testResult.runCount() +
                             ", Failures: " + testResult.failureCount() +
                             ", Errors: " + testResult.errorCount());
        }
    }

    public ERXTestResult testResult() {
        return testResult;
    }

    public String errorMessage() {
        return _errorMessage;
    }

    public long runTime() {
        return runTime;
    }

    public String test() {
        return _test;
    }

    public boolean showExceptions() {
        return _showExceptions;
    }
    
    //////////////////////////////////////
    // TestListener implementation
    //////////////////////////////////////

    public synchronized void addError(Test test, Throwable t) {
        userLog().info ("[E] " + test.toString() + " : " + t.getMessage());
    }

    public synchronized void addFailure(Test test, AssertionFailedError t) {
        userLog().info ("[F] " + test.toString() + " : " + t.getMessage());
    }

    public synchronized void startTest(Test test) {
        userLog().info ("[START] " + test.toString());
    }

    public void endTest(Test test) {
        userLog().info ("[END] " + test.toString());
    }

    //////////////////////////////////////
    // ERTestListener implementation
    //////////////////////////////////////
    public void runFailed(String message) {
        userLog().info("[RUN FAILED] " + message);
    }

    public void clearStatus() {
        userLog().info("[CLEAR STATUS]");
    }
}
