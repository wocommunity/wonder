/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.testrunner;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import junit.framework.*;
import junit.runner.*;
import org.apache.log4j.Category;
import java.io.PrintStream;
import java.util.*;

public class ERXBatchTestInterface extends Object implements ERXTestListener {

    ///////////////////////////////////////  log4j category  /////////////////////////////////////
    public static final Category cat = Category.getInstance(ERXBatchTestInterface.class);

    public static final boolean SHOW_EXCEPTIONS=true;
    public static final boolean HIDE_EXCEPTIONS=false;
    
    private Category _userCat;

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
    public void performTest(Category userCategory, boolean showExceptions) {
        _showExceptions = showExceptions;
        _userCat = userCategory;
        resetInterface();
        try {
            testResult = start();
            print();
        } catch(Exception e) {
            _errorMessage = e.getMessage();
            userCat().error("[ERROR] " + e.getMessage());
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

    private Category userCat() {
        if (_userCat == null) {
            return cat;
        } else {
            return _userCat;
        }
    }
    
    public synchronized void print() {
        printHeader();
        printFailures();
        printErrors();
    }

    private PrintStream writer() { return System.out; }
    /**
     * Prints the errors to the standard output
     */
    protected void printErrors() {
        if (testResult.errorCount() != 0) {
            if (testResult.errorCount() == 1)
                userCat().info("There was "+testResult.errorCount()+" error:");
            else
                userCat().info("There were "+testResult.errorCount()+" errors:");

            int i= 1;
            for (Enumeration e= testResult.errors(); e.hasMoreElements(); i++) {
                TestFailure failure= (TestFailure)e.nextElement();
                userCat().info(i + ") " + failure.failedTest());
                userCat().info(aTestRunner.getFilteredTrace(failure.thrownException()));
            }
        }
    }
    /**
     * Prints failures to the standard output
     */
    protected void printFailures() {
        if (testResult.failureCount() != 0) {
            if (testResult.failureCount() == 1)
                userCat().info("There was " + testResult.failureCount() + " failure:");
            else
                userCat().info("There were " + testResult.failureCount() + " failures:");
            int i = 1;
            for (Enumeration e= testResult.failures(); e.hasMoreElements(); i++) {
                TestFailure failure= (TestFailure) e.nextElement();
                userCat().info(i + ") " + failure.failedTest());
                userCat().info(aTestRunner.getFilteredTrace(failure.thrownException()));
            }
        }
    }
    /**
     * Prints the header of the report
     */
    protected void printHeader() {
        if (testResult.wasSuccessful()) {
            userCat().info("OK (" + testResult.runCount() + " tests)");

        } else {
            userCat().info("FAILURES!!!");
            userCat().info("Tests run: " + testResult.runCount() +
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
        userCat().info ("[E] " + test.toString() + " : " + t.getMessage());
    }

    public synchronized void addFailure(Test test, AssertionFailedError t) {
        userCat().info ("[F] " + test.toString() + " : " + t.getMessage());
    }

    public synchronized void startTest(Test test) {
        userCat().info ("[START] " + test.toString());
    }

    public void endTest(Test test) {
        userCat().info ("[END] " + test.toString());
    }

    //////////////////////////////////////
    // ERTestListener implementation
    //////////////////////////////////////
    public void runFailed(String message) {
        userCat().info("[RUN FAILED] " + message);
    }

    public void clearStatus() {
        userCat().info("[CLEAR STATUS]");
    }
}
