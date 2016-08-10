/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;

/**
 * Displays a groups of objects grouped by a key.
 * 
 * @d2wKey groupingKey
 * @d2wKey groupingComponentName
 * @d2wKey groupingItemKey
 * @d2wKey subTask
 */
public class ERD2WGroupingListPage extends ERD2WListPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Public constructor
     * @param context current context
     */
    public ERD2WGroupingListPage(WOContext context) { super(context); }
    
    public NSArray sublist;
    public Object sublistSection;

    // the sorting will come only from the rules
    @Override
    public boolean userPreferencesCanSpecifySorting() { 
        return false;
    }
    public String groupingKey() { 
        return (String)d2wContext().valueForKey("groupingKey");
    }
    public String groupingComponentName() { 
        return (String)d2wContext().valueForKey("groupingComponentName"); 
    }
    public String groupingItemKey() {
        return (String)d2wContext().valueForKey("groupingItemKey");
    }
    public int colspanForNavBar() { 
        return 2*displayPropertyKeys().count()+2; 
    }
    public Object section() { 
        return object().valueForKeyPath(groupingItemKey()); 
    }
    public Object sumForSublist() { 
        return sublist.valueForKey("@sum."+propertyKey()); 
    }
    public void setSublist(NSArray value) {
        NSArray sortOrderings = sortOrderings();
        if(sortOrderings != null &&  sortOrderings.count() > 0) {
            value = EOSortOrdering.sortedArrayUsingKeyOrderArray(value, sortOrderings);
        }
        sublist = value;
    }
    // we don't ever want to batch for printerFriendly
    @Override
    public int numberOfObjectsPerBatch() { 
        return ("printerFriendly".equals(d2wContext().valueForKey("subTask"))? 0 : super.numberOfObjectsPerBatch()); 
    }
}
