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

// dscheck: 5/10/2007
// I changed the component to use the ERXOptionalForm component allowig ERXBatchNavigationBar to also work within an existing form.
// This seems to be a better approach than always having to change the rule to use ERXBatchNavigationBarInForm when a form already exists.
// The component uses a hidden WOImageButton and javascript onChange to simulate clicking the button when the either textfield value is changed.
// This ensures that the default action for an existing form is not invoked when the user changes the value and hits enter.
 
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

/**
 * Better navigation bar<br />
 * 
 * @binding d2wContext
 * @binding displayGroup
 * @binding width
 * @binding objectName
 * @binding border
 * @binding bgcolor
 * @binding textColor
 * @binding sortKeyList
 * @binding showForm
*/

public class ERXBatchNavigationBar extends WOComponent {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXBatchNavigationBar.class);

    /** Contains a string that names the notification posted when the batch size changes */
    public final static String BatchSizeChanged = "BatchSizeChanged";
    private String _buttonId=null;

    /** Public constructor */
    public ERXBatchNavigationBar(WOContext aContext) {
        super(aContext);
    }

    /** component is stateless */
    public boolean isStateless() { return true; }
    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }

    public void reset() {
        super.reset();
        _displayGroup = null;
        _buttonId = null;
    }
    
    public void appendToResponse(WOResponse response, WOContext context) {
        if (displayGroup() != null  &&  ! displayGroup().hasMultipleBatches()) {
            if (currentBatchIndex() != 0) 
                setCurrentBatchIndex(ERXConstant.ZeroInteger);
        }
        super.appendToResponse(response, context);
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
                if (log.isDebugEnabled()) log.debug("The batch index is being set to :"+newValue.intValue());
            }
        }
    }
    
    public void setNumberOfObjectsPerBatch(Number newValue) {
        if (newValue!=null) {
            if (displayGroup()!=null) {
                log.debug("Setting db # of objects per batch to "+newValue);
                displayGroup().setNumberOfObjectsPerBatch(newValue.intValue());

                if(log.isDebugEnabled()) log.debug("The batch index is being set to : "+ 1);
                displayGroup().setCurrentBatchIndex(1);
            }
            Object context=valueForBinding("d2wContext");
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
    

    public int objectCount() {
        if (displayGroup() instanceof ERXBatchingDisplayGroup) {
            return ((ERXBatchingDisplayGroup)displayGroup()).rowCount();
        } else {
            return displayGroup().allObjects().count();
        }
    }

    public WOComponent refresh() {
        return null;
    }

    public String hiddenImageButtonId() {
        if (_buttonId == null)
            _buttonId = context().elementID()+".hiddenBatchNavButton";

        return _buttonId;
    }

    public String simulateHiddenButtonClick() {
        return "return document.getElementById('"+hiddenImageButtonId()+"').click();";
    }

}
