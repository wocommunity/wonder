package er.directtoweb.components._ajax;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDDebuggingHelp;
import er.extensions.foundation.ERXStringUtilities;

public class ERDAjaxDebuggingHelp extends ERDDebuggingHelp {
	private String id;

	public ERDAjaxDebuggingHelp(WOContext context) {
		super(context);
	}
	
	// accessors
	public String id() {
		if (id == null) id = ERXStringUtilities.safeIdentifierName(context().elementID());
		return id;
	}
	
	public String summaryId() {
		return id() + "_summary";
	}
	
	public String onMouseOver() {
		return "$('" + summaryId() + "').appear({duration: 0.2});";
	}
	
	public String onMouseOut() {
		return "$('" + summaryId() + "').fade({duration: 0.2});";
	}
	
	public String onClick() {
		return "Effect.toggle('" + id() + "', 'appear', {duration: 0.2}); return false;";
	}
	
	// actions
	public WOComponent submit() {
		return context().page();
	}
}
