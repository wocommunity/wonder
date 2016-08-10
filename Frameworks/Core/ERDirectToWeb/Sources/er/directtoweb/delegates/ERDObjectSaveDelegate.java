/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

/**
 * Simple {@link com.webobjects.directtoweb.NextPageDelegate}
 * implementation that saves the editing context of an enterprise
 * object before returning the next page. This can be particularly
 * handy for example if you want a user to confirm an action before
 * the editing context is committed, for example:<br>
 * Assume that you have a User object that has been edited in
 * a peer context and now you want the user to confirm that they
 * really should save the changes to the editing context, you
 * could have this method:
 * <pre><code>
 * public WOComponent confirmSave() {
 *      ConfirmPageInterface cpi = (ConfirmPageInterface)D2W.factory().pageForConfigurationNamed("ConfirmSaveUserChanges", session());
 *      cpi.setConfirmDelegate(new ERXObjectSaveDelegate(user, context().page()));
 *      cpi.setCancelDelegate(someCancelDelegate);
 *	return (WOComponent)cpi;
 * }</code></pre>
 * This way if the user selects the confirm button the editing context
 * will be saved and they will be brought back to the current page.
 */
public class ERDObjectSaveDelegate implements NextPageDelegate {
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
     * Public constructor
     * @param object to be saved
     * @param nextPage to be returned
     */
    public ERDObjectSaveDelegate(EOEnterpriseObject object, WOComponent nextPage) {
        _object = object;
        if (_object != null)
            _context = _object.editingContext();
        _nextPage = nextPage;
    }
    
    /**
     * returns the object. Useful for subclasses
     * @return the object
     */
    protected EOEnterpriseObject object() { return _object; }
    /**
     * returns the editing context of the object.
     * Useful for subclasses
     * @return the editing context
     */
    protected EOEditingContext editingContext() { return _context; }

    /**
     * Implementation of the NextPageDelegate interface
     * First saves the changes in the object's editing
     * context and then returns the nextPage.
     * @param sender component calling the delegate
     * @return the nextPage component passed in via the
     *		constructor.
     */
    public WOComponent nextPage(WOComponent sender) {
        if (_context != null && _context.hasChanges())
            _context.saveChanges();
        return _nextPage;
    }
}
