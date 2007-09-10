/*
 * Copyright (c) 2000 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_LICENSE_HEADER_START@
 * 
 * Portions Copyright (c) 2000 Apple Computer, Inc.  All Rights
 * Reserved.  This file contains Original Code and/or Modifications of
 * Original Code as defined in and that are subject to the Apple Public
 * Source License Version 1.1 (the "License").  You may not use this file
 * except in compliance with the License.  Please obtain a copy of the
 * License at http://www.apple.com/publicsource and read it before using
 * this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON- INFRINGEMENT.  Please see the
 * License for the specific language governing rights and limitations
 * under the License.
 * 
 * @APPLE_LICENSE_HEADER_END@
 */
package er.extensions;
//package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.util.Enumeration;

/**
 * (Back port from WO 5 WOExtensions)<br />
 * 
 */

public class WOToOneRelationship extends WOComponent {

    // ** passed-in (required)
    String _sourceEntityName;
    String _relationshipKey;
    Object _sourceObject;
    // ** passed-in (optional)
    String _destinationDisplayKey;
    EODataSource _dataSource;
    String _uiStyle;
    boolean _isMandatory;
    boolean _localizeDisplayKeysRead;
    // ** internal
    Object theCurrentItem;
    NSArray _privateList;
    Object _privateSelection;

    public String _noneString;

    public WOToOneRelationship(WOContext aContext)  {
        super(aContext);
    }

    public String noneString() {
        if(_noneString == null) {
            _noneString = localizer().localizedStringForKey("WOToOneRelationship.noneString");
        }
        return _noneString;
    }
    
    public boolean isStateless() {
        return true;
    }

    public String sourceEntityName()
    {
        return _sourceEntityName;
    }

    public void setSourceEntityName(String aValue) {
        _sourceEntityName = aValue;
    }

    public String relationshipKey()
    {
        return _relationshipKey;
    }

    public void setRelationshipKey(String aValue) {
        _relationshipKey = aValue;
    }

    public Object sourceObject()
    {
        return _sourceObject;
    }

    public void setSourceObject(Object aValue) {
        _sourceObject = aValue;
    }

    public String destinationDisplayKey()
    {
        return _destinationDisplayKey;
    }

    public void setDestinationDisplayKey(String aValue) {
        _destinationDisplayKey = aValue;
    }

    public EODataSource dataSource()
    {
        return _dataSource;
    }

    public void setDataSource(EODataSource aValue) {
        _dataSource = aValue;
    }

    public String uiStyle()
    {
        return _uiStyle;
    }

    public void setUiStyle(String aValue) {
        _uiStyle = aValue;
    }

    boolean isMandatory()
    {
        return _isMandatory;
    }

    public void setIsMandatory(Object aValue) {
        try {
            _isMandatory = ERXValueUtilities.booleanValue(aValue);
        } catch (Throwable e) {
            String error = "WOToOneRelationship (setIsMandatory) - unable to set isMandatory value "+e.getMessage();
            NSLog.err.appendln(error);
        }
    }

//////////////////////////////////////////////////
// The following are used internally/privately
//////////////////////////////////////////////////
    public Object theCurrentItem()
    {
        return theCurrentItem;
    }

    public void setTheCurrentItem(Object aValue) {
        theCurrentItem = aValue;
    }

    protected NSArray _privateList()
    {
        return _privateList;
    }

    public void set_privateList(NSArray aValue)
    {
        _privateList = aValue;
    }

    public Object _privateSelection()
    {
        return _privateSelection;
    }

    public void set_privateSelection(Object aValue) {
        _privateSelection = aValue;
    }

    protected void _invalidateCaches() {
        // In order for this to behave like an element, all instance
        // variables need to be flushed when this component sleeps
        // so that it will pull via association.
        setSourceEntityName(null);
        setRelationshipKey(null);
        setSourceObject(null);
        setDataSource(null);
        setDestinationDisplayKey(null);
        setUiStyle(null);
        setIsMandatory(null);
        setTheCurrentItem(null);
        set_privateList(null);
        set_privateSelection(null);
        _noneString = null;
        _localizeDisplayKeysRead = false;
    }

