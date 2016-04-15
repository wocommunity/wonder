/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.testrunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestSuite;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.woextensions.WOParsedErrorLine;

/**
 * WOComponent display of an ERTestResult.
 * 
 * @binding errorMessage
 * @binding testResult
 * @binding runTime
 * @binding test
 * @binding showExceptions
 */

public class ERXWOTestResult extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public Throwable exception;
    public String currentReasonLine;

    public NSArray error;
    public WOParsedErrorLine errorline;


    public ERXWOTestResult(WOContext aContext) {
        super(aContext);
    }

    public void setCurrentError(TestFailure value) {
        currentError = value;
        if(currentError != null) {
            exception = currentError.thrownException();
            //AK: doesn't compile
            // error = new WOExceptionParser(exception);
            // error = WOExceptionParser.parseException(exception);
         }
    }

    public String errorMessage;
    public ERXTestResult testResult;
    public TestFailure currentError;
    public int currentErrorIndex;
    public long runTime;
    public String test;
    public Boolean showExceptions;

    /////////////////////////////////////
    // conditions
    /////////////////////////////////////
    public boolean hasTestResult() {
        return testResult != null;
    }
    public boolean hasErrorMessage() {
        return errorMessage != null;
    }

    /////////////////////////////////////
    // derived accessors
    /////////////////////////////////////
    public String currentErrorStackTrace() {
        ByteArrayOutputStream byos = new ByteArrayOutputStream();
        currentErrorThrownException().printStackTrace(new PrintStream(byos));
        return byos.toString();
    }
    public String currentErrorTestName() {
        Object failedTest = currentError.failedTest();
        if (failedTest instanceof TestCase)
            return ((TestCase)failedTest).getName();
        else if (failedTest instanceof TestSuite)
            return ((TestSuite)failedTest).getName();
        else
            return failedTest.toString();
    }
    public String currentErrorTestClassName() {
        return ((Object)currentError.failedTest()).getClass().getName();
    }
    public String currentErrorExceptionMessage() {
        return currentErrorThrownException().getMessage();
    }
    public int index() {
        return currentErrorIndex + 1;
    }
    public Throwable currentErrorThrownException() {
        if(currentError.thrownException() instanceof NSForwardException) {
            return ((NSForwardException)currentError.thrownException()).originalException();
        }
        return currentError.thrownException();
    }
    // external factory methods
    public static WOComponent reportFromBatchTestInterface(ERXBatchTestInterface bti) {
        ERXWOTestResult report = (ERXWOTestResult)WOApplication.application().pageWithName("ERXWOTestResult", new WOContext(new WORequest(null,null,null,null,null,null)));
        report.takeValueForKey(bti.testResult(), "testResult");
        report.takeValueForKey(bti.errorMessage(), "errorMessage");
        report.takeValueForKey(Long.valueOf(bti.runTime()), "runTime");
        report.takeValueForKey(bti.test(), "test");
        report.takeValueForKey(bti.showExceptions() ? Boolean.TRUE : Boolean.FALSE, "showExceptions");
        return report;
    }
}
