/*
 * WOBatchNavigationBar.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */
 
package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;

public class WOBatchNavigationBar extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public WOBatchNavigationBar(WOContext aContext)  {
        super(aContext);
    }

    @Override
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

    protected String _singularName() {
        String name = (String) valueForBinding("objectName");
        if (name == null || name.length() == 0) {
            name = "item";
        }
        return name;
    }

    protected String _pluralName() {
        String name = (String) valueForBinding("pluralName");
        if (name == null || name.length() == 0) {
            name = _singularName() + "s";
        }
        return name;
    }
        
    public String entityLabel() {
        WODisplayGroup dg = (WODisplayGroup)valueForBinding("displayGroup");
        if (dg.allObjects().count() == 1) {
            return _singularName();
        } else {
            return _pluralName();
        }
    }
}
