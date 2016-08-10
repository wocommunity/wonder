package wowodc.background.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wowodc.background.utilities.Utilities;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;

import er.coolcomponents.CCAjaxLongResponsePage;
import er.extensions.appserver.IERXPerformWOActionForResult;

/**
 *  A simple class that demonstrates how to implement custom handling of errors thrown by tasks running in {@link CCAjaxLongResponsePage}
 *  
 * @author kieran
 */
public class CustomNextPageForErrorResultController implements IERXPerformWOActionForResult {
	private static final Logger log = LoggerFactory.getLogger(CustomNextPageForErrorResultController.class);
	
	private Exception _result = null;
	private final WOComponent _nextPage;
	
	public CustomNextPageForErrorResultController(WOComponent pageThatInitiatedTheTask) {
		_nextPage = pageThatInitiatedTheTask;
		log.debug("Constructor called with WOComponent argument = {}", pageThatInitiatedTheTask);
	}
	
	public WOActionResults performAction() {

		Utilities.addErrorMessage(_result.getMessage());
		
		return _nextPage;
	}

	public void setResult(Object result) {
		// Validate that we are being given a valid result
		if (result == null) {
			throw new IllegalArgumentException("We expected a non-null argument for the error result!");
		}
		
		if (result instanceof Exception) {
			_result = (Exception) result;
		} else {
			throw new IllegalArgumentException("We expected an Exception argument, but instead we got " + result + "!"); 
		}
	}
}
