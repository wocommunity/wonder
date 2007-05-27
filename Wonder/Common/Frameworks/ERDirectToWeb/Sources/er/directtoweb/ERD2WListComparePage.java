/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

public class ERD2WListComparePage extends ERD2WListPage {

    /**
     * Public constructor
     * @param context current context
     */
    public ERD2WListComparePage(WOContext context) { super(context); }

    public int index, d2wPropertyKeyIndex, col;

    private NSMutableArray _compareList;
    public NSArray compareList() {
        if (_compareList == null) {
            NSArray list = displayGroup().displayedObjects();
            _compareList = new NSMutableArray();
            if (list != null && list.count() > 0) {
                _compareList.addObject(list.objectAtIndex(0));
                Object o=comparisonObject();
                if (o!=null) _compareList.addObject(o);
                _compareList.addObjectsFromArray(list);
            }
        }
        return _compareList;
    }

    private Object _comparisonObject=NSKeyValueCoding.NullValue;
    private EOEditingContext _comparisonObjectEditingContext;
    public Object comparisonObject() {
        if (_comparisonObject==NSKeyValueCoding.NullValue) {
            _comparisonObject=d2wContext().valueForKey("comparisonObject");
            // we retain the EC -- bug #3975
            if (_comparisonObject!=null && _comparisonObject instanceof EOEnterpriseObject)
                _comparisonObjectEditingContext=((EOEnterpriseObject)_comparisonObject).editingContext();
        }
        return _comparisonObject;
    }

    public boolean shouldMapPropertyKey() {
        return col == 1 && comparisonObject()!=null;
    }

    private NSArray _displayPropertyKeys;
    public NSArray displayPropertyKeys() {
        if (_displayPropertyKeys == null) {
            _displayPropertyKeys = (NSArray)d2wContext().valueForKey("displayPropertyKeys");
        }
        return _displayPropertyKeys;
    }

    public boolean showDisplayProperty() { return col == 0; }

    public void setD2wPropertyKeyIndex(int newD2wPropertyKeyIndex) {
        if (newD2wPropertyKeyIndex != d2wPropertyKeyIndex) {
            d2wPropertyKeyIndex=newD2wPropertyKeyIndex;
            if (d2wPropertyKeyIndex < displayPropertyKeys().count())
                d2wContext().takeValueForKey(displayPropertyKeys().objectAtIndex(d2wPropertyKeyIndex), "propertyKey");
        }
    }

    public void prepare() {
        _compareList = null;
        d2wPropertyKeyIndex=-1;
        d2wContext().takeValueForKey(null, "propertyKey");
    }

    public void appendToResponse(WOResponse response, WOContext context) {
        prepare();
        super.appendToResponse(response, context);
    }
}
