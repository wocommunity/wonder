/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ObjectSaveDelegate.java created by max on Fri 27-Apr-2001 */
package er.extensions;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;

// All this guy does is save the editing context of the object and return the next page.
// Useful for confirm pages.
public class ERXObjectSaveDelegate implements NextPageDelegate {
    /** holds the object */
    private EOEnterpriseObject _object;
    /**
     * holds a reference to the objects ec so that it won't be
     * collected by the garbage collector
     */
    private EOEditingContext _context;
    /** holds the next page */
    private WOComponent _nextPage;

    /**
     * returns the object
     */
    protected EOEnterpriseObject object() { return _object; }
    protected EOEditingContext editingContext() { return _context; }
    
    public ERXObjectSaveDelegate(EOEnterpriseObject object, WOComponent nextPage) {
        _object = object;
        if (_object != null)
            _context = _object.editingContext();
        _nextPage = nextPage;
    }

    public WOComponent nextPage(WOComponent sender) {
        if (_context != null && _context.hasChanges())
            _context.saveChanges();
        return _nextPage;
    }
}
