package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

/**
 * Handy component when you need to return a WOActionResults and the interface
 * requires a WOComponent.
 * 
 * @author ak
 */
public class ERXResponseComponent extends ERXStatelessComponent {

	protected WOResponse _response;
	protected WOComponent _component;

	public ERXResponseComponent(WOContext context) {
		super(context);
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		if (_component == null) {
			super.takeValuesFromRequest(request, context);
		}
		context._setCurrentComponent(_component);
		_component.ensureAwakeInContext(context);
		try {
			_component.takeValuesFromRequest(request, context);
		}
		finally {
			_component._sleepInContext(context);
			context._setCurrentComponent(null);
		}
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		if (_component == null) {
			return super.invokeAction(request, context);
		}
		context._setCurrentComponent(_component);
		_component.ensureAwakeInContext(context);
		try {
			return _component.invokeAction(request, context);
		}
		finally {
			_component._sleepInContext(context);
			context._setCurrentComponent(null);
		}
	}

	public void setResponse(WOResponse response) {
		_response = response;
	}

	public void setActionResults(WOActionResults results) {
		_response = results.generateResponse();
	}

	public void setComponent(WOComponent results) {
		_component = results;
	}

	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		aResponse.setContent(_response.content());
		aResponse.setContentEncoding(_response.contentEncoding());
		aResponse.setHeaders(_response.headers());
		aResponse.setStatus(_response.status());
	}

}
