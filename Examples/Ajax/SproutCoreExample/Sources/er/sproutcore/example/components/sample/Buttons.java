package er.sproutcore.example.components.sample;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.sproutcore.views.SCComponent;

public class Buttons extends SCComponent {

	public String _segment;

	public Buttons(WOContext context) {
		super(context);
        setClassName("SC.View");
	}

	public NSArray<String> segments() {
		return new NSArray<String>(new String[] { "one", "two", "three" });
	}

	public NSArray<Object> values() {
		return new NSArray<Object>(new Object[] {});
	}
}