//
// BaseComponent.java
//

package com.uw.shared;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

import java.util.*;

public class BaseComponent extends WOComponent {

    protected String message;
    public EOEditingContext localContext = new EOEditingContext();

    public BaseComponent() {
        super();
    }
    
    public BaseComponent (WOContext context) {
        super(context);
    }
        
    public void awake() {
        message = " ";
    }

    public String message() {
        return message;
    }

    public void setMessage(String newMessage) {
        message = newMessage;
    }

    public WOComponent saveChanges() {
        try {
            localContext.lock();
            localContext.saveChanges();
            localContext.invalidateAllObjects();
            message = "Save successful";
        }
        catch (Exception e) {
            message = e.getMessage();
        }
        finally {
            localContext.unlock();
        }
        return null;
    }

    // sort an array passed in on indices passed in through the current request
    public NSArray sortArrayFromRequest(NSArray originalArray) {
        WORequest request = session().context().request();
        NSDictionary sortColumns = request.formValues();

        System.out.println(sortColumns);

        Enumeration objectEnumerator = sortColumns.objectEnumerator();
        NSArray object;

        NSMutableArray sortOrderingArray = new NSMutableArray();

        while (objectEnumerator.hasMoreElements()) {

            object = (NSArray) objectEnumerator.nextElement();

            sortOrderingArray.addObject(new EOSortOrdering((String) object.objectAtIndex(0),EOSortOrdering.CompareAscending));

        }

        return EOSortOrdering.sortedArrayUsingKeyOrderArray(originalArray, sortOrderingArray);
    }
    
}
