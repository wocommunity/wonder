package er.extensions;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

/**
 * ERXLongResponse is like WOLongResponsePage from JavaWOExtensions, but:
 * - it can be used as a component, doesn't need to be subclassed.
 *   Instead, you provide a Task subclass and set it via either the bindings or explicitely
 * - it contains a progress component. 
 *
 * @binding task implementation of ERXLongResponse.Task
 * @created ak on Tue Feb 03 2004
 * @project ERExtensions
 */

public class ERXLongResponse extends WOComponent {
    static String WOMetaRefreshSenderId = "WOMetaRefresh";

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERXLongResponse.class,"components");

    protected int _refreshInterval;
    protected boolean _performingAction;
    protected boolean _doneAndRefreshed;

    public static abstract class Task implements Runnable {
        protected ERXLongResponse _sender;
        protected Object _status;
        protected Object _result;
        protected Exception _exception;
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
        }
        
        public Task() {
             _finishInitialization();
        }
        
        public void setRefreshPage(ERXLongResponse sender) {
        	_sender = sender;
        }
        
        public void run() {
            WOApplication app = WOApplication.application();

            setResult(null);

            _done = false;

            String name = getClass().getName();

            log.debug("<"+name+">: creating computation thread");

            // called to start new thread
            try {
                setResult(performAction());
            } catch (Exception localException) {
                setException(localException);
                log.error("<"+name+">: long response thread raised : "+localException.getMessage(), localException);
            }
            log.debug("<"+name+">: exiting computation thread");
            _done = true;
        }

        public Object message() {
            return _status;
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
        public Exception exception() {
            return _exception;
        }

        public void setException(Exception anObject) {
            if (anObject != _exception) {
                synchronized(this) {
                    _exception = anObject;
                }
            }
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

        public boolean isCancelled() {
            return _cancelled;
        }

        public boolean isDone() {
            return _done;
        }

        public void setCancelled(boolean aBool) {
            if (aBool != _cancelled) {
                synchronized(this) {
                    _cancelled = aBool;
                }
            }
        }

        public WOComponent pageForException(Exception exception) {
            throw new RuntimeException("<WOLongResponsePage> Exception occurred in long response thread: "+exception.toString());
        }

        public WOComponent refreshPageForStatus(Object aStatus)  {
            return _sender.context().page();
        }

        public WOComponent pageForResult(Object aResult)  {
            return _sender.context().page();
        }

        public WOComponent cancelPageForStatus(Object aStatus)  {
            return refreshPageForStatus(aStatus);
        }

        /** @return result of performing the action */
        public abstract Object performAction();

    }
    
    protected Task _task;

    public ERXLongResponse(WOContext aContext)  {
        super(aContext);
        _doneAndRefreshed = false;
        _refreshInterval = 0;
        _performingAction = false;
    }

    public Task task() {
        return _task;
    }

    public void setTask(Task task) {
        _task = task;
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext)  {

        if (!_performingAction) {
            _performingAction = true;
            task().setRefreshPage(this);
            try {
                Thread t = new Thread(task());
                t.start();
            } catch (Exception localException) {
                throw new RuntimeException ("<ERXLongResponse> Exception occurred while creating long response thread: "+localException.toString());
            }
        }

        // If the refreshInterval was set and we did not get a result yet, let's add the refresh header.
        if ((((int)refreshInterval())!=0) && !task().isDone()) {
            String modifiedDynamicUrl = aContext.urlWithRequestHandlerKey(WOApplication.application().componentRequestHandlerKey(), null, null);

            String header = "" +_refreshInterval+ ";url=" +modifiedDynamicUrl+ "/" + aContext.session().sessionID()+ "/" +aContext.contextID()+ "." +WOMetaRefreshSenderId;

            aResponse.setHeader(header, "Refresh");
        } else if ((refreshInterval()!=0) && task().isDone() && ! _doneAndRefreshed ) {
            // If the response is done and finished quickly (before the first branch of this conditional is invoked),
            // make sure to refresh the page immediately.
            String modifiedDynamicUrl = aContext.urlWithRequestHandlerKey(WOApplication.application().componentRequestHandlerKey(), null, null);

            String header = "0;url=" +modifiedDynamicUrl+ "/" + aContext.session().sessionID()+ "/" +aContext.contextID()+ "." +WOMetaRefreshSenderId;

            aResponse.setHeader(header, "Refresh");
            _doneAndRefreshed = true;
        }

        super.appendToResponse(aResponse, aContext);
    }

    public int refreshInterval() {
        return _refreshInterval;
    }

    public void setRefreshInterval(int value) {
    	_refreshInterval = value;
    }

    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext)  {
        if (aContext.senderID().equals(WOMetaRefreshSenderId)) {
            // We recognized the elementID that was set for the meta refresh.
            // we know which action to call, it is -returnRefreshedPage.
            return refresh();
        }
        return super.invokeAction(aRequest, aContext);
    }

    public WOComponent refresh() {
        Exception e = task().exception();
        if (e!=null) {
            return task().pageForException(e);
        }

        if (task().isDone()) {
            if (task().isCancelled()) {
                return task().cancelPageForStatus(task().status());
            } else {
                return task().pageForResult(task().result());
            }
        }
        return task().refreshPageForStatus(task().status());
    }

    public WOComponent cancel()  {
        task().setCancelled(true);
        return task().cancelPageForStatus(task().status());
    }
}
