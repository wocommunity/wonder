package er.extensions.concurrency;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.woextensions.WOLongResponsePage;

import er.extensions.appserver.ERXApplication;

/**
 * ERXWOLongResponsePage is just like WOLongResponsePage except that it
 * cleans up editing context locks at the end of run() just like the behavior
 * at the end of a normal R-R loop.
 * 
 * @author mschrag
 */
public abstract class ERXWOLongResponsePage extends WOLongResponsePage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ERXWOLongResponsePage(WOContext context) {
		super(context);
	}
	
	public <T extends WOComponent> T pageWithName(Class<T> componentClass) {
		return (T) super.pageWithName(componentClass.getName());
	}
	
	public <T extends WOComponent> T pageWithName(Class<T> componentClass, WOContext context) {
		return (T) WOApplication.application().pageWithName(componentClass.getName(), context);
	}
	
	@Override
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
