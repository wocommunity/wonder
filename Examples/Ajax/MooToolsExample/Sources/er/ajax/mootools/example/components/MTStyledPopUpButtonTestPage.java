package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXStringUtilities;

public class MTStyledPopUpButtonTestPage extends Main {
    
	public String _item;
	public String _selectedItem;
	
	public MTStyledPopUpButtonTestPage(WOContext context) {
        super(context);
        String favFruit = context.request().stringFormValueForKey("FavFruit");
        if(ERXStringUtilities.stringIsNullOrEmpty(favFruit) == false && list().containsObject(favFruit)) {
        	_selectedItem = favFruit;
        }
    }

	public NSArray<String> list() {
		return new NSArray<String>(new String[] {"Apple", "Banana", "Mango", "Orange", "Pineapple" });
	}

	public WOActionResults selectFruit() {
		return null;
	}

}