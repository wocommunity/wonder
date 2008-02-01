package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXComponentUtilities;

/**
 * AjaxSelectionList provides a list component that supports keyboard navigation and component renderers. Externally,
 * the component behaves like a form field, similar to WOPopUpButton except that it uses its component content as the
 * renderer for each item instead of taking a displayString.
 * 
 * The javascript wrapper exposes events of the selection changing, the item being selected (via double-click or the
 * enter key), and an item being deleted (with the delete key).
 * 
 * @author mschrag
 * @binding elementName (optional) the type of element for the wrapper (ul, ol, table)
 * @binding list (required) the list to render
 * @binding item (required) bound for each item of the list
 * @binding selection (optional) the current selection
 * @binding mandatory (optional) if false, a null value will be prepended to the list. It is up to your component
 *          content to handle the null value to show a "No Selection" value. mandatory defaults to true. Mandatory does
 *          not imply any validation, it is the semantic equivalent of noSelectionString when compared to WOPopUpButton.
 * @binding id (optional) the id of the list
 * @binding class (optional) the css class of the list
 * @binding style (optional) the css style of the list
 * @binding onchange (optional) the javascript to execute when the selection changes
 * @binding onselect (optional) the javascript to execute when the user presses enter or double-clicks
 * @binding ondelete (optional) the javascript to execute when backspace or delete is pressed
 */
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

	public void setItem(Object item) {
		if (item instanceof NSKeyValueCoding.Null) {
			setValueForBinding(null, "item");
		}
		else {
			setValueForBinding(item, "item");
		}
	}

	public Object item() {
		return valueForBinding("item");
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