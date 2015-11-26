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

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.foundation.ERXPatcher;

/**
 * component for interactively running tests.
 */

public class ERXWOTestInterface extends WOComponent implements ERXTestListener {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXWOTestInterface.class);

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
            NSMutableSet theClassNames = new NSMutableSet();
            Enumeration bundleEnum = bundles().objectEnumerator();
            while (bundleEnum.hasMoreElements()) {
                NSBundle bundle = (NSBundle)bundleEnum.nextElement();
                if (!bundle.name().equals(thisBundleName)) {
                    Enumeration classNameEnum = bundle.bundleClassNames().objectEnumerator();
                    while (classNameEnum.hasMoreElements()) {
                        String className = (String)classNameEnum.nextElement();
                        if (className != null
                            && ( className.endsWith( "Test" ) || className.endsWith( "TestCase" ) || className.indexOf("tests.") == 0 || className.indexOf(".tests.") > 0)
                            && !className.startsWith( "junit." )
                            && className.indexOf( "$" ) < 0) {
                        	try {
                        		Class c = ERXPatcher.classForName(className);
                        		//if(c != null && c.isAssignableFrom(TestCase.class))
                        			theClassNames.addObject(munge(className));
                        	} catch(Exception ex) {
                        		// ignored
                        		log.warn("Skipping test " + className + ": " + ex);
                        	}
                        }
                    }
                }
            }
            allTests = theClassNames.allObjects();
            try {
                allTests = allTests.sortedArrayUsingComparator(NSComparator.AscendingStringComparator);
            } catch (Exception ex) {
                log.warn(ex);
                // so we won't get sorted, oh well :)
            }
        }
        return allTests;
    }

    private String munge(String className) {
        String mungedName = className;
        int offset = className.lastIndexOf('.');
        if(offset >= 0) {
            mungedName = className.substring(offset+1) + " - " + className.substring(0, offset);
        }
        return mungedName;
    }

    private String demunge(String className) {
        String demungedName = className;
        int offset = className.indexOf(" - ");
        if(offset >= 0) {
            demungedName = className.substring(offset+3) + "." + className.substring(0, offset);
        }
        return demungedName;
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
            Test suite = aTestRunner.getTest(demunge(theTest));
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
        log.error ("[E] " + test.toString() + " : " + t.getMessage(), t);
    }

    public synchronized void addFailure(Test test, AssertionFailedError t) {
        log.error ("[F] " + test.toString() + " : " + t.getMessage());
    }

    public synchronized void startTest(Test test) {
        log.info ("[START] " + test.toString());
    }

    public void endTest(Test test) {
        log.info ("[END] " + test.toString());
    }

    //////////////////////////////////////
    // ERTestListener implementation
    //////////////////////////////////////
    public void runFailed(String message) {
        log.debug ("--------------------------- runFailed() ---------------------------");
        errorMessage = message;
    }

    public void clearStatus() {
        log.debug ("-------------------------- clearStatus() --------------------------");
        errorMessage = "";
    }

    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
    	if (session().objectForKey("ERXWOTestInterface.enabled") != null) {
    		super.appendToResponse(r, c);
    	}
    	else {
    		r.appendContentString("please use the ERXDirectAction testAction to login first!");
    	}
    }
}
