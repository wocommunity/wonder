//
// ERNEUListPage.java: Class file for WO Component 'ERNEUListPage'
// Project ERNeutralLook
//
// Created by patrice on Mon Jun 03 2002
//

package er.neutral;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.directtoweb.pages.ERD2WListPage;

import java.util.Enumeration;

public class ERNEUListPage extends ERD2WListPage {

    protected Integer colspan;
    protected String deleteButtonComponentName;


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
    
    
    /**
     * Component to be used to delete button in the list.
     * key to be used is: <code>deleteButtonComponentName</code>
     * @return name of the component used for delete button in this list.
     */
    public String deleteButtonComponentName() {
        if(deleteButtonComponentName == null) {
            deleteButtonComponentName = (String) d2wContext().valueForKey("deleteButtonComponentName");
            //default to ERDTrashcan
            deleteButtonComponentName  = deleteButtonComponentName == null ? "ERDTrashcan" : deleteButtonComponentName;
        }
       
        return deleteButtonComponentName;
    }

}
