package er.directtoweb.assignments.defaults;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOGenericRecord;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.assignments.ERDAssignment;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXDictionaryUtilities;
import er.extensions.foundation.ERXStringUtilities;

/**
 * An assignment to auto-compute a unique(ish), human-readable DOM ids from the d2wContext for Selenium, CSS, Ajax, Javascript, etc.
 * For Ajax updates you may also use this assignment to compute an updateContainerID key (by setting it to idForSection, idForPageConfiguration, etc. as required).
 *
 * This assignment provids defaults for the following keys:
 * <ul>
 * <li><code>idForProperty</code></li>
 * <li><code>idForSection</code></li>
 * <li><code>idForPageConfiguration</code></li>
 * <li><code>idForEmbeddedPageConfiguration</code></li>
 * <li><code>idForEmbeddedProperty</code></li>
 * <li><code>idForForm</code></li>
 * </ul>
 * 
 * To use: Bind D2W component id binding to d2wContext.id (or d2wContext.idForProperty or d2wContext.idForSection, etc)
 * 
 * You may also override these auto computed id by setting rules for the above keys, if necessary.
 * 
 * @author mendis
 *
 */
public class ERDDefaultIDAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** holds the array of keys this assignment depends upon */
    protected static final NSDictionary keys = ERXDictionaryUtilities.dictionaryWithObjectsAndKeys( new Object [] {
        new NSArray(new Object[] {"propertyKey", "task", "entity.name"}), "idForProperty",
        new NSArray(new Object[] {"pageConfiguration", "task", "entity.name", "sectionKey"}), "idForSection",
        new NSArray(new Object[] {"pageConfiguration", "task", "entity.name"}), "idForPageConfiguration",
        new NSArray(new Object[] {"pageConfiguration", "task", "entity.name", "object"}), "idForEmbeddedPageConfiguration",
        new NSArray(new Object[] {"propertyKey", "task", "entity.name", "object"}), "idForEmbeddedProperty",
        new NSArray(new Object[] {"pageConfiguration", "task", "entity.name"}), "idForForm",
    });

    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This array
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
     // ENHANCEME: Only ever need one of these assignments.
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDefaultIDAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDDefaultIDAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDefaultIDAssignment (String key, Object value) { super(key,value); }
    
    /**
     * a DOM id based on the triple &lt;task, entity, propertyKey&gt;
     * 
     * @param c d2w context
     * @return an id representing the &lt;task, entity, propertyKey&gt;
     * 
     * TODO Maybe change to pageConfig + propertyKey?
     */
    public Object idForProperty(D2WContext c) {
    	return ERXStringUtilities.safeIdentifierName(c.entity().name() + "_" + c.task() + "_" + c.propertyKey());
    }
    
    /**
     * a DOM id based on the triple &lt;task, entity, propertyKey, pk&gt;
     * 
     * @param c d2w context
     * @return an id representing the &lt;task, entity, propertyKey, pk&gt;
     * 
     * TODO Maybe change to pageConfig + propertyKey?
     */
    public Object idForEmbeddedProperty(D2WContext c) {
    	return idForProperty(c) + "_" + ERXEOControlUtilities.primaryKeyStringForObject((EOGenericRecord) c.valueForKey("object"));
    }
    
    /**
     * A DOM id based on the pageConfig
     * 
     * @param c d2w context
     * @return an id representing the &lt;task, entity&gt;
     */
    public Object idForPageConfiguration(D2WContext c) {
    	String _idForPageConfiguration = (c.dynamicPage() != null) ? c.dynamicPage() : c.task() + "_" + c.entity().name();
    	return ERXStringUtilities.safeIdentifierName(_idForPageConfiguration);
    }
    
    /**
     * A DOM id based on the pageConfig + primaryKey
     * 
     * @param c d2w context
     * @return an id representing the &lt;task, entity, pk&gt;
     */
    public Object idForEmbeddedPageConfiguration(D2WContext c) {
    	return idForPageConfiguration(c) + "_" + ERXEOControlUtilities.primaryKeyStringForObject((EOGenericRecord) c.valueForKey("object"));
    }
    
    /**
     * A DOM id based on the pageConfig and sectionKey
     * 
     * @param c d2w context
     * @return an id representing the section in a tab page
     */
    public Object idForSection(D2WContext c) {
    	String sectionKey = (String) c.valueForKey("sectionKey");
    	String section = !"".equals(sectionKey) ? ERXStringUtilities.safeIdentifierName(sectionKey) : "section";
    	return idForPageConfiguration(c) + "_" + section;
    }
    
    /**
     * A DOM id based on the pageConfig 
     * 
     * @param c d2w context
     * @return an id representing the form for the d2w page
     */
    public Object idForForm(D2WContext c) {
    	return idForPageConfiguration(c) + "_form";
    }
}
