//
// ERD2WDisplayDictionary.java: Class file for WO Component 'ERD2WDisplayDictionary'
// Project ERDirectToWeb
//
// Created by jbl on 5/26/05
//

package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayString;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

/**
 * Displays a dictionary by serializing it as property list.  The dictionary is assumed
 * to only contain objects that are valid for an old-style (i.e., non-XML) plist.
 *
 * @deprecated Use {@link ERD2WDisplayPreformattedString} instead.
 */
public class ERD2WDisplayDictionary extends D2WDisplayString {

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
