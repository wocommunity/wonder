package er.directtoweb;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

/**
 * @deprecated, use {@link ERDDelayedLocalizedAssignment}.
 */
public class ERDLocalizedMessageAssignment extends ERDDelayedLocalizedAssignment {
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        ERDAssignment.logDeprecatedMessage(ERDLocalizedMessageAssignment.class, ERDDelayedLocalizedAssignment.class);
        return new ERDLocalizedMessageAssignment (eokeyvalueunarchiver);
    }
    public ERDLocalizedMessageAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDLocalizedMessageAssignment (String key, Object value) { super(key,value); }
}
