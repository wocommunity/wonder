/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import org.apache.log4j.*;
import er.extensions.*;

public abstract class ERD2WListPage extends D2WListPage {

    /** logging support */
    public final static Category cat = Category.getInstance(ERD2WListPage.class);

    /**
     * Public constructor
     * @param c current context
     */
    public ERD2WListPage(WOContext c) {
        super(c);
    }
    
    public WOComponent printerFriendlyVersion() {
        D2WListPage result=(D2WListPage)ERDirectToWeb.printerFriendlyPageForD2WContext(d2wContext(),session());
        result.setDataSource(dataSource());
        result.displayGroup().setSortOrderings(displayGroup().sortOrderings());
        result.displayGroup().setNumberOfObjectsPerBatch(displayGroup().allObjects().count());
        result.displayGroup().updateDisplayedObjects();
        return result;
    }

    // This will allow d2w pages to be listed on a per configuration basis in stats collecting.
    public String descriptionForResponse(WOResponse aResponse, WOContext aContext) {
        String descriptionForResponse = (String)d2wContext().valueForKey("pageConfiguration");
        /*
        if (descriptionForResponse == null)
            cat.info("Unable to find pageConfiguration in d2wContext: " + d2wContext());
         */
        return descriptionForResponse != null ? descriptionForResponse : super.descriptionForResponse(aResponse, aContext);
    }
    
    // for SmartAttributeAssignment we need object in the context
    public void setObject(EOEnterpriseObject eo) {
        super.setObject(eo);
        d2wContext().takeValueForKey(eo,"object");
    }

    public boolean isEntityReadOnly() {
        Integer isEntityEditable=(Integer)d2wContext().valueForKey("isEntityEditable");
        return  isEntityEditable!=null ? isEntityEditable.intValue()==0 : super.isEntityReadOnly();
    }

    private boolean _hasBeenInitialized=false;

    private Integer _batchSize = null;
    public int numberOfObjectsPerBatch() {
        if (_batchSize == null) {
            NSKeyValueCoding userPreferences=(NSKeyValueCoding)d2wContext().valueForKey("userPreferences");
            if (userPreferences!=null) {
                String key=ERXExtensions.userPreferencesKeyFromContext("batchSize", d2wContext());
                // batchSize prefs are expected in the form vfk batchSize.<pageConfigName>
                Number batchSizePref = (Number)userPreferences.valueForKey(key);
                if (cat.isDebugEnabled()) cat.debug("batchSize User Prefererence: " + batchSizePref);
                if (batchSizePref!=null) _batchSize = ERXConstant.integerForInt(batchSizePref.intValue());
            }
            if (_batchSize == null) {
                if (cat.isDebugEnabled()) {
                    cat.debug("No userPrefs...  Using default values: batchSize = " + d2wContext().valueForKey("defaultBatchSize"));
                }
                _batchSize = ERXConstant.integerForString((String)d2wContext().valueForKey("defaultBatchSize"));
            }
        }
        return _batchSize.intValue();
    } 

    // this can be overridden by subclasses for which sorting has to be fixed (i.e. Grouping Lists)
    public boolean userPreferencesCanSpecifySorting() { return true && !"printerFriendly".equals(d2wContext().valueForKey("subTask")); }
    public NSArray sortOrderings() {
        NSArray sortOrderings=null;
        if (userPreferencesCanSpecifySorting()) {
            NSKeyValueCoding userPreferences=(NSKeyValueCoding)d2wContext().valueForKey("userPreferences");
            if (userPreferences!=null) {
                String key=ERXExtensions.userPreferencesKeyFromContext("sortOrdering", d2wContext());
                // sort ordering prefs are expected in the form vfk sortOrdering.<pageConfigName>
                sortOrderings=(NSArray)userPreferences.valueForKey(key);
                if (cat.isDebugEnabled()) cat.debug("Found sort Orderings in user prefs "+ sortOrderings);
            }
        }
        if (sortOrderings==null) {
            NSArray sortOrderingDefinition=(NSArray)d2wContext().valueForKey("defaultSortOrdering");
            if (sortOrderingDefinition!=null) {
                NSMutableArray so=new NSMutableArray();
                for (int i=0; i< sortOrderingDefinition.count();) {
                    String sortKey=(String)sortOrderingDefinition.objectAtIndex(i++);
                    String sortSelectorKey=(String)sortOrderingDefinition.objectAtIndex(i++);
                    EOSortOrdering sortOrdering=new EOSortOrdering(sortKey,
                                                                   ERXUtilities.sortSelectorWithKey(sortSelectorKey));
                    so.addObject(sortOrdering);
                }
                sortOrderings=so;
                if (cat.isDebugEnabled()) cat.debug("Found sort Orderings in rules "+ sortOrderings);
            }
        }
        return sortOrderings;
    }


