//
// ERNEUListPage.java: Class file for WO Component 'ERNEUListPage'
// Project ERNeutralLook
//
// Created by patrice on Mon Jun 03 2002
//

package er.neutral;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import er.directtoweb.*;

public class ERNEUListPage extends ERD2WListPage {

    public ERNEUListPage(WOContext context) { super(context); }

    public int colSpan() {
        int multiplier = 1;
        if (shouldDisplayDetailedPageMetrics()) {
            multiplier = 2;
        }
        return (((NSArray)d2wContext().valueForKey("displayPropertyKeys")).count() * multiplier) + 2;
    }
}
