/*
 * WOLongResponsePage.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSLog;

public abstract class WOLongResponsePage extends WOComponent implements Runnable {

    static String WOMetaRefreshSenderId = "WOMetaRefresh";
    
    protected Object _status;
    protected Object _result;
    protected Exception _exception;
    protected int _refreshInterval;
    protected boolean _performingAction;
    protected boolean _cancelled;
    protected boolean _done;
    protected boolean _doneAndRefreshed;

    protected void _finishInitialization() {
        if (!WOApplication.application().adaptorsDispatchRequestsConcurrently()) {
            throw new RuntimeException("<"+getClass().getName()+"> Cannot initialize because:\nThe application must be set to run with multiple threads to use this component. You must first increase the application's worker thread count to at least 1. You then have several options:\n1. If you set the count to 1, your code does not need to be thread safe.\n2. If you set the count above 1, and your code is not thread safe, disable concurrent request handling.\n3. you set the count above 1, and your code is thread safe, you can enable concurrent request handling.");
        }

        _status = null;
        _result = null;
        _done = false;
        _doneAndRefreshed = false;
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

        setResult(null);

        _done = false;
        _doneAndRefreshed = false;

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
                Thread t = new Thread(this, "WOLongResponsePage: " + getClass().getName());
                t.start();
            } catch (Exception localException) {
                throw new RuntimeException ("<WOLongResponsePage> Exception occurred while creating long response thread: "+localException.toString());
                                     
            }
        }

        // If the refreshInterval was set and we did not get a result yet, let's add the refresh header.
        if ((((int)refreshInterval())!=0) && !_done) {
            String modifiedDynamicUrl = aContext.urlWithRequestHandlerKey(WOApplication.application().componentRequestHandlerKey(), null, null);

            String header = "" +_refreshInterval+ ";url=" +modifiedDynamicUrl+ "/" + aContext.session().sessionID()+ "/" +aContext.contextID()+ "." +WOMetaRefreshSenderId;

            aResponse.setHeader(header, "Refresh");
        } else if ( (_refreshInterval!=0) && _done && ! _doneAndRefreshed ) {
            // If the response is done and finished quickly (before the first branch of this conditional is invoked),
            // make sure to refresh the page immediately.
            String modifiedDynamicUrl = aContext.urlWithRequestHandlerKey(WOApplication.application().componentRequestHandlerKey(), null, null);

            String header = "0;url=" +modifiedDynamicUrl+ "/" + aContext.session().sessionID()+ "/" +aContext.contextID()+ "." +WOMetaRefreshSenderId;

            aResponse.setHeader(header, "Refresh");
            _doneAndRefreshed = true;
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
