/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.testrunner;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import junit.framework.*;
import junit.runner.*;
import org.apache.log4j.Category;
import java.io.PrintStream;
import java.util.*;

public class ERXWOTestInterface extends WOComponent implements ERXTestListener {
    
    //////////////////////////////////////  log4j category  /////////////////////////////////////
    public static final Category cat = Category.getInstance(ERXWOTestInterface.class);

    // bind to a WOTextField
    public String theTest;
    // bind to a WOString
    public String errorMessage;
    // bind to a WOCheckbox
    public Boolean showExceptions;

    public ERXTestRunner aTestRunner;
    public ERXTestResult testResult;
    public long runTime;
    public NSArray allTests;
    
    public ERXWOTestInterface(WOContext context) {
        super(context);
        showExceptions = Boolean.FALSE;
        theTest = "";
        runTime = 0;
        errorMessage = "";
        testResult = null;
    }

    protected NSArray bundles() {
        NSMutableArray bundles = new NSMutableArray(NSBundle.frameworkBundles());
        bundles.addObject(NSBundle.mainBundle());
        return bundles;
    }

    public NSArray allTests() {
        if(allTests == null) {
            String thisBundleName = NSBundle.bundleForClass(getClass()).name();
            NSMutableArray theClassNames = new NSMutableArray();
            Enumeration bundleEnum = bundles().objectEnumerator();
            while (bundleEnum.hasMoreElements()) {
                NSBundle bundle = (NSBundle)bundleEnum.nextElement();
                if (!bundle.name().equals(thisBundleName)) {
                    Enumeration classNameEnum = bundle.bundleClassNames().objectEnumerator();
                    while (classNameEnum.hasMoreElements()) {
                        String className = (String)classNameEnum.nextElement();
                        if (className != null
                            && ( className.endsWith( "TestCase" ) || className.startsWith("tests."))
                            && !className.startsWith( "junit." )
                            && className.indexOf( "$" ) < 0)
                            theClassNames.addObject(className);
                    }
                }
            }
            allTests = theClassNames;
        }
        return allTests;
    }
    
    private void resetInterface() {
        runTime = 0;
        errorMessage = "";
        aTestRunner = new ERXTestRunner(this);
        testResult = new ERXTestResult();
        allTests = null;
    }

    // ACTION METHOD
    public WOComponent performTest() {
        resetInterface();
        try {
            testResult = start();
        } catch(Exception e) {
            errorMessage = e.getMessage();
        }
        return context().page();
    }

    // Starts a test run. Analyzes the command line arguments (test) and runs the given test suite.
    public ERXTestResult start() throws Exception {
        if (theTest.equals("")) {
            throw new Exception("You need to provide the name of a class to use as the TestCase for this run.");
        }
        try {
            Test suite = aTestRunner.getTest(theTest);
            return doRun(suite);
        }
        catch(Exception e) {
            throw new Exception("Could not create and run test suite: "+e);
        }
    }

    public ERXTestResult doRun(Test suite) {
        testResult.addListener(this);
        long startTime = System.currentTimeMillis();
        suite.run(testResult);
        long endTime = System.currentTimeMillis();
        runTime = endTime-startTime;
        return testResult;
    }
    
    //////////////////////////////////////
    // TestListener implementation
    //////////////////////////////////////
    public synchronized void addError(Test test, Throwable t) {
        cat.error ("[E] " + test.toString() + " : " + t.getMessage());
    }

    public synchronized void addFailure(Test test, AssertionFailedError t) {
        cat.error ("[F] " + test.toString() + " : " + t.getMessage());
    }

    public synchronized void startTest(Test test) {
        cat.info ("[START] " + test.toString());
    }

    public void endTest(Test test) {
        cat.info ("[END] " + test.toString());
    }

    //////////////////////////////////////
    // ERTestListener implementation
    //////////////////////////////////////
    public void runFailed(String message) {
        cat.debug ("--------------------------- runFailed() ---------------------------");
        errorMessage = message;
    }

    public void clearStatus() {
        cat.debug ("-------------------------- clearStatus() --------------------------");
        errorMessage = "";
    }
}
