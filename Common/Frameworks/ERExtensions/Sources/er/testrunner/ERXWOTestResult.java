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
import java.io.ByteArrayOutputStream;
import com.webobjects.woextensions.*;

public class ERXWOTestResult extends WOComponent {
    public Throwable exception;
    private NSArray _reasonLines;
    public String currentReasonLine;

    public WOExceptionParser error;
    public WOParsedErrorLine errorline;


    public ERXWOTestResult(WOContext aContext) {
        super(aContext);
    }

    /////////////////////////////////////  log4j category  ////////////////////////////////////
    public static final Category cat = Category.getInstance(ERXWOTestResult.class);

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
        exception = currentError.thrownException();
        error = new WOExceptionParser(exception);
        exception.printStackTrace(new PrintStream(byos));
        return byos.toString();
    }
    public String currentErrorTestName() {
        Object test = (Object)currentError.failedTest();
        if (test instanceof TestCase)
            return ((TestCase)test).getName();
        else if (test instanceof TestSuite)
            return ((TestSuite)test).getName();
        else
            return test.toString();
    }
    public String currentErrorTestClassName() {
        return ((Object)currentError.failedTest()).getClass().getName();
    }
    public String currentErrorExceptionMessage() {
        return currentError.thrownException().getMessage();
    }
    public int index() {
        return currentErrorIndex + 1;
    }

    // external factory methods
    public static WOComponent reportFromBatchTestInterface(ERXBatchTestInterface bti) {
        ERXWOTestResult report = (ERXWOTestResult)WOApplication.application().pageWithName("ERXWOTestResult", new WOContext(new WORequest(null,null,null,null,null,null)));
        report.takeValueForKey(bti.testResult(), "testResult");
        report.takeValueForKey(bti.errorMessage(), "errorMessage");
        report.takeValueForKey(new Long(bti.runTime()), "runTime");
        report.takeValueForKey(bti.test(), "test");
        report.takeValueForKey(bti.showExceptions() ? Boolean.TRUE : Boolean.FALSE, "showExceptions");
        return report;
    }
}
