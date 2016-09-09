package er.modern.directtoweb.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSNotificationCenter;

import er.ajax.AjaxUpdateContainer;
import er.directtoweb.ERD2WContainer;
import er.directtoweb.components.ERDCustomComponent;
import er.directtoweb.components.repetitions.ERDAttributeRepetition;
import er.directtoweb.pages.ERD2WPage;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.modern.directtoweb.components.repetitions.ERMDListPageRepetition;

/**
 * Adds a small drop-down menu to list table headers to allow the user a choice
 * of which columns should be displayed. The choices made are persisted via the 
 * ERCoreBusinessLogic preferences system. To enable it, add a rule like the
 * following:
 * 
 * <pre>100 : *true* => showColumnSelector = "true"
 * [com.webobjects.directtoweb.BooleanAssignment]</pre>
 * 
 * By default the component is placed in the right action block and falls back 
 * to the left when no right action block is being displayed.
 *  
 * @author fpeters
 */
public class ERMD2WColumnSelector extends ERDCustomComponent {

    private static final long serialVersionUID = 1L;

    public ERMD2WColumnSelector(WOContext aContext) {
        super(aContext);
    }

    public String aPropertyKey;

    public NSArray sectionsContents() {
        NSArray result = ((ERMDListPageRepetition) parent()).sectionsContents();
        if (result.count() == 1) {
            return result;
        }
        if (result.count() == 0) {
            return NSArray.EmptyArray;
        }
        ERD2WContainer pair = (ERD2WContainer) result.objectAtIndex(0);
        return new NSArray(pair);
    }

    public ERD2WContainer currentSection;

    public NSArray currentSectionKeys() {
        NSArray keys = (NSArray) d2wContext().valueForKey("alternateKeyInfo");
        keys = keys == null ? (NSArray) currentSection.keys : keys;
        return keys;
    }

    public String propertyKey() {
        return (String) d2wContext().valueForKey("propertyKey");
    }

    public void setPropertyKey(String propertyKey) {
        d2wContext().takeValueForKey(propertyKey, "propertyKey");
    }

    public WOActionResults toggleColumnVisibility() {
        String displayVariant = (String) d2wContext().valueForKey(
                ERD2WPage.Keys.displayVariant);
        NSKeyValueCoding userPreferences = (NSKeyValueCoding) d2wContext().valueForKey(
                "userPreferences");
        if (userPreferences != null) {
            displayVariant = (String) userPreferences.valueForKey("displayVariant."
                    + d2wContext().propertyKey() + "."
                    + d2wContext().valueForKey("pageConfiguration"));
        }
        if ("omit".equals(displayVariant)) {
            displayVariant = "default";
        } else {
            displayVariant = "omit";
        }
        NSNotificationCenter.defaultCenter().postNotification(
                ERDAttributeRepetition.DisplayVariantChanged, displayVariant,
                new NSDictionary(d2wContext(), "d2wContext"));
        AjaxUpdateContainer.safeUpdateContainerWithID(
                AjaxUpdateContainer.currentUpdateContainerID(), context());
        return null;
    }

    public String displayNameForProperty() {
        // if(_displayNameForProperty == null) {
        String _displayNameForProperty = (String) d2wContext().valueForKey(
                "displayNameForProperty");
        // }
        return _displayNameForProperty;
    }

    /**
     * Gets the <code>displayVariant</code> for the current property key. The
     * intention is that the display variant allows variation in the display
     * method of property keys without needing different, slightly varying,
     * <code>displayPropertyKeys</code> or <code>tabSectionsContents</code>
     * rules. Template support has been added for the <code>omit</code> and
     * <code>blank</code> variants. One could imagine others, such as
     * <code>collapsed</code>, <code>ajax</code>, etc.
     * 
     * @return the display variant, if specified
     */
    public String displayVariant() {
        String displayVariant = (String) d2wContext().valueForKey(
                ERD2WPage.Keys.displayVariant);
        if (!("omit".equals(displayVariant) || "blank".equals(displayVariant))) {
            // the property is neither omitted nor blanked via the rules,
            // so we let the user decide
            String key = ERD2WPage.Keys.displayVariant + "." + propertyKey();
            NSKeyValueCoding userPreferences = (NSKeyValueCoding) d2wContext()
                    .valueForKey("userPreferences");
            String preference = (String) userPreferences.valueForKey("displayVariant."
                    + d2wContext().propertyKey() + "."
                    + d2wContext().valueForKey("pageConfiguration"));
            if (!ERXStringUtilities.isBlank(preference)) {
                displayVariant = preference;
            }
        }
        return displayVariant;
    }

    public String columnSelectorClass() {
        return displayVariant();
    }
    
    public boolean isShowColumnSelector() {
        return ERXValueUtilities.booleanValueWithDefault(
                d2wContext().valueForKey("showColumnSelector"), false);
    }
}