    public void reset() {
        _invalidateCaches();
    }

///////////////////////
// Internal Accessors
///////////////////////
    protected Object _localSourceObject()
    {
        if (null==sourceObject()) {
            setSourceObject(valueForBinding("sourceObject"));
            if (null==sourceObject()) {
                throw new IllegalStateException("<"+getClass().getName()+" sourceObject binding required. sourceObject value is nil or missing>");
            }
        }
        return sourceObject();
    }


    protected boolean _localIsMandatory() {
        if (!isMandatory()) {
            Object aValue = valueForBinding("isMandatory");
            setIsMandatory(aValue);
        }
        return isMandatory();
    }

    protected String _localSourceEntityName() {
        if (null==sourceEntityName()) {
            setSourceEntityName((String)valueForBinding("sourceEntityName"));
            if (null==sourceEntityName()) {
                throw new IllegalStateException("<"+getClass().getName()+" sourceEntityName binding required. sourceEntityName value is nil or missing>");
            }
        }
        return sourceEntityName();
    }

    protected String _localRelationshipKey()
    {
        if (null==relationshipKey()) {
            setRelationshipKey((String)valueForBinding("relationshipKey"));
            if (null==relationshipKey()) {
                throw new IllegalStateException("<"+getClass().getName()+" relationshipKey binding required. relationshipKey value is nil or missing>");
            }
        }
        return relationshipKey();
    }

    protected String _localDestinationDisplayKey() {
        String destinationDisplayKey = destinationDisplayKey();
        if (null==destinationDisplayKey) {
            setDestinationDisplayKey( (String)valueForBinding("destinationDisplayKey"));
            if (null==destinationDisplayKey()) {
                setDestinationDisplayKey("userPresentableDescription");
            }
            return destinationDisplayKey();
        } else
            return destinationDisplayKey;
    }


    public EOEntity entityWithEntityAndKeyPath(EOEntity entity, String keyPath) {
        NSArray keys= NSArray.componentsSeparatedByString(keyPath, ".");
        Enumeration keysEnumerator = keys.objectEnumerator();
        String key=null;
        EOEntity result=entity;
        while (keysEnumerator.hasMoreElements()) {
            key = (String)keysEnumerator.nextElement();
            result = result.relationshipNamed(key).destinationEntity();
        }
        return result;
    }

    protected EODataSource _localDataSource() {
        EODataSource dataSource = dataSource();
        if (dataSource == null) {
            dataSource = (EODatabaseDataSource)valueForBinding("dataSource");
            if (dataSource == null) {
                String anEntityName = _localSourceEntityName();
                Object _source = _localSourceObject();
                EOEditingContext anEditingContext = null;
                if (_source instanceof EOEnterpriseObject) {
                    anEditingContext = ((EOEnterpriseObject)_source).editingContext();
                }
                if (anEditingContext == null) {
                    anEditingContext = session().defaultEditingContext() ;
                }

                EOEntity anEntity = ERXEOAccessUtilities.entityNamed(anEditingContext, anEntityName);
                if (anEntity == null) {
                    throw new IllegalStateException("<" + getClass().getName() + " could not find entity named " + anEntityName + ">");
                }

                EOEntity destinationEntity = entityWithEntityAndKeyPath(anEntity, _localRelationshipKey());
                String destinationEntityName = destinationEntity.name();

                if( ERXEOAccessUtilities.entityWithNamedIsShared(anEditingContext, destinationEntityName) ) {
                    EOArrayDataSource arrayDataSource = new EOArrayDataSource(destinationEntity.classDescriptionForInstances(), anEditingContext);
                    NSArray sharedEOs = ERXEOControlUtilities.sharedObjectsForEntityNamed(destinationEntityName);

                    arrayDataSource.setArray(sharedEOs);
                    dataSource = arrayDataSource;
                } else {
                    dataSource = new EODatabaseDataSource(anEditingContext, destinationEntity.name());
                }
            }
            setDataSource(dataSource);
        }

        return dataSource;
    }

