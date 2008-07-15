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

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import er.extensions.ERXUtilities;
import java.util.Enumeration;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
// This is a back port of WOToManyRelationship from WO 5 WOExtensions to 4.5.  The only changes that have been
// made are due to api changes between the two.
////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
/**
 * Only useful in that it uses ERXRadioButtonMatrix and ERCheckboxMatrix components.<br />
 * 
 */

public class WOToManyRelationship extends WOComponent {

    public WOToManyRelationship(WOContext aContext) {
        super(aContext);
    }
    
    // ** passed-in (required)
    String _sourceEntityName;
    String _relationshipKey;
    Object _sourceObject;
    // ** passed-in (optional)
    String _destinationDisplayKey;
    EODataSource _dataSource;
    String _uiStyle;
    boolean _isMandatory;

    // ** internal
    Object theCurrentItem;
    NSArray _privateList;
    NSArray _privateSelections;
    boolean _localizeDisplayKeysRead;

/////////////////////////////////////////////////////////////////
// The following may be set/passed-in by user of this component
/////////////////////////////////////////////////////////////////

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

    protected void set_privateList(NSArray aValue)
    {
        _privateList = aValue;
    }

    public NSArray _privateSelections()
    {
        return _privateSelections;
    }

    public void set_privateSelections(NSArray aValue) {
        _privateSelections = aValue;
    }

    public void _invalidateCaches() {
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
        set_privateSelections(null);
        _localizeDisplayKeysRead = false;
    }

    public void reset() {
        _invalidateCaches();
    }

    ///////////////////////
    // Internal Accessors
    ///////////////////////
    protected String _localSourceEntityName() {
        if (null==sourceEntityName()) {
            setSourceEntityName((String)valueForBinding("sourceEntityName"));
            if (null==sourceEntityName()) {
                throw new IllegalStateException("<"+getClass().getName()+" sourceEntityName binding required. sourceEntityName value is nil or missing");
            }
        }
        return sourceEntityName();
    }

