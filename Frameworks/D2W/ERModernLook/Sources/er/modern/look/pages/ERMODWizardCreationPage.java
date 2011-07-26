package er.modern.look.pages;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotificationCenter;

import er.directtoweb.ERD2WContainer;
import er.directtoweb.pages.ERD2WWizardCreationPage;
import er.directtoweb.pages.templates.ERD2WWizardCreationPageTemplate;
import er.extensions.ERXExtensions;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.foundation.ERXValueUtilities;

/**
 * A wizard inspect/edit template. Can be used in-line, and supports ajax updates<br />
 * 
 * @d2wKey cancelButtonLabel
 * @d2wKey bannerFileName
 * @d2wKey showBanner
 * @d2wKey headerComponentName
 * @d2wKey formEncoding
 * @d2wKey repetitionComponentName
 * @d2wKey previousButtonLabel
 * @d2wKey pageWrapperName
 * @d2wKey nextButtonLabel
 * @d2wKey saveButtonLabel
 * @d2wKey useAjaxControlsWhenEmbedded
 * 
 * @author davidleber
 */
public class ERMODWizardCreationPage extends ERD2WWizardCreationPageTemplate {
	
	public interface Keys extends ERD2WWizardCreationPageTemplate.Keys{
		public static final String useAjaxControlsWhenEmbedded = "useAjaxControlsWhenEmbedded";
		public static final String parentPageConfiguration = "parentPageConfiguration";
		public static final String idForParentMainContainer = "idForParentMainContainer";
		public static final String idForMainContainer = "idForMainContainer";
	}
	
	public boolean showCancelDialog;
	
	public ERMODWizardCreationPage(WOContext wocontext) {
		super(wocontext);
	}
	
	@Override
	public void awake() {
		super.awake();
		clearValidationFailed();
	}
	
	
	/**
	 * Change to the previous step. Overridden to set _currentStep here instead of in appendToResponse 
	 * so ajax requests work.
	 */
	@Override
	@SuppressWarnings("unchecked")
    public WOComponent previousStep() {
        // if we had an error message and are going back, we don't want the message
        // to show up on the previous page; the error message will reappear
        // when the user gets back to the initial page
        errorMessages = new NSMutableDictionary();
        if (showPrevious() && _currentStep > 1) _currentStep--;
        setCurrentTab((ERD2WContainer)tabSectionsContents().objectAtIndex(_currentStep-1));
        return null;
    }
	
	/**
	 * Change to the next step. Overridden to set _currentStep here instead of in appendToResponse 
	 * so ajax requests work
	 */
	@Override
    public WOComponent nextStep() {
        // FIXME: This is no longer needed.  We now have validationKeys that will serve the same purpose.
        NSNotificationCenter.defaultCenter().postNotification(ERD2WWizardCreationPage.WILL_GOTO_NEXT_PAGE, null);
        if (errorMessages.count()==0 && _currentStep < tabSectionsContents().count())
            _currentStep++;
        
        setCurrentTab((ERD2WContainer)tabSectionsContents().objectAtIndex(_currentStep-1));
        return null;
    }
	
	/**
	 * Perform the cancel action. Overridden to handle showing a cancel dialog in-line if 
	 * useAjaxControlsWhenEmbedded is true.
	 */
	@Override
	public WOComponent cancelAction() {
		boolean useAjax = ERXValueUtilities.booleanValue(d2wContext().valueForKey(Keys.useAjaxControlsWhenEmbedded));
		if (useAjax && showCancel()) {
			if (_currentStep>1 && ERXEOControlUtilities.isNewObject(object())) { 
				showCancelDialog = true;
				return null;
			} else {
				return super.superCancelAction();
			}
		} else {
			return super.cancelAction();
		}
	}
	
	/**
	 * Action called when the cancel button is clicked.
	 */
	public WOComponent doCancelAction() {
		showCancelDialog = false;
		return super.superCancelAction();
	}
	
	/**
	 * Action called when the cancel dialog is dismissed.
	 */
	public WOActionResults dismissCancelDialogAction() {
		showCancelDialog = false;
		return null;
	}
	
	/**
	 * Show the cancel button. From parent: Should we show the cancel button? It's only visible 
	 * when we have a nextPage set up. Overridden to allow us to show the cancel button if the 
	 * page is embedded and shouldShowCancelButton is true.
	 */
	@Override
    public boolean showCancel() {
        return ((_nextPageDelegate != null || _nextPage != null) || d2wContext().valueForKey(Keys.parentPageConfiguration) != null) && shouldShowCancelButton();
    }
	
	/**
	 * Sets the page object. Overridden to reset the current step if the object changes
	 */
	@Override
	public void setObject(EOEnterpriseObject eoenterpriseobject) {
		// If we are getting a new EO, then reset the current step.
		if (eoenterpriseobject != null && !eoenterpriseobject.equals(object())) {
			_currentStep = 1;
		}
		super.setObject(eoenterpriseobject);
	}

	/**
	 * Return the ajax update container id for the cancel button.
	 */
	public String cancelUpdateContainerID() {
		Object result = null;
		if (!showCancelDialog)
			result = d2wContext().valueForKey(Keys.idForParentMainContainer);
		else 
			result = d2wContext().valueForKey(Keys.idForMainContainer);
		return (String)result;
	}
	
	/**
	 * Performs submit action. Overridden to reset the nested validation setting on the
	 * object.
	 */
	// FIXME - Is this needed here? davidleber
	@Override
	public WOComponent submitAction() throws Throwable {
		WOComponent result = super.submitAction();
		if (object() instanceof ERXGenericRecord) {
			((ERXGenericRecord)object()).setValidatedWhenNested(true);
		}
		return result;
	}
	
	
	// What follows is a hack.
	// I am not proud of it, but there it is.
	// This is necessary because the wizard component will blow chunks
	// if you don't clear its tabSectionContents if the d2wContext's entity
	// changes when embedded, in-line, and updated with ajax requests.
	// davidleber
	
	private EOEntity _cachedEntity;
	
	@Override
	public D2WContext d2wContext() {
		D2WContext result = super.d2wContext();
		if (_cachedEntity == null) {
			_cachedEntity = result.entity();
		} else if (ERXExtensions.safeDifferent(_cachedEntity, result.entity())) {
			clearTabSectionsContents();
			_cachedEntity = result.entity();
		}
		return super.d2wContext();
	}

}
