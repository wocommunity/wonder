//
// ERD2WEditSortedToManyRelationship.java: Class file for WO Component 'ERD2WEditSortedToManyRelationship'
// Project ERDirectToWeb
//
// Created by bposokho on Thu Sep 19 2002
//

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import er.directtoweb.*;
import er.extensions.*;

public class ERD2WEditSortedToManyFault extends D2WEditToManyFault {

    public ERD2WEditSortedToManyFault(WOContext context) {
        super(context);
    }

    public NSArray sortedBrowserList()
{
        return  ERXArrayUtilities.sortedArraySortedWithKey(browserList(),
                                                           (String)d2wContext().valueForKey("indexKey"),
                                                           null);
}

}