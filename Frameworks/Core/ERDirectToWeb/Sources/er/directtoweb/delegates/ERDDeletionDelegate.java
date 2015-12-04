/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 * 
 * This software is published under the terms of the NetStruxr Public Software
 * License version 0.5, a copy of which has been included with this distribution
 * in the LICENSE.NPL file.
 */
package er.directtoweb.delegates;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
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

import er.directtoweb.interfaces.ERDErrorPageInterface;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.localization.ERXLocalizer;
import er.extensions.validation.ERXValidationException;
import er.extensions.validation.ERXValidationFactory;

/**
 * Delete used after confirming a delete action.
 */
public class ERDDeletionDelegate implements NextPageDelegate {

    /** logging support */
    public final static Logger log = Logger.getLogger("er.directtoweb.delegates.ERDDeletionDelegate");

    private EOEnterpriseObject    _object;
    private EODataSource          _dataSource;
    private WOComponent           _followPage;

    // Can be overridden in subclasses to provide different followPages.
    protected WOComponent followPage(WOComponent sender) {
        log.debug("In FollowPage");
        return _followPage;
    }

    public ERDDeletionDelegate(EOEnterpriseObject object, WOComponent nextPage) {
        this(object, null, nextPage);
    }

    public ERDDeletionDelegate(EOEnterpriseObject object, EODataSource dataSource, WOComponent nextPage) {
        _object = object;
        _dataSource = dataSource;
        _followPage = nextPage;
    }

    public WOComponent nextPage(WOComponent sender) {
        if (_object != null && _object.editingContext() != null) {
            EOEditingContext editingContext = _object.editingContext();
            NSValidation.ValidationException exception = null;
            try {
                if (_dataSource != null) _dataSource.deleteObject(_object);
                if (editingContext instanceof EOSharedEditingContext) {
                    //fault the eo into another ec, one cannot delete objects
                    // in an shared editing context
                    EOEditingContext ec = ERXEC.newEditingContext();
                    ec.lock();
                    try {
                        ec.setSharedEditingContext(null);
                        EOEnterpriseObject object = EOUtilities.localInstanceOfObject(ec, _object);
                        ec.deleteObject(object);
                        ec.saveChanges();
                    } finally {
                        ec.unlock();
                        ec.dispose();
                    }
                } else {
                	//Place the EO into a nested ec and try to delete there
                	//to prevent the appearance of a successful delete if
                	//validation fails.
                	EOEnterpriseObject eo = ERXEOControlUtilities.editableInstanceOfObject(_object, true);
                	EOEditingContext childEC = eo.editingContext();
                	childEC.deleteObject(eo);
                	childEC.saveChanges();
                	
                    if (ERXEOControlUtilities.isNewObject(_object)) {
                        // This is necessary to force state synching, e.g., for display groups, etc.
                        editingContext.processRecentChanges();
                    } else {
                        // Only save if the object is NOT new.
                        editingContext.saveChanges();
                    }
                    _object = null;
                }
            } catch (EOObjectNotAvailableException e) {
                exception = ERXValidationFactory.defaultFactory().createCustomException(_object, "EOObjectNotAvailableException");
            } catch (EOGeneralAdaptorException e) {
            	NSDictionary userInfo = e.userInfo();
            	if(userInfo != null) {
            		EODatabaseOperation op = (EODatabaseOperation)userInfo.objectForKey(EODatabaseContext.FailedDatabaseOperationKey);
            		if(op.databaseOperator() == EODatabaseOperation.DatabaseDeleteOperator) {
            			exception = ERXValidationFactory.defaultFactory().createCustomException(_object, "EOObjectNotAvailableException");
            		}
            	}
            	if(exception == null) {
            		exception = ERXValidationFactory.defaultFactory().createCustomException(_object, "Database error: " + e.getMessage());
            	}
            } catch (NSValidation.ValidationException e) {
                exception = e;
            }
            if(exception != null) {
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
                editingContext.revert();
                String errorMessage = ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("CouldNotSave", exception);
                ErrorPageInterface epf = D2W.factory().errorPage(sender.session());
                if (epf instanceof ERDErrorPageInterface) {
                    ((ERDErrorPageInterface) epf).setException(exception);
                }
                epf.setMessage(errorMessage);
                epf.setNextPage(followPage(sender));
                return (WOComponent) epf;
            }
        }
        return followPage(sender);
    }
}