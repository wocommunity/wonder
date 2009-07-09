//
// ERNEUPickListPage.java: Class file for WO Component 'ERNEUPickListPage'
// Project ERNeutralLook
//
// Created by bposokho on Mon Oct 07 2002
//
package er.neutral;

import com.webobjects.appserver.*;
import com.webobjects.foundation.NSArray;
import er.directtoweb.*;

public class ERNEUPickListPage extends ERD2WPickListPage {

    public ERNEUPickListPage(WOContext context) {
        super(context);
    }

    public int colSpan() {
        int multiplier = 1;
        if (shouldDisplayDetailedPageMetrics()) {
            multiplier = 2;
        }
        return (((NSArray)d2wContext().valueForKey("displayPropertyKeys")).count() * multiplier) + 3;
    }

}
