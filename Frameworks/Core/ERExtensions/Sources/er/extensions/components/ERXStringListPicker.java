/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * Useful for picking a String from a list.
 * 
 * @binding item 
 * @binding selection
 * @binding explanationComponentName
 * @binding choices List (NSDictionary) containing the list of string to display
 * @binding cancelPage A WOComponent that is used when people click on Cancel button
 * @binding nextPage A WOComponent that is used when people click on Next button
 */

public class ERXStringListPicker extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXStringListPicker(WOContext aContext) {
        super(aContext);
    }

    public Object item, _selection;
    public String explanationComponentName;
    public NSDictionary choices;
    public WOComponent cancelPage, nextPage;
    

    private NSArray _list;
    public NSArray list() {
        if (_list == null) {
            _list = EOSortOrdering.sortedArrayUsingKeyOrderArray(choices.allKeys(),
                                                         new NSArray(EOSortOrdering.sortOrderingWithKey("toString", EOSortOrdering.CompareAscending)));
        }
        return _list;
    }

    public Object selection() {
        if (_selection==null && list().count() > 0)
                _selection = list().objectAtIndex(0);
        return _selection;
    }
    
    public String entityNameForNewInstances() { return (String)choices.objectForKey(_selection); }
    
    public WOComponent next() { return nextPage; }
    public WOComponent cancel() { return cancelPage; }
}
