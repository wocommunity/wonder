package er.extensions.appserver;


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXEC;

/**
 * A useful general purpose nextPage delegate that can be configured and passed into a {@link CCAjaxLongResponsePage} for handling a result
 * and returning the nextPage.
 * 
 * A result key can be provided for passing the successful result into the next page.
 * 
 * Note that if the result is a {@link EOGlobalID}, since only a crazy person would return an EOEnterpriseObject
 * from a background thread, then a new editing context is automatically created and
 * the global ID is converted to an EOEnterpriseObject. The EOEnterpriseObject is in then passed into the next
 * page via the nextPageResultKey.
 * 
 * If a specific nextPage WOComponent is not set, the original page in which this controller was instantiated
 * will be returned by default.
 * 
 * Since everyone implements their own error handling, you may want to make your own version of this that checks for
 * a result that is instanceof Throwable in {@link #performAction()} and handles it appropriately since, right now, 
 * error results only logged by this delegate.
 * 
 * @author kieran
 */
public class ERXNextPageForResultWOAction extends ERXAbstractPerformWOAction implements IERXPerformWOActionForResult {
	private static final Logger log = LoggerFactory.getLogger(ERXNextPageForResultWOAction.class);
	
	protected Object _result;
	protected final WOComponent _nextPage;
	private final String _nextPageResultKey;
	private NSMutableDictionary<String, ?> _nextPageValues;

	/**
	 * Convenience constructor that calls {@link #ERXNextPageForResultWOAction(WOComponent, String)}
	 */
	public ERXNextPageForResultWOAction() {
		this(null, null);
	}
	
	/**
	 * Convenience constructor that calls {@link #ERXNextPageForResultWOAction(WOComponent, String)}
	 * 
	 * @param nextPage
	 */
	public ERXNextPageForResultWOAction(WOComponent nextPage) {
		this(nextPage, null);
	}
	
	/**
	 * @param nextPage
	 *            The page that should be returned by {@link #performAction()}.
	 *            Defaults to the page in the {@link WOContext} in which this
	 *            class was created.
	 * @param nextPageResultKey
	 *            The key in which to set the result in the nextPage
	 */
	public ERXNextPageForResultWOAction(WOComponent nextPage, String nextPageResultKey) {
		// We assume that this class is instantiated in the originating page and so we capture the
		// current page in context when this class is created and we will use that as the default
		// nextPage.
		_nextPage = ( nextPage == null ? ERXWOContext.currentContext().page() : nextPage );
		_nextPageResultKey = nextPageResultKey;
	}

	/* (non-Javadoc)
	 * @see er.extensions.appserver.IERXPerformWOActionForResult#setResult(java.lang.Object)
	 */
	public void setResult(Object result) {
		_result = result;

	}

	@Override
	public WOActionResults performAction() {
		log.debug("The result of the task was {}", _result);
		if (_nextPage != null && _nextPageResultKey != null) {
			if (_result instanceof EOGlobalID) {
				
				// Inflate it to a fault
				EOEditingContext ec = ERXEC.newEditingContext();
				// Let's ensure fresh ec since we are likely coming out of a background task
				ec.setFetchTimestamp(System.currentTimeMillis());
				
				_result = ec.faultForGlobalID((EOGlobalID) _result, ec);
				
			}
			_nextPage.takeValueForKey(_result, _nextPageResultKey);
		}
		
		if (_nextPage != null && _nextPageValues != null) {
			for (String key : nextPageValues().allKeys()) {
				Object value = nextPageValues().valueForKey(key);
				_nextPage.takeValueForKey(value, key);
			}
		}
		
		if (_nextPage != null && _nextPage instanceof IERXRefreshPage) {
			((IERXRefreshPage)_nextPage).refresh();
		}

		return _nextPage;
	}
	

	
	/** 
	 * 
	 * @return a mutable dictionary whose values will be pushed into keys in the destination page that is returned by performAction
	 * 
	 * This can be useful for configuring the page that will be returned by this delegate after a long response page task
	 * has completed.
	 * 
	 * For example, if you create a key <code>isEditable</code> with a value <code>Boolean.TRUE</code> in this mutable
	 * dictionary, then when performAction is called, the method setIsEditable( Boolean ) will be called with a parameter
	 * of <code>Boolean.TRUE</code> on the destination page before that page is returned.
	 * 
	 **/
	public NSMutableDictionary<String, ?> nextPageValues() {
		if ( _nextPageValues == null ) {
			_nextPageValues = new NSMutableDictionary<String, Object>();
		}
		return _nextPageValues;
	}
	
	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.append("nextPage", _nextPage);
		b.append("nextPageResultKey", _nextPageResultKey);
		return b.toString();
	}

}
