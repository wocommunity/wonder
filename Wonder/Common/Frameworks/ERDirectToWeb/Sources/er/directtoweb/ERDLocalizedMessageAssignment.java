//
// ERDDelayedLocalizedAssignment.java
// Project ERDirectToWeb
//
// Created by ak on Wed Apr 17 2002
//
package er.directtoweb;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

public class ERDLocalizedMessageAssignment extends ERDDelayedAssignment  implements ERDLocalizableAssignmentInterface{
    static final ERXLogger log = ERXLogger.getLogger(ERDLocalizedMessageAssignment.class);

    public ERDLocalizedMessageAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDLocalizedMessageAssignment (String key, Object value) { super(key,value); }

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDLocalizedMessageAssignment (eokeyvalueunarchiver);
    }

    public ERXLocalizer localizerForContext(D2WContext c) {
        return ERXLocalizer.localizerForSession(c.valueForKey("session"));
    }

    public NSArray _dependentKeys;
    public NSArray dependentKeys(String keyPath) {
        if (_dependentKeys==null) {
            // FIXME: (ak) if we knew that we could get at a valid context, we could use the
            // localized string and it could use different keys for every language. The way
            // things are, all keys for all languages must be in the value()
            // String format = localizerForContext(c).localizedValueForKeyWithDefault((String)value());
            String format = (String)value();
            _dependentKeys = ERXSimpleTemplateParser.sharedInstance().keysInTemplate(format, null);
            if (log.isDebugEnabled())
                log.debug("dependentKeys: " + _dependentKeys);
        }
        return _dependentKeys;
    }

    public Object fireNow(D2WContext c) {
        String key = (String)value();
        if (log.isDebugEnabled()) {
            String value = localizerForContext(c).localizedStringForKey(key);
            log.debug("Resolving delayed fire for template " + value + " - " + c.valueForKeyPath("displayNameForEntity") + " - " + c + " - " + dependentKeys(""));
        }
        return localizerForContext(c).localizedTemplateStringForKeyWithObject(key, c);
    }
}
