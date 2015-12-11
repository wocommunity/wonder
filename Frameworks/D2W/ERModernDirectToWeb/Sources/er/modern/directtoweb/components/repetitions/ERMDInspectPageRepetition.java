package er.modern.directtoweb.components.repetitions;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver._private.WOGenericContainer;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSNotificationCenter;

import er.ajax.AjaxUpdateContainer;
import er.directtoweb.components.repetitions.ERDInspectPageRepetition;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.modern.directtoweb.components.ERMDAjaxNotificationCenter;

/**
 * Modern tableless inspect/edit page repetition
 * 
 * @d2wKey componentName
 * @d2wKey propertyNameComponentName
 * @d2wKey sectionComponentName
 * @d2wKey baseClassForLine
 * @d2wKey pageType
 * @d2wKey displayNameForProperty
 * @d2wKey classForSection
 * @d2wKey classForAttributeValue
 * @d2wKey classForLabelSpan
 * @d2wKey classForEmptyLabelSpan
 * @d2wKey classForAttributeRepetitionWrapper
 * 
 * @author davidleber
 */
public class ERMDInspectPageRepetition extends ERDInspectPageRepetition {
	
    private static final long serialVersionUID = 1L;

    public int index;
	
    public ERMDInspectPageRepetition(WOContext context) {
        super(context);
    }
    
	// LINE
	
    /**
     * CSS class for the current line in the repetition.
     * <p>
     * Examples:
     * <p>
     * "Line OddLine FirstLine InspectLine Attribute1Line"
     * "Line EvenLine InspectLine Attribute2Line"
     * "Line OddLine InspectLine Attribute3Line"
     * "Line EvenLine LastLine InspectLine Attribute4Line"
     * <p>
     * "Line OddLine FirstLine EditLine Attribute1Line ErrorLine"
     * 
     * @return String css class derived from rules and position
     */
	public String lineDivClass() {
		String lineBase = (String)d2wContext().valueForKey("baseClassForLine");
		String evenessAndPosition = "Even" + lineBase;
		int lastIndex = currentSectionKeys().count() - 1;
		if (index %2 == 0) {
			evenessAndPosition = "Odd" + lineBase;
		}
		if (index == 0) {
			evenessAndPosition += " First" + lineBase;
		} else if (index == lastIndex) {
			evenessAndPosition += " Last" + lineBase;
		}
		String error = hasNoErrors() ? "" : " Error" + lineBase;
		return lineBase + " " + evenessAndPosition + " " + d2wContext().valueForKey("pageType") + lineBase + " " + ERXStringUtilities.capitalize(propertyKey()) + lineBase + error;
	}

	
	// ERRORS //
	
    public boolean hasNoErrors() {
      //    if(false) {
      //        String keyPath = "errorMessages." + displayNameForProperty();
      //        return d2wContext().valueForKeyPath(keyPath) == null;
      //    }
        return !validationExceptionOccurredForPropertyKey();
    }
    
    public String displayNameForProperty() {
    	return (String)d2wContext().valueForKey("displayNameForProperty");
    }
    
    public boolean validationExceptionOccurredForPropertyKey() {
        if (d2wContext().propertyKey() == null) {
            return false;
        } else {
            String propertyKey = d2wContext().propertyKey();
            boolean contains = keyPathsWithValidationExceptions().containsObject(propertyKey);
            return contains;
        }
    }
    
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public NSArray<String> keyPathsWithValidationExceptions() {
        NSArray exceptions = (NSArray)d2wContext().valueForKey("keyPathsWithValidationExceptions");
        return exceptions != null ? exceptions : NSArray.EmptyArray;
    }
    
    // AJAX notification center support
	
	public boolean isDependent() {
	    return ERXValueUtilities.booleanValueWithDefault(
	            d2wContext().valueForKey("isDependent"), false);
	}
	
	public boolean shouldObserve() {
	    return ERXValueUtilities.booleanValueWithDefault(
	            d2wContext().valueForKey("shouldObserve"), false);
	}

	public String lineDivId() {
	    String lineDivId = null;
	    // only needed if this is a dependent property
	    if (isDependent()) {
	        String pageConfiguration = (String) d2wContext().valueForKey(
	                "pageConfiguration");
	        lineDivId = pageConfiguration
	                + ERXStringUtilities.capitalize(propertyKey()).replaceAll("\\.", "_")
	                + "LineUC";
	    }
	    return lineDivId;
	}

    /**
     * If the current property key is depending on an observed property key, we
     * surround it with an update container.
     * 
     * @return the component name to use as the line div
     */
    public String lineDivComponentName() {
        String lineDivComponentName = WOGenericContainer.class.getSimpleName();
        if (isDependent()) {
            lineDivComponentName = AjaxUpdateContainer.class.getSimpleName();
        }
        return lineDivComponentName;
    }

    /**
     * Posts a change notification when an observed property key has changed.
     */
    public void postChangeNotification() {
        NSNotificationCenter.defaultCenter().postNotification(
                ERMDAjaxNotificationCenter.PropertyChangedNotification, d2wContext());
    }
}