    public String defaultSortKey() {
        // the default D2W mechanism is completely disabled
        return null;
    }

    // make kvc happy
    public void setD2wContext(D2WContext newValue) {}
    
    public void setLocalContext(D2WContext newValue) {
        if (ERXExtensions.safeDifferent(newValue,localContext())) {
            _hasBeenInitialized=false;
            _batchSize=null;
            // HACK ALERT: this next line is made necessary by the brain-damageness of
            // D2WComponent.setLocalContext, which holds on to the first non null value it gets.
            // I swear if I could get my hands on the person who did that.. :-)
            _localContext=newValue;
            if (cat.isDebugEnabled()) cat.debug("SetLocalContext "+newValue);
        }
        super.setLocalContext(newValue);
    }

    
    public void takeValuesFromRequest(WORequest r, WOContext c) {
        setupPhase();
        NDC.push("Page: " + getClass().getName()+ (d2wContext()!= null ? (" - Configuration: "+d2wContext().valueForKey("pageConfiguration")) : ""));
        try {
            super.takeValuesFromRequest(r, c);
        } finally {
            NDC.pop();
        }
    }

    public WOActionResults invokeAction(WORequest r, WOContext c) {
        setupPhase();
        WOActionResults result=null;
        NDC.push("Page: " + getClass().getName()+ (d2wContext()!= null ? (" - Configuration: "+d2wContext().valueForKey("pageConfiguration")) : ""));
        try {
            result= super.invokeAction(r, c);
        } finally {
            NDC.pop();
        }
        return result;
    }

    public void appendToResponse(WOResponse r, WOContext c) {
        setupPhase();
        NDC.push("Page: " + getClass().getName()+ (d2wContext()!= null ? (" - Configuration: "+d2wContext().valueForKey("pageConfiguration")) : ""));
        try {
            super.appendToResponse(r,c);
        } finally {
            NDC.pop();
        }
    }

    protected void setupPhase() {
        WODisplayGroup dg=displayGroup();
        if (dg!=null) {
            if (!_hasBeenInitialized) {
                cat.debug("Initializing display group");
                NSArray sortOrderings=sortOrderings();
                displayGroup().setSortOrderings(sortOrderings!=null ? sortOrderings : ERXConstant.EmptyArray);
                displayGroup().setNumberOfObjectsPerBatch(numberOfObjectsPerBatch());
                displayGroup().fetch();
                displayGroup().updateDisplayedObjects();
                _hasBeenInitialized=true;
            }
            // this will have the side effect of resetting the batch # to sth correct, in case
            // the current index if out of range
            cat.debug("dg.currentBatchIndex() "+dg.currentBatchIndex());
            dg.setCurrentBatchIndex(dg.currentBatchIndex());
            if (dg.allObjects().count() > 0)
                d2wContext().takeValueForKey(dg.allObjects().objectAtIndex(0), "object");
        }
    }

    public boolean isEntityInspectable() {
        Integer isEntityInspectable=(Integer)d2wContext().valueForKey("isEntityInspectable");
        return isEntityReadOnly() && (isEntityInspectable!=null && isEntityInspectable.intValue()!=0);
    }

