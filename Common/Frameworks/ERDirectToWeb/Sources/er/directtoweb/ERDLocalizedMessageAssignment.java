package er.directtoweb;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;

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
