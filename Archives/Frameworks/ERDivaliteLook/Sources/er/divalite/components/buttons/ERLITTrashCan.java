package er.divalite.components.buttons;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EODatabaseOperation;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOObjectNotAvailableException;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.components.buttons.ERDTrashcan;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.localization.ERXLocalizer;
import er.extensions.validation.ERXValidationException;
import er.extensions.validation.ERXValidationFactory;

public class ERLITTrashCan extends ERDTrashcan {
	
    public final static Logger log = Logger.getLogger(ERLITTrashCan.class);


	public ERLITTrashCan(WOContext aContext) {
		super(aContext);
	}

	// accessors
	public String onClick() {
		return "return confirm('" + message() + "')";
	}
	
    public String message() {
        return ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("ERLITTrashCan.confirmDeletionMessage", d2wContext()); 
    }
    
    // actions
    public void deleteObject() {
        if (object() != null && object().editingContext() != null) {
            EOEditingContext editingContext = object().editingContext();
            NSValidation.ValidationException exception = null;
            try {
                if (dataSource() != null) dataSource().deleteObject(object());
                if (editingContext instanceof EOSharedEditingContext) {
                    //fault the eo into another ec, one cannot delete objects
                    // in an shared editing context
                    EOEditingContext ec = ERXEC.newEditingContext();
                    ec.lock();
                    try {
                        ec.setSharedEditingContext(null);
                        EOEnterpriseObject object = EOUtilities.localInstanceOfObject(ec, object());
                        ec.deleteObject(object);
                        ec.saveChanges();
                    } finally {
                        ec.unlock();
                        ec.dispose();
                    }
                } else {
                    editingContext.deleteObject(object());
                    if (ERXEOControlUtilities.isNewObject(object())) {
                        // This is necessary to force state synching, e.g., for display groups, etc.
                        editingContext.processRecentChanges();
                    } else {
                        // Only save if the object is NOT new.
                        editingContext.saveChanges();
                    }
                }
            } catch (EOObjectNotAvailableException e) {
                exception = ERXValidationFactory.defaultFactory().createCustomException(object(), "EOObjectNotAvailableException");
            } catch (EOGeneralAdaptorException e) {
            	@SuppressWarnings("unchecked") NSDictionary userInfo = e.userInfo();
            	if(userInfo != null) {
            		EODatabaseOperation op = (EODatabaseOperation)userInfo.objectForKey(EODatabaseContext.FailedDatabaseOperationKey);
            		if(op.databaseOperator() == EODatabaseOperation.DatabaseDeleteOperator) {
            			exception = ERXValidationFactory.defaultFactory().createCustomException(object(), "EOObjectNotAvailableException");
            		}
            	}
            	if(exception == null) {
            		exception = ERXValidationFactory.defaultFactory().createCustomException(object(), "Database error: " + e.getMessage());
            	}
            } catch (NSValidation.ValidationException e) {
                exception = e;
            }
            if(exception != null) {
                if (exception instanceof ERXValidationException) {
                    ERXValidationException ex = (ERXValidationException) exception;
                    D2WContext context = d2wContext();
                    Object o = ex.object();

                    if (o instanceof EOEnterpriseObject) {
                        EOEnterpriseObject eo = (EOEnterpriseObject) o;
                        context.takeValueForKey(eo.entityName(), "entityName");
                        context.takeValueForKey(ex.propertyKey(), "propertyKey");
                    }
                    ((ERXValidationException) exception).setContext(context);
                    
                    log.info("Validation Exception: " + exception + exception.getMessage());
                    editingContext.revert();
                } else throw exception;
            }
        }    
    }
}
