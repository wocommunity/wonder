/*
 * WOProjectBuilderAction.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (“Apple”) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under Apple’s copyrights in this original Apple 
 * software (the “Apple Software”), to use, reproduce, modify and 
 * redistribute the Apple Software, with or without modifications, in 
 * source and/or binary forms; provided that if you redistribute the  
 * Apple Software in its entirety and without modifications, you must 
 * retain this notice and the following text and disclaimers in all such
 * redistributions of the Apple Software.  Neither the name, trademarks,
 * service marks or logos of Apple Computer, Inc. may be used to endorse
 * or promote products derived from the Apple Software without specific
 * prior written permission from Apple.  Except as expressly stated in
 * this notice, no other rights or licenses, express or implied, are
 * granted by Apple herein, including but not limited to any patent
 * rights that may be infringed by your derivative works or by other
 * works in which the Apple Software may be incorporated.
 *
 * The Apple Software is provided by Apple on an "AS IS" basis.  APPLE 
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS
 * USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 
 *
 * IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT,
 * INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE,
 * REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE,
 * HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING
 * NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects._ideservices.*;
import com.webobjects.appserver._private.*;

import java.net.*;
import java.io.*;
import java.util.Properties;

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
        String methodName,filename, errorMessage, fullClassName;
        Number line;
        
        methodName = (String)request.stringFormValueForKey("methodName");
        line = request.numericFormValueForKey("line",new NSNumberFormatter("#0"));
        filename = (String)request.stringFormValueForKey("filename");
        errorMessage = (String)request.stringFormValueForKey("errorMessage");
        fullClassName = (String)request.stringFormValueForKey("fullClassName");
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
