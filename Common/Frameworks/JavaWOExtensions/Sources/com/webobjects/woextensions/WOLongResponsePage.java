/*
 * WOLongResponsePage.java
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

public abstract class WOLongResponsePage extends WOComponent implements Runnable {

    static String WOMetaRefreshSenderId = "WOMetaRefresh";
    
    protected Object _status;
    protected Object _result;
    protected Exception _exception;
    protected int _refreshInterval;
    protected boolean _performingAction;
    protected boolean _cancelled;
    protected boolean _done;

    protected void _finishInitialization() {
        if (!WOApplication.application().adaptorsDispatchRequestsConcurrently()) {
            throw new RuntimeException("<"+getClass().getName()+"> Cannot initialize because:\nThe application must be set to run with multiple threads to use this component. You must first increase the application's worker thread count to at least 1. You then have several options:\n1. If you set the count to 1, your code does not need to be thread safe.\n2. If you set the count above 1, and your code is not thread safe, disable concurrent request handling.\n3. you set the count above 1, and your code is thread safe, you can enable concurrent request handling.");
        }

        _status = null;
        _result = null;
        _done = false;
        _exception = null;
        _cancelled = false;
        _refreshInterval = 0;
        _performingAction = false;
    }
    
    public WOLongResponsePage(WOContext aContext)  {
        super(aContext);
        _finishInitialization();
    }

    public Object status() {
        return _status;
    }

    public void setStatus(Object anObject) {
        if (anObject != _status) {
            synchronized(this) {
                _status = anObject;
            }
        }
    }

    protected Exception _exception() {
        return _exception;
    }

    protected void _setException(Exception anObject) {
        if (anObject != _exception) {
            synchronized(this) {
                _exception = anObject;
            }
        }
    }

    public void setRefreshInterval(double interval) {
        if (interval > 0) {
            _refreshInterval = (int)interval;
        } else {
            _refreshInterval = 0;
        }
    }

    public double refreshInterval() {
        return (double)_refreshInterval;
    }


    public Object result() {
        return _result;
    }

    public void setResult(Object anObject) {
        if (anObject != _result) {
            synchronized(this) {
                _result = anObject;
            }
        }
    }

    // Laurent R-: modified this method to register the newly created
    // thread with the JavaVM, if necessary. Radar bug #: 2244036
    public void run() {

        WOApplication app = WOApplication.application();

        setResult(null);

        _done = false;

        String name = getClass().getName();

        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupWebObjects)) {
            NSLog.debug.appendln("<"+name+">: creating computation thread");
        }

        // called to start new thread

        // We need to catch the possible exceptions too.
        try {
            setResult(performAction());
        } catch (Exception localException) {
            
            _setException(localException);

            NSLog.err.appendln("<"+getClass().getName()+"> long response thread raised : "+localException.toString());
            NSLog.err.appendln("STACK TRACE:");
            NSLog.err.appendln(localException);
        }
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupWebObjects)) {
            NSLog.debug.appendln("<"+name+">: exiting computation thread");
        }
        _done = true;
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext)  {

        if (!_performingAction) {
            _performingAction = true;
            try {
                Thread t = new Thread(this);
                t.start();
            } catch (Exception localException) {
                throw new RuntimeException ("<WOLongResponsePage> Exception occurred while creating long response thread: "+localException.toString());
                                     
            }
        }

        // If the refreshInterval was set and we did not get a result yet, let's add the refresh header.
        if ((_refreshInterval!=0) && !_done) {
            String modifiedDynamicUrl = aContext.urlWithRequestHandlerKey(WOApplication.application().componentRequestHandlerKey(), null, null);

            String header = "" +_refreshInterval+ ";url=" +modifiedDynamicUrl+ "/" + aContext.session().sessionID()+ "/" +aContext.contextID()+ "." +WOMetaRefreshSenderId;

            aResponse.setHeader(header, "Refresh");
        }

        super.appendToResponse(aResponse, aContext);
    }

    public WOComponent pageForException(Exception exception) {
        throw new RuntimeException("<WOLongResponsePage> Exception occurred in long response thread: "+exception.toString());
    }

    public WOComponent refreshPageForStatus(Object aStatus)  {
        return this;
    }

    public WOComponent pageForResult(Object aResult)  {
        return this;
    }

    public WOComponent cancelPageForStatus(Object aStatus)  {
        return refreshPageForStatus(aStatus);
    }

    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext)  {
        if (aContext.senderID().equals(WOMetaRefreshSenderId)) {
            // We recognized the elementID that was set for the meta refresh.
            // we know which action to call, it is -returnRefreshedPage.
            return refresh();
        }
        return super.invokeAction(aRequest, aContext);
    }

    public boolean isCancelled() {
        return _cancelled;
    }

    public void setCancelled(boolean aBool) {
        if (aBool != _cancelled) {
            synchronized(this) {
                _cancelled = aBool;
            }
        }
    }

    public WOComponent refresh() {
        Exception e = _exception();
        if (e!=null) {
            return pageForException(e);
        }

        if (_done) {
            if (_cancelled) {
                return cancelPageForStatus(status());
            } else {
                return pageForResult(result());
            }
        }
        return refreshPageForStatus(status());
    }

    public WOComponent cancel()  {
        setCancelled(true);
        return cancelPageForStatus(status());
    }

    public abstract Object performAction() ;
}
