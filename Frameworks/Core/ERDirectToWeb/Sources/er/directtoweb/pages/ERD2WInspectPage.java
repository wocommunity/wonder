/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages;

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

import er.directtoweb.ERD2WFactory;
import er.directtoweb.interfaces.ERDEditPageInterface;
import er.directtoweb.interfaces.ERDFollowPageInterface;
import er.directtoweb.interfaces.ERDObjectSaverInterface;
import er.extensions.appserver.ERXComponentActionRedirector;
import er.extensions.components._private.ERXWOForm;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * Superclass for all inspecting/editing ERD2W templates.<br />
 * @d2wKey inspectConfirmConfigurationName
 * @d2wKey object
 * @d2wKey editConfigurationName
 * @d2wKey useNestedEditingContext
 * @d2wKey shouldRenderBorder
 * @d2wKey shouldShowActionButtons
 * @d2wKey shouldShowCancelButtons
 * @d2wKey shouldShowSubmitButton
 * @d2wKey hasForm
 * @d2wKey validationKeys
 * @d2wKey shouldRevertChanges
 * @d2wKey shouldSaveChanges
 * @d2wKey shoudlvalidateBeforeSave
 * @d2wKey shouldCollectValidationExceptions
 * @d2wKey shouldRecoverFromOptimisticLockingFailure
 * @d2wKey shouldRevertUponSaveFailure
 * @d2wKey firstResponder
 */
public class ERD2WInspectPage extends ERD2WPage implements InspectPageInterface, ERDEditPageInterface, ERDObjectSaverInterface, ERDFollowPageInterface, ERXComponentActionRedirector.Restorable  {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Public constructor
     * @param context current context
     */
    public ERD2WInspectPage(WOContext context) { super(context); }
    
    /** logging support */
    public static final Logger log = Logger.getLogger(ERD2WInspectPage.class);
    public static final Logger validationCat = Logger.getLogger(ERD2WInspectPage.class+".validation");

	protected static final String firstResponderContainerName = "FirstResponderContainer";

	@Override
    public String urlForCurrentState() {
    	NSDictionary<String, Object> dict = null;
    	String actionName = d2wContext().dynamicPage();
    	if(object() != null) {
    		String primaryKeyString = ERXEOControlUtilities.primaryKeyStringForObject(object());
    		if(primaryKeyString != null) {
    			dict = new NSDictionary<String, Object>(primaryKeyString, "__key");
    		}
    	}
    	return context().directActionURLForActionNamed(actionName, dict).replaceAll("&amp;", "&");
    }

    
    protected boolean _objectWasSaved;

    public boolean objectWasSaved() { return _objectWasSaved; }

    private WOComponent _previousPage;

    public WOComponent previousPage() { return _previousPage;}

    public void setPreviousPage(WOComponent existingPageName) { _previousPage = existingPageName; }

	@Override
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
            return ipi;
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

	@Override
    public boolean showCancel() { return super.showCancel() && shouldShowCancelButton(); }
    public boolean doesNotHaveForm() { return !ERXValueUtilities.booleanValue(d2wContext().valueForKey("hasForm")); }

