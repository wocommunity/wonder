/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

// Corrects two bugs
// 1) User typing null for either number of objects per batch or the page number
// 2) When resetting the number of items per batch the page first page displayed would be the last page.

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import org.apache.log4j.Category;

public class ERXBatchNavigationBar extends WOComponent {

    public ERXBatchNavigationBar(WOContext aContext) {
        super(aContext);
    }

    ////////////////////////////////////////// log4j category  //////////////////////////////////////
    public static final Category cat = Category.getInstance(ERXBatchNavigationBar.class);

    ////////////////////////////////////////// Notification Hooks ///////////////////////////////////
    public final static String BatchSizeChanged = "BatchSizeChanged";
    
    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }

    public void reset() {
        super.reset();
        _displayGroup = null;
    }
    
    private WODisplayGroup _displayGroup;
    public WODisplayGroup displayGroup() {
        if (_displayGroup == null) {
            _displayGroup = (WODisplayGroup)valueForBinding("displayGroup");
        }
        return _displayGroup;
    }
    
    public boolean hasObjectName() { return hasBinding("objectName"); }
    public boolean hasSortKeyList() { return hasBinding("sortKeyList"); }

    public int numberOfObjectsPerBatch() {
        return displayGroup()!=null ? displayGroup().numberOfObjectsPerBatch() : 0;
    }

    public int currentBatchIndex() {
        return displayGroup()!=null ? displayGroup().currentBatchIndex() : 0;        
    }

    public void setCurrentBatchIndex(Number newValue) {
        if (newValue!=null) {
            if (displayGroup()!=null){
                displayGroup().setCurrentBatchIndex(newValue.intValue());
                if (cat.isDebugEnabled()) cat.debug("The batch index is being set to :"+newValue.intValue());
            }
        }
    }
    
    public void setNumberOfObjectsPerBatch(Number newValue) {
        if (newValue!=null) {
            if (displayGroup()!=null) {
                cat.debug("Setting db # of objects per batch to "+newValue);
                displayGroup().setNumberOfObjectsPerBatch(newValue.intValue());
                // For some strange reason when switching the number of items per batch
                // the display group likes to start on the last batch.
                if(cat.isDebugEnabled()) cat.debug("The batch index is being set to :"+0);
                displayGroup().setCurrentBatchIndex(0);
            }
            D2WContext context=(D2WContext)valueForBinding("d2wContext");
            if (context!=null) {
                NSNotificationCenter.defaultCenter().postNotification("BatchSizeChanged",
                                                                      ERXConstant.integerForInt(newValue.intValue()),
                                                                      new NSDictionary(context,"d2wContext"));
            }
        }
    }

    public int filteredObjectsCount() {
        WODisplayGroup dg=displayGroup();
        int result=0;
        EOQualifier q=dg.qualifier();
        if (q!=null) {
            result=EOQualifier.filteredArrayWithQualifier(dg.allObjects(),q).count();
        } else {
            result=dg.allObjects().count();
        }
        return result;
    }
    
}
