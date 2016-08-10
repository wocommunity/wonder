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
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	protected WOResponse _response;
	protected WOComponent _component;

	public ERXResponseComponent(WOContext context) {
		super(context);
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		if (_component == null) {
			super.takeValuesFromRequest(request, context);
		} else {
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

	@Override
	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		aContext._setResponse(aResponse);
		aResponse.setContent(_response.content());
		aResponse.setContentEncoding(_response.contentEncoding());
		aResponse.setHeaders(_response.headers());
		aResponse.setStatus(_response.status());
	}

}
