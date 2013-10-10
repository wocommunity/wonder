package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayBoolean;
import com.webobjects.foundation.NSArray;
/**
 * Allows for a flag if an object has a given related object. Given a "User" with a "groups" 
 * relationship to a "Group" entity, you can have a list page for your users and tabular display
 * if a user is in a group or not. You need some rules like: 
 * <code><pre>
 * 100 : (task = 'list') and (entity.name = "User") => displayPropertyKeys = (name, "groups.@Admin", "groups.@Accounting")
 * 100 : (task = 'list') and (entity.name = "User") and (propertyKey like 'groups.*') => componentName = "ERD2WDisplayRelationshipFlag"
 * 100 : (entity.name = 'Group') => keyWhenRelationship = "name"
 * 100 : (propertyKey = 'groups.@Admin') => displayNameForProperty = "Admin"
 * 100 : (propertyKey = 'groups.@Accounting') => displayNameForProperty = "Accounting"
 * </pre></code>
 * However, this might be too much work, as you need to change the rules anytime you add or remove 
 * from the Group entity. To automatically display possible values from the Group entity, use 
 * the ERDDelayedRelationshipFlagAssignment: 
 * <code><pre>
 * 100 : (task = 'list') and (entity.name = "User") => displayPropertyKeys = (name, "@groups") [er.directtoweb.ERDDelayedRelationshipFlagAssignment]
 * 100 : (task = 'list') and (entity.name = "User") and (propertyKey like 'groups.*') => componentName = "ERD2WDisplayRelationshipFlag"
 * 100 : (entity.name = 'Group') => keyWhenRelationship = "name"
 * 100 : (propertyKey like 'groups.@*') => displayNameForProperty = '&lt;computed&gt;' [er.directtoweb.ERDDelayedRelationshipFlagAssignment]
 * 100 : (propertyKey like 'groups.@*') => restrictedChoiceKey = "session.possibleGroups"
 * </pre></code>
 * The "@" in the displayPropertyKeys rule is the flag that tells the assignment which relationship to
 * expand. Together with a method that returns the candidates for the Group entity in the Session class, you now have an 
 * automatic display of boolean flags:
 * <code><pre>
 * Name   |  Admin   | Accounting | Marketing
 * ------------------------------------------
 * Fred   |   [x]    |    [ ]     |   [ ]    
 * Carl   |   [ ]    |    [x]     |   [x]    
 * Suzi   |   [x]    |    [ ]     |   [ ]    
 * </pre></code>
 * Note that the ERDDelayedRelationshipFlagAssignment gets called up pretty often, so you might need to make your 
 * candidate method in the session cache the keys.
 * 
 * @author ak
 * @d2wKey keyWhenRelationship
 */
public class ERD2WDisplayRelationshipFlag extends D2WDisplayBoolean {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WDisplayRelationshipFlag(WOContext context) {
        super(context);
    }

    @Override
    public Object objectPropertyValue() {
        String propertyKey = d2wContext().propertyKey();
        int index = propertyKey.indexOf(".@");
        if(index > -1) {
            String keyPath = propertyKey.substring(0, index);
            String value = propertyKey.substring(index+2);
            d2wContext().setPropertyKey(propertyKey);
            Object o = object().valueForKeyPath(keyPath + "." + d2wContext().valueForKey("keyWhenRelationship"));
            d2wContext().setPropertyKey(propertyKey);
            if (o instanceof NSArray) {
                NSArray array = (NSArray) o;
                return array.containsObject(value) ? Boolean.TRUE : Boolean.FALSE;
            }
            return (o != null && o.equals(value) ? Boolean.TRUE : Boolean.FALSE);
        }
        return Boolean.FALSE;
    }
}
