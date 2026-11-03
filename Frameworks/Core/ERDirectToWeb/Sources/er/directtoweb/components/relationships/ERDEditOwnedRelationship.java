/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.relationships;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EODetailDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.ERDirectToWeb;
import er.directtoweb.components.ERDCustomEditComponent;
import er.directtoweb.interfaces.ERDPickPageInterface;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXEnterpriseObject;

/////////////////////////////////////////////////////////////////////////////////
// Important D2W Keys:
//	showAddButton - boolean to tell component to show the add button
//	entityNamesForNewInstances - array of strings that detail which entities are to be created.
//	explanationComponentName - component that is used in the StringListPicker to explain what it is that they are picking.
//	additionalRelationshipKeys - key-value pairs of additional relationships that need to be set after the eo is created.
//	uiStyle - list style will use a D2WList, popup/radio/browser will use an ERToOneRelationship FIXME: add in ERToManyRelationship
//	listConfigurationName - optional key that is for the list configuration of the D2W component.
//	destinationDisplayKey - key used to denote what to display in the ERToOneRelationship
//	selectionListKey - key that denotes what to use as the dataSource for the ERToOneRelationship.
//	permissionToEdit - key that determines if the add and/or edit button are shown.
//	postCreateNextPageDelegateKey - key that allows you to get your own nextPageDelegate into the fray.
/////////////////////////////////////////////////////////////////////////////////

/**
 * Crazy component.  Useful for editing/creating objects in an owned toOne or toMany relationship.  Even works with relationships to abstract entities.
 * 
 * @binding showAddButton defaults=Boolean
 * @binding key
 * @binding object
 * @binding listConfigurationName
 * @binding entityNamesForNewInstances
 * @binding explanationComponentName
 * @binding uiStyle
 * @binding destinationSortKey
 * @binding destinationDisplayKey
 * @binding selectionListKey
 * @binding preRelationshipKeys
 * @binding permissionToEdit defaults=Boolean
 * @binding postRelationshipKeys
 * @binding useForms defaults=Boolean
 */

