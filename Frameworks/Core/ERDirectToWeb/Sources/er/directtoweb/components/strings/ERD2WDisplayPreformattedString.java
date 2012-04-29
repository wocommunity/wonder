//
// ERD2WDisplayPreformattedString.java: Class file for WO Component 'ERD2WDisplayPreformattedString'
// Project ERDirectToWeb
//
// Created by jclites on 3/22/07
//

package er.directtoweb.components.strings;

import java.util.regex.Pattern;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayString;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

/**
 * Displays string representation of object inside of "pre" tags.
 * keyWhenRelationship is respected if the object is an EOEnterpriseObject.
 * NSDictionary and NSArray objects are displayed using NSPropertyListSerialization.
 */
public class ERD2WDisplayPreformattedString extends D2WDisplayString {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WDisplayPreformattedString(WOContext context) {
        super(context);
    }

    public String stringContents() {
        Object object = objectPropertyValue();
        String result = null;
        String keyWhenRelationship = null;

        if( (object instanceof EOEnterpriseObject) && (keyWhenRelationship = keyWhenRelationship()) != null ) {
            object = ((EOEnterpriseObject)object).valueForKeyPath(keyWhenRelationship);
        }

        if( (object instanceof NSDictionary) || (object instanceof NSArray) ) {
            result = NSPropertyListSerialization.stringFromPropertyList(object);

            if( result != null ) { //replace tabs in plist by 4 spaces, for nicer display
                result = Pattern.compile("\t").matcher(result).replaceAll("    ");
            }
        } else if( object != null ) {
            result = object.toString();
        }

        return result;
    }

}
