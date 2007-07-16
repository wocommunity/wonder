package er.extensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.woextensions.WOLongResponsePage;

/**
 * ERXWOLongResponsePage is just like WOLongResponsePage except that it
 * cleans up editing context locks at the end of run() just like the behavior
 * at the end of a normal R-R loop.
 * 
 * @author mschrag
 */
public abstract class ERXWOLongResponsePage extends WOLongResponsePage {
	public ERXWOLongResponsePage(WOContext context) {
		super(context);
	}
	
	public void run() {
		ERXApplication._startRequest();
		try {
			super.run();
		}
		finally {
			ERXApplication._endRequest();
		}
	}
}
