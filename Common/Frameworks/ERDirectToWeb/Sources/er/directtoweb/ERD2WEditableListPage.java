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
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGenericRecord;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSValidation;
import com.webobjects.foundation._NSDictionaryUtilities;

import er.extensions.ERXConstant;
import er.extensions.ERXEC;
import er.extensions.ERXEOAccessUtilities;
import er.extensions.ERXExceptionHolder;
import er.extensions.ERXLocalizer;
import er.extensions.ERXValidation;
import er.extensions.ERXValueUtilities;

/**
 * List page for editing all items in the list.
 * Name your page EditListYourEntityName.  task will be edit, subTask will be list.
 * See Component ERD2WEditableListTemplate for html/wod example.
 *
 * There is a "mass change" feature that can apply a change to all displayed objects.
 * Think of it as an "input assistant".  The changes are not saved when propagated, and the rows can be updated individually after a mass change has been applied.
 * (Note: There is a {@link ERDMassModifyButton} class that may be more appropriate depending on your needs)
 *
 * To enable the mass change feature on an editable list page, do the following:
 *
 * 1/ Add a "showMassChange" rule that returns "true" for your edit list page
 * 2/ If you want to restrict the keys that can be "mass edited", add a displayPropertyKeys rule with a restricted set of keys with the qualifer "(massChangeEntityDisplay = 1)"
 *
 * Known Issues:
 *      changing the number of items per batch causes problems (the display group's batch is updated too soon in the request/response loop)
 */

public class ERD2WEditableListPage extends ERD2WListPage implements ERXExceptionHolder, ERDObjectSaverInterface {

    public static final Logger log = Logger.getLogger(ERD2WEditableListPage.class);

    public ERD2WEditableListPage(WOContext context) {super(context);}

    public int colspanForNavBar() {
        return 2*displayPropertyKeys().count()+2;
    }

