package er.r2d2w.assignments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.ERD2WUtilities;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;

import er.directtoweb.assignments.ERDAssignment;
import er.directtoweb.assignments.delayed.ERDDelayedAssignment;
import er.directtoweb.pages.ERD2WPage;
import er.extensions.foundation.ERXUtilities;

public class R2DPropertyMessageAssignment extends ERDDelayedAssignment {
	/**
	 * Do I need to update serialVersionUID? See section 5.6 <cite>Type Changes
	 * Affecting Serialization</cite> on page 51 of the <a
	 * href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object
	 * Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(R2DPropertyMessageAssignment.class);

	public R2DPropertyMessageAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DPropertyMessageAssignment(String key, Object value) {
		super(key, value);
	}

	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		super.encodeWithKeyValueArchiver(archiver);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DPropertyMessageAssignment(unarchiver);
	}

	@Override
	public Object fireNow(D2WContext c) {
        Object result = null;
        try {
            Method m = getClass().getMethod(keyPath(), ERDAssignment.D2WContextClassArray);
            result = m.invoke(this, new Object[] { c });
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException occurred in ERAssignment: " + e.toString() 
            + " keyPath(): " + keyPath() + " target exception: " 
            + e.getTargetException()+ " assignment was " + this + "\n\n" + "Target exception backtrace: "
            + ERXUtilities.stackTrace(e.getTargetException()));
        } catch (Exception e) {
            log.error("Exception occurred in ERDAssignment of class: " + this.getClass().getName() 
            + ": " + e.toString() + " keyPath(): " + keyPath() + " assignment was " + this);
        }
        return result;
	}
	
	public String propertyMessage(D2WContext c) {
		return errorMessage(c);
	}
	
	public String imageID(D2WContext c) {
		if(errorMessage(c) != null) {
			return "stop_icon";
		}
		return null;
	}
	
	public String errorMessage(D2WContext c) {
		//Find the first parent page that collects exceptions
		WOComponent component = (WOComponent)c.valueForKeyPath("session.context.component");
		ERD2WPage page = ERD2WUtilities.enclosingComponentOfClass(component, ERD2WPage.class);
		while(page != null) {
			if(page.shouldCollectValidationExceptions()) {
				break;
			}
			page = (ERD2WPage) ERD2WUtilities.enclosingPageOfClass(page, ERD2WPage.class);
		}
		
		return page == null?null:page.errorMessageForPropertyKey();
	}
}
