package er.rest.routes.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;

public class ERXRestBatchNavigationBar extends ERXStatelessComponent {
	public ERXRestBatchNavigationBar(WOContext context) {
		super(context);
	}
	
	public Object record() {
		return valueForBinding("record");
	}
	
}