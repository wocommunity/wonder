package er.extensions.concurrency;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.components.ERXNonSynchronizingComponent;
import er.extensions.eof.ERXConstant;

/**
 * ERXLongResponse is like WOLongResponsePage from JavaWOExtensions, but
 * it can be used as a component and doesn't need to be subclassed.
 * Instead, you provide a ERXLongResponseTask subclass and set it 
 * via either the bindings or explicitely.
 *
 * @binding task implementation of ERXLongResponseTask
 * @created ak on Tue Feb 03 2004
 * @project ERExtensions
 */

public class ERXLongResponse extends ERXNonSynchronizingComponent {
    static String WOMetaRefreshSenderId = "WOMetaRefresh";

    /** logging support */
    private static final Logger log = Logger.getLogger(ERXLongResponse.class);

    protected Number _refreshInterval;
    protected boolean _performingAction;
    protected boolean _doneAndRefreshed;

    protected ERXLongResponseTask _task;

    public ERXLongResponse(WOContext aContext)  {
        super(aContext);
        _doneAndRefreshed = false;
        _refreshInterval = ERXConstant.ZeroInteger;
        _performingAction = false;
    }

    public ERXLongResponseTask task() {
    	if(_task == null) {
    		_task = (ERXLongResponseTask)valueForBinding("task");
    	}
        return _task;
    }
    public void setTask(ERXLongResponseTask task) {
        _task = task;
    }


    public int refreshInterval() {
    	if(ERXConstant.ZeroInteger.equals(_refreshInterval)) {
    		Number n = (Number)valueForBinding("refreshInterval");
    		if(n != null) {
    			_refreshInterval = n;
    		}
    	}
    	return _refreshInterval.intValue();
    }
    public void setRefreshInterval(int value) {
    	_refreshInterval = new Integer(value);
    }
    
    public WOComponent refresh() {
    	return task().nextPage();
    }
    
    public void appendToResponse(WOResponse aResponse, WOContext aContext)  {
        if (!_performingAction) {
            _performingAction = true;
            task().setLongResponse(this);
            task().start();
        }
        boolean done = task().isDone();
        boolean doneButNotRefreshed = done && !_doneAndRefreshed;
        // If the refreshInterval was set and we did not get a result yet, let's add the refresh header.
        int interval = (doneButNotRefreshed ? 0 : refreshInterval());

        if(!done || (doneButNotRefreshed)) {
            // If the response is done and finished quickly (before the first branch of this conditional is invoked),
            // make sure to refresh the page immediately.
            String modifiedDynamicUrl = aContext.urlWithRequestHandlerKey(WOApplication.application().componentRequestHandlerKey(), null, null);

            String header = interval + ";url=" +modifiedDynamicUrl+ "/" + aContext.session().sessionID()+ "/" +aContext.contextID()+ "." +WOMetaRefreshSenderId;

            aResponse.setHeader(header, "Refresh");
            if((doneButNotRefreshed)) {
                _doneAndRefreshed = true;
            }
        }
        super.appendToResponse(aResponse, aContext);
    }

    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext)  {
        if (aContext.senderID().equals(WOMetaRefreshSenderId)) {
            // We recognized the elementID that was set for the meta refresh.
            // we know which action to call, it is -returnRefreshedPage.
            return refresh();
        }
        return super.invokeAction(aRequest, aContext);
    }
}
