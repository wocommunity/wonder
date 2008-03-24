/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

/**
 * Delete used after confirming a delete action.<br />
 * 
 */

public class ERDDeletionDelegate implements NextPageDelegate {

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger("er.directtoweb.delegates.ERDDeletionDelegate");

    private EOEditingContext _ec;
    private EOEnterpriseObject _object;
    private EODataSource _dataSource;
    private WOComponent _followPage;

    // Can be overridden in subclasses to provide different followPages.
    protected WOComponent followPage(WOComponent sender) {
        log.debug("In FollowPage");
        return _followPage;
    }
    
    public ERDDeletionDelegate(EOEnterpriseObject object, WOComponent nextPage) {
        this(object, null, nextPage);
    }
    
    public ERDDeletionDelegate(EOEnterpriseObject object, EODataSource dataSource,WOComponent nextPage) {
        _object=object; _dataSource=dataSource; _followPage=nextPage;
        if (_object != null)
            _ec = _object.editingContext();
    }
    public WOComponent nextPage(WOComponent sender) {
        if (_object!=null && _object.editingContext()!=null) {
            EOEditingContext editingContext=_object.editingContext();
            try {
                if (_dataSource != null)
                    _dataSource.deleteObject(_object);
                if (editingContext instanceof EOSharedEditingContext) {
                    if (ERXExtensions.isNewObject(_object)) {
                        log.error("found a new object in a shared editing context "+editingContext);
                        editingContext.deleteObject(_object);
                    } else {
                        //fault the eo into another ec, one cannot delete objects in an shared editing context
                        EOEditingContext ec = ERXEC.newEditingContext();
                        ec.lock();
                        try {
                            ec.setSharedEditingContext(null);
                            _object = EOUtilities.localInstanceOfObject(ec, _object);
                            ec.deleteObject(_object);
                            ec.saveChanges();
                        } finally {
                            ec.unlock();
                            ec.dispose();
                        }
                    }
                } else {
                    editingContext.deleteObject(_object);
                    if (ERXExtensions.isNewObject(_object)) {
                        // This is necessary to force state synching, e.g., for display groups, etc.
                        editingContext.processRecentChanges();
                    } else {
                        // Only save if the object is NOT new.
                        editingContext.saveChanges();
                    }
                    _object=null;
                }
            } catch (NSValidation.ValidationException e) {
                if(e instanceof ERXValidationException) {
                    ERXValidationException ex = (ERXValidationException)e;
                    D2WContext context = (D2WContext)sender.valueForKey("d2wContext");
                    Object o = ex.object();
                    
                    if(o instanceof EOEnterpriseObject) {
                        EOEnterpriseObject eo = (EOEnterpriseObject)o;
                        context.takeValueForKey( eo.entityName(),"entityName");
                        context.takeValueForKey( ex.propertyKey(),"propertyKey");
                    }
                    ((ERXValidationException)e).setContext(context);
                }
                log.info("Validation Exception: "+ e + e.getMessage());
                editingContext.revert();
                String errorMessage = " Could not save your changes: "+e.getMessage()+" ";
                ErrorPageInterface epf=D2W.factory().errorPage(sender.session());
                if(epf instanceof ERDErrorPageInterface) {
                	((ERDErrorPageInterface)epf).setException(e);
                }
                epf.setMessage(errorMessage);
                epf.setNextPage(followPage(sender));
                return (WOComponent)epf;
            }
        }
        return followPage(sender);
    }
}
