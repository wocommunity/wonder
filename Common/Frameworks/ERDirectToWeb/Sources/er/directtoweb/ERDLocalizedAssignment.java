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

public class ERDLocalizedAssignment extends ERDAssignment implements ERDLocalizableAssignmentInterface, ERDComputingAssignmentInterface {
    static final ERXLogger log = ERXLogger.getLogger(ERDLocalizedAssignment.class);

    public ERDLocalizedAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDLocalizedAssignment (String key, Object value) { super(key,value); }

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDLocalizedAssignment (eokeyvalueunarchiver);
    }

    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "propertyKey"});
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
