package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXComponentUtilities;

public class AjaxSelectionList extends AjaxComponent {
	private String _id;
	private String _value;
	private NSArray _list;

	public AjaxSelectionList(WOContext context) {
		super(context);
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public void sleep() {
		super.sleep();
		_list = null;
	}

	public NSArray list() {
		if (_list == null) {
			_list = (NSArray) valueForBinding("list");
			if (!ERXComponentUtilities.booleanValueForBinding(this, "mandatory", true)) {
				NSMutableArray optionList = _list.mutableClone();
				optionList.insertObjectAtIndex(NSKeyValueCoding.NullValue, 0);
				_list = optionList;
			}
		}
		return _list;
	}

	public String elementName() {
		String elementName = (String) valueForBinding("elementName");
		if (elementName == null) {
			elementName = "ul";
		}
		return elementName;
	}

	public String containerID() {
		return id() + "_Container";
	}

	public String fieldID() {
		return id() + "_Field";
	}
	
	public String id() {
		if (_id == null) {
			_id = (String) valueForBinding("id");
			if (_id == null) {
				_id = AjaxUtils.toSafeElementID(context().elementID());
			}
		}
		return _id;
	}

	public Object selection() {
		return valueForBinding("selection");
	}

	public void setSelection(Object selection) {
		setValueForBinding(selection, "selection");
	}

	public void setValue(String value) {
		_value = value;
	}

	public String value() {
		return _value;
	}

	public int selectedIndex() {
		NSArray list = list();
		int selectedIndex;
		Object selection = selection();
		if (selection == null) {
			selectedIndex = -1;
		}
		else {
			selectedIndex = list.indexOfObject(selection);
		}
		return selectedIndex;
	}

	public void takeValuesFromRequest(WORequest request, WOContext context) {
		super.takeValuesFromRequest(request, context);
		if (_value == null) {
			setSelection(null);
		}
		else {
			int index = Integer.parseInt(_value);
			NSArray list = list();
			Object selection = null;
			if (index < list.count()) {
				selection = list.objectAtIndex(index);
			}
			if (selection instanceof NSKeyValueCoding.Null) {
				setSelection(null);
			}
			else {
				setSelection(selection);
			}
		}
	}

	protected void addRequiredWebResources(WOResponse res) {
		addScriptResourceInHead(res, "prototype.js");
		addScriptResourceInHead(res, "AjaxSelectionList.js");
		addStylesheetResourceInHead(res, "AjaxSelectionList.css");
	}

	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}
}