    public WOComponent deleteObjectAction() {
        // FIXME: Shouldn't ave a hard dependency on names here.
        ConfirmPageInterface nextPage = (ConfirmPageInterface)pageWithName("ERQuestionPage2") /* JC_WARNING - Please check: since WO4.5, using pageWithName on the application or session instance requires two parameters (the name of the page and a WOContext). If this pageWithName is in a component or direct action instance, it is valid as is. */;
        nextPage.setConfirmDelegate(new ERDDeletionDelegate(object(),dataSource(),context().page()));
        nextPage.setCancelDelegate(new ERDDeletionDelegate(null,null,context().page()));

        nextPage.setMessage("<B>Are you sure you want to delete the following "+d2wContext().valueForKey("displayNameForEntity")+"</B>:<br> "+object().userPresentableDescription()+ " ?");
        return (WOComponent) nextPage;
    }

    public WOComponent editObjectAction() {
        WOComponent result = null;
        String editConfigurationName=(String)d2wContext().valueForKey("editConfigurationName");
        if(editConfigurationName!=null){
            EditPageInterface epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed(editConfigurationName,session());
            epi.setObject(object());
            epi.setNextPage(context().page());
            return (WOComponent)epi;
        }else{result = super.editObjectAction();}
        return result;
    }

    public WOComponent inspectObjectAction() {
        WOComponent result = null;
        String inspectConfigurationName=(String)d2wContext().valueForKey("inspectConfigurationName");
        if(inspectConfigurationName!=null){
            InspectPageInterface ipi=(InspectPageInterface)D2W.factory().pageForConfigurationNamed(inspectConfigurationName,session());
            ipi.setObject(object());
            ipi.setNextPage(context().page());
            return (WOComponent)ipi;
        }else{result = super.inspectObjectAction();}
        return result;
    }

    public boolean showCancel() { return nextPage()!=null; }

    public boolean isSelectingNotTopLevel(){
        boolean result = false;
        if(isSelecting()&&(parent()!=null))
            result = true;
        return result;
    }

    private String _formTargetJavaScriptUrl;
    public String formTargetJavaScriptUrl() {
        if (_formTargetJavaScriptUrl==null) {
            _formTargetJavaScriptUrl= application().resourceManager().urlForResourceNamed("formTarget.js",
                                                                                          "ERDirectToWeb",
                                                                                          null,
                                                                                          context().request());
        }
        return _formTargetJavaScriptUrl;
    }

    public String targetString(){
        String result = "";
        NSDictionary targetDictionary = (NSDictionary)d2wContext().valueForKey("targetDictionary");
        if(targetDictionary != null){
            StringBuffer buffer = new StringBuffer();
            buffer.append( targetDictionary.valueForKey("targetName")!=null ?
                           targetDictionary.valueForKey("targetName") : "foobar");
            buffer.append(":width=");
            buffer.append( targetDictionary.valueForKey("width")!=null ?
                           targetDictionary.valueForKey("width") : "{window.screen.width/2}");
            buffer.append(", height=");
            buffer.append( targetDictionary.valueForKey("height")!=null ?
                           targetDictionary.valueForKey("height") : "{myHeight}");
            buffer.append(",");
            buffer.append( (targetDictionary.valueForKey("scrollbars")!=null && targetDictionary.valueForKey("scrollbars")== "NO")?
                           " " : "scrollbars");
            buffer.append(", {(isResizable)?'resizable':''}, status");
            result = buffer.toString();
        }else{
            result = "foobar:width={window.screen.width/2}, height={myHeight}, scrollbars, {(isResizable)?'resizable':''}, status";
        }
        return result;
    }

    public boolean shouldShowSelectAll() {
        return displayGroup().allObjects().count()>10;
    }
/*
// FIXME: This needs to be generalized.
public String pageTitle() {
    return "NetStruxr - "+d2wContext().valueForKey("displayNameForEntity")+" List";
}
*/
    public void warmUpForDisplay(){
        //default implementation does nothing
    }

}