public class ERDEditOwnedRelationship extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;


    /** logging support */
    public final static Logger log = LoggerFactory.getLogger("er.directtoweb.components.ERDEditOwnedRelationship");

    protected EOEditingContext localContext;

    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }

    public ERDEditOwnedRelationship(WOContext c) {
        super(c);
    }

    @Override
    public void awake() {
        _selectionList = null;
    }

    public NSDictionary preRelationshipKeys() { return (NSDictionary)valueForBinding("preRelationshipKeys"); }
    public NSDictionary postRelationshipKeys() { return (NSDictionary)valueForBinding("postRelationshipKeys"); }
    public NSArray entityNamesForNewInstances() { return (NSArray)valueForBinding("entityNamesForNewInstances"); }
    public String explanationComponentName() { return (String)valueForBinding("explanationComponentName"); }
    public String listConfigurationName() { return (String)valueForBinding("listConfigurationName"); }
    public String selectionListKey() { return (String)valueForBinding("selectionListKey"); }
    public String postCreateNextPageDelegateKey() { return (String)valueForBinding("postCreateNextPageDelegateKey"); }
    public String errorMessage() { return hasBinding("noSelectionErrorMessage") ? (String)valueForBinding("noSelectionErrorMessage") : "";}

    public NSArray list() {
        // we need to put the list in a peer (for add/delete ops) or in a child if it's a new object
        NSArray initialArray = (NSArray) (selectionListKey()!=null ? object().valueForKeyPath(selectionListKey()) :
                                          objectKeyPathValue());
        NSArray result = null;
        if (initialArray != null) {
            localContext=((ERXEnterpriseObject)object()).isNewObject() ?
            ERXEC.newEditingContext(object().editingContext(), false) :
            ERXEC.newEditingContext(object().editingContext().parentObjectStore());
            result = EOUtilities.localInstancesOfObjects(localContext, initialArray);
        }
        return result != null ? result : ERXConstant.EmptyArray;
    }

    private EODetailDataSource _detailDataSource;
    public EODataSource detailDataSource() {
        if (_detailDataSource==null) {
            String relationshipKey=selectionListKey()!=null ? selectionListKey() : key();
            _detailDataSource=new EODetailDataSource(object().classDescription(), relationshipKey);
            _detailDataSource.qualifyWithRelationshipKey(relationshipKey, object());
        }
        return _detailDataSource;
    }

    public EORelationship entityRelationship() {
        EOEntity e = EOModelGroup.defaultGroup().entityNamed(object().entityName());
        EORelationship result=e.relationshipNamed(key());
        if (result==null)
            throw new RuntimeException("Could not find relationship for entity "+object().entityName()+" - "+key());
        return result;
    }

    public String relationshipEntityName() { return entityRelationship().destinationEntity().name(); }
    public boolean relationshipIsManditory() {return hasBinding("isMandatory") ? booleanValueForBinding("isMandatory") : entityRelationship().isMandatory(); }

    public EOEnterpriseObject item;

    private NSArray _selectionList;
    public NSArray selectionList() {
        if (_selectionList == null) {
            // Test if we have a binding for selectionListKey
            if (selectionListKey() != null)
                _selectionList = (NSArray)object().valueForKeyPath(selectionListKey());
            else
                _selectionList = ERXConstant.EmptyArray;
        }
        return _selectionList;
    }

    public EOArrayDataSource selectionDataSource() {
        EOArrayDataSource dataSource = new EOArrayDataSource(EOClassDescription.classDescriptionForEntityName(relationshipEntityName()), object().editingContext());
        dataSource.setArray(selectionList());
        return dataSource;
    }

    public CreateEOWithChoicesDelegate createEODelegate() {
        return new CreateEOWithChoicesDelegate();
    }

    public WOComponent add() {
        WOComponent result = null;
        clearValidationFailed();
        CreateEOWithChoicesDelegate createEOWithChoices = createEODelegate();
        createEOWithChoices.object=object();
        createEOWithChoices.key=key();
        createEOWithChoices.followPage=context().page();
        createEOWithChoices.preRelationshipKeys=preRelationshipKeys();
        createEOWithChoices.postRelationshipKeys=postRelationshipKeys();
        createEOWithChoices.postCreateNextPageDelegateKey = postCreateNextPageDelegateKey();
        if (entityNamesForNewInstances() == null || entityNamesForNewInstances().count() == 0) {
            createEOWithChoices.entityNameForNewInstances=relationshipEntityName();
        } else if (entityNamesForNewInstances().count() == 1) {
            createEOWithChoices.entityNameForNewInstances=(String)entityNamesForNewInstances().objectAtIndex(0);
        } else {
            String pickTypePageConfiguration = null;
            if (hasBinding("pickTypePageConfiguration"))
                pickTypePageConfiguration = (String)valueForBinding("pickTypePageConfiguration");
            else
                pickTypePageConfiguration = "SelectPickType" + relationshipEntityName();
            ERDPickPageInterface ppi = (ERDPickPageInterface)D2W.factory().pageForConfigurationNamed(pickTypePageConfiguration, session());
            ppi.setNextPageDelegate(createEOWithChoices);
            ppi.setCancelPage(context().page());
            NSMutableArray choices = new NSMutableArray();
            for (Enumeration e = entityNamesForNewInstances().objectEnumerator(); e.hasMoreElements();) {
                String entityName = (String)e.nextElement();
                String displayNameForEntity = (String)ERDirectToWeb.d2wContextValueForKey("displayNameForEntity", entityName, null);
                if (entityName != null && displayNameForEntity != null) {
                    choices.addObject(new EOCreationMultipleChoice(displayNameForEntity, entityName));
                }
            }
            ppi.setChoices(choices);
            result = (WOComponent)ppi;
        }
        return result != null ? result : createEOWithChoices.nextPage();
    }

    public WOComponent edit() {
        EOEnterpriseObject eo = (EOEnterpriseObject)objectKeyPathValue();
        EditPageInterface epi = null;
        if (eo == null) {
            parent().validationFailedWithException(new NSValidation.ValidationException(errorMessage()),objectPropertyValue(), key());
        } else {
            String editConfigurationName = (String)ERDirectToWeb.d2wContextValueForKey("editConfigurationName", eo.entityName());
            epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed(editConfigurationName, session());
            epi.setNextPage(context().page());
            if (((ERXEnterpriseObject)eo).isNewObject())
                localContext = ERXEC.newEditingContext(object().editingContext(), false);
            else
                localContext = ERXEC.newEditingContext(object().editingContext().parentObjectStore());
            epi.setObject(EOUtilities.localInstanceOfObject(localContext, eo));
            localContext.hasChanges();
        }
        return (WOComponent)epi;
    }

    static class CreateEOWithChoicesDelegate implements NextPageDelegate {
        protected String entityNameForNewInstances;
        protected String currentPageConfiguration;
        protected String postCreateNextPageDelegateKey = null;
        protected String key;
        protected EOEnterpriseObject object;
        protected EOEditingContext localContext;
        protected NSDictionary preRelationshipKeys, postRelationshipKeys;
        protected WOComponent followPage;
        protected NextPageDelegate postCreateNextPageDelegate = null;

        public WOComponent nextPage(WOComponent sender) {
            //entityNameForNewInstances = (String)sender.valueForKey("entityNameForNewInstances");
            entityNameForNewInstances =
            ((EOCreationMultipleChoice)((ERDPickPageInterface)sender).selectedObjects().objectAtIndex(0)).entityName;
            if (log.isDebugEnabled())
                log.debug("Creating "+entityNameForNewInstances);
            if (postCreateNextPageDelegateKey != null) {
                postCreateNextPageDelegate = (NextPageDelegate)sender.valueForKeyPath(postCreateNextPageDelegateKey);
            }
            return nextPage();
        }

        public WOComponent nextPage() {
            EOEnterpriseObject newEO = createEO();
            NSDictionary extraValues=currentPageConfiguration!=null ? new NSDictionary(currentPageConfiguration, "pageConfiguration") : null;
            String createPageConfigurationName = (String)ERDirectToWeb.d2wContextValueForKey("createConfigurationName",
                                                                                             entityNameForNewInstances,
                                                                                             extraValues);
            if (log.isDebugEnabled())
                log.debug("Create Page Config: " + createPageConfigurationName + " for entity: " + entityNameForNewInstances+" - "+extraValues);
            EditPageInterface epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed(createPageConfigurationName,followPage.session());
            epi.setObject(newEO);
            if (postCreateNextPageDelegate == null) {
                ERDEditOwnedRelationship.PostSaveDelegate postSaveDelegate = new PostSaveDelegate();
                postSaveDelegate.postRelationshipKeys=postRelationshipKeys;
                postSaveDelegate.object=object;
                postSaveDelegate.key=key;
                postSaveDelegate.savedObject=newEO;
                postSaveDelegate.localContext=localContext;
                postSaveDelegate.followPage=followPage;
                epi.setNextPageDelegate(postSaveDelegate);
            } else {
                epi.setNextPageDelegate(postCreateNextPageDelegate);
            }
            return (WOComponent)epi;
        }

        public EOEnterpriseObject createEO() {
            // The relationship will be set in the PostSaveDelegate if object is a new eo, ie ok to use a peer context.
            if (object!=null && !(object.editingContext().parentObjectStore() instanceof EOObjectStoreCoordinator)) {
                log.warn("Newly created object is in an editing context that will not save to the database");
            }
            localContext = ERXEC.newEditingContext(object.editingContext().parentObjectStore());
            if (log.isDebugEnabled()) log.debug("Creating "+entityNameForNewInstances);
            EOEnterpriseObject newEO = ERXEOControlUtilities.createAndInsertObject(localContext, entityNameForNewInstances);
            // If the object already exists, then hookup the relationship, if not do it after the object is saved.
            if (!((ERXEnterpriseObject)object).isNewObject()) {
                EOEnterpriseObject localEO = EOUtilities.localInstanceOfObject(localContext, object);
                if (localEO != null)
                    localEO.addObjectToBothSidesOfRelationshipWithKey(newEO, key);
            }
            if (preRelationshipKeys != null) {
                for(Enumeration e = preRelationshipKeys.allKeys().objectEnumerator(); e.hasMoreElements();) {
                    String relationshipKey = (String)e.nextElement();
                    String objectKeyPath = (String)preRelationshipKeys.objectForKey(relationshipKey);
                    EOEnterpriseObject localObject = EOUtilities.localInstanceOfObject(localContext, (EOEnterpriseObject)object.valueForKeyPath(objectKeyPath));
                    localObject.addObjectToBothSidesOfRelationshipWithKey(newEO, relationshipKey);
                }
            }
            localContext.hasChanges(); // Make sure the EC survives.
            return newEO;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Delegate Explanation:
    // This delegate is used to make sure that the object makes it to the database, ie if the user was in a childEC when they
    // hit the edit button or add button, then the change would only be propogated to the session's ec not to the db
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static class PostSaveDelegate implements NextPageDelegate {
        NSDictionary postRelationshipKeys;
        EOEnterpriseObject object;
        EOEnterpriseObject savedObject;
        EOEditingContext localContext;
        WOComponent followPage;
        String key;

        public WOComponent nextPage(WOComponent sender) {
            // If cancel is clicked the ec will be reverted which will set the ec on the inserted object to null
            if (savedObject.editingContext() != null)
                postProcessing();
            return followPage;
        }

        public void postProcessing() {
            if (object != null && savedObject != null && key != null) {
                EOEnterpriseObject localEO = EOUtilities.localInstanceOfObject(object.editingContext(), savedObject), localObject;
                // because this might be null, an array, or an EO
                if ((object.valueForKey(key) != null)&&(object.valueForKey(key) instanceof NSArray)&&(!((NSArray)object.valueForKey(key)).containsObject(localEO)))
                    object.addObjectToBothSidesOfRelationshipWithKey(localEO, key);
                if (object.valueForKey(key) == null)
                    object.addObjectToBothSidesOfRelationshipWithKey(localEO, key);
                if (postRelationshipKeys != null) {
                    for(Enumeration e = postRelationshipKeys.allKeys().objectEnumerator(); e.hasMoreElements();) {
                        String relationshipKey = (String)e.nextElement();
                        String objectKeyPath = (String)postRelationshipKeys.objectForKey(relationshipKey);
                        if (objectKeyPath.equals("this")) {
                            localObject = localEO;
                        } else {
                            localObject = (EOEnterpriseObject)localEO.valueForKeyPath(objectKeyPath);
                        }
                        localObject.addObjectToBothSidesOfRelationshipWithKey(savedObject, relationshipKey);
                    }
                }
            }
        }
    }

    public String noSelectionString() {
        return hasBinding("noSelectionString") ? (String)valueForBinding("noSelectionString"):"- none -";
    }

    public static class EOCreationMultipleChoice {

        public String displayName, entityName;

        public EOCreationMultipleChoice(String displayName, String entityName) {
            this.displayName = displayName;
            this.entityName = entityName;
        }
        @Override
        public String toString() { return displayName; }
    }

    public boolean useForms() { return booleanValueForBinding("useForms"); }
    public boolean doNotUseForm() { return !useForms(); }
}
