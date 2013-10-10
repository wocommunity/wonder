//
//  ERExcelLook.java
//  ERExcelLook
//
//  Created by Max Muller III on Mon Apr 26 2004.
//
package er.directtoweb.excel;

import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXDictionaryUtilities;

/**
 * Principal class of the framework. Not registered yet because it doesn't do anything yet.
 */
public class ERExcelLook {

    /** Caches the styles dictionary */
    protected static NSDictionary styles;

    /**
     * Gets the styles dictionary from the playlist. This is not extensible at the moment
     * perhaps in the future we could provide a way to incorportate all of the styles loaded
     * in a dynamic fashion much the same way that rule files are found and loaded.
     * @return dictionary of excel styles
     */
    public static NSDictionary styles() {
        if (styles == null) {
            styles = ERXDictionaryUtilities.dictionaryFromPropertyList("Styles",
                                                                       NSBundle.bundleForName("ERExcelLook"));
        }
        return styles;
    }
}
