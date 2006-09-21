//
// ERNEUListPage.java: Class file for WO Component 'ERNEUListPage'
// Project ERNeutralLook
//
// Created by patrice on Mon Jun 03 2002
//

package er.neutral;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.directtoweb.ERD2WListPage;

public class ERNEUListPage extends ERD2WListPage {

    public ERNEUListPage(WOContext context) { super(context); }

    public int colSpan() {
        return ((NSArray)d2wContext().valueForKey("displayPropertyKeys")).count() + 2;
    }
}
