package er.directtoweb.assignments.defaults;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;

/**
 * This assignment calculates default (ajax) page configuration
 * names for the current entity in the context.
 * 
 * Note: As they mirror the default configurations, the assignment class is separate (and a subclass).
 * This means that the default configurations can be overridden in the rules more gracefully.
 * 
 * @see er.directtoweb.assignments.defaults.ERDDefaultConfigurationNameAssignment
 * 
 * @author mendis
 */
public class ERDDefaultAjaxConfigurationNameAssignment extends ERDDefaultConfigurationNameAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDefaultAjaxConfigurationNameAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDDefaultAjaxConfigurationNameAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDefaultAjaxConfigurationNameAssignment (String key, Object value) { super(key,value); }
    
    /**
     * Generates a default ajax edit page configuration
     * based on the current entity name. Default format
     * is 'AjaxEdit' + entity name.
     * @param c current D2W context
     * @return default edit page configuration name
     */
	@Override
    public Object editConfigurationName(D2WContext c) {
    	return "Ajax" + super.editConfigurationName(c);
    }
    
    /**
     * Generates a default ajax inspect page configuration
     * based on the current entity name. Default format
     * is 'AjaxInspect' + entity name.
     * @param c current D2W context
     * @return default inspect page configuration name
     */
	@Override
    public Object inspectConfigurationName(D2WContext c) {
        return "Ajax" + super.inspectConfigurationName(c);
    }
	
    /**
     * Generates a default ajax list page configuration
     * based on the current entity name. Default format
     * is 'AjaxList' + entity name.
     * @param c current D2W context
     * @return default list page configuration name
     */
	@Override
    public Object listConfigurationName(D2WContext c) {
        return "Ajax" +  super.listConfigurationName(c); 
    }
}
