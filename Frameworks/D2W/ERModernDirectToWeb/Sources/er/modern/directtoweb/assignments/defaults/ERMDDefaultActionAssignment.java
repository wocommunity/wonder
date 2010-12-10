package er.modern.directtoweb.assignments.defaults;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.assignments.defaults.ERDDefaultActionAssignment;

/**
 * Custom default action assignment class to return the left actions in their correct
 * order
 * 
 * @author davidleber
 *
 */
public class ERMDDefaultActionAssignment extends ERDDefaultActionAssignment{

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERMDDefaultActionAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERMDDefaultActionAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERMDDefaultActionAssignment (String key, Object value) { super(key,value); }
    
    /**
     * Calculates the default left actions names for a given context.
     * The array is set according to whether the entity is editable, inspectable and printable.
     * FIXME - there is probably a nicer way to do this, but the design expects the order of left actions
     * to be inspect, edit. Which is backwards from the ERDDefaultActionAssignment
     * @param c a D2W context
     * @return array of action names for that context.
     */
    @Override
    public NSArray defaultLeftActions(D2WContext c) {
        NSMutableArray actions = new NSMutableArray();
        if(booleanContextValueForKey(c, "isEntityInspectable", false))
            actions.addObject("inspectAction");
        if(booleanContextValueForKey(c, "isEntityEditable", false) || booleanContextValueForKey(c, "readOnly", true))
            actions.addObject("editAction");
        if(booleanContextValueForKey(c, "isEntityPrintable", false))
            actions.addObject("printAction");
        return actions;
    }
    
}
