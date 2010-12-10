package er.directtoweb.assignments.delayed;
import org.apache.log4j.Logger;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;

import er.directtoweb.assignments.ERDLocalizableAssignmentInterface;
import er.directtoweb.assignments.ERDLocalizedAssignment;
import er.extensions.localization.ERXLocalizer;

/**
 * Same as {@link ERDLocalizedAssignment}, except that firing is delayed.
 */
public class ERDDelayedLocalizedAssignment extends ERDDelayedAssignment implements ERDLocalizableAssignmentInterface {

    /** logging support */
    static final Logger log = Logger.getLogger(ERDDelayedLocalizedAssignment.class);

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
            String value = (String)ERXLocalizer.currentLocalizer().valueForKey(key);
            log.debug("Fire for template \"" + key + "\": " + value);
        }
        return ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject(key, c);
    }
}
