/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import org.apache.log4j.Category;

public class ERXTable extends WOTable {

    public ERXTable(WOContext context) {
        super(context);
    }
    
    ////////////////////////////////////////////////  log4j category  ////////////////////////////////////////////
    public final static Category cat = Category.getInstance(ERXTable.class);

    protected String header;

       
    protected void _resetInternalCaches() {
        super._resetInternalCaches();
        _goingVertically = null;
    }
    
    protected Boolean _goingVertically;
    public boolean goingVertically() {
        if (_goingVertically == null) {
            _goingVertically=ERXUtilities.booleanValue(valueForBinding("goingVertically")) ?
            Boolean.TRUE : Boolean.FALSE;
        }
        return _goingVertically.booleanValue();
    }


    public void pushItem() {
        NSArray aList = list();
        int index;
        if (goingVertically()) {
            int c=aList.count() % maxColumns();
            index = currentRow+rowCount()*currentCol;
            if (c!=0 && currentCol>c) index-=(currentCol-c);
        } else {
            index = currentCol+maxColumns()*currentRow;
        }
        Object item = aList.objectAtIndex(index);
        setValueForBinding(item, "item");
        if (canSetValueForBinding("row"))
            setValueForBinding(ERXConstant.integerForInt(currentRow), "row");
        if (canSetValueForBinding("col"))
            setValueForBinding(ERXConstant.integerForInt(currentCol), "col");
        if (canSetValueForBinding("index"))
            setValueForBinding(ERXConstant.integerForInt(index), "index");
        currentItemIndex++;
    }

    public boolean hasHeaders() { return hasBinding("headerImages"); }
}
