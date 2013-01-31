//
// ERNEUListPage.java: Class file for WO Component 'ERNEUListPage'
// Project ERNeutralLook
//
// Created by patrice on Mon Jun 03 2002
//

package er.neutral;

import java.util.Enumeration;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.directtoweb.pages.ERD2WListPage;

public class ERNEUListPage extends ERD2WListPage {

    protected Integer colspan;

    public ERNEUListPage(WOContext context) { super(context); }

    public int colSpan() {
        if (null == colspan) {
            int numVisibleKeys = 0;
            int multiplier = shouldDisplayDetailedPageMetrics() ? 2 : 1;
            String currentKey = d2wContext().propertyKey(); // Cache the current key.
            NSArray displayPropertyKeys = (NSArray)d2wContext().valueForKey("displayPropertyKeys");
            for (Enumeration keysEnum = displayPropertyKeys.objectEnumerator(); keysEnum.hasMoreElements();) {
                String key = (String)keysEnum.nextElement();
                d2wContext().setPropertyKey(key);
                if (!isKeyOmitted()) {
                    numVisibleKeys++;
                }
            }
            d2wContext().setPropertyKey(currentKey); // Restore the key.
            colspan = (numVisibleKeys * multiplier) + 2;
        }
        return colspan;
    }
}
