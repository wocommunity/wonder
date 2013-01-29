/*
 * WOProjectBuilderAction.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects._ideservices._IDEProject;
import com.webobjects._ideservices._WOProject;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODeployedBundle;
import com.webobjects.appserver._private.WOProjectBundle;
import com.webobjects.foundation.NSNumberFormatter;

/* This DirectAction is used for driving ProjectBuilder : it creates a socket and hope that
 ProjectBuilder is listening on it, then it sends the details about the exception so PB can
 display the correct file at the correct line number.
 The result of the DirectAction is a basic HTML page that use a JavaScript to go back. The JavaScript
 source code is directly hard coded in the page.
*/
public class WOProjectBuilderAction extends WODirectAction {

    public WOProjectBuilderAction(WORequest aRequest) {
        super(aRequest);
    }

    protected WOResponse javascriptBack() {
        // Return an HTML page that contains a JavaScript code to do a 'back'
        WOResponse response = WOApplication.application().createResponseInContext(null);
        response.appendContentString("<HTML><BODY><SCRIPT>history.go(-1);</SCRIPT><P>Please use the <B>back</B> button of your browser to go back to the Exception page.</P></BODY></HTML>");
        return response;
    }
    
    public WOActionResults openInProjectBuilderAction() {

        // Read now the information about the request : which method, which line #, which file, which message
        WORequest request = request();
        String filename, errorMessage, fullClassName;
        Number line;
        
        // String methodName = (String)request.stringFormValueForKey("methodName");
        line = request.numericFormValueForKey("line",new NSNumberFormatter("#0"));
        filename = request.stringFormValueForKey("filename");
        errorMessage = request.stringFormValueForKey("errorMessage");
        fullClassName = request.stringFormValueForKey("fullClassName");
        WOResourceManager resources = WOApplication.application().resourceManager();

        // pay no attention to this use of protected API
        WODeployedBundle appBundle = resources._appProjectBundle();
        if (appBundle instanceof WOProjectBundle) {
            WOProjectBundle project = (WOProjectBundle) appBundle;
            _WOProject woproject = project._woProject();
            String filePath = woproject._pathToSourceFileForClass(fullClassName, filename);
            if (filePath == null) {
                
                // inform user file not found?
            } else {
                
                _IDEProject ideproject = woproject.ideProject();
                int lineInt = (line == null) ? 0 : line.intValue();

                ideproject.openFile(filePath, lineInt, errorMessage);
            }
        }

        return javascriptBack();
    }
}
