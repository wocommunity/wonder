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

import org.apache.log4j.Logger;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * Better navigation bar<br />
 * 
 * @binding d2wContext the D2W context that this component is in
 * @binding displayGroup the WODisplayGroup that is being controlled
 * @binding width the width of the navigation bar table (there is a minimum 500 pixel width if tableClass is not specified)
 * @binding objectName the name of the type of object that is contained in the WODisplayGroup
 * @binding border the border width of the navigation bar table
 * @binding bgcolor the background color of the navigation bar table
 * @binding textColor no longer used?
 * @binding sortKeyList an NSArray of sort key paths that will be displayed in a popup button
 * @binding tableClass the CSS class for the navigation table (overrides minimum 500 pixel width when set)
 * @binding imageFramework the name of the framework that contains the navigation arrow images
 * @binding leftArrowImage the name of the left navigation arrow image
 * @binding rightArrowImage the name of the right navigation arrow image
 */
public class ERXBatchNavigationBar extends WOComponent {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXBatchNavigationBar.class);

    /** Contains a string that names the notification posted when the batch size changes */
    public final static String BatchSizeChanged = "BatchSizeChanged";

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
    }
    
    public void appendToResponse(WOResponse response, WOContext context) {
        // set the numberOfObjectsPerBatch
        
        if (newNumberOfObjectsPerBatch!=null && newNumberOfObjectsPerBatch.intValue() != displayGroup().numberOfObjectsPerBatch()) {
            if (displayGroup()!=null) {
                if(log.isDebugEnabled()) log.debug("Setting db # of objects per batch to "+newNumberOfObjectsPerBatch);
                displayGroup().setNumberOfObjectsPerBatch(newNumberOfObjectsPerBatch.intValue());

                if(log.isDebugEnabled()) log.debug("The batch index is being set to : "+ 1);
                displayGroup().setCurrentBatchIndex(1);
            }
            Object d2wcontext=valueForBinding("d2wContext");
            if (d2wcontext!=null) {
                NSNotificationCenter.defaultCenter().postNotification("BatchSizeChanged",
                                                                      ERXConstant.integerForInt(newNumberOfObjectsPerBatch.intValue()),
                                                                      new NSDictionary(d2wcontext,"d2wContext"));
            }
        }
        newNumberOfObjectsPerBatch = null;
        
        if (displayGroup() != null  &&  ! displayGroup().hasMultipleBatches()) {
            if (currentBatchIndex() != 0) 
                setCurrentBatchIndex(ERXConstant.ZeroInteger);
        }
        super.appendToResponse(response, context);
    }
    
    private WODisplayGroup _displayGroup;

    private Number newNumberOfObjectsPerBatch;
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
        newNumberOfObjectsPerBatch = newValue;
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
        return displayGroup().allObjects().count();
    }
    
    public String imageFramework() {
      String imageFramework;
      if (!hasBinding("imageFramework")) {
        imageFramework = "JavaWOExtensions";
      }
      else {
        imageFramework = (String)valueForBinding("imageFramework");
      }
      return imageFramework;
    }
    
    public String leftArrowImage() {
      String leftArrowImageName;
      if (!hasBinding("leftArrowImage")) {
        leftArrowImageName = "lft-OSarw.gif";
      }
      else {
        leftArrowImageName = (String)valueForBinding("leftArrowImage");
      }
      return leftArrowImageName;
    }
    
    public String rightArrowImage() {
      String rightArrowImageName;
      if (!hasBinding("rightArrowImage")) {
        rightArrowImageName = "rt-OSarw.gif";
      }
      else {
        rightArrowImageName = (String)valueForBinding("rightArrowImage");
      }
      return rightArrowImageName;
    }
}
