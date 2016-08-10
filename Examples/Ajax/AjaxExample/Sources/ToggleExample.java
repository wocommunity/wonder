import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;

import er.ajax.example.ExampleDataFactory;

public class ToggleExample extends WOComponent {
	public boolean _toggleAreaVisible;
	public boolean _toggleAreaVisible2;
	public boolean _toggleAreaVisible3;
	public String _text;

	public NSArray<String> _names;
	public String _repetitionName;
	public NSMutableSet<String> _selectedNames;

	public ToggleExample(WOContext context) {
		super(context);
		_text = "This text should not change!";
		_names = ExampleDataFactory.values("Name ", 4);
		_selectedNames = new NSMutableSet<String>();
	}

	public void setNameSelected(boolean selected) {
		if (selected) {
			_selectedNames.addObject(_repetitionName);
		}
		else {
			_selectedNames.removeObject(_repetitionName);
		}
	}

	public boolean isNameSelected() {
		return _selectedNames.containsObject(_repetitionName);
	}

	public WOActionResults toggled1() {
		System.out.println("ToggleExample.toggled: toggled value 1 to " + _toggleAreaVisible);
		return null;
	}

	public WOActionResults toggled3() {
		System.out.println("ToggleExample.toggled: toggled value 3 to " + _toggleAreaVisible3);
		return null;
	}
}
