package er.ajax;

import java.util.Enumeration;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * AjaxResponse provides support for delayed evaluation of IAjaxElements. This allows the entire invokeAction cycle to
 * execute without having to run through the full appendToResponse process.
 * 
 * @author mschrag
 */
public class AjaxResponse extends WOResponse {
	private WORequest _request;
	private WOContext _context;
	private NSMutableArray _delayedElements;

	public AjaxResponse(WORequest request, WOContext context) {
		_request = request;
		_context = context;
	}

	/**
	 * Enqueues an IAjaxElement to be evaluated at the end of the invokeAction process.
	 * 
	 * @param element
	 *            the element to handle
	 * @param currentComponent
	 *            the component associated with the element
	 * @param currentElementID
	 *            the elementID of the associated element
	 */
	public void addDelayedElement(IAjaxElement element, WOComponent currentComponent, String currentElementID) {
		if (_delayedElements == null) {
			_delayedElements = new NSMutableArray();
		}
		_delayedElements.addObject(new AjaxState(element, currentComponent, currentElementID));
	}

	public WOResponse generateResponse() {
		if (_delayedElements != null) {
			Enumeration delayedElementsEnum = _delayedElements.objectEnumerator();
			while (delayedElementsEnum.hasMoreElements()) {
				AjaxState ajaxState = (AjaxState) delayedElementsEnum.nextElement();
				_context._setCurrentComponent(ajaxState._currentComponent);
				ajaxState._currentComponent._setParent(ajaxState._parentComponent, ajaxState._keyAssociations, ajaxState._childTemplate);
				_context.appendElementIDComponent(ajaxState._currentElementID);
				ajaxState._element.handleRequest(_request, _context);
				_context.deleteAllElementIDComponents();
			}
		}
		return this;
	}

	protected static class AjaxState {
		public IAjaxElement _element;
		public WOComponent _currentComponent;
		public String _currentElementID;
		public WOComponent _parentComponent;
		public NSMutableDictionary _keyAssociations;
		public WOElement _childTemplate;

		public AjaxState(IAjaxElement element, WOComponent currentComponent, String currentElementID) {
			_element = element;
			_currentComponent = currentComponent;
			_currentElementID = currentElementID;
			_parentComponent = _currentComponent.parent();
			_childTemplate = _currentComponent._childTemplate();
			_keyAssociations = _currentComponent._keyAssociations;
		}
	}
}
