package er.r2d2w.assignments;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.ERD2WUtilities;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.assignments.delayed.ERDDelayedAssignment;
import er.directtoweb.pages.ERD2WPage;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;

public class R2DPropertyCSSClassAssignment extends ERDDelayedAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final String ERROR_CLASS = "error";
	public static final String MANDATORY_CLASS = "mandatory";

	public R2DPropertyCSSClassAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DPropertyCSSClassAssignment(String key, Object value) {
		super(key, value);
	}

	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		super.encodeWithKeyValueArchiver(archiver);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DPropertyCSSClassAssignment(unarchiver);
	}

	public Object fireNow(D2WContext c) {
		NSMutableArray<String> classes = new NSMutableArray<String>();
		
		//Find the first parent page that collects exceptions
		WOComponent component = (WOComponent)c.valueForKeyPath("session.context.component");
		ERD2WPage page = ERD2WUtilities.enclosingComponentOfClass(component, ERD2WPage.class);
		while(page != null) {
			if(page.shouldCollectValidationExceptions()) {
				break;
			}
			page = (ERD2WPage) ERD2WUtilities.enclosingPageOfClass(page, ERD2WPage.class);
		}
		
		if(page != null && page.hasValidationExceptionForPropertyKey()) {
			classes.add(ERROR_CLASS);
		}
		if(ERXValueUtilities.booleanValue(c.valueForKey("displayRequiredMarker"))){
			classes.add(MANDATORY_CLASS);
		}
		classes.add(ERXStringUtilities.safeIdentifierName(c.propertyKey()));
		return classes.componentsJoinedByString(" ");
	}
	
}
