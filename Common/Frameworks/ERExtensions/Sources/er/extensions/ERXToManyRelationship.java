/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import java.util.Enumeration;

/**
 * description forthcoming!<br />
 * 
 * @binding dataSource
 * @binding destinationDisplayKey
 * @binding isMandatory
 * @binding relationshipKey
 * @binding sourceEntityName
 * @binding destinationEntityName
 * @binding sourceObject
 * @binding uiStyle
 * @binding qualifier
 * @binding possibleChoices
 * @binding maxColumns
 * @binding size
 * @binding width
 * @binding destinationSortKey
 * @binding goingVertically
 * @binding localizeDisplayKeys" defaults="Boolean
 */

public class ERXToManyRelationship extends WOToManyRelationship {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXToManyRelationship.class);

    /** caches the destination sort key */
    protected String _destinationSortKey;
    
    public ERXToManyRelationship(WOContext context) {
        super(context);
    }

    public boolean isStateless() { return true; }

    public void reset() {
        super.reset();
        _destinationSortKey = null;
    }
    
    public NSArray selections() {
        if (_privateSelections() == null && canGetValueForBinding("selectedObjects")) {
            NSArray selectedObjects = (NSArray)valueForBinding("selectedObjects");
            if (selectedObjects != null)
                setSelections(selectedObjects);
        }
        return super.selections();
    }

    public String destinationSortKey() { return _destinationSortKey; }
    public void setDestinationSortKey(String aValue) { _destinationSortKey = aValue; }
    
    protected String _localSortKey() {
        if (destinationSortKey() == null) {
            setDestinationSortKey((String)valueForBinding("destinationSortKey"));
            if (destinationSortKey() == null|| destinationSortKey().length() == 0)
                setDestinationSortKey(_localDestinationDisplayKey());
        }
        return destinationSortKey();
    }

    protected EOEntity destinationEntity() {
        String anEntityName = (String)valueForBinding("destinationEntityName");
        EOEntity destinationEntity = null;

        Object _source = _localSourceObject();
        EOEditingContext anEditingContext = null;

        if (_source instanceof EOEnterpriseObject) {
            anEditingContext = ((EOEnterpriseObject)_source).editingContext();
        }
        if (anEditingContext == null) {
            anEditingContext = session().defaultEditingContext() ;
        }
        
        if(anEntityName == null) {
            anEntityName = _localSourceEntityName();
            EOEntity anEntity = EOUtilities.entityNamed(anEditingContext, anEntityName);
            if (anEntity == null) {
                throw new IllegalStateException("<" + getClass().getName() + " could not find entity named " + anEntityName + ">");
            }
            
            if (_source instanceof EOEnterpriseObject) {
                EORelationship relationship = ERXUtilities.relationshipWithObjectAndKeyPath((EOEnterpriseObject)_source,
                                                                                            _localRelationshipKey());
                if(relationship!=null) {
                    destinationEntity = relationship.destinationEntity();
                }
            }
            if (destinationEntity == null) {
                destinationEntity = entityWithEntityAndKeyPath(anEntity, _localRelationshipKey());
            }
        } else {
            destinationEntity = EOUtilities.entityNamed(anEditingContext, anEntityName);
            if (destinationEntity == null) {
                throw new IllegalStateException("<" + getClass().getName() + " could not find entity named " + anEntityName + ">");
            }
        }

        return destinationEntity;
    }

    

    protected EODataSource _localDataSource() {
        if (null==dataSource()) {
            setDataSource((EODatabaseDataSource)valueForBinding("dataSource"));
            if (dataSource() == null) {
                EOEditingContext anEditingContext = null;
                Object _source = _localSourceObject();

                if (_source instanceof EOEnterpriseObject) {
                    anEditingContext = ((EOEnterpriseObject)_source).editingContext();
                }
                
                if (anEditingContext == null) {
                    anEditingContext = session().defaultEditingContext() ;
                }

                NSArray possibleChoices = (NSArray)valueForBinding("possibleChoices");

                if (possibleChoices != null) {
                    EOArrayDataSource ads = new EOArrayDataSource(destinationEntity().classDescriptionForInstances(), anEditingContext);
                    ads.setArray(possibleChoices);
                    setDataSource(ads);
                } else {
                    EODatabaseDataSource aDatabaseDataSource = new EODatabaseDataSource(anEditingContext, destinationEntity().name());

                    if (hasBinding("qualifier")) {
                        aDatabaseDataSource.setAuxiliaryQualifier((EOQualifier)valueForBinding("qualifier"));
                    }
                    setDataSource(aDatabaseDataSource);
                }
            }
        }
        return dataSource();
    }

    public NSArray theList() {
        NSMutableArray aSortedArray;
        NSArray anUnsortedArray;
        if (_privateList()==null) {
            EODataSource aDataSource = _localDataSource();
            // Need to make sure that the eos are in the right editingContext.
            anUnsortedArray = ERXEOControlUtilities.localInstancesOfObjects(((EOEnterpriseObject)sourceObject()).editingContext(), aDataSource.fetchObjects());
            // 81398 sort contents
            aSortedArray = new NSMutableArray(anUnsortedArray);

            ERXArrayUtilities.sortArrayWithKey(aSortedArray, _localSortKey());

            // if there is a value on the EO, then we need to make sure that the list's EOs are in the same EC
            // otherwise the popup selection will be wrong (will default to the first element)
            // this happens for ex on a wizard page with a popup. Select sth in the popup, but leave a mandatory field blank
            // click next --> the page comes back with the error, but the popup lost the selection you made
            if (_localSourceObject() instanceof EOEnterpriseObject &&
                ((EOEnterpriseObject)_localSourceObject()).valueForKeyPath(_localRelationshipKey()) != null) {
                NSMutableArray localArray= new NSMutableArray();
                EOEnterpriseObject eo;
                EOEditingContext ec = ((EOEnterpriseObject)_localSourceObject()).editingContext();
                for (Enumeration e = aSortedArray.objectEnumerator(); e.hasMoreElements();) {
                    eo = (EOEnterpriseObject)e.nextElement();
                    localArray.addObject((ec != eo.editingContext() && ERXEOControlUtilities.localInstanceOfObject(ec, eo) != null ?
                                          ERXEOControlUtilities.localInstanceOfObject(ec, eo) : eo));
                }
                aSortedArray=localArray;
            }
            set_privateList(aSortedArray);
        }
        return _privateList();
    }

    public void takeValuesFromRequest(WORequest r, WOContext c) {
        // we want to pass the validation here for the case where we are creating a new object
        // and are given isMandatory=0 on a mandatory relationship to force users to pick one..
        super.takeValuesFromRequest(r, c);
        if (_localRelativeSourceObject() instanceof EOEnterpriseObject) {
            EOEnterpriseObject localObject = (EOEnterpriseObject)_localRelativeSourceObject();
            Object value = localObject.valueForKeyPath(_localRelativeRelationshipKey());
            try {
                localObject.validateValueForKey(value, _localRelativeRelationshipKey());
            } catch (NSValidation.ValidationException eov) {
                parent().validationFailedWithException(eov, value, _localRelativeRelationshipKey());
            }
        }
    }

    public void updateSourceObject(NSArray newValues) {
        // If no selections are choosen then newValues is null.
        newValues = newValues != null ? newValues : NSArray.EmptyArray;
        String masterKey = _localRelationshipKey();
        Object aSourceObject = _localSourceObject();
        boolean isDictionary = (aSourceObject instanceof NSMutableDictionary);
        NSMutableDictionary _dictionary = (isDictionary) ? (NSMutableDictionary)aSourceObject : null;
        EOEnterpriseObject _eo = !(isDictionary) ? (EOEnterpriseObject)aSourceObject : null;
        newValues = ERXEOControlUtilities.localInstancesOfObjects(_eo.editingContext(), newValues);
        // Need to handle the keyPath situation.
        if (_eo != null && masterKey.indexOf(".") != -1) {
            String partialKeyPath=ERXStringUtilities.keyPathWithoutLastProperty(masterKey);
            _eo = (EOEnterpriseObject)_eo.valueForKeyPath(partialKeyPath);
            masterKey = ERXStringUtilities.lastPropertyKeyInKeyPath(masterKey);
        }
        NSArray currentValues = (NSArray)NSKeyValueCodingAdditions.Utility.valueForKeyPath(_eo, masterKey);
        NSMutableArray mutableCurrentValues;
        if(currentValues instanceof NSMutableArray) {
            mutableCurrentValues = (NSMutableArray)currentValues;
        } else {
            mutableCurrentValues = currentValues.mutableClone();
            NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(_eo, mutableCurrentValues, masterKey);
        }
        int count = mutableCurrentValues.count();
        EOEnterpriseObject o;
        for (int i = count - 1; i >= 0; i--) {
            o = (EOEnterpriseObject)mutableCurrentValues.objectAtIndex(i);
            if ((null==newValues) || (newValues.indexOfIdenticalObject(o) == NSArray.NotFound)) { // not found
                if (isDictionary) {
                    mutableCurrentValues.removeObject(o);
                }
                else {
                    _eo.removeObjectFromBothSidesOfRelationshipWithKey(o, masterKey);
                }
            }
        }
        count = newValues.count();
        if ((isDictionary) && (mutableCurrentValues==null)) {
            mutableCurrentValues = new NSMutableArray(count);
            _dictionary.setObjectForKey(currentValues, masterKey);
        }
        for (int i = count - 1; i >= 0; i--) {
            o = (EOEnterpriseObject)newValues.objectAtIndex(i);
            if ((null==mutableCurrentValues) || (mutableCurrentValues.indexOfIdenticalObject(o) == NSArray.NotFound)) {  // not found
                if (isDictionary) {
                    mutableCurrentValues.addObject(o);
                } else {
                    _eo.addObjectToBothSidesOfRelationshipWithKey(o, masterKey);
                }
            }
        }
    }
    
    protected Object _localRelativeSourceObject() {
        Object relativeSourceObject = null;
        if (_localSourceObject() instanceof EOEnterpriseObject && hasKeyPath()) {
            String partialKeyPath=ERXStringUtilities.keyPathWithoutLastProperty(_localRelationshipKey());
            relativeSourceObject = ((EOEnterpriseObject)_localSourceObject()).valueForKeyPath(partialKeyPath);
        }
        return relativeSourceObject != null ? relativeSourceObject : _localSourceObject();
    }

    protected boolean hasKeyPath() { return _localRelationshipKey().indexOf(".") != -1; }

    protected String _localRelativeRelationshipKey() {
        return hasKeyPath() ? ERXStringUtilities.lastPropertyKeyInKeyPath(_localRelationshipKey()) : _localRelationshipKey();
    }

    public boolean isBrowser() {
        return !(isCheckBox() || isJSEditor()); // Browser is the default.
    }

    public boolean isJSEditor() {
        return "jsEditor".equalsIgnoreCase(uiStyle());
    }

}
