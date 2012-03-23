package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class MTAjaxSortablesTestPage extends Main {
	
	private NSMutableArray<NSDictionary<String, Object>> _listA;
	
    public MTAjaxSortablesTestPage(WOContext context) {
        super(context);
        _listA = new NSMutableArray<NSDictionary<String, Object>>();
		NSMutableDictionary<String, Object> red = new NSMutableDictionary<String, Object>();
		red.takeValueForKey(redSquareSrc(), "url");
		red.takeValueForKey("Red", "title");
		red.takeValueForKey(1, "id");
		_listA.addObject(red);
		NSMutableDictionary<String, Object> yellow = new NSMutableDictionary<String, Object>();
		yellow.takeValueForKey(yellowSquareSrc(), "url");
		yellow.takeValueForKey("Yellow", "title");
		yellow.takeValueForKey(2, "id");
		_listA.addObject(yellow);
		NSMutableDictionary<String, Object> green = new NSMutableDictionary<String, Object>();
		green.takeValueForKey(greenSquareSrc(), "url");
		green.takeValueForKey("Green", "title");
		green.takeValueForKey(3, "id");
		_listA.addObject(green);

    }

	
	public String redSquareSrc() {
		WOResourceManager rm = application().resourceManager();
		return rm.urlForResourceNamed("img/Red_Thumb.png", "app", null, context().request());
	}
	
	public String yellowSquareSrc() {		
		WOResourceManager rm = application().resourceManager();
		return rm.urlForResourceNamed("img/Yellow_Thumb.png", "app", null, context().request());
	}
	
	public String greenSquareSrc() {
		WOResourceManager rm = application().resourceManager();
		return rm.urlForResourceNamed("img/Green_Thumb.png", "app", null, context().request());
	}
    
	public NSMutableArray<NSDictionary<String, Object>> listA() {
		return _listA;
	}

	public WOActionResults orderChanged() {
		System.out.println("Hello?");
		return null;
	}

	public NSDictionary<String, Object> repetitionListItemA;

	public WOActionResults saveChanges() {
		return null;
	}


}