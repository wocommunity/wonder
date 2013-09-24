import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;

import er.ajax.example.ExampleDataFactory;
import er.extensions.foundation.ERXStringUtilities;

public class RadioButtonExample extends com.webobjects.appserver.WOComponent {
	private NSMutableArray<String> _values;
	private String _itemValue;
	private NSMutableSet<String> _selectedItems;

	public RadioButtonExample(WOContext context) {
		super(context);
		_values = ExampleDataFactory.values("Radio Button #", 10);
		_selectedItems = new NSMutableSet<String>();
	}
	
	public NSMutableSet<String> selectedItems() {
		return _selectedItems;
	}

	public NSMutableArray<String> getValues() {
		return _values;
	}

	public void setItemValue(String itemValue) {
		_itemValue = itemValue;
	}

	public String getItemValue() {
		return _itemValue;
	}
	
	public String buttonID () {
		return ERXStringUtilities.safeIdentifierName("button" + _itemValue);
	}

	public void setSelected(boolean selected) {
		if (selected) {
			//_selectedItems.removeAllObjects();
			_selectedItems.addObject(_itemValue);
		}
		else {
			_selectedItems.removeObject(_itemValue);
		}
	}

	public boolean isSelected() {
		return _selectedItems.containsObject(_itemValue);
	}
	
	public WOActionResults submit() {
		System.out.println("RadioButtonExample.submit: submit");
		return null;
	}
}
