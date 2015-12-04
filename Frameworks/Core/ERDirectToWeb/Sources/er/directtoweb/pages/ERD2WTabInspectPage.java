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
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.ERD2WContainer;
import er.directtoweb.ERD2WFactory;
import er.directtoweb.interfaces.ERDTabEditPageInterface;
import er.extensions.components._private.ERXWOForm;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Superclass for all tab and wizard pages.
 * 
 * @d2wKey firstResponder
 * @d2wKey tabComponentName
 */
public class ERD2WTabInspectPage extends ERD2WInspectPage implements ERDTabEditPageInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public final static String WILL_SWITCH_TAB = "willSwitchTab";

    public ERD2WTabInspectPage(WOContext c) {
        super(c);
    }

    /** logging support */
    public static final Logger log = Logger.getLogger(ERD2WTabInspectPage.class);
    public static final Logger validationLog = Logger.getLogger("er.directtoweb.validation.ERD2WTabInspectPage");


    public String switchTabActionName() { return isEditing() ? "switchTabAction" : null; }
    public boolean switchTabAction() {
        boolean switchTab = true;
        if (shouldSaveChangesForTab()) {
            if (validationLog.isDebugEnabled())
                validationLog.debug("Calling tryToSaveChanges");
            switchTab = tryToSaveChanges(true);
        }
        if (switchTab && errorMessages.count()==0 && object().editingContext().hasChanges() && shouldNotSwitchIfHasChanges()) {
            validationFailedWithException(new NSValidation.ValidationException("You currently have changes outstanding.  Please either save or cancel your changes before navigating elsewhere."), null, "editingContextChanges");
        }
        return switchTab && errorMessages.count()==0;
    }

    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context) {
        // ak: this only works in a direct link or if there are no form
        // values...
        String tabName = request.stringFormValueForKey("__tab");
        setTabByName(tabName);
        super.takeValuesFromRequest(request, context);
    }

    //AK: what are these used for? They do nothing?
    protected Integer _tabNumber;

    public Integer tabNumber(){ return _tabNumber;}

    public void setTabNumber(Integer newTabNumber){ _tabNumber  = newTabNumber;}

    @Override
    public WOComponent printerFriendlyVersion() {
        WOComponent result=ERD2WFactory.erFactory().printerFriendlyPageForD2WContext(d2wContext(),session());
        ((EditPageInterface)result).setObject(object());
        return result;
    }

    public void setTabByName(String tabName) {
        if (tabName != null) {
            int i = 0;
            for (ERD2WContainer container : tabSectionsContents()) {
                if (tabName.equals(container.name)) {
                    setTabNumber(Integer.valueOf(i));
                    setCurrentTab(container);
                    break;
                }
                i++;
            }
        }
    }

    @Override
    public String urlForCurrentState() {
        String url = super.urlForCurrentState();
        if (currentTab() != null) {
            // AK: sloppy, I know...
            url = url + "&__tab=" + ERXStringUtilities.urlEncode(currentTab().name);
        }
        return url;
    }

	/**
     * <p>Constructs a JavaScript string that will give a particular field focus when the page is loaded.  If the key
     * <code>firstResponderKey</code> from the d2wContext resolves, the script will attempt to focus on the form field
     * belonging to the property key named by the <code>firstResponderKey</code>.  Otherwise, the script will just focus
     * on the first field in the form.</p>
     *
     * <p>Note that the key <code>useFocus</code> must resolve to <code>true</code> in order for the script to be
     * generated.</p>
     * @return a JavaScript string.
     */
    @Override
    public String tabScriptString() {
		if (d2wContext().valueForKey(Keys.firstResponderKey) != null) {
            return scriptForFirstResponderActivation();
        } else {
            String formName = ERXWOForm.formName(context(), "EditForm");
            String result = "var focusedElement = document.querySelector('form[name=\"" + formName + "\"] input:focus');"
                    // only continue when no form element is focused, yet
                    + "if (focusedElement == undefined) {"
                    + "    var focusableElements = document.querySelectorAll('form[name=\"" + formName + "\"] input');"
                    + "    var qualifiedTypes = ['text', 'textarea'];"
                    + "    for (i = 0; i < focusableElements.length; i++) { "
                    + "        var anElement = focusableElements[i];"
                    + "        if (qualifiedTypes.include(anElement.type.toLowerCase())) {"
                    + "            anElement.focus();"
                    + "            break;"
                    + "        }"
                    + "    }"
                    + "}";
            return result;
        }
    }
 
    private boolean d2wContextValueForKey(String key, boolean defaultValue) {
        return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey(key), defaultValue);
    }
    // These style rules should definately be restricted to tabKeys if needed.
    // CHECKME ak is this default correct?
    public boolean shouldNotSwitchIfHasChanges() {
        return d2wContextValueForKey("shouldNotSwitchIfHasChanges", false);
    }
    // CHECKME ak is this default correct?
    public boolean shouldSaveChangesForTab() {
        return d2wContextValueForKey("shouldSaveChangesForTab", false);
    }
    // CHECKME ak What's all this??? Why not simply show the buttons and en-/disable them??
    public boolean shouldShowNextPreviousButtons() {
        return d2wContextValueForKey("shouldShowNextPreviousButtons", true);
    }
    public boolean shouldShowPreviousButton() {
        return d2wContextValueForKey("shouldShowPreviousButton", !currentTabIsFirstTab());
    }
    public boolean shouldShowNextButton() {
        return d2wContextValueForKey("shouldShowNextButton", !currentTabIsLastTab());
    }

    //CHECKME ak Is this needed? 
    public boolean useSubmitImages() {
        return d2wContextValueForKey("useSubmitImages", false);
    }
    public boolean useTabImages() {
        return d2wContextValueForKey("useTabImages", false);
    }
    public boolean useTabSectionImages() {
        return d2wContextValueForKey("useTabSectionImages", false);
    }
    
    public WOComponent nextTabAction() {
        if (switchTabAction()) {
            int currentIndex = tabSectionsContents().indexOfObject(currentTab());
            if (tabSectionsContents().count() >= currentIndex + 2 && currentIndex >= 0) {
                NSNotificationCenter.defaultCenter().postNotification(WILL_SWITCH_TAB, this);
                setCurrentTab(tabSectionsContents().objectAtIndex(currentIndex + 1));
            }
            else
                log.warn("Attempting to move to next tab when current index is: " + currentIndex + " and tab count: " +
                         tabSectionsContents().count());
        }
        return null;
    }

    public WOComponent previousTabAction() {
        if (switchTabAction()) {
            int currentIndex = tabSectionsContents().indexOfObject(currentTab());
            if (tabSectionsContents().count() >= currentIndex && currentIndex > 0)
                setCurrentTab(tabSectionsContents().objectAtIndex(currentIndex - 1));
            else
                log.warn("Attempting to move to previous tab when current index is: " + currentIndex + " and tab count: " +
                         tabSectionsContents().count());
        }
        return null;
    }

    public boolean currentTabIsFirstTab() {
        return tabSectionsContents() != null && tabSectionsContents().count() > 0 && currentTab() != null ?
        tabSectionsContents().objectAtIndex(0).equals(currentTab()) : false;
    }

    public boolean currentTabIsLastTab() {
        return tabSectionsContents() != null && tabSectionsContents().count() > 0 && currentTab() != null ?
        tabSectionsContents().lastObject().equals(currentTab()) : false;
    }

    public String tabComponentName() {
    	return (String)d2wContext().valueForKey("tabComponentName");
    }
    
    public boolean disablePrevious() {
        return currentTabIsFirstTab();
    }
    public boolean disableNext() {
        return currentTabIsLastTab();
    }
    public boolean disableCancel() {
        return !showCancel();
    }
    public boolean disableSave() {
        return false;
    }
}
