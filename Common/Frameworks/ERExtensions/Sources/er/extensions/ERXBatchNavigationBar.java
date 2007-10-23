/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotificationCenter;

/**
 * Better batch navigation bar to page thtough display groups.<br />
 * <ul>
 * <li>User typing null for either number of objects per batch or the page number</li>
 * <li>When resetting the number of items per batch the page first page displayed would be the last page.</li>
 * <li>Broadcasts a notification when the batch size changes</li>
 * <li>Has go first/go last methods/li>
 * <li>Has option to not clear the selection when paging, which WODisplayGroup does when calling displayNext()/displayPrevious()</li>
 * <li>Broadcasts a notification when the batch size changes</li>
 * <li>Can be used inside or outside of a <code>form</code>
 * <li>Graphics can be easily configured
 * <li>Has localization support, both for static texts and the object count.
 * </ul>
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
 * @binding clearSelection boolean that indicates if the selection should be reset on paging (default false)
 */
public class ERXBatchNavigationBar extends ERXStatelessComponent {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXBatchNavigationBar.class);

    /** Contains a string that names the notification posted when the batch size changes */
    public final static String BatchSizeChanged = "BatchSizeChanged";

    public boolean wasInForm;
    private String _threadStorageKey;
    
    /** Public constructor */
    public ERXBatchNavigationBar(WOContext aContext) {
        super(aContext);
    }
    
    public void awake() {
    	super.awake();
    	wasInForm = context().isInForm();
    	_threadStorageKey = "ERXBatchNavigationBar_numberOfObjectsPerBatch_" + context().elementID();
    }

    public void reset() {
        super.reset();
        _displayGroup = null;
        _threadStorageKey=null;
    }
    
    public void appendToResponse(WOResponse response, WOContext context) {
    	// set the numberOfObjectsPerBatch
        Number newNumberOfObjectsPerBatch = (Number) ERXThreadStorage.valueForKey(_threadStorageKey);
        if (newNumberOfObjectsPerBatch != null && newNumberOfObjectsPerBatch.intValue() != displayGroup().numberOfObjectsPerBatch()) {
        	if (displayGroup()!=null) {
            	NSArray selection = selection();
                
                if(log.isDebugEnabled()) log.debug("Setting db # of objects per batch to "+newNumberOfObjectsPerBatch);
                displayGroup().setNumberOfObjectsPerBatch(newNumberOfObjectsPerBatch.intValue());

                if(log.isDebugEnabled()) log.debug("The batch index is being set to : "+ 1);
                displayGroup().setCurrentBatchIndex(1);
                clearSelection(selection);
            }
            Object d2wcontext=valueForBinding("d2wContext");
            if (d2wcontext!=null) {
                NSNotificationCenter.defaultCenter().postNotification("BatchSizeChanged",
                                                                      ERXConstant.integerForInt(newNumberOfObjectsPerBatch.intValue()),
                                                                      new NSDictionary(d2wcontext,"d2wContext"));
            }
            ERXThreadStorage.takeValueForKey(null, _threadStorageKey);
        }
        
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
    	ERXThreadStorage.takeValueForKey(newValue, _threadStorageKey);
    }

    public int filteredObjectsCount() {
        WODisplayGroup dg=displayGroup();
        if (dg instanceof ERXDisplayGroup) {
			ERXDisplayGroup erxdg = (ERXDisplayGroup) dg;
			return erxdg.filteredObjects().count();
		}
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

    protected NSArray selection() {
    	return displayGroup().selectedObjects();
    }

    protected void clearSelection(NSArray selection) {
    	if(booleanValueForBinding("clearSelection", false)) {
    		displayGroup().setSelectedObjects(NSArray.EmptyArray);
    	} else {
    		// displayGroup().setSelectedObject(selection);
    	}
    }

    public WOComponent displayNextBatch() {
    	if (displayGroup() != null && displayGroup().numberOfObjectsPerBatch() != 0) {
    		NSArray selection = selection();
    		displayGroup().setCurrentBatchIndex(displayGroup().currentBatchIndex() + 1);
       		clearSelection(selection);
    	}
    	return context().page();
    }

    public WOComponent displayPreviousBatch() {
    	if (displayGroup() != null && displayGroup().numberOfObjectsPerBatch() != 0) {
    		NSArray selection = selection();
    		displayGroup().setCurrentBatchIndex(displayGroup().currentBatchIndex() - 1);
       		clearSelection(selection);
    	}
    	return context().page();
    }
   
    public WOComponent displayFirstBatch() {
    	if (displayGroup() != null && displayGroup().numberOfObjectsPerBatch() != 0) {
    		NSArray selection = selection();
    		displayGroup().setCurrentBatchIndex(1);
       		clearSelection(selection);
    	}
    	return context().page();
    }

    public WOComponent displayLastBatch() {
    	if (displayGroup() != null && displayGroup().numberOfObjectsPerBatch() != 0) {
    		NSArray selection = selection();
    		displayGroup().setCurrentBatchIndex(displayGroup().batchCount());
       		clearSelection(selection);
    	}
    	return context().page();
    }

    public String formTarget() {
    	if(wasInForm) {
    		return ERXWOForm.formName(context(), "EditForm") + ".target='_self';";
    	}
    	return null;
    }
}
