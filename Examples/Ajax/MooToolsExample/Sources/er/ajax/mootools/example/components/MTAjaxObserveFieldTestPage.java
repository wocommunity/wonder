package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

public class MTAjaxObserveFieldTestPage extends Main {
 
	public String primaryColor, selectedPrimaryColor, secondaryColor, selectedSecondaryColor;
	private NSArray<String> _secondaryColors;
	
	public MTAjaxObserveFieldTestPage(WOContext context) {
        super(context);
    }

	public WOActionResults doStuff() {
		wasteTime();
		return null;
	}
	
	public NSArray<String> primaryColors() {
		return new NSArray<String>(new String[] {
				"Red", "Yellow", "Green"
		});
	}

	public NSArray<String> secondaryColors() {

		int index = selectedPrimaryColor != null ? primaryColors().indexOf(selectedPrimaryColor) : -1;
		switch (index) {
		case 0:
			setSecondaryColors(new NSArray<String>(new String[] { "light red", "red", "dark red"}));
			break;
		case 1:
			setSecondaryColors(new NSArray<String>(new String[] { "light yellow", "yellow", "dark yellow" }));
			break;
		case 2:
			setSecondaryColors(new NSArray<String>(new String[] { "light green", "green", "dark green" }));
			break;
		default:
			 _secondaryColors = null;
		}

		return _secondaryColors;
		
	}
	
	public void setSecondaryColors(NSArray<String> secondaryColors) {
		wasteTime();
		_secondaryColors = secondaryColors;
	}

}