    protected Object _localUiStyle() {
        if (null== uiStyle()) {
            setUiStyle((String)valueForBinding("uiStyle"));
            // if still no value let's determine one
            if (null==uiStyle()) {
                int aSize = theList().count();
                if (aSize < 5) {
                    setUiStyle("radio");
                }
                if ((aSize >= 5) && (aSize < 20)) {
                    setUiStyle("popup");
                }
                if (aSize >= 20) {
                    setUiStyle("browser");
                }
            }
        }
        return uiStyle();
    }

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
        if (anEO!=null) {
            if (isDictionary) {
                _dictionary.setObjectForKey(anEO, masterKey);
            } else {
                _eo.addObjectToBothSidesOfRelationshipWithKey((EOEnterpriseObject )anEO,masterKey);
            }
        } else { // setting to "nil"
            if (isDictionary) {
                _dictionary.removeObjectForKey(masterKey);
            } else {
                _eo.removeObjectFromBothSidesOfRelationshipWithKey((EOEnterpriseObject )_eo.valueForKey(masterKey), masterKey);
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

        if (anEO==noneString()) {
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
            set_privateSelection( NSKeyValueCoding.Utility.valueForKey(_localSourceObject(), _localRelationshipKey()));
        }
        // deal with isMandatory
        if ((_privateSelection()==null) && !_localIsMandatory()) {
            setSelection(noneString());
        }
        return _privateSelection();
    }

    public NSArray theList() {
        NSMutableArray aSortedArray;
        NSArray anUnsortedArray;
        if (_privateList()==null) {
            EODataSource aDataSource = _localDataSource();
            EOEditingContext ec = aDataSource.editingContext();
            ec.lock();
            try {
            	anUnsortedArray = aDataSource.fetchObjects();
            }  finally {
            	ec.unlock();
            }
            // 81398 sort contents
            aSortedArray = new NSMutableArray(anUnsortedArray);
            try {
                // WO 5
                //_RelationshipSupport._sortEOsUsingSingleKey(aSortedArray, _localDestinationDisplayKey());
                ERXArrayUtilities.sortArrayWithKey(aSortedArray, _localDestinationDisplayKey());
           // WO 5    
            //catch (NSComparator.ComparisonException e)
            //    throw new NSForwardException(e);
            } catch (Exception e) {
                new RuntimeException("Exception: " + e.getMessage());
            }

            if (!_localIsMandatory()) {
                aSortedArray.insertObjectAtIndex(noneString(), 0);
            }
            set_privateList(aSortedArray);
        }
        return _privateList();
    }

    public void setTheList(NSArray aValue) {
    }

    public static boolean localizeDisplayKeysDefault = ERXValueUtilities.booleanValueWithDefault(System.getProperty("er.extensions.WOToOneRelationship.localizeDisplayKeysDefault"), false);
    boolean _localizeDisplayKeys;
    public boolean localizeDisplayKeys() {
        if(!_localizeDisplayKeysRead) {
            _localizeDisplayKeysRead = true;
            _localizeDisplayKeys = ERXValueUtilities.booleanValueForBindingOnComponentWithDefault("localizeDisplayKeys", this, localizeDisplayKeysDefault);
        }
        return _localizeDisplayKeys;
    }

    public ERXLocalizer localizer() {
        return ERXLocalizer.localizerForSession(session());
    }

    public Object theCurrentValue() {
        // handle the case where it's the - none - string
        if (theCurrentItem==noneString()) {
            return theCurrentItem;
        }
        Object currentValue = NSKeyValueCoding.Utility.valueForKey(theCurrentItem , _localDestinationDisplayKey());
        if(localizeDisplayKeys()) {
            String stringValue;
            if(!(currentValue instanceof String))
                stringValue = (String)currentValue;
            else
                stringValue = currentValue.toString();
            stringValue = localizer().localizedStringForKeyWithDefault(stringValue);
            return stringValue;
        }
        return currentValue;
    }
    
    public boolean isRadio() {
        if (_localUiStyle().equals("radio")) {
            return true;
        }
        return false;
    }

    public boolean isPopup() {
        if (_localUiStyle().equals("popup")) {
            return true;
        }
        return false;
    }

    public boolean isBrowser() {
        if (_localUiStyle().equals("browser")) {
            return true;
        }
        return false;
    }
}
