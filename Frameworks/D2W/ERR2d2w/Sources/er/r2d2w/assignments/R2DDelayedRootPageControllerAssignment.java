package er.r2d2w.assignments;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.ERD2WUtilities;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSForwardException;

import er.directtoweb.assignments.delayed.ERDDelayedAssignment;
import er.extensions.appserver.ERXWOContext;

/**
 * Simple assignment that returns true if the parent D2WPage of
 * the current component has no other parent D2WPage components
 */
public class R2DDelayedRootPageControllerAssignment extends ERDDelayedAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public R2DDelayedRootPageControllerAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DDelayedRootPageControllerAssignment(String key, Object value) {
		super(key, value);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DDelayedRootPageControllerAssignment(unarchiver);
	}

	@Override
	public Object fireNow(D2WContext c) {
		WOComponent component = ERXWOContext.currentContext().component();
		D2WPage currentPage = ERD2WUtilities.enclosingComponentOfClass(component, D2WPage.class);
		if(ERD2WUtilities.enclosingPageOfClass(currentPage, D2WPage.class) == null) {
			try {
				Class<?> clazz = Class.forName((String)value());
            	return clazz.newInstance();
			} catch(Exception e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		}
		return null;
	}

}
