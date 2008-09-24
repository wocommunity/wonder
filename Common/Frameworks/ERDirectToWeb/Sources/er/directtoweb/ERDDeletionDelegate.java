/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr Public Software
 * License version 0.5, a copy of which has been included with this distribution
 * in the LICENSE.NPL file.
 */
package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.ErrorPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EODatabaseOperation;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOObjectNotAvailableException;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.ERDErrorPageInterface;
import er.extensions.ERXEC;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXValidationException;
import er.extensions.ERXValidationFactory;

/**
 * A delegate invoked after confirming a delete action.
 */
public class ERDDeletionDelegate implements NextPageDelegate {

    /** logging support */
    public final static Logger log = Logger.getLogger(ERDDeletionDelegate.class);

    private EOEnterpriseObject    _object;
    private EODataSource          _dataSource;
    private WOComponent           _followPage;

    public ERDDeletionDelegate(EOEnterpriseObject object, WOComponent nextPage) {
        this(object, null, nextPage);
    }

    public ERDDeletionDelegate(EOEnterpriseObject object, EODataSource dataSource, WOComponent nextPage) {
        _object = object;
        _dataSource = dataSource;
        _followPage = nextPage;
    }

    /**
     * Gets the object for deletion.
     * @return the object for deletion
     */
    public EOEnterpriseObject object() {
        return _object;
    }

    /**
     * Gets the destination page which should be returned to the user when the delegate is invoked.
     * Can be overridden in subclasses to provide different followPages.
     * @return the destination page
     */
    protected WOComponent followPage(WOComponent sender) {
        if (log.isDebugEnabled()) { log.debug("In FollowPage"); }
        return _followPage;
    }

    /**
     * Invokes the delegate to delete the object and save changes.
     * @param sender component
     * @return the {@link #followPage(WOComponent)}, or an error message page if there was an exception
     */
    public WOComponent nextPage(WOComponent sender) {
        if (_object != null && _object.editingContext() != null) {
            EOEditingContext editingContext = _object.editingContext();
            NSValidation.ValidationException exception = null;
            try {
                deleteObject();
                saveChanges(editingContext);
            } catch (NSValidation.ValidationException e) {
                editingContext.revert();
                return errorPageWithSenderAndException(sender, e);
            }
        }
        return followPage(sender);
    }
    
    /**
     * Deletes the object.  Override this method to customize deletion behavior.
     * @throws NSValidation.ValidationException if something goes wrong during deletion
     */
    protected void deleteObject() throws NSValidation.ValidationException {
        EOEditingContext editingContext = _object.editingContext();
        if (_dataSource != null) _dataSource.deleteObject(_object);
        if (editingContext instanceof EOSharedEditingContext) {
            // Fault the eo into another ec, as one cannot delete objects
            // in an shared editing context.
            EOEditingContext ec = ERXEC.newEditingContext();
            ec.lock();
            try {
                ec.setSharedEditingContext(null);
                EOEnterpriseObject object = EOUtilities.localInstanceOfObject(ec, _object);
                ec.deleteObject(object);
                saveChanges(ec);
            } catch (EOObjectNotAvailableException eonae) {
                throw ERXValidationFactory.defaultFactory().createCustomException(_object, "EOObjectNotAvailableException");
            } finally {
                ec.unlock();
                ec.dispose();
            }
        } else { // Working with a normal editing context.
            if (ERXEOControlUtilities.isNewObject(_object)) {
                editingContext.forgetObject(_object);
            } else {
                editingContext.deleteObject(_object);
            }
        }
    }
    
    /**
     * Saves the deletion of the object to the provided editing context.  Override this method to customize saving behavior.
     * @param ec to which the changes should be saved
     * @throws NSValidation.ValidationException if something goes wrong during save
     */
    protected void saveChanges(EOEditingContext ec) throws NSValidation.ValidationException {
        try {
            if (ERXEOControlUtilities.isNewObject(_object)) {
                // This is necessary to force state synching, e.g., for display groups, etc.
                ec.processRecentChanges();
            } else { // Only save if the object is NOT new.
                // In order to support using the delete button in an embedded page configuration, where saving is 
                // not a desirable default behavior, we need to try to detect when the page was embedded.  We look 
                // at the task of the destination page (followPage) to take a best guess as to when it's appropriate
                // to save.
                if (_followPage != null && _followPage instanceof D2WPage) {
                    D2WPage fp = (D2WPage)_followPage;
                    if (!"edit".equals(fp.task())) {
                        // Save when the destination page is not an edit page, as it will not have its own save button.
                        ec.saveChanges();
                    } else {
                        // Just make sure the page will refresh properly.
                        ec.processRecentChanges();
                    }
                } else {
                    // Fall back to the default behavior.
                    ec.saveChanges();
                }
            }
        } catch (EOGeneralAdaptorException gae) {
            NSDictionary userInfo = gae.userInfo();
            NSValidation.ValidationException exception = null;
            if (userInfo != null) {
                EODatabaseOperation op = (EODatabaseOperation)userInfo.objectForKey(EODatabaseContext.FailedDatabaseOperationKey);
                if (op.databaseOperator() == EODatabaseOperation.DatabaseDeleteOperator) {
                    exception = ERXValidationFactory.defaultFactory().createCustomException(_object, "EOObjectNotAvailableException");
                }
            }
            if (exception == null) {
                exception = ERXValidationFactory.defaultFactory().createCustomException(_object, "Database error: " + gae.getMessage());
            }
            throw exception;
        }
    }
    
    /**
     * Returns an error message page with the given exception as its basis.
     * @param sender component
     * @param exception whose error message to display
     * @return the error message page
     */
    private WOComponent errorPageWithSenderAndException(WOComponent sender, NSValidation.ValidationException exception) {
        if (exception instanceof ERXValidationException) {
            ERXValidationException ex = (ERXValidationException) exception;
            D2WContext context = (D2WContext) sender.valueForKey("d2wContext");
            Object o = ex.object();

            if (o instanceof EOEnterpriseObject) {
                EOEnterpriseObject eo = (EOEnterpriseObject) o;
                context.takeValueForKey(eo.entityName(), "entityName");
                context.takeValueForKey(ex.propertyKey(), "propertyKey");
            }
            ((ERXValidationException) exception).setContext(context);
        }
        log.info("Validation Exception: " + exception + exception.getMessage());
        
        String errorMessage = " Could not save your changes: " + exception.getMessage() + " ";
        ErrorPageInterface epf = D2W.factory().errorPage(sender.session());
        if (epf instanceof ERDErrorPageInterface) {
            ((ERDErrorPageInterface) epf).setException(exception);
        }
        epf.setMessage(errorMessage);
        epf.setNextPage(followPage(sender));
        return (WOComponent) epf;
    }
}
