//
// ERXEOControlUtilities.java
// Project ERExtensions
//
// Created by max on Wed Oct 09 2002
//
package er.extensions;

import com.webobjects.foundation.NSArray;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;

/**
 * Collection of EOF utility method centered around
 * EOControl specific utilities.
 */
public class ERXEOControlUtilities {

    /**
     * Turns a given enterprise object back into a fault.
     * @param eo enterprise object to refault
     */
    public static void refaultObject(EOEnterpriseObject eo) {
        if (eo != null && !eo.isFault()) {
            EOEditingContext ec = eo.editingContext();
            NSArray gids = new NSArray(ec.globalIDForObject(eo));
            ec.invalidateObjectsWithGlobalIDs(gids);
        }
    }
}
