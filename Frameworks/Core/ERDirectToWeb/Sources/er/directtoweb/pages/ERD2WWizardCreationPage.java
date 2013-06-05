/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.ConfirmPageInterface;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotificationCenter;

import er.directtoweb.ERD2WFactory;
import er.directtoweb.delegates.ERDPageDelegate;
import er.extensions.eof.ERXEOControlUtilities;

/**
 * @d2wKey cancelMessage
 */
public class ERD2WWizardCreationPage extends ERD2WTabInspectPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public static final Logger log = Logger.getLogger("er.directtoweb.templates.ERWizardCreationPageTemplate");

    // Notification titles
    // FIXME: This is silly now that we have validationKeys.  Once all referenecs are removed will delete.
    public final static String WILL_GOTO_NEXT_PAGE = "willGotoNextPage";
    protected int _currentStep=1;

    public ERD2WWizardCreationPage(WOContext context) {
        super(context);
    }

    public int currentStep() { return _currentStep; }
    
    public boolean showPrevious() { return _currentStep>1 && shouldShowPreviousButton(); }

    public boolean showNext() { return _currentStep< tabSectionsContents().count() && shouldShowNextButton(); }

    public WOComponent nextStep() {
        // FIXME: This is no longer needed.  We now have validationKeys that will serve the same purpose.
        NSNotificationCenter.defaultCenter().postNotification(ERD2WWizardCreationPage.WILL_GOTO_NEXT_PAGE, null);
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
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        setCurrentTab(tabSectionsContents().objectAtIndex(_currentStep-1));
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
    @Override
    public WOComponent printerFriendlyVersion() {
        WOComponent result=ERD2WFactory.erFactory().printerFriendlyPageForD2WContext(d2wContext(),session());
        ((EditPageInterface)result).setObject(object());
        return result;
    }

    @Override
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
            return superCancelAction();
        }
    }


}
