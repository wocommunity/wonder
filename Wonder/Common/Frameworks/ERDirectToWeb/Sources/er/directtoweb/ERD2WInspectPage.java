/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSValidation;

import er.extensions.ERXComponentActionRedirector;
import er.extensions.ERXEOAccessUtilities;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXLocalizer;
import er.extensions.ERXValueUtilities;
import er.extensions.ERXWOForm;

/**
 * Superclass for all inspecting/editing ERD2W templates.<br />
 * 
 */

public class ERD2WInspectPage extends ERD2WPage implements InspectPageInterface, ERDEditPageInterface, ERDObjectSaverInterface, ERDFollowPageInterface, ERXComponentActionRedirector.Restorable  {

    /**
     * Public constructor
     * @param context current context
     */
    public ERD2WInspectPage(WOContext context) { super(context); }
    
    /** logging support */
    public static final Logger log = Logger.getLogger(ERD2WInspectPage.class);
    public static final Logger validationCat = Logger.getLogger(ERD2WInspectPage.class+".validation");

    public String urlForCurrentState() {
    	NSDictionary dict = null;
    	String actionName = d2wContext().dynamicPage();
    	if(object() != null) {
    		String primaryKeyString = ERXEOControlUtilities.primaryKeyStringForObject(object());
    		if(primaryKeyString != null) {
    			dict = new NSDictionary(primaryKeyString, "__key");
    		}
    	}
    	return context().directActionURLForActionNamed(actionName, dict).replaceAll("&amp;", "&");
    }

    
    private boolean _objectWasSaved;
    public boolean objectWasSaved() { return _objectWasSaved; }

    private WOComponent _previousPage;
    public WOComponent previousPage() { return _previousPage;}
    public void setPreviousPage(WOComponent existingPageName) { _previousPage = existingPageName; }

    public WOComponent nextPage() { return nextPage(true); }

    public WOComponent nextPage(boolean doConfirm) {
        Object inspectConfirmConfigurationName = d2wContext().valueForKey("inspectConfirmConfigurationName");
        if(doConfirm && inspectConfirmConfigurationName != null && ! "".equals(inspectConfirmConfigurationName)) {
            WOComponent ipi = D2W.factory().pageForConfigurationNamed((String)inspectConfirmConfigurationName, session());
            if (ipi instanceof InspectPageInterface) {
                ((InspectPageInterface)ipi).setObject((EOEnterpriseObject)d2wContext().valueForKey("object"));
                ((InspectPageInterface)ipi).setNextPageDelegate(nextPageDelegate());
                ((InspectPageInterface)ipi).setNextPage(super.nextPage());
            }
            if (ipi instanceof ERDFollowPageInterface)
                ((ERDFollowPageInterface)ipi).setPreviousPage(context().page());
            return (WOComponent)ipi;
        }
        WOComponent result = nextPageFromDelegate();
    	if(result == null) {
    		result = super.nextPage();
    	}
        return result;
    }
    
    public WOComponent editAction() {
        WOComponent returnPage = null;
        if (previousPage() == null) {
            String editConfigurationName = (String)d2wContext().valueForKey("editConfigurationName");
            EditPageInterface editPage;
            if(editConfigurationName != null && editConfigurationName.length() > 0) {
                editPage = (EditPageInterface)D2W.factory().pageForConfigurationNamed(editConfigurationName,session());
            } else {
                editPage = D2W.factory().editPageForEntityNamed(object().entityName(),session());
            }
	    	Object value = d2wContext().valueForKey("useNestedEditingContext");
	    	boolean createNestedContext = ERXValueUtilities.booleanValue(value);
	    	EOEnterpriseObject object = ERXEOControlUtilities.editableInstanceOfObject(object(), createNestedContext);
	    	editPage.setObject(object);
            editPage.setNextPage(nextPage());
            returnPage = (WOComponent)editPage;
        }
        return returnPage != null ? returnPage : previousPage();
    }

    public WOComponent deleteAction() throws Throwable {
        EOEnterpriseObject anEO = object();
        if (anEO.editingContext()!=null) {
            anEO.editingContext().deleteObject(anEO);
            return tryToSaveChanges(false) ? nextPage() : null;
        }
        return nextPage();
    }   

    public WOComponent cancelAction() {
        if ((object() != null) && (object().editingContext()!=null) && shouldRevertChanges()) {
            object().editingContext().revert();
        }
        return nextPage(false);
    }

    public boolean shouldRenderBorder() { return ERXValueUtilities.booleanValue(d2wContext().valueForKey("shouldRenderBorder")); }
    public boolean shouldShowActionButtons() { return ERXValueUtilities.booleanValue(d2wContext().valueForKey("shouldShowActionButtons")); }
    public boolean shouldShowCancelButton() { return ERXValueUtilities.booleanValue(d2wContext().valueForKey("shouldShowCancelButton")); }
    public boolean shouldShowSubmitButton() { return ERXValueUtilities.booleanValue(d2wContext().valueForKey("shouldShowSubmitButton")); }
    public boolean showCancel() { return super.showCancel() && shouldShowCancelButton(); }
    public boolean doesNotHaveForm() { return !ERXValueUtilities.booleanValue(d2wContext().valueForKey("hasForm")); }

    public void setObject(EOEnterpriseObject eoenterpriseobject) {
        super.setObject(eoenterpriseobject);
    }
    
