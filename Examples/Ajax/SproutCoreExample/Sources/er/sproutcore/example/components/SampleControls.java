package er.sproutcore.example.components;

import com.webobjects.appserver.*;
import com.webobjects.foundation.NSArray;

public class SampleControls extends WOComponent {
	
	public String item;
	
    public SampleControls(WOContext context) {
        super(context);
    }

	public NSArray<String> segments() {
		return new NSArray(new Object[]{"welcome", "buttons", "collections"});
	}
}