    public int numberOfObjectsPerBatch() {
        // if we are not showing the nav bar, do not batch the display group (since user will have no way to navigate batches)
        if (!ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("showBanner"), true)) {
            return 0;
        }
        else {
            return super.numberOfObjectsPerBatch();
        }
    }

    /*
     FIXME:
     1/ we are missing a formal protocol for selectedObject
     2/ this component uses a hidden trick:
     - clicking on the select button uses next page delegate
     - the cancel button uses the next page

     */

    private boolean _objectWasSaved;
    public boolean objectWasSaved() { return _objectWasSaved; }

    private NSMutableDictionary _errorMessagesDictionaries;

    protected NSMutableDictionary errorMessagesDictionaries(){
        if(_errorMessagesDictionaries == null){
            _errorMessagesDictionaries = new NSMutableDictionary();
        }
        return _errorMessagesDictionaries;
    }

    public NSMutableDictionary errorDictionaryForObject(Object object) {
        int hashCode = object != null ? object.hashCode() : 0;
        Object key = ERXConstant.integerForInt(hashCode);
        if (errorMessagesDictionaries().objectForKey(key) == null) {
            errorMessagesDictionaries().setObjectForKey(new NSMutableDictionary(), key);
        }
        if (log.isDebugEnabled()) log.debug("errorDictionaryForObject("+object+") hashCode/key: "+hashCode+"/"+key+"  errorMessages: "+errorMessagesDictionaries().objectForKey(key));
        return (NSMutableDictionary)errorMessagesDictionaries().objectForKey(key);
    }

    public NSMutableDictionary currentErrorDictionary() {
        Object key = d2wContext().valueForKeyPath("object.hashCode");
        if (errorMessagesDictionaries().objectForKey(key) == null) {
            errorMessagesDictionaries().setObjectForKey(new NSMutableDictionary(), key);
        }
        if (log.isDebugEnabled()) log.debug("currentErrorDictionary() key: "+key+"  errorMessages: "+errorMessagesDictionaries().objectForKey(key));
        return (NSMutableDictionary)errorMessagesDictionaries().objectForKey(key);
    }

    public String dummy;

    public boolean showCancel() {
        return _nextPage!=null;
    }

    public boolean isEntityInspectable() {
        return isEntityReadOnly() && ERXValueUtilities.booleanValue(d2wContext().valueForKey("isEntityInspectable"));
    }

    public void setObject(EOEnterpriseObject eo) {
        super.setObject(eo);
    }

    public WOComponent backAction() {
        return super.backAction();
    }

    public WOComponent nextPage() {
        return (nextPageDelegate() != null) ? nextPageDelegate().nextPage(this) : super.nextPage();
    }

    public boolean shouldValidateBeforeSave() { return ERXValueUtilities.booleanValue(d2wContext().valueForKey("shouldValidateBeforeSave")); }
    public boolean shouldSaveChanges() { return ERXValueUtilities.booleanValue(d2wContext().valueForKey("shouldSaveChanges")); }
    public boolean shouldRecoverFromOptimisticLockingFailure() { return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("shouldRecoverFromOptimisticLockingFailure"), false); }

    private static final NSSelector ValidateForInsertSelector = new NSSelector("validateForInsert");
    private static final NSSelector ValidateForSaveSelector = new NSSelector("validateForUpdate");
    
    public boolean tryToSaveChanges(boolean validateObjects) {
        if (log.isDebugEnabled()) log.debug("tryToSaveChanges() validateObjects: "+validateObjects+"  shouldSaveChanges: "+shouldSaveChanges());
        boolean saved = false;
        try {
            if (!isListEmpty() && validateObjects && shouldValidateBeforeSave()) {
                if (log.isDebugEnabled()) log.debug("tryToSaveChanges calling validateForSave");
                editingContext().insertedObjects().makeObjectsPerformSelector(ValidateForInsertSelector, null);
                editingContext().updatedObjects().makeObjectsPerformSelector(ValidateForSaveSelector, null);
            }
            if (!isListEmpty() && shouldSaveChanges() && editingContext().hasChanges())
                editingContext().saveChanges();
            saved = true;
        } catch (NSValidation.ValidationException ex) {
            setErrorMessage(ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("CouldNotSave", ex));
            validationFailedWithException(ex, ex.object(), "saveChangesExceptionKey");
        } catch(EOGeneralAdaptorException ex) {
            if(shouldRecoverFromOptimisticLockingFailure()) {
                EOEnterpriseObject eo = ERXEOAccessUtilities.refetchFailedObject(editingContext(), ex);
                setErrorMessage(ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("CouldNotSavePleaseReapply", d2wContext()));
                validationFailedWithException(ex, eo, "CouldNotSavePleaseReapply");
            } else {
                throw ex;
            }
        }

        return saved;
    }

    public WOComponent saveAction() {
        WOComponent returnComponent = null;
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
            errorMessage=null;
        }
        return returnComponent;
    }

    public WOComponent cancel(){
        clearValidationFailed();
        if(!isListEmpty()) {
            editingContext().revert();
        }
        if(_massChangeEO != null) {
            if(_massChangeEO.editingContext() != null) {
                _massChangeEO.editingContext().revert();
            }
            _massChangeEO = null;
            _massChangeDisplayGroup = null;
        }
        return backAction();
    }

    public void validationFailedWithException (Throwable e, Object value, String keyPath) {
        if (value != null) {
            ERXValidation.validationFailedWithException(e, value, keyPath, errorDictionaryForObject(value), propertyKey(), ERXLocalizer.currentLocalizer(), d2wContext().entity(), shouldSetFailedValidationValue());
        }
        super.validationFailedWithException(e, value, keyPath);
    }

    public void clearValidationFailed(){
        super.clearValidationFailed();
        setErrorMessage(null);
        for(Enumeration e = errorMessagesDictionaries().objectEnumerator(); e.hasMoreElements();){
            ((NSMutableDictionary)e.nextElement()).removeAllObjects();
        }
    }

    public WOComponent update() {
        tryToSaveChanges(true);
        return null;
    }

    public void takeValuesFromRequest(WORequest r, WOContext c) {
        // Need to make sure that we have a clean slate, every time
        clearValidationFailed();
        _errorMessagesDictionaries = null;
        super.takeValuesFromRequest(r, c);
    }

    public String saveLabel() {
        String templateKey = (String)d2wContext().valueForKey("saveLabelTemplateKey");
        String displayName = (String)d2wContext().valueForKey("displayNameForEntity");
        int count = displayGroup().allObjects().count();
        if(templateKey == null)
            templateKey = "ERDEditList.saveLabel";

        String saveLabel = ERXLocalizer.currentLocalizer().plurifiedStringWithTemplateForKey(templateKey, displayName, count, d2wContext());
        if (log.isDebugEnabled()) log.debug("saveLabel() - "+saveLabel);
        return saveLabel;
    }

    //
    // Mass change feature
    //

    public boolean shouldShowMassChange() {
        int displayedObjectsCount = displayGroup().displayedObjects() != null ? displayGroup().displayedObjects().count() : 0;
        return displayedObjectsCount > 0 && ERXValueUtilities.booleanValue(d2wContext().valueForKey("showMassChange"));
    }

    public static final String MassChangeEntityDisplayKey = "massChangeEntityDisplay";
    private D2WContext _d2wContextForMassChangeEO;
    protected D2WContext d2wContextForMassChangeEO() {
        if (_d2wContextForMassChangeEO == null) {
            _d2wContextForMassChangeEO = new D2WContext(d2wContext());
            _d2wContextForMassChangeEO.takeValueForKey(Boolean.TRUE, MassChangeEntityDisplayKey);
        }
        return _d2wContextForMassChangeEO;
    }

    protected WODisplayGroup _massChangeDisplayGroup;
    public WODisplayGroup massChangeDisplayGroup() {
        if (_massChangeDisplayGroup == null) {
            final EOArrayDataSource ads = new EOArrayDataSource(massChangeEO().classDescription(), massChangeEO().editingContext());
            ads.setArray(new NSArray(massChangeEO()));
            _massChangeDisplayGroup = new WODisplayGroup();
            _massChangeDisplayGroup.setDataSource(ads);
            _massChangeDisplayGroup.setNumberOfObjectsPerBatch(0);
            _massChangeDisplayGroup.fetch();
            if(log.isDebugEnabled()) log.debug("_massChangeDisplayGroup: " + _massChangeDisplayGroup);
        }
        return _massChangeDisplayGroup;
    }

    // custom generic record class that manages unbound keys in a dictionary.
    public class ERDMassChangeGenericRecord extends EOGenericRecord {
        // dictionary of non property key values
        private NSMutableDictionary _unboundKeyDictionary;
        public ERDMassChangeGenericRecord(EOClassDescription classDescription) {
            super(classDescription);
            _unboundKeyDictionary = new NSMutableDictionary();
        }

        public Object handleQueryWithUnboundKey(String key) {
            return _unboundKeyDictionary.objectForKey(key);
        }

        public void handleTakeValueForUnboundKey(Object value, String key) {
            _unboundKeyDictionary.takeValueForKey(value, key);
        }

        public NSDictionary pendingChanges() {
            final NSMutableDictionary combinedKeyChanges = _unboundKeyDictionary.mutableClone();
            final NSDictionary changesFromSnapshot = massChangeEO().changesFromCommittedSnapshot();

            if (changesFromSnapshot!= null && changesFromSnapshot.count() > 0) {
                combinedKeyChanges.addEntriesFromDictionary(changesFromSnapshot);
            }

            return combinedKeyChanges.immutableClone();
        }

        public void clearPendingChanges() {
            final NSDictionary changesFromSnapshot = massChangeEO().changesFromCommittedSnapshot();
            if (changesFromSnapshot != null && changesFromSnapshot.count() > 0) {
                NSDictionary nullValues = _NSDictionaryUtilities.dictionaryWithNullValuesForKeys(changesFromSnapshot.allKeys());
                _massChangeEO.takeValuesFromDictionary(nullValues);
            }
            _unboundKeyDictionary.removeAllObjects();
        }

        // committed snapshot convenience from ERXGenericRecord
        private NSDictionary changesFromCommittedSnapshot() {
            return changesFromSnapshot(editingContext().committedSnapshotForObject(this));
        }
    }

    protected ERDMassChangeGenericRecord _massChangeEO;
    public ERDMassChangeGenericRecord massChangeEO() {
        if (_massChangeEO == null) {
            // create our dummy EO to hold our potential mass changes
            _massChangeEO = new ERDMassChangeGenericRecord(EOClassDescription.classDescriptionForEntityName(d2wContext().entity().name()));
            ERXEC.newEditingContext().insertObject(_massChangeEO);
            if(log.isDebugEnabled()) log.debug("Created _massChangeEO (for entity: "+d2wContext().entity().name()+"): " + _massChangeEO);
        }
        return _massChangeEO;
    }

    public WOComponent clearMassChangeEO() {
        if (_massChangeEO != null) {
            _massChangeEO.clearPendingChanges();
        }
        return null;
    }

    private static final NSSelector TakeValuesFromDictionarySelector = new NSSelector("takeValuesFromDictionary", new Class [] {NSDictionary.class});
    private NSDictionary _lastAppliedMassChanges;
    public WOComponent propagateChangesToVisibleObjects() {
        final NSArray displayedObjects = displayGroup().displayedObjects();
        final NSDictionary changes = massChangeEO().pendingChanges();

        _lastAppliedMassChanges = null;
        if (displayedObjects != null && changes != null && changes.count() > 0) {
            displayedObjects.makeObjectsPerformSelector(TakeValuesFromDictionarySelector, new Object[]{changes});
            _lastAppliedMassChanges = changes;
            clearMassChangeEO();
        }

        return null;
    }

    public String propagateChangesDetails() {
        return "last applied changes: " + _lastAppliedMassChanges + "<br>current changes: " + massChangeEO().pendingChanges();
    }

}