    protected String _localRelationshipKey()
    {
        if (null==relationshipKey()) {
            setRelationshipKey((String)valueForBinding("relationshipKey"));
            if (null==relationshipKey()) {
                throw new IllegalStateException("<"+getClass().getName()+" relationshipKey binding required. relationshipKey value is nil or missing");
            }
        }
        return relationshipKey();
    }

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
            if(result.relationshipNamed(key)!=null)
                result = result.relationshipNamed(key).destinationEntity();                   }
        return result;
    }

    protected EODataSource _localDataSource() {

        if (null==dataSource()) {
            setDataSource((EODatabaseDataSource)valueForBinding("dataSource"));
            if (null==dataSource()) {
                String anEntityName = _localSourceEntityName();
                Object _source = _localSourceObject();
                EOEditingContext anEditingContext = null;
                EOEntity destinationEntity = null;
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
                if (_source instanceof EOEnterpriseObject) {
                    EORelationship relationship = ERXUtilities.relationshipWithObjectAndKeyPath((EOEnterpriseObject)_source, _localRelationshipKey());
                    destinationEntity = relationship != null ? relationship.entity() : null;
                } else {
                    destinationEntity = entityWithEntityAndKeyPath(anEntity, _localRelationshipKey());
                }
                EODatabaseDataSource aDatabaseDataSource = new EODatabaseDataSource(anEditingContext, destinationEntity.name());
                setDataSource(aDatabaseDataSource);
            }
        }

        return dataSource();
    }

    protected Object _localUiStyle() {
        if (null== uiStyle()) {
            setUiStyle((String)valueForBinding("uiStyle"));
            // if still no value let's determine one
            if (null==uiStyle()) {
                int aSize = theList().count();
                if (aSize <= 5) {
                    setUiStyle("checkbox");
                }
                if (aSize > 5) {
                    setUiStyle("browser");
                }
            }
        }
        return uiStyle();
    }

    protected boolean _localIsMandatory() {
        if (!isMandatory()) {
            Object aValue = valueForBinding("isMandatory");
            setIsMandatory(aValue);
        }
        return isMandatory();
    }

    /*
    *  -updateSourceObject does the real work here updating
    * the relationship (or setting the keys for a query).
    */

    public void updateSourceObject(NSArray newValues) {
        // add new values to relationship, remove old values
        Object aSourceObject = _localSourceObject();
        boolean isDictionary = (aSourceObject instanceof NSMutableDictionary);
        NSMutableDictionary _dictionary = (isDictionary) ? (NSMutableDictionary)aSourceObject : null;
        EOEnterpriseObject _eo = !(isDictionary) ? (EOEnterpriseObject)aSourceObject : null;
        String masterKey = _localRelationshipKey();
        NSMutableArray currentValues = (NSMutableArray)NSKeyValueCodingAdditions.Utility.valueForKeyPath(aSourceObject, masterKey);
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
            // WO 5
            //currentValues = new NSMutableArray(count);
            currentValues = new NSMutableArray();
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


    ////////////////////////////////////
    //  Accessed through HTML and WOD
    ////////////////////////////////////

    /*
    *  -selections and -setSelections: are called by WOF when
    * syncing up the contents of this component.  These are
    * accessed only through the declarations.
    */
    public void setSelections(NSArray aValue) {
        NSArray aTempValue = aValue;
        // set selections to nil if it's an empty array
        if ((aValue==null) || (aValue.count() == 0)) {
            if (isMandatory() && (theList().count() > 0)) {
                Object anObject = theList().objectAtIndex(0);
                aTempValue = new NSArray(anObject);
            } else {
                aTempValue = null;
            }
        }
        // deal with isMandatory
        set_privateSelections(aTempValue);
        updateSourceObject(aTempValue);
    }

    public NSArray selections() {
        if (_privateSelections()==null) {
            set_privateSelections((NSArray)NSKeyValueCodingAdditions.Utility.valueForKeyPath(_localSourceObject(), _localRelationshipKey()));
            // deal with isMandatory
            if ((_privateSelections()==null) && _localIsMandatory()) {
                if (theList().count() > 0) {
                    Object anObject = theList().objectAtIndex(0);
                    set_privateSelections(new NSArray(anObject));
                }
            }
        }
        return _privateSelections();
    }

    public NSArray theList() {
        NSMutableArray aSortedArray;
        // ** This is cached because WOBrowser and WOCheckBoxList
        // ** might ask for list many times.
        if (_privateList()==null) {
            // 81398 sort contents
            aSortedArray = (NSMutableArray)_localDataSource().fetchObjects().mutableClone();
            try {
                // WO 5
                //_RelationshipSupport._sortEOsUsingSingleKey(aSortedArray, _localDestinationDisplayKey());
                ERXArrayUtilities.sortArrayWithKey(aSortedArray, _localDestinationDisplayKey());
            // WO 5    
            // catch (NSComparator.ComparisonException e)
            //    throw new NSForwardException(e);
            } catch (Exception e) {
                throw new RuntimeException("Exception: " + e.getMessage());
            }
            set_privateList(aSortedArray);
        }
        return _privateList();
    }

    public static boolean localizeDisplayKeysDefault = ERXValueUtilities.booleanValueWithDefault(System.getProperty("er.extensions.WOToManyRelationship.localizeDisplayKeysDefault"), false);
    boolean _localizeDisplayKeys;
    public boolean localizeDisplayKeys() {
        if(!_localizeDisplayKeysRead) {
            _localizeDisplayKeysRead = true;
            _localizeDisplayKeys = ERXComponentUtilities.booleanValueForBinding(this, "localizeDisplayKeys", localizeDisplayKeysDefault);
        }
        return _localizeDisplayKeys;
    }

    public ERXLocalizer localizer() {
        return ERXLocalizer.localizerForSession(session());
    }

    public Object theCurrentValue() {
        Object currentValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(theCurrentItem , _localDestinationDisplayKey());
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
    
    public boolean isCheckBox() {
        if (_localUiStyle().equals("checkbox")) {
            return true;
        }
        return false;
    }
}