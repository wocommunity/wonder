package er.sproutcore.example.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXComponent;

public class Buttons extends ERXComponent {

	public String _segment;

	public Buttons(WOContext context) {
		super(context);
	}

	public NSArray<String> segments() {
		return new NSArray<String>(new String[] { "one", "two", "three" });
	}

	public NSArray<Object> values() {
		return new NSArray<Object>(new Object[] {});
	}
}