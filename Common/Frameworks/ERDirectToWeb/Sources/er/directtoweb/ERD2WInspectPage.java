/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import java.util.Enumeration;
import er.extensions.*;

/**
 * Superclass for all inspecting/editing ERD2W templates.<br />
 * 
 */

public class ERD2WInspectPage extends ERD2WPage implements InspectPageInterface, ERDEditPageInterface, ERDObjectSaverInterface, ERDFollowPageInterface {

    /**
     * Public constructor
     * @param context current context
     */
    public ERD2WInspectPage(WOContext context) { super(context); }
    
    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger("er.directtoweb.templates.ERD2WInspectPage");
    public static final ERXLogger validationCat = ERXLogger.getERXLogger("er.directtoweb.validation.ERInspectPage");
    
    private boolean _objectWasSaved;
    public boolean objectWasSaved() { return _objectWasSaved; }

    private WOComponent _previousPage;
    public WOComponent previousPage() { return _previousPage;}
    public void setPreviousPage(WOComponent existingPageName) { _previousPage = existingPageName; }

    public WOComponent nextPage() { return nextPage(true); }

    public WOComponent nextPage(boolean doConfirm) {
        Object inspectConfirmConfigurationName = d2wContext().valueForKey("inspectConfirmConfigurationName");
        if(doConfirm && inspectConfirmConfigurationName != null && ! "".equals(inspectConfirmConfigurationName)) {
            WOComponent ipi = D2W.factory().pageForConfigurationNamed((String)d2wContext().valueForKey("inspectConfirmConfigurationName"), session());
            if (ipi instanceof InspectPageInterface) {
                ((InspectPageInterface)ipi).setObject((EOEnterpriseObject)d2wContext().valueForKey("object"));
                ((InspectPageInterface)ipi).setNextPageDelegate(nextPageDelegate());
                ((InspectPageInterface)ipi).setNextPage(super.nextPage());
            }
            if (ipi instanceof ERDFollowPageInterface)
                ((ERDFollowPageInterface)ipi).setPreviousPage(context().page());
            return (WOComponent)ipi;
        }
        return (nextPageDelegate() != null) ? nextPageDelegate().nextPage(this) : super.nextPage();
    }