	@Override
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
        NSArray<String> validationKeys = (NSArray<String>)d2wContext().valueForKey("validationKeys");
        if (validationKeys != null && validationKeys.count() > 0) {
            if (log.isDebugEnabled())
                log.debug("Validating Keys: " + validationKeys + " on eo: " + object());
            for (String validationKey : validationKeys) {
                try {
                    object().valueForKeyPath(validationKey);
                } catch (NSValidation.ValidationException ev) {
                    validationFailedWithException(ev, object(), validationKey);
                }
            }
        }
    }

	@Override
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

	@Override
    public boolean shouldCollectValidationExceptions() { return ERXValueUtilities.booleanValue(d2wContext().valueForKey("shouldCollectValidationExceptions")); }

    public boolean shouldRecoverFromOptimisticLockingFailure() { return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("shouldRecoverFromOptimisticLockingFailure"), false); }
    public boolean shouldRevertUponSaveFailure() { return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("shouldRevertUponSaveFailure"), false); }

    public boolean tryToSaveChanges(boolean validateObject) { // throws Throwable {
    	validationLog.debug("tryToSaveChanges calling validateForSave");
    	boolean saved = false;
    	if(object()!=null) {
    		EOEditingContext ec = object().editingContext();
    		boolean shouldRevert = false;
    		try {
    			if (object()!=null && validateObject && shouldValidateBeforeSave()) {
    				if (ec.insertedObjects().containsObject(object()))
    					object().validateForInsert();
    				else
    					object().validateForUpdate();
    			}
            if (object()!=null && shouldSaveChanges() && ec.hasChanges()) {
                try {
                    ec.saveChanges();
                } catch (RuntimeException e) {
                    if( shouldRevertUponSaveFailure() ) {
                        shouldRevert = true;
                    }
                    throw e;
                }
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
    				} else if(ERXEOAccessUtilities.isUniqueFailure(ex)) { 
    				  EOEnterpriseObject eo = ERXEOAccessUtilities.refetchFailedObject(ec, ex);
    				  setErrorMessage(ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("DatabaseUniqException", d2wContext()));
    				  validationFailedWithException(ex, eo, "DatabaseUniqException");
    			} else {
    				throw ex;
    			}
			} finally {
				if( shouldRevert ) {
					ec.lock();
					try {
						ec.revert();
					} finally {
						ec.unlock();
					}
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

    /**
     * Generates other strings to be included in the WOGenericContainer tag for the propertyKey component cell.  This is
     * used in conjunction with the <code>firstResponderKey</code> to mark the cell where the propertyKey is that named 
     * by the <code>firstResponderKey</code> so that the "focusing" JavaScript {@link #tabScriptString tabScriptString}
     * can identify it.
     * @return a String to be included in the <code>td</code> tag for the propertyKey component cell.
     */
    public String otherTagStringsForPropertyKeyComponentCell() {
        String firstResponderKey = (String)d2wContext().valueForKey(Keys.firstResponderKey);
        if (firstResponderKey != null && firstResponderKey.equals(propertyKey())) {
            return " id=\"" + firstResponderContainerName + "\"";
        }
        return null;
    }

	/**
     * <p>Constructs a JavaScript string that will give a particular field focus when the page is loaded.  If the key
     * <code>firstResponderKey</code> from the d2wContext resolves, the script will attempt to focus on the form field
     * belonging to the property key named by the <code>firstResponderKey</code>.  Otherwise, the script will just focus
     * on the first field in the form.</p>
     *
     * <p>Note that the key <code>useFocus</code> must resolve to <code>true</code> in order for the script to be
     * generated.</p>
     * @return a JavaScript string.
     */
    public String tabScriptString() {
		if (d2wContext().valueForKey(Keys.firstResponderKey) != null) {
			return scriptForFirstResponderActivation();
		} else {
			String result="";
			String formName = ERXWOForm.formName(context(), "EditForm");
			if (formName!=null) {
				result="var elem = document."+formName+".elements[0];"+
				"if (elem!=null && (elem.type == 'text' || elem.type ==  'area')) elem.focus();";
			}
			return result;
		}
    }

	/**
	     * <p>Constructs a JavaScript string to include in the WOComponent that will give a particular field focus when the
     * page is loaded, if the key <code>firstResponderKey</code> from the d2wContext resolves.  The script will attempt
     * to focus on the form field belonging to the property key named by the <code>firstResponderKey</code>.
     * @return a JavaScript string to bring focus to a specific form element.
     */
    public String scriptForFirstResponderActivation() {
        /* This is a bit of a roundabout way of getting to the form element for the propertyKey designated by the
         * firstResponderKey.  The problem is that, basically, we don't know what component will be rendered until
         * the rules are fired.  We also can't find the component by name or by id because we don't get to attach
         * that info. to the components used by D2W, since they are all very generic.
         *
         * So, the approach here is to:
         *
         * 1) Get as close as possible to the right form field by demarcating the containing element (the table cell)
         * with the id="FirstResponderContainer" property. See the otherTagStringsForPropertyKeyComponentCell method.
         * Then the JavaScript can easily find the element with that id.
         * 2) Once we have that, the script goes spelunking through the element's children until it finds
         * one that is of a reasonable type to be used in a form.
         * 3) Finally, the script attempts to activate the focus on that form element.
         */
        if (d2wContext().valueForKey(Keys.firstResponderKey) == null) { return null; }

        StringBuilder sb = new StringBuilder();
        sb.append("function activateFirstResponder() {\n");

        // Get the container element.
        sb.append("\tvar container = document.getElementById('").append(firstResponderContainerName).append("');\n");
        sb.append("\tif (!container) { return; }\n");

        // Go through all the child elements of the container to find
        // the first that is of a type that can be used as a form input.
        // Note that this excludes image and button/submit tags.
        sb.append("\tvar candidates = container.getElementsByTagName('*');\n");
        sb.append("\tif (candidates && candidates.length > 0) {\n");
        sb.append("\t\tfor (var i = 0; i < candidates.length; i++) {\n");
        sb.append("\t\t\tvar el = candidates[i];\n");
        sb.append("\t\t\tvar type = el.type;\n");

        sb.append("\t\t\tif (type == 'text' || type == 'checkbox' || type == 'radio' || \n");
        sb.append("\t\t\t\ttype == 'select-one' || type == 'select-multiple' || \n");
        sb.append("\t\t\t\ttype == 'file') {\n");

        // Found an element of an acceptable type.  Try to set focus on it.
        sb.append("\t\t\t\ttry {\n");
        sb.append("\t\t\t\t\tel.focus();\n");
        sb.append("\t\t\t\t\treturn;\n");
        sb.append("\t\t\t\t} catch (e) {}// Eat the exception.\n");
        sb.append("\t\t\t}\n");
        sb.append("\t\t}\n");
        sb.append("\t}\n");
        sb.append('}');

        // Now call the function.
        sb.append("activateFirstResponder();");

        return sb.toString();
    }

}


