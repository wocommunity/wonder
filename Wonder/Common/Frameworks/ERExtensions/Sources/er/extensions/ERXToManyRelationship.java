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
import com.webobjects.directtoweb.*;
import java.util.Enumeration;
import org.apache.log4j.Category;

public class ERXToManyRelationship extends WOToManyRelationship {

    public ERXToManyRelationship(WOContext context) {
        super(context);
    }

    ////////////////////////////////////////////// log4j category //////////////////////////////////////////////////
    public static final Category cat = Category.getInstance(ERXToManyRelationship.class);

    public boolean isStateless() { return true; }

    public void reset() {
        super.reset();
        _destinationSortKey = null;
    }
    
    protected String _destinationSortKey;

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
            if (destinationSortKey() == null)
                setDestinationSortKey(destinationDisplayKey());
        }
        return destinationSortKey();
    }
    
    protected EODataSource _localDataSource() {
        if (null==dataSource()) {
            setDataSource((EODatabaseDataSource)valueForBinding("dataSource"));
            if (dataSource() == null) {
                String anEntityName = _localSourceEntityName();
                // FIXME (msanchez, 08/00, 2520053): use modelGroup on ObjectStoreCoordinator of our editingContext
                EOModelGroup aModelGroup = EOModelGroup.defaultGroup();
                EOEntity anEntity = aModelGroup.entityNamed(anEntityName);

                if (anEntity == null) {
                    throw new IllegalStateException("<" + getClass().getName() + " could not find entity named " + anEntityName + ">");
                }
                EOEntity destinationEntity = null;
                EOEditingContext anEditingContext = null;
                Object _source = _localSourceObject();                
                if (_source instanceof EOEnterpriseObject) {
                    EORelationship relationship = ERXUtilities.relationshipWithObjectAndKeyPath((EOEnterpriseObject)_source,
                                                                                               _localRelationshipKey());
                    destinationEntity = relationship.destinationEntity();
                    anEditingContext = ((EOEnterpriseObject)_source).editingContext();
                }
                if (destinationEntity == null)
                    destinationEntity = entityWithEntityAndKeyPath(anEntity, _localRelationshipKey());
                if (anEditingContext == null) {
                    anEditingContext = session().defaultEditingContext() ;
                }
                NSArray possibleChoices = (NSArray)valueForBinding("possibleChoices");
                if (possibleChoices != null) {
                    EOArrayDataSource ads = new EOArrayDataSource(destinationEntity.classDescriptionForInstances(), anEditingContext);
                    ads.setArray(possibleChoices);
                    setDataSource(ads);
                } else {
                    EODatabaseDataSource aDatabaseDataSource = new EODatabaseDataSource(anEditingContext, destinationEntity.name());
                    if (hasBinding("qualifier"))
                        aDatabaseDataSource.setAuxiliaryQualifier((EOQualifier)valueForBinding("qualifier"));
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
            anUnsortedArray = ERXUtilities.localInstancesOfObjects(((EOEnterpriseObject)sourceObject()).editingContext(), aDataSource.fetchObjects());
            // 81398 sort contents
            aSortedArray = new NSMutableArray(anUnsortedArray);
            /* WO5
            try {
                _RelationshipSupport._sortEOsUsingSingleKey(aSortedArray, _localSortKey());
            } catch (NSComparator.ComparisonException e) {
                throw new NSForwardException(e);
            }
            */
            if (_localSortKey() != null && _localSortKey().length() > 0)
                ERXUtilities.sortEOsUsingSingleKey(aSortedArray, _localSortKey());

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
                    localArray.addObject((ec != eo.editingContext() && ERXUtilities.localInstanceOfObject(ec, eo) != null ?
                                          ERXUtilities.localInstanceOfObject(ec, eo) : eo));
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
            Object value = localObject.valueForKey(_localRelativeRelationshipKey());
            try {
                localObject.validateValueForKey(value, _localRelativeRelationshipKey());
            } catch (NSValidation.ValidationException eov) {
                parent().validationFailedWithException(eov, value, _localRelativeRelationshipKey());
            }
        }
    }

    public void updateSourceObject(NSArray newValues) {
        // If no selections are choosen then newValues is null.
        newValues = newValues != null ? newValues : ERXConstant.EmptyArray;
        String masterKey = _localRelationshipKey();
        Object aSourceObject = _localSourceObject();
        boolean isDictionary = (aSourceObject instanceof NSMutableDictionary);
        NSMutableDictionary _dictionary = (isDictionary) ? (NSMutableDictionary)aSourceObject : null;
        EOEnterpriseObject _eo = !(isDictionary) ? (EOEnterpriseObject)aSourceObject : null;
        newValues = ERXUtilities.localInstancesOfObjects(_eo.editingContext(), newValues);
        // Need to handle the keyPath situation.
        if (_eo != null && masterKey.indexOf(".") != -1) {
            String partialKeyPath=KeyValuePath.keyPathWithoutLastProperty(masterKey);
            _eo = (EOEnterpriseObject)_eo.valueForKeyPath(partialKeyPath);
            masterKey = KeyValuePath.lastPropertyKeyInKeyPath(masterKey);
        }
        NSMutableArray currentValues = (NSMutableArray)NSKeyValueCoding.Utility.valueForKey(_eo, masterKey);
        int count = currentValues.count();
        EOEnterpriseObject o;
        for (int i = count - 1; i >= 0; i--) {
            o = (EOEnterpriseObject)currentValues.objectAtIndex(i);
            if ((null==newValues) || (newValues.indexOfIdenticalObject(o) == NSArray.NotFound)) { // not found
                if (isDictionary) {
                    currentValues.removeObject(o);
                }
                else {
                    _eo.removeObjectFromBothSidesOfRelationshipWithKey(o, masterKey);
                }
            }
        }
        count = newValues.count();
        if ((isDictionary) && (currentValues==null)) {
            currentValues = new NSMutableArray(count);
            _dictionary.setObjectForKey(currentValues, masterKey);
        }
        for (int i = count - 1; i >= 0; i--) {
            o = (EOEnterpriseObject)newValues.objectAtIndex(i);
            if ((null==currentValues) || (currentValues.indexOfIdenticalObject(o) == NSArray.NotFound)) {  // not found
                if (isDictionary) {
                    currentValues.addObject(o);
                } else {
                    _eo.addObjectToBothSidesOfRelationshipWithKey(o, masterKey);
                }
            }
        }
    }
    
    protected Object _localRelativeSourceObject() {
        Object relativeSourceObject = null;
        if (_localSourceObject() instanceof EOEnterpriseObject && hasKeyPath()) {
            String partialKeyPath=KeyValuePath.keyPathWithoutLastProperty(_localRelationshipKey());
            relativeSourceObject = ((EOEnterpriseObject)_localSourceObject()).valueForKeyPath(partialKeyPath);
        }
        return relativeSourceObject != null ? relativeSourceObject : _localSourceObject();
    }

    protected boolean hasKeyPath() { return _localRelationshipKey().indexOf(".") != -1; }

    protected String _localRelativeRelationshipKey() {
        return hasKeyPath() ? KeyValuePath.lastPropertyKeyInKeyPath(_localRelationshipKey()) : _localRelationshipKey();
    }
}