    public boolean isEntityReadOnly() {
        return !ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("isEntityEditable"), !super.isEntityReadOnly());
    }
    
    public WOComponent editAction() {
        WOComponent returnPage = null;
        if (previousPage() == null) {
            EditPageInterface editPage=D2W.factory().editPageForEntityNamed(object().entityName(),session());
            editPage.setObject(ERD2WUtilities.localInstanceFromObjectWithD2WContext(object(), d2wContext()));
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

    public boolean shouldRenderBorder() { return ERXUtilities.booleanValue(d2wContext().valueForKey("shouldRenderBorder")); }
    public boolean shouldShowActionButtons() { return ERXUtilities.booleanValue(d2wContext().valueForKey("shouldShowActionButtons")); }
    public boolean shouldShowCancelButton() { return ERXUtilities.booleanValue(d2wContext().valueForKey("shouldShowCancelButton")); }
    public boolean shouldShowSubmitButton() { return ERXUtilities.booleanValue(d2wContext().valueForKey("shouldShowSubmitButton")); }
    public boolean showCancel() { return super.showCancel() && shouldShowCancelButton(); }
    public boolean doesNotHaveForm() { return !ERXUtilities.booleanValue(d2wContext().valueForKey("hasForm")); }

    public void awake() {
        super.awake();
    	if (_context!=null) {
            _context.lock();
        }
    }
    
    public void sleep() {
    	if (_context!=null) {
            _context.unlock();
        }
        super.sleep();
   }
    

    public void setEditingContext(EOEditingContext newEditingContext) {
        if (newEditingContext != _context) {
            if (_context != null) {
                _context.unlock();
            }
            _context = newEditingContext;
            if (_context != null) {
                _context.lock();
            }
        }
    }

    public void setObject(EOEnterpriseObject eoenterpriseobject) {
        EOEditingContext eoeditingcontext = eoenterpriseobject == null ? null : eoenterpriseobject.editingContext();
        setEditingContext(eoeditingcontext);
        super.setObject(eoenterpriseobject);
    }
    
    /*

     We expect d2wContext.sectionsContents to return one of the three following formats:
     ( ( section1, key1, key2, key4 ), ( section2, key76, key 5, ..) .. )
     OR with the sections enclosed in "()" - this is most useful with the WebAssistant
     ( "(section1)", key1, key2, key3, "(section2)", key3, key4, key5... )
     OR with normal displayPropertyKeys array in fact if sectionContents isn't found then it will look for displayPropertyKeys
     ( key1, key2, key3, ... )
     */

    private ERD2WContainer _currentSection;
    public ERD2WContainer currentSection() { return _currentSection; }
    public void setCurrentSection(ERD2WContainer value) {
        _currentSection = value;
        if (value != null) {
            d2wContext().takeValueForKey(value.name, "sectionKey");
            // we can fire rules from the WebAssistant when we push it the -remangled sectionName into the context
            d2wContext().takeValueForKey( "(" + value.name +")", "propertyKey");
            if (log.isDebugEnabled())
                log.debug("Setting sectionKey: " + value.name);
        }
    }

    public NSArray currentSectionKeys() {
        if (log.isDebugEnabled())
            log.debug("currentSectionKeys()");
        NSArray keys = (NSArray)d2wContext().valueForKey("alternateKeyInfo");
        if (log.isDebugEnabled())
            log.debug("currentSectionKeys (from alternateKeyInfo):" +
                      keys);
        keys = keys == null ? (NSArray)this.currentSection().keys : keys;
        if (log.isDebugEnabled())
            log.debug("Setting sectionKey and keys: " + _currentSection.name + keys);
        return keys;
    }

    private NSMutableArray _sectionsContents;

    public NSArray sectionsContents() {
        if (_sectionsContents ==null) {
            NSArray sectionsContentsFromRule=(NSArray)d2wContext().valueForKey("sectionsContents");
            if (sectionsContentsFromRule==null) {
                sectionsContentsFromRule=(NSArray)d2wContext().valueForKey("displayPropertyKeys");
            }
            if (sectionsContentsFromRule == null)
                throw new RuntimeException("Could not find sectionsContents or displayPropertyKeys in "+d2wContext());
            _sectionsContents = ERDirectToWeb.convertedPropertyKeyArray(sectionsContentsFromRule, '(', ')');

        }
        return _sectionsContents;
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

    public boolean shouldRevertChanges() { return ERXUtilities.booleanValue(d2wContext().valueForKey("shouldRevertChanges")); }
    public boolean shouldSaveChanges() { return ERXUtilities.booleanValue(d2wContext().valueForKey("shouldSaveChanges")); }
    public boolean shouldValidateBeforeSave() { return ERXUtilities.booleanValue(d2wContext().valueForKey("shouldValidateBeforeSave")); }
    public boolean shouldCollectValidationExceptions() { return ERXUtilities.booleanValue(d2wContext().valueForKey("shouldCollectValidationExceptions")); }
    public boolean shouldRecoverFromOptimisticLockingFailure() { return ERXUtilities.booleanValueWithDefault(d2wContext().valueForKey("shouldRecoverFromOptimisticLockingFailure"), false); }

    public boolean tryToSaveChanges(boolean validateObject) { // throws Throwable {
        validationLog.debug("tryToSaveChanges calling validateForSave");
        boolean saved = false;
        try {
            if (object()!=null && validateObject && shouldValidateBeforeSave()) {
                if (_context.insertedObjects().containsObject(object()))
                    object().validateForInsert();
                else
                    object().validateForUpdate();
            }
            if (object()!=null && shouldSaveChanges() && object().editingContext().hasChanges())
                object().editingContext().saveChanges();
            saved = true;
        } catch (ERXValidationException ex) {
            String propertyKey = ex.propertyKey();
            ex.setContext(d2wContext());
            ex.setTargetLanguage((String)session().valueForKeyPath("language"));
            Object o = ex.object();
            if(o instanceof EOEnterpriseObject) {
                EOEnterpriseObject eo = (EOEnterpriseObject)o;
                d2wContext().takeValueForKey( eo.entityName(),"entityName");
                d2wContext().takeValueForKey( propertyKey,"propertyKey");
            }
            if(propertyKey != null && propertyKey.indexOf(",") > 0) {
                keyPathsWithValidationExceptions.addObjectsFromArray(NSArray.componentsSeparatedByString(propertyKey, ","));
            }
            errorMessage = " Could not save your changes: "+ex.getMessage()+" ";
        } catch (NSValidation.ValidationException e) {
            log.info(e.getMessage(), e);
            errorMessage = " Could not save your changes: "+e.getMessage()+" ";
        }catch(EOGeneralAdaptorException e){
           if(shouldRecoverFromOptimisticLockingFailure()){
              NSDictionary userInfo = (NSDictionary)e.userInfo();
              if(!(userInfo == null)) {
                 String eType = (String)userInfo.objectForKey("EOAdaptorFailureKey");
                 if (!(eType == null)) {
                    if (eType.equals("EOAdaptorOptimisticLockingFailure")) {
                       //if (log.isDebugEnabled()) log.debug("about to get EOFailedAdaptorOperationKey");
                       EOAdaptorOperation op = (EOAdaptorOperation) userInfo.objectForKey("EOFailedAdaptorOperationKey");
                       EODatabaseOperation dbop = (EODatabaseOperation) userInfo.objectForKey("EOFailedDatabaseOperationKey");
                       //if (log.isDebugEnabled()) log.debug("about to get _changedValues");
                       if (op != null && dbop != null) {
                          NSDictionary changedValues =  op.changedValues();
                          //if (log.isDebugEnabled()) log.debug("about to get _entity: _changedValues"+ changedValues);
                          NSDictionary snapshot = dbop.dbSnapshot();
                          if (log.isDebugEnabled()) log.debug("snapshot"+ snapshot);
                          EOEntity ent = op.entity();
                          String entName = ent.name();
                          if (log.isDebugEnabled()) log.debug("entName"+ entName);
                          NSArray pkAttribs = ent.primaryKeyAttributes();
                          EOQualifier qual = ERXTolerantSaver.qualifierWithSnapshotAndPks(pkAttribs, snapshot);
                          EOFetchSpecification fs = new EOFetchSpecification(entName, qual, null);
                          fs.setRefreshesRefetchedObjects(true);
                          NSArray objs = object().editingContext().objectsWithFetchSpecification(fs);
                          object().editingContext().revert();
                          errorMessage = "Could not save your changes. The "+ent.name()+
                             " has changed in the database before you could save. Your changes have been lost. Please reapply them.";
                       } else {
                          log.error("Missing EOFailedAdaptorOperationKey or EOFailedDatabaseOperationKey. "+e+"\n\n"+e.userInfo());
                       }
                    }
                 }
              }
           }else{
              throw e;
           }
        }
        
        return saved;
    }

    public WOComponent submitAction() throws Throwable {
        WOComponent returnComponent = null;
        // catch the case where the user hits cancel and then the back button
        if (object()!=null && object().editingContext()==null) {
            errorMessage="<b>You already aborted this operation</b>. Please hit cancel and try again from the first step";
            clearValidationFailed();
        } else {
            if (errorMessages.count()==0) {
                try {
                    _objectWasSaved=true;
                    returnComponent = tryToSaveChanges(true) ? nextPage() : null;
                }catch (NSValidation.ValidationException e) {
						 log.info(e.getMessage(), e);
						 errorMessage = " Could not save your changes: "+e.getMessage()+" ";
					 } finally {
                    _objectWasSaved=false;
                }
            } else {
                // if we don't do this, we end up with the error message in two places
                // in errorMessages and errorMessage (super class)
                errorMessage=null;
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
        WOComponent result=ERDirectToWeb.printerFriendlyPageForD2WContext(d2wContext(),session());
        ((EditPageInterface)result).setObject(object());
        return result;
    }    

    public String tabScriptString() {
        return "var elem = document.EditForm.elements[0];"+
        "if (elem!=null && (elem.type == 'text' || elem.type ==  'area')) elem.focus();";
    }

    // (ak) these actually belong to CompactEdit and PrinterFriendlyInspect
    // moved them here to avoid to much subclassing
    public boolean isEmbedded() {
        return ERXUtilities.booleanValueForBindingOnComponentWithDefault("isEmbedded", this, false);
    }

/*    // FIXME: Should be dynamic
    public String pageTitle() {
        return "NetStruxr - "+d2wContext().valueForKey("displayNameForEntity")+" View";
    }
*/
    public NSTimestamp now() { return new NSTimestamp(); }
    }


