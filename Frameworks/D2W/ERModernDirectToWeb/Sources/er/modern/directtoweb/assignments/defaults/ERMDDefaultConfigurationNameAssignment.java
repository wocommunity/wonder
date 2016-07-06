package er.modern.directtoweb.assignments.defaults;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.assignments.defaults.ERDDefaultConfigurationNameAssignment;
import er.extensions.foundation.ERXDictionaryUtilities;

/**
 * This assignment calculates default page configuration
 * names for the current entity in the context.
 */
public class ERMDDefaultConfigurationNameAssignment extends ERDDefaultConfigurationNameAssignment {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	protected static final NSDictionary<String, Object> keys = ERXDictionaryUtilities.dictionaryWithObjectsAndKeys( new Object [] {
			new NSArray<String>(new String[] {"propertyKey", "object.entityName", "entity.name"}), "queryEmbeddedConfigurationName",
			new NSArray<String>(new String[] {"propertyKey", "object.entityName", "entity.name"}), "createEmbeddedConfigurationName",
			new NSArray<String>(new String[] {"propertyKey", "object.entityName", "entity.name"}), "selectEmbeddedConfigurationName",
			new NSArray<String>(new String[] {"propertyKey", "object.entityName", "entity.name"}), "pickEmbeddedConfigurationName",
			new NSArray<String>(new String[] {"propertyKey", "subTask", "object.entityName", "entity.name"}), "editListEmbeddedConfigurationName",
			new NSArray<String>(new String[] {"propertyKey", "object.entityName", "entity.name"}), "editRelationshipEmbeddedConfigurationName",
			new NSArray<String>(new String[] {"propertyKey", "object.entityName", "entity.name", "inlineTask"}), "inlineConfigurationName"
			});
	
    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERMDDefaultConfigurationNameAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERMDDefaultConfigurationNameAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERMDDefaultConfigurationNameAssignment (String key, Object value) { super(key,value); }

    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon the context keys: "entity.name" and 
     * "object.entityName". This array of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    @Override
    @SuppressWarnings("unchecked")
	public NSArray<String> dependentKeys(String keyPath) { 
    	return (NSArray<String>)keys.valueForKeyPath(keyPath);
    }
    
    /**
     * Generates a default embedded query page configuration
     * based on the current entity name. Default format
     * is 'EditEmbedded' + entity name.
     * @param c current D2W context
     * @return default edit page configuration name
     */
    public Object queryEmbeddedConfigurationName(D2WContext c) {
    	return "QueryEmbedded" + entityNameForContext(c);
    }
    
    /**
     * Generates a default embedded create page configuration
     * based on the current entity name. Default format
     * is 'CreateEmbedded' + entity name.
     * @param c current D2W context
     * @return default edit page configuration name
     */
    @Override
    public Object createEmbeddedConfigurationName(D2WContext c) {
    	return "CreateEmbedded" + entityNameForContext(c);
    }
    
    /**
     * Generates a default embedded create page configuration
     * based on the current entity name. Default format
     * is 'SelectEmbedded' + entity name.
     * @param c current D2W context
     * @return default edit page configuration name
     */
    public Object selectEmbeddedConfigurationName(D2WContext c) {
    	return "SelectEmbedded" + entityNameForContext(c);
    }
    
    /**
     * Generates a default embedded edit list page configuration
     * based on the current entity name. Default format
     * is 'EditListEmbedded' + entity name.
     * @param c current D2W context
     * @return default edit list page configuration name
     */
    public Object editListEmbeddedConfigurationName(D2WContext c) {
    	return "EditListEmbedded" + entityNameForContext(c);
    }
    
    /**
     * Generates a default embedded create page configuration
     * based on the current entity name. Default format
     * is 'PickEmbedded' + entity name.
     * @param c current D2W context
     * @return default edit page configuration name
     */
    public Object pickEmbeddedConfigurationName(D2WContext c) {
    	return "PickEmbedded" + entityNameForContext(c);
    }
    
    /**
     * Generates a default embedded create page configuration
     * based on the current entity name. Default format
     * is 'EditRelationshipEmbedded' + entity name.
     * @param c current D2W context
     * @return default edit page configuration name
     */
    @Override
    public Object editRelationshipEmbeddedConfigurationName(D2WContext c) {
    	return "EditRelationshipEmbedded" + entityNameForContext(c);
    }
    
    /**
     * Generates a default embedded create page configuration
     * based on the current entity name. Default format
     * is 'CreateEmbedded' + entity name.
     * @param c current D2W context
     * @return default edit page configuration name
     */
    public String inlineConfigurationName(D2WContext c) {
    	String inlineTask = (String)c.valueForKey("inlineTask");
    	String entityName = entityNameForContext(c);
    	String result = null;
    	if ("inspect".equals(inlineTask)) result = "InspectEmbedded" + entityName;
    	if ("edit".equals(inlineTask)) result = "EditEmbedded" + entityName;
    	if ("create".equals(inlineTask)) result = "CreateEmbedded" + entityName;
    	if ("query".equals(inlineTask)) result = "QueryEmbedded" + entityName;
    	return result;
    }
}