    // Useful for validating after all the values have been poked in
    // Example:  consider a wizard page or tab page with values X and Y, in the business logic X + Y > 10.
    //	         however the tab page or wizard step is not the last so validateForSave can't help.  In this
    //		 case you would want the method validateXY to be called after X and Y are both successfully set
    //		 on the eo.  In this case you sould write a the following rule:
    //		pageConfiguration = 'Foo' && tabKey = 'Bar' => validationKeys = "(validateXY)"
    public void performAdditionalValidations() {
        NSArray validationKeys = (NSArray)d2wContext().valueForKey("validationKeys");
        if (validationKeys != null && validationKeys.count() > 0) {
            if (log.isDebugEnabled())
                log.debug("Validating Keys: " + validationKeys + " on eo: " + object());
            for (Enumeration e = validationKeys.objectEnumerator(); e.hasMoreElements();) {
                String validationKey = (String)e.nextElement();
                try {
                    object().valueForKeyPath(validationKey);
                } catch (NSValidation.ValidationException ev) {
                    validationFailedWithException(ev, object(), validationKey);
                }
            }
        }
    }

    public void takeValuesFromRequest(WORequest request, WOContext context) {
        super.takeValuesFromRequest(request, context);
        if (isEditing() && errorMessages.count() == 0) {
            performAdditionalValidations();
        }
    }

    public boolean hasPropertyName() {
        String displayNameForProperty=displayNameForProperty();
        return displayNameForProperty!=null && displayNameForProperty.length()>0;
    }

    public boolean shouldRevertChanges() { return ERXValueUtilities.booleanValue(d2wContext().valueForKey("shouldRevertChanges")); }
    public boolean shouldSaveChanges() { return ERXValueUtilities.booleanValue(d2wContext().valueForKey("shouldSaveChanges")); }
    public boolean shouldValidateBeforeSave() { return ERXValueUtilities.booleanValue(d2wContext().valueForKey("shouldValidateBeforeSave")); }
    public boolean shouldCollectValidationExceptions() { return ERXValueUtilities.booleanValue(d2wContext().valueForKey("shouldCollectValidationExceptions")); }
    public boolean shouldRecoverFromOptimisticLockingFailure() { return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("shouldRecoverFromOptimisticLockingFailure"), false); }

    public boolean tryToSaveChanges(boolean validateObject) { // throws Throwable {
    	validationLog.debug("tryToSaveChanges calling validateForSave");
    	boolean saved = false;
    	if(object()!=null) {
    		EOEditingContext ec = object().editingContext();
    		try {
    			if (object()!=null && validateObject && shouldValidateBeforeSave()) {
    				if (ec.insertedObjects().containsObject(object()))
    					object().validateForInsert();
    				else
    					object().validateForUpdate();
    			}
    			if (object()!=null && shouldSaveChanges() && ec.hasChanges()) {
    				ec.saveChanges();
    			}
    			saved = true;
    		} catch (NSValidation.ValidationException ex) {
    			setErrorMessage(ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("CouldNotSave", ex));
    			validationFailedWithException(ex, ex.object(), "saveChangesExceptionKey");
    		} catch(EOGeneralAdaptorException ex) {
    			if(ERXEOAccessUtilities.isOptimisticLockingFailure(ex) && shouldRecoverFromOptimisticLockingFailure()) {
    				EOEnterpriseObject eo = ERXEOAccessUtilities.refetchFailedObject(ec, ex);
    				setErrorMessage(ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("CouldNotSavePleaseReapply", d2wContext()));
    				validationFailedWithException(ex, eo, "CouldNotSavePleaseReapply");
    			} else {
    				throw ex;
    			}
    		}
    	} else {
    		saved = true;
    	}
    	return saved;
    }
    
    public WOComponent submitAction() throws Throwable {
        WOComponent returnComponent = null;
        // catch the case where the user hits cancel and then the back button
        if (object()!=null && object().editingContext()==null) {
            setErrorMessage(ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("ERD2WInspect.alreadyAborted", d2wContext()));
            clearValidationFailed();
        } else {
            if (errorMessages.count()==0) {
                try {
                    _objectWasSaved=true;
                    returnComponent = tryToSaveChanges(true) ? nextPage() : null;
                } finally {
                    _objectWasSaved=false;
                }
            } else {
                // if we don't do this, we end up with the error message in two places
                // in errorMessages and errorMessage (super class)
                setErrorMessage(null);
            }
        }
        return returnComponent;
    }
    
    public String saveButtonFileName() {
        return object()!=null && object().editingContext()!=null ?
        object().editingContext().parentObjectStore() instanceof EOObjectStoreCoordinator ? "SaveMetalBtn.gif" : "OKMetalBtn.gif" :
        "SaveMetalBtn.gif";
    }

    public WOComponent printerFriendlyVersion() {
        WOComponent result=ERD2WFactory.erFactory().printerFriendlyPageForD2WContext(d2wContext(),session());
        ((EditPageInterface)result).setObject(object());
        return result;
    }    

    public String tabScriptString() {
		String result="";
    	String formName = ERXWOForm.formName(context(), "EditForm");
		if (formName!=null) {
			result="var elem = document."+formName+".elements[0];"+
			"if (elem!=null && (elem.type == 'text' || elem.type ==  'area')) elem.focus();";
		}
		return result;
    }
}


