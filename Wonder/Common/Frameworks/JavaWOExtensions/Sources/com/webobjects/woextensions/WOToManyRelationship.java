/*
 * WOToManyRelationship.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (“Apple”) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under Apple’s copyrights in this original Apple 
 * software (the “Apple Software”), to use, reproduce, modify and 
 * redistribute the Apple Software, with or without modifications, in 
 * source and/or binary forms; provided that if you redistribute the  
 * Apple Software in its entirety and without modifications, you must 
 * retain this notice and the following text and disclaimers in all such
 * redistributions of the Apple Software.  Neither the name, trademarks,
 * service marks or logos of Apple Computer, Inc. may be used to endorse
 * or promote products derived from the Apple Software without specific
 * prior written permission from Apple.  Except as expressly stated in
 * this notice, no other rights or licenses, express or implied, are
 * granted by Apple herein, including but not limited to any patent
 * rights that may be infringed by your derivative works or by other
 * works in which the Apple Software may be incorporated.
 *
 * The Apple Software is provided by Apple on an "AS IS" basis.  APPLE 
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS
 * USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 
 *
 * IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT,
 * INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE,
 * REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE,
 * HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING
 * NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

import java.util.Enumeration;

public class WOToManyRelationship extends WOComponent {

    // ** passed-in (required)
    protected String _sourceEntityName;
    protected String _relationshipKey;
    protected Object _sourceObject;

    // ** passed-in (optional)
    protected String _destinationDisplayKey;
    protected EODatabaseDataSource _dataSource;
    protected String _uiStyle;
    protected boolean _isMandatory;

    // ** internal
    protected Object theCurrentItem;
    protected NSArray _privateList;
    protected NSArray _privateSelections;

/////////////////////////////////////////////////////////////////
// The following may be set/passed-in by user of this component
/////////////////////////////////////////////////////////////////

    public WOToManyRelationship(WOContext aContext)  {
        super(aContext);
    }

    public boolean isStateless() {
        return true;
    }

    public String sourceEntityName() {
        return _sourceEntityName;
    }

    public void setSourceEntityName(String aValue) {
        _sourceEntityName = aValue;
    }

    public String relationshipKey() {
        return _relationshipKey;
    }

    public void setRelationshipKey(String aValue) {
        _relationshipKey = aValue;
    }

    public Object sourceObject() {
        return _sourceObject;
    }

    public void setSourceObject(Object aValue) {
        _sourceObject = aValue;
    }

    public String destinationDisplayKey() {
        return _destinationDisplayKey;
    }

    public void setDestinationDisplayKey(String aValue) {
        _destinationDisplayKey = aValue;
    }

    public EODatabaseDataSource dataSource() {
        return _dataSource;
    }

    public void setDataSource(EODatabaseDataSource aValue) {
        _dataSource = aValue;
    }

    public String uiStyle() {
        return _uiStyle;
    }

    public void setUiStyle(String aValue) {
        _uiStyle = aValue;
    }

    boolean isMandatory() {
        return _isMandatory;
    }

    public void setIsMandatory(Object aValue) {

        try {
            _isMandatory = _WOJExtensionsUtil.booleanValue(aValue);
        }
        catch (Throwable e) {
            NSLog.err.appendln("WOToOneRelationship (setIsMandatory) - unable to set isMandatory value " + e.getMessage());
            NSLog.err.appendln(e);
        }

    }
    
    //////////////////////////////////////////////////
    // The following are used internally/privately
    //////////////////////////////////////////////////

    public Object theCurrentItem() {
        return theCurrentItem;
    }

    public void setTheCurrentItem(Object aValue) {
        theCurrentItem = aValue;
    }

    protected NSArray _privateList() {
        return _privateList;
    }

    public void set_privateList(NSArray aValue) {
        _privateList = aValue;
    }

    public NSArray _privateSelections() {
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
    }

    public void reset() {
        _invalidateCaches();
    }

    ///////////////////////
    // Internal Accessors
    ///////////////////////
    protected String _localSourceEntityName() {

        if (sourceEntityName() == null) {
            setSourceEntityName((String) _WOJExtensionsUtil.valueForBindingOrNull("sourceEntityName", this));

            if (sourceEntityName() == null) {
                throw new IllegalStateException("<" + getClass().getName() + " sourceEntityName binding required. sourceEntityName value is null or missing>");
            }

        }

        return sourceEntityName();
    }

    protected String _localRelationshipKey() {

        if (relationshipKey() == null) {
            setRelationshipKey((String) _WOJExtensionsUtil.valueForBindingOrNull("relationshipKey",this));

            if (relationshipKey() == null) {
                throw new IllegalStateException("<" + getClass().getName() + " relationshipKey binding required. relationshipKey value is null or missing>");
            }

        }

        return relationshipKey();
    }

    protected Object _localSourceObject() {

        if (sourceObject() == null) {
            setSourceObject(valueForBinding("sourceObject"));

            if (sourceObject() == null) {
                throw new IllegalStateException("<" + getClass().getName() + " sourceObject binding required. sourceObject value is null or missing>");
            }

        }

        return sourceObject();
    }

    protected String _localDestinationDisplayKey() {
        String destinationDisplayKey = destinationDisplayKey();

        if (destinationDisplayKey == null) {
            setDestinationDisplayKey((String) _WOJExtensionsUtil.valueForBindingOrNull("destinationDisplayKey", this));

            if (destinationDisplayKey() == null) {
                setDestinationDisplayKey("userPresentableDescription");
            }

            return destinationDisplayKey();
        }

        return destinationDisplayKey;
    }

    public EOEntity entityWithEntityAndKeyPath(EOEntity entity, String keyPath) {
        NSArray keys = NSArray.componentsSeparatedByString(keyPath, ".");
        Enumeration keysEnumerator = keys.objectEnumerator();
        String key = null;
        EOEntity result = entity;

        while (keysEnumerator.hasMoreElements()) {
            key = (String) keysEnumerator.nextElement();
            result = result.relationshipNamed(key).destinationEntity();
        }

        return result;
    }

    protected EODataSource _localDataSource() {

        if (dataSource() == null) {
            setDataSource((EODatabaseDataSource) _WOJExtensionsUtil.valueForBindingOrNull("dataSource", this));

            if (dataSource() == null) {
                String anEntityName = _localSourceEntityName();
                EOModelGroup aModelGroup = EOModelGroup.defaultGroup();
                EOEntity anEntity = aModelGroup.entityNamed(anEntityName);

                if (anEntity == null) {
                    throw new IllegalStateException("<" + getClass().getName() + " could not find entity named " + anEntityName + ">");
                }
                
                EOEntity destinationEntity = entityWithEntityAndKeyPath(anEntity, _localRelationshipKey());
                Object _source = _localSourceObject();
                EOEditingContext anEditingContext = null;

                if (_source instanceof EOEnterpriseObject) {
                    anEditingContext = ((EOEnterpriseObject) _source).editingContext();
                }

                if (anEditingContext == null) {
                    anEditingContext = session().defaultEditingContext();
                }

                EODatabaseDataSource aDatabaseDataSource = new EODatabaseDataSource(anEditingContext, destinationEntity.name());
                setDataSource(aDatabaseDataSource);
            }

        }

        return dataSource();
    }

    protected Object _localUiStyle() {

        if (uiStyle() == null) {
            setUiStyle((String) _WOJExtensionsUtil.valueForBindingOrNull("uiStyle", this));

            // if still no value let's determine one
            if (uiStyle() == null) {
                setUiStyle((theList().count() <= 5) ? "checkbox" : "browser");
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
        NSMutableDictionary _dictionary = (isDictionary) ? (NSMutableDictionary) aSourceObject : null;
        EOEnterpriseObject _eo = (!isDictionary) ? (EOEnterpriseObject) aSourceObject : null;
        String masterKey = _localRelationshipKey();
        NSMutableArray currentValues = (NSMutableArray) NSKeyValueCoding.Utility.valueForKey(aSourceObject, masterKey);
        int count = currentValues.count();
        EOEnterpriseObject o;

        for (int i = count - 1; i >= 0; i--) {
            o = (EOEnterpriseObject) currentValues.objectAtIndex(i);

            if (newValues == null || newValues.indexOfIdenticalObject(o) == NSArray.NotFound) { // not found

                if (isDictionary) {
                    currentValues.removeObject(o);
                }
                else {
                    _eo.removeObjectFromBothSidesOfRelationshipWithKey(o, masterKey);
                }

            }

        }

        count = (newValues == null) ? 0 : newValues.count();

        if (isDictionary && currentValues == null) {
            currentValues = new NSMutableArray(count);

            _dictionary.setObjectForKey(currentValues, masterKey);
        }

        for (int i = count - 1; i >= 0; i--) {
            o = (EOEnterpriseObject) newValues.objectAtIndex(i);

            if (currentValues == null || currentValues.indexOfIdenticalObject(o) == NSArray.NotFound) {  // not found

                if (isDictionary) {
                    currentValues.addObject(o);
                }
                else {
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

        // set selections to null if it's an empty array
        if (aValue == null || aValue.count() == 0) {

            if (isMandatory() && theList().count() > 0) {
                Object anObject = theList().objectAtIndex(0);
                aTempValue = new NSArray(anObject);
            }
            else {
                aTempValue = null;
            }

        }

        // deal with isMandatory
        set_privateSelections(aTempValue);
        updateSourceObject(aTempValue);
    }

    public NSArray selections() {

        if (_privateSelections() == null) {
            set_privateSelections((NSArray) NSKeyValueCoding.Utility.valueForKey(_localSourceObject(), _localRelationshipKey()));

            // deal with isMandatory
            if (_privateSelections() == null && _localIsMandatory()) {

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
        if (_privateList() == null) {

            // 81398 sort contents
            aSortedArray = _localDataSource().fetchObjects().mutableClone();

            try {
                _WOJExtensionsUtil._sortEOsUsingSingleKey(aSortedArray, _localDestinationDisplayKey());
            }
            catch (NSComparator.ComparisonException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
            }

            set_privateList(aSortedArray);
        }

        return _privateList();
    }

    public Object theCurrentValue() {
        return NSKeyValueCoding.Utility.valueForKey(theCurrentItem, _localDestinationDisplayKey());
    }

    public boolean isCheckBox() {
        return _localUiStyle().equals("checkbox");
    }
}
