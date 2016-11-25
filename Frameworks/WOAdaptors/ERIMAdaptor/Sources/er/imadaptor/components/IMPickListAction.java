package er.imadaptor.components;

import org.apache.commons.lang3.StringUtils;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

import er.imadaptor.InstantMessengerAdaptor;

public class IMPickListAction extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private Object _repetitionItem;
	private int _index;

	public IMPickListAction(WOContext context) {
		super(context);
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public void setRepetitionItem(Object repetitionItem) {
		_repetitionItem = repetitionItem;
	}

	public Object repetitionItem() {
		return _repetitionItem;
	}

	public void setIndex(int index) {
		_index = index;
	}

	public int index() {
		return _index;
	}

	public int displayIndex() {
		return _index + 1;
	}

	public String displayItem() {
		String item;
		String displayStringKeyPath = (String) valueForBinding("displayStringKeyPath");
		if (displayStringKeyPath == null) {
			if (_repetitionItem == null) {
				item = "";
			}
			else {
				item = _repetitionItem.toString();
			}
		}
		else {
			Object displayValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(_repetitionItem, displayStringKeyPath);
			if (displayValue == null) {
				item = "";
			}
			else {
				item = displayValue.toString();
			}
		}
		return item;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		String actionUrl = context.componentActionURL(WOApplication.application().componentRequestHandlerKey(), false);
		response.setHeader(actionUrl, InstantMessengerAdaptor.IM_ACTION_URL_KEY);
		super.appendToResponse(response, context);
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults results = null;
		if (context.elementID().equals(context.senderID())) {
			String message = context.request().stringFormValueForKey(InstantMessengerAdaptor.MESSAGE_KEY);
			NSArray list = (NSArray) valueForBinding("list");
			NSArray selectedObjects;
			Object selectedObject;
			if (StringUtils.isNumeric(message)) {
				int selectionIndex = Integer.parseInt(message) - 1;
				if (selectionIndex >= 0 && selectionIndex < list.count()) {
					selectedObject = list.objectAtIndex(selectionIndex);
					selectedObjects = new NSArray(selectedObject);
				}
				else {
					selectedObject = null;
					selectedObjects = NSArray.EmptyArray;
				}
			}
			else {
				String displayStringKeyPath = (String) valueForBinding("displayStringKeyPath");
				Boolean quicksilverBoolean = (Boolean) valueForBinding("quicksilver");
				boolean quicksilver = (quicksilverBoolean != null && quicksilverBoolean.booleanValue());
				selectedObjects = IMSearchOptionsAction.selectedValues(list, displayStringKeyPath, quicksilver, message);
				if (selectedObjects.count() == 1) {
					selectedObject = selectedObjects.objectAtIndex(0);
				}
				else {
					selectedObject = null;
				}
			}
			boolean selectionBound = canSetValueForBinding("selection");
			if (canSetValueForBinding("selections")) {
				setValueForBinding(selectedObjects, "selections");
			}
			if (canSetValueForBinding("selection")) {
				setValueForBinding(selectedObject, "selection");
			}
			results = (WOActionResults) valueForBinding("action");
		}
		return results;
	}
}