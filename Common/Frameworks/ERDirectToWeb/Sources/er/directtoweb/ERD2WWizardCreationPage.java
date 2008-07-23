/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import java.util.*;
import er.extensions.*;

public class ERD2WWizardCreationPage extends ERD2WTabInspectPage {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger("er.directtoweb.templates.ERWizardCreationPageTemplate");

    // Notification titles
    // FIXME: This is silly now that we have validationKeys.  Once all referenecs are removed will delete.
    public final static String WILL_GOTO_NEXT_PAGE = "willGotoNextPage";
    private Object _dummy;
    private NSArray _subList; // used by the grouping repetition
    private Object section;
    protected int _currentStep=1;

    public ERD2WWizardCreationPage(WOContext context) {
        super(context);
    }

    public int currentStep() { return _currentStep; }
    
    public boolean showPrevious() { return _currentStep>1 && shouldShowPreviousButton(); }

    public boolean showNext() { return _currentStep< tabSectionsContents().count() && shouldShowNextButton(); }

    public WOComponent nextStep() {
        // FIXME: This is no longer needed.  We now have validationKeys that will serve the same purpose.
        NSNotificationCenter.defaultCenter().postNotification(ERD2WWizardCreationPageTemplate.WILL_GOTO_NEXT_PAGE, null);
        if (errorMessages.count()==0 && _currentStep < tabSectionsContents().count())
            _currentStep++;
        return null;
    }

    public WOComponent previousStep() {
        // if we had an error message and are going back, we don't want the message
        // to show up on the previous page; the error message will reappear
        // when the user gets back to the initial page
        errorMessages = new NSMutableDictionary();
        if (showPrevious() && _currentStep > 1) _currentStep--;
        return null;
    }

    // Setting the tab has the effect of setting the tabKey in the d2wContext.
    public void appendToResponse(WOResponse response, WOContext context) {
        setCurrentTab((ERD2WContainer)tabSectionsContents().objectAtIndex(_currentStep-1));
        super.appendToResponse(response, context);
    }

    /*
     public String displayedTabName() {
         return (String)tabs().objectAtIndex(_currentStep-1);
     }

     public NSArray tabKeys() {
         return (NSArray)tabContents().objectForKey(displayedTabName());
     }

     public String defaultRowspan () {
         return ""+(tabKeys().count()+4);
     }
     */
    public WOComponent printerFriendlyVersion() {
        WOComponent result=ERD2WFactory.erFactory().printerFriendlyPageForD2WContext(d2wContext(),session());
        ((EditPageInterface)result).setObject(object());
        return result;
    }

    public String tabScriptString() {
        return "var elem = document.EditForm.elements[0];"+
        "if (elem!=null && (elem.type == 'text' || elem.type ==  'area')) elem.focus();";
    }


    public WOComponent cancelAction() {
        WOComponent result=null;
        if (_currentStep>1 && ERXEOControlUtilities.isNewObject(object())) { // only show this if we've been through more than one page
            ConfirmPageInterface cpi = (ConfirmPageInterface)D2W.factory().pageForConfigurationNamed("ConfirmCancelCreationOf" + entityName(),
                                                                                                     session());
            cpi.setCancelDelegate(new ERDPageDelegate(context().page()));
            cpi.setConfirmDelegate(new _confirmCancellationDelegate());
            cpi.setMessage((String)d2wContext().valueForKey("cancelMessage"));
            if(cpi instanceof InspectPageInterface) {
                ((InspectPageInterface)cpi).setObject(object());
            }
            result=(WOComponent)cpi;
        } else {
            result=superCancelAction();
        }
        return result;
    }
    public WOComponent superCancelAction() {
        return super.cancelAction();
    }


    // !! note this inner class is not static, which may cause cycles and leaks
    class _confirmCancellationDelegate implements NextPageDelegate {
        public WOComponent nextPage(WOComponent sender) {
            return ERD2WWizardCreationPage.this.superCancelAction();
        }
    }


}
