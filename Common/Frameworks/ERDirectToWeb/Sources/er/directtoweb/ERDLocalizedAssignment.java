//
// ERDLocalizabledAssignment.java
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

public class ERDLocalizedAssignment extends ERDAssignment implements ERDLocalizableAssignmentInterface {

    /** logging supprt */
    static final ERXLogger log = ERXLogger.getLogger(ERDLocalizedAssignment.class);

    /** holds the dependent keys of the assignment */
    protected static final NSArray _DEPENDENT_KEYS=new NSArray("propertyKey");

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDLocalizedAssignment (eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDLocalizedAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDLocalizedAssignment (String key, Object value) { super(key,value); }

    /**
     * Implementation of the {@link ERDComputingAssignmentInterface}. This
     * assignment depends upon the context key: "propertyKey". This key 
     * is used when constructing the significant keys for the passed
     * in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) {
        return _DEPENDENT_KEYS;
    }
    public Object localizedValueForKey(D2WContext c) {
        return localizedValueForKeyInContext((String)value(), c);
    }
    public Object localizedValueForKeyWithDefault(D2WContext c) {
        return localizedValueForKeyWithDefaultInContext((String)value(), c);
    }
    public Object localizedValueForKeyPath(D2WContext c) {
        return localizerForContext(c).valueForKeyPath((String)value());
    }
    public Object displayNameForProperty(D2WContext c) {
        return localizedValueForKeyWithDefaultInContext((String)value(), c);
    }
    public Object displayNameForEntity(D2WContext c) {
        return localizedValueForKeyWithDefaultInContext((String)value(), c);
    }
    public ERXLocalizer localizerForContext(D2WContext c) {
        return ERXLocalizer.localizerForSession(c.valueForKey("session"));
    }
    
    public Object fire(D2WContext c) {
        return localizerForContext(c).localizedValueForKeyWithDefault((String)value());
    }
}
