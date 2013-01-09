package er.directtoweb.assignments;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;

import er.directtoweb.assignments.delayed.ERDDelayedLocalizedAssignment;

/**
 * @deprecated  use {@link er.directtoweb.assignments.delayed.ERDDelayedLocalizedAssignment}
 */
@Deprecated
public class ERDLocalizedMessageAssignment extends ERDDelayedLocalizedAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        ERDAssignment.logDeprecatedMessage(ERDLocalizedMessageAssignment.class, ERDDelayedLocalizedAssignment.class);
        return new ERDLocalizedMessageAssignment (eokeyvalueunarchiver);
    }
    public ERDLocalizedMessageAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDLocalizedMessageAssignment (String key, Object value) { super(key,value); }
}
