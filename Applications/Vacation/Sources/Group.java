//
// Group.java
// Project Vacation
//
// Created by mishra on Mon Nov 04 2002
//

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class Group extends _Group {

    public NSArray sortedPersons() {
        EOSortOrdering userTypeOrdering =  EOSortOrdering.sortOrderingWithKey("type", EOSortOrdering.CompareAscending);
        NSArray sortOrderings = new NSArray(userTypeOrdering);
        return EOSortOrdering.sortedArrayUsingKeyOrderArray(persons(),sortOrderings);

    }

}
