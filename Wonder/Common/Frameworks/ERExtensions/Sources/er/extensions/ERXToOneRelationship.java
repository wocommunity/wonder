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
import org.apache.log4j.Category;

public class ERXToOneRelationship extends WOToOneRelationship {

    public ERXToOneRelationship(WOContext context) {
        super(context);
    }
    
    //////////////////////////////////////////  log4j category  ///////////////////////////////////////////
    public final static Category cat = Category.getInstance(ERXToOneRelationship.class);

    // ** passed-in (optional)
    protected String _destinationSortKey;
    protected String _noSelectionString;

    public String destinationSortKey() { return _destinationSortKey; }
    public void setDestinationSortKey(String aValue) { _destinationSortKey = aValue; }

    public String noSelectionString() { return _noSelectionString != null ? _noSelectionString : noneString(); }
    public void setNoSelectionString(String aValue) { _noSelectionString = aValue; }

    protected void _invalidateCaches() {
        // In order for this to behave like an element, all instance
        // variables need to be flushed when this component sleeps
        // so that it will pull via association.
        super._invalidateCaches();
        setNoSelectionString(null);
        setDestinationSortKey(null);
    }

    protected EODataSource _localDataSource() {
        if (null==dataSource()) {
            setDataSource((EODataSource)valueForBinding("dataSource"));
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
                    destinationEntity = relationship != null ? relationship.destinationEntity() : null;
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

    protected String _localSortKey() {
        if (destinationSortKey() == null) {
            setDestinationSortKey((String)valueForBinding("destinationSortKey"));
            if (destinationSortKey() == null)
                setDestinationSortKey(destinationDisplayKey());
        }
        return destinationSortKey();
    }

    protected Object _localRelativeSourceObject() {
        Object relativeSourceObject = null;
        if (_localSourceObject() instanceof EOEnterpriseObject && hasKeyPath()) {
            String partialKeyPath=ERXStringUtilities.keyPathWithoutLastProperty(_localRelationshipKey());
            relativeSourceObject = ((EOEnterpriseObject)_localSourceObject()).valueForKeyPath(partialKeyPath);
        }
        return relativeSourceObject != null ? relativeSourceObject : _localSourceObject();
    }

    protected String _localRelativeRelationshipKey() {
        return hasKeyPath() ? ERXStringUtilities.lastPropertyKeyInKeyPath(_localRelationshipKey()) : _localRelationshipKey();
    }

    protected boolean hasKeyPath() { return _localRelationshipKey().indexOf(".") != -1; }

    /*
     *  -updateSourceObject does the real work here updating
     * the relationship (or setting the keys for a query).
     */

    public void updateSourceObject(Object anEO) {
        String masterKey = _localRelationshipKey();
        Object aSourceObject = _localSourceObject();
        boolean isDictionary = (aSourceObject instanceof NSMutableDictionary);
        NSMutableDictionary _dictionary = (isDictionary) ? (NSMutableDictionary)aSourceObject : null;
        EOEnterpriseObject _eo = !(isDictionary) ? (EOEnterpriseObject)aSourceObject : null;
        EOEnterpriseObject localEO = !isDictionary && anEO != null ?
            ERXUtilities.localInstanceOfObject(_eo.editingContext(), (EOEnterpriseObject)anEO) : null;
        // Need to handle the keyPath situation.
        if (_eo != null && masterKey.indexOf(".") != -1) {
            String partialKeyPath=ERXStringUtilities.keyPathWithoutLastProperty(masterKey);
            _eo = (EOEnterpriseObject)_eo.valueForKeyPath(partialKeyPath);
            masterKey = ERXStringUtilities.lastPropertyKeyInKeyPath(masterKey);
        }
        if (anEO!=null) {
            if (isDictionary) {
                _dictionary.setObjectForKey(anEO, masterKey);
            } else if (_eo.valueForKeyPath(masterKey) != localEO) {
                _eo.addObjectToBothSidesOfRelationshipWithKey((EOEnterpriseObject)anEO, masterKey);
            }
        } else { // setting to "nil"
            if (isDictionary) {
                _dictionary.removeObjectForKey(masterKey);
            } else if (_eo.valueForKey(masterKey) == null) {
                // WO5FIXME
                //|| _eo.valueForKey(masterKey) != EONullValue.nullValue()) {
                _eo.removeObjectFromBothSidesOfRelationshipWithKey((EOEnterpriseObject )_eo.valueForKey(masterKey), masterKey);
            }
        }
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

    ////////////////////////////////////
    //  Accessed through HTML and WOD
    ////////////////////////////////////

    /*
     *  -selection and -setSelection: are called by WOF when
     * syncing up the contents of this component.  These are
     * accessed only through the declarations.
     */

    public void setSelection(Object anEO) {
        Object aValue = null;

        // deal with array when ui is browser
        if ((anEO!=null) && (anEO instanceof NSArray)) {
            NSArray anEOArray = (NSArray)anEO;
            if (anEOArray.count() == 0) {
                anEO = null;
            } else {
                anEO = anEOArray.objectAtIndex(0);
            }
        }

        if (anEO==noSelectionString()) {
            aValue = null;
        } else {
            aValue = anEO;
        }

        set_privateSelection(aValue);
        // this set method needs to trigger the setSourceObject:
        // it's the only way our value will get back into the parent
        updateSourceObject(aValue);
    }

    public Object selection() {
        if (_privateSelection()==null) {
            set_privateSelection(NSKeyValueCoding.Utility.valueForKey(_localSourceObject(), _localRelationshipKey()));
        }
        // deal with isMandatory
        if ((_privateSelection()==null) && !_localIsMandatory()) {
            setSelection(noSelectionString());
        }
        return _privateSelection();
    }

    public NSArray theList() {
        NSMutableArray aSortedArray;
        NSArray anUnsortedArray;
        if (_privateList()==null ) {
            EODataSource aDataSource = _localDataSource();
            // Need to make sure that the eos are in the right editingContext.
	    // ak: We get a dictionary as object if we use this in a query,
	    // so we have to check....
	    EOEditingContext ec;
	    
	    if(_sourceObject instanceof EOEnterpriseObject && ((EOEnterpriseObject)_sourceObject).editingContext() != null)
		ec = ((EOEnterpriseObject)_sourceObject).editingContext();
	    else
		ec = session().defaultEditingContext();
	    
	    anUnsortedArray = ERXUtilities.localInstancesOfObjects(ec, aDataSource.fetchObjects());
            // 81398 sort contents
            aSortedArray = new NSMutableArray(anUnsortedArray);
            /* WO5
                try {
                    _RelationshipSupport._sortEOsUsingSingleKey(aSortedArray, _localSortKey());
                } catch (NSComparator.ComparisonException e) {
                    throw new NSForwardException(e);
                }
            */
            if (_localSortKey()!=null && _localSortKey().length()>0)
                ERXUtilities.sortEOsUsingSingleKey(aSortedArray, _localSortKey());

            // if there is a value on the EO, then we need to make sure that the list's EOs are in the same EC
            // otherwise the popup selection will be wrong (will default to the first element)
            // this happens for ex on a wizard page with a popup. Select sth in the popup, but leave a mandatory field blank
            // click next --> the page comes back with the error, but the popup lost the selection you made
            if (_localSourceObject() instanceof EOEnterpriseObject &&
                ((EOEnterpriseObject)_localSourceObject()).valueForKeyPath(_localRelationshipKey()) != null) {
                NSMutableArray localArray= new NSMutableArray();
                EOEnterpriseObject eo;
                ec = ((EOEnterpriseObject)_localSourceObject()).editingContext();
                for (Enumeration e = aSortedArray.objectEnumerator(); e.hasMoreElements();) {
                    eo = (EOEnterpriseObject)e.nextElement();
                    localArray.addObject((ec != eo.editingContext() && ERXUtilities.localInstanceOfObject(ec, eo) != null ?
                                          ERXUtilities.localInstanceOfObject(ec, eo) : eo));
                }
                aSortedArray=localArray;
            }

            if (!_localIsMandatory()) {
                if(noSelectionString() != null)
                    aSortedArray.insertObjectAtIndex(noSelectionString(), 0);
            }
            set_privateList(aSortedArray);
        }
        return _privateList();
    }

    public Object theCurrentValue() {
        if (theCurrentItem==noSelectionString()) {
            return theCurrentItem;
        }
        return super.theCurrentValue();
    }
}
