/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.components.ERXDateGrouper;

/**
 * Superclass of all calendar list pages.
 * <p>
 * Note that they are not compatible with the NetStruxr version.
 * Using a {@link ERXDateGrouper} as the display group, most of the logic is gone from this page.
 */
public class ERD2WCalendarPage extends ERD2WListPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WCalendarPage(WOContext c) {
        super(c);
    }

    @Override
    public WODisplayGroup displayGroup() {
        if(_displayGroup == null) {
            ERXDateGrouper grouper = new ERXDateGrouper();
            _displayGroup = grouper;
            NSArray sortOrderings = sortOrderings();
            if(sortOrderings.count() > 0) {
                Object o = sortOrderings.objectAtIndex(0);
                if(o instanceof EOSortOrdering) {
                    grouper.setDateKeyPath(((EOSortOrdering)o).key());
                }
            }
        }
        return super.displayGroup();
    }
    
    public ERXDateGrouper grouper() {
        return (ERXDateGrouper)displayGroup();
    }

    @Override
    public int numberOfObjectsPerBatch() {
        return 0;	// we want all the objects in one batch
    }

    public NSTimestamp selectedDate() {
        return grouper().selectedDate();
    }
    public void setSelectedDate(NSTimestamp value) {
        grouper().setSelectedDate(value);
        session().setObjectForKey(grouper().selectedDate(), "selectedDate");
    }

    public WOComponent selectDateAction() {
        setSelectedDate(selectedDate());
        return context().page();
    }
}
