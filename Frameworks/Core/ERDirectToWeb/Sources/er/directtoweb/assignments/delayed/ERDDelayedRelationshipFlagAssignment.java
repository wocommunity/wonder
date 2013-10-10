package er.directtoweb.assignments.delayed;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * Creates the needed values to have dymamic values in a list page repetition. 
 * Answers on displayPropertyKeys and displayNameForProperty.
 * @see er.directtoweb.components.relationships.ERD2WDisplayRelationshipFlag for more info
 * @author ak 
 *
 */
public class ERDDelayedRelationshipFlagAssignment extends ERDDelayedAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDelayedRelationshipFlagAssignment(eokeyvalueunarchiver);
    }
    
    /** Logging support */
    public final static Logger log = Logger.getLogger(ERDDelayedRelationshipFlagAssignment.class);

    public ERDDelayedRelationshipFlagAssignment(EOKeyValueUnarchiver u) { super(u); }
    public ERDDelayedRelationshipFlagAssignment(String key, Object value) { super(key,value); }

    @Override
    public Object fireNow(D2WContext c) {
        String path = keyPath();
        if("displayPropertyKeys".equals(path)) {
            NSArray keys = (NSArray) value();
            NSMutableArray result = new NSMutableArray();
            for (Enumeration enumerator = keys.objectEnumerator(); enumerator.hasMoreElements();) {
                String key = (String) enumerator.nextElement();
                if(key.startsWith("@")) {
                    key = key.substring(1);
                    c.setPropertyKey(key);
                    String keyPath = (String) c.valueForKey("restrictedChoiceKey");
                    NSArray objects = NSArray.EmptyArray;
                    String relationshipKey = (String) c.valueForKey("keyWhenRelationship");
                    if(keyPath != null) {
                        objects = (NSArray) c.valueForKeyPath(keyPath + "." + relationshipKey +".@flatten.@unique");
                    }
                    if(objects != null) {
                        for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();) {
                            Object o = e.nextElement();
                            result.addObject(key + ".@" + o);
                        }
                    }
                    c.setPropertyKey(null);
                } else {
                    result.addObject(key);
                }
            }
            return result;
        } else if("displayNameForProperty".equals(path)) {
            String propertyKey = c.propertyKey();
            if(propertyKey != null) {
                return propertyKey.substring(propertyKey.indexOf("@")+1);
            }
        }
        return null;
    }

}
