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
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

public class ERDLocalizedAssignment extends ERDAssignment implements ERDLocalizableAssignmentInterface {

    /** logging supprt */
    static final ERXLogger log = ERXLogger.getERXLogger(ERDLocalizedAssignment.class);

    /** holds the dependent keys of the assignment */
    protected static final NSArray _pageConfiguration_Keys = new NSArray(new Object[]{"pageConfiguration", "task", "entity.name"});
    protected static final NSArray _entityName_Keys = new NSArray("entity.name");
    protected static final NSArray _propertyKey_Keys = new NSArray("propertyKey");
    protected static final NSArray _sectionKey_Keys = new NSArray("sectionKey");
    protected static final NSArray _tabKey_Keys = new NSArray("tabKey");
    protected static final NSArray _destinationEntity_Keys = new NSArray(new Object[]{ "object.entity", "propertyKey"});

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
        if(keyPath.equals("displayNameForProperty")) {
            return _propertyKey_Keys;
        } else if(keyPath.equals("displayNameForEntity")) {
            return _entityName_Keys;
        } else if(keyPath.equals("displayNameForDestinationEntity")) {
            return _destinationEntity_Keys;
        } else if(keyPath.equals("displayNameForSectionKey")) {
            return _sectionKey_Keys;
        } else if(keyPath.equals("displayNameForTabKey")) {
            return _tabKey_Keys;
        } else if(keyPath.equals("displayNameForPageConfiguration")) {
            return _pageConfiguration_Keys;
        }
        return NSArray.EmptyArray;
    }
    protected Object localizedValueForDisplayNameOfKeyPath(String keyPath, D2WContext c) {
        String result = ERXStringUtilities.displayNameForKey((String)c.valueForKeyPath(keyPath));
        return localizerForContext(c).localizedStringForKeyWithDefault(result);
    }
    public Object displayNameForProperty(D2WContext c) {
        return localizedValueForDisplayNameOfKeyPath("propertyKey", c);
    }
    public Object displayNameForEntity(D2WContext c) {
        return localizedValueForDisplayNameOfKeyPath("entity.name", c);
    }
    public Object displayNameForDestinationEntity(D2WContext c) {
        Object result = null;
        EOEntity destinationEntity = (EOEntity)c.valueForKeyPath("smartRelationship.destinationEntity");
        if(destinationEntity != null) {
            EOEntity entity = (EOEntity)c.valueForKey("entity");
            c.takeValueForKey(destinationEntity, "entity");
            result = c.valueForKey("displayNameForEntity");
            c.takeValueForKey(entity, "entity");
        }
        return result;
    }
    
    public Object displayNameForPageConfiguration(D2WContext c) {
        String pageConfiguration = (String)c.valueForKey("pageConfiguration");
        // do we have task__entityName?
        if(pageConfiguration.indexOf("__") == 0) {
            String taskName = (String)c.valueForKey("task");
            String entityName = (String)c.valueForKeyPath("entity.name");
            pageConfiguration = taskName.substring(0,1).toUpperCase() + taskName.substring(1) + entityName;
        }
        String value = pageConfiguration;
        if(value != null && value.length() > 0) {
            value = ERXStringUtilities.displayNameForKey(pageConfiguration);
        }
        return localizedValueForKeyWithDefaultInContext(value, c);
    }
    public Object displayNameForSectionKey(D2WContext c) {
        return localizedValueForDisplayNameOfKeyPath("sectionKey", c);
    }
    public Object displayNameForTabKey(D2WContext c) {
        return localizedValueForDisplayNameOfKeyPath("tabKey", c);
    }
    public ERXLocalizer localizerForContext(D2WContext c) {
        return ERXLocalizer.localizerForSession(c.valueForKey("session"));
    }
    public Object fire(D2WContext c) {
        Object result = super.fire(c);
        log.info(keyForMethodLookup(c) + "-" + result);
        return result;
    }
}
