//
// ERD2WDisplayDictionary.java: Class file for WO Component 'ERD2WDisplayDictionary'
// Project ERDirectToWeb
//
// Created by jbl on 5/26/05
//

package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayString;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.directtoweb.components.strings.ERD2WDisplayPreformattedString;

/**
 * Displays a dictionary by serializing it as property list.  The dictionary is assumed
 * to only contain objects that are valid for an old-style (i.e., non-XML) plist.
 *
 * @deprecated use {@link ERD2WDisplayPreformattedString}
 */
@Deprecated
public class ERD2WDisplayDictionary extends D2WDisplayString {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WDisplayDictionary(WOContext context) {
        super(context);
    }
    
    public String stringContents() {
        final NSDictionary dictionary = (NSDictionary)objectPropertyValue();
        String result = null;
        
        if ( dictionary != null && dictionary.count() > 0 )
            result = NSPropertyListSerialization.stringFromPropertyList(dictionary);
        
        return result;
    }

}
