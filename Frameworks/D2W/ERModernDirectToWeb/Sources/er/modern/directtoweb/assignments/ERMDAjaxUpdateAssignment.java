package er.modern.directtoweb.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.assignments.ERDAssignment;
import er.directtoweb.pages.ERD2WPage;
import er.extensions.foundation.ERXArrayUtilities;
import er.modern.directtoweb.components.ERMDAjaxNotificationCenter;

/**
 * Simple assignment that checks whether a property is to be observed for
 * changes or is dependent on another property.
 * 
 * See {@link er.modern.directtoweb.components.ERMDAjaxNotificationCenter}
 * 
 * @d2wKey propertyDependencies
 * @d2wKey propertyKey
 * 
 * @author fpeters
 *
 */
public class ERMDAjaxUpdateAssignment extends ERDAssignment {

    private static final long serialVersionUID = 1L;

    /**
     * Static constructor required by the EOKeyValueUnarchiver interface. If
     * this isn't implemented then the default behavior is to construct the
     * first super class that does implement this method. Very lame.
     * 
     * @param eokeyvalueunarchiver
     *            to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver) {
        return new ERMDAjaxUpdateAssignment(eokeyvalueunarchiver);
    }

    /**
     * Public constructor
     * 
     * @param u
     *            key-value unarchiver used when unarchiving from rule files.
     */
    public ERMDAjaxUpdateAssignment(EOKeyValueUnarchiver u) {
        super(u);
    }

    /**
     * Public constructor
     * 
     * @param key
     *            context key
     * @param value
     *            of the assignment
     */
    public ERMDAjaxUpdateAssignment(String key, Object value) {
        super(key, value);
    }

    /**
     * Implementation of the
     * {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}.
     * 
     * @return empty array.
     */
    @Override
    public NSArray<String> dependentKeys(String keyPath) {
        return new NSArray<String>(ERD2WPage.Keys.propertyKey,
                ERMDAjaxNotificationCenter.PROPERTY_DEPENDENCIES.key());
    }

    /**
     * Checks whether the current property is declared as to be observed via the
     * propertyDependencies key.
     * 
     * @return true if the current property key is to be observed
     */
    public Object shouldObserve(D2WContext context) {
        boolean shouldObserve = false;
        NSDictionary<String, NSArray<String>> propertyDependencies = ERMDAjaxNotificationCenter.PROPERTY_DEPENDENCIES
                .valueInObject(context);
        if (propertyDependencies != null
                && propertyDependencies.containsKey(context.propertyKey())) {
            shouldObserve = true;
        }
        return shouldObserve;
    }

    /**
     * Checks whether the current property is declared dependent via the
     * propertyDependencies key.
     * 
     * @return true if the current property is dependent
     */
    public Object isDependent(D2WContext context) {
        boolean isDependent = false;
        NSDictionary<String, NSArray<String>> propertyDependencies = ERMDAjaxNotificationCenter.PROPERTY_DEPENDENCIES
                .valueInObject(context);
        if (propertyDependencies != null) {
            if (ERXArrayUtilities.flatten(propertyDependencies.allValues()).contains(
                    context.propertyKey())) {
                isDependent = true;
            }
        }
        return isDependent;
    }

}
