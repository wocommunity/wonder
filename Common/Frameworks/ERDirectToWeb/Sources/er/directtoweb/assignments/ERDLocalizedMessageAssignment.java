package er.directtoweb.assignments;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;

import er.directtoweb.assignments.delayed.ERDDelayedLocalizedAssignment;

/**
 * @deprecated  use {@link ERDDelayedLocalizedAssignment}.
 */
public class ERDLocalizedMessageAssignment extends ERDDelayedLocalizedAssignment {
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        ERDAssignment.logDeprecatedMessage(ERDLocalizedMessageAssignment.class, ERDDelayedLocalizedAssignment.class);
        return new ERDLocalizedMessageAssignment (eokeyvalueunarchiver);
    }
    public ERDLocalizedMessageAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDLocalizedMessageAssignment (String key, Object value) { super(key,value); }
}
