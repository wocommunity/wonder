package er.directtoweb;

import java.util.*;

import com.webobjects.directtoweb.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import er.extensions.*;

/**
 * Beautify the display names for the various keys in D2W a better way.<br />
 * @author ak
 */

public class ERDDefaultDisplayNameAssignment extends ERDAssignment implements ERDLocalizableAssignmentInterface {

    /** logging support */
    static final ERXLogger log = ERXLogger.getERXLogger(ERDDefaultDisplayNameAssignment.class);

    /** holds the dependent keys of the assignment */
    protected static final NSDictionary keys = ERXDictionaryUtilities.dictionaryWithObjectsAndKeys( new Object [] {
        new NSArray(new Object[] {"pageConfiguration", "task", "entity.name"}), "displayNameForPageConfiguration",
        new NSArray(new Object[] {"entity", "object.entityName"}), "displayNameForEntity",
        new NSArray(new Object[] {"sectionKey"}), "displayNameForSectionKey",
        new NSArray(new Object[] {"tabKey"}), "displayNameForTabKey",
        new NSArray(new Object[] {"propertyKey"}), "displayNameForProperty",
        new NSArray(new Object[] {"propertyKey"}), "displayNameForKeyPath",
        //new NSArray(new Object[] {"destinationEntityName"}), "displayNameForDestinationEntity",
        new NSArray(new Object[] {"smartRelationship.destinationEntity", "destinationEntityName"}), "displayNameForDestinationEntity",
        new NSArray(new Object[] {"editConfigurationName"}), "displayNameForEditConfiguration",
        new NSArray(new Object[] {"inspectConfigurationName"}), "displayNameForInspectConfiguration",
        new NSArray(new Object[] {"createConfigurationName"}), "displayNameForCreateConfiguration",
        new NSArray(new Object[] {"confirmDeleteConfigurationName"}), "displayNameForConfirmDeleteConfiguration"
    });

    /**
     * Implementation of the {@link ERDComputingAssignmentInterface}. This array
     * of keys is used when constructing the
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for.
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) {
        return (NSArray)keys.valueForKey(keyPath);
    }


    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDefaultDisplayNameAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */    
    public ERDDefaultDisplayNameAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDefaultDisplayNameAssignment (String key, Object value) { super(key,value); }

    /** Helper to get pull the value, pretty-print it and run it through the localizer. */
    protected Object localizedValueForDisplayNameOfKeyPath(String keyPath, D2WContext c) {
        String result = ERXStringUtilities.displayNameForKey((String)c.valueForKeyPath(keyPath));
        if(result != null) {
            result = ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(result);
            return result;
        }
        return null;
    }

    /** @return a beautified, localized display name for the current <code>propertyKey</code> */
    public Object displayNameForProperty(D2WContext c) {
        return localizedValueForDisplayNameOfKeyPath("propertyKey", c);
    }

    /** @return a beautified, localized display name for the key path of the current <code>propertyKey</code> */
    public Object displayNameForKeyPath(D2WContext c) {
        String keyPath = (String)c.valueForKey("propertyKey");
        if(keyPath != null) {
            String result = "";
            for(Enumeration parts = NSArray.componentsSeparatedByString(keyPath, ".").objectEnumerator(); parts.hasMoreElements(); ) {
                String key = (String)parts.nextElement();
                String displayName = ERXStringUtilities.displayNameForKey(key);
                if(displayName != null)
                    result += displayName;
                if(parts.hasMoreElements())
                    result += " ";
            }
            return ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(result);
        }
        return null;
    }

    /** @return a beautified, localized display name for the current <code>entity.name</code> */
    public Object displayNameForEntity(D2WContext c) {
        return localizedValueForDisplayNameOfKeyPath("entity.name", c);
    }

    /** @return a beautified, localized display name for the current <code>destinationEntity</code> */
    public Object displayNameForDestinationEntity(D2WContext c) {
        Object result = null;
//        if(true) return null;
        EOEntity destinationEntity = (EOEntity)c.valueForKeyPath("smartRelationship.destinationEntity");
        //destinationEntity = (EOEntity)c.valueForKeyPath("destinationEntity");
        if(destinationEntity != null) {
            EOEntity entity = (EOEntity)c.valueForKey("entity");
            c.takeValueForKey(destinationEntity, "entity");
            result = c.valueForKey("displayNameForEntity");
            c.takeValueForKey(entity, "entity");
        } 
        return result;
    }

    /** @return a beautified, localized display name for the current <code>sectionKey</code> */
    public Object displayNameForSectionKey(D2WContext c) {
        return localizedValueForDisplayNameOfKeyPath("sectionKey", c);
    }

    /** @return a beautified, localized display name for the current <code>tabKey</code> */
    public Object displayNameForTabKey(D2WContext c) {
        return localizedValueForDisplayNameOfKeyPath("tabKey", c);
    }

    /** @return a beautified, localized display name for the current <code>pageConfiguration</code> */
    public Object displayNameForPageConfiguration(D2WContext c) {
        String pageConfiguration = (String)c.valueForKey("pageConfiguration");
        // do we have task__entityName?
        if(pageConfiguration == null) {
        	return "";
        }
        if(pageConfiguration.indexOf("__") == 0) {
            String taskName = (String)c.valueForKey("task");
            if(taskName == null) {
                taskName = pageConfiguration.substring(2,pageConfiguration.indexOf("__",3));
            }
            String entityName = (String)c.valueForKeyPath("entity.name");
            log.debug(pageConfiguration + ": task=" + taskName +  ", entity=" + entityName);
            pageConfiguration = taskName.substring(0,1).toUpperCase() + taskName.substring(1) + (entityName != null ? entityName : "");
        }
        String value = pageConfiguration;
        if(value != null && value.length() > 0) {
            value = ERXStringUtilities.displayNameForKey(pageConfiguration);
        }
        return localizedValueForKeyWithDefaultInContext(value, c);
    }

    /** @return a beautified, localized display name for the current <code>editConfigurationName</code> */
    public Object displayNameForEditConfiguration(D2WContext c) {
        return localizedValueForDisplayNameOfKeyPath("editConfigurationName", c);
    }

    /** @return a beautified, localized display name for the current <code>inspectConfigurationName</code> */
    public Object displayNameForInspectConfiguration(D2WContext c) {
        return localizedValueForDisplayNameOfKeyPath("inspectConfigurationName", c);
    }

    /** @return a beautified, localized display name for the current <code>createConfigurationName</code> */
    public Object displayNameForCreateConfiguration(D2WContext c) {
        return localizedValueForDisplayNameOfKeyPath("createConfigurationName", c);
    }
    
    /** @return a beautified, localized display name for the current <code>confirmDeleteConfigurationName</code> */
    public Object displayNameForConfirmDeleteConfiguration(D2WContext c) {
        return localizedValueForDisplayNameOfKeyPath("confirmDeleteConfigurationName", c);
    }
}
