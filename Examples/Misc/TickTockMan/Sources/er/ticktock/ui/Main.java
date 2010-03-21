package er.ticktock.ui;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.components.ERXComponent;

public class Main extends ERXComponent {

	public Main(WOContext context) {
		super(context);
	}

	public NSTimestamp time() { return new NSTimestamp(); }
	
	public long millis() { return System.currentTimeMillis(); }
}
