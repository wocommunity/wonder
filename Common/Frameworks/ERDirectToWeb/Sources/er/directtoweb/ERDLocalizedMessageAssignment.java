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

// CHECKME: This Assignment does not implement the computing assignment interface,
//		but does implement the method from that interface. Should this class
//		implement the interface?
public class ERDLocalizedMessageAssignment extends ERDDelayedAssignment implements ERDLocalizableAssignmentInterface {

    /** logging supprt */
    static final ERXLogger log = ERXLogger.getERXLogger(ERDLocalizedMessageAssignment.class);

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDLocalizedMessageAssignment (eokeyvalueunarchiver);
    }

    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDLocalizedMessageAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDLocalizedMessageAssignment (String key, Object value) { super(key,value); }

    public ERXLocalizer localizerForContext(D2WContext c) {
        return ERXLocalizer.localizerForSession(c.valueForKey("session"));
    }

    /**
     * Implementation of the {@link ERDComputingAssignmentInterface}. This
     * assignment depends upon the template keys from the value of this assignment. 
     * This array of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) {
        // FIXME: (ak) if we knew that we could get at a valid context, we could use the
        // localized string and it could use different keys for every language. The way
        // things are, all keys for all languages must be in the value()
        // String format = localizerForContext(c).localizedValueForKeyWithDefault((String)value());
        String format = (String)value();
        NSArray dependentKeys = ERXSimpleTemplateParser.sharedInstance().keysInTemplate(format, null);
        if (log.isDebugEnabled())
            log.debug("dependentKeys: " + dependentKeys);
        return dependentKeys;
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
