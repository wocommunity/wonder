package er.directtoweb;
import com.webobjects.directtoweb.*;
import com.webobjects.eocontrol.*;

import er.extensions.*;

/**
 * Same as {@link ERDLocalizedAssignment}, except that firing is delayed.
 */
public class ERDDelayedLocalizedAssignment extends ERDDelayedAssignment implements ERDLocalizableAssignmentInterface {

    /** logging support */
    static final ERXLogger log = ERXLogger.getERXLogger(ERDDelayedLocalizedAssignment.class);

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDelayedLocalizedAssignment (eokeyvalueunarchiver);
    }

    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDDelayedLocalizedAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDelayedLocalizedAssignment (String key, Object value) { super(key,value); }

    public Object fireNow(D2WContext c) {
        String key = (String)value();
        if (log.isDebugEnabled()) {
            String value = ERXLocalizer.currentLocalizer().localizedStringForKey(key);
            log.debug("Fire for template \"" + key + "\": " + value);
        }
        return ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject(key, c);
    }
}
