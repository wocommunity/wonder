//
// ERExcelListPage.java: Class file for WO Component 'ERExcelListPage'
// Project ERExcelLook
//
// Created by max on Mon Apr 26 2004
//
package er.directtoweb.excel;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import er.directtoweb.*;

public class ERExcelListPage extends ERD2WListPage {

    public ERExcelListPage(WOContext context) {
        super(context);
    }

    public NSDictionary styles() { return ERExcelLook.styles(); }

    public NSArray objectsForSheet() {
        NSArray objectsForSheet = displayGroup().allObjects();
        NSArray sortOrderings = displayGroup().sortOrderings();
        if (sortOrderings != null && sortOrderings.count() > 0) {
            objectsForSheet = EOSortOrdering.sortedArrayUsingKeyOrderArray(objectsForSheet, sortOrderings);
        }
        return objectsForSheet;
    }
}
