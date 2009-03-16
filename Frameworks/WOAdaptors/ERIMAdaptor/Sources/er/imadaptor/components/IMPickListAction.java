package er.imadaptor.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

import er.extensions.foundation.ERXStringUtilities;
import er.imadaptor.InstantMessengerAdaptor;

public class IMPickListAction extends WOComponent {
	private static Logger log = Logger.getLogger(IMPickListAction.class);

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
		String actionUrl = context._componentActionURL(false);
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
			if (ERXStringUtilities.isDigitsOnly(message)) {
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