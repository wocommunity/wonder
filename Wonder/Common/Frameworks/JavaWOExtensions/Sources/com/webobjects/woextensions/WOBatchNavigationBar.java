/*
 * WOBatchNavigationBar.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This code not the original code. */
 
package com.webobjects.woextensions;

import java.util.*;
import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

public class WOBatchNavigationBar extends WOComponent {

    public WOBatchNavigationBar(WOContext aContext)  {
        super(aContext);
    }

    public boolean isStateless() {
        return true;
    }

    public boolean hasObjectName() {
        return hasBinding("objectName");
    }

    public boolean hasSortKeyList() {
        return hasBinding("sortKeyList");
    }

    public int numberOfObjectsPerBatch() {

        return ((WODisplayGroup)valueForBinding("displayGroup")).numberOfObjectsPerBatch();
    }

    public void setNumberOfObjectsPerBatch(Integer number) {
        int _number;
        
        //If a negative number is provided we default the number
        //of objects per batch to 0.
        _number = ((number != null) && (number.intValue() > 0)) ? number.intValue() : 0;
        
        ((WODisplayGroup)valueForBinding("displayGroup")).setNumberOfObjectsPerBatch(_number);      
    }

    public int batchIndex() {
        return ((WODisplayGroup)valueForBinding("displayGroup")).currentBatchIndex();
    }

    public void setBatchIndex(Integer index) {
        int _batchIndex;

        //Treat a null index as a 0 index. Negative numbers are handled
        //by the display group.
        _batchIndex = (index != null) ? index.intValue() : 0;

        ((WODisplayGroup)valueForBinding("displayGroup")).setCurrentBatchIndex(_batchIndex);       
    }
}