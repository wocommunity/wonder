package er.directtoweb.components.repetitions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

/**
 * Grouping list that can be used as a repetition in list pages.
 *
 * @author ak on 31.10.05
 * 
 * @d2wKey justification
 * @d2wKey componentName
 * @d2wKey propertyKey
 * @d2wKey displayPropertyKeys
 * @d2wKey displayNameForEntity
 * @d2wKey totallingKeys
 * @d2wKey propertyNameComponentName
 * @d2wKey formatter
 * @d2wKey groupingKey
 * @d2wKey groupingComponentName
 * @d2wKey object
 */
public class ERDGroupingListPageRepetition extends ERDListPageRepetition {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/** logging support */
	private static final Logger log = LoggerFactory.getLogger(ERDGroupingListPageRepetition.class);
	
	/**
	 * Public constructor
	 * @param context the context
	 */
	public ERDGroupingListPageRepetition(WOContext context) {
		super(context);
	}
	
	/** component does not synchronize it's variables */
	@Override
	public boolean synchronizesVariablesWithBindings() { return false; }
	
	
	public NSArray sublist;
	public Object sublistSection;
	
	public Object object() {
		return d2wContext().valueForKey("object");
	}
	public void setObject(Object value) {
		d2wContext().takeValueForKey(value, "object");
	}
	
	public int colspanForNavBar() { 
    	return 2*displayPropertyKeys().count()+2; 
    }
    public String groupingKey() {
    	return (String)valueForBinding("groupingKey");
    }
    public String groupingItemKey() {
        return (String)valueForBinding("groupingItemKey");
    }
    public Object sumForSublist() { 
        return sublist.valueForKey("@sum."+propertyKey()); 
    }
    public Object section() {
        return ((EOEnterpriseObject)object()).valueForKeyPath(groupingItemKey());
    }
    public boolean userPreferencesCanSpecifySorting() { 
        return false;
    }
    public int numberOfObjectsPerBatch() { 
        return 10000; 
    